package org.bson.codecs;

import org.bson.*;

public class DoubleCodec implements Codec<Double>
{
    @Override
    public void encode(final BsonWriter writer, final Double value, final EncoderContext encoderContext) {
        writer.writeDouble(value);
    }
    
    @Override
    public Double decode(final BsonReader reader, final DecoderContext decoderContext) {
        return reader.readDouble();
    }
    
    @Override
    public Class<Double> getEncoderClass() {
        return Double.class;
    }
}
