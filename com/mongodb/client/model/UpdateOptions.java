package com.mongodb.client.model;

public class UpdateOptions
{
    private boolean upsert;
    private Boolean bypassDocumentValidation;
    private Collation collation;
    
    public boolean isUpsert() {
        return this.upsert;
    }
    
    public UpdateOptions upsert(final boolean upsert) {
        this.upsert = upsert;
        return this;
    }
    
    public Boolean getBypassDocumentValidation() {
        return this.bypassDocumentValidation;
    }
    
    public UpdateOptions bypassDocumentValidation(final Boolean bypassDocumentValidation) {
        this.bypassDocumentValidation = bypassDocumentValidation;
        return this;
    }
    
    public Collation getCollation() {
        return this.collation;
    }
    
    public UpdateOptions collation(final Collation collation) {
        this.collation = collation;
        return this;
    }
}
