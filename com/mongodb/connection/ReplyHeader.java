package com.mongodb.connection;

import org.bson.io.*;
import com.mongodb.*;

class ReplyHeader
{
    public static final int REPLY_HEADER_LENGTH = 36;
    private static final int CURSOR_NOT_FOUND_RESPONSE_FLAG = 1;
    private static final int QUERY_FAILURE_RESPONSE_FLAG = 2;
    private static final int OP_REPLY_OP_CODE = 1;
    private static final int MIN_BSON_DOCUMENT_LENGTH = 5;
    private final int messageLength;
    private final int requestId;
    private final int responseTo;
    private final int responseFlags;
    private final long cursorId;
    private final int startingFrom;
    private final int numberReturned;
    
    public ReplyHeader(final BsonInput header, final int maxMessageLength) {
        this.messageLength = header.readInt32();
        this.requestId = header.readInt32();
        this.responseTo = header.readInt32();
        final int opCode = header.readInt32();
        this.responseFlags = header.readInt32();
        this.cursorId = header.readInt64();
        this.startingFrom = header.readInt32();
        this.numberReturned = header.readInt32();
        if (opCode != 1) {
            throw new MongoInternalException(String.format("The reply message opCode %d does not match the expected opCode %d", opCode, 1));
        }
        if (this.messageLength < 36) {
            throw new MongoInternalException(String.format("The reply message length %d is less than the mimimum message length %d", this.messageLength, 36));
        }
        if (this.messageLength > maxMessageLength) {
            throw new MongoInternalException(String.format("The reply message length %d is less than the maximum message length %d", this.messageLength, maxMessageLength));
        }
        if (this.numberReturned < 0) {
            throw new MongoInternalException(String.format("The reply message number of returned documents, %d, is less than 0", this.numberReturned));
        }
    }
    
    public int getMessageLength() {
        return this.messageLength;
    }
    
    public int getRequestId() {
        return this.requestId;
    }
    
    public int getResponseTo() {
        return this.responseTo;
    }
    
    public int getResponseFlags() {
        return this.responseFlags;
    }
    
    public long getCursorId() {
        return this.cursorId;
    }
    
    public int getStartingFrom() {
        return this.startingFrom;
    }
    
    public int getNumberReturned() {
        return this.numberReturned;
    }
    
    public boolean isCursorNotFound() {
        return (this.responseFlags & 0x1) == 0x1;
    }
    
    public boolean isQueryFailure() {
        return (this.responseFlags & 0x2) == 0x2;
    }
}
