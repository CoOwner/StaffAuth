package com.mongodb.client.model;

public class DBCreateViewOptions
{
    private Collation collation;
    
    public Collation getCollation() {
        return this.collation;
    }
    
    public DBCreateViewOptions collation(final Collation collation) {
        this.collation = collation;
        return this;
    }
}
