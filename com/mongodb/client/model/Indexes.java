package com.mongodb.client.model;

import org.bson.conversions.*;
import com.mongodb.assertions.*;
import org.bson.*;
import org.bson.codecs.configuration.*;
import java.util.*;

public final class Indexes
{
    private Indexes() {
    }
    
    public static Bson ascending(final String... fieldNames) {
        return ascending(Arrays.asList(fieldNames));
    }
    
    public static Bson ascending(final List<String> fieldNames) {
        Assertions.notNull("fieldNames", fieldNames);
        return compoundIndex(fieldNames, new BsonInt32(1));
    }
    
    public static Bson descending(final String... fieldNames) {
        return descending(Arrays.asList(fieldNames));
    }
    
    public static Bson descending(final List<String> fieldNames) {
        Assertions.notNull("fieldNames", fieldNames);
        return compoundIndex(fieldNames, new BsonInt32(-1));
    }
    
    public static Bson geo2dsphere(final String... fieldNames) {
        return geo2dsphere(Arrays.asList(fieldNames));
    }
    
    public static Bson geo2dsphere(final List<String> fieldNames) {
        Assertions.notNull("fieldNames", fieldNames);
        return compoundIndex(fieldNames, new BsonString("2dsphere"));
    }
    
    public static Bson geo2d(final String fieldName) {
        Assertions.notNull("fieldName", fieldName);
        return new BsonDocument(fieldName, new BsonString("2d"));
    }
    
    public static Bson geoHaystack(final String fieldName, final Bson additional) {
        Assertions.notNull("fieldName", fieldName);
        return compoundIndex(new BsonDocument(fieldName, new BsonString("geoHaystack")), additional);
    }
    
    public static Bson text(final String fieldName) {
        Assertions.notNull("fieldName", fieldName);
        return new BsonDocument(fieldName, new BsonString("text"));
    }
    
    public static Bson hashed(final String fieldName) {
        Assertions.notNull("fieldName", fieldName);
        return new BsonDocument(fieldName, new BsonString("hashed"));
    }
    
    public static Bson compoundIndex(final Bson... indexes) {
        return compoundIndex(Arrays.asList(indexes));
    }
    
    public static Bson compoundIndex(final List<Bson> indexes) {
        Assertions.notNull("indexes", indexes);
        return new Bson() {
            @Override
            public <TDocument> BsonDocument toBsonDocument(final Class<TDocument> documentClass, final CodecRegistry codecRegistry) {
                final BsonDocument compoundIndex = new BsonDocument();
                for (final Bson index : indexes) {
                    final BsonDocument indexDocument = index.toBsonDocument(documentClass, codecRegistry);
                    for (final String key : indexDocument.keySet()) {
                        compoundIndex.append(key, indexDocument.get(key));
                    }
                }
                return compoundIndex;
            }
        };
    }
    
    private static Bson compoundIndex(final List<String> fieldNames, final BsonValue value) {
        final BsonDocument document = new BsonDocument();
        for (final String fieldName : fieldNames) {
            document.append(fieldName, value);
        }
        return document;
    }
}
