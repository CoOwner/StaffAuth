package com.mongodb;

public class MongoException extends RuntimeException
{
    private static final long serialVersionUID = -4415279469780082174L;
    private final int code;
    
    public static MongoException fromThrowable(final Throwable t) {
        if (t == null) {
            return null;
        }
        if (t instanceof MongoException) {
            return (MongoException)t;
        }
        return new MongoException(t.getMessage(), t);
    }
    
    public MongoException(final String msg) {
        super(msg);
        this.code = -3;
    }
    
    public MongoException(final int code, final String msg) {
        super(msg);
        this.code = code;
    }
    
    public MongoException(final String msg, final Throwable t) {
        super(msg, t);
        this.code = -4;
    }
    
    public MongoException(final int code, final String msg, final Throwable t) {
        super(msg, t);
        this.code = code;
    }
    
    public int getCode() {
        return this.code;
    }
}
