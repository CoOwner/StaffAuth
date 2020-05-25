package com.mongodb;

import org.bson.*;
import com.mongodb.assertions.*;

public class WriteError
{
    private final int code;
    private final String message;
    private final BsonDocument details;
    
    public WriteError(final int code, final String message, final BsonDocument details) {
        this.code = code;
        this.message = Assertions.notNull("message", message);
        this.details = Assertions.notNull("details", details);
    }
    
    public WriteError(final WriteError writeError) {
        this.code = writeError.code;
        this.message = writeError.message;
        this.details = writeError.details;
    }
    
    public ErrorCategory getCategory() {
        return ErrorCategory.fromErrorCode(this.code);
    }
    
    public int getCode() {
        return this.code;
    }
    
    public String getMessage() {
        return this.message;
    }
    
    public BsonDocument getDetails() {
        return this.details;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final WriteError that = (WriteError)o;
        return this.code == that.code && this.details.equals(that.details) && this.message.equals(that.message);
    }
    
    @Override
    public int hashCode() {
        int result = this.code;
        result = 31 * result + this.message.hashCode();
        result = 31 * result + this.details.hashCode();
        return result;
    }
    
    @Override
    public String toString() {
        return "WriteError{code=" + this.code + ", message='" + this.message + '\'' + ", details=" + this.details + '}';
    }
}
