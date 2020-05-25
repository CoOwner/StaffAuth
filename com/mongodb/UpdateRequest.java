package com.mongodb;

import org.bson.codecs.*;
import com.mongodb.client.model.*;
import org.bson.*;

class UpdateRequest extends WriteRequest
{
    private final DBObject query;
    private final DBObject update;
    private final boolean multi;
    private final boolean upsert;
    private final Encoder<DBObject> codec;
    private final Collation collation;
    
    public UpdateRequest(final DBObject query, final DBObject update, final boolean multi, final boolean upsert, final Encoder<DBObject> codec, final Collation collation) {
        this.query = query;
        this.update = update;
        this.multi = multi;
        this.upsert = upsert;
        this.codec = codec;
        this.collation = collation;
    }
    
    public DBObject getQuery() {
        return this.query;
    }
    
    public DBObject getUpdate() {
        return this.update;
    }
    
    public boolean isUpsert() {
        return this.upsert;
    }
    
    public boolean isMulti() {
        return this.multi;
    }
    
    public Collation getCollation() {
        return this.collation;
    }
    
    @Override
    com.mongodb.bulk.WriteRequest toNew() {
        return new com.mongodb.bulk.UpdateRequest(new BsonDocumentWrapper<Object>(this.query, this.codec), new BsonDocumentWrapper<Object>(this.update, this.codec), com.mongodb.bulk.WriteRequest.Type.UPDATE).upsert(this.isUpsert()).multi(this.isMulti()).collation(this.getCollation());
    }
}
