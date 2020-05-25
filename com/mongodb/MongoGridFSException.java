package com.mongodb;

public class MongoGridFSException extends MongoException
{
    private static final long serialVersionUID = -3894346172927543978L;
    
    public MongoGridFSException(final String message) {
        super(message);
    }
    
    public MongoGridFSException(final String message, final Throwable t) {
        super(message, t);
    }
}
