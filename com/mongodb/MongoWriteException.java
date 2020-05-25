package com.mongodb;

public class MongoWriteException extends MongoServerException
{
    private static final long serialVersionUID = -1906795074458258147L;
    private final WriteError error;
    
    public MongoWriteException(final WriteError error, final ServerAddress serverAddress) {
        super(error.getCode(), error.getMessage(), serverAddress);
        this.error = error;
    }
    
    public WriteError getError() {
        return this.error;
    }
}
