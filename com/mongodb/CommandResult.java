package com.mongodb;

import com.mongodb.assertions.*;
import org.bson.*;

public class CommandResult extends BasicDBObject
{
    private static final long serialVersionUID = 5907909423864204060L;
    private final BsonDocument response;
    private final ServerAddress address;
    
    CommandResult(final BsonDocument response) {
        this(response, null);
    }
    
    CommandResult(final BsonDocument response, final ServerAddress address) {
        this.address = address;
        this.response = Assertions.notNull("response", response);
        this.putAll(DBObjects.toDBObject(response));
    }
    
    public boolean ok() {
        final Object okValue = this.get("ok");
        if (okValue instanceof Boolean) {
            return (boolean)okValue;
        }
        return okValue instanceof Number && ((Number)okValue).intValue() == 1;
    }
    
    public String getErrorMessage() {
        final Object foo = this.get("errmsg");
        if (foo == null) {
            return null;
        }
        return foo.toString();
    }
    
    public MongoException getException() {
        if (!this.ok()) {
            return new MongoCommandException(this.response, this.address);
        }
        return null;
    }
    
    public void throwOnError() {
        if (!this.ok()) {
            throw this.getException();
        }
    }
}
