package com.mongodb.operation;

import com.mongodb.assertions.*;
import com.mongodb.*;
import com.mongodb.binding.*;
import com.mongodb.async.*;
import com.mongodb.internal.async.*;
import com.mongodb.connection.*;
import org.bson.*;

public class DropIndexOperation implements AsyncWriteOperation<Void>, WriteOperation<Void>
{
    private final MongoNamespace namespace;
    private final String indexName;
    private final WriteConcern writeConcern;
    
    @Deprecated
    public DropIndexOperation(final MongoNamespace namespace, final String indexName) {
        this(namespace, indexName, null);
    }
    
    @Deprecated
    public DropIndexOperation(final MongoNamespace namespace, final BsonDocument keys) {
        this(namespace, keys, null);
    }
    
    public DropIndexOperation(final MongoNamespace namespace, final String indexName, final WriteConcern writeConcern) {
        this.namespace = Assertions.notNull("namespace", namespace);
        this.indexName = Assertions.notNull("indexName", indexName);
        this.writeConcern = writeConcern;
    }
    
    public DropIndexOperation(final MongoNamespace namespace, final BsonDocument keys, final WriteConcern writeConcern) {
        this.namespace = Assertions.notNull("namespace", namespace);
        this.indexName = IndexHelper.generateIndexName(Assertions.notNull("keys", keys));
        this.writeConcern = writeConcern;
    }
    
    public WriteConcern getWriteConcern() {
        return this.writeConcern;
    }
    
    @Override
    public Void execute(final WriteBinding binding) {
        return OperationHelper.withConnection(binding, (OperationHelper.CallableWithConnection<Void>)new OperationHelper.CallableWithConnection<Void>() {
            @Override
            public Void call(final Connection connection) {
                try {
                    CommandOperationHelper.executeWrappedCommandProtocol(binding, DropIndexOperation.this.namespace.getDatabaseName(), DropIndexOperation.this.getCommand(connection.getDescription()), connection, WriteConcernHelper.writeConcernErrorTransformer());
                }
                catch (MongoCommandException e) {
                    CommandOperationHelper.rethrowIfNotNamespaceError(e);
                }
                return null;
            }
        });
    }
    
    @Override
    public void executeAsync(final AsyncWriteBinding binding, final SingleResultCallback<Void> callback) {
        OperationHelper.withConnection(binding, new OperationHelper.AsyncCallableWithConnection() {
            @Override
            public void call(final AsyncConnection connection, final Throwable t) {
                final SingleResultCallback<Void> errHandlingCallback = ErrorHandlingResultCallback.errorHandlingCallback(callback, OperationHelper.LOGGER);
                if (t != null) {
                    errHandlingCallback.onResult(null, t);
                }
                else {
                    final SingleResultCallback<Void> releasingCallback = OperationHelper.releasingCallback(errHandlingCallback, connection);
                    CommandOperationHelper.executeWrappedCommandProtocolAsync(binding, DropIndexOperation.this.namespace.getDatabaseName(), DropIndexOperation.this.getCommand(connection.getDescription()), connection, WriteConcernHelper.writeConcernErrorTransformer(), new SingleResultCallback<Void>() {
                        @Override
                        public void onResult(final Void result, final Throwable t) {
                            if (t != null && !CommandOperationHelper.isNamespaceError(t)) {
                                releasingCallback.onResult(null, t);
                            }
                            else {
                                releasingCallback.onResult(result, null);
                            }
                        }
                    });
                }
            }
        });
    }
    
    private BsonDocument getCommand(final ConnectionDescription description) {
        final BsonDocument commandDocument = new BsonDocument("dropIndexes", new BsonString(this.namespace.getCollectionName())).append("index", new BsonString(this.indexName));
        WriteConcernHelper.appendWriteConcernToCommand(this.writeConcern, commandDocument, description);
        return commandDocument;
    }
}
