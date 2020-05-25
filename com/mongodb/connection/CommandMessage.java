package com.mongodb.connection;

import org.bson.*;
import com.mongodb.internal.validator.*;
import org.bson.io.*;

class CommandMessage extends RequestMessage
{
    private final boolean slaveOk;
    private final BsonDocument command;
    private final FieldNameValidator validator;
    
    public CommandMessage(final String collectionName, final BsonDocument command, final boolean slaveOk, final MessageSettings settings) {
        this(collectionName, command, slaveOk, new NoOpFieldNameValidator(), settings);
    }
    
    public CommandMessage(final String collectionName, final BsonDocument command, final boolean slaveOk, final FieldNameValidator validator, final MessageSettings settings) {
        super(collectionName, OpCode.OP_QUERY, settings);
        this.slaveOk = slaveOk;
        this.command = command;
        this.validator = validator;
    }
    
    @Override
    protected RequestMessage encodeMessageBody(final BsonOutput bsonOutput, final int messageStartPosition) {
        return this.encodeMessageBodyWithMetadata(bsonOutput, messageStartPosition).getNextMessage();
    }
    
    @Override
    protected EncodingMetadata encodeMessageBodyWithMetadata(final BsonOutput bsonOutput, final int messageStartPosition) {
        bsonOutput.writeInt32(this.slaveOk ? 4 : 0);
        bsonOutput.writeCString(this.getCollectionName());
        bsonOutput.writeInt32(0);
        bsonOutput.writeInt32(-1);
        final int firstDocumentPosition = bsonOutput.getPosition();
        this.addDocument(this.command, bsonOutput, this.validator);
        return new EncodingMetadata(null, firstDocumentPosition);
    }
}
