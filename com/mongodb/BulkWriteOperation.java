package com.mongodb;

import java.util.*;
import com.mongodb.assertions.*;
import org.bson.types.*;
import org.bson.codecs.*;

public class BulkWriteOperation
{
    private static final String ID_FIELD_NAME = "_id";
    private final boolean ordered;
    private final DBCollection collection;
    private final List<WriteRequest> requests;
    private Boolean bypassDocumentValidation;
    private boolean closed;
    
    BulkWriteOperation(final boolean ordered, final DBCollection collection) {
        this.requests = new ArrayList<WriteRequest>();
        this.ordered = ordered;
        this.collection = collection;
    }
    
    public boolean isOrdered() {
        return this.ordered;
    }
    
    public Boolean getBypassDocumentValidation() {
        return this.bypassDocumentValidation;
    }
    
    public void setBypassDocumentValidation(final Boolean bypassDocumentValidation) {
        this.bypassDocumentValidation = bypassDocumentValidation;
    }
    
    public void insert(final DBObject document) {
        Assertions.isTrue("already executed", !this.closed);
        if (document.get("_id") == null) {
            document.put("_id", new ObjectId());
        }
        this.addRequest(new InsertRequest(document, this.collection.getObjectCodec()));
    }
    
    public BulkWriteRequestBuilder find(final DBObject query) {
        Assertions.isTrue("already executed", !this.closed);
        return new BulkWriteRequestBuilder(this, query, this.collection.getDefaultDBObjectCodec(), this.collection.getObjectCodec());
    }
    
    public BulkWriteResult execute() {
        Assertions.isTrue("already executed", !this.closed);
        this.closed = true;
        return this.collection.executeBulkWriteOperation(this.ordered, this.bypassDocumentValidation, this.requests);
    }
    
    public BulkWriteResult execute(final WriteConcern writeConcern) {
        Assertions.isTrue("already executed", !this.closed);
        this.closed = true;
        return this.collection.executeBulkWriteOperation(this.ordered, this.bypassDocumentValidation, this.requests, writeConcern);
    }
    
    void addRequest(final WriteRequest request) {
        Assertions.isTrue("already executed", !this.closed);
        this.requests.add(request);
    }
}
