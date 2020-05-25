package com.mongodb.connection;

import org.bson.io.*;

class GetMoreMessage extends RequestMessage
{
    private final long cursorId;
    private final int numberToReturn;
    
    public GetMoreMessage(final String collectionName, final long cursorId, final int numberToReturn) {
        super(collectionName, OpCode.OP_GETMORE, MessageSettings.builder().build());
        this.cursorId = cursorId;
        this.numberToReturn = numberToReturn;
    }
    
    public long getCursorId() {
        return this.cursorId;
    }
    
    @Override
    protected RequestMessage encodeMessageBody(final BsonOutput bsonOutput, final int messageStartPosition) {
        return this.encodeMessageBodyWithMetadata(bsonOutput, messageStartPosition).getNextMessage();
    }
    
    @Override
    protected EncodingMetadata encodeMessageBodyWithMetadata(final BsonOutput bsonOutput, final int messageStartPosition) {
        this.writeGetMore(bsonOutput);
        return new EncodingMetadata(null, bsonOutput.getPosition());
    }
    
    private void writeGetMore(final BsonOutput buffer) {
        buffer.writeInt32(0);
        buffer.writeCString(this.getCollectionName());
        buffer.writeInt32(this.numberToReturn);
        buffer.writeInt64(this.cursorId);
    }
}
