package com.mongodb;

import com.mongodb.assertions.*;

public class BulkWriteError
{
    private final int index;
    private final int code;
    private final String message;
    private final DBObject details;
    
    public BulkWriteError(final int code, final String message, final DBObject details, final int index) {
        this.code = code;
        this.message = Assertions.notNull("message", message);
        this.details = Assertions.notNull("details", details);
        this.index = index;
    }
    
    public int getCode() {
        return this.code;
    }
    
    public String getMessage() {
        return this.message;
    }
    
    public DBObject getDetails() {
        return this.details;
    }
    
    public int getIndex() {
        return this.index;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final BulkWriteError that = (BulkWriteError)o;
        return this.code == that.code && this.index == that.index && this.details.equals(that.details) && this.message.equals(that.message);
    }
    
    @Override
    public int hashCode() {
        int result = this.index;
        result = 31 * result + this.code;
        result = 31 * result + this.message.hashCode();
        result = 31 * result + this.details.hashCode();
        return result;
    }
    
    @Override
    public String toString() {
        return "BulkWriteError{index=" + this.index + ", code=" + this.code + ", message='" + this.message + '\'' + ", details=" + this.details + '}';
    }
}
