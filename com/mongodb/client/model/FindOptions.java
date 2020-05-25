package com.mongodb.client.model;

import org.bson.conversions.*;
import com.mongodb.*;
import java.util.concurrent.*;
import com.mongodb.assertions.*;

public final class FindOptions
{
    private int batchSize;
    private int limit;
    private Bson modifiers;
    private Bson projection;
    private long maxTimeMS;
    private long maxAwaitTimeMS;
    private int skip;
    private Bson sort;
    private CursorType cursorType;
    private boolean noCursorTimeout;
    private boolean oplogReplay;
    private boolean partial;
    private Collation collation;
    
    public FindOptions() {
        this.cursorType = CursorType.NonTailable;
    }
    
    public FindOptions(final FindOptions from) {
        this.cursorType = CursorType.NonTailable;
        this.batchSize = from.batchSize;
        this.limit = from.limit;
        this.modifiers = from.modifiers;
        this.projection = from.projection;
        this.maxTimeMS = from.maxTimeMS;
        this.maxAwaitTimeMS = from.maxAwaitTimeMS;
        this.skip = from.skip;
        this.sort = from.sort;
        this.cursorType = from.cursorType;
        this.noCursorTimeout = from.noCursorTimeout;
        this.oplogReplay = from.oplogReplay;
        this.partial = from.partial;
    }
    
    public int getLimit() {
        return this.limit;
    }
    
    public FindOptions limit(final int limit) {
        this.limit = limit;
        return this;
    }
    
    public int getSkip() {
        return this.skip;
    }
    
    public FindOptions skip(final int skip) {
        this.skip = skip;
        return this;
    }
    
    public long getMaxTime(final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        return timeUnit.convert(this.maxTimeMS, TimeUnit.MILLISECONDS);
    }
    
    public FindOptions maxTime(final long maxTime, final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        Assertions.isTrueArgument("maxTime > = 0", maxTime >= 0L);
        this.maxTimeMS = TimeUnit.MILLISECONDS.convert(maxTime, timeUnit);
        return this;
    }
    
    public long getMaxAwaitTime(final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        return timeUnit.convert(this.maxAwaitTimeMS, TimeUnit.MILLISECONDS);
    }
    
    public FindOptions maxAwaitTime(final long maxAwaitTime, final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        Assertions.isTrueArgument("maxAwaitTime > = 0", maxAwaitTime >= 0L);
        this.maxAwaitTimeMS = TimeUnit.MILLISECONDS.convert(maxAwaitTime, timeUnit);
        return this;
    }
    
    public int getBatchSize() {
        return this.batchSize;
    }
    
    public FindOptions batchSize(final int batchSize) {
        this.batchSize = batchSize;
        return this;
    }
    
    public Bson getModifiers() {
        return this.modifiers;
    }
    
    public FindOptions modifiers(final Bson modifiers) {
        this.modifiers = modifiers;
        return this;
    }
    
    public Bson getProjection() {
        return this.projection;
    }
    
    public FindOptions projection(final Bson projection) {
        this.projection = projection;
        return this;
    }
    
    public Bson getSort() {
        return this.sort;
    }
    
    public FindOptions sort(final Bson sort) {
        this.sort = sort;
        return this;
    }
    
    public boolean isNoCursorTimeout() {
        return this.noCursorTimeout;
    }
    
    public FindOptions noCursorTimeout(final boolean noCursorTimeout) {
        this.noCursorTimeout = noCursorTimeout;
        return this;
    }
    
    public boolean isOplogReplay() {
        return this.oplogReplay;
    }
    
    public FindOptions oplogReplay(final boolean oplogReplay) {
        this.oplogReplay = oplogReplay;
        return this;
    }
    
    public boolean isPartial() {
        return this.partial;
    }
    
    public FindOptions partial(final boolean partial) {
        this.partial = partial;
        return this;
    }
    
    public CursorType getCursorType() {
        return this.cursorType;
    }
    
    public FindOptions cursorType(final CursorType cursorType) {
        this.cursorType = Assertions.notNull("cursorType", cursorType);
        return this;
    }
    
    public Collation getCollation() {
        return this.collation;
    }
    
    public FindOptions collation(final Collation collation) {
        this.collation = collation;
        return this;
    }
    
    @Override
    public String toString() {
        return "FindOptions{, batchSize=" + this.batchSize + ", limit=" + this.limit + ", modifiers=" + this.modifiers + ", projection=" + this.projection + ", maxTimeMS=" + this.maxTimeMS + ", skip=" + this.skip + ", sort=" + this.sort + ", cursorType=" + this.cursorType + ", noCursorTimeout=" + this.noCursorTimeout + ", oplogReplay=" + this.oplogReplay + ", partial=" + this.partial + ", collation=" + this.collation + '}';
    }
}
