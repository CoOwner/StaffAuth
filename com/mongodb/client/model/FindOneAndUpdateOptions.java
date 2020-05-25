package com.mongodb.client.model;

import org.bson.conversions.*;
import java.util.concurrent.*;
import com.mongodb.assertions.*;

public class FindOneAndUpdateOptions
{
    private Bson projection;
    private Bson sort;
    private boolean upsert;
    private ReturnDocument returnDocument;
    private long maxTimeMS;
    private Boolean bypassDocumentValidation;
    private Collation collation;
    
    public FindOneAndUpdateOptions() {
        this.returnDocument = ReturnDocument.BEFORE;
    }
    
    public Bson getProjection() {
        return this.projection;
    }
    
    public FindOneAndUpdateOptions projection(final Bson projection) {
        this.projection = projection;
        return this;
    }
    
    public Bson getSort() {
        return this.sort;
    }
    
    public FindOneAndUpdateOptions sort(final Bson sort) {
        this.sort = sort;
        return this;
    }
    
    public boolean isUpsert() {
        return this.upsert;
    }
    
    public FindOneAndUpdateOptions upsert(final boolean upsert) {
        this.upsert = upsert;
        return this;
    }
    
    public ReturnDocument getReturnDocument() {
        return this.returnDocument;
    }
    
    public FindOneAndUpdateOptions returnDocument(final ReturnDocument returnDocument) {
        this.returnDocument = returnDocument;
        return this;
    }
    
    public FindOneAndUpdateOptions maxTime(final long maxTime, final TimeUnit timeUnit) {
        Assertions.notNull("timeUnit", timeUnit);
        this.maxTimeMS = TimeUnit.MILLISECONDS.convert(maxTime, timeUnit);
        return this;
    }
    
    public long getMaxTime(final TimeUnit timeUnit) {
        return timeUnit.convert(this.maxTimeMS, TimeUnit.MILLISECONDS);
    }
    
    public Boolean getBypassDocumentValidation() {
        return this.bypassDocumentValidation;
    }
    
    public FindOneAndUpdateOptions bypassDocumentValidation(final Boolean bypassDocumentValidation) {
        this.bypassDocumentValidation = bypassDocumentValidation;
        return this;
    }
    
    public Collation getCollation() {
        return this.collation;
    }
    
    public FindOneAndUpdateOptions collation(final Collation collation) {
        this.collation = collation;
        return this;
    }
}
