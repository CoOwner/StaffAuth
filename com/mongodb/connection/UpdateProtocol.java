package com.mongodb.connection;

import com.mongodb.bulk.*;
import com.mongodb.*;
import com.mongodb.async.*;
import java.util.*;
import org.bson.*;
import com.mongodb.diagnostics.logging.*;

class UpdateProtocol extends WriteProtocol
{
    private static final Logger LOGGER;
    private final List<UpdateRequest> updates;
    
    public UpdateProtocol(final MongoNamespace namespace, final boolean ordered, final WriteConcern writeConcern, final List<UpdateRequest> updates) {
        super(namespace, ordered, writeConcern);
        this.updates = updates;
    }
    
    @Override
    public WriteConcernResult execute(final InternalConnection connection) {
        if (UpdateProtocol.LOGGER.isDebugEnabled()) {
            UpdateProtocol.LOGGER.debug(String.format("Updating documents in namespace %s on connection [%s] to server %s", this.getNamespace(), connection.getDescription().getConnectionId(), connection.getDescription().getServerAddress()));
        }
        final WriteConcernResult writeConcernResult = super.execute(connection);
        UpdateProtocol.LOGGER.debug("Update completed");
        return writeConcernResult;
    }
    
    @Override
    public void executeAsync(final InternalConnection connection, final SingleResultCallback<WriteConcernResult> callback) {
        try {
            if (UpdateProtocol.LOGGER.isDebugEnabled()) {
                UpdateProtocol.LOGGER.debug(String.format("Asynchronously updating documents in namespace %s on connection [%s] to server %s", this.getNamespace(), connection.getDescription().getConnectionId(), connection.getDescription().getServerAddress()));
            }
            super.executeAsync(connection, new SingleResultCallback<WriteConcernResult>() {
                @Override
                public void onResult(final WriteConcernResult result, final Throwable t) {
                    if (t != null) {
                        callback.onResult(null, t);
                    }
                    else {
                        UpdateProtocol.LOGGER.debug("Asynchronous update completed");
                        callback.onResult(result, null);
                    }
                }
            });
        }
        catch (Throwable t) {
            callback.onResult(null, t);
        }
    }
    
    @Override
    protected BsonDocument getAsWriteCommand(final ByteBufferBsonOutput bsonOutput, final int firstDocumentPosition) {
        final List<ByteBufBsonDocument> documents = ByteBufBsonDocument.create(bsonOutput, firstDocumentPosition);
        final BsonDocument updateDocument = new BsonDocument("q", documents.get(0)).append("u", documents.get(1));
        if (this.updates.get(0).isMulti()) {
            updateDocument.append("multi", BsonBoolean.TRUE);
        }
        if (this.updates.get(0).isUpsert()) {
            updateDocument.append("upsert", BsonBoolean.TRUE);
        }
        return this.getBaseCommandDocument("update").append("updates", new BsonArray(Collections.singletonList(updateDocument)));
    }
    
    @Override
    protected RequestMessage createRequestMessage(final MessageSettings settings) {
        return new UpdateMessage(this.getNamespace().getFullName(), this.updates, settings);
    }
    
    @Override
    protected void appendToWriteCommandResponseDocument(final RequestMessage curMessage, final RequestMessage nextMessage, final WriteConcernResult writeConcernResult, final BsonDocument response) {
        response.append("n", new BsonInt32(writeConcernResult.getCount()));
        final UpdateMessage updateMessage = (UpdateMessage)curMessage;
        final UpdateRequest updateRequest = updateMessage.getUpdateRequests().get(0);
        BsonValue upsertedId = null;
        if (writeConcernResult.getUpsertedId() != null) {
            upsertedId = writeConcernResult.getUpsertedId();
        }
        else if (!writeConcernResult.isUpdateOfExisting() && updateRequest.isUpsert()) {
            if (updateRequest.getUpdate().containsKey("_id")) {
                upsertedId = updateRequest.getUpdate().get("_id");
            }
            else if (updateRequest.getFilter().containsKey("_id")) {
                upsertedId = updateRequest.getFilter().get("_id");
            }
        }
        if (upsertedId != null) {
            response.append("upserted", new BsonArray(Collections.singletonList(new BsonDocument("index", new BsonInt32(0)).append("_id", upsertedId))));
        }
    }
    
    @Override
    protected Logger getLogger() {
        return UpdateProtocol.LOGGER;
    }
    
    static {
        LOGGER = Loggers.getLogger("protocol.update");
    }
}
