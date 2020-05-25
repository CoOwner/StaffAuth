package com.mongodb.operation;

import com.mongodb.assertions.*;
import com.mongodb.bulk.*;
import java.util.*;
import com.mongodb.binding.*;
import com.mongodb.async.*;
import com.mongodb.internal.async.*;
import com.mongodb.*;
import org.bson.*;
import com.mongodb.connection.*;

public class DropUserOperation implements AsyncWriteOperation<Void>, WriteOperation<Void>
{
    private final String databaseName;
    private final String userName;
    private final WriteConcern writeConcern;
    
    @Deprecated
    public DropUserOperation(final String databaseName, final String userName) {
        this(databaseName, userName, null);
    }
    
    public DropUserOperation(final String databaseName, final String userName, final WriteConcern writeConcern) {
        this.databaseName = Assertions.notNull("databaseName", databaseName);
        this.userName = Assertions.notNull("userName", userName);
        this.writeConcern = writeConcern;
    }
    
    @Override
    public Void execute(final WriteBinding binding) {
        return OperationHelper.withConnection(binding, (OperationHelper.CallableWithConnection<Void>)new OperationHelper.CallableWithConnection<Void>() {
            @Override
            public Void call(final Connection connection) {
                if (OperationHelper.serverIsAtLeastVersionTwoDotSix(connection.getDescription())) {
                    try {
                        CommandOperationHelper.executeWrappedCommandProtocol(binding, DropUserOperation.this.databaseName, DropUserOperation.this.getCommand(connection.getDescription()), connection, WriteConcernHelper.writeConcernErrorTransformer());
                    }
                    catch (MongoCommandException e) {
                        UserOperationHelper.translateUserCommandException(e);
                    }
                }
                else {
                    connection.delete(DropUserOperation.this.getNamespace(), true, WriteConcern.ACKNOWLEDGED, Arrays.asList(DropUserOperation.this.getDeleteRequest()));
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
                    final SingleResultCallback<Void> wrappedCallback = OperationHelper.releasingCallback(errHandlingCallback, connection);
                    if (OperationHelper.serverIsAtLeastVersionTwoDotSix(connection.getDescription())) {
                        CommandOperationHelper.executeWrappedCommandProtocolAsync(binding, DropUserOperation.this.databaseName, DropUserOperation.this.getCommand(connection.getDescription()), connection, WriteConcernHelper.writeConcernErrorTransformer(), UserOperationHelper.userCommandCallback(wrappedCallback));
                    }
                    else {
                        connection.deleteAsync(DropUserOperation.this.getNamespace(), true, WriteConcern.ACKNOWLEDGED, Arrays.asList(DropUserOperation.this.getDeleteRequest()), new SingleResultCallback<WriteConcernResult>() {
                            @Override
                            public void onResult(final WriteConcernResult result, final Throwable t) {
                                wrappedCallback.onResult(null, t);
                            }
                        });
                    }
                }
            }
        });
    }
    
    private MongoNamespace getNamespace() {
        return new MongoNamespace(this.databaseName, "system.users");
    }
    
    private DeleteRequest getDeleteRequest() {
        return new DeleteRequest(new BsonDocument("user", new BsonString(this.userName)));
    }
    
    private BsonDocument getCommand(final ConnectionDescription description) {
        final BsonDocument commandDocument = new BsonDocument("dropUser", new BsonString(this.userName));
        WriteConcernHelper.appendWriteConcernToCommand(this.writeConcern, commandDocument, description);
        return commandDocument;
    }
}
