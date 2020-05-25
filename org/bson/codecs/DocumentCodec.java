package org.bson.codecs;

import org.bson.assertions.*;
import org.bson.*;
import java.util.*;
import org.bson.codecs.configuration.*;

public class DocumentCodec implements CollectibleCodec<Document>
{
    private static final String ID_FIELD_NAME = "_id";
    private static final CodecRegistry DEFAULT_REGISTRY;
    private static final BsonTypeClassMap DEFAULT_BSON_TYPE_CLASS_MAP;
    private final BsonTypeCodecMap bsonTypeCodecMap;
    private final CodecRegistry registry;
    private final IdGenerator idGenerator;
    private final Transformer valueTransformer;
    
    public DocumentCodec() {
        this(DocumentCodec.DEFAULT_REGISTRY, DocumentCodec.DEFAULT_BSON_TYPE_CLASS_MAP);
    }
    
    public DocumentCodec(final CodecRegistry registry, final BsonTypeClassMap bsonTypeClassMap) {
        this(registry, bsonTypeClassMap, null);
    }
    
    public DocumentCodec(final CodecRegistry registry, final BsonTypeClassMap bsonTypeClassMap, final Transformer valueTransformer) {
        this.registry = Assertions.notNull("registry", registry);
        this.bsonTypeCodecMap = new BsonTypeCodecMap(Assertions.notNull("bsonTypeClassMap", bsonTypeClassMap), registry);
        this.idGenerator = new ObjectIdGenerator();
        this.valueTransformer = ((valueTransformer != null) ? valueTransformer : new Transformer() {
            @Override
            public Object transform(final Object value) {
                return value;
            }
        });
    }
    
    @Override
    public boolean documentHasId(final Document document) {
        return document.containsKey("_id");
    }
    
    @Override
    public BsonValue getDocumentId(final Document document) {
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
    public Document generateIdIfAbsentFromDocument(final Document document) {
        if (!this.documentHasId(document)) {
            document.put("_id", this.idGenerator.generate());
        }
        return document;
    }
    
    @Override
    public void encode(final BsonWriter writer, final Document document, final EncoderContext encoderContext) {
        this.writeMap(writer, document, encoderContext);
    }
    
    @Override
    public Document decode(final BsonReader reader, final DecoderContext decoderContext) {
        final Document document = new Document();
        reader.readStartDocument();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            final String fieldName = reader.readName();
            document.put(fieldName, this.readValue(reader, decoderContext));
        }
        reader.readEndDocument();
        return document;
    }
    
    @Override
    public Class<Document> getEncoderClass() {
        return Document.class;
    }
    
    private void beforeFields(final BsonWriter bsonWriter, final EncoderContext encoderContext, final Map<String, Object> document) {
        if (encoderContext.isEncodingCollectibleDocument() && document.containsKey("_id")) {
            bsonWriter.writeName("_id");
            this.writeValue(bsonWriter, encoderContext, document.get("_id"));
        }
    }
    
    private boolean skipField(final EncoderContext encoderContext, final String key) {
        return encoderContext.isEncodingCollectibleDocument() && key.equals("_id");
    }
    
    private void writeValue(final BsonWriter writer, final EncoderContext encoderContext, final Object value) {
        if (value == null) {
            writer.writeNull();
        }
        else if (value instanceof Iterable) {
            this.writeIterable(writer, (Iterable<Object>)value, encoderContext.getChildContext());
        }
        else if (value instanceof Map) {
            this.writeMap(writer, (Map<String, Object>)value, encoderContext.getChildContext());
        }
        else {
            final Codec codec = this.registry.get(value.getClass());
            encoderContext.encodeWithChildContext(codec, writer, value);
        }
    }
    
    private void writeMap(final BsonWriter writer, final Map<String, Object> map, final EncoderContext encoderContext) {
        writer.writeStartDocument();
        this.beforeFields(writer, encoderContext, map);
        for (final Map.Entry<String, Object> entry : map.entrySet()) {
            if (this.skipField(encoderContext, entry.getKey())) {
                continue;
            }
            writer.writeName(entry.getKey());
            this.writeValue(writer, encoderContext, entry.getValue());
        }
        writer.writeEndDocument();
    }
    
    private void writeIterable(final BsonWriter writer, final Iterable<Object> list, final EncoderContext encoderContext) {
        writer.writeStartArray();
        for (final Object value : list) {
            this.writeValue(writer, encoderContext, value);
        }
        writer.writeEndArray();
    }
    
    private Object readValue(final BsonReader reader, final DecoderContext decoderContext) {
        final BsonType bsonType = reader.getCurrentBsonType();
        if (bsonType == BsonType.NULL) {
            reader.readNull();
            return null;
        }
        if (bsonType == BsonType.ARRAY) {
            return this.readList(reader, decoderContext);
        }
        if (bsonType == BsonType.BINARY && BsonBinarySubType.isUuid(reader.peekBinarySubType()) && reader.peekBinarySize() == 16) {
            return this.registry.get(UUID.class).decode(reader, decoderContext);
        }
        return this.valueTransformer.transform(this.bsonTypeCodecMap.get(bsonType).decode(reader, decoderContext));
    }
    
    private List<Object> readList(final BsonReader reader, final DecoderContext decoderContext) {
        reader.readStartArray();
        final List<Object> list = new ArrayList<Object>();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            list.add(this.readValue(reader, decoderContext));
        }
        reader.readEndArray();
        return list;
    }
    
    static {
        DEFAULT_REGISTRY = CodecRegistries.fromProviders(Arrays.asList(new ValueCodecProvider(), new BsonValueCodecProvider(), new DocumentCodecProvider()));
        DEFAULT_BSON_TYPE_CLASS_MAP = new BsonTypeClassMap();
    }
}
