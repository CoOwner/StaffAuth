package com.mongodb.operation;

import org.bson.codecs.*;
import com.mongodb.client.model.*;
import com.mongodb.assertions.*;
import com.mongodb.binding.*;
import com.mongodb.async.*;
import com.mongodb.internal.async.*;
import org.bson.*;
import com.mongodb.*;
import com.mongodb.connection.*;

public class GroupOperation<T> implements AsyncReadOperation<AsyncBatchCursor<T>>, ReadOperation<BatchCursor<T>>
{
    private final MongoNamespace namespace;
    private final Decoder<T> decoder;
    private final BsonJavaScript reduceFunction;
    private final BsonDocument initial;
    private BsonDocument key;
    private BsonJavaScript keyFunction;
    private BsonDocument filter;
    private BsonJavaScript finalizeFunction;
    private Collation collation;
    
    public GroupOperation(final MongoNamespace namespace, final BsonJavaScript reduceFunction, final BsonDocument initial, final Decoder<T> decoder) {
        this.namespace = Assertions.notNull("namespace", namespace);
        this.reduceFunction = Assertions.notNull("reduceFunction", reduceFunction);
        this.initial = Assertions.notNull("initial", initial);
        this.decoder = Assertions.notNull("decoder", decoder);
    }
    
    public MongoNamespace getNamespace() {
        return this.namespace;
    }
    
    public Decoder<T> getDecoder() {
        return this.decoder;
    }
    
    public BsonDocument getKey() {
        return this.key;
    }
    
    public GroupOperation<T> key(final BsonDocument key) {
        this.key = key;
        return this;
    }
    
    public BsonJavaScript getKeyFunction() {
        return this.keyFunction;
    }
    
    public GroupOperation<T> keyFunction(final BsonJavaScript keyFunction) {
        this.keyFunction = keyFunction;
        return this;
    }
    
    public BsonDocument getInitial() {
        return this.initial;
    }
    
    public BsonJavaScript getReduceFunction() {
        return this.reduceFunction;
    }
    
    public BsonDocument getFilter() {
        return this.filter;
    }
    
    public GroupOperation<T> filter(final BsonDocument filter) {
        this.filter = filter;
        return this;
    }
    
    public BsonJavaScript getFinalizeFunction() {
        return this.finalizeFunction;
    }
    
    public GroupOperation<T> finalizeFunction(final BsonJavaScript finalizeFunction) {
        this.finalizeFunction = finalizeFunction;
        return this;
    }
    
    public Collation getCollation() {
        return this.collation;
    }
    
    public GroupOperation<T> collation(final Collation collation) {
        this.collation = collation;
        return this;
    }
    
    @Override
    public BatchCursor<T> execute(final ReadBinding binding) {
        return OperationHelper.withConnection(binding, (OperationHelper.CallableWithConnectionAndSource<BatchCursor<T>>)new OperationHelper.CallableWithConnectionAndSource<BatchCursor<T>>() {
            @Override
            public BatchCursor<T> call(final ConnectionSource connectionSource, final Connection connection) {
                OperationHelper.validateCollation(connection, GroupOperation.this.collation);
                return CommandOperationHelper.executeWrappedCommandProtocol(binding, GroupOperation.this.namespace.getDatabaseName(), GroupOperation.this.getCommand(), CommandResultDocumentCodec.create((Decoder<Object>)GroupOperation.this.decoder, "retval"), connection, GroupOperation.this.transformer(connectionSource, connection));
            }
        });
    }
    
    @Override
    public void executeAsync(final AsyncReadBinding binding, final SingleResultCallback<AsyncBatchCursor<T>> callback) {
        OperationHelper.withConnection(binding, new OperationHelper.AsyncCallableWithConnection() {
            @Override
            public void call(final AsyncConnection connection, final Throwable t) {
                final SingleResultCallback<AsyncBatchCursor<T>> errHandlingCallback = ErrorHandlingResultCallback.errorHandlingCallback(callback, OperationHelper.LOGGER);
                if (t != null) {
                    errHandlingCallback.onResult(null, t);
                }
                else {
                    final SingleResultCallback<AsyncBatchCursor<T>> wrappedCallback = OperationHelper.releasingCallback(errHandlingCallback, connection);
                    OperationHelper.validateCollation(connection, GroupOperation.this.collation, new OperationHelper.AsyncCallableWithConnection() {
                        @Override
                        public void call(final AsyncConnection connection, final Throwable t) {
                            if (t != null) {
                                wrappedCallback.onResult(null, t);
                            }
                            else {
                                CommandOperationHelper.executeWrappedCommandProtocolAsync(binding, GroupOperation.this.namespace.getDatabaseName(), GroupOperation.this.getCommand(), CommandResultDocumentCodec.create((Decoder<Object>)GroupOperation.this.decoder, "retval"), connection, GroupOperation.this.asyncTransformer(connection), (SingleResultCallback<Object>)wrappedCallback);
                            }
                        }
                    });
                }
            }
        });
    }
    
    private BsonDocument getCommand() {
        final BsonDocument commandDocument = new BsonDocument("ns", new BsonString(this.namespace.getCollectionName()));
        if (this.getKey() != null) {
            commandDocument.put("key", this.getKey());
        }
        else if (this.getKeyFunction() != null) {
            commandDocument.put("$keyf", this.getKeyFunction());
        }
        commandDocument.put("initial", this.getInitial());
        commandDocument.put("$reduce", this.getReduceFunction());
        if (this.getFinalizeFunction() != null) {
            commandDocument.put("finalize", this.getFinalizeFunction());
        }
        if (this.getFilter() != null) {
            commandDocument.put("cond", this.getFilter());
        }
        if (this.getCollation() != null) {
            commandDocument.put("collation", this.collation.asDocument());
        }
        return new BsonDocument("group", commandDocument);
    }
    
    private CommandOperationHelper.CommandTransformer<BsonDocument, BatchCursor<T>> transformer(final ConnectionSource source, final Connection connection) {
        return new CommandOperationHelper.CommandTransformer<BsonDocument, BatchCursor<T>>() {
            @Override
            public BatchCursor<T> apply(final BsonDocument result, final ServerAddress serverAddress) {
                return new QueryBatchCursor<T>(GroupOperation.this.createQueryResult(result, connection.getDescription()), 0, 0, GroupOperation.this.decoder, source);
            }
        };
    }
    
    private CommandOperationHelper.CommandTransformer<BsonDocument, AsyncBatchCursor<T>> asyncTransformer(final AsyncConnection connection) {
        return new CommandOperationHelper.CommandTransformer<BsonDocument, AsyncBatchCursor<T>>() {
            @Override
            public AsyncBatchCursor<T> apply(final BsonDocument result, final ServerAddress serverAddress) {
                return new AsyncQueryBatchCursor<T>(GroupOperation.this.createQueryResult(result, connection.getDescription()), 0, 0, GroupOperation.this.decoder);
            }
        };
    }
    
    private QueryResult<T> createQueryResult(final BsonDocument result, final ConnectionDescription description) {
        return new QueryResult<T>(this.namespace, BsonDocumentWrapperHelper.toList(result, "retval"), 0L, description.getServerAddress());
    }
}
