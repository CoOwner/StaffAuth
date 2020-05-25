package com.mongodb.connection;

import com.mongodb.*;
import com.mongodb.event.*;
import org.bson.codecs.*;
import com.mongodb.async.*;
import org.bson.io.*;
import org.bson.*;
import com.mongodb.diagnostics.logging.*;
import java.util.*;

class QueryProtocol<T> implements Protocol<QueryResult<T>>
{
    public static final Logger LOGGER;
    private static final String FIND_COMMAND_NAME = "find";
    private static final String EXPLAIN_COMMAND_NAME = "explain";
    private final int skip;
    private final int limit;
    private final int batchSize;
    private final int numberToReturn;
    private final boolean withLimitAndBatchSize;
    private final BsonDocument queryDocument;
    private final BsonDocument fields;
    private final Decoder<T> resultDecoder;
    private final MongoNamespace namespace;
    private boolean tailableCursor;
    private boolean slaveOk;
    private boolean oplogReplay;
    private boolean noCursorTimeout;
    private boolean awaitData;
    private boolean partial;
    private CommandListener commandListener;
    private static final Map<String, String> META_OPERATOR_TO_COMMAND_FIELD_MAP;
    
    public QueryProtocol(final MongoNamespace namespace, final int skip, final int numberToReturn, final BsonDocument queryDocument, final BsonDocument fields, final Decoder<T> resultDecoder) {
        this.namespace = namespace;
        this.skip = skip;
        this.withLimitAndBatchSize = false;
        this.numberToReturn = numberToReturn;
        this.limit = 0;
        this.batchSize = 0;
        this.queryDocument = queryDocument;
        this.fields = fields;
        this.resultDecoder = resultDecoder;
    }
    
    public QueryProtocol(final MongoNamespace namespace, final int skip, final int limit, final int batchSize, final BsonDocument queryDocument, final BsonDocument fields, final Decoder<T> resultDecoder) {
        this.namespace = namespace;
        this.skip = skip;
        this.withLimitAndBatchSize = true;
        this.numberToReturn = 0;
        this.limit = limit;
        this.batchSize = batchSize;
        this.queryDocument = queryDocument;
        this.fields = fields;
        this.resultDecoder = resultDecoder;
    }
    
    @Override
    public void setCommandListener(final CommandListener commandListener) {
        this.commandListener = commandListener;
    }
    
    public CommandListener getCommandListener() {
        return this.commandListener;
    }
    
    public boolean isTailableCursor() {
        return this.tailableCursor;
    }
    
    public QueryProtocol<T> tailableCursor(final boolean tailableCursor) {
        this.tailableCursor = tailableCursor;
        return this;
    }
    
    public boolean isSlaveOk() {
        return this.slaveOk;
    }
    
    public QueryProtocol<T> slaveOk(final boolean slaveOk) {
        this.slaveOk = slaveOk;
        return this;
    }
    
    public boolean isOplogReplay() {
        return this.oplogReplay;
    }
    
    public QueryProtocol<T> oplogReplay(final boolean oplogReplay) {
        this.oplogReplay = oplogReplay;
        return this;
    }
    
    public boolean isNoCursorTimeout() {
        return this.noCursorTimeout;
    }
    
    public QueryProtocol<T> noCursorTimeout(final boolean noCursorTimeout) {
        this.noCursorTimeout = noCursorTimeout;
        return this;
    }
    
    public boolean isAwaitData() {
        return this.awaitData;
    }
    
    public QueryProtocol<T> awaitData(final boolean awaitData) {
        this.awaitData = awaitData;
        return this;
    }
    
    public boolean isPartial() {
        return this.partial;
    }
    
    public QueryProtocol<T> partial(final boolean partial) {
        this.partial = partial;
        return this;
    }
    
    @Override
    public QueryResult<T> execute(final InternalConnection connection) {
        if (QueryProtocol.LOGGER.isDebugEnabled()) {
            QueryProtocol.LOGGER.debug(String.format("Sending query of namespace %s on connection [%s] to server %s", this.namespace, connection.getDescription().getConnectionId(), connection.getDescription().getServerAddress()));
        }
        final long startTimeNanos = System.nanoTime();
        QueryMessage message = null;
        try {
            boolean isExplain = false;
            final ByteBufferBsonOutput bsonOutput = new ByteBufferBsonOutput(connection);
            try {
                message = this.createQueryMessage(connection.getDescription());
                final RequestMessage.EncodingMetadata metadata = message.encodeWithMetadata(bsonOutput);
                isExplain = this.sendQueryStartedEvent(connection, message, bsonOutput, metadata);
                connection.sendMessage(bsonOutput.getByteBuffers(), message.getId());
            }
            finally {
                bsonOutput.close();
            }
            final ResponseBuffers responseBuffers = connection.receiveMessage(message.getId());
            try {
                if (responseBuffers.getReplyHeader().isQueryFailure()) {
                    final BsonDocument errorDocument = new ReplyMessage<BsonDocument>(responseBuffers, new BsonDocumentCodec(), message.getId()).getDocuments().get(0);
                    throw ProtocolHelper.getQueryFailureException(errorDocument, connection.getDescription().getServerAddress());
                }
                final ReplyMessage<T> replyMessage = new ReplyMessage<T>(responseBuffers, this.resultDecoder, message.getId());
                final QueryResult<T> queryResult = new QueryResult<T>(this.namespace, replyMessage, connection.getDescription().getServerAddress());
                this.sendQuerySucceededEvent(connection.getDescription(), startTimeNanos, message, isExplain, responseBuffers, queryResult);
                QueryProtocol.LOGGER.debug("Query completed");
                return queryResult;
            }
            finally {
                responseBuffers.close();
            }
        }
        catch (RuntimeException e) {
            if (this.commandListener != null) {
                ProtocolHelper.sendCommandFailedEvent(message, "find", connection.getDescription(), startTimeNanos, e, this.commandListener);
            }
            throw e;
        }
    }
    
    @Override
    public void executeAsync(final InternalConnection connection, final SingleResultCallback<QueryResult<T>> callback) {
        final long startTimeNanos = System.nanoTime();
        final QueryMessage message = this.createQueryMessage(connection.getDescription());
        boolean sentStartedEvent = true;
        try {
            if (QueryProtocol.LOGGER.isDebugEnabled()) {
                QueryProtocol.LOGGER.debug(String.format("Asynchronously sending query of namespace %s on connection [%s] to server %s", this.namespace, connection.getDescription().getConnectionId(), connection.getDescription().getServerAddress()));
            }
            final ByteBufferBsonOutput bsonOutput = new ByteBufferBsonOutput(connection);
            final RequestMessage.EncodingMetadata metadata = ProtocolHelper.encodeMessageWithMetadata(message, bsonOutput);
            final boolean isExplainEvent = this.sendQueryStartedEvent(connection, message, bsonOutput, metadata);
            sentStartedEvent = true;
            final SingleResultCallback<ResponseBuffers> receiveCallback = new QueryResultCallback(callback, message.getId(), startTimeNanos, message, isExplainEvent, connection.getDescription());
            connection.sendMessageAsync(bsonOutput.getByteBuffers(), message.getId(), new SendMessageCallback<Object>(connection, bsonOutput, message, this.getCommandName(isExplainEvent), startTimeNanos, this.commandListener, callback, receiveCallback));
        }
        catch (Throwable t) {
            if (this.commandListener != null && sentStartedEvent) {
                ProtocolHelper.sendCommandFailedEvent(message, "find", connection.getDescription(), startTimeNanos, t, this.commandListener);
            }
            callback.onResult(null, t);
        }
    }
    
    private boolean sendQueryStartedEvent(final InternalConnection connection, final QueryMessage message, final ByteBufferBsonOutput bsonOutput, final RequestMessage.EncodingMetadata metadata) {
        boolean isExplainEvent = false;
        if (this.commandListener != null) {
            final BsonDocument command = this.asFindCommandDocument(bsonOutput, metadata.getFirstDocumentPosition());
            isExplainEvent = command.keySet().iterator().next().equals("explain");
            ProtocolHelper.sendCommandStartedEvent(message, this.namespace.getDatabaseName(), this.getCommandName(isExplainEvent), command, connection.getDescription(), this.commandListener);
        }
        return isExplainEvent;
    }
    
    private String getCommandName(final boolean isExplainEvent) {
        return isExplainEvent ? "explain" : "find";
    }
    
    private void sendQuerySucceededEvent(final ConnectionDescription connectionDescription, final long startTimeNanos, final QueryMessage message, final boolean isExplainEvent, final ResponseBuffers responseBuffers, final QueryResult<T> queryResult) {
        if (this.commandListener != null) {
            final BsonDocument response = this.asFindCommandResponseDocument(responseBuffers, queryResult, isExplainEvent);
            ProtocolHelper.sendCommandSucceededEvent(message, this.getCommandName(isExplainEvent), response, connectionDescription, startTimeNanos, this.commandListener);
        }
    }
    
    private QueryMessage createQueryMessage(final ConnectionDescription connectionDescription) {
        return (QueryMessage)new QueryMessage(this.namespace.getFullName(), this.skip, this.getNumberToReturn(), this.queryDocument, this.fields, ProtocolHelper.getMessageSettings(connectionDescription)).tailableCursor(this.isTailableCursor()).slaveOk(this.isSlaveOk()).oplogReplay(this.isOplogReplay()).noCursorTimeout(this.isNoCursorTimeout()).awaitData(this.isAwaitData()).partial(this.isPartial());
    }
    
    private int getNumberToReturn() {
        if (!this.withLimitAndBatchSize) {
            return this.numberToReturn;
        }
        if (this.limit < 0) {
            return this.limit;
        }
        if (this.limit == 0) {
            return this.batchSize;
        }
        if (this.batchSize == 0) {
            return this.limit;
        }
        if (this.limit < Math.abs(this.batchSize)) {
            return this.limit;
        }
        return this.batchSize;
    }
    
    private BsonDocument asFindCommandDocument(final ByteBufferBsonOutput bsonOutput, final int firstDocumentPosition) {
        BsonDocument command = new BsonDocument("find", new BsonString(this.namespace.getCollectionName()));
        boolean isExplain = false;
        final List<ByteBufBsonDocument> documents = ByteBufBsonDocument.create(bsonOutput, firstDocumentPosition);
        final ByteBufBsonDocument rawQueryDocument = documents.get(0);
        for (final Map.Entry<String, BsonValue> cur : rawQueryDocument.entrySet()) {
            final String commandFieldName = QueryProtocol.META_OPERATOR_TO_COMMAND_FIELD_MAP.get(cur.getKey());
            if (commandFieldName != null) {
                command.append(commandFieldName, cur.getValue());
            }
            else {
                if (!cur.getKey().equals("$explain")) {
                    continue;
                }
                isExplain = true;
            }
        }
        if (command.size() == 1) {
            command.append("filter", rawQueryDocument);
        }
        if (documents.size() == 2) {
            command.append("projection", documents.get(1));
        }
        if (this.skip != 0) {
            command.append("skip", new BsonInt32(this.skip));
        }
        if (this.withLimitAndBatchSize) {
            if (this.limit != 0) {
                command.append("limit", new BsonInt32(this.limit));
            }
            if (this.batchSize != 0) {
                command.append("batchSize", new BsonInt32(this.batchSize));
            }
        }
        if (this.tailableCursor) {
            command.append("tailable", BsonBoolean.valueOf(this.tailableCursor));
        }
        if (this.noCursorTimeout) {
            command.append("noCursorTimeout", BsonBoolean.valueOf(this.noCursorTimeout));
        }
        if (this.oplogReplay) {
            command.append("oplogReplay", BsonBoolean.valueOf(this.oplogReplay));
        }
        if (this.awaitData) {
            command.append("awaitData", BsonBoolean.valueOf(this.awaitData));
        }
        if (this.partial) {
            command.append("allowPartialResults", BsonBoolean.valueOf(this.partial));
        }
        if (isExplain) {
            command = new BsonDocument("explain", command);
        }
        return command;
    }
    
    private BsonDocument asFindCommandResponseDocument(final ResponseBuffers responseBuffers, final QueryResult<T> queryResult, final boolean isExplain) {
        List<ByteBufBsonDocument> rawResultDocuments = Collections.emptyList();
        if (responseBuffers.getReplyHeader().getNumberReturned() > 0) {
            responseBuffers.getBodyByteBuffer().position(0);
            rawResultDocuments = ByteBufBsonDocument.create(responseBuffers);
        }
        if (isExplain) {
            final BsonDocument explainCommandResponseDocument = new BsonDocument("ok", new BsonDouble(1.0));
            explainCommandResponseDocument.putAll(rawResultDocuments.get(0));
            return explainCommandResponseDocument;
        }
        final BsonDocument cursorDocument = new BsonDocument("id", (queryResult.getCursor() == null) ? new BsonInt64(0L) : new BsonInt64(queryResult.getCursor().getId())).append("ns", new BsonString(this.namespace.getFullName())).append("firstBatch", new BsonArray(rawResultDocuments));
        return new BsonDocument("cursor", cursorDocument).append("ok", new BsonDouble(1.0));
    }
    
    static {
        LOGGER = Loggers.getLogger("protocol.query");
        (META_OPERATOR_TO_COMMAND_FIELD_MAP = new HashMap<String, String>()).put("$query", "filter");
        QueryProtocol.META_OPERATOR_TO_COMMAND_FIELD_MAP.put("$orderby", "sort");
        QueryProtocol.META_OPERATOR_TO_COMMAND_FIELD_MAP.put("$hint", "hint");
        QueryProtocol.META_OPERATOR_TO_COMMAND_FIELD_MAP.put("$comment", "comment");
        QueryProtocol.META_OPERATOR_TO_COMMAND_FIELD_MAP.put("$maxScan", "maxScan");
        QueryProtocol.META_OPERATOR_TO_COMMAND_FIELD_MAP.put("$maxTimeMS", "maxTimeMS");
        QueryProtocol.META_OPERATOR_TO_COMMAND_FIELD_MAP.put("$max", "max");
        QueryProtocol.META_OPERATOR_TO_COMMAND_FIELD_MAP.put("$min", "min");
        QueryProtocol.META_OPERATOR_TO_COMMAND_FIELD_MAP.put("$returnKey", "returnKey");
        QueryProtocol.META_OPERATOR_TO_COMMAND_FIELD_MAP.put("$showDiskLoc", "showRecordId");
        QueryProtocol.META_OPERATOR_TO_COMMAND_FIELD_MAP.put("$snapshot", "snapshot");
    }
    
    class QueryResultCallback extends ResponseCallback
    {
        private final SingleResultCallback<QueryResult<T>> callback;
        private final ConnectionDescription connectionDescription;
        private final long startTimeNanos;
        private final QueryMessage message;
        private final boolean isExplainEvent;
        
        public QueryResultCallback(final SingleResultCallback<QueryResult<T>> callback, final int requestId, final long startTimeNanos, final QueryMessage message, final boolean isExplainEvent, final ConnectionDescription connectionDescription) {
            super(requestId, connectionDescription.getServerAddress());
            this.callback = callback;
            this.startTimeNanos = startTimeNanos;
            this.message = message;
            this.isExplainEvent = isExplainEvent;
            this.connectionDescription = connectionDescription;
        }
        
        @Override
        protected void callCallback(final ResponseBuffers responseBuffers, final Throwable throwableFromCallback) {
            try {
                if (throwableFromCallback != null) {
                    throw throwableFromCallback;
                }
                if (responseBuffers.getReplyHeader().isQueryFailure()) {
                    final BsonDocument errorDocument = new ReplyMessage<BsonDocument>(responseBuffers, new BsonDocumentCodec(), this.getRequestId()).getDocuments().get(0);
                    throw ProtocolHelper.getQueryFailureException(errorDocument, this.getServerAddress());
                }
                final QueryResult<T> result = new QueryResult<T>(QueryProtocol.this.namespace, new ReplyMessage<T>(responseBuffers, QueryProtocol.this.resultDecoder, this.getRequestId()), this.getServerAddress());
                QueryProtocol.this.sendQuerySucceededEvent(this.connectionDescription, this.startTimeNanos, this.message, this.isExplainEvent, responseBuffers, result);
                if (QueryProtocol.LOGGER.isDebugEnabled()) {
                    QueryProtocol.LOGGER.debug(String.format("Query results received %s documents with cursor %s", result.getResults().size(), result.getCursor()));
                }
                this.callback.onResult(result, null);
            }
            catch (Throwable t) {
                if (QueryProtocol.this.commandListener != null) {
                    ProtocolHelper.sendCommandFailedEvent(this.message, "find", this.connectionDescription, this.startTimeNanos, t, QueryProtocol.this.commandListener);
                }
                this.callback.onResult(null, t);
                try {
                    if (responseBuffers != null) {
                        responseBuffers.close();
                    }
                }
                catch (Throwable t2) {
                    QueryProtocol.LOGGER.debug("GetMore ResponseBuffer close exception", t2);
                }
            }
            finally {
                try {
                    if (responseBuffers != null) {
                        responseBuffers.close();
                    }
                }
                catch (Throwable t3) {
                    QueryProtocol.LOGGER.debug("GetMore ResponseBuffer close exception", t3);
                }
            }
        }
    }
}
