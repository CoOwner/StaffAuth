package com.mongodb;

import org.bson.codecs.*;
import org.bson.*;

class InsertRequest extends WriteRequest
{
    private final DBObject document;
    private final Encoder<DBObject> codec;
    
    public InsertRequest(final DBObject document, final Encoder<DBObject> codec) {
        this.document = document;
        this.codec = codec;
    }
    
    public DBObject getDocument() {
        return this.document;
    }
    
    @Override
    com.mongodb.bulk.WriteRequest toNew() {
        return new com.mongodb.bulk.InsertRequest(new BsonDocumentWrapper<Object>(this.document, this.codec));
    }
}
