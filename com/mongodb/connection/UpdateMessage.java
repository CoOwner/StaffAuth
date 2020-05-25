package com.mongodb.connection;

import java.util.*;
import org.bson.io.*;
import org.bson.*;
import com.mongodb.bulk.*;
import com.mongodb.internal.validator.*;

class UpdateMessage extends RequestMessage
{
    private final List<UpdateRequest> updates;
    
    public UpdateMessage(final String collectionName, final List<UpdateRequest> updates, final MessageSettings settings) {
        super(collectionName, OpCode.OP_UPDATE, settings);
        this.updates = updates;
    }
    
    public List<UpdateRequest> getUpdateRequests() {
        return this.updates;
    }
    
    @Override
    protected RequestMessage encodeMessageBody(final BsonOutput bsonOutput, final int messageStartPosition) {
        return this.encodeMessageBodyWithMetadata(bsonOutput, messageStartPosition).getNextMessage();
    }
    
    @Override
    protected EncodingMetadata encodeMessageBodyWithMetadata(final BsonOutput bsonOutput, final int messageStartPosition) {
        bsonOutput.writeInt32(0);
        bsonOutput.writeCString(this.getCollectionName());
        final UpdateRequest updateRequest = this.updates.get(0);
        int flags = 0;
        if (updateRequest.isUpsert()) {
            flags |= 0x1;
        }
        if (updateRequest.isMulti()) {
            flags |= 0x2;
        }
        bsonOutput.writeInt32(flags);
        final int firstDocumentStartPosition = bsonOutput.getPosition();
        this.addDocument(updateRequest.getFilter(), bsonOutput, new NoOpFieldNameValidator());
        if (updateRequest.getType() == WriteRequest.Type.REPLACE) {
            this.addCollectibleDocument(updateRequest.getUpdate(), bsonOutput, new CollectibleDocumentFieldNameValidator());
        }
        else {
            final int bufferPosition = bsonOutput.getPosition();
            this.addDocument(updateRequest.getUpdate(), bsonOutput, new UpdateFieldNameValidator());
            if (bsonOutput.getPosition() == bufferPosition + 5) {
                throw new IllegalArgumentException("Invalid BSON document for an update");
            }
        }
        if (this.updates.size() == 1) {
            return new EncodingMetadata(null, firstDocumentStartPosition);
        }
        return new EncodingMetadata(new UpdateMessage(this.getCollectionName(), this.updates.subList(1, this.updates.size()), this.getSettings()), firstDocumentStartPosition);
    }
}
