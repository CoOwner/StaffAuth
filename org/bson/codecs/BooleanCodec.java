package org.bson.codecs;

import org.bson.*;

public class BooleanCodec implements Codec<Boolean>
{
    @Override
    public void encode(final BsonWriter writer, final Boolean value, final EncoderContext encoderContext) {
        writer.writeBoolean(value);
    }
    
    @Override
    public Boolean decode(final BsonReader reader, final DecoderContext decoderContext) {
        return reader.readBoolean();
    }
    
    @Override
    public Class<Boolean> getEncoderClass() {
        return Boolean.class;
    }
}
