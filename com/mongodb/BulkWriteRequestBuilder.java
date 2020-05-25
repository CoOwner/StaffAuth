package com.mongodb;

import org.bson.codecs.*;
import com.mongodb.client.model.*;

public class BulkWriteRequestBuilder
{
    private final BulkWriteOperation bulkWriteOperation;
    private final DBObject query;
    private final Encoder<DBObject> codec;
    private final Encoder<DBObject> replacementCodec;
    private Collation collation;
    
    BulkWriteRequestBuilder(final BulkWriteOperation bulkWriteOperation, final DBObject query, final Encoder<DBObject> queryCodec, final Encoder<DBObject> replacementCodec) {
        this.bulkWriteOperation = bulkWriteOperation;
        this.query = query;
        this.codec = queryCodec;
        this.replacementCodec = replacementCodec;
    }
    
    public Collation getCollation() {
        return this.collation;
    }
    
    public BulkWriteRequestBuilder collation(final Collation collation) {
        this.collation = collation;
        return this;
    }
    
    public void remove() {
        this.bulkWriteOperation.addRequest(new RemoveRequest(this.query, true, this.codec, this.collation));
    }
    
    public void removeOne() {
        this.bulkWriteOperation.addRequest(new RemoveRequest(this.query, false, this.codec, this.collation));
    }
    
    public void replaceOne(final DBObject document) {
        new BulkUpdateRequestBuilder(this.bulkWriteOperation, this.query, false, this.codec, this.replacementCodec, this.collation).replaceOne(document);
    }
    
    public void update(final DBObject update) {
        new BulkUpdateRequestBuilder(this.bulkWriteOperation, this.query, false, this.codec, this.replacementCodec, this.collation).update(update);
    }
    
    public void updateOne(final DBObject update) {
        new BulkUpdateRequestBuilder(this.bulkWriteOperation, this.query, false, this.codec, this.replacementCodec, this.collation).updateOne(update);
    }
    
    public BulkUpdateRequestBuilder upsert() {
        return new BulkUpdateRequestBuilder(this.bulkWriteOperation, this.query, true, this.codec, this.replacementCodec, this.collation);
    }
}
