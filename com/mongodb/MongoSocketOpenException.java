package com.mongodb;

public class MongoSocketOpenException extends MongoSocketException
{
    private static final long serialVersionUID = 4176754100200191238L;
    
    public MongoSocketOpenException(final String message, final ServerAddress address, final Throwable cause) {
        super(message, address, cause);
    }
}
