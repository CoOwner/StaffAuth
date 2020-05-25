package com.mongodb;

public class MongoNotPrimaryException extends MongoServerException
{
    private static final long serialVersionUID = 694876345217027108L;
    
    public MongoNotPrimaryException(final ServerAddress serverAddress) {
        super("The server is not the primary and did not execute the operation", serverAddress);
    }
}
