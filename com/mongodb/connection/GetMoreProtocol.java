package com.mongodb.connection;

import com.mongodb.event.*;
import com.mongodb.*;
import org.bson.codecs.*;
import com.mongodb.async.*;
import org.bson.io.*;
import java.util.*;
import org.bson.*;
import com.mongodb.diagnostics.logging.*;

class GetMoreProtocol<T> implements Protocol<QueryResult<T>>
{
    public static final Logger LOGGER;
    private static final String COMMAND_NAME = "getMore";
    private final Decoder<T> resultDecoder;
    private final MongoNamespace namespace;
    private final long cursorId;
    private final int numberToReturn;
    private CommandListener commandListener;
    
    public GetMoreProtocol(final MongoNamespace namespace, final long cursorId, final int numberToReturn, final Decoder<T> resultDecoder) {
        this.namespace = namespace;
        this.cursorId = cursorId;
        this.numberToReturn = numberToReturn;
        this.resultDecoder = resultDecoder;
    }
    
    @Override
    public QueryResult<T> execute(final InternalConnection connection) {
        if (GetMoreProtocol.LOGGER.isDebugEnabled()) {
            GetMoreProtocol.LOGGER.debug(String.format("Getting more documents from namespace %s with cursor %d on connection [%s] to server %s", this.namespace, this.cursorId, connection.getDescription().getConnectionId(), connection.getDescription().getServerAddress()));
        }
        final long startTimeNanos = System.nanoTime();
        final GetMoreMessage message = new GetMoreMessage(this.namespace.getFullName(), this.cursorId, this.numberToReturn);
        QueryResult<T> queryResult = null;
        try {
            this.sendMessage(message, connection);
            final ResponseBuffers responseBuffers = connection.receiveMessage(message.getId());
            try {
                if (responseBuffers.getReplyHeader().isCursorNotFound()) {
                    throw new MongoCursorNotFoundException(message.getCursorId(), connection.getDescription().getServerAddress());
                }
                if (responseBuffers.getReplyHeader().isQueryFailure()) {
                    final BsonDocument errorDocument = new ReplyMessage<BsonDocument>(responseBuffers, new BsonDocumentCodec(), message.getId()).getDocuments().get(0);
                    throw ProtocolHelper.getQueryFailureException(errorDocument, connection.getDescription().getServerAddress());
                }
                queryResult = new QueryResult<T>(this.namespace, new ReplyMessage<T>(responseBuffers, this.resultDecoder, message.getId()), connection.getDescription().getServerAddress());
                if (this.commandListener != null) {
                    ProtocolHelper.sendCommandSucceededEvent(message, "getMore", this.asGetMoreCommandResponseDocument(queryResult, responseBuffers), connection.getDescription(), startTimeNanos, this.commandListener);
                }
            }
            finally {
                responseBuffers.close();
            }
            GetMoreProtocol.LOGGER.debug("Get-more completed");
            return queryResult;
        }
        catch (RuntimeException e) {
            if (this.commandListener != null) {
                ProtocolHelper.sendCommandFailedEvent(message, "getMore", connection.getDescription(), startTimeNanos, e, this.commandListener);
            }
            throw e;
        }
    }
    
    @Override
    public void executeAsync(final InternalConnection connection, final SingleResultCallback<QueryResult<T>> callback) {
        final long startTimeNanos = System.nanoTime();
        final GetMoreMessage message = new GetMoreMessage(this.namespace.getFullName(), this.cursorId, this.numberToReturn);
        boolean sentStartedEvent = false;
        try {
            if (GetMoreProtocol.LOGGER.isDebugEnabled()) {
                GetMoreProtocol.LOGGER.debug(String.format("Asynchronously getting more documents from namespace %s with cursor %d on connection [%s] to server %s", this.namespace, this.cursorId, connection.getDescription().getConnectionId(), connection.getDescription().getServerAddress()));
            }
            final ByteBufferBsonOutput bsonOutput = new ByteBufferBsonOutput(connection);
            if (this.commandListener != null) {
                ProtocolHelper.sendCommandStartedEvent(message, this.namespace.getDatabaseName(), "getMore", this.asGetMoreCommandDocument(), connection.getDescription(), this.commandListener);
                sentStartedEvent = true;
            }
            ProtocolHelper.encodeMessage(message, bsonOutput);
            final SingleResultCallback<ResponseBuffers> receiveCallback = new GetMoreResultCallback(callback, this.cursorId, message, connection.getDescription(), this.commandListener, startTimeNanos);
            connection.sendMessageAsync(bsonOutput.getByteBuffers(), message.getId(), new SendMessageCallback<Object>(connection, bsonOutput, message, "getMore", startTimeNanos, this.commandListener, callback, receiveCallback));
        }
        catch (Throwable t) {
            if (sentStartedEvent) {
                ProtocolHelper.sendCommandFailedEvent(message, "getMore", connection.getDescription(), startTimeNanos, t, this.commandListener);
            }
            callback.onResult(null, t);
        }
    }
    
    @Override
    public void setCommandListener(final CommandListener commandListener) {
        this.commandListener = commandListener;
    }
    
    private void sendMessage(final GetMoreMessage message, final InternalConnection connection) {
        final ByteBufferBsonOutput bsonOutput = new ByteBufferBsonOutput(connection);
        try {
            if (this.commandListener != null) {
                ProtocolHelper.sendCommandStartedEvent(message, this.namespace.getDatabaseName(), "getMore", this.asGetMoreCommandDocument(), connection.getDescription(), this.commandListener);
            }
            message.encode(bsonOutput);
            connection.sendMessage(bsonOutput.getByteBuffers(), message.getId());
        }
        finally {
            bsonOutput.close();
        }
    }
    
    private BsonDocument asGetMoreCommandDocument() {
        return new BsonDocument("getMore", new BsonInt64(this.cursorId)).append("collection", new BsonString(this.namespace.getCollectionName())).append("batchSize", new BsonInt32(this.numberToReturn));
    }
    
    private BsonDocument asGetMoreCommandResponseDocument(final QueryResult<T> queryResult, final ResponseBuffers responseBuffers) {
        List<ByteBufBsonDocument> rawResultDocuments = Collections.emptyList();
        if (responseBuffers.getReplyHeader().getNumberReturned() != 0) {
            responseBuffers.getBodyByteBuffer().position(0);
            rawResultDocuments = ByteBufBsonDocument.create(responseBuffers);
        }
        final BsonDocument cursorDocument = new BsonDocument("id", (queryResult.getCursor() == null) ? new BsonInt64(0L) : new BsonInt64(queryResult.getCursor().getId())).append("ns", new BsonString(this.namespace.getFullName())).append("nextBatch", new BsonArray(rawResultDocuments));
        return new BsonDocument("cursor", cursorDocument).append("ok", new BsonDouble(1.0));
    }
    
    static {
        LOGGER = Loggers.getLogger("protocol.getmore");
    }
    
    class GetMoreResultCallback extends ResponseCallback
    {
        private final SingleResultCallback<QueryResult<T>> callback;
        private final long cursorId;
        private final GetMoreMessage message;
        private final ConnectionDescription connectionDescription;
        private final CommandListener commandListener;
        private final long startTimeNanos;
        
        public GetMoreResultCallback(final SingleResultCallback<QueryResult<T>> callback, final long cursorId, final GetMoreMessage message, final ConnectionDescription connectionDescription, final CommandListener commandListener, final long startTimeNanos) {
            super(message.getId(), connectionDescription.getServerAddress());
            this.callback = callback;
            this.cursorId = cursorId;
            this.message = message;
            this.connectionDescription = connectionDescription;
            this.commandListener = commandListener;
            this.startTimeNanos = startTimeNanos;
        }
        
        @Override
        protected void callCallback(final ResponseBuffers responseBuffers, final Throwable throwableFromCallback) {
            try {
                if (throwableFromCallback != null) {
                    throw throwableFromCallback;
                }
                if (responseBuffers.getReplyHeader().isCursorNotFound()) {
                    throw new MongoCursorNotFoundException(this.cursorId, this.getServerAddress());
                }
                if (responseBuffers.getReplyHeader().isQueryFailure()) {
                    final BsonDocument errorDocument = new ReplyMessage<BsonDocument>(responseBuffers, new BsonDocumentCodec(), this.message.getId()).getDocuments().get(0);
                    throw ProtocolHelper.getQueryFailureException(errorDocument, this.connectionDescription.getServerAddress());
                }
                final QueryResult<T> result = new QueryResult<T>(GetMoreProtocol.this.namespace, new ReplyMessage<T>(responseBuffers, GetMoreProtocol.this.resultDecoder, this.getRequestId()), this.getServerAddress());
                if (this.commandListener != null) {
                    ProtocolHelper.sendCommandSucceededEvent(this.message, "getMore", GetMoreProtocol.this.asGetMoreCommandResponseDocument(result, responseBuffers), this.connectionDescription, this.startTimeNanos, this.commandListener);
                }
                if (GetMoreProtocol.LOGGER.isDebugEnabled()) {
                    GetMoreProtocol.LOGGER.debug(String.format("GetMore results received %s documents with cursor %s", result.getResults().size(), result.getCursor()));
                }
                this.callback.onResult(result, null);
            }
            catch (Throwable t) {
                if (this.commandListener != null) {
                    ProtocolHelper.sendCommandFailedEvent(this.message, "getMore", this.connectionDescription, this.startTimeNanos, t, this.commandListener);
                }
                this.callback.onResult(null, t);
                try {
                    if (responseBuffers != null) {
                        responseBuffers.close();
                    }
                }
                catch (Throwable t2) {
                    GetMoreProtocol.LOGGER.debug("GetMore ResponseBuffer close exception", t2);
                }
            }
            finally {
                try {
                    if (responseBuffers != null) {
                        responseBuffers.close();
                    }
                }
                catch (Throwable t3) {
                    GetMoreProtocol.LOGGER.debug("GetMore ResponseBuffer close exception", t3);
                }
            }
        }
    }
}
