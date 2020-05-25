package com.mongodb.client.model;

import com.mongodb.*;
import java.util.concurrent.*;
import com.mongodb.assertions.*;

public final class DBCollectionFindOptions
{
    private int batchSize;
    private int limit;
    private DBObject modifiers;
    private DBObject projection;
    private long maxTimeMS;
    private long maxAwaitTimeMS;
    private int skip;
    private DBObject sort;
    private CursorType cursorType;
    private boolean noCursorTimeout;
    private boolean oplogReplay;
    private boolean partial;
    private ReadPreference readPreference;
    private ReadConcern readConcern;
    private Collation collation;
    
    public DBCollectionFindOptions() {
        this.modifiers = new BasicDBObject();
        this.cursorType = CursorType.NonTailable;
    }
    
    public DBCollectionFindOptions copy() {
        final DBCollectionFindOptions copiedOptions = new DBCollectionFindOptions();
        copiedOptions.batchSize(this.batchSize);
        copiedOptions.limit(this.limit);
        copiedOptions.modifiers(this.modifiers);
        copiedOptions.projection(this.projection);
        copiedOptions.maxTime(this.maxTimeMS, TimeUnit.MILLISECONDS);
        copiedOptions.maxAwaitTime(this.maxAwaitTimeMS, TimeUnit.MILLISECONDS);
        copiedOptions.skip(this.skip);
        copiedOptions.sort(this.sort);
        copiedOptions.cursorType(this.cursorType);
        copiedOptions.noCursorTimeout(this.noCursorTimeout);
        copiedOptions.oplogReplay(this.oplogReplay);
        copiedOptions.partial(this.partial);
        copiedOptions.readPreference(this.readPreference);
        copiedOptions.readConcern(this.readConcern);
        copiedOptions.collation(this.collation);
        return copiedOptions;
    }
    
    public int getLimit() {
        return this.limit;
    }
    
    public DBCollectionFindOptions limit(final int limit) {
        this.limit = limit;
        return this;
    }
    
    public int getSkip() {
        return this.skip;
    }
    
    public DBCollectionFindOptions skip(final int skip) {
        this.skip = skip;
        return this;
    }
    
    public long getMaxTime(final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        return timeUnit.convert(this.maxTimeMS, TimeUnit.MILLISECONDS);
    }
    
    public DBCollectionFindOptions maxTime(final long maxTime, final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        Assertions.isTrueArgument("maxTime > = 0", maxTime >= 0L);
        this.maxTimeMS = TimeUnit.MILLISECONDS.convert(maxTime, timeUnit);
        return this;
    }
    
    public long getMaxAwaitTime(final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        return timeUnit.convert(this.maxAwaitTimeMS, TimeUnit.MILLISECONDS);
    }
    
    public DBCollectionFindOptions maxAwaitTime(final long maxAwaitTime, final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        Assertions.isTrueArgument("maxAwaitTime > = 0", maxAwaitTime >= 0L);
        this.maxAwaitTimeMS = TimeUnit.MILLISECONDS.convert(maxAwaitTime, timeUnit);
        return this;
    }
    
    public int getBatchSize() {
        return this.batchSize;
    }
    
    public DBCollectionFindOptions batchSize(final int batchSize) {
        this.batchSize = batchSize;
        return this;
    }
    
    public DBObject getModifiers() {
        return this.modifiers;
    }
    
    public DBCollectionFindOptions modifiers(final DBObject modifiers) {
        this.modifiers = Assertions.notNull("modifiers", modifiers);
        return this;
    }
    
    public DBObject getProjection() {
        return this.projection;
    }
    
    public DBCollectionFindOptions projection(final DBObject projection) {
        this.projection = projection;
        return this;
    }
    
    public DBObject getSort() {
        return this.sort;
    }
    
    public DBCollectionFindOptions sort(final DBObject sort) {
        this.sort = sort;
        return this;
    }
    
    public boolean isNoCursorTimeout() {
        return this.noCursorTimeout;
    }
    
    public DBCollectionFindOptions noCursorTimeout(final boolean noCursorTimeout) {
        this.noCursorTimeout = noCursorTimeout;
        return this;
    }
    
    public boolean isOplogReplay() {
        return this.oplogReplay;
    }
    
    public DBCollectionFindOptions oplogReplay(final boolean oplogReplay) {
        this.oplogReplay = oplogReplay;
        return this;
    }
    
    public boolean isPartial() {
        return this.partial;
    }
    
    public DBCollectionFindOptions partial(final boolean partial) {
        this.partial = partial;
        return this;
    }
    
    public CursorType getCursorType() {
        return this.cursorType;
    }
    
    public DBCollectionFindOptions cursorType(final CursorType cursorType) {
        this.cursorType = Assertions.notNull("cursorType", cursorType);
        return this;
    }
    
    public ReadPreference getReadPreference() {
        return this.readPreference;
    }
    
    public DBCollectionFindOptions readPreference(final ReadPreference readPreference) {
        this.readPreference = readPreference;
        return this;
    }
    
    public ReadConcern getReadConcern() {
        return this.readConcern;
    }
    
    public DBCollectionFindOptions readConcern(final ReadConcern readConcern) {
        this.readConcern = readConcern;
        return this;
    }
    
    public Collation getCollation() {
        return this.collation;
    }
    
    public DBCollectionFindOptions collation(final Collation collation) {
        this.collation = collation;
        return this;
    }
}
