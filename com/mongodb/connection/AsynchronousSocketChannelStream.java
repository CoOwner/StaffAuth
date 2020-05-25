package com.mongodb.connection;

import org.bson.*;
import java.io.*;
import com.mongodb.assertions.*;
import java.util.*;
import java.util.concurrent.*;
import java.nio.*;
import java.util.concurrent.atomic.*;
import java.nio.channels.*;
import com.mongodb.*;
import java.net.*;

final class AsynchronousSocketChannelStream implements Stream
{
    private final ServerAddress serverAddress;
    private final SocketSettings settings;
    private final BufferProvider bufferProvider;
    private volatile AsynchronousSocketChannel channel;
    private volatile boolean isClosed;
    
    AsynchronousSocketChannelStream(final ServerAddress serverAddress, final SocketSettings settings, final BufferProvider bufferProvider) {
        this.serverAddress = serverAddress;
        this.settings = settings;
        this.bufferProvider = bufferProvider;
    }
    
    @Override
    public ByteBuf getBuffer(final int size) {
        return this.bufferProvider.getBuffer(size);
    }
    
    @Override
    public void open() throws IOException {
        final FutureAsyncCompletionHandler<Void> handler = new FutureAsyncCompletionHandler<Void>();
        this.openAsync(handler);
        handler.getOpen();
    }
    
    @Override
    public void openAsync(final AsyncCompletionHandler<Void> handler) {
        Assertions.isTrue("unopened", this.channel == null);
        try {
            (this.channel = AsynchronousSocketChannel.open()).setOption(StandardSocketOptions.TCP_NODELAY, Boolean.valueOf(true));
            this.channel.setOption(StandardSocketOptions.SO_KEEPALIVE, Boolean.valueOf(this.settings.isKeepAlive()));
            if (this.settings.getReceiveBufferSize() > 0) {
                this.channel.setOption(StandardSocketOptions.SO_RCVBUF, Integer.valueOf(this.settings.getReceiveBufferSize()));
            }
            if (this.settings.getSendBufferSize() > 0) {
                this.channel.setOption(StandardSocketOptions.SO_SNDBUF, Integer.valueOf(this.settings.getSendBufferSize()));
            }
            this.channel.connect(this.serverAddress.getSocketAddress(), (Object)null, new OpenCompletionHandler(handler));
        }
        catch (IOException e) {
            handler.failed(new MongoSocketOpenException("Exception opening socket", this.serverAddress, e));
        }
        catch (Throwable t) {
            handler.failed(t);
        }
    }
    
    @Override
    public void write(final List<ByteBuf> buffers) throws IOException {
        final FutureAsyncCompletionHandler<Void> handler = new FutureAsyncCompletionHandler<Void>();
        this.writeAsync(buffers, handler);
        handler.getWrite();
    }
    
    @Override
    public ByteBuf read(final int numBytes) throws IOException {
        final FutureAsyncCompletionHandler<ByteBuf> handler = new FutureAsyncCompletionHandler<ByteBuf>();
        this.readAsync(numBytes, handler);
        return handler.getRead();
    }
    
    @Override
    public void writeAsync(final List<ByteBuf> buffers, final AsyncCompletionHandler<Void> handler) {
        final AsyncWritableByteChannel byteChannel = new AsyncWritableByteChannelAdapter();
        final Iterator<ByteBuf> iter = buffers.iterator();
        this.pipeOneBuffer(byteChannel, iter.next(), new AsyncCompletionHandler<Void>() {
            @Override
            public void completed(final Void t) {
                if (iter.hasNext()) {
                    AsynchronousSocketChannelStream.this.pipeOneBuffer(byteChannel, iter.next(), this);
                }
                else {
                    handler.completed(null);
                }
            }
            
            @Override
            public void failed(final Throwable t) {
                handler.failed(t);
            }
        });
    }
    
    @Override
    public void readAsync(final int numBytes, final AsyncCompletionHandler<ByteBuf> handler) {
        final ByteBuf buffer = this.bufferProvider.getBuffer(numBytes);
        this.channel.read(buffer.asNIO(), this.settings.getReadTimeout(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS, (Object)null, (CompletionHandler<Integer, ? super Object>)new BasicCompletionHandler(buffer, (AsyncCompletionHandler)handler));
    }
    
    @Override
    public ServerAddress getAddress() {
        return this.serverAddress;
    }
    
    @Override
    public void close() {
        try {
            if (this.channel != null) {
                this.channel.close();
            }
        }
        catch (IOException ex) {}
        finally {
            this.channel = null;
            this.isClosed = true;
        }
    }
    
    @Override
    public boolean isClosed() {
        return this.isClosed;
    }
    
    private void pipeOneBuffer(final AsyncWritableByteChannel byteChannel, final ByteBuf byteBuffer, final AsyncCompletionHandler<Void> outerHandler) {
        byteChannel.write(byteBuffer.asNIO(), new AsyncCompletionHandler<Void>() {
            @Override
            public void completed(final Void t) {
                if (byteBuffer.hasRemaining()) {
                    byteChannel.write(byteBuffer.asNIO(), this);
                }
                else {
                    outerHandler.completed(null);
                }
            }
            
            @Override
            public void failed(final Throwable t) {
                outerHandler.failed(t);
            }
        });
    }
    
    private class AsyncWritableByteChannelAdapter implements AsyncWritableByteChannel
    {
        @Override
        public void write(final ByteBuffer src, final AsyncCompletionHandler<Void> handler) {
            AsynchronousSocketChannelStream.this.channel.write(src, (Object)null, new WriteCompletionHandler(handler));
        }
        
        private class WriteCompletionHandler extends BaseCompletionHandler<Void, Integer, Object>
        {
            public WriteCompletionHandler(final AsyncCompletionHandler<Void> handler) {
                super(handler);
            }
            
            @Override
            public void completed(final Integer result, final Object attachment) {
                final AsyncCompletionHandler<Void> localHandler = ((BaseCompletionHandler<Void, V, A>)this).getHandlerAndClear();
                localHandler.completed(null);
            }
            
            @Override
            public void failed(final Throwable exc, final Object attachment) {
                final AsyncCompletionHandler<Void> localHandler = ((BaseCompletionHandler<Void, V, A>)this).getHandlerAndClear();
                localHandler.failed(exc);
            }
        }
    }
    
    private final class BasicCompletionHandler extends BaseCompletionHandler<ByteBuf, Integer, Void>
    {
        private final AtomicReference<ByteBuf> byteBufReference;
        
        private BasicCompletionHandler(final ByteBuf dst, final AsyncCompletionHandler<ByteBuf> handler) {
            super(handler);
            this.byteBufReference = new AtomicReference<ByteBuf>(dst);
        }
        
        @Override
        public void completed(final Integer result, final Void attachment) {
            final AsyncCompletionHandler<ByteBuf> localHandler = ((BaseCompletionHandler<ByteBuf, V, A>)this).getHandlerAndClear();
            final ByteBuf localByteBuf = this.byteBufReference.getAndSet(null);
            if (result == -1) {
                localByteBuf.release();
                localHandler.failed(new MongoSocketReadException("Prematurely reached end of stream", AsynchronousSocketChannelStream.this.serverAddress));
            }
            else if (!localByteBuf.hasRemaining()) {
                localByteBuf.flip();
                localHandler.completed(localByteBuf);
            }
            else {
                AsynchronousSocketChannelStream.this.channel.read(localByteBuf.asNIO(), AsynchronousSocketChannelStream.this.settings.getReadTimeout(TimeUnit.MILLISECONDS), TimeUnit.MILLISECONDS, (Object)null, (CompletionHandler<Integer, ? super Object>)new BasicCompletionHandler(localByteBuf, localHandler));
            }
        }
        
        @Override
        public void failed(final Throwable t, final Void attachment) {
            final AsyncCompletionHandler<ByteBuf> localHandler = ((BaseCompletionHandler<ByteBuf, V, A>)this).getHandlerAndClear();
            final ByteBuf localByteBuf = this.byteBufReference.getAndSet(null);
            localByteBuf.release();
            if (t instanceof InterruptedByTimeoutException) {
                localHandler.failed(new MongoSocketReadTimeoutException("Timeout while receiving message", AsynchronousSocketChannelStream.this.serverAddress, t));
            }
            else {
                localHandler.failed(t);
            }
        }
    }
    
    private class OpenCompletionHandler extends BaseCompletionHandler<Void, Void, Object>
    {
        public OpenCompletionHandler(final AsyncCompletionHandler<Void> handler) {
            super(handler);
        }
        
        @Override
        public void completed(final Void result, final Object attachment) {
            final AsyncCompletionHandler<Void> localHandler = ((BaseCompletionHandler<Void, V, A>)this).getHandlerAndClear();
            localHandler.completed(null);
        }
        
        @Override
        public void failed(final Throwable exc, final Object attachment) {
            final AsyncCompletionHandler<Void> localHandler = ((BaseCompletionHandler<Void, V, A>)this).getHandlerAndClear();
            if (exc instanceof ConnectException) {
                localHandler.failed(new MongoSocketOpenException("Exception opening socket", AsynchronousSocketChannelStream.this.getAddress(), exc));
            }
            else {
                localHandler.failed(exc);
            }
        }
    }
    
    private abstract static class BaseCompletionHandler<T, V, A> implements CompletionHandler<V, A>
    {
        private final AtomicReference<AsyncCompletionHandler<T>> handlerReference;
        
        public BaseCompletionHandler(final AsyncCompletionHandler<T> handler) {
            this.handlerReference = new AtomicReference<AsyncCompletionHandler<T>>(handler);
        }
        
        protected AsyncCompletionHandler<T> getHandlerAndClear() {
            return this.handlerReference.getAndSet(null);
        }
    }
}
