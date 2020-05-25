package com.mongodb;

public class MongoQueryException extends MongoServerException
{
    private static final long serialVersionUID = -5113350133297015801L;
    private final String errorMessage;
    
    public MongoQueryException(final ServerAddress address, final int errorCode, final String errorMessage) {
        super(errorCode, String.format("Query failed with error code %d and error message '%s' on server %s", errorCode, errorMessage, address), address);
        this.errorMessage = errorMessage;
    }
    
    public int getErrorCode() {
        return this.getCode();
    }
    
    public String getErrorMessage() {
        return this.errorMessage;
    }
}
