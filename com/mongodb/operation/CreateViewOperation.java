package com.mongodb.operation;

import java.util.*;
import com.mongodb.client.model.*;
import com.mongodb.assertions.*;
import com.mongodb.binding.*;
import com.mongodb.async.*;
import com.mongodb.internal.async.*;
import com.mongodb.connection.*;
import org.bson.*;
import com.mongodb.*;

public class CreateViewOperation implements AsyncWriteOperation<Void>, WriteOperation<Void>
{
    private final String databaseName;
    private final String viewName;
    private final String viewOn;
    private final List<BsonDocument> pipeline;
    private final WriteConcern writeConcern;
    private Collation collation;
    
    public CreateViewOperation(final String databaseName, final String viewName, final String viewOn, final List<BsonDocument> pipeline, final WriteConcern writeConcern) {
        this.databaseName = Assertions.notNull("databaseName", databaseName);
        this.viewName = Assertions.notNull("viewName", viewName);
        this.viewOn = Assertions.notNull("viewOn", viewOn);
        this.pipeline = Assertions.notNull("pipeline", pipeline);
        this.writeConcern = Assertions.notNull("writeConcern", writeConcern);
    }
    
    public String getDatabaseName() {
        return this.databaseName;
    }
    
    public String getViewName() {
        return this.viewName;
    }
    
    public String getViewOn() {
        return this.viewOn;
    }
    
    public List<BsonDocument> getPipeline() {
        return this.pipeline;
    }
    
    public WriteConcern getWriteConcern() {
        return this.writeConcern;
    }
    
    public Collation getCollation() {
        return this.collation;
    }
    
    public CreateViewOperation collation(final Collation collation) {
        this.collation = collation;
        return this;
    }
    
    @Override
    public Void execute(final WriteBinding binding) {
        return OperationHelper.withConnection(binding, (OperationHelper.CallableWithConnection<Void>)new OperationHelper.CallableWithConnection<Void>() {
            @Override
            public Void call(final Connection connection) {
                if (!OperationHelper.serverIsAtLeastVersionThreeDotFour(connection.getDescription())) {
                    throw CreateViewOperation.this.createExceptionForIncompatibleServerVersion();
                }
                CommandOperationHelper.executeWrappedCommandProtocol(binding, CreateViewOperation.this.databaseName, CreateViewOperation.this.getCommand(connection.getDescription()), WriteConcernHelper.writeConcernErrorTransformer());
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
                    if (!OperationHelper.serverIsAtLeastVersionThreeDotFour(connection.getDescription())) {
                        wrappedCallback.onResult(null, CreateViewOperation.this.createExceptionForIncompatibleServerVersion());
                    }
                    CommandOperationHelper.executeWrappedCommandProtocolAsync(binding, CreateViewOperation.this.databaseName, CreateViewOperation.this.getCommand(connection.getDescription()), connection, WriteConcernHelper.writeConcernErrorTransformer(), wrappedCallback);
                }
            }
        });
    }
    
    private BsonDocument getCommand(final ConnectionDescription description) {
        final BsonDocument commandDocument = new BsonDocument("create", new BsonString(this.viewName)).append("viewOn", new BsonString(this.viewOn)).append("pipeline", new BsonArray(this.pipeline));
        if (this.collation != null) {
            commandDocument.put("collation", this.collation.asDocument());
        }
        WriteConcernHelper.appendWriteConcernToCommand(this.writeConcern, commandDocument, description);
        return commandDocument;
    }
    
    private MongoClientException createExceptionForIncompatibleServerVersion() {
        return new MongoClientException("Can not create view.  The minimum server version that supports view creation is 3.4");
    }
}
