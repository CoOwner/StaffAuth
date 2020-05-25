package com.mongodb;

public abstract class MongoServerException extends MongoException
{
    private static final long serialVersionUID = -5213859742051776206L;
    private final ServerAddress serverAddress;
    
    public MongoServerException(final String message, final ServerAddress serverAddress) {
        super(message);
        this.serverAddress = serverAddress;
    }
    
    public MongoServerException(final int code, final String message, final ServerAddress serverAddress) {
        super(code, message);
        this.serverAddress = serverAddress;
    }
    
    public ServerAddress getServerAddress() {
        return this.serverAddress;
    }
}
