package com.mongodb.bulk;

import org.bson.*;
import com.mongodb.client.model.*;
import com.mongodb.assertions.*;

public final class UpdateRequest extends WriteRequest
{
    private final BsonDocument update;
    private final Type updateType;
    private final BsonDocument filter;
    private boolean isMulti;
    private boolean isUpsert;
    private Collation collation;
    
    public UpdateRequest(final BsonDocument filter, final BsonDocument update, final Type updateType) {
        this.isMulti = true;
        this.isUpsert = false;
        if (updateType != Type.UPDATE && updateType != Type.REPLACE) {
            throw new IllegalArgumentException("Update type must be UPDATE or REPLACE");
        }
        this.filter = Assertions.notNull("filter", filter);
        this.update = Assertions.notNull("update", update);
        this.isMulti = ((this.updateType = updateType) == Type.UPDATE);
    }
    
    @Override
    public Type getType() {
        return this.updateType;
    }
    
    public BsonDocument getFilter() {
        return this.filter;
    }
    
    public BsonDocument getUpdate() {
        return this.update;
    }
    
    public boolean isMulti() {
        return this.isMulti;
    }
    
    public UpdateRequest multi(final boolean isMulti) {
        if (isMulti && this.updateType == Type.REPLACE) {
            throw new IllegalArgumentException("Replacements can not be multi");
        }
        this.isMulti = isMulti;
        return this;
    }
    
    public boolean isUpsert() {
        return this.isUpsert;
    }
    
    public UpdateRequest upsert(final boolean isUpsert) {
        this.isUpsert = isUpsert;
        return this;
    }
    
    public Collation getCollation() {
        return this.collation;
    }
    
    public UpdateRequest collation(final Collation collation) {
        this.collation = collation;
        return this;
    }
}
