package com.mongodb;

import org.bson.codecs.*;
import com.mongodb.client.model.*;
import com.mongodb.bulk.*;
import org.bson.*;

class ReplaceRequest extends WriteRequest
{
    private final DBObject query;
    private final DBObject document;
    private final boolean upsert;
    private final Encoder<DBObject> codec;
    private final Encoder<DBObject> replacementCodec;
    private final Collation collation;
    
    public ReplaceRequest(final DBObject query, final DBObject document, final boolean upsert, final Encoder<DBObject> codec, final Encoder<DBObject> replacementCodec, final Collation collation) {
        this.query = query;
        this.document = document;
        this.upsert = upsert;
        this.codec = codec;
        this.replacementCodec = replacementCodec;
        this.collation = collation;
    }
    
    public DBObject getQuery() {
        return this.query;
    }
    
    public DBObject getDocument() {
        return this.document;
    }
    
    public boolean isUpsert() {
        return this.upsert;
    }
    
    public Collation getCollation() {
        return this.collation;
    }
    
    @Override
    com.mongodb.bulk.WriteRequest toNew() {
        return new UpdateRequest(new BsonDocumentWrapper<Object>(this.query, this.codec), new BsonDocumentWrapper<Object>(this.document, this.replacementCodec), com.mongodb.bulk.WriteRequest.Type.REPLACE).upsert(this.isUpsert()).collation(this.getCollation());
    }
}
