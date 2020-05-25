package com.mongodb;

import org.bson.codecs.*;
import org.bson.*;

final class DBObjects
{
    public static DBObject toDBObject(final BsonDocument document) {
        return MongoClient.getDefaultCodecRegistry().get(DBObject.class).decode(new BsonDocumentReader(document), DecoderContext.builder().build());
    }
    
    private DBObjects() {
    }
}
