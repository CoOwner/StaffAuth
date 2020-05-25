package com.mongodb.operation;

import com.mongodb.assertions.*;
import com.mongodb.*;
import com.mongodb.binding.*;
import com.mongodb.async.*;
import com.mongodb.internal.async.*;
import com.mongodb.connection.*;
import org.bson.*;

public class DropCollectionOperation implements AsyncWriteOperation<Void>, WriteOperation<Void>
{
    private final MongoNamespace namespace;
    private final WriteConcern writeConcern;
    
    @Deprecated
    public DropCollectionOperation(final MongoNamespace namespace) {
        this(namespace, null);
    }
    
    public DropCollectionOperation(final MongoNamespace namespace, final WriteConcern writeConcern) {
        this.namespace = Assertions.notNull("namespace", namespace);
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
                    CommandOperationHelper.executeWrappedCommandProtocol(binding, DropCollectionOperation.this.namespace.getDatabaseName(), DropCollectionOperation.this.getCommand(connection.getDescription()), connection, WriteConcernHelper.writeConcernErrorTransformer());
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
                    CommandOperationHelper.executeWrappedCommandProtocolAsync(binding, DropCollectionOperation.this.namespace.getDatabaseName(), DropCollectionOperation.this.getCommand(connection.getDescription()), connection, WriteConcernHelper.writeConcernErrorTransformer(), new SingleResultCallback<Void>() {
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
        final BsonDocument commandDocument = new BsonDocument("drop", new BsonString(this.namespace.getCollectionName()));
        WriteConcernHelper.appendWriteConcernToCommand(this.writeConcern, commandDocument, description);
        return commandDocument;
    }
}
