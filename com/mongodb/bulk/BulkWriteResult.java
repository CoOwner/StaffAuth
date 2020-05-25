package com.mongodb.bulk;

import java.util.*;

public abstract class BulkWriteResult
{
    public abstract boolean wasAcknowledged();
    
    public abstract int getInsertedCount();
    
    public abstract int getMatchedCount();
    
    public abstract int getDeletedCount();
    
    public abstract boolean isModifiedCountAvailable();
    
    public abstract int getModifiedCount();
    
    public abstract List<BulkWriteUpsert> getUpserts();
    
    public static BulkWriteResult acknowledged(final WriteRequest.Type type, final int count, final List<BulkWriteUpsert> upserts) {
        return acknowledged(type, count, 0, upserts);
    }
    
    public static BulkWriteResult acknowledged(final WriteRequest.Type type, final int count, final Integer modifiedCount, final List<BulkWriteUpsert> upserts) {
        return acknowledged((type == WriteRequest.Type.INSERT) ? count : 0, (type == WriteRequest.Type.UPDATE || type == WriteRequest.Type.REPLACE) ? count : 0, (type == WriteRequest.Type.DELETE) ? count : 0, modifiedCount, upserts);
    }
    
    public static BulkWriteResult acknowledged(final int insertedCount, final int matchedCount, final int removedCount, final Integer modifiedCount, final List<BulkWriteUpsert> upserts) {
        return new BulkWriteResult() {
            @Override
            public boolean wasAcknowledged() {
                return true;
            }
            
            @Override
            public int getInsertedCount() {
                return insertedCount;
            }
            
            @Override
            public int getMatchedCount() {
                return matchedCount;
            }
            
            @Override
            public int getDeletedCount() {
                return removedCount;
            }
            
            @Override
            public boolean isModifiedCountAvailable() {
                return modifiedCount != null;
            }
            
            @Override
            public int getModifiedCount() {
                if (modifiedCount == null) {
                    throw new UnsupportedOperationException("The modifiedCount is not available because at least one of the servers that was updated was not able to provide this information (the server is must be at least version 2.6");
                }
                return modifiedCount;
            }
            
            @Override
            public List<BulkWriteUpsert> getUpserts() {
                return upserts;
            }
            
            @Override
            public boolean equals(final Object o) {
                if (this == o) {
                    return true;
                }
                if (o == null || this.getClass() != o.getClass()) {
                    return false;
                }
                final BulkWriteResult that = (BulkWriteResult)o;
                return that.wasAcknowledged() && insertedCount == that.getInsertedCount() && this.isModifiedCountAvailable() == that.isModifiedCountAvailable() && (modifiedCount == null || modifiedCount.equals(that.getModifiedCount())) && removedCount == that.getDeletedCount() && matchedCount == that.getMatchedCount() && upserts.equals(that.getUpserts());
            }
            
            @Override
            public int hashCode() {
                int result = upserts.hashCode();
                result = 31 * result + insertedCount;
                result = 31 * result + matchedCount;
                result = 31 * result + removedCount;
                result = 31 * result + ((modifiedCount != null) ? modifiedCount.hashCode() : 0);
                return result;
            }
            
            @Override
            public String toString() {
                return "AcknowledgedBulkWriteResult{insertedCount=" + insertedCount + ", matchedCount=" + matchedCount + ", removedCount=" + removedCount + ", modifiedCount=" + modifiedCount + ", upserts=" + upserts + '}';
            }
        };
    }
    
    public static BulkWriteResult unacknowledged() {
        return new BulkWriteResult() {
            @Override
            public boolean wasAcknowledged() {
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
            public int getDeletedCount() {
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
            
            @Override
            public boolean equals(final Object o) {
                if (this == o) {
                    return true;
                }
                if (o == null || this.getClass() != o.getClass()) {
                    return false;
                }
                final BulkWriteResult that = (BulkWriteResult)o;
                return !that.wasAcknowledged();
            }
            
            @Override
            public int hashCode() {
                return 0;
            }
            
            @Override
            public String toString() {
                return "UnacknowledgedBulkWriteResult{}";
            }
            
            private UnsupportedOperationException getUnacknowledgedWriteException() {
                return new UnsupportedOperationException("Cannot get information about an unacknowledged write");
            }
        };
    }
}
