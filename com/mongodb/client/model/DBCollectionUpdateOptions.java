package com.mongodb.client.model;

import com.mongodb.*;

public class DBCollectionUpdateOptions
{
    private boolean upsert;
    private Boolean bypassDocumentValidation;
    private boolean multi;
    private Collation collation;
    private WriteConcern writeConcern;
    private DBEncoder encoder;
    
    public boolean isUpsert() {
        return this.upsert;
    }
    
    public DBCollectionUpdateOptions upsert(final boolean isUpsert) {
        this.upsert = isUpsert;
        return this;
    }
    
    public Boolean getBypassDocumentValidation() {
        return this.bypassDocumentValidation;
    }
    
    public DBCollectionUpdateOptions bypassDocumentValidation(final Boolean bypassDocumentValidation) {
        this.bypassDocumentValidation = bypassDocumentValidation;
        return this;
    }
    
    public DBCollectionUpdateOptions multi(final boolean multi) {
        this.multi = multi;
        return this;
    }
    
    public boolean isMulti() {
        return this.multi;
    }
    
    public Collation getCollation() {
        return this.collation;
    }
    
    public DBCollectionUpdateOptions collation(final Collation collation) {
        this.collation = collation;
        return this;
    }
    
    public WriteConcern getWriteConcern() {
        return this.writeConcern;
    }
    
    public DBCollectionUpdateOptions writeConcern(final WriteConcern writeConcern) {
        this.writeConcern = writeConcern;
        return this;
    }
    
    public DBEncoder getEncoder() {
        return this.encoder;
    }
    
    public DBCollectionUpdateOptions encoder(final DBEncoder encoder) {
        this.encoder = encoder;
        return this;
    }
}
