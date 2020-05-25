package com.mongodb.connection;

import com.mongodb.*;
import java.util.*;
import com.mongodb.bulk.*;
import org.bson.io.*;
import org.bson.*;
import com.mongodb.internal.validator.*;

class InsertMessage extends RequestMessage
{
    private final boolean ordered;
    private final WriteConcern writeConcern;
    private final List<InsertRequest> insertRequestList;
    
    public InsertMessage(final String collectionName, final boolean ordered, final WriteConcern writeConcern, final List<InsertRequest> insertRequestList, final MessageSettings settings) {
        super(collectionName, OpCode.OP_INSERT, settings);
        this.ordered = ordered;
        this.writeConcern = writeConcern;
        this.insertRequestList = insertRequestList;
    }
    
    public List<InsertRequest> getInsertRequestList() {
        return this.insertRequestList;
    }
    
    @Override
    protected RequestMessage encodeMessageBody(final BsonOutput outputStream, final int messageStartPosition) {
        return this.encodeMessageBodyWithMetadata(outputStream, messageStartPosition).getNextMessage();
    }
    
    @Override
    protected EncodingMetadata encodeMessageBodyWithMetadata(final BsonOutput outputStream, final int messageStartPosition) {
        this.writeInsertPrologue(outputStream);
        final int firstDocumentPosition = outputStream.getPosition();
        for (int i = 0; i < this.insertRequestList.size(); ++i) {
            final BsonDocument document = this.insertRequestList.get(i).getDocument();
            final int pos = outputStream.getPosition();
            this.addCollectibleDocument(document, outputStream, this.createValidator());
            if (outputStream.getPosition() - messageStartPosition > this.getSettings().getMaxMessageSize()) {
                outputStream.truncateToPosition(pos);
                return new EncodingMetadata(new InsertMessage(this.getCollectionName(), this.ordered, this.writeConcern, this.insertRequestList.subList(i, this.insertRequestList.size()), this.getSettings()), firstDocumentPosition);
            }
        }
        return new EncodingMetadata(null, firstDocumentPosition);
    }
    
    private FieldNameValidator createValidator() {
        if (this.getCollectionName().endsWith(".system.indexes")) {
            return new NoOpFieldNameValidator();
        }
        return new CollectibleDocumentFieldNameValidator();
    }
    
    private void writeInsertPrologue(final BsonOutput outputStream) {
        int flags = 0;
        if (!this.ordered) {
            flags |= 0x1;
        }
        outputStream.writeInt32(flags);
        outputStream.writeCString(this.getCollectionName());
    }
}
