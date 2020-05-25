package com.mongodb.client.model;

import org.bson.conversions.*;
import com.mongodb.assertions.*;
import org.bson.codecs.configuration.*;
import org.bson.*;
import java.util.*;

public final class Updates
{
    public static Bson combine(final Bson... updates) {
        return combine(Arrays.asList(updates));
    }
    
    public static Bson combine(final List<Bson> updates) {
        Assertions.notNull("updates", updates);
        return new CompositeUpdate(updates);
    }
    
    public static <TItem> Bson set(final String fieldName, final TItem value) {
        return new SimpleUpdate<Object>(fieldName, value, "$set");
    }
    
    public static Bson unset(final String fieldName) {
        return new SimpleUpdate<Object>(fieldName, "", "$unset");
    }
    
    public static <TItem> Bson setOnInsert(final String fieldName, final TItem value) {
        return new SimpleUpdate<Object>(fieldName, value, "$setOnInsert");
    }
    
    public static Bson rename(final String fieldName, final String newFieldName) {
        Assertions.notNull("newFieldName", newFieldName);
        return new SimpleUpdate<Object>(fieldName, newFieldName, "$rename");
    }
    
    public static Bson inc(final String fieldName, final Number number) {
        Assertions.notNull("number", number);
        return new SimpleUpdate<Object>(fieldName, number, "$inc");
    }
    
    public static Bson mul(final String fieldName, final Number number) {
        Assertions.notNull("number", number);
        return new SimpleUpdate<Object>(fieldName, number, "$mul");
    }
    
    public static <TItem> Bson min(final String fieldName, final TItem value) {
        return new SimpleUpdate<Object>(fieldName, value, "$min");
    }
    
    public static <TItem> Bson max(final String fieldName, final TItem value) {
        return new SimpleUpdate<Object>(fieldName, value, "$max");
    }
    
    public static Bson currentDate(final String fieldName) {
        return new SimpleUpdate<Object>(fieldName, (Object)true, "$currentDate");
    }
    
    public static Bson currentTimestamp(final String fieldName) {
        return new SimpleUpdate<Object>(fieldName, new BsonDocument("$type", new BsonString("timestamp")), "$currentDate");
    }
    
    public static <TItem> Bson addToSet(final String fieldName, final TItem value) {
        return new SimpleUpdate<Object>(fieldName, value, "$addToSet");
    }
    
    public static <TItem> Bson addEachToSet(final String fieldName, final List<TItem> values) {
        return new WithEachUpdate<Object>(fieldName, values, "$addToSet");
    }
    
    public static <TItem> Bson push(final String fieldName, final TItem value) {
        return new SimpleUpdate<Object>(fieldName, value, "$push");
    }
    
    public static <TItem> Bson pushEach(final String fieldName, final List<TItem> values) {
        return new PushUpdate<Object>(fieldName, values, new PushOptions());
    }
    
    public static <TItem> Bson pushEach(final String fieldName, final List<TItem> values, final PushOptions options) {
        return new PushUpdate<Object>(fieldName, values, options);
    }
    
    public static <TItem> Bson pull(final String fieldName, final TItem value) {
        return new SimpleUpdate<Object>(fieldName, value, "$pull");
    }
    
    public static Bson pullByFilter(final Bson filter) {
        return new Bson() {
            @Override
            public <TDocument> BsonDocument toBsonDocument(final Class<TDocument> tDocumentClass, final CodecRegistry codecRegistry) {
                final BsonDocumentWriter writer = new BsonDocumentWriter(new BsonDocument());
                writer.writeStartDocument();
                writer.writeName("$pull");
                BuildersHelper.encodeValue(writer, filter, codecRegistry);
                writer.writeEndDocument();
                return writer.getDocument();
            }
        };
    }
    
    public static <TItem> Bson pullAll(final String fieldName, final List<TItem> values) {
        return new PullAllUpdate<Object>(fieldName, values);
    }
    
    public static Bson popFirst(final String fieldName) {
        return new SimpleUpdate<Object>(fieldName, (Object)(-1), "$pop");
    }
    
    public static Bson popLast(final String fieldName) {
        return new SimpleUpdate<Object>(fieldName, (Object)1, "$pop");
    }
    
    public static Bson bitwiseAnd(final String fieldName, final int value) {
        return createBitUpdateDocument(fieldName, "and", value);
    }
    
    public static Bson bitwiseAnd(final String fieldName, final long value) {
        return createBitUpdateDocument(fieldName, "and", value);
    }
    
    public static Bson bitwiseOr(final String fieldName, final int value) {
        return createBitUpdateDocument(fieldName, "or", value);
    }
    
    public static Bson bitwiseOr(final String fieldName, final long value) {
        return createBitUpdateDocument(fieldName, "or", value);
    }
    
    public static Bson bitwiseXor(final String fieldName, final int value) {
        return createBitUpdateDocument(fieldName, "xor", value);
    }
    
    public static Bson bitwiseXor(final String fieldName, final long value) {
        return createBitUpdateDocument(fieldName, "xor", value);
    }
    
    private static Bson createBitUpdateDocument(final String fieldName, final String bitwiseOperator, final int value) {
        return createBitUpdateDocument(fieldName, bitwiseOperator, new BsonInt32(value));
    }
    
    private static Bson createBitUpdateDocument(final String fieldName, final String bitwiseOperator, final long value) {
        return createBitUpdateDocument(fieldName, bitwiseOperator, new BsonInt64(value));
    }
    
    private static Bson createBitUpdateDocument(final String fieldName, final String bitwiseOperator, final BsonValue value) {
        return new BsonDocument("$bit", new BsonDocument(fieldName, new BsonDocument(bitwiseOperator, value)));
    }
    
    private Updates() {
    }
    
    private static class SimpleUpdate<TItem> implements Bson
    {
        private final String fieldName;
        private final TItem value;
        private final String operator;
        
        public SimpleUpdate(final String fieldName, final TItem value, final String operator) {
            this.fieldName = Assertions.notNull("fieldName", fieldName);
            this.value = value;
            this.operator = operator;
        }
        
        @Override
        public <TDocument> BsonDocument toBsonDocument(final Class<TDocument> tDocumentClass, final CodecRegistry codecRegistry) {
            final BsonDocumentWriter writer = new BsonDocumentWriter(new BsonDocument());
            writer.writeStartDocument();
            writer.writeName(this.operator);
            writer.writeStartDocument();
            writer.writeName(this.fieldName);
            BuildersHelper.encodeValue(writer, this.value, codecRegistry);
            writer.writeEndDocument();
            writer.writeEndDocument();
            return writer.getDocument();
        }
        
        @Override
        public String toString() {
            return "Update{fieldName='" + this.fieldName + '\'' + ", operator='" + this.operator + '\'' + ", value=" + this.value + '}';
        }
    }
    
    private static class WithEachUpdate<TItem> implements Bson
    {
        private final String fieldName;
        private final List<TItem> values;
        private final String operator;
        
        public WithEachUpdate(final String fieldName, final List<TItem> values, final String operator) {
            this.fieldName = Assertions.notNull("fieldName", fieldName);
            this.values = Assertions.notNull("values", values);
            this.operator = operator;
        }
        
        @Override
        public <TDocument> BsonDocument toBsonDocument(final Class<TDocument> tDocumentClass, final CodecRegistry codecRegistry) {
            final BsonDocumentWriter writer = new BsonDocumentWriter(new BsonDocument());
            writer.writeStartDocument();
            writer.writeName(this.operator);
            writer.writeStartDocument();
            writer.writeName(this.fieldName);
            writer.writeStartDocument();
            writer.writeStartArray("$each");
            for (final TItem value : this.values) {
                BuildersHelper.encodeValue(writer, value, codecRegistry);
            }
            writer.writeEndArray();
            this.writeAdditionalFields(writer, tDocumentClass, codecRegistry);
            writer.writeEndDocument();
            writer.writeEndDocument();
            writer.writeEndDocument();
            return writer.getDocument();
        }
        
        protected <TDocument> void writeAdditionalFields(final BsonDocumentWriter writer, final Class<TDocument> tDocumentClass, final CodecRegistry codecRegistry) {
        }
        
        protected String additionalFieldsToString() {
            return "";
        }
        
        @Override
        public String toString() {
            return "Each Update{fieldName='" + this.fieldName + '\'' + ", operator='" + this.operator + '\'' + ", values=" + this.values + this.additionalFieldsToString() + '}';
        }
    }
    
    private static class PushUpdate<TItem> extends WithEachUpdate<TItem>
    {
        private final PushOptions options;
        
        public PushUpdate(final String fieldName, final List<TItem> values, final PushOptions options) {
            super(fieldName, values, "$push");
            this.options = Assertions.notNull("options", options);
        }
        
        @Override
        protected <TDocument> void writeAdditionalFields(final BsonDocumentWriter writer, final Class<TDocument> tDocumentClass, final CodecRegistry codecRegistry) {
            if (this.options.getPosition() != null) {
                writer.writeInt32("$position", this.options.getPosition());
            }
            if (this.options.getSlice() != null) {
                writer.writeInt32("$slice", this.options.getSlice());
            }
            if (this.options.getSort() != null) {
                writer.writeInt32("$sort", this.options.getSort());
            }
            else if (this.options.getSortDocument() != null) {
                writer.writeName("$sort");
                BuildersHelper.encodeValue(writer, this.options.getSortDocument(), codecRegistry);
            }
        }
        
        @Override
        protected String additionalFieldsToString() {
            return ", options=" + this.options;
        }
    }
    
    private static class PullAllUpdate<TItem> implements Bson
    {
        private final String fieldName;
        private final List<TItem> values;
        
        public PullAllUpdate(final String fieldName, final List<TItem> values) {
            this.fieldName = Assertions.notNull("fieldName", fieldName);
            this.values = Assertions.notNull("values", values);
        }
        
        @Override
        public <TDocument> BsonDocument toBsonDocument(final Class<TDocument> tDocumentClass, final CodecRegistry codecRegistry) {
            final BsonDocumentWriter writer = new BsonDocumentWriter(new BsonDocument());
            writer.writeStartDocument();
            writer.writeName("$pullAll");
            writer.writeStartDocument();
            writer.writeName(this.fieldName);
            writer.writeStartArray();
            for (final TItem value : this.values) {
                BuildersHelper.encodeValue(writer, value, codecRegistry);
            }
            writer.writeEndArray();
            writer.writeEndDocument();
            writer.writeEndDocument();
            return writer.getDocument();
        }
        
        @Override
        public String toString() {
            return "Update{fieldName='" + this.fieldName + '\'' + ", operator='$pullAll'" + ", value=" + this.values + '}';
        }
    }
    
    private static class CompositeUpdate implements Bson
    {
        private final List<Bson> updates;
        
        public CompositeUpdate(final List<Bson> updates) {
            this.updates = updates;
        }
        
        @Override
        public <TDocument> BsonDocument toBsonDocument(final Class<TDocument> tDocumentClass, final CodecRegistry codecRegistry) {
            final BsonDocument document = new BsonDocument();
            for (final Bson update : this.updates) {
                final BsonDocument rendered = update.toBsonDocument(tDocumentClass, codecRegistry);
                for (final Map.Entry<String, BsonValue> element : rendered.entrySet()) {
                    if (document.containsKey(element.getKey())) {
                        final BsonDocument currentOperatorDocument = element.getValue();
                        final BsonDocument existingOperatorDocument = document.getDocument(element.getKey());
                        for (final Map.Entry<String, BsonValue> currentOperationDocumentElements : currentOperatorDocument.entrySet()) {
                            existingOperatorDocument.append(currentOperationDocumentElements.getKey(), currentOperationDocumentElements.getValue());
                        }
                    }
                    else {
                        document.append(element.getKey(), element.getValue());
                    }
                }
            }
            return document;
        }
        
        @Override
        public String toString() {
            return "Updates{updates=" + this.updates + '}';
        }
    }
}
