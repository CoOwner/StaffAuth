package com.mongodb.connection;

import java.io.*;
import org.bson.*;

class ResponseBuffers implements Closeable
{
    private final ReplyHeader replyHeader;
    private final ByteBuf bodyByteBuffer;
    private volatile boolean isClosed;
    
    public ResponseBuffers(final ReplyHeader replyHeader, final ByteBuf bodyByteBuffer) {
        this.replyHeader = replyHeader;
        this.bodyByteBuffer = bodyByteBuffer;
    }
    
    public ReplyHeader getReplyHeader() {
        return this.replyHeader;
    }
    
    public ByteBuf getBodyByteBuffer() {
        return this.bodyByteBuffer.asReadOnly();
    }
    
    public void reset() {
        this.bodyByteBuffer.position(0);
    }
    
    @Override
    public void close() {
        if (!this.isClosed) {
            if (this.bodyByteBuffer != null) {
                this.bodyByteBuffer.release();
            }
            this.isClosed = true;
        }
    }
}
