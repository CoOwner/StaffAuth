package com.mongodb.bulk;

import com.mongodb.*;
import org.bson.*;

public class BulkWriteError extends WriteError
{
    private final int index;
    
    public BulkWriteError(final int code, final String message, final BsonDocument details, final int index) {
        super(code, message, details);
        this.index = index;
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
        return this.index == that.index && super.equals(that);
    }
    
    @Override
    public int hashCode() {
        int result = this.index;
        result = 31 * super.hashCode();
        return result;
    }
    
    @Override
    public String toString() {
        return "BulkWriteError{index=" + this.index + ", code=" + this.getCode() + ", message='" + this.getMessage() + '\'' + ", details=" + this.getDetails() + '}';
    }
}
