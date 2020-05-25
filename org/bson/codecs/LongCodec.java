package org.bson.codecs;

import org.bson.*;

public class LongCodec implements Codec<Long>
{
    @Override
    public void encode(final BsonWriter writer, final Long value, final EncoderContext encoderContext) {
        writer.writeInt64(value);
    }
    
    @Override
    public Long decode(final BsonReader reader, final DecoderContext decoderContext) {
        return reader.readInt64();
    }
    
    @Override
    public Class<Long> getEncoderClass() {
        return Long.class;
    }
}
