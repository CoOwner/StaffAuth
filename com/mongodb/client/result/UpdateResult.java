package com.mongodb.client.result;

import org.bson.*;

public abstract class UpdateResult
{
    public abstract boolean wasAcknowledged();
    
    public abstract long getMatchedCount();
    
    public abstract boolean isModifiedCountAvailable();
    
    public abstract long getModifiedCount();
    
    public abstract BsonValue getUpsertedId();
    
    public static UpdateResult acknowledged(final long matchedCount, final Long modifiedCount, final BsonValue upsertedId) {
        return new AcknowledgedUpdateResult(matchedCount, modifiedCount, upsertedId);
    }
    
    public static UpdateResult unacknowledged() {
        return new UnacknowledgedUpdateResult();
    }
    
    private static class AcknowledgedUpdateResult extends UpdateResult
    {
        private final long matchedCount;
        private final Long modifiedCount;
        private final BsonValue upsertedId;
        
        public AcknowledgedUpdateResult(final long matchedCount, final Long modifiedCount, final BsonValue upsertedId) {
            this.matchedCount = matchedCount;
            this.modifiedCount = modifiedCount;
            this.upsertedId = upsertedId;
        }
        
        @Override
        public boolean wasAcknowledged() {
            return true;
        }
        
        @Override
        public long getMatchedCount() {
            return this.matchedCount;
        }
        
        @Override
        public boolean isModifiedCountAvailable() {
            return this.modifiedCount != null;
        }
        
        @Override
        public long getModifiedCount() {
            if (this.modifiedCount == null) {
                throw new UnsupportedOperationException("Modified count is only available when connected to MongoDB 2.6 servers or above.");
            }
            return this.modifiedCount;
        }
        
        @Override
        public BsonValue getUpsertedId() {
            return this.upsertedId;
        }
        
        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final AcknowledgedUpdateResult that = (AcknowledgedUpdateResult)o;
            if (this.matchedCount != that.matchedCount) {
                return false;
            }
            Label_0076: {
                if (this.modifiedCount != null) {
                    if (this.modifiedCount.equals(that.modifiedCount)) {
                        break Label_0076;
                    }
                }
                else if (that.modifiedCount == null) {
                    break Label_0076;
                }
                return false;
            }
            if (this.upsertedId != null) {
                if (this.upsertedId.equals(that.upsertedId)) {
                    return true;
                }
            }
            else if (that.upsertedId == null) {
                return true;
            }
            return false;
        }
        
        @Override
        public int hashCode() {
            int result = (int)(this.matchedCount ^ this.matchedCount >>> 32);
            result = 31 * result + ((this.modifiedCount != null) ? this.modifiedCount.hashCode() : 0);
            result = 31 * result + ((this.upsertedId != null) ? this.upsertedId.hashCode() : 0);
            return result;
        }
        
        @Override
        public String toString() {
            return "AcknowledgedUpdateResult{matchedCount=" + this.matchedCount + ", modifiedCount=" + this.modifiedCount + ", upsertedId=" + this.upsertedId + '}';
        }
    }
    
    private static class UnacknowledgedUpdateResult extends UpdateResult
    {
        @Override
        public boolean wasAcknowledged() {
            return false;
        }
        
        @Override
        public long getMatchedCount() {
            throw this.getUnacknowledgedWriteException();
        }
        
        @Override
        public boolean isModifiedCountAvailable() {
            return false;
        }
        
        @Override
        public long getModifiedCount() {
            throw this.getUnacknowledgedWriteException();
        }
        
        @Override
        public BsonValue getUpsertedId() {
            throw this.getUnacknowledgedWriteException();
        }
        
        private UnsupportedOperationException getUnacknowledgedWriteException() {
            return new UnsupportedOperationException("Cannot get information about an unacknowledged update");
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
            return "UnacknowledgedUpdateResult{}";
        }
    }
}
