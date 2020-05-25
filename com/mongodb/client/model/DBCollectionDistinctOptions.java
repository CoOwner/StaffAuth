package com.mongodb.client.model;

import com.mongodb.*;

public class DBCollectionDistinctOptions
{
    private DBObject filter;
    private ReadPreference readPreference;
    private ReadConcern readConcern;
    private Collation collation;
    
    public DBObject getFilter() {
        return this.filter;
    }
    
    public DBCollectionDistinctOptions filter(final DBObject filter) {
        this.filter = filter;
        return this;
    }
    
    public ReadPreference getReadPreference() {
        return this.readPreference;
    }
    
    public DBCollectionDistinctOptions readPreference(final ReadPreference readPreference) {
        this.readPreference = readPreference;
        return this;
    }
    
    public ReadConcern getReadConcern() {
        return this.readConcern;
    }
    
    public DBCollectionDistinctOptions readConcern(final ReadConcern readConcern) {
        this.readConcern = readConcern;
        return this;
    }
    
    public Collation getCollation() {
        return this.collation;
    }
    
    public DBCollectionDistinctOptions collation(final Collation collation) {
        this.collation = collation;
        return this;
    }
}
