package com.mongodb.bulk;

import org.bson.*;
import com.mongodb.client.model.*;
import com.mongodb.assertions.*;

public final class DeleteRequest extends WriteRequest
{
    private final BsonDocument filter;
    private boolean isMulti;
    private Collation collation;
    
    public DeleteRequest(final BsonDocument filter) {
        this.isMulti = true;
        this.filter = Assertions.notNull("filter", filter);
    }
    
    public BsonDocument getFilter() {
        return this.filter;
    }
    
    public DeleteRequest multi(final boolean isMulti) {
        this.isMulti = isMulti;
        return this;
    }
    
    public boolean isMulti() {
        return this.isMulti;
    }
    
    public Collation getCollation() {
        return this.collation;
    }
    
    public DeleteRequest collation(final Collation collation) {
        this.collation = collation;
        return this;
    }
    
    @Override
    public Type getType() {
        return Type.DELETE;
    }
}
