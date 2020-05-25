package com.mongodb.client.model;

import org.bson.conversions.*;
import com.mongodb.assertions.*;
import org.bson.*;
import java.util.*;
import org.bson.codecs.configuration.*;

public final class Sorts
{
    private Sorts() {
    }
    
    public static Bson ascending(final String... fieldNames) {
        return ascending(Arrays.asList(fieldNames));
    }
    
    public static Bson ascending(final List<String> fieldNames) {
        Assertions.notNull("fieldNames", fieldNames);
        return orderBy(fieldNames, new BsonInt32(1));
    }
    
    public static Bson descending(final String... fieldNames) {
        return descending(Arrays.asList(fieldNames));
    }
    
    public static Bson descending(final List<String> fieldNames) {
        Assertions.notNull("fieldNames", fieldNames);
        return orderBy(fieldNames, new BsonInt32(-1));
    }
    
    public static Bson metaTextScore(final String fieldName) {
        return new BsonDocument(fieldName, new BsonDocument("$meta", new BsonString("textScore")));
    }
    
    public static Bson orderBy(final Bson... sorts) {
        return orderBy(Arrays.asList(sorts));
    }
    
    public static Bson orderBy(final List<Bson> sorts) {
        Assertions.notNull("sorts", sorts);
        return new CompoundSort((List)sorts);
    }
    
    private static Bson orderBy(final List<String> fieldNames, final BsonValue value) {
        final BsonDocument document = new BsonDocument();
        for (final String fieldName : fieldNames) {
            document.append(fieldName, value);
        }
        return document;
    }
    
    private static final class CompoundSort implements Bson
    {
        private final List<Bson> sorts;
        
        private CompoundSort(final List<Bson> sorts) {
            this.sorts = sorts;
        }
        
        @Override
        public <TDocument> BsonDocument toBsonDocument(final Class<TDocument> documentClass, final CodecRegistry codecRegistry) {
            final BsonDocument combinedDocument = new BsonDocument();
            for (final Bson sort : this.sorts) {
                final BsonDocument sortDocument = sort.toBsonDocument(documentClass, codecRegistry);
                for (final String key : sortDocument.keySet()) {
                    combinedDocument.append(key, sortDocument.get(key));
                }
            }
            return combinedDocument;
        }
        
        @Override
        public String toString() {
            return "Compound Sort{sorts=" + this.sorts + '}';
        }
    }
}
