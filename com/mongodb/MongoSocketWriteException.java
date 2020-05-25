package com.mongodb;

public class MongoSocketWriteException extends MongoSocketException
{
    private static final long serialVersionUID = 5088061954415484493L;
    
    public MongoSocketWriteException(final String message, final ServerAddress address, final Throwable cause) {
        super(message, address, cause);
    }
}
