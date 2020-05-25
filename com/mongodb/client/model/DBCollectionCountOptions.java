package com.mongodb.client.model;

import com.mongodb.*;
import com.mongodb.assertions.*;
import java.util.concurrent.*;

public class DBCollectionCountOptions
{
    private DBObject hint;
    private String hintString;
    private int limit;
    private int skip;
    private long maxTimeMS;
    private ReadPreference readPreference;
    private ReadConcern readConcern;
    private Collation collation;
    
    public DBObject getHint() {
        return this.hint;
    }
    
    public String getHintString() {
        return this.hintString;
    }
    
    public DBCollectionCountOptions hint(final DBObject hint) {
        this.hint = hint;
        return this;
    }
    
    public DBCollectionCountOptions hintString(final String hint) {
        this.hintString = hint;
        return this;
    }
    
    public int getLimit() {
        return this.limit;
    }
    
    public DBCollectionCountOptions limit(final int limit) {
        this.limit = limit;
        return this;
    }
    
    public int getSkip() {
        return this.skip;
    }
    
    public DBCollectionCountOptions skip(final int skip) {
        this.skip = skip;
        return this;
    }
    
    public DBCollectionCountOptions limit(final long limit) {
        Assertions.isTrue("limit is too large", limit <= 2147483647L);
        this.limit = (int)limit;
        return this;
    }
    
    public DBCollectionCountOptions skip(final long skip) {
        Assertions.isTrue("skip is too large", skip <= 2147483647L);
        this.skip = (int)skip;
        return this;
    }
    
    public long getMaxTime(final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        return timeUnit.convert(this.maxTimeMS, TimeUnit.MILLISECONDS);
    }
    
    public DBCollectionCountOptions maxTime(final long maxTime, final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        this.maxTimeMS = TimeUnit.MILLISECONDS.convert(maxTime, timeUnit);
        return this;
    }
    
    public ReadPreference getReadPreference() {
        return this.readPreference;
    }
    
    public DBCollectionCountOptions readPreference(final ReadPreference readPreference) {
        this.readPreference = readPreference;
        return this;
    }
    
    public ReadConcern getReadConcern() {
        return this.readConcern;
    }
    
    public DBCollectionCountOptions readConcern(final ReadConcern readConcern) {
        this.readConcern = readConcern;
        return this;
    }
    
    public Collation getCollation() {
        return this.collation;
    }
    
    public DBCollectionCountOptions collation(final Collation collation) {
        this.collation = collation;
        return this;
    }
}
