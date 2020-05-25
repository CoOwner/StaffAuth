package com.mongodb.client.model;

import org.bson.conversions.*;
import java.util.concurrent.*;
import com.mongodb.assertions.*;

public class FindOneAndDeleteOptions
{
    private Bson projection;
    private Bson sort;
    private long maxTimeMS;
    private Collation collation;
    
    public Bson getProjection() {
        return this.projection;
    }
    
    public FindOneAndDeleteOptions projection(final Bson projection) {
        this.projection = projection;
        return this;
    }
    
    public Bson getSort() {
        return this.sort;
    }
    
    public FindOneAndDeleteOptions sort(final Bson sort) {
        this.sort = sort;
        return this;
    }
    
    public FindOneAndDeleteOptions maxTime(final long maxTime, final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        this.maxTimeMS = TimeUnit.MILLISECONDS.convert(maxTime, timeUnit);
        return this;
    }
    
    public long getMaxTime(final TimeUnit timeUnit) {
        return timeUnit.convert(this.maxTimeMS, TimeUnit.MILLISECONDS);
    }
    
    public Collation getCollation() {
        return this.collation;
    }
    
    public FindOneAndDeleteOptions collation(final Collation collation) {
        this.collation = collation;
        return this;
    }
}
