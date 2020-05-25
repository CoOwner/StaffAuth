package com.mongodb.operation;

import com.mongodb.client.model.*;
import com.mongodb.assertions.*;
import java.util.concurrent.*;
import com.mongodb.async.*;
import com.mongodb.internal.async.*;
import com.mongodb.binding.*;
import com.mongodb.connection.*;
import org.bson.codecs.*;
import org.bson.*;
import java.util.*;
import com.mongodb.*;

public class FindOperation<T> implements AsyncReadOperation<AsyncBatchCursor<T>>, ReadOperation<BatchCursor<T>>
{
    private static final String FIRST_BATCH = "firstBatch";
    private final MongoNamespace namespace;
    private final Decoder<T> decoder;
    private BsonDocument filter;
    private int batchSize;
    private int limit;
    private BsonDocument modifiers;
    private BsonDocument projection;
    private long maxTimeMS;
    private long maxAwaitTimeMS;
    private int skip;
    private BsonDocument sort;
    private CursorType cursorType;
    private boolean slaveOk;
    private boolean oplogReplay;
    private boolean noCursorTimeout;
    private boolean partial;
    private ReadConcern readConcern;
    private Collation collation;
    private static final Map<String, String> META_OPERATOR_TO_COMMAND_FIELD_MAP;
    
    public FindOperation(final MongoNamespace namespace, final Decoder<T> decoder) {
        this.cursorType = CursorType.NonTailable;
        this.readConcern = ReadConcern.DEFAULT;
        this.namespace = Assertions.notNull("namespace", namespace);
        this.decoder = Assertions.notNull("decoder", decoder);
    }
    
    public MongoNamespace getNamespace() {
        return this.namespace;
    }
    
    public Decoder<T> getDecoder() {
        return this.decoder;
    }
    
    public BsonDocument getFilter() {
        return this.filter;
    }
    
    public FindOperation<T> filter(final BsonDocument filter) {
        this.filter = filter;
        return this;
    }
    
    public int getBatchSize() {
        return this.batchSize;
    }
    
    public FindOperation<T> batchSize(final int batchSize) {
        this.batchSize = batchSize;
        return this;
    }
    
    public int getLimit() {
        return this.limit;
    }
    
    public FindOperation<T> limit(final int limit) {
        this.limit = limit;
        return this;
    }
    
    public BsonDocument getModifiers() {
        return this.modifiers;
    }
    
    public FindOperation<T> modifiers(final BsonDocument modifiers) {
        this.modifiers = modifiers;
        return this;
    }
    
    public BsonDocument getProjection() {
        return this.projection;
    }
    
    public FindOperation<T> projection(final BsonDocument projection) {
        this.projection = projection;
        return this;
    }
    
    public long getMaxTime(final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        return timeUnit.convert(this.maxTimeMS, TimeUnit.MILLISECONDS);
    }
    
    public FindOperation<T> maxTime(final long maxTime, final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        Assertions.isTrueArgument("maxTime >= 0", maxTime >= 0L);
        this.maxTimeMS = TimeUnit.MILLISECONDS.convert(maxTime, timeUnit);
        return this;
    }
    
    public long getMaxAwaitTime(final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        return timeUnit.convert(this.maxAwaitTimeMS, TimeUnit.MILLISECONDS);
    }
    
    public FindOperation<T> maxAwaitTime(final long maxAwaitTime, final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        Assertions.isTrueArgument("maxAwaitTime >= 0", maxAwaitTime >= 0L);
        this.maxAwaitTimeMS = TimeUnit.MILLISECONDS.convert(maxAwaitTime, timeUnit);
        return this;
    }
    
    public int getSkip() {
        return this.skip;
    }
    
    public FindOperation<T> skip(final int skip) {
        this.skip = skip;
        return this;
    }
    
    public BsonDocument getSort() {
        return this.sort;
    }
    
    public FindOperation<T> sort(final BsonDocument sort) {
        this.sort = sort;
        return this;
    }
    
    public CursorType getCursorType() {
        return this.cursorType;
    }
    
    public FindOperation<T> cursorType(final CursorType cursorType) {
        this.cursorType = Assertions.notNull("cursorType", cursorType);
        return this;
    }
    
    public boolean isSlaveOk() {
        return this.slaveOk;
    }
    
    public FindOperation<T> slaveOk(final boolean slaveOk) {
        this.slaveOk = slaveOk;
        return this;
    }
    
    public boolean isOplogReplay() {
        return this.oplogReplay;
    }
    
    public FindOperation<T> oplogReplay(final boolean oplogReplay) {
        this.oplogReplay = oplogReplay;
        return this;
    }
    
    public boolean isNoCursorTimeout() {
        return this.noCursorTimeout;
    }
    
    public FindOperation<T> noCursorTimeout(final boolean noCursorTimeout) {
        this.noCursorTimeout = noCursorTimeout;
        return this;
    }
    
    public boolean isPartial() {
        return this.partial;
    }
    
    public FindOperation<T> partial(final boolean partial) {
        this.partial = partial;
        return this;
    }
    
    public ReadConcern getReadConcern() {
        return this.readConcern;
    }
    
    public FindOperation<T> readConcern(final ReadConcern readConcern) {
        this.readConcern = Assertions.notNull("readConcern", readConcern);
        return this;
    }
    
    public Collation getCollation() {
        return this.collation;
    }
    
    public FindOperation<T> collation(final Collation collation) {
        this.collation = collation;
        return this;
    }
    
    @Override
    public BatchCursor<T> execute(final ReadBinding binding) {
        return OperationHelper.withConnection(binding, (OperationHelper.CallableWithConnectionAndSource<BatchCursor<T>>)new OperationHelper.CallableWithConnectionAndSource<BatchCursor<T>>() {
            @Override
            public BatchCursor<T> call(final ConnectionSource source, final Connection connection) {
                if (OperationHelper.serverIsAtLeastVersionThreeDotTwo(connection.getDescription())) {
                    try {
                        OperationHelper.validateReadConcernAndCollation(connection, FindOperation.this.readConcern, FindOperation.this.collation);
                        return CommandOperationHelper.executeWrappedCommandProtocol(binding, FindOperation.this.namespace.getDatabaseName(), FindOperation.this.wrapInExplainIfNecessary(FindOperation.this.getCommand()), CommandResultDocumentCodec.create((Decoder<Object>)FindOperation.this.decoder, "firstBatch"), connection, FindOperation.this.transformer(source, connection));
                    }
                    catch (MongoCommandException e) {
                        throw new MongoQueryException(e.getServerAddress(), e.getErrorCode(), e.getErrorMessage());
                    }
                }
                OperationHelper.validateReadConcernAndCollation(connection, FindOperation.this.readConcern, FindOperation.this.collation);
                final QueryResult<T> queryResult = connection.query(FindOperation.this.namespace, FindOperation.this.asDocument(connection.getDescription(), binding.getReadPreference()), FindOperation.this.projection, FindOperation.this.skip, FindOperation.this.limit, FindOperation.this.batchSize, FindOperation.this.isSlaveOk() || binding.getReadPreference().isSlaveOk(), FindOperation.this.isTailableCursor(), FindOperation.this.isAwaitData(), FindOperation.this.isNoCursorTimeout(), FindOperation.this.isPartial(), FindOperation.this.isOplogReplay(), FindOperation.this.decoder);
                return new QueryBatchCursor<T>(queryResult, FindOperation.this.limit, FindOperation.this.batchSize, FindOperation.this.getMaxTimeForCursor(), FindOperation.this.decoder, source, connection);
            }
        });
    }
    
    @Override
    public void executeAsync(final AsyncReadBinding binding, final SingleResultCallback<AsyncBatchCursor<T>> callback) {
        OperationHelper.withConnection(binding, new OperationHelper.AsyncCallableWithConnectionAndSource() {
            @Override
            public void call(final AsyncConnectionSource source, final AsyncConnection connection, final Throwable t) {
                final SingleResultCallback<AsyncBatchCursor<T>> errHandlingCallback = ErrorHandlingResultCallback.errorHandlingCallback(callback, OperationHelper.LOGGER);
                if (t != null) {
                    errHandlingCallback.onResult(null, t);
                }
                else if (OperationHelper.serverIsAtLeastVersionThreeDotTwo(connection.getDescription())) {
                    final SingleResultCallback<AsyncBatchCursor<T>> wrappedCallback = OperationHelper.releasingCallback((SingleResultCallback<AsyncBatchCursor<T>>)exceptionTransformingCallback((SingleResultCallback<Object>)errHandlingCallback), source, connection);
                    OperationHelper.validateReadConcernAndCollation(source, connection, FindOperation.this.readConcern, FindOperation.this.collation, new OperationHelper.AsyncCallableWithConnectionAndSource() {
                        @Override
                        public void call(final AsyncConnectionSource source, final AsyncConnection connection, final Throwable t) {
                            if (t != null) {
                                wrappedCallback.onResult(null, t);
                            }
                            else {
                                CommandOperationHelper.executeWrappedCommandProtocolAsync(binding, FindOperation.this.namespace.getDatabaseName(), FindOperation.this.wrapInExplainIfNecessary(FindOperation.this.getCommand()), CommandResultDocumentCodec.create((Decoder<Object>)FindOperation.this.decoder, "firstBatch"), connection, FindOperation.this.asyncTransformer(source, connection), (SingleResultCallback<Object>)wrappedCallback);
                            }
                        }
                    });
                }
                else {
                    final SingleResultCallback<AsyncBatchCursor<T>> wrappedCallback = OperationHelper.releasingCallback(errHandlingCallback, source, connection);
                    OperationHelper.validateReadConcernAndCollation(source, connection, FindOperation.this.readConcern, FindOperation.this.collation, new OperationHelper.AsyncCallableWithConnectionAndSource() {
                        @Override
                        public void call(final AsyncConnectionSource source, final AsyncConnection connection, final Throwable t) {
                            if (t != null) {
                                wrappedCallback.onResult(null, t);
                            }
                            else {
                                connection.queryAsync(FindOperation.this.namespace, FindOperation.this.asDocument(connection.getDescription(), binding.getReadPreference()), FindOperation.this.projection, FindOperation.this.skip, FindOperation.this.limit, FindOperation.this.batchSize, FindOperation.this.isSlaveOk() || binding.getReadPreference().isSlaveOk(), FindOperation.this.isTailableCursor(), FindOperation.this.isAwaitData(), FindOperation.this.isNoCursorTimeout(), FindOperation.this.isPartial(), FindOperation.this.isOplogReplay(), FindOperation.this.decoder, (SingleResultCallback<QueryResult<Object>>)new SingleResultCallback<QueryResult<T>>() {
                                    @Override
                                    public void onResult(final QueryResult<T> result, final Throwable t) {
                                        if (t != null) {
                                            wrappedCallback.onResult(null, t);
                                        }
                                        else {
                                            wrappedCallback.onResult(new AsyncQueryBatchCursor<T>(result, FindOperation.this.limit, FindOperation.this.batchSize, FindOperation.this.getMaxTimeForCursor(), FindOperation.this.decoder, source, connection), null);
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
    }
    
    private static <T> SingleResultCallback<T> exceptionTransformingCallback(final SingleResultCallback<T> callback) {
        return new SingleResultCallback<T>() {
            @Override
            public void onResult(final T result, final Throwable t) {
                if (t != null) {
                    if (t instanceof MongoCommandException) {
                        final MongoCommandException commandException = (MongoCommandException)t;
                        callback.onResult(result, new MongoQueryException(commandException.getServerAddress(), commandException.getErrorCode(), commandException.getErrorMessage()));
                    }
                    else {
                        callback.onResult(result, t);
                    }
                }
                else {
                    callback.onResult(result, null);
                }
            }
        };
    }
    
    public ReadOperation<BsonDocument> asExplainableOperation(final ExplainVerbosity explainVerbosity) {
        Assertions.notNull("explainVerbosity", explainVerbosity);
        return new ReadOperation<BsonDocument>() {
            @Override
            public BsonDocument execute(final ReadBinding binding) {
                return OperationHelper.withConnection(binding, (OperationHelper.CallableWithConnectionAndSource<BsonDocument>)new OperationHelper.CallableWithConnectionAndSource<BsonDocument>() {
                    @Override
                    public BsonDocument call(final ConnectionSource connectionSource, final Connection connection) {
                        final ReadBinding singleConnectionBinding = new SingleConnectionReadBinding(binding.getReadPreference(), connectionSource.getServerDescription(), connection);
                        try {
                            if (OperationHelper.serverIsAtLeastVersionThreeDotTwo(connection.getDescription())) {
                                try {
                                    return new CommandReadOperation<BsonDocument>(FindOperation.this.getNamespace().getDatabaseName(), new BsonDocument("explain", FindOperation.this.getCommand()), new BsonDocumentCodec()).execute(singleConnectionBinding);
                                }
                                catch (MongoCommandException e) {
                                    throw new MongoQueryException(e.getServerAddress(), e.getErrorCode(), e.getErrorMessage());
                                }
                            }
                            final BatchCursor<BsonDocument> cursor = FindOperation.this.createExplainableQueryOperation().execute(singleConnectionBinding);
                            try {
                                return cursor.next().iterator().next();
                            }
                            finally {
                                cursor.close();
                            }
                        }
                        finally {
                            singleConnectionBinding.release();
                        }
                    }
                });
            }
        };
    }
    
    public AsyncReadOperation<BsonDocument> asExplainableOperationAsync(final ExplainVerbosity explainVerbosity) {
        Assertions.notNull("explainVerbosity", explainVerbosity);
        return new AsyncReadOperation<BsonDocument>() {
            @Override
            public void executeAsync(final AsyncReadBinding binding, final SingleResultCallback<BsonDocument> callback) {
                OperationHelper.withConnection(binding, new OperationHelper.AsyncCallableWithConnectionAndSource() {
                    @Override
                    public void call(final AsyncConnectionSource connectionSource, final AsyncConnection connection, final Throwable t) {
                        final SingleResultCallback<BsonDocument> errHandlingCallback = ErrorHandlingResultCallback.errorHandlingCallback(callback, OperationHelper.LOGGER);
                        if (t != null) {
                            errHandlingCallback.onResult(null, t);
                        }
                        else {
                            final AsyncReadBinding singleConnectionReadBinding = new AsyncSingleConnectionReadBinding(binding.getReadPreference(), connectionSource.getServerDescription(), connection);
                            if (OperationHelper.serverIsAtLeastVersionThreeDotTwo(connection.getDescription())) {
                                new CommandReadOperation<Object>(FindOperation.this.namespace.getDatabaseName(), new BsonDocument("explain", FindOperation.this.getCommand()), (Decoder<Object>)new BsonDocumentCodec()).executeAsync(singleConnectionReadBinding, OperationHelper.releasingCallback(exceptionTransformingCallback((SingleResultCallback<Object>)errHandlingCallback), singleConnectionReadBinding, connectionSource, connection));
                            }
                            else {
                                FindOperation.this.createExplainableQueryOperation().executeAsync(singleConnectionReadBinding, OperationHelper.releasingCallback((SingleResultCallback<AsyncBatchCursor<T>>)new ExplainResultCallback(errHandlingCallback), singleConnectionReadBinding, connectionSource, connection));
                            }
                        }
                    }
                });
            }
        };
    }
    
    private FindOperation<BsonDocument> createExplainableQueryOperation() {
        final FindOperation<BsonDocument> explainFindOperation = new FindOperation<BsonDocument>(this.namespace, new BsonDocumentCodec());
        final BsonDocument explainModifiers = new BsonDocument();
        if (this.modifiers != null) {
            explainModifiers.putAll(this.modifiers);
        }
        explainModifiers.append("$explain", BsonBoolean.TRUE);
        return explainFindOperation.filter(this.filter).projection(this.projection).sort(this.sort).skip(this.skip).limit(Math.abs(this.limit) * -1).modifiers(explainModifiers);
    }
    
    private BsonDocument asDocument(final ConnectionDescription connectionDescription, final ReadPreference readPreference) {
        BsonDocument document = new BsonDocument();
        if (this.modifiers != null) {
            document.putAll(this.modifiers);
        }
        if (this.sort != null) {
            document.put("$orderby", this.sort);
        }
        if (this.maxTimeMS > 0L) {
            document.put("$maxTimeMS", new BsonInt64(this.maxTimeMS));
        }
        if (connectionDescription.getServerType() == ServerType.SHARD_ROUTER && !readPreference.equals(ReadPreference.primary())) {
            document.put("$readPreference", readPreference.toDocument());
        }
        if (document.isEmpty()) {
            document = ((this.filter != null) ? this.filter : new BsonDocument());
        }
        else if (this.filter != null) {
            document.put("$query", this.filter);
        }
        else if (!document.containsKey("$query")) {
            document.put("$query", new BsonDocument());
        }
        return document;
    }
    
    private BsonDocument getCommand() {
        final BsonDocument commandDocument = new BsonDocument("find", new BsonString(this.namespace.getCollectionName()));
        if (this.modifiers != null) {
            for (final Map.Entry<String, BsonValue> cur : this.modifiers.entrySet()) {
                final String commandFieldName = FindOperation.META_OPERATOR_TO_COMMAND_FIELD_MAP.get(cur.getKey());
                if (commandFieldName != null) {
                    commandDocument.append(commandFieldName, cur.getValue());
                }
            }
        }
        if (this.filter != null) {
            commandDocument.put("filter", this.filter);
        }
        if (this.sort != null) {
            commandDocument.put("sort", this.sort);
        }
        if (this.projection != null) {
            commandDocument.put("projection", this.projection);
        }
        if (this.skip > 0) {
            commandDocument.put("skip", new BsonInt32(this.skip));
        }
        if (this.limit != 0) {
            commandDocument.put("limit", new BsonInt32(Math.abs(this.limit)));
        }
        if (this.limit >= 0) {
            if (this.batchSize < 0 && Math.abs(this.batchSize) < this.limit) {
                commandDocument.put("limit", new BsonInt32(Math.abs(this.batchSize)));
            }
            else if (this.batchSize != 0) {
                commandDocument.put("batchSize", new BsonInt32(Math.abs(this.batchSize)));
            }
        }
        if (this.limit < 0 || this.batchSize < 0) {
            commandDocument.put("singleBatch", BsonBoolean.TRUE);
        }
        if (this.maxTimeMS > 0L) {
            commandDocument.put("maxTimeMS", new BsonInt64(this.maxTimeMS));
        }
        if (this.isTailableCursor()) {
            commandDocument.put("tailable", BsonBoolean.TRUE);
        }
        if (this.isAwaitData()) {
            commandDocument.put("awaitData", BsonBoolean.TRUE);
        }
        if (this.oplogReplay) {
            commandDocument.put("oplogReplay", BsonBoolean.TRUE);
        }
        if (this.noCursorTimeout) {
            commandDocument.put("noCursorTimeout", BsonBoolean.TRUE);
        }
        if (this.partial) {
            commandDocument.put("allowPartialResults", BsonBoolean.TRUE);
        }
        if (!this.readConcern.isServerDefault()) {
            commandDocument.put("readConcern", this.readConcern.asDocument());
        }
        if (this.collation != null) {
            commandDocument.put("collation", this.collation.asDocument());
        }
        return commandDocument;
    }
    
    private BsonDocument wrapInExplainIfNecessary(final BsonDocument commandDocument) {
        if (this.isExplain()) {
            return new BsonDocument("explain", commandDocument);
        }
        return commandDocument;
    }
    
    private boolean isExplain() {
        return this.modifiers != null && this.modifiers.get("$explain", BsonBoolean.FALSE).equals(BsonBoolean.TRUE);
    }
    
    private boolean isTailableCursor() {
        return this.cursorType.isTailable();
    }
    
    private boolean isAwaitData() {
        return this.cursorType == CursorType.TailableAwait;
    }
    
    private CommandOperationHelper.CommandTransformer<BsonDocument, BatchCursor<T>> transformer(final ConnectionSource source, final Connection connection) {
        return new CommandOperationHelper.CommandTransformer<BsonDocument, BatchCursor<T>>() {
            @Override
            public BatchCursor<T> apply(final BsonDocument result, final ServerAddress serverAddress) {
                final QueryResult<T> queryResult = (QueryResult<T>)FindOperation.this.documentToQueryResult(result, serverAddress);
                return new QueryBatchCursor<T>(queryResult, FindOperation.this.limit, FindOperation.this.batchSize, FindOperation.this.getMaxTimeForCursor(), FindOperation.this.decoder, source, connection);
            }
        };
    }
    
    private long getMaxTimeForCursor() {
        return (this.cursorType == CursorType.TailableAwait) ? this.maxAwaitTimeMS : 0L;
    }
    
    private CommandOperationHelper.CommandTransformer<BsonDocument, AsyncBatchCursor<T>> asyncTransformer(final AsyncConnectionSource source, final AsyncConnection connection) {
        return new CommandOperationHelper.CommandTransformer<BsonDocument, AsyncBatchCursor<T>>() {
            @Override
            public AsyncBatchCursor<T> apply(final BsonDocument result, final ServerAddress serverAddress) {
                final QueryResult<T> queryResult = (QueryResult<T>)FindOperation.this.documentToQueryResult(result, serverAddress);
                return new AsyncQueryBatchCursor<T>(queryResult, FindOperation.this.limit, FindOperation.this.batchSize, FindOperation.this.getMaxTimeForCursor(), FindOperation.this.decoder, source, connection);
            }
        };
    }
    
    private QueryResult<T> documentToQueryResult(final BsonDocument result, final ServerAddress serverAddress) {
        QueryResult<T> queryResult;
        if (this.isExplain()) {
            final T decodedDocument = this.decoder.decode(new BsonDocumentReader(result), DecoderContext.builder().build());
            queryResult = new QueryResult<T>(this.getNamespace(), Collections.singletonList(decodedDocument), 0L, serverAddress);
        }
        else {
            queryResult = OperationHelper.cursorDocumentToQueryResult(result.getDocument("cursor"), serverAddress);
        }
        return queryResult;
    }
    
    static {
        (META_OPERATOR_TO_COMMAND_FIELD_MAP = new HashMap<String, String>()).put("$query", "filter");
        FindOperation.META_OPERATOR_TO_COMMAND_FIELD_MAP.put("$orderby", "sort");
        FindOperation.META_OPERATOR_TO_COMMAND_FIELD_MAP.put("$hint", "hint");
        FindOperation.META_OPERATOR_TO_COMMAND_FIELD_MAP.put("$comment", "comment");
        FindOperation.META_OPERATOR_TO_COMMAND_FIELD_MAP.put("$maxScan", "maxScan");
        FindOperation.META_OPERATOR_TO_COMMAND_FIELD_MAP.put("$maxTimeMS", "maxTimeMS");
        FindOperation.META_OPERATOR_TO_COMMAND_FIELD_MAP.put("$max", "max");
        FindOperation.META_OPERATOR_TO_COMMAND_FIELD_MAP.put("$min", "min");
        FindOperation.META_OPERATOR_TO_COMMAND_FIELD_MAP.put("$returnKey", "returnKey");
        FindOperation.META_OPERATOR_TO_COMMAND_FIELD_MAP.put("$showDiskLoc", "showRecordId");
        FindOperation.META_OPERATOR_TO_COMMAND_FIELD_MAP.put("$snapshot", "snapshot");
    }
    
    private static class ExplainResultCallback implements SingleResultCallback<AsyncBatchCursor<BsonDocument>>
    {
        private final SingleResultCallback<BsonDocument> callback;
        
        public ExplainResultCallback(final SingleResultCallback<BsonDocument> callback) {
            this.callback = callback;
        }
        
        @Override
        public void onResult(final AsyncBatchCursor<BsonDocument> cursor, final Throwable t) {
            if (t != null) {
                this.callback.onResult(null, t);
            }
            else {
                cursor.next(new SingleResultCallback<List<BsonDocument>>() {
                    @Override
                    public void onResult(final List<BsonDocument> result, final Throwable t) {
                        cursor.close();
                        if (t != null) {
                            ExplainResultCallback.this.callback.onResult(null, t);
                        }
                        else if (result == null || result.size() == 0) {
                            ExplainResultCallback.this.callback.onResult(null, new MongoInternalException("Expected explain result"));
                        }
                        else {
                            ExplainResultCallback.this.callback.onResult(result.get(0), null);
                        }
                    }
                });
            }
        }
    }
}
