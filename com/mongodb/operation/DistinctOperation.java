package com.mongodb.operation;

import com.mongodb.client.model.*;
import com.mongodb.assertions.*;
import java.util.concurrent.*;
import com.mongodb.async.*;
import com.mongodb.binding.*;
import com.mongodb.internal.async.*;
import org.bson.codecs.*;
import com.mongodb.connection.*;
import com.mongodb.*;
import org.bson.*;

public class DistinctOperation<T> implements AsyncReadOperation<AsyncBatchCursor<T>>, ReadOperation<BatchCursor<T>>
{
    private static final String VALUES = "values";
    private final MongoNamespace namespace;
    private final String fieldName;
    private final Decoder<T> decoder;
    private BsonDocument filter;
    private long maxTimeMS;
    private ReadConcern readConcern;
    private Collation collation;
    
    public DistinctOperation(final MongoNamespace namespace, final String fieldName, final Decoder<T> decoder) {
        this.readConcern = ReadConcern.DEFAULT;
        this.namespace = Assertions.notNull("namespace", namespace);
        this.fieldName = Assertions.notNull("fieldName", fieldName);
        this.decoder = Assertions.notNull("decoder", decoder);
    }
    
    public BsonDocument getFilter() {
        return this.filter;
    }
    
    public DistinctOperation<T> filter(final BsonDocument filter) {
        this.filter = filter;
        return this;
    }
    
    public long getMaxTime(final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        return timeUnit.convert(this.maxTimeMS, TimeUnit.MILLISECONDS);
    }
    
    public DistinctOperation<T> maxTime(final long maxTime, final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        this.maxTimeMS = TimeUnit.MILLISECONDS.convert(maxTime, timeUnit);
        return this;
    }
    
    public ReadConcern getReadConcern() {
        return this.readConcern;
    }
    
    public DistinctOperation<T> readConcern(final ReadConcern readConcern) {
        this.readConcern = Assertions.notNull("readConcern", readConcern);
        return this;
    }
    
    public Collation getCollation() {
        return this.collation;
    }
    
    public DistinctOperation<T> collation(final Collation collation) {
        this.collation = collation;
        return this;
    }
    
    @Override
    public BatchCursor<T> execute(final ReadBinding binding) {
        return OperationHelper.withConnection(binding, (OperationHelper.CallableWithConnectionAndSource<BatchCursor<T>>)new OperationHelper.CallableWithConnectionAndSource<BatchCursor<T>>() {
            @Override
            public BatchCursor<T> call(final ConnectionSource source, final Connection connection) {
                OperationHelper.validateReadConcernAndCollation(connection, DistinctOperation.this.readConcern, DistinctOperation.this.collation);
                return CommandOperationHelper.executeWrappedCommandProtocol(binding, DistinctOperation.this.namespace.getDatabaseName(), DistinctOperation.this.getCommand(), DistinctOperation.this.createCommandDecoder(), connection, DistinctOperation.this.transformer(source, connection));
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
                    OperationHelper.validateReadConcernAndCollation(source, connection, DistinctOperation.this.readConcern, DistinctOperation.this.collation, new OperationHelper.AsyncCallableWithConnectionAndSource() {
                        @Override
                        public void call(final AsyncConnectionSource source, final AsyncConnection connection, final Throwable t) {
                            if (t != null) {
                                wrappedCallback.onResult(null, t);
                            }
                            else {
                                CommandOperationHelper.executeWrappedCommandProtocolAsync(binding, DistinctOperation.this.namespace.getDatabaseName(), DistinctOperation.this.getCommand(), DistinctOperation.this.createCommandDecoder(), connection, DistinctOperation.this.asyncTransformer(connection.getDescription()), (SingleResultCallback<Object>)wrappedCallback);
                            }
                        }
                    });
                }
            }
        });
    }
    
    private Codec<BsonDocument> createCommandDecoder() {
        return CommandResultDocumentCodec.create(this.decoder, "values");
    }
    
    private QueryResult<T> createQueryResult(final BsonDocument result, final ConnectionDescription description) {
        return new QueryResult<T>(this.namespace, BsonDocumentWrapperHelper.toList(result, "values"), 0L, description.getServerAddress());
    }
    
    private CommandOperationHelper.CommandTransformer<BsonDocument, BatchCursor<T>> transformer(final ConnectionSource source, final Connection connection) {
        return new CommandOperationHelper.CommandTransformer<BsonDocument, BatchCursor<T>>() {
            @Override
            public BatchCursor<T> apply(final BsonDocument result, final ServerAddress serverAddress) {
                final QueryResult<T> queryResult = (QueryResult<T>)DistinctOperation.this.createQueryResult(result, connection.getDescription());
                return new QueryBatchCursor<T>(queryResult, 0, 0, DistinctOperation.this.decoder, source);
            }
        };
    }
    
    private CommandOperationHelper.CommandTransformer<BsonDocument, AsyncBatchCursor<T>> asyncTransformer(final ConnectionDescription connectionDescription) {
        return new CommandOperationHelper.CommandTransformer<BsonDocument, AsyncBatchCursor<T>>() {
            @Override
            public AsyncBatchCursor<T> apply(final BsonDocument result, final ServerAddress serverAddress) {
                final QueryResult<T> queryResult = (QueryResult<T>)DistinctOperation.this.createQueryResult(result, connectionDescription);
                return new AsyncQueryBatchCursor<T>(queryResult, 0, 0, null);
            }
        };
    }
    
    private BsonDocument getCommand() {
        final BsonDocument commandDocument = new BsonDocument("distinct", new BsonString(this.namespace.getCollectionName()));
        commandDocument.put("key", new BsonString(this.fieldName));
        DocumentHelper.putIfNotNull(commandDocument, "query", this.filter);
        DocumentHelper.putIfNotZero(commandDocument, "maxTimeMS", this.maxTimeMS);
        if (!this.readConcern.isServerDefault()) {
            commandDocument.put("readConcern", this.readConcern.asDocument());
        }
        if (this.collation != null) {
            commandDocument.put("collation", this.collation.asDocument());
        }
        return commandDocument;
    }
}
