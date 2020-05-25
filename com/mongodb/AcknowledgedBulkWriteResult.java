package com.mongodb;

import com.mongodb.assertions.*;
import java.util.*;

class AcknowledgedBulkWriteResult extends BulkWriteResult
{
    private int insertedCount;
    private int matchedCount;
    private int removedCount;
    private Integer modifiedCount;
    private final List<BulkWriteUpsert> upserts;
    
    AcknowledgedBulkWriteResult(final int insertedCount, final int matchedCount, final int removedCount, final Integer modifiedCount, final List<BulkWriteUpsert> upserts) {
        this.insertedCount = insertedCount;
        this.matchedCount = matchedCount;
        this.removedCount = removedCount;
        this.modifiedCount = modifiedCount;
        this.upserts = Collections.unmodifiableList((List<? extends BulkWriteUpsert>)Assertions.notNull("upserts", (List<? extends T>)upserts));
    }
    
    @Override
    public boolean isAcknowledged() {
        return true;
    }
    
    @Override
    public int getInsertedCount() {
        return this.insertedCount;
    }
    
    @Override
    public int getMatchedCount() {
        return this.matchedCount;
    }
    
    @Override
    public int getRemovedCount() {
        return this.removedCount;
    }
    
    @Override
    public boolean isModifiedCountAvailable() {
        return this.modifiedCount != null;
    }
    
    @Override
    public int getModifiedCount() {
        if (this.modifiedCount == null) {
            throw new UnsupportedOperationException("The modifiedCount is not available because at least one of the servers updated was not able to provide this information (the server must be at least version 2.6 or higher.");
        }
        return this.modifiedCount;
    }
    
    @Override
    public List<BulkWriteUpsert> getUpserts() {
        return this.upserts;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final AcknowledgedBulkWriteResult that = (AcknowledgedBulkWriteResult)o;
        if (this.insertedCount != that.insertedCount) {
            return false;
        }
        if (this.matchedCount != that.matchedCount) {
            return false;
        }
        if (this.removedCount != that.removedCount) {
            return false;
        }
        if (this.modifiedCount != null) {
            if (this.modifiedCount.equals(that.modifiedCount)) {
                return this.upserts.equals(that.upserts);
            }
        }
        else if (that.modifiedCount == null) {
            return this.upserts.equals(that.upserts);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int result = this.insertedCount;
        result = 31 * result + this.matchedCount;
        result = 31 * result + this.removedCount;
        result = 31 * result + ((this.modifiedCount != null) ? this.modifiedCount.hashCode() : 0);
        result = 31 * result + this.upserts.hashCode();
        return result;
    }
    
    @Override
    public String toString() {
        return "AcknowledgedBulkWriteResult{insertedCount=" + this.insertedCount + ", matchedCount=" + this.matchedCount + ", removedCount=" + this.removedCount + ", modifiedCount=" + this.modifiedCount + ", upserts=" + this.upserts + '}';
    }
}
