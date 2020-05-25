package org.bson.codecs;

import org.bson.*;

public class FloatCodec implements Codec<Float>
{
    @Override
    public void encode(final BsonWriter writer, final Float value, final EncoderContext encoderContext) {
        writer.writeDouble(value);
    }
    
    @Override
    public Float decode(final BsonReader reader, final DecoderContext decoderContext) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Class<Float> getEncoderClass() {
        return Float.class;
    }
}
