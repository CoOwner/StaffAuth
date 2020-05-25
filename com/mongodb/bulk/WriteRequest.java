package com.mongodb.bulk;

public abstract class WriteRequest
{
    WriteRequest() {
    }
    
    public abstract Type getType();
    
    public enum Type
    {
        INSERT, 
        UPDATE, 
        REPLACE, 
        DELETE;
    }
}
