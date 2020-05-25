package com.mongodb.connection;

import com.mongodb.assertions.*;
import java.util.*;
import com.mongodb.*;
import com.mongodb.async.*;
import com.mongodb.bulk.*;
import org.bson.*;
import org.bson.codecs.*;
import com.mongodb.internal.async.*;
import com.mongodb.binding.*;
import com.mongodb.diagnostics.logging.*;

class DefaultServerConnection extends AbstractReferenceCounted implements Connection, AsyncConnection
{
    private static final Logger LOGGER;
    private final InternalConnection wrapped;
    private final ProtocolExecutor protocolExecutor;
    private final ClusterConnectionMode clusterConnectionMode;
    
    public DefaultServerConnection(final InternalConnection wrapped, final ProtocolExecutor protocolExecutor, final ClusterConnectionMode clusterConnectionMode) {
        this.wrapped = wrapped;
        this.protocolExecutor = protocolExecutor;
        this.clusterConnectionMode = clusterConnectionMode;
    }
    
    @Override
    public DefaultServerConnection retain() {
        super.retain();
        return this;
    }
    
    @Override
    public void release() {
        super.release();
        if (this.getCount() == 0) {
            this.wrapped.close();
        }
    }
    
    @Override
    public ConnectionDescription getDescription() {
        Assertions.isTrue("open", this.getCount() > 0);
        return this.wrapped.getDescription();
    }
    
    @Override
    public WriteConcernResult insert(final MongoNamespace namespace, final boolean ordered, final WriteConcern writeConcern, final List<InsertRequest> inserts) {
        return this.executeProtocol((Protocol<WriteConcernResult>)new InsertProtocol(namespace, ordered, writeConcern, inserts));
    }
    
    @Override
    public void insertAsync(final MongoNamespace namespace, final boolean ordered, final WriteConcern writeConcern, final List<InsertRequest> inserts, final SingleResultCallback<WriteConcernResult> callback) {
        this.executeProtocolAsync(new InsertProtocol(namespace, ordered, writeConcern, inserts), callback);
    }
    
    @Override
    public WriteConcernResult update(final MongoNamespace namespace, final boolean ordered, final WriteConcern writeConcern, final List<UpdateRequest> updates) {
        return this.executeProtocol((Protocol<WriteConcernResult>)new UpdateProtocol(namespace, ordered, writeConcern, updates));
    }
    
    @Override
    public void updateAsync(final MongoNamespace namespace, final boolean ordered, final WriteConcern writeConcern, final List<UpdateRequest> updates, final SingleResultCallback<WriteConcernResult> callback) {
        this.executeProtocolAsync(new UpdateProtocol(namespace, ordered, writeConcern, updates), callback);
    }
    
    @Override
    public WriteConcernResult delete(final MongoNamespace namespace, final boolean ordered, final WriteConcern writeConcern, final List<DeleteRequest> deletes) {
        return this.executeProtocol((Protocol<WriteConcernResult>)new DeleteProtocol(namespace, ordered, writeConcern, deletes));
    }
    
    @Override
    public void deleteAsync(final MongoNamespace namespace, final boolean ordered, final WriteConcern writeConcern, final List<DeleteRequest> deletes, final SingleResultCallback<WriteConcernResult> callback) {
        this.executeProtocolAsync(new DeleteProtocol(namespace, ordered, writeConcern, deletes), callback);
    }
    
    @Override
    public BulkWriteResult insertCommand(final MongoNamespace namespace, final boolean ordered, final WriteConcern writeConcern, final List<InsertRequest> inserts) {
        return this.insertCommand(namespace, ordered, writeConcern, null, inserts);
    }
    
    @Override
    public BulkWriteResult insertCommand(final MongoNamespace namespace, final boolean ordered, final WriteConcern writeConcern, final Boolean bypassDocumentValidation, final List<InsertRequest> inserts) {
        return this.executeProtocol((Protocol<BulkWriteResult>)new InsertCommandProtocol(namespace, ordered, writeConcern, bypassDocumentValidation, inserts));
    }
    
    @Override
    public void insertCommandAsync(final MongoNamespace namespace, final boolean ordered, final WriteConcern writeConcern, final List<InsertRequest> inserts, final SingleResultCallback<BulkWriteResult> callback) {
        this.insertCommandAsync(namespace, ordered, writeConcern, null, inserts, callback);
    }
    
    @Override
    public void insertCommandAsync(final MongoNamespace namespace, final boolean ordered, final WriteConcern writeConcern, final Boolean bypassDocumentValidation, final List<InsertRequest> inserts, final SingleResultCallback<BulkWriteResult> callback) {
        this.executeProtocolAsync(new InsertCommandProtocol(namespace, ordered, writeConcern, bypassDocumentValidation, inserts), callback);
    }
    
    @Override
    public BulkWriteResult updateCommand(final MongoNamespace namespace, final boolean ordered, final WriteConcern writeConcern, final List<UpdateRequest> updates) {
        return this.updateCommand(namespace, ordered, writeConcern, null, updates);
    }
    
    @Override
    public BulkWriteResult updateCommand(final MongoNamespace namespace, final boolean ordered, final WriteConcern writeConcern, final Boolean bypassDocumentValidation, final List<UpdateRequest> updates) {
        return this.executeProtocol((Protocol<BulkWriteResult>)new UpdateCommandProtocol(namespace, ordered, writeConcern, bypassDocumentValidation, updates));
    }
    
    @Override
    public void updateCommandAsync(final MongoNamespace namespace, final boolean ordered, final WriteConcern writeConcern, final List<UpdateRequest> updates, final SingleResultCallback<BulkWriteResult> callback) {
        this.updateCommandAsync(namespace, ordered, writeConcern, null, updates, callback);
    }
    
    @Override
    public void updateCommandAsync(final MongoNamespace namespace, final boolean ordered, final WriteConcern writeConcern, final Boolean bypassDocumentValidation, final List<UpdateRequest> updates, final SingleResultCallback<BulkWriteResult> callback) {
        this.executeProtocolAsync(new UpdateCommandProtocol(namespace, ordered, writeConcern, bypassDocumentValidation, updates), callback);
    }
    
    @Override
    public BulkWriteResult deleteCommand(final MongoNamespace namespace, final boolean ordered, final WriteConcern writeConcern, final List<DeleteRequest> deletes) {
        return this.executeProtocol((Protocol<BulkWriteResult>)new DeleteCommandProtocol(namespace, ordered, writeConcern, deletes));
    }
    
    @Override
    public void deleteCommandAsync(final MongoNamespace namespace, final boolean ordered, final WriteConcern writeConcern, final List<DeleteRequest> deletes, final SingleResultCallback<BulkWriteResult> callback) {
        this.executeProtocolAsync(new DeleteCommandProtocol(namespace, ordered, writeConcern, deletes), callback);
    }
    
    @Override
    public <T> T command(final String database, final BsonDocument command, final boolean slaveOk, final FieldNameValidator fieldNameValidator, final Decoder<T> commandResultDecoder) {
        return this.executeProtocol(new CommandProtocol<T>(database, command, fieldNameValidator, commandResultDecoder).slaveOk(this.getSlaveOk(slaveOk)));
    }
    
    @Override
    public <T> void commandAsync(final String database, final BsonDocument command, final boolean slaveOk, final FieldNameValidator fieldNameValidator, final Decoder<T> commandResultDecoder, final SingleResultCallback<T> callback) {
        this.executeProtocolAsync(new CommandProtocol<T>(database, command, fieldNameValidator, commandResultDecoder).slaveOk(this.getSlaveOk(slaveOk)), callback);
    }
    
    @Override
    public <T> QueryResult<T> query(final MongoNamespace namespace, final BsonDocument queryDocument, final BsonDocument fields, final int numberToReturn, final int skip, final boolean slaveOk, final boolean tailableCursor, final boolean awaitData, final boolean noCursorTimeout, final boolean partial, final boolean oplogReplay, final Decoder<T> resultDecoder) {
        return this.executeProtocol(new QueryProtocol<T>(namespace, skip, numberToReturn, queryDocument, fields, resultDecoder).tailableCursor(tailableCursor).slaveOk(this.getSlaveOk(slaveOk)).oplogReplay(oplogReplay).noCursorTimeout(noCursorTimeout).awaitData(awaitData).partial(partial));
    }
    
    @Override
    public <T> QueryResult<T> query(final MongoNamespace namespace, final BsonDocument queryDocument, final BsonDocument fields, final int skip, final int limit, final int batchSize, final boolean slaveOk, final boolean tailableCursor, final boolean awaitData, final boolean noCursorTimeout, final boolean partial, final boolean oplogReplay, final Decoder<T> resultDecoder) {
        return this.executeProtocol(new QueryProtocol<T>(namespace, skip, limit, batchSize, queryDocument, fields, resultDecoder).tailableCursor(tailableCursor).slaveOk(this.getSlaveOk(slaveOk)).oplogReplay(oplogReplay).noCursorTimeout(noCursorTimeout).awaitData(awaitData).partial(partial));
    }
    
    @Override
    public <T> void queryAsync(final MongoNamespace namespace, final BsonDocument queryDocument, final BsonDocument fields, final int numberToReturn, final int skip, final boolean slaveOk, final boolean tailableCursor, final boolean awaitData, final boolean noCursorTimeout, final boolean partial, final boolean oplogReplay, final Decoder<T> resultDecoder, final SingleResultCallback<QueryResult<T>> callback) {
        this.executeProtocolAsync(new QueryProtocol<T>(namespace, skip, numberToReturn, queryDocument, fields, resultDecoder).tailableCursor(tailableCursor).slaveOk(this.getSlaveOk(slaveOk)).oplogReplay(oplogReplay).noCursorTimeout(noCursorTimeout).awaitData(awaitData).partial(partial), callback);
    }
    
    @Override
    public <T> void queryAsync(final MongoNamespace namespace, final BsonDocument queryDocument, final BsonDocument fields, final int skip, final int limit, final int batchSize, final boolean slaveOk, final boolean tailableCursor, final boolean awaitData, final boolean noCursorTimeout, final boolean partial, final boolean oplogReplay, final Decoder<T> resultDecoder, final SingleResultCallback<QueryResult<T>> callback) {
        this.executeProtocolAsync(new QueryProtocol<T>(namespace, skip, limit, batchSize, queryDocument, fields, resultDecoder).tailableCursor(tailableCursor).slaveOk(this.getSlaveOk(slaveOk)).oplogReplay(oplogReplay).noCursorTimeout(noCursorTimeout).awaitData(awaitData).partial(partial), callback);
    }
    
    @Override
    public <T> QueryResult<T> getMore(final MongoNamespace namespace, final long cursorId, final int numberToReturn, final Decoder<T> resultDecoder) {
        return this.executeProtocol(new GetMoreProtocol<T>(namespace, cursorId, numberToReturn, resultDecoder));
    }
    
    @Override
    public <T> void getMoreAsync(final MongoNamespace namespace, final long cursorId, final int numberToReturn, final Decoder<T> resultDecoder, final SingleResultCallback<QueryResult<T>> callback) {
        this.executeProtocolAsync(new GetMoreProtocol<T>(namespace, cursorId, numberToReturn, resultDecoder), callback);
    }
    
    @Override
    public void killCursor(final List<Long> cursors) {
        this.killCursor(null, cursors);
    }
    
    @Override
    public void killCursor(final MongoNamespace namespace, final List<Long> cursors) {
        this.executeProtocol((Protocol<Object>)new KillCursorProtocol(namespace, cursors));
    }
    
    @Override
    public void killCursorAsync(final List<Long> cursors, final SingleResultCallback<Void> callback) {
        this.killCursorAsync(null, cursors, callback);
    }
    
    @Override
    public void killCursorAsync(final MongoNamespace namespace, final List<Long> cursors, final SingleResultCallback<Void> callback) {
        this.executeProtocolAsync(new KillCursorProtocol(namespace, cursors), callback);
    }
    
    private boolean getSlaveOk(final boolean slaveOk) {
        return slaveOk || (this.clusterConnectionMode == ClusterConnectionMode.SINGLE && this.wrapped.getDescription().getServerType() != ServerType.SHARD_ROUTER);
    }
    
    private <T> T executeProtocol(final Protocol<T> protocol) {
        return this.protocolExecutor.execute(protocol, this.wrapped);
    }
    
    private <T> void executeProtocolAsync(final Protocol<T> protocol, final SingleResultCallback<T> callback) {
        final SingleResultCallback<T> errHandlingCallback = ErrorHandlingResultCallback.errorHandlingCallback(callback, DefaultServerConnection.LOGGER);
        try {
            this.protocolExecutor.executeAsync(protocol, this.wrapped, errHandlingCallback);
        }
        catch (Throwable t) {
            errHandlingCallback.onResult(null, t);
        }
    }
    
    static {
        LOGGER = Loggers.getLogger("connection");
    }
}
