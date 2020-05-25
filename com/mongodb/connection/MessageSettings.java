package com.mongodb.connection;

import com.mongodb.annotations.*;

@Immutable
final class MessageSettings
{
    private static final int DEFAULT_MAX_DOCUMENT_SIZE = 16777216;
    private static final int DEFAULT_MAX_MESSAGE_SIZE = 33554432;
    private static final int DEFAULT_MAX_BATCH_COUNT = 1000;
    private final int maxDocumentSize;
    private final int maxMessageSize;
    private final int maxBatchCount;
    
    public static Builder builder() {
        return new Builder();
    }
    
    public int getMaxDocumentSize() {
        return this.maxDocumentSize;
    }
    
    public int getMaxMessageSize() {
        return this.maxMessageSize;
    }
    
    public int getMaxBatchCount() {
        return this.maxBatchCount;
    }
    
    private MessageSettings(final Builder builder) {
        this.maxDocumentSize = builder.maxDocumentSize;
        this.maxMessageSize = builder.maxMessageSize;
        this.maxBatchCount = builder.maxBatchCount;
    }
    
    @NotThreadSafe
    public static final class Builder
    {
        private int maxDocumentSize;
        private int maxMessageSize;
        private int maxBatchCount;
        
        public Builder() {
            this.maxDocumentSize = 16777216;
            this.maxMessageSize = 33554432;
            this.maxBatchCount = 1000;
        }
        
        public MessageSettings build() {
            return new MessageSettings(this, null);
        }
        
        public Builder maxDocumentSize(final int maxDocumentSize) {
            this.maxDocumentSize = maxDocumentSize;
            return this;
        }
        
        public Builder maxMessageSize(final int maxMessageSize) {
            this.maxMessageSize = maxMessageSize;
            return this;
        }
        
        public Builder maxBatchCount(final int maxBatchCount) {
            this.maxBatchCount = maxBatchCount;
            return this;
        }
    }
}
