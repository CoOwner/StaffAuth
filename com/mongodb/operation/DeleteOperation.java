package com.mongodb.operation;

import java.util.*;
import com.mongodb.assertions.*;
import com.mongodb.*;
import com.mongodb.connection.*;
import com.mongodb.async.*;
import com.mongodb.bulk.*;

public class DeleteOperation extends BaseWriteOperation
{
    private final List<DeleteRequest> deleteRequests;
    
    public DeleteOperation(final MongoNamespace namespace, final boolean ordered, final WriteConcern writeConcern, final List<DeleteRequest> deleteRequests) {
        super(namespace, ordered, writeConcern);
        this.deleteRequests = Assertions.notNull("removes", deleteRequests);
    }
    
    public List<DeleteRequest> getDeleteRequests() {
        return this.deleteRequests;
    }
    
    @Override
    protected WriteConcernResult executeProtocol(final Connection connection) {
        OperationHelper.validateWriteRequestCollations(connection, this.deleteRequests, this.getWriteConcern());
        return connection.delete(this.getNamespace(), this.isOrdered(), this.getWriteConcern(), this.deleteRequests);
    }
    
    @Override
    protected void executeProtocolAsync(final AsyncConnection connection, final SingleResultCallback<WriteConcernResult> callback) {
        OperationHelper.validateWriteRequestCollations(connection, this.deleteRequests, this.getWriteConcern(), new OperationHelper.AsyncCallableWithConnection() {
            @Override
            public void call(final AsyncConnection connection, final Throwable t) {
                if (t != null) {
                    callback.onResult(null, t);
                }
                else {
                    connection.deleteAsync(DeleteOperation.this.getNamespace(), DeleteOperation.this.isOrdered(), DeleteOperation.this.getWriteConcern(), DeleteOperation.this.deleteRequests, callback);
                }
            }
        });
    }
    
    @Override
    protected BulkWriteResult executeCommandProtocol(final Connection connection) {
        OperationHelper.validateWriteRequestCollations(connection, this.deleteRequests, this.getWriteConcern());
        return connection.deleteCommand(this.getNamespace(), this.isOrdered(), this.getWriteConcern(), this.deleteRequests);
    }
    
    @Override
    protected void executeCommandProtocolAsync(final AsyncConnection connection, final SingleResultCallback<BulkWriteResult> callback) {
        OperationHelper.validateWriteRequestCollations(connection, this.deleteRequests, this.getWriteConcern(), new OperationHelper.AsyncCallableWithConnection() {
            @Override
            public void call(final AsyncConnection connection, final Throwable t) {
                if (t != null) {
                    callback.onResult(null, t);
                }
                else {
                    connection.deleteCommandAsync(DeleteOperation.this.getNamespace(), DeleteOperation.this.isOrdered(), DeleteOperation.this.getWriteConcern(), DeleteOperation.this.deleteRequests, callback);
                }
            }
        });
    }
    
    @Override
    protected WriteRequest.Type getType() {
        return WriteRequest.Type.DELETE;
    }
    
    @Override
    protected int getCount(final BulkWriteResult bulkWriteResult) {
        return bulkWriteResult.getDeletedCount();
    }
}
