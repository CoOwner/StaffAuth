package com.mongodb;

import org.bson.*;

public abstract class WriteConcernResult
{
    public abstract boolean wasAcknowledged();
    
    public abstract int getCount();
    
    public abstract boolean isUpdateOfExisting();
    
    public abstract BsonValue getUpsertedId();
    
    public static WriteConcernResult acknowledged(final int count, final boolean isUpdateOfExisting, final BsonValue upsertedId) {
        return new WriteConcernResult() {
            @Override
            public boolean wasAcknowledged() {
                return true;
            }
            
            @Override
            public int getCount() {
                return count;
            }
            
            @Override
            public boolean isUpdateOfExisting() {
                return isUpdateOfExisting;
            }
            
            @Override
            public BsonValue getUpsertedId() {
                return upsertedId;
            }
            
            @Override
            public boolean equals(final Object o) {
                if (this == o) {
                    return true;
                }
                if (o == null || this.getClass() != o.getClass()) {
                    return false;
                }
                final WriteConcernResult that = (WriteConcernResult)o;
                if (!that.wasAcknowledged()) {
                    return false;
                }
                if (count != that.getCount()) {
                    return false;
                }
                if (isUpdateOfExisting != that.isUpdateOfExisting()) {
                    return false;
                }
                if (upsertedId != null) {
                    if (upsertedId.equals(that.getUpsertedId())) {
                        return true;
                    }
                }
                else if (that.getUpsertedId() == null) {
                    return true;
                }
                return false;
            }
            
            @Override
            public int hashCode() {
                int result = count;
                result = 31 * result + (isUpdateOfExisting ? 1 : 0);
                result = 31 * result + ((upsertedId != null) ? upsertedId.hashCode() : 0);
                return result;
            }
            
            @Override
            public String toString() {
                return "AcknowledgedWriteResult{count=" + count + ", isUpdateOfExisting=" + isUpdateOfExisting + ", upsertedId=" + upsertedId + '}';
            }
        };
    }
    
    public static WriteConcernResult unacknowledged() {
        return new WriteConcernResult() {
            @Override
            public boolean wasAcknowledged() {
                return false;
            }
            
            @Override
            public int getCount() {
                throw this.getUnacknowledgedWriteException();
            }
            
            @Override
            public boolean isUpdateOfExisting() {
                throw this.getUnacknowledgedWriteException();
            }
            
            @Override
            public BsonValue getUpsertedId() {
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
                final WriteConcernResult that = (WriteConcernResult)o;
                return !that.wasAcknowledged();
            }
            
            @Override
            public int hashCode() {
                return 1;
            }
            
            @Override
            public String toString() {
                return "UnacknowledgedWriteResult{}";
            }
            
            private UnsupportedOperationException getUnacknowledgedWriteException() {
                return new UnsupportedOperationException("Cannot get information about an unacknowledged write");
            }
        };
    }
}
