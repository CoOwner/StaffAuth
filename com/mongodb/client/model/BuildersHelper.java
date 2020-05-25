package com.mongodb.client.model;

import org.bson.codecs.configuration.*;
import org.bson.conversions.*;
import org.bson.codecs.*;
import org.bson.*;

final class BuildersHelper
{
    static <TItem> void encodeValue(final BsonDocumentWriter writer, final TItem value, final CodecRegistry codecRegistry) {
        if (value == null) {
            writer.writeNull();
        }
        else if (value instanceof Bson) {
            codecRegistry.get(BsonDocument.class).encode(writer, ((Bson)value).toBsonDocument(BsonDocument.class, codecRegistry), EncoderContext.builder().build());
        }
        else {
            codecRegistry.get(value.getClass()).encode(writer, value, EncoderContext.builder().build());
        }
    }
    
    private BuildersHelper() {
    }
}
