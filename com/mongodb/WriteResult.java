package com.mongodb;

public class WriteResult
{
    private final boolean acknowledged;
    private final int n;
    private final boolean updateOfExisting;
    private final Object upsertedId;
    
    public static WriteResult unacknowledged() {
        return new WriteResult();
    }
    
    public WriteResult(final int n, final boolean updateOfExisting, final Object upsertedId) {
        this.acknowledged = true;
        this.n = n;
        this.updateOfExisting = updateOfExisting;
        this.upsertedId = upsertedId;
    }
    
    WriteResult() {
        this.acknowledged = false;
        this.n = 0;
        this.updateOfExisting = false;
        this.upsertedId = null;
    }
    
    public boolean wasAcknowledged() {
        return this.acknowledged;
    }
    
    public int getN() {
        this.throwIfUnacknowledged("n");
        return this.n;
    }
    
    public Object getUpsertedId() {
        this.throwIfUnacknowledged("upsertedId");
        return this.upsertedId;
    }
    
    public boolean isUpdateOfExisting() {
        this.throwIfUnacknowledged("updateOfExisting");
        return this.updateOfExisting;
    }
    
    @Override
    public String toString() {
        if (this.acknowledged) {
            return "WriteResult{n=" + this.n + ", updateOfExisting=" + this.updateOfExisting + ", upsertedId=" + this.upsertedId + '}';
        }
        return "WriteResult{acknowledged=false}";
    }
    
    private void throwIfUnacknowledged(final String property) {
        if (!this.acknowledged) {
            throw new UnsupportedOperationException("Cannot get " + property + " property for an unacknowledged write");
        }
    }
}
