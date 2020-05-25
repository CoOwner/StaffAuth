package com.mongodb;

public class MongoSocketReadException extends MongoSocketException
{
    private static final long serialVersionUID = -1142547119966956531L;
    
    public MongoSocketReadException(final String message, final ServerAddress address) {
        super(message, address);
    }
    
    public MongoSocketReadException(final String message, final ServerAddress address, final Throwable cause) {
        super(message, address, cause);
    }
}
