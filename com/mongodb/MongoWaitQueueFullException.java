package com.mongodb;

public class MongoWaitQueueFullException extends MongoClientException
{
    private static final long serialVersionUID = 1482094507852255793L;
    
    public MongoWaitQueueFullException(final String message) {
        super(message);
    }
}
