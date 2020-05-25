package org.bson.codecs;

import org.bson.*;

public class BsonDecimal128Codec implements Codec<BsonDecimal128>
{
    @Override
    public BsonDecimal128 decode(final BsonReader reader, final DecoderContext decoderContext) {
        return new BsonDecimal128(reader.readDecimal128());
    }
    
    @Override
    public void encode(final BsonWriter writer, final BsonDecimal128 value, final EncoderContext encoderContext) {
        writer.writeDecimal128(value.getValue());
    }
    
    @Override
    public Class<BsonDecimal128> getEncoderClass() {
        return BsonDecimal128.class;
    }
}
