package com.mongodb.connection;

import org.bson.io.*;

abstract class BaseQueryMessage extends RequestMessage
{
    private final int skip;
    private final int numberToReturn;
    private boolean tailableCursor;
    private boolean slaveOk;
    private boolean oplogReplay;
    private boolean noCursorTimeout;
    private boolean awaitData;
    private boolean partial;
    
    public BaseQueryMessage(final String collectionName, final int skip, final int numberToReturn, final MessageSettings settings) {
        super(collectionName, OpCode.OP_QUERY, settings);
        this.skip = skip;
        this.numberToReturn = numberToReturn;
    }
    
    public boolean isTailableCursor() {
        return this.tailableCursor;
    }
    
    public BaseQueryMessage tailableCursor(final boolean tailableCursor) {
        this.tailableCursor = tailableCursor;
        return this;
    }
    
    public boolean isSlaveOk() {
        return this.slaveOk;
    }
    
    public BaseQueryMessage slaveOk(final boolean slaveOk) {
        this.slaveOk = slaveOk;
        return this;
    }
    
    public boolean isOplogReplay() {
        return this.oplogReplay;
    }
    
    public BaseQueryMessage oplogReplay(final boolean oplogReplay) {
        this.oplogReplay = oplogReplay;
        return this;
    }
    
    public boolean isNoCursorTimeout() {
        return this.noCursorTimeout;
    }
    
    public BaseQueryMessage noCursorTimeout(final boolean noCursorTimeout) {
        this.noCursorTimeout = noCursorTimeout;
        return this;
    }
    
    public boolean isAwaitData() {
        return this.awaitData;
    }
    
    public BaseQueryMessage awaitData(final boolean awaitData) {
        this.awaitData = awaitData;
        return this;
    }
    
    public boolean isPartial() {
        return this.partial;
    }
    
    public BaseQueryMessage partial(final boolean partial) {
        this.partial = partial;
        return this;
    }
    
    private int getCursorFlag() {
        int cursorFlag = 0;
        if (this.isTailableCursor()) {
            cursorFlag |= 0x2;
        }
        if (this.isSlaveOk()) {
            cursorFlag |= 0x4;
        }
        if (this.isOplogReplay()) {
            cursorFlag |= 0x8;
        }
        if (this.isNoCursorTimeout()) {
            cursorFlag |= 0x10;
        }
        if (this.isAwaitData()) {
            cursorFlag |= 0x20;
        }
        if (this.isPartial()) {
            cursorFlag |= 0x80;
        }
        return cursorFlag;
    }
    
    protected void writeQueryPrologue(final BsonOutput bsonOutput) {
        bsonOutput.writeInt32(this.getCursorFlag());
        bsonOutput.writeCString(this.getCollectionName());
        bsonOutput.writeInt32(this.skip);
        bsonOutput.writeInt32(this.numberToReturn);
    }
}
