package com.mongodb.operation;

import com.mongodb.*;
import com.mongodb.assertions.*;
import com.mongodb.binding.*;
import com.mongodb.async.*;
import com.mongodb.internal.async.*;
import com.mongodb.connection.*;
import org.bson.*;

public class DropDatabaseOperation implements AsyncWriteOperation<Void>, WriteOperation<Void>
{
    private static final BsonDocument DROP_DATABASE;
    private final String databaseName;
    private final WriteConcern writeConcern;
    
    @Deprecated
    public DropDatabaseOperation(final String databaseName) {
        this(databaseName, null);
    }
    
    public DropDatabaseOperation(final String databaseName, final WriteConcern writeConcern) {
        this.databaseName = Assertions.notNull("databaseName", databaseName);
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
                CommandOperationHelper.executeWrappedCommandProtocol(binding, DropDatabaseOperation.this.databaseName, DropDatabaseOperation.this.getCommand(connection.getDescription()), connection, WriteConcernHelper.writeConcernErrorTransformer());
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
                    CommandOperationHelper.executeWrappedCommandProtocolAsync(binding, DropDatabaseOperation.this.databaseName, DropDatabaseOperation.this.getCommand(connection.getDescription()), connection, WriteConcernHelper.writeConcernErrorTransformer(), (SingleResultCallback<Void>)OperationHelper.releasingCallback((SingleResultCallback<T>)errHandlingCallback, connection));
                }
            }
        });
    }
    
    private BsonDocument getCommand(final ConnectionDescription description) {
        final BsonDocument commandDocument = new BsonDocument("dropDatabase", new BsonInt32(1));
        WriteConcernHelper.appendWriteConcernToCommand(this.writeConcern, commandDocument, description);
        return commandDocument;
    }
    
    static {
        DROP_DATABASE = new BsonDocument("dropDatabase", new BsonInt32(1));
    }
}
