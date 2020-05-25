package com.mongodb.client.model;

import org.bson.conversions.*;
import org.bson.*;
import com.mongodb.assertions.*;
import java.util.*;
import org.bson.codecs.configuration.*;

public final class Projections
{
    private Projections() {
    }
    
    public static <TExpression> Bson computed(final String fieldName, final TExpression expression) {
        return new SimpleExpression<Object>(fieldName, expression);
    }
    
    public static Bson include(final String... fieldNames) {
        return include(Arrays.asList(fieldNames));
    }
    
    public static Bson include(final List<String> fieldNames) {
        return combine(fieldNames, new BsonInt32(1));
    }
    
    public static Bson exclude(final String... fieldNames) {
        return exclude(Arrays.asList(fieldNames));
    }
    
    public static Bson exclude(final List<String> fieldNames) {
        return combine(fieldNames, new BsonInt32(0));
    }
    
    public static Bson excludeId() {
        return new BsonDocument("_id", new BsonInt32(0));
    }
    
    public static Bson elemMatch(final String fieldName) {
        return new BsonDocument(fieldName + ".$", new BsonInt32(1));
    }
    
    public static Bson elemMatch(final String fieldName, final Bson filter) {
        return new ElemMatchFilterProjection(fieldName, filter);
    }
    
    public static Bson metaTextScore(final String fieldName) {
        return new BsonDocument(fieldName, new BsonDocument("$meta", new BsonString("textScore")));
    }
    
    public static Bson slice(final String fieldName, final int limit) {
        return new BsonDocument(fieldName, new BsonDocument("$slice", new BsonInt32(limit)));
    }
    
    public static Bson slice(final String fieldName, final int skip, final int limit) {
        return new BsonDocument(fieldName, new BsonDocument("$slice", new BsonArray(Arrays.asList(new BsonInt32(skip), new BsonInt32(limit)))));
    }
    
    public static Bson fields(final Bson... projections) {
        return fields(Arrays.asList(projections));
    }
    
    public static Bson fields(final List<Bson> projections) {
        Assertions.notNull("sorts", projections);
        return new FieldsProjection(projections);
    }
    
    private static Bson combine(final List<String> fieldNames, final BsonValue value) {
        final BsonDocument document = new BsonDocument();
        for (final String fieldName : fieldNames) {
            document.remove(fieldName);
            document.append(fieldName, value);
        }
        return document;
    }
    
    private static class FieldsProjection implements Bson
    {
        private final List<Bson> projections;
        
        public FieldsProjection(final List<Bson> projections) {
            this.projections = projections;
        }
        
        @Override
        public <TDocument> BsonDocument toBsonDocument(final Class<TDocument> documentClass, final CodecRegistry codecRegistry) {
            final BsonDocument combinedDocument = new BsonDocument();
            for (final Bson sort : this.projections) {
                final BsonDocument sortDocument = sort.toBsonDocument(documentClass, codecRegistry);
                for (final String key : sortDocument.keySet()) {
                    combinedDocument.remove(key);
                    combinedDocument.append(key, sortDocument.get(key));
                }
            }
            return combinedDocument;
        }
        
        @Override
        public String toString() {
            return "Projections{projections=" + this.projections + '}';
        }
    }
    
    private static class ElemMatchFilterProjection implements Bson
    {
        private final String fieldName;
        private final Bson filter;
        
        public ElemMatchFilterProjection(final String fieldName, final Bson filter) {
            this.fieldName = fieldName;
            this.filter = filter;
        }
        
        @Override
        public <TDocument> BsonDocument toBsonDocument(final Class<TDocument> documentClass, final CodecRegistry codecRegistry) {
            return new BsonDocument(this.fieldName, new BsonDocument("$elemMatch", this.filter.toBsonDocument(documentClass, codecRegistry)));
        }
        
        @Override
        public String toString() {
            return "ElemMatch Projection{fieldName='" + this.fieldName + '\'' + ", filter=" + this.filter + '}';
        }
    }
}
