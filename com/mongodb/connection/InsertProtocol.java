package com.mongodb.connection;

import java.util.*;
import com.mongodb.bulk.*;
import com.mongodb.*;
import com.mongodb.async.*;
import org.bson.*;
import com.mongodb.diagnostics.logging.*;

class InsertProtocol extends WriteProtocol
{
    private static final Logger LOGGER;
    private final List<InsertRequest> insertRequestList;
    
    public InsertProtocol(final MongoNamespace namespace, final boolean ordered, final WriteConcern writeConcern, final List<InsertRequest> insertRequestList) {
        super(namespace, ordered, writeConcern);
        this.insertRequestList = insertRequestList;
    }
    
    @Override
    public WriteConcernResult execute(final InternalConnection connection) {
        if (InsertProtocol.LOGGER.isDebugEnabled()) {
            InsertProtocol.LOGGER.debug(String.format("Inserting %d documents into namespace %s on connection [%s] to server %s", this.insertRequestList.size(), this.getNamespace(), connection.getDescription().getConnectionId(), connection.getDescription().getServerAddress()));
        }
        final WriteConcernResult writeConcernResult = super.execute(connection);
        InsertProtocol.LOGGER.debug("Insert completed");
        return writeConcernResult;
    }
    
    @Override
    public void executeAsync(final InternalConnection connection, final SingleResultCallback<WriteConcernResult> callback) {
        try {
            if (InsertProtocol.LOGGER.isDebugEnabled()) {
                InsertProtocol.LOGGER.debug(String.format("Asynchronously inserting %d documents into namespace %s on connection [%s] to server %s", this.insertRequestList.size(), this.getNamespace(), connection.getDescription().getConnectionId(), connection.getDescription().getServerAddress()));
            }
            super.executeAsync(connection, new SingleResultCallback<WriteConcernResult>() {
                @Override
                public void onResult(final WriteConcernResult result, final Throwable t) {
                    if (t != null) {
                        callback.onResult(null, t);
                    }
                    else {
                        InsertProtocol.LOGGER.debug("Asynchronous insert completed");
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
        return this.getBaseCommandDocument("insert").append("documents", new BsonArray(ByteBufBsonDocument.create(bsonOutput, firstDocumentPosition)));
    }
    
    @Override
    protected RequestMessage createRequestMessage(final MessageSettings settings) {
        return new InsertMessage(this.getNamespace().getFullName(), this.isOrdered(), this.getWriteConcern(), this.insertRequestList, settings);
    }
    
    @Override
    protected void appendToWriteCommandResponseDocument(final RequestMessage curMessage, final RequestMessage nextMessage, final WriteConcernResult writeConcernResult, final BsonDocument response) {
        response.append("n", new BsonInt32((nextMessage == null) ? ((InsertMessage)curMessage).getInsertRequestList().size() : (((InsertMessage)curMessage).getInsertRequestList().size() - ((InsertMessage)nextMessage).getInsertRequestList().size())));
    }
    
    @Override
    protected Logger getLogger() {
        return InsertProtocol.LOGGER;
    }
    
    static {
        LOGGER = Loggers.getLogger("protocol.insert");
    }
}
