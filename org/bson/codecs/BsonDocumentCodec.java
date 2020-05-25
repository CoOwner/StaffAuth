package org.bson.codecs;

import java.util.*;
import org.bson.types.*;
import org.bson.*;
import org.bson.codecs.configuration.*;

public class BsonDocumentCodec implements CollectibleCodec<BsonDocument>
{
    private static final String ID_FIELD_NAME = "_id";
    private static final CodecRegistry DEFAULT_REGISTRY;
    private final CodecRegistry codecRegistry;
    private final BsonTypeCodecMap bsonTypeCodecMap;
    
    public BsonDocumentCodec() {
        this(BsonDocumentCodec.DEFAULT_REGISTRY);
    }
    
    public BsonDocumentCodec(final CodecRegistry codecRegistry) {
        if (codecRegistry == null) {
            throw new IllegalArgumentException("Codec registry can not be null");
        }
        this.codecRegistry = codecRegistry;
        this.bsonTypeCodecMap = new BsonTypeCodecMap(BsonValueCodecProvider.getBsonTypeClassMap(), codecRegistry);
    }
    
    public CodecRegistry getCodecRegistry() {
        return this.codecRegistry;
    }
    
    @Override
    public BsonDocument decode(final BsonReader reader, final DecoderContext decoderContext) {
        final List<BsonElement> keyValuePairs = new ArrayList<BsonElement>();
        reader.readStartDocument();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            final String fieldName = reader.readName();
            keyValuePairs.add(new BsonElement(fieldName, this.readValue(reader, decoderContext)));
        }
        reader.readEndDocument();
        return new BsonDocument(keyValuePairs);
    }
    
    protected BsonValue readValue(final BsonReader reader, final DecoderContext decoderContext) {
        return this.bsonTypeCodecMap.get(reader.getCurrentBsonType()).decode(reader, decoderContext);
    }
    
    @Override
    public void encode(final BsonWriter writer, final BsonDocument value, final EncoderContext encoderContext) {
        writer.writeStartDocument();
        this.beforeFields(writer, encoderContext, value);
        for (final Map.Entry<String, BsonValue> entry : value.entrySet()) {
            if (this.skipField(encoderContext, entry.getKey())) {
                continue;
            }
            writer.writeName(entry.getKey());
            this.writeValue(writer, encoderContext, entry.getValue());
        }
        writer.writeEndDocument();
    }
    
    private void beforeFields(final BsonWriter bsonWriter, final EncoderContext encoderContext, final BsonDocument value) {
        if (encoderContext.isEncodingCollectibleDocument() && value.containsKey("_id")) {
            bsonWriter.writeName("_id");
            this.writeValue(bsonWriter, encoderContext, value.get("_id"));
        }
    }
    
    private boolean skipField(final EncoderContext encoderContext, final String key) {
        return encoderContext.isEncodingCollectibleDocument() && key.equals("_id");
    }
    
    private void writeValue(final BsonWriter writer, final EncoderContext encoderContext, final BsonValue value) {
        final Codec codec = this.codecRegistry.get(value.getClass());
        encoderContext.encodeWithChildContext(codec, writer, value);
    }
    
    @Override
    public Class<BsonDocument> getEncoderClass() {
        return BsonDocument.class;
    }
    
    @Override
    public BsonDocument generateIdIfAbsentFromDocument(final BsonDocument document) {
        if (!this.documentHasId(document)) {
            document.put("_id", new BsonObjectId(new ObjectId()));
        }
        return document;
    }
    
    @Override
    public boolean documentHasId(final BsonDocument document) {
        return document.containsKey("_id");
    }
    
    @Override
    public BsonValue getDocumentId(final BsonDocument document) {
        return document.get("_id");
    }
    
    static {
        DEFAULT_REGISTRY = CodecRegistries.fromProviders(new BsonValueCodecProvider());
    }
}
