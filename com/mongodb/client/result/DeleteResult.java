package com.mongodb.client.result;

public abstract class DeleteResult
{
    public abstract boolean wasAcknowledged();
    
    public abstract long getDeletedCount();
    
    public static DeleteResult acknowledged(final long deletedCount) {
        return new AcknowledgedDeleteResult(deletedCount);
    }
    
    public static DeleteResult unacknowledged() {
        return new UnacknowledgedDeleteResult();
    }
    
    private static class AcknowledgedDeleteResult extends DeleteResult
    {
        private final long deletedCount;
        
        public AcknowledgedDeleteResult(final long deletedCount) {
            this.deletedCount = deletedCount;
        }
        
        @Override
        public boolean wasAcknowledged() {
            return true;
        }
        
        @Override
        public long getDeletedCount() {
            return this.deletedCount;
        }
        
        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || this.getClass() != o.getClass()) {
                return false;
            }
            final AcknowledgedDeleteResult that = (AcknowledgedDeleteResult)o;
            return this.deletedCount == that.deletedCount;
        }
        
        @Override
        public int hashCode() {
            return (int)(this.deletedCount ^ this.deletedCount >>> 32);
        }
        
        @Override
        public String toString() {
            return "AcknowledgedDeleteResult{deletedCount=" + this.deletedCount + '}';
        }
    }
    
    private static class UnacknowledgedDeleteResult extends DeleteResult
    {
        @Override
        public boolean wasAcknowledged() {
            return false;
        }
        
        @Override
        public long getDeletedCount() {
            throw new UnsupportedOperationException("Cannot get information about an unacknowledged delete");
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
            return "UnacknowledgedDeleteResult{}";
        }
    }
}
