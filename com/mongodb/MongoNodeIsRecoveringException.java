package com.mongodb;

public class MongoNodeIsRecoveringException extends MongoServerException
{
    private static final long serialVersionUID = 6062524147327071635L;
    
    public MongoNodeIsRecoveringException(final ServerAddress serverAddress) {
        super("The server is in recovery mode and did not execute the operation", serverAddress);
    }
}
