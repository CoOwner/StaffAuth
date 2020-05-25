package com.mongodb.operation;

import com.mongodb.assertions.*;
import org.bson.*;
import org.bson.codecs.*;
import com.mongodb.binding.*;
import com.mongodb.async.*;
import com.mongodb.internal.async.*;
import com.mongodb.connection.*;
import com.mongodb.*;

public class UserExistsOperation implements AsyncReadOperation<Boolean>, ReadOperation<Boolean>
{
    private final String databaseName;
    private final String userName;
    
    public UserExistsOperation(final String databaseName, final String userName) {
        this.databaseName = Assertions.notNull("databaseName", databaseName);
        this.userName = Assertions.notNull("userName", userName);
    }
    
    @Override
    public Boolean execute(final ReadBinding binding) {
        return OperationHelper.withConnection(binding, (OperationHelper.CallableWithConnection<Boolean>)new OperationHelper.CallableWithConnection<Boolean>() {
            @Override
            public Boolean call(final Connection connection) {
                if (OperationHelper.serverIsAtLeastVersionTwoDotSix(connection.getDescription())) {
                    return CommandOperationHelper.executeWrappedCommandProtocol(binding, UserExistsOperation.this.databaseName, UserExistsOperation.this.getCommand(), connection, UserExistsOperation.this.transformer());
                }
                return UserExistsOperation.this.transformQueryResult().apply(connection.query(new MongoNamespace(UserExistsOperation.this.databaseName, "system.users"), new BsonDocument("user", new BsonString(UserExistsOperation.this.userName)), null, 0, 1, 0, binding.getReadPreference().isSlaveOk(), false, false, false, false, false, (Decoder<Object>)new BsonDocumentCodec()), connection.getDescription().getServerAddress());
            }
        });
    }
    
    @Override
    public void executeAsync(final AsyncReadBinding binding, final SingleResultCallback<Boolean> callback) {
        OperationHelper.withConnection(binding, new OperationHelper.AsyncCallableWithConnection() {
            @Override
            public void call(final AsyncConnection connection, final Throwable t) {
                final SingleResultCallback<Boolean> errHandlingCallback = ErrorHandlingResultCallback.errorHandlingCallback(callback, OperationHelper.LOGGER);
                if (t != null) {
                    errHandlingCallback.onResult(null, t);
                }
                else {
                    final SingleResultCallback<Boolean> wrappedCallback = OperationHelper.releasingCallback(errHandlingCallback, connection);
                    if (OperationHelper.serverIsAtLeastVersionTwoDotSix(connection.getDescription())) {
                        CommandOperationHelper.executeWrappedCommandProtocolAsync(binding, UserExistsOperation.this.databaseName, UserExistsOperation.this.getCommand(), new BsonDocumentCodec(), connection, UserExistsOperation.this.transformer(), wrappedCallback);
                    }
                    else {
                        connection.queryAsync(new MongoNamespace(UserExistsOperation.this.databaseName, "system.users"), new BsonDocument("user", new BsonString(UserExistsOperation.this.userName)), null, 0, 1, 0, binding.getReadPreference().isSlaveOk(), false, false, false, false, false, (Decoder<Object>)new BsonDocumentCodec(), (SingleResultCallback<QueryResult<Object>>)new SingleResultCallback<QueryResult<BsonDocument>>() {
                            @Override
                            public void onResult(final QueryResult<BsonDocument> result, final Throwable t) {
                                if (t != null) {
                                    wrappedCallback.onResult(null, t);
                                }
                                else {
                                    try {
                                        wrappedCallback.onResult(UserExistsOperation.this.transformQueryResult().apply(result, connection.getDescription().getServerAddress()), null);
                                    }
                                    catch (Throwable tr) {
                                        wrappedCallback.onResult(null, tr);
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });
    }
    
    private CommandOperationHelper.CommandTransformer<BsonDocument, Boolean> transformer() {
        return new CommandOperationHelper.CommandTransformer<BsonDocument, Boolean>() {
            @Override
            public Boolean apply(final BsonDocument result, final ServerAddress serverAddress) {
                return result.get("users").isArray() && !result.getArray("users").isEmpty();
            }
        };
    }
    
    private CommandOperationHelper.CommandTransformer<QueryResult<BsonDocument>, Boolean> transformQueryResult() {
        return new CommandOperationHelper.CommandTransformer<QueryResult<BsonDocument>, Boolean>() {
            @Override
            public Boolean apply(final QueryResult<BsonDocument> queryResult, final ServerAddress serverAddress) {
                return !queryResult.getResults().isEmpty();
            }
        };
    }
    
    private BsonDocument getCommand() {
        return new BsonDocument("usersInfo", new BsonString(this.userName));
    }
}
