package org.bson.codecs;

import org.bson.types.*;
import org.bson.*;

public class MaxKeyCodec implements Codec<MaxKey>
{
    @Override
    public void encode(final BsonWriter writer, final MaxKey value, final EncoderContext encoderContext) {
        writer.writeMaxKey();
    }
    
    @Override
    public MaxKey decode(final BsonReader reader, final DecoderContext decoderContext) {
        reader.readMaxKey();
        return new MaxKey();
    }
    
    @Override
    public Class<MaxKey> getEncoderClass() {
        return MaxKey.class;
    }
}
