package com.mongodb.client.model;

import org.bson.conversions.*;
import com.mongodb.assertions.*;

public class CreateCollectionOptions
{
    private boolean autoIndex;
    private long maxDocuments;
    private boolean capped;
    private long sizeInBytes;
    private Boolean usePowerOf2Sizes;
    private Bson storageEngineOptions;
    private IndexOptionDefaults indexOptionDefaults;
    private ValidationOptions validationOptions;
    private Collation collation;
    
    public CreateCollectionOptions() {
        this.autoIndex = true;
        this.indexOptionDefaults = new IndexOptionDefaults();
        this.validationOptions = new ValidationOptions();
    }
    
    public boolean isAutoIndex() {
        return this.autoIndex;
    }
    
    public CreateCollectionOptions autoIndex(final boolean autoIndex) {
        this.autoIndex = autoIndex;
        return this;
    }
    
    public long getMaxDocuments() {
        return this.maxDocuments;
    }
    
    public CreateCollectionOptions maxDocuments(final long maxDocuments) {
        this.maxDocuments = maxDocuments;
        return this;
    }
    
    public boolean isCapped() {
        return this.capped;
    }
    
    public CreateCollectionOptions capped(final boolean capped) {
        this.capped = capped;
        return this;
    }
    
    public long getSizeInBytes() {
        return this.sizeInBytes;
    }
    
    public CreateCollectionOptions sizeInBytes(final long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
        return this;
    }
    
    @Deprecated
    public Boolean isUsePowerOf2Sizes() {
        return this.usePowerOf2Sizes;
    }
    
    @Deprecated
    public CreateCollectionOptions usePowerOf2Sizes(final Boolean usePowerOf2Sizes) {
        this.usePowerOf2Sizes = usePowerOf2Sizes;
        return this;
    }
    
    public Bson getStorageEngineOptions() {
        return this.storageEngineOptions;
    }
    
    public CreateCollectionOptions storageEngineOptions(final Bson storageEngineOptions) {
        this.storageEngineOptions = storageEngineOptions;
        return this;
    }
    
    public IndexOptionDefaults getIndexOptionDefaults() {
        return this.indexOptionDefaults;
    }
    
    public CreateCollectionOptions indexOptionDefaults(final IndexOptionDefaults indexOptionDefaults) {
        this.indexOptionDefaults = indexOptionDefaults;
        return this;
    }
    
    public ValidationOptions getValidationOptions() {
        return this.validationOptions;
    }
    
    public CreateCollectionOptions validationOptions(final ValidationOptions validationOptions) {
        this.validationOptions = Assertions.notNull("validationOptions", validationOptions);
        return this;
    }
    
    public Collation getCollation() {
        return this.collation;
    }
    
    public CreateCollectionOptions collation(final Collation collation) {
        this.collation = collation;
        return this;
    }
}
