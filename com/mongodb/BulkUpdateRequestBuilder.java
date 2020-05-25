package com.mongodb;

import org.bson.codecs.*;
import com.mongodb.client.model.*;

public class BulkUpdateRequestBuilder
{
    private final BulkWriteOperation bulkWriteOperation;
    private final DBObject query;
    private final boolean upsert;
    private final Encoder<DBObject> queryCodec;
    private final Encoder<DBObject> replacementCodec;
    private Collation collation;
    
    BulkUpdateRequestBuilder(final BulkWriteOperation bulkWriteOperation, final DBObject query, final boolean upsert, final Encoder<DBObject> queryCodec, final Encoder<DBObject> replacementCodec, final Collation collation) {
        this.bulkWriteOperation = bulkWriteOperation;
        this.query = query;
        this.upsert = upsert;
        this.queryCodec = queryCodec;
        this.replacementCodec = replacementCodec;
        this.collation = collation;
    }
    
    public Collation getCollation() {
        return this.collation;
    }
    
    public BulkUpdateRequestBuilder collation(final Collation collation) {
        this.collation = collation;
        return this;
    }
    
    public void replaceOne(final DBObject document) {
        this.bulkWriteOperation.addRequest(new ReplaceRequest(this.query, document, this.upsert, this.queryCodec, this.replacementCodec, this.collation));
    }
    
    public void update(final DBObject update) {
        this.bulkWriteOperation.addRequest(new UpdateRequest(this.query, update, true, this.upsert, this.queryCodec, this.collation));
    }
    
    public void updateOne(final DBObject update) {
        this.bulkWriteOperation.addRequest(new UpdateRequest(this.query, update, false, this.upsert, this.queryCodec, this.collation));
    }
}
