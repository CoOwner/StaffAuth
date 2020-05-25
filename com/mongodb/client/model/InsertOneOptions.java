package com.mongodb.client.model;

public final class InsertOneOptions
{
    private Boolean bypassDocumentValidation;
    
    public Boolean getBypassDocumentValidation() {
        return this.bypassDocumentValidation;
    }
    
    public InsertOneOptions bypassDocumentValidation(final Boolean bypassDocumentValidation) {
        this.bypassDocumentValidation = bypassDocumentValidation;
        return this;
    }
}
