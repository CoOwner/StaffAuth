package com.mongodb;

import org.bson.codecs.*;
import com.mongodb.client.model.*;
import com.mongodb.bulk.*;
import org.bson.*;

class RemoveRequest extends WriteRequest
{
    private final DBObject query;
    private final boolean multi;
    private final Encoder<DBObject> codec;
    private final Collation collation;
    
    public RemoveRequest(final DBObject query, final boolean multi, final Encoder<DBObject> codec, final Collation collation) {
        this.query = query;
        this.multi = multi;
        this.codec = codec;
        this.collation = collation;
    }
    
    public DBObject getQuery() {
        return this.query;
    }
    
    public boolean isMulti() {
        return this.multi;
    }
    
    @Override
    com.mongodb.bulk.WriteRequest toNew() {
        return new DeleteRequest(new BsonDocumentWrapper<Object>(this.query, this.codec)).multi(this.isMulti()).collation(this.collation);
    }
}
