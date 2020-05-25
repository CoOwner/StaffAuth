package com.mongodb;

import com.mongodb.client.model.*;
import java.util.concurrent.*;
import com.mongodb.annotations.*;

public class AggregationOptions
{
    private final Integer batchSize;
    private final Boolean allowDiskUse;
    private final OutputMode outputMode;
    private final long maxTimeMS;
    private final Boolean bypassDocumentValidation;
    private final Collation collation;
    
    AggregationOptions(final Builder builder) {
        this.batchSize = builder.batchSize;
        this.allowDiskUse = builder.allowDiskUse;
        this.outputMode = builder.outputMode;
        this.maxTimeMS = builder.maxTimeMS;
        this.bypassDocumentValidation = builder.bypassDocumentValidation;
        this.collation = builder.collation;
    }
    
    public Boolean getAllowDiskUse() {
        return this.allowDiskUse;
    }
    
    public Integer getBatchSize() {
        return this.batchSize;
    }
    
    public OutputMode getOutputMode() {
        return this.outputMode;
    }
    
    public long getMaxTime(final TimeUnit timeUnit) {
        return timeUnit.convert(this.maxTimeMS, TimeUnit.MILLISECONDS);
    }
    
    public Boolean getBypassDocumentValidation() {
        return this.bypassDocumentValidation;
    }
    
    public Collation getCollation() {
        return this.collation;
    }
    
    @Override
    public String toString() {
        return "AggregationOptions{batchSize=" + this.batchSize + ", allowDiskUse=" + this.allowDiskUse + ", outputMode=" + this.outputMode + ", maxTimeMS=" + this.maxTimeMS + ", bypassDocumentValidation=" + this.bypassDocumentValidation + ", collation=" + this.collation + "}";
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public enum OutputMode
    {
        INLINE, 
        CURSOR;
    }
    
    @NotThreadSafe
    public static class Builder
    {
        private Integer batchSize;
        private Boolean allowDiskUse;
        private OutputMode outputMode;
        private long maxTimeMS;
        private Boolean bypassDocumentValidation;
        private Collation collation;
        
        private Builder() {
            this.outputMode = OutputMode.INLINE;
        }
        
        public Builder batchSize(final Integer size) {
            this.batchSize = size;
            return this;
        }
        
        public Builder allowDiskUse(final Boolean allowDiskUse) {
            this.allowDiskUse = allowDiskUse;
            return this;
        }
        
        public Builder outputMode(final OutputMode mode) {
            this.outputMode = mode;
            return this;
        }
        
        public Builder maxTime(final long maxTime, final TimeUnit timeUnit) {
            this.maxTimeMS = TimeUnit.MILLISECONDS.convert(maxTime, timeUnit);
            return this;
        }
        
        public Builder bypassDocumentValidation(final Boolean bypassDocumentValidation) {
            this.bypassDocumentValidation = bypassDocumentValidation;
            return this;
        }
        
        public Builder collation(final Collation collation) {
            this.collation = collation;
            return this;
        }
        
        public AggregationOptions build() {
            return new AggregationOptions(this);
        }
    }
}
