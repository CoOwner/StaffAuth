package com.mongodb;

public class MongoClientException extends MongoException
{
    private static final long serialVersionUID = -5127414714432646066L;
    
    public MongoClientException(final String message) {
        super(message);
    }
    
    public MongoClientException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
