package com.mongodb;

public class MongoInterruptedException extends MongoException
{
    private static final long serialVersionUID = -4110417867718417860L;
    
    public MongoInterruptedException(final String message, final Exception e) {
        super(message, e);
    }
}
