package com.mongodb.bulk;

import org.bson.*;
import com.mongodb.client.model.*;
import com.mongodb.assertions.*;
import java.util.concurrent.*;
import java.util.*;

public class IndexRequest
{
    private final BsonDocument keys;
    private static final List<Integer> VALID_TEXT_INDEX_VERSIONS;
    private static final List<Integer> VALID_SPHERE_INDEX_VERSIONS;
    private boolean background;
    private boolean unique;
    private String name;
    private boolean sparse;
    private Long expireAfterSeconds;
    private Integer version;
    private BsonDocument weights;
    private String defaultLanguage;
    private String languageOverride;
    private Integer textVersion;
    private Integer sphereVersion;
    private Integer bits;
    private Double min;
    private Double max;
    private Double bucketSize;
    private boolean dropDups;
    private BsonDocument storageEngine;
    private BsonDocument partialFilterExpression;
    private Collation collation;
    
    public IndexRequest(final BsonDocument keys) {
        this.keys = Assertions.notNull("keys", keys);
    }
    
    public BsonDocument getKeys() {
        return this.keys;
    }
    
    public boolean isBackground() {
        return this.background;
    }
    
    public IndexRequest background(final boolean background) {
        this.background = background;
        return this;
    }
    
    public boolean isUnique() {
        return this.unique;
    }
    
    public IndexRequest unique(final boolean unique) {
        this.unique = unique;
        return this;
    }
    
    public String getName() {
        return this.name;
    }
    
    public IndexRequest name(final String name) {
        this.name = name;
        return this;
    }
    
    public boolean isSparse() {
        return this.sparse;
    }
    
    public IndexRequest sparse(final boolean sparse) {
        this.sparse = sparse;
        return this;
    }
    
    public Long getExpireAfter(final TimeUnit timeUnit) {
        if (this.expireAfterSeconds == null) {
            return null;
        }
        return timeUnit.convert(this.expireAfterSeconds, TimeUnit.SECONDS);
    }
    
    public IndexRequest expireAfter(final Long expireAfter, final TimeUnit timeUnit) {
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
    
    public IndexRequest version(final Integer version) {
        this.version = version;
        return this;
    }
    
    public BsonDocument getWeights() {
        return this.weights;
    }
    
    public IndexRequest weights(final BsonDocument weights) {
        this.weights = weights;
        return this;
    }
    
    public String getDefaultLanguage() {
        return this.defaultLanguage;
    }
    
    public IndexRequest defaultLanguage(final String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
        return this;
    }
    
    public String getLanguageOverride() {
        return this.languageOverride;
    }
    
    public IndexRequest languageOverride(final String languageOverride) {
        this.languageOverride = languageOverride;
        return this;
    }
    
    public Integer getTextVersion() {
        return this.textVersion;
    }
    
    public IndexRequest textVersion(final Integer textVersion) {
        if (textVersion != null) {
            Assertions.isTrueArgument("textVersion must be 1, 2 or 3", IndexRequest.VALID_TEXT_INDEX_VERSIONS.contains(textVersion));
        }
        this.textVersion = textVersion;
        return this;
    }
    
    public Integer getSphereVersion() {
        return this.sphereVersion;
    }
    
    public IndexRequest sphereVersion(final Integer sphereVersion) {
        if (sphereVersion != null) {
            Assertions.isTrueArgument("sphereIndexVersion must be 1 or 2", IndexRequest.VALID_SPHERE_INDEX_VERSIONS.contains(sphereVersion));
        }
        this.sphereVersion = sphereVersion;
        return this;
    }
    
    public Integer getBits() {
        return this.bits;
    }
    
    public IndexRequest bits(final Integer bits) {
        this.bits = bits;
        return this;
    }
    
    public Double getMin() {
        return this.min;
    }
    
    public IndexRequest min(final Double min) {
        this.min = min;
        return this;
    }
    
    public Double getMax() {
        return this.max;
    }
    
    public IndexRequest max(final Double max) {
        this.max = max;
        return this;
    }
    
    public Double getBucketSize() {
        return this.bucketSize;
    }
    
    public IndexRequest bucketSize(final Double bucketSize) {
        this.bucketSize = bucketSize;
        return this;
    }
    
    public boolean getDropDups() {
        return this.dropDups;
    }
    
    public IndexRequest dropDups(final boolean dropDups) {
        this.dropDups = dropDups;
        return this;
    }
    
    public BsonDocument getStorageEngine() {
        return this.storageEngine;
    }
    
    public IndexRequest storageEngine(final BsonDocument storageEngineOptions) {
        this.storageEngine = storageEngineOptions;
        return this;
    }
    
    public BsonDocument getPartialFilterExpression() {
        return this.partialFilterExpression;
    }
    
    public IndexRequest partialFilterExpression(final BsonDocument partialFilterExpression) {
        this.partialFilterExpression = partialFilterExpression;
        return this;
    }
    
    public Collation getCollation() {
        return this.collation;
    }
    
    public IndexRequest collation(final Collation collation) {
        this.collation = collation;
        return this;
    }
    
    static {
        VALID_TEXT_INDEX_VERSIONS = Arrays.asList(1, 2, 3);
        VALID_SPHERE_INDEX_VERSIONS = Arrays.asList(1, 2);
    }
}
