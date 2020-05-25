package com.mongodb;

import org.bson.codecs.configuration.*;
import java.util.regex.*;
import com.mongodb.assertions.*;
import org.bson.codecs.*;
import java.lang.reflect.*;
import org.bson.*;
import java.util.*;
import org.bson.types.*;

public class DBObjectCodec implements CollectibleCodec<DBObject>
{
    private static final BsonTypeClassMap DEFAULT_BSON_TYPE_CLASS_MAP;
    private static final String ID_FIELD_NAME = "_id";
    private final CodecRegistry codecRegistry;
    private final BsonTypeCodecMap bsonTypeCodecMap;
    private final DBObjectFactory objectFactory;
    private final IdGenerator idGenerator;
    
    static BsonTypeClassMap createDefaultBsonTypeClassMap() {
        final Map<BsonType, Class<?>> replacements = new HashMap<BsonType, Class<?>>();
        replacements.put(BsonType.REGULAR_EXPRESSION, Pattern.class);
        replacements.put(BsonType.SYMBOL, String.class);
        replacements.put(BsonType.TIMESTAMP, BSONTimestamp.class);
        replacements.put(BsonType.JAVASCRIPT_WITH_SCOPE, null);
        replacements.put(BsonType.DOCUMENT, null);
        return new BsonTypeClassMap(replacements);
    }
    
    static BsonTypeClassMap getDefaultBsonTypeClassMap() {
        return DBObjectCodec.DEFAULT_BSON_TYPE_CLASS_MAP;
    }
    
    public DBObjectCodec(final CodecRegistry codecRegistry) {
        this(codecRegistry, DBObjectCodec.DEFAULT_BSON_TYPE_CLASS_MAP);
    }
    
    public DBObjectCodec(final CodecRegistry codecRegistry, final BsonTypeClassMap bsonTypeClassMap) {
        this(codecRegistry, bsonTypeClassMap, new BasicDBObjectFactory());
    }
    
    public DBObjectCodec(final CodecRegistry codecRegistry, final BsonTypeClassMap bsonTypeClassMap, final DBObjectFactory objectFactory) {
        this.idGenerator = new ObjectIdGenerator();
        this.objectFactory = Assertions.notNull("objectFactory", objectFactory);
        this.codecRegistry = Assertions.notNull("codecRegistry", codecRegistry);
        this.bsonTypeCodecMap = new BsonTypeCodecMap(Assertions.notNull("bsonTypeClassMap", bsonTypeClassMap), codecRegistry);
    }
    
    @Override
    public void encode(final BsonWriter writer, final DBObject document, final EncoderContext encoderContext) {
        writer.writeStartDocument();
        this.beforeFields(writer, encoderContext, document);
        for (final String key : document.keySet()) {
            if (this.skipField(encoderContext, key)) {
                continue;
            }
            writer.writeName(key);
            this.writeValue(writer, encoderContext, document.get(key));
        }
        writer.writeEndDocument();
    }
    
    @Override
    public DBObject decode(final BsonReader reader, final DecoderContext decoderContext) {
        final List<String> path = new ArrayList<String>(10);
        return this.readDocument(reader, decoderContext, path);
    }
    
    @Override
    public Class<DBObject> getEncoderClass() {
        return DBObject.class;
    }
    
    @Override
    public boolean documentHasId(final DBObject document) {
        return document.containsField("_id");
    }
    
    @Override
    public BsonValue getDocumentId(final DBObject document) {
        if (!this.documentHasId(document)) {
            throw new IllegalStateException("The document does not contain an _id");
        }
        final Object id = document.get("_id");
        if (id instanceof BsonValue) {
            return (BsonValue)id;
        }
        final BsonDocument idHoldingDocument = new BsonDocument();
        final BsonWriter writer = new BsonDocumentWriter(idHoldingDocument);
        writer.writeStartDocument();
        writer.writeName("_id");
        this.writeValue(writer, EncoderContext.builder().build(), id);
        writer.writeEndDocument();
        return idHoldingDocument.get("_id");
    }
    
    @Override
    public DBObject generateIdIfAbsentFromDocument(final DBObject document) {
        if (!this.documentHasId(document)) {
            document.put("_id", this.idGenerator.generate());
        }
        return document;
    }
    
    private void beforeFields(final BsonWriter bsonWriter, final EncoderContext encoderContext, final DBObject document) {
        if (encoderContext.isEncodingCollectibleDocument() && document.containsField("_id")) {
            bsonWriter.writeName("_id");
            this.writeValue(bsonWriter, null, document.get("_id"));
        }
    }
    
    private boolean skipField(final EncoderContext encoderContext, final String key) {
        return encoderContext.isEncodingCollectibleDocument() && key.equals("_id");
    }
    
    private void writeValue(final BsonWriter bsonWriter, final EncoderContext encoderContext, final Object initialValue) {
        final Object value = BSON.applyEncodingHooks(initialValue);
        if (value == null) {
            bsonWriter.writeNull();
        }
        else if (value instanceof DBRef) {
            this.encodeDBRef(bsonWriter, (DBRef)value);
        }
        else if (value instanceof Map) {
            this.encodeMap(bsonWriter, (Map<String, Object>)value);
        }
        else if (value instanceof Iterable) {
            this.encodeIterable(bsonWriter, (Iterable)value);
        }
        else if (value instanceof BSONObject) {
            this.encodeBsonObject(bsonWriter, (BSONObject)value);
        }
        else if (value instanceof CodeWScope) {
            this.encodeCodeWScope(bsonWriter, (CodeWScope)value);
        }
        else if (value instanceof byte[]) {
            this.encodeByteArray(bsonWriter, (byte[])value);
        }
        else if (value.getClass().isArray()) {
            this.encodeArray(bsonWriter, value);
        }
        else if (value instanceof Symbol) {
            bsonWriter.writeSymbol(((Symbol)value).getSymbol());
        }
        else {
            final Codec codec = this.codecRegistry.get(value.getClass());
            codec.encode(bsonWriter, value, encoderContext);
        }
    }
    
    private void encodeMap(final BsonWriter bsonWriter, final Map<String, Object> document) {
        bsonWriter.writeStartDocument();
        for (final Map.Entry<String, Object> entry : document.entrySet()) {
            bsonWriter.writeName(entry.getKey());
            this.writeValue(bsonWriter, null, entry.getValue());
        }
        bsonWriter.writeEndDocument();
    }
    
    private void encodeBsonObject(final BsonWriter bsonWriter, final BSONObject document) {
        bsonWriter.writeStartDocument();
        for (final String key : document.keySet()) {
            bsonWriter.writeName(key);
            this.writeValue(bsonWriter, null, document.get(key));
        }
        bsonWriter.writeEndDocument();
    }
    
    private void encodeByteArray(final BsonWriter bsonWriter, final byte[] value) {
        bsonWriter.writeBinaryData(new BsonBinary(value));
    }
    
    private void encodeArray(final BsonWriter bsonWriter, final Object value) {
        bsonWriter.writeStartArray();
        for (int size = Array.getLength(value), i = 0; i < size; ++i) {
            this.writeValue(bsonWriter, null, Array.get(value, i));
        }
        bsonWriter.writeEndArray();
    }
    
    private void encodeDBRef(final BsonWriter bsonWriter, final DBRef dbRef) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeString("$ref", dbRef.getCollectionName());
        bsonWriter.writeName("$id");
        this.writeValue(bsonWriter, null, dbRef.getId());
        bsonWriter.writeEndDocument();
    }
    
    private void encodeCodeWScope(final BsonWriter bsonWriter, final CodeWScope value) {
        bsonWriter.writeJavaScriptWithScope(value.getCode());
        this.encodeBsonObject(bsonWriter, value.getScope());
    }
    
    private void encodeIterable(final BsonWriter bsonWriter, final Iterable iterable) {
        bsonWriter.writeStartArray();
        for (final Object cur : iterable) {
            this.writeValue(bsonWriter, null, cur);
        }
        bsonWriter.writeEndArray();
    }
    
    private Object readValue(final BsonReader reader, final DecoderContext decoderContext, final String fieldName, final List<String> path) {
        final BsonType bsonType = reader.getCurrentBsonType();
        if (bsonType.isContainer() && fieldName != null) {
            path.add(fieldName);
        }
        Object initialRetVal = null;
        switch (bsonType) {
            case DOCUMENT: {
                initialRetVal = this.verifyForDBRef(this.readDocument(reader, decoderContext, path));
                break;
            }
            case ARRAY: {
                initialRetVal = this.readArray(reader, decoderContext, path);
                break;
            }
            case JAVASCRIPT_WITH_SCOPE: {
                initialRetVal = this.readCodeWScope(reader, decoderContext, path);
                break;
            }
            case DB_POINTER: {
                final BsonDbPointer dbPointer = reader.readDBPointer();
                initialRetVal = new DBRef(dbPointer.getNamespace(), dbPointer.getId());
                break;
            }
            case BINARY: {
                initialRetVal = this.readBinary(reader, decoderContext);
                break;
            }
            case NULL: {
                reader.readNull();
                initialRetVal = null;
                break;
            }
            default: {
                initialRetVal = this.bsonTypeCodecMap.get(bsonType).decode(reader, decoderContext);
                break;
            }
        }
        if (bsonType.isContainer() && fieldName != null) {
            path.remove(fieldName);
        }
        return BSON.applyDecodingHooks(initialRetVal);
    }
    
    private Object readBinary(final BsonReader reader, final DecoderContext decoderContext) {
        final byte bsonBinarySubType = reader.peekBinarySubType();
        if (BsonBinarySubType.isUuid(bsonBinarySubType) && reader.peekBinarySize() == 16) {
            return this.codecRegistry.get(UUID.class).decode(reader, decoderContext);
        }
        if (bsonBinarySubType == BsonBinarySubType.BINARY.getValue() || bsonBinarySubType == BsonBinarySubType.OLD_BINARY.getValue()) {
            return this.codecRegistry.get(byte[].class).decode(reader, decoderContext);
        }
        return this.codecRegistry.get(Binary.class).decode(reader, decoderContext);
    }
    
    private List readArray(final BsonReader reader, final DecoderContext decoderContext, final List<String> path) {
        reader.readStartArray();
        final BasicDBList list = new BasicDBList();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            list.add(this.readValue(reader, decoderContext, null, path));
        }
        reader.readEndArray();
        return list;
    }
    
    private DBObject readDocument(final BsonReader reader, final DecoderContext decoderContext, final List<String> path) {
        final DBObject document = this.objectFactory.getInstance(path);
        reader.readStartDocument();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            final String fieldName = reader.readName();
            document.put(fieldName, this.readValue(reader, decoderContext, fieldName, path));
        }
        reader.readEndDocument();
        return document;
    }
    
    private CodeWScope readCodeWScope(final BsonReader reader, final DecoderContext decoderContext, final List<String> path) {
        return new CodeWScope(reader.readJavaScriptWithScope(), this.readDocument(reader, decoderContext, path));
    }
    
    private Object verifyForDBRef(final DBObject document) {
        if (document.containsField("$ref") && document.containsField("$id")) {
            return new DBRef((String)document.get("$ref"), document.get("$id"));
        }
        return document;
    }
    
    static {
        DEFAULT_BSON_TYPE_CLASS_MAP = createDefaultBsonTypeClassMap();
    }
}
