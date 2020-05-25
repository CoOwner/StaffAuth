package com.mongodb.bulk;

import org.bson.*;
import com.mongodb.assertions.*;

public final class InsertRequest extends WriteRequest
{
    private final BsonDocument document;
    
    public InsertRequest(final BsonDocument document) {
        this.document = Assertions.notNull("document", document);
    }
    
    public BsonDocument getDocument() {
        return this.document;
    }
    
    @Override
    public Type getType() {
        return Type.INSERT;
    }
}
