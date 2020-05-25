package com.mongodb.client.model;

import com.mongodb.*;

public final class DBCollectionRemoveOptions
{
    private Collation collation;
    private WriteConcern writeConcern;
    private DBEncoder encoder;
    
    public Collation getCollation() {
        return this.collation;
    }
    
    public DBCollectionRemoveOptions collation(final Collation collation) {
        this.collation = collation;
        return this;
    }
    
    public WriteConcern getWriteConcern() {
        return this.writeConcern;
    }
    
    public DBCollectionRemoveOptions writeConcern(final WriteConcern writeConcern) {
        this.writeConcern = writeConcern;
        return this;
    }
    
    public DBEncoder getEncoder() {
        return this.encoder;
    }
    
    public DBCollectionRemoveOptions encoder(final DBEncoder encoder) {
        this.encoder = encoder;
        return this;
    }
}
