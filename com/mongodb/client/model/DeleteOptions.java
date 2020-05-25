package com.mongodb.client.model;

public class DeleteOptions
{
    private Collation collation;
    
    public Collation getCollation() {
        return this.collation;
    }
    
    public DeleteOptions collation(final Collation collation) {
        this.collation = collation;
        return this;
    }
}
