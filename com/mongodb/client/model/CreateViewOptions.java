package com.mongodb.client.model;

public class CreateViewOptions
{
    private Collation collation;
    
    public Collation getCollation() {
        return this.collation;
    }
    
    public CreateViewOptions collation(final Collation collation) {
        this.collation = collation;
        return this;
    }
}
