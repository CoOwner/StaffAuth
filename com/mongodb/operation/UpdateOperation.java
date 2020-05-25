package com.mongodb.operation;

import java.util.*;
import com.mongodb.assertions.*;
import com.mongodb.*;
import com.mongodb.connection.*;
import com.mongodb.async.*;
import com.mongodb.bulk.*;

public class UpdateOperation extends BaseWriteOperation
{
    private final List<UpdateRequest> updates;
    
    public UpdateOperation(final MongoNamespace namespace, final boolean ordered, final WriteConcern writeConcern, final List<UpdateRequest> updates) {
        super(namespace, ordered, writeConcern);
        this.updates = Assertions.notNull("update", updates);
    }
    
    public List<UpdateRequest> getUpdateRequests() {
        return this.updates;
    }
    
    @Override
    protected WriteConcernResult executeProtocol(final Connection connection) {
        OperationHelper.validateWriteRequestCollations(connection, this.updates, this.getWriteConcern());
        return connection.update(this.getNamespace(), this.isOrdered(), this.getWriteConcern(), this.updates);
    }
    
    @Override
    protected void executeProtocolAsync(final AsyncConnection connection, final SingleResultCallback<WriteConcernResult> callback) {
        OperationHelper.validateWriteRequestCollations(connection, this.updates, this.getWriteConcern(), new OperationHelper.AsyncCallableWithConnection() {
            @Override
            public void call(final AsyncConnection connection, final Throwable t) {
                if (t != null) {
                    callback.onResult(null, t);
                }
                else {
                    connection.updateAsync(UpdateOperation.this.getNamespace(), UpdateOperation.this.isOrdered(), UpdateOperation.this.getWriteConcern(), UpdateOperation.this.updates, callback);
                }
            }
        });
    }
    
    @Override
    protected BulkWriteResult executeCommandProtocol(final Connection connection) {
        OperationHelper.validateWriteRequestCollations(connection, this.updates, this.getWriteConcern());
        return connection.updateCommand(this.getNamespace(), this.isOrdered(), this.getWriteConcern(), this.getBypassDocumentValidation(), this.updates);
    }
    
    @Override
    protected void executeCommandProtocolAsync(final AsyncConnection connection, final SingleResultCallback<BulkWriteResult> callback) {
        OperationHelper.validateWriteRequestCollations(connection, this.updates, this.getWriteConcern(), new OperationHelper.AsyncCallableWithConnection() {
            @Override
            public void call(final AsyncConnection connection, final Throwable t) {
                if (t != null) {
                    callback.onResult(null, t);
                }
                else {
                    connection.updateCommandAsync(UpdateOperation.this.getNamespace(), UpdateOperation.this.isOrdered(), UpdateOperation.this.getWriteConcern(), UpdateOperation.this.getBypassDocumentValidation(), UpdateOperation.this.updates, callback);
                }
            }
        });
    }
    
    @Override
    protected WriteRequest.Type getType() {
        return WriteRequest.Type.UPDATE;
    }
    
    @Override
    protected int getCount(final BulkWriteResult bulkWriteResult) {
        return bulkWriteResult.getMatchedCount() + bulkWriteResult.getUpserts().size();
    }
    
    @Override
    protected boolean getUpdatedExisting(final BulkWriteResult bulkWriteResult) {
        return bulkWriteResult.getMatchedCount() > 0;
    }
}
