package com.mongodb.client.model;

public final class BulkWriteOptions
{
    private boolean ordered;
    private Boolean bypassDocumentValidation;
    
    public BulkWriteOptions() {
        this.ordered = true;
    }
    
    public boolean isOrdered() {
        return this.ordered;
    }
    
    public BulkWriteOptions ordered(final boolean ordered) {
        this.ordered = ordered;
        return this;
    }
    
    public Boolean getBypassDocumentValidation() {
        return this.bypassDocumentValidation;
    }
    
    public BulkWriteOptions bypassDocumentValidation(final Boolean bypassDocumentValidation) {
        this.bypassDocumentValidation = bypassDocumentValidation;
        return this;
    }
}
