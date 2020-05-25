package com.mongodb;

import java.util.*;

class UnacknowledgedBulkWriteResult extends BulkWriteResult
{
    @Override
    public boolean isAcknowledged() {
        return false;
    }
    
    @Override
    public int getInsertedCount() {
        throw this.getUnacknowledgedWriteException();
    }
    
    @Override
    public int getMatchedCount() {
        throw this.getUnacknowledgedWriteException();
    }
    
    @Override
    public int getRemovedCount() {
        throw this.getUnacknowledgedWriteException();
    }
    
    @Override
    public boolean isModifiedCountAvailable() {
        throw this.getUnacknowledgedWriteException();
    }
    
    @Override
    public int getModifiedCount() {
        throw this.getUnacknowledgedWriteException();
    }
    
    @Override
    public List<BulkWriteUpsert> getUpserts() {
        throw this.getUnacknowledgedWriteException();
    }
    
    private UnsupportedOperationException getUnacknowledgedWriteException() {
        return new UnsupportedOperationException("Can not get information about an unacknowledged write");
    }
    
    @Override
    public boolean equals(final Object o) {
        return this == o || (o != null && this.getClass() == o.getClass());
    }
    
    @Override
    public int hashCode() {
        return 0;
    }
    
    @Override
    public String toString() {
        return "UnacknowledgedBulkWriteResult{}";
    }
}
