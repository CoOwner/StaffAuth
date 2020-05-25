package com.mongodb;

public class MongoSocketException extends MongoException
{
    private static final long serialVersionUID = -4415279469780082174L;
    private final ServerAddress serverAddress;
    
    MongoSocketException(final String msg, final ServerAddress serverAddress, final Throwable e) {
        super(-2, msg, e);
        this.serverAddress = serverAddress;
    }
    
    public MongoSocketException(final String message, final ServerAddress serverAddress) {
        super(-2, message);
        this.serverAddress = serverAddress;
    }
    
    public ServerAddress getServerAddress() {
        return this.serverAddress;
    }
}
