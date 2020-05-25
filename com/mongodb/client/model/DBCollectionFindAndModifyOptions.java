package com.mongodb.client.model;

import com.mongodb.*;
import java.util.concurrent.*;
import com.mongodb.assertions.*;

public final class DBCollectionFindAndModifyOptions
{
    private DBObject projection;
    private DBObject sort;
    private boolean remove;
    private DBObject update;
    private boolean upsert;
    private boolean returnNew;
    private Boolean bypassDocumentValidation;
    private long maxTimeMS;
    private WriteConcern writeConcern;
    private Collation collation;
    
    public DBObject getProjection() {
        return this.projection;
    }
    
    public DBCollectionFindAndModifyOptions projection(final DBObject projection) {
        this.projection = projection;
        return this;
    }
    
    public DBObject getSort() {
        return this.sort;
    }
    
    public DBCollectionFindAndModifyOptions sort(final DBObject sort) {
        this.sort = sort;
        return this;
    }
    
    public boolean isRemove() {
        return this.remove;
    }
    
    public DBCollectionFindAndModifyOptions remove(final boolean remove) {
        this.remove = remove;
        return this;
    }
    
    public DBObject getUpdate() {
        return this.update;
    }
    
    public DBCollectionFindAndModifyOptions update(final DBObject update) {
        this.update = update;
        return this;
    }
    
    public boolean isUpsert() {
        return this.upsert;
    }
    
    public DBCollectionFindAndModifyOptions upsert(final boolean upsert) {
        this.upsert = upsert;
        return this;
    }
    
    public boolean returnNew() {
        return this.returnNew;
    }
    
    public DBCollectionFindAndModifyOptions returnNew(final boolean returnNew) {
        this.returnNew = returnNew;
        return this;
    }
    
    public Boolean getBypassDocumentValidation() {
        return this.bypassDocumentValidation;
    }
    
    public DBCollectionFindAndModifyOptions bypassDocumentValidation(final Boolean bypassDocumentValidation) {
        this.bypassDocumentValidation = bypassDocumentValidation;
        return this;
    }
    
    public long getMaxTime(final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        return timeUnit.convert(this.maxTimeMS, TimeUnit.MILLISECONDS);
    }
    
    public DBCollectionFindAndModifyOptions maxTime(final long maxTime, final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        Assertions.isTrueArgument("maxTime > = 0", maxTime >= 0L);
        this.maxTimeMS = TimeUnit.MILLISECONDS.convert(maxTime, timeUnit);
        return this;
    }
    
    public WriteConcern getWriteConcern() {
        return this.writeConcern;
    }
    
    public DBCollectionFindAndModifyOptions writeConcern(final WriteConcern writeConcern) {
        this.writeConcern = writeConcern;
        return this;
    }
    
    public Collation getCollation() {
        return this.collation;
    }
    
    public DBCollectionFindAndModifyOptions collation(final Collation collation) {
        this.collation = collation;
        return this;
    }
}
