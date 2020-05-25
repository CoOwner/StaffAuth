package org.bson.codecs;

import org.bson.*;

public class BsonBooleanCodec implements Codec<BsonBoolean>
{
    @Override
    public BsonBoolean decode(final BsonReader reader, final DecoderContext decoderContext) {
        final boolean value = reader.readBoolean();
        return BsonBoolean.valueOf(value);
    }
    
    @Override
    public void encode(final BsonWriter writer, final BsonBoolean value, final EncoderContext encoderContext) {
        writer.writeBoolean(value.getValue());
    }
    
    @Override
    public Class<BsonBoolean> getEncoderClass() {
        return BsonBoolean.class;
    }
}
