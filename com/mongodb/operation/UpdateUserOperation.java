package com.mongodb.operation;

import com.mongodb.assertions.*;
import java.util.*;
import com.mongodb.binding.*;
import com.mongodb.async.*;
import com.mongodb.internal.async.*;
import com.mongodb.bulk.*;
import com.mongodb.*;
import com.mongodb.connection.*;
import org.bson.*;

public class UpdateUserOperation implements AsyncWriteOperation<Void>, WriteOperation<Void>
{
    private final MongoCredential credential;
    private final boolean readOnly;
    private final WriteConcern writeConcern;
    
    @Deprecated
    public UpdateUserOperation(final MongoCredential credential, final boolean readOnly) {
        this(credential, readOnly, null);
    }
    
    public UpdateUserOperation(final MongoCredential credential, final boolean readOnly, final WriteConcern writeConcern) {
        this.credential = Assertions.notNull("credential", credential);
        this.readOnly = readOnly;
        this.writeConcern = writeConcern;
    }
    
    public MongoCredential getCredential() {
        return this.credential;
    }
    
    public boolean isReadOnly() {
        return this.readOnly;
    }
    
    @Override
    public Void execute(final WriteBinding binding) {
        return OperationHelper.withConnection(binding, (OperationHelper.CallableWithConnection<Void>)new OperationHelper.CallableWithConnection<Void>() {
            @Override
            public Void call(final Connection connection) {
                if (OperationHelper.serverIsAtLeastVersionTwoDotSix(connection.getDescription())) {
                    try {
                        CommandOperationHelper.executeWrappedCommandProtocol(binding, UpdateUserOperation.this.getCredential().getSource(), UpdateUserOperation.this.getCommand(connection.getDescription()), connection, WriteConcernHelper.writeConcernErrorTransformer());
                    }
                    catch (MongoCommandException e) {
                        UserOperationHelper.translateUserCommandException(e);
                    }
                }
                else {
                    connection.update(UpdateUserOperation.this.getNamespace(), true, WriteConcern.ACKNOWLEDGED, Arrays.asList(UpdateUserOperation.this.getUpdateRequest()));
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
                        CommandOperationHelper.executeWrappedCommandProtocolAsync(binding, UpdateUserOperation.this.credential.getSource(), UpdateUserOperation.this.getCommand(connection.getDescription()), connection, WriteConcernHelper.writeConcernErrorTransformer(), UserOperationHelper.userCommandCallback(wrappedCallback));
                    }
                    else {
                        connection.updateAsync(UpdateUserOperation.this.getNamespace(), true, WriteConcern.ACKNOWLEDGED, Arrays.asList(UpdateUserOperation.this.getUpdateRequest()), new SingleResultCallback<WriteConcernResult>() {
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
    
    private UpdateRequest getUpdateRequest() {
        return new UpdateRequest(UserOperationHelper.asCollectionQueryDocument(this.credential), UserOperationHelper.asCollectionUpdateDocument(this.credential, this.readOnly), WriteRequest.Type.REPLACE);
    }
    
    private MongoNamespace getNamespace() {
        return new MongoNamespace(this.credential.getSource(), "system.users");
    }
    
    private BsonDocument getCommand(final ConnectionDescription description) {
        final BsonDocument commandDocument = UserOperationHelper.asCommandDocument(this.credential, this.readOnly, "updateUser");
        WriteConcernHelper.appendWriteConcernToCommand(this.writeConcern, commandDocument, description);
        return commandDocument;
    }
}
