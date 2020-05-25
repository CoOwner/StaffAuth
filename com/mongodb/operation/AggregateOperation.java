package com.mongodb.operation;

import java.util.*;
import org.bson.codecs.*;
import com.mongodb.client.model.*;
import com.mongodb.assertions.*;
import java.util.concurrent.*;
import com.mongodb.async.*;
import com.mongodb.binding.*;
import com.mongodb.internal.async.*;
import org.bson.*;
import com.mongodb.connection.*;
import com.mongodb.*;

public class AggregateOperation<T> implements AsyncReadOperation<AsyncBatchCursor<T>>, ReadOperation<BatchCursor<T>>
{
    private static final String RESULT = "result";
    private static final String FIRST_BATCH = "firstBatch";
    private final MongoNamespace namespace;
    private final List<BsonDocument> pipeline;
    private final Decoder<T> decoder;
    private Boolean allowDiskUse;
    private Integer batchSize;
    private long maxTimeMS;
    private Boolean useCursor;
    private ReadConcern readConcern;
    private Collation collation;
    
    public AggregateOperation(final MongoNamespace namespace, final List<BsonDocument> pipeline, final Decoder<T> decoder) {
        this.readConcern = ReadConcern.DEFAULT;
        this.namespace = Assertions.notNull("namespace", namespace);
        this.pipeline = Assertions.notNull("pipeline", pipeline);
        this.decoder = Assertions.notNull("decoder", decoder);
    }
    
    public List<BsonDocument> getPipeline() {
        return this.pipeline;
    }
    
    public Boolean getAllowDiskUse() {
        return this.allowDiskUse;
    }
    
    public AggregateOperation<T> allowDiskUse(final Boolean allowDiskUse) {
        this.allowDiskUse = allowDiskUse;
        return this;
    }
    
    public Integer getBatchSize() {
        return this.batchSize;
    }
    
    public AggregateOperation<T> batchSize(final Integer batchSize) {
        this.batchSize = batchSize;
        return this;
    }
    
    public long getMaxTime(final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        return timeUnit.convert(this.maxTimeMS, TimeUnit.MILLISECONDS);
    }
    
    public AggregateOperation<T> maxTime(final long maxTime, final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        this.maxTimeMS = TimeUnit.MILLISECONDS.convert(maxTime, timeUnit);
        return this;
    }
    
    public Boolean getUseCursor() {
        return this.useCursor;
    }
    
    public AggregateOperation<T> useCursor(final Boolean useCursor) {
        this.useCursor = useCursor;
        return this;
    }
    
    public ReadConcern getReadConcern() {
        return this.readConcern;
    }
    
    public AggregateOperation<T> readConcern(final ReadConcern readConcern) {
        this.readConcern = Assertions.notNull("readConcern", readConcern);
        return this;
    }
    
    public Collation getCollation() {
        return this.collation;
    }
    
    public AggregateOperation<T> collation(final Collation collation) {
        this.collation = collation;
        return this;
    }
    
    @Override
    public BatchCursor<T> execute(final ReadBinding binding) {
        return OperationHelper.withConnection(binding, (OperationHelper.CallableWithConnectionAndSource<BatchCursor<T>>)new OperationHelper.CallableWithConnectionAndSource<BatchCursor<T>>() {
            @Override
            public BatchCursor<T> call(final ConnectionSource source, final Connection connection) {
                OperationHelper.validateReadConcernAndCollation(connection, AggregateOperation.this.readConcern, AggregateOperation.this.collation);
                return CommandOperationHelper.executeWrappedCommandProtocol(binding, AggregateOperation.this.namespace.getDatabaseName(), AggregateOperation.this.getCommand(connection.getDescription()), CommandResultDocumentCodec.create((Decoder<Object>)AggregateOperation.this.decoder, AggregateOperation.this.getFieldNameWithResults(connection.getDescription())), connection, AggregateOperation.this.transformer(source, connection));
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
                else {
                    final SingleResultCallback<AsyncBatchCursor<T>> wrappedCallback = OperationHelper.releasingCallback(errHandlingCallback, source, connection);
                    OperationHelper.validateReadConcernAndCollation(source, connection, AggregateOperation.this.readConcern, AggregateOperation.this.collation, new OperationHelper.AsyncCallableWithConnectionAndSource() {
                        @Override
                        public void call(final AsyncConnectionSource source, final AsyncConnection connection, final Throwable t) {
                            if (t != null) {
                                wrappedCallback.onResult(null, t);
                            }
                            else {
                                CommandOperationHelper.executeWrappedCommandProtocolAsync(binding, AggregateOperation.this.namespace.getDatabaseName(), AggregateOperation.this.getCommand(connection.getDescription()), CommandResultDocumentCodec.create((Decoder<Object>)AggregateOperation.this.decoder, AggregateOperation.this.getFieldNameWithResults(connection.getDescription())), connection, AggregateOperation.this.asyncTransformer(source, connection), (SingleResultCallback<Object>)wrappedCallback);
                            }
                        }
                    });
                }
            }
        });
    }
    
    public ReadOperation<BsonDocument> asExplainableOperation(final ExplainVerbosity explainVerbosity) {
        return new AggregateExplainOperation(this.namespace, this.pipeline).allowDiskUse(this.allowDiskUse).maxTime(this.maxTimeMS, TimeUnit.MILLISECONDS);
    }
    
    public AsyncReadOperation<BsonDocument> asExplainableOperationAsync(final ExplainVerbosity explainVerbosity) {
        return new AggregateExplainOperation(this.namespace, this.pipeline).allowDiskUse(this.allowDiskUse).maxTime(this.maxTimeMS, TimeUnit.MILLISECONDS);
    }
    
    private boolean isInline(final ConnectionDescription description) {
        return (this.useCursor != null && !this.useCursor) || !OperationHelper.serverIsAtLeastVersionTwoDotSix(description);
    }
    
    private BsonDocument getCommand(final ConnectionDescription description) {
        final BsonDocument commandDocument = new BsonDocument("aggregate", new BsonString(this.namespace.getCollectionName()));
        commandDocument.put("pipeline", new BsonArray(this.pipeline));
        if (this.maxTimeMS > 0L) {
            commandDocument.put("maxTimeMS", new BsonInt64(this.maxTimeMS));
        }
        if ((this.useCursor == null || this.useCursor) && OperationHelper.serverIsAtLeastVersionTwoDotSix(description)) {
            final BsonDocument cursor = new BsonDocument();
            if (this.batchSize != null) {
                cursor.put("batchSize", new BsonInt32(this.batchSize));
            }
            commandDocument.put("cursor", cursor);
        }
        if (this.allowDiskUse != null) {
            commandDocument.put("allowDiskUse", BsonBoolean.valueOf(this.allowDiskUse));
        }
        if (!this.readConcern.isServerDefault()) {
            commandDocument.put("readConcern", this.readConcern.asDocument());
        }
        if (this.collation != null) {
            commandDocument.put("collation", this.collation.asDocument());
        }
        return commandDocument;
    }
    
    String getFieldNameWithResults(final ConnectionDescription description) {
        return ((this.useCursor == null || this.useCursor) && OperationHelper.serverIsAtLeastVersionTwoDotSix(description)) ? "firstBatch" : "result";
    }
    
    private QueryResult<T> createQueryResult(final BsonDocument result, final ConnectionDescription description) {
        if (this.isInline(description)) {
            return new QueryResult<T>(this.namespace, BsonDocumentWrapperHelper.toList(result, "result"), 0L, description.getServerAddress());
        }
        return OperationHelper.cursorDocumentToQueryResult(result.getDocument("cursor"), description.getServerAddress());
    }
    
    private CommandOperationHelper.CommandTransformer<BsonDocument, BatchCursor<T>> transformer(final ConnectionSource source, final Connection connection) {
        return new CommandOperationHelper.CommandTransformer<BsonDocument, BatchCursor<T>>() {
            @Override
            public BatchCursor<T> apply(final BsonDocument result, final ServerAddress serverAddress) {
                final QueryResult<T> queryResult = (QueryResult<T>)AggregateOperation.this.createQueryResult(result, connection.getDescription());
                return new QueryBatchCursor<T>(queryResult, 0, (AggregateOperation.this.batchSize != null) ? AggregateOperation.this.batchSize : 0, AggregateOperation.this.decoder, source);
            }
        };
    }
    
    private CommandOperationHelper.CommandTransformer<BsonDocument, AsyncBatchCursor<T>> asyncTransformer(final AsyncConnectionSource source, final AsyncConnection connection) {
        return new CommandOperationHelper.CommandTransformer<BsonDocument, AsyncBatchCursor<T>>() {
            @Override
            public AsyncBatchCursor<T> apply(final BsonDocument result, final ServerAddress serverAddress) {
                final QueryResult<T> queryResult = (QueryResult<T>)AggregateOperation.this.createQueryResult(result, connection.getDescription());
                return new AsyncQueryBatchCursor<T>(queryResult, 0, (AggregateOperation.this.batchSize != null) ? AggregateOperation.this.batchSize : 0, 0L, AggregateOperation.this.decoder, source, connection);
            }
        };
    }
}
