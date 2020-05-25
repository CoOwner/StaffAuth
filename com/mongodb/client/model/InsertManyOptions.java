package com.mongodb.client.model;

public final class InsertManyOptions
{
    private boolean ordered;
    private Boolean bypassDocumentValidation;
    
    public InsertManyOptions() {
        this.ordered = true;
    }
    
    public boolean isOrdered() {
        return this.ordered;
    }
    
    public InsertManyOptions ordered(final boolean ordered) {
        this.ordered = ordered;
        return this;
    }
    
    public Boolean getBypassDocumentValidation() {
        return this.bypassDocumentValidation;
    }
    
    public InsertManyOptions bypassDocumentValidation(final Boolean bypassDocumentValidation) {
        this.bypassDocumentValidation = bypassDocumentValidation;
        return this;
    }
}
