package com.mongodb.connection;

import com.mongodb.async.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;
import java.util.concurrent.*;
import com.mongodb.assertions.*;
import org.bson.*;
import com.mongodb.event.*;
import com.mongodb.internal.async.*;
import java.net.*;
import java.io.*;
import java.nio.channels.*;
import com.mongodb.*;
import org.bson.io.*;
import java.util.*;
import com.mongodb.diagnostics.logging.*;

class InternalStreamConnection implements InternalConnection
{
    private final ServerId serverId;
    private final StreamFactory streamFactory;
    private final InternalConnectionInitializer connectionInitializer;
    private final ConnectionListener connectionListener;
    private final Lock writerLock;
    private final Lock readerLock;
    private final Deque<SendMessageRequest> writeQueue;
    private final Map<Integer, SingleResultCallback<ResponseBuffers>> readQueue;
    private final Map<Integer, ResponseBuffers> messages;
    private boolean isWriting;
    private boolean isReading;
    private final AtomicReference<CountDownLatch> readingPhase;
    private volatile MongoException exceptionThatPrecededStreamClosing;
    private volatile ConnectionDescription description;
    private volatile Stream stream;
    private final AtomicBoolean isClosed;
    private final AtomicBoolean opened;
    static final Logger LOGGER;
    
    InternalStreamConnection(final ServerId serverId, final StreamFactory streamFactory, final InternalConnectionInitializer connectionInitializer, final ConnectionListener connectionListener) {
        this.writerLock = new ReentrantLock(false);
        this.readerLock = new ReentrantLock(false);
        this.writeQueue = new ArrayDeque<SendMessageRequest>();
        this.readQueue = new HashMap<Integer, SingleResultCallback<ResponseBuffers>>();
        this.messages = new ConcurrentHashMap<Integer, ResponseBuffers>();
        this.readingPhase = new AtomicReference<CountDownLatch>(new CountDownLatch(1));
        this.isClosed = new AtomicBoolean();
        this.opened = new AtomicBoolean();
        this.serverId = Assertions.notNull("serverId", serverId);
        this.streamFactory = Assertions.notNull("streamFactory", streamFactory);
        this.connectionInitializer = Assertions.notNull("connectionInitializer", connectionInitializer);
        this.connectionListener = new ErrorHandlingConnectionListener(Assertions.notNull("connectionListener", connectionListener));
        this.description = new ConnectionDescription(serverId);
    }
    
    @Override
    public ConnectionDescription getDescription() {
        return this.description;
    }
    
    @Override
    public void open() {
        Assertions.isTrue("Open already called", this.stream == null);
        this.stream = this.streamFactory.create(this.serverId.getAddress());
        try {
            this.stream.open();
            this.description = this.connectionInitializer.initialize(this);
            this.opened.set(true);
            this.connectionListener.connectionOpened(new ConnectionOpenedEvent(this.getId()));
            InternalStreamConnection.LOGGER.info(String.format("Opened connection [%s] to %s", this.getId(), this.serverId.getAddress()));
        }
        catch (Throwable t) {
            this.close();
            if (t instanceof MongoException) {
                throw (MongoException)t;
            }
            throw new MongoException(t.toString(), t);
        }
    }
    
    @Override
    public void openAsync(final SingleResultCallback<Void> callback) {
        Assertions.isTrue("Open already called", this.stream == null, callback);
        try {
            this.stream = this.streamFactory.create(this.serverId.getAddress());
        }
        catch (Throwable t) {
            callback.onResult(null, t);
            return;
        }
        this.stream.openAsync(new AsyncCompletionHandler<Void>() {
            @Override
            public void completed(final Void aVoid) {
                InternalStreamConnection.this.connectionInitializer.initializeAsync(InternalStreamConnection.this, new SingleResultCallback<ConnectionDescription>() {
                    @Override
                    public void onResult(final ConnectionDescription result, final Throwable t) {
                        if (t != null) {
                            InternalStreamConnection.this.close();
                            callback.onResult(null, t);
                        }
                        else {
                            InternalStreamConnection.this.description = result;
                            InternalStreamConnection.this.opened.set(true);
                            InternalStreamConnection.this.connectionListener.connectionOpened(new ConnectionOpenedEvent(InternalStreamConnection.this.getId()));
                            if (InternalStreamConnection.LOGGER.isInfoEnabled()) {
                                InternalStreamConnection.LOGGER.info(String.format("Opened connection [%s] to %s", InternalStreamConnection.this.getId(), InternalStreamConnection.this.serverId.getAddress()));
                            }
                            callback.onResult(null, null);
                        }
                    }
                });
            }
            
            @Override
            public void failed(final Throwable t) {
                callback.onResult(null, t);
            }
        });
    }
    
    @Override
    public void close() {
        if (!this.isClosed.getAndSet(true)) {
            if (InternalStreamConnection.LOGGER.isDebugEnabled()) {
                InternalStreamConnection.LOGGER.debug(String.format("Closing connection %s", this.getId()));
            }
            if (this.stream != null) {
                this.stream.close();
            }
            this.connectionListener.connectionClosed(new ConnectionClosedEvent(this.getId()));
        }
    }
    
    @Override
    public boolean opened() {
        return this.opened.get();
    }
    
    @Override
    public boolean isClosed() {
        return this.isClosed.get();
    }
    
    @Override
    public void sendMessage(final List<ByteBuf> byteBuffers, final int lastRequestId) {
        Assertions.notNull("stream is open", this.stream);
        if (this.isClosed()) {
            throw new MongoSocketClosedException("Cannot write to a closed stream", this.getServerAddress());
        }
        this.writerLock.lock();
        try {
            final int messageSize = this.getMessageSize(byteBuffers);
            this.stream.write(byteBuffers);
            this.connectionListener.messagesSent(new ConnectionMessagesSentEvent(this.getId(), lastRequestId, messageSize));
        }
        catch (Exception e) {
            this.close();
            throw this.translateWriteException(e);
        }
        finally {
            this.writerLock.unlock();
        }
    }
    
    @Override
    public ResponseBuffers receiveMessage(final int responseTo) {
        Assertions.notNull("stream is open", this.stream);
        if (this.isClosed()) {
            throw new MongoSocketClosedException("Cannot read from a closed stream", this.getServerAddress());
        }
        CountDownLatch localLatch = new CountDownLatch(1);
        this.readerLock.lock();
        try {
            final ResponseBuffers responseBuffers = this.receiveResponseBuffers();
            this.messages.put(responseBuffers.getReplyHeader().getResponseTo(), responseBuffers);
            this.readingPhase.getAndSet(localLatch).countDown();
        }
        catch (Throwable t) {
            this.exceptionThatPrecededStreamClosing = this.translateReadException(t);
            this.close();
            this.readingPhase.getAndSet(localLatch).countDown();
        }
        finally {
            this.readerLock.unlock();
        }
        while (!this.isClosed()) {
            final ResponseBuffers myResponse = this.messages.remove(responseTo);
            if (myResponse != null) {
                this.connectionListener.messageReceived(new ConnectionMessageReceivedEvent(this.getId(), myResponse.getReplyHeader().getResponseTo(), myResponse.getReplyHeader().getMessageLength()));
                return myResponse;
            }
            try {
                localLatch.await();
            }
            catch (InterruptedException e) {
                throw new MongoInterruptedException("Interrupted while reading from stream", e);
            }
            localLatch = this.readingPhase.get();
        }
        if (this.exceptionThatPrecededStreamClosing != null) {
            throw this.exceptionThatPrecededStreamClosing;
        }
        throw new MongoSocketClosedException("Socket has been closed", this.getServerAddress());
    }
    
    @Override
    public void sendMessageAsync(final List<ByteBuf> byteBuffers, final int lastRequestId, final SingleResultCallback<Void> callback) {
        Assertions.notNull("stream is open", this.stream, callback);
        if (this.isClosed()) {
            callback.onResult(null, new MongoSocketClosedException("Can not read from a closed socket", this.getServerAddress()));
            return;
        }
        if (InternalStreamConnection.LOGGER.isTraceEnabled()) {
            InternalStreamConnection.LOGGER.trace(String.format("Queuing send message: %s. ([%s])", lastRequestId, this.getId()));
        }
        final SendMessageRequest sendMessageRequest = new SendMessageRequest(byteBuffers, lastRequestId, ErrorHandlingResultCallback.errorHandlingCallback(callback, InternalStreamConnection.LOGGER));
        boolean mustWrite = false;
        this.writerLock.lock();
        try {
            if (this.isWriting) {
                this.writeQueue.add(sendMessageRequest);
            }
            else {
                this.isWriting = true;
                mustWrite = true;
            }
        }
        finally {
            this.writerLock.unlock();
        }
        if (mustWrite) {
            this.writeAsync(sendMessageRequest);
        }
    }
    
    private void writeAsync(final SendMessageRequest request) {
        final int messageSize = this.getMessageSize(request.getByteBuffers());
        this.stream.writeAsync(request.getByteBuffers(), new AsyncCompletionHandler<Void>() {
            @Override
            public void completed(final Void v) {
                SendMessageRequest nextMessage = null;
                InternalStreamConnection.this.writerLock.lock();
                try {
                    nextMessage = InternalStreamConnection.this.writeQueue.poll();
                    if (nextMessage == null) {
                        InternalStreamConnection.this.isWriting = false;
                    }
                }
                finally {
                    InternalStreamConnection.this.writerLock.unlock();
                }
                InternalStreamConnection.this.connectionListener.messagesSent(new ConnectionMessagesSentEvent(InternalStreamConnection.this.getId(), request.getMessageId(), messageSize));
                request.getCallback().onResult(null, null);
                if (nextMessage != null) {
                    InternalStreamConnection.this.writeAsync(nextMessage);
                }
            }
            
            @Override
            public void failed(final Throwable t) {
                InternalStreamConnection.this.writerLock.lock();
                try {
                    final MongoException translatedWriteException = InternalStreamConnection.this.translateWriteException(t);
                    request.getCallback().onResult(null, translatedWriteException);
                    SendMessageRequest nextMessage;
                    while ((nextMessage = InternalStreamConnection.this.writeQueue.poll()) != null) {
                        nextMessage.callback.onResult(null, translatedWriteException);
                    }
                    InternalStreamConnection.this.isWriting = false;
                    InternalStreamConnection.this.close();
                }
                finally {
                    InternalStreamConnection.this.writerLock.unlock();
                }
            }
        });
    }
    
    @Override
    public void receiveMessageAsync(final int responseTo, final SingleResultCallback<ResponseBuffers> callback) {
        Assertions.isTrue("stream is open", this.stream != null, callback);
        if (this.isClosed()) {
            callback.onResult(null, new MongoSocketClosedException("Can not read from a closed socket", this.getServerAddress()));
            return;
        }
        if (InternalStreamConnection.LOGGER.isTraceEnabled()) {
            InternalStreamConnection.LOGGER.trace(String.format("Queuing read message: %s. ([%s])", responseTo, this.getId()));
        }
        ResponseBuffers response = null;
        this.readerLock.lock();
        boolean mustRead = false;
        try {
            response = this.messages.remove(responseTo);
            if (response == null) {
                this.readQueue.put(responseTo, callback);
            }
            if (!this.readQueue.isEmpty() && !this.isReading) {
                this.isReading = true;
                mustRead = true;
            }
        }
        finally {
            this.readerLock.unlock();
        }
        this.executeCallbackAndReceiveResponse(callback, response, mustRead);
    }
    
    private void executeCallbackAndReceiveResponse(final SingleResultCallback<ResponseBuffers> callback, final ResponseBuffers result, final boolean mustRead) {
        if (callback != null && result != null) {
            if (InternalStreamConnection.LOGGER.isTraceEnabled()) {
                InternalStreamConnection.LOGGER.trace(String.format("Executing callback for %s on %s", result.getReplyHeader().getResponseTo(), this.getId()));
            }
            callback.onResult(result, null);
        }
        if (mustRead) {
            if (InternalStreamConnection.LOGGER.isTraceEnabled()) {
                InternalStreamConnection.LOGGER.trace(String.format("Start receiving response on %s", this.getId()));
            }
            this.receiveResponseAsync();
        }
    }
    
    private ConnectionId getId() {
        return this.description.getConnectionId();
    }
    
    private ServerAddress getServerAddress() {
        return this.description.getServerAddress();
    }
    
    private void receiveResponseAsync() {
        this.readAsync(36, ErrorHandlingResultCallback.errorHandlingCallback((SingleResultCallback<ByteBuf>)new ResponseHeaderCallback(new ResponseBuffersCallback()), InternalStreamConnection.LOGGER));
    }
    
    private void readAsync(final int numBytes, final SingleResultCallback<ByteBuf> callback) {
        if (this.isClosed()) {
            callback.onResult(null, new MongoSocketClosedException("Cannot read from a closed stream", this.getServerAddress()));
            return;
        }
        try {
            this.stream.readAsync(numBytes, new AsyncCompletionHandler<ByteBuf>() {
                @Override
                public void completed(final ByteBuf buffer) {
                    callback.onResult(buffer, null);
                }
                
                @Override
                public void failed(final Throwable t) {
                    InternalStreamConnection.this.close();
                    callback.onResult(null, InternalStreamConnection.this.translateReadException(t));
                }
            });
        }
        catch (Exception e) {
            callback.onResult(null, this.translateReadException(e));
        }
    }
    
    private MongoException translateWriteException(final Throwable e) {
        if (e instanceof MongoException) {
            return (MongoException)e;
        }
        if (e instanceof IOException) {
            return new MongoSocketWriteException("Exception sending message", this.getServerAddress(), e);
        }
        if (e instanceof InterruptedException) {
            return new MongoInternalException("Thread interrupted exception", e);
        }
        return new MongoInternalException("Unexpected exception", e);
    }
    
    private MongoException translateReadException(final Throwable e) {
        if (e instanceof MongoException) {
            return (MongoException)e;
        }
        if (e instanceof SocketTimeoutException) {
            return new MongoSocketReadTimeoutException("Timeout while receiving message", this.getServerAddress(), e);
        }
        if (e instanceof InterruptedIOException) {
            return new MongoInterruptedException("Interrupted while receiving message", (Exception)e);
        }
        if (e instanceof ClosedByInterruptException) {
            return new MongoInterruptedException("Interrupted while receiving message", (Exception)e);
        }
        if (e instanceof IOException) {
            return new MongoSocketReadException("Exception receiving message", this.getServerAddress(), e);
        }
        if (e instanceof RuntimeException) {
            return new MongoInternalException("Unexpected runtime exception", e);
        }
        if (e instanceof InterruptedException) {
            return new MongoInternalException("Interrupted exception", e);
        }
        return new MongoInternalException("Unexpected exception", e);
    }
    
    private ResponseBuffers receiveResponseBuffers() throws IOException {
        final ByteBuf headerByteBuffer = this.stream.read(36);
        final ByteBufferBsonInput headerInputBuffer = new ByteBufferBsonInput(headerByteBuffer);
        ReplyHeader replyHeader;
        try {
            replyHeader = new ReplyHeader(headerInputBuffer, this.description.getMaxMessageSize());
        }
        finally {
            headerInputBuffer.close();
        }
        ByteBuf bodyByteBuffer = null;
        if (replyHeader.getNumberReturned() > 0) {
            bodyByteBuffer = this.stream.read(replyHeader.getMessageLength() - 36);
        }
        return new ResponseBuffers(replyHeader, bodyByteBuffer);
    }
    
    @Override
    public ByteBuf getBuffer(final int size) {
        Assertions.notNull("open", this.stream);
        return this.stream.getBuffer(size);
    }
    
    private int getMessageSize(final List<ByteBuf> byteBuffers) {
        int messageSize = 0;
        for (final ByteBuf cur : byteBuffers) {
            messageSize += cur.remaining();
        }
        return messageSize;
    }
    
    private void failAllQueuedReads(final Throwable t) {
        this.close();
        final Iterator<Map.Entry<Integer, SingleResultCallback<ResponseBuffers>>> it = this.readQueue.entrySet().iterator();
        while (it.hasNext()) {
            final Map.Entry<Integer, SingleResultCallback<ResponseBuffers>> pairs = it.next();
            if (InternalStreamConnection.LOGGER.isTraceEnabled()) {
                InternalStreamConnection.LOGGER.trace(String.format("Processing unknown failed message: %s. ([%s] %s)", pairs.getKey(), this.getId(), this.serverId));
            }
            final SingleResultCallback<ResponseBuffers> callback = pairs.getValue();
            it.remove();
            try {
                callback.onResult(null, t);
            }
            catch (Throwable tr) {
                InternalStreamConnection.LOGGER.warn("Exception calling callback", tr);
            }
        }
    }
    
    static {
        LOGGER = Loggers.getLogger("connection");
    }
    
    private class ResponseBuffersCallback implements SingleResultCallback<ResponseBuffers>
    {
        @Override
        public void onResult(final ResponseBuffers result, final Throwable t) {
            SingleResultCallback<ResponseBuffers> callback = null;
            boolean mustRead = false;
            InternalStreamConnection.this.readerLock.lock();
            try {
                if (t != null) {
                    InternalStreamConnection.this.failAllQueuedReads(t);
                    return;
                }
                if (InternalStreamConnection.LOGGER.isTraceEnabled()) {
                    InternalStreamConnection.LOGGER.trace(String.format("Read response to message %s on %s", result.getReplyHeader().getResponseTo(), InternalStreamConnection.this.getId()));
                }
                callback = InternalStreamConnection.this.readQueue.remove(result.getReplyHeader().getResponseTo());
                if (InternalStreamConnection.this.readQueue.isEmpty()) {
                    InternalStreamConnection.this.isReading = false;
                }
                else {
                    mustRead = true;
                }
                if (callback == null) {
                    InternalStreamConnection.this.messages.put(result.getReplyHeader().getResponseTo(), result);
                }
            }
            finally {
                InternalStreamConnection.this.readerLock.unlock();
            }
            InternalStreamConnection.this.executeCallbackAndReceiveResponse(callback, result, mustRead);
        }
    }
    
    private class ResponseHeaderCallback implements SingleResultCallback<ByteBuf>
    {
        private final SingleResultCallback<ResponseBuffers> callback;
        
        public ResponseHeaderCallback(final SingleResultCallback<ResponseBuffers> callback) {
            this.callback = callback;
        }
        
        @Override
        public void onResult(final ByteBuf result, final Throwable throwableFromCallback) {
            if (throwableFromCallback != null) {
                this.callback.onResult(null, throwableFromCallback);
            }
            else {
                try {
                    final ByteBufferBsonInput headerInputBuffer = new ByteBufferBsonInput(result);
                    ReplyHeader replyHeader;
                    try {
                        replyHeader = new ReplyHeader(headerInputBuffer, InternalStreamConnection.this.description.getMaxMessageSize());
                    }
                    finally {
                        headerInputBuffer.close();
                    }
                    if (replyHeader.getMessageLength() == 36) {
                        this.onSuccess(new ResponseBuffers(replyHeader, null));
                    }
                    else {
                        InternalStreamConnection.this.readAsync(replyHeader.getMessageLength() - 36, new ResponseBodyCallback(replyHeader));
                    }
                }
                catch (Throwable t) {
                    this.callback.onResult(null, t);
                }
            }
        }
        
        private void onSuccess(final ResponseBuffers responseBuffers) {
            if (responseBuffers == null) {
                this.callback.onResult(null, new MongoException("Unexpected empty response buffers"));
                return;
            }
            InternalStreamConnection.this.connectionListener.messageReceived(new ConnectionMessageReceivedEvent(InternalStreamConnection.this.getId(), responseBuffers.getReplyHeader().getResponseTo(), responseBuffers.getReplyHeader().getMessageLength()));
            try {
                this.callback.onResult(responseBuffers, null);
            }
            catch (Throwable t) {
                InternalStreamConnection.LOGGER.warn("Exception calling callback", t);
            }
        }
        
        private class ResponseBodyCallback implements SingleResultCallback<ByteBuf>
        {
            private final ReplyHeader replyHeader;
            
            public ResponseBodyCallback(final ReplyHeader replyHeader) {
                this.replyHeader = replyHeader;
            }
            
            @Override
            public void onResult(final ByteBuf result, final Throwable t) {
                if (t != null) {
                    try {
                        ResponseHeaderCallback.this.callback.onResult(new ResponseBuffers(this.replyHeader, result), t);
                    }
                    catch (Throwable tr) {
                        InternalStreamConnection.LOGGER.warn("Exception calling callback", tr);
                    }
                }
                else {
                    ResponseHeaderCallback.this.onSuccess(new ResponseBuffers(this.replyHeader, result));
                }
            }
        }
    }
    
    private static class SendMessageRequest
    {
        private final SingleResultCallback<Void> callback;
        private final List<ByteBuf> byteBuffers;
        private final int messageId;
        
        SendMessageRequest(final List<ByteBuf> byteBuffers, final int messageId, final SingleResultCallback<Void> callback) {
            this.byteBuffers = byteBuffers;
            this.messageId = messageId;
            this.callback = callback;
        }
        
        public SingleResultCallback<Void> getCallback() {
            return this.callback;
        }
        
        public List<ByteBuf> getByteBuffers() {
            return this.byteBuffers;
        }
        
        public int getMessageId() {
            return this.messageId;
        }
    }
    
    private static class ErrorHandlingConnectionListener implements ConnectionListener
    {
        private final ConnectionListener wrapped;
        
        public ErrorHandlingConnectionListener(final ConnectionListener wrapped) {
            this.wrapped = wrapped;
        }
        
        @Override
        public void connectionOpened(final ConnectionOpenedEvent event) {
            try {
                this.wrapped.connectionOpened(event);
            }
            catch (Throwable t) {
                InternalStreamConnection.LOGGER.warn("Exception when trying to signal connectionOpened to the connectionListener", t);
            }
        }
        
        @Override
        public void connectionClosed(final ConnectionClosedEvent event) {
            try {
                this.wrapped.connectionClosed(event);
            }
            catch (Throwable t) {
                InternalStreamConnection.LOGGER.warn("Exception when trying to signal connectionOpened to the connectionListener", t);
            }
        }
        
        @Override
        public void messagesSent(final ConnectionMessagesSentEvent event) {
            try {
                this.wrapped.messagesSent(event);
            }
            catch (Throwable t) {
                InternalStreamConnection.LOGGER.warn("Exception when trying to signal connectionOpened to the connectionListener", t);
            }
        }
        
        @Override
        public void messageReceived(final ConnectionMessageReceivedEvent event) {
            try {
                this.wrapped.messageReceived(event);
            }
            catch (Throwable t) {
                InternalStreamConnection.LOGGER.warn("Exception when trying to signal connectionOpened to the connectionListener", t);
            }
        }
    }
}
