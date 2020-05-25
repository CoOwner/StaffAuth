package org.bson.codecs;

import org.bson.*;

public class ShortCodec implements Codec<Short>
{
    @Override
    public void encode(final BsonWriter writer, final Short value, final EncoderContext encoderContext) {
        writer.writeInt32(value);
    }
    
    @Override
    public Short decode(final BsonReader reader, final DecoderContext decoderContext) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Class<Short> getEncoderClass() {
        return Short.class;
    }
}
