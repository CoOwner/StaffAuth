package com.mongodb;

public final class InsertOptions
{
    private WriteConcern writeConcern;
    private boolean continueOnError;
    private DBEncoder dbEncoder;
    private Boolean bypassDocumentValidation;
    
    public InsertOptions writeConcern(final WriteConcern writeConcern) {
        this.writeConcern = writeConcern;
        return this;
    }
    
    public InsertOptions continueOnError(final boolean continueOnError) {
        this.continueOnError = continueOnError;
        return this;
    }
    
    public InsertOptions dbEncoder(final DBEncoder dbEncoder) {
        this.dbEncoder = dbEncoder;
        return this;
    }
    
    public WriteConcern getWriteConcern() {
        return this.writeConcern;
    }
    
    public boolean isContinueOnError() {
        return this.continueOnError;
    }
    
    public DBEncoder getDbEncoder() {
        return this.dbEncoder;
    }
    
    public Boolean getBypassDocumentValidation() {
        return this.bypassDocumentValidation;
    }
    
    public InsertOptions bypassDocumentValidation(final Boolean bypassDocumentValidation) {
        this.bypassDocumentValidation = bypassDocumentValidation;
        return this;
    }
}
