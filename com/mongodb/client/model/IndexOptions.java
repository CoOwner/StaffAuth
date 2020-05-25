package com.mongodb.client.model;

import org.bson.conversions.*;
import java.util.concurrent.*;

public class IndexOptions
{
    private boolean background;
    private boolean unique;
    private String name;
    private boolean sparse;
    private Long expireAfterSeconds;
    private Integer version;
    private Bson weights;
    private String defaultLanguage;
    private String languageOverride;
    private Integer textVersion;
    private Integer sphereVersion;
    private Integer bits;
    private Double min;
    private Double max;
    private Double bucketSize;
    private Bson storageEngine;
    private Bson partialFilterExpression;
    private Collation collation;
    
    public boolean isBackground() {
        return this.background;
    }
    
    public IndexOptions background(final boolean background) {
        this.background = background;
        return this;
    }
    
    public boolean isUnique() {
        return this.unique;
    }
    
    public IndexOptions unique(final boolean unique) {
        this.unique = unique;
        return this;
    }
    
    public String getName() {
        return this.name;
    }
    
    public IndexOptions name(final String name) {
        this.name = name;
        return this;
    }
    
    public boolean isSparse() {
        return this.sparse;
    }
    
    public IndexOptions sparse(final boolean sparse) {
        this.sparse = sparse;
        return this;
    }
    
    public Long getExpireAfter(final TimeUnit timeUnit) {
        if (this.expireAfterSeconds == null) {
            return null;
        }
        return timeUnit.convert(this.expireAfterSeconds, TimeUnit.SECONDS);
    }
    
    public IndexOptions expireAfter(final Long expireAfter, final TimeUnit timeUnit) {
        if (expireAfter == null) {
            this.expireAfterSeconds = null;
        }
        else {
            this.expireAfterSeconds = TimeUnit.SECONDS.convert(expireAfter, timeUnit);
        }
        return this;
    }
    
    public Integer getVersion() {
        return this.version;
    }
    
    public IndexOptions version(final Integer version) {
        this.version = version;
        return this;
    }
    
    public Bson getWeights() {
        return this.weights;
    }
    
    public IndexOptions weights(final Bson weights) {
        this.weights = weights;
        return this;
    }
    
    public String getDefaultLanguage() {
        return this.defaultLanguage;
    }
    
    public IndexOptions defaultLanguage(final String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
        return this;
    }
    
    public String getLanguageOverride() {
        return this.languageOverride;
    }
    
    public IndexOptions languageOverride(final String languageOverride) {
        this.languageOverride = languageOverride;
        return this;
    }
    
    public Integer getTextVersion() {
        return this.textVersion;
    }
    
    public IndexOptions textVersion(final Integer textVersion) {
        this.textVersion = textVersion;
        return this;
    }
    
    public Integer getSphereVersion() {
        return this.sphereVersion;
    }
    
    public IndexOptions sphereVersion(final Integer sphereVersion) {
        this.sphereVersion = sphereVersion;
        return this;
    }
    
    public Integer getBits() {
        return this.bits;
    }
    
    public IndexOptions bits(final Integer bits) {
        this.bits = bits;
        return this;
    }
    
    public Double getMin() {
        return this.min;
    }
    
    public IndexOptions min(final Double min) {
        this.min = min;
        return this;
    }
    
    public Double getMax() {
        return this.max;
    }
    
    public IndexOptions max(final Double max) {
        this.max = max;
        return this;
    }
    
    public Double getBucketSize() {
        return this.bucketSize;
    }
    
    public IndexOptions bucketSize(final Double bucketSize) {
        this.bucketSize = bucketSize;
        return this;
    }
    
    public Bson getStorageEngine() {
        return this.storageEngine;
    }
    
    public IndexOptions storageEngine(final Bson storageEngine) {
        this.storageEngine = storageEngine;
        return this;
    }
    
    public Bson getPartialFilterExpression() {
        return this.partialFilterExpression;
    }
    
    public IndexOptions partialFilterExpression(final Bson partialFilterExpression) {
        this.partialFilterExpression = partialFilterExpression;
        return this;
    }
    
    public Collation getCollation() {
        return this.collation;
    }
    
    public IndexOptions collation(final Collation collation) {
        this.collation = collation;
        return this;
    }
}
