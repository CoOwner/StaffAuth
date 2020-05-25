package org.bson.codecs;

import org.bson.assertions.*;
import org.bson.*;
import java.util.*;
import org.bson.codecs.configuration.*;

public class BsonArrayCodec implements Codec<BsonArray>
{
    private static final CodecRegistry DEFAULT_REGISTRY;
    private final CodecRegistry codecRegistry;
    
    public BsonArrayCodec() {
        this(BsonArrayCodec.DEFAULT_REGISTRY);
    }
    
    public BsonArrayCodec(final CodecRegistry codecRegistry) {
        this.codecRegistry = Assertions.notNull("codecRegistry", codecRegistry);
    }
    
    @Override
    public BsonArray decode(final BsonReader reader, final DecoderContext decoderContext) {
        reader.readStartArray();
        final List<BsonValue> list = new ArrayList<BsonValue>();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            list.add(this.readValue(reader, decoderContext));
        }
        reader.readEndArray();
        return new BsonArray(list);
    }
    
    @Override
    public void encode(final BsonWriter writer, final BsonArray array, final EncoderContext encoderContext) {
        writer.writeStartArray();
        for (final BsonValue value : array) {
            final Codec codec = this.codecRegistry.get(value.getClass());
            encoderContext.encodeWithChildContext(codec, writer, value);
        }
        writer.writeEndArray();
    }
    
    @Override
    public Class<BsonArray> getEncoderClass() {
        return BsonArray.class;
    }
    
    protected BsonValue readValue(final BsonReader reader, final DecoderContext decoderContext) {
        return this.codecRegistry.get(BsonValueCodecProvider.getClassForBsonType(reader.getCurrentBsonType())).decode(reader, decoderContext);
    }
    
    static {
        DEFAULT_REGISTRY = CodecRegistries.fromProviders(new BsonValueCodecProvider());
    }
}
