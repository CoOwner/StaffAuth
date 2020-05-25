package com.mongodb.operation;

import com.mongodb.*;
import com.mongodb.assertions.*;
import com.mongodb.binding.*;
import com.mongodb.async.*;
import com.mongodb.internal.async.*;
import com.mongodb.connection.*;
import org.bson.*;

public class RenameCollectionOperation implements AsyncWriteOperation<Void>, WriteOperation<Void>
{
    private final MongoNamespace originalNamespace;
    private final MongoNamespace newNamespace;
    private final WriteConcern writeConcern;
    private boolean dropTarget;
    
    @Deprecated
    public RenameCollectionOperation(final MongoNamespace originalNamespace, final MongoNamespace newNamespace) {
        this(originalNamespace, newNamespace, null);
    }
    
    public RenameCollectionOperation(final MongoNamespace originalNamespace, final MongoNamespace newNamespace, final WriteConcern writeConcern) {
        this.originalNamespace = Assertions.notNull("originalNamespace", originalNamespace);
        this.newNamespace = Assertions.notNull("newNamespace", newNamespace);
        this.writeConcern = writeConcern;
    }
    
    public WriteConcern getWriteConcern() {
        return this.writeConcern;
    }
    
    public boolean isDropTarget() {
        return this.dropTarget;
    }
    
    public RenameCollectionOperation dropTarget(final boolean dropTarget) {
        this.dropTarget = dropTarget;
        return this;
    }
    
    @Override
    public Void execute(final WriteBinding binding) {
        return OperationHelper.withConnection(binding, (OperationHelper.CallableWithConnection<Void>)new OperationHelper.CallableWithConnection<Void>() {
            @Override
            public Void call(final Connection connection) {
                return CommandOperationHelper.executeWrappedCommandProtocol(binding, "admin", RenameCollectionOperation.this.getCommand(connection.getDescription()), connection, WriteConcernHelper.writeConcernErrorTransformer());
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
                    CommandOperationHelper.executeWrappedCommandProtocolAsync(binding, "admin", RenameCollectionOperation.this.getCommand(connection.getDescription()), connection, WriteConcernHelper.writeConcernErrorTransformer(), (SingleResultCallback<Void>)OperationHelper.releasingCallback((SingleResultCallback<T>)errHandlingCallback, connection));
                }
            }
        });
    }
    
    private BsonDocument getCommand(final ConnectionDescription description) {
        final BsonDocument commandDocument = new BsonDocument("renameCollection", new BsonString(this.originalNamespace.getFullName())).append("to", new BsonString(this.newNamespace.getFullName())).append("dropTarget", BsonBoolean.valueOf(this.dropTarget));
        WriteConcernHelper.appendWriteConcernToCommand(this.writeConcern, commandDocument, description);
        return commandDocument;
    }
}
