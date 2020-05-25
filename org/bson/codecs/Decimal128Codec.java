package org.bson.codecs;

import org.bson.types.*;
import org.bson.*;

public final class Decimal128Codec implements Codec<Decimal128>
{
    @Override
    public Decimal128 decode(final BsonReader reader, final DecoderContext decoderContext) {
        return reader.readDecimal128();
    }
    
    @Override
    public void encode(final BsonWriter writer, final Decimal128 value, final EncoderContext encoderContext) {
        writer.writeDecimal128(value);
    }
    
    @Override
    public Class<Decimal128> getEncoderClass() {
        return Decimal128.class;
    }
}
