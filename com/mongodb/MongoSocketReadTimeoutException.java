package com.mongodb;

public class MongoSocketReadTimeoutException extends MongoSocketException
{
    private static final long serialVersionUID = -7237059971254608960L;
    
    public MongoSocketReadTimeoutException(final String message, final ServerAddress address, final Throwable cause) {
        super(message, address, cause);
    }
}
