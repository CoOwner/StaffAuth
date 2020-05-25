package org.bson.codecs;

import org.bson.*;

public class BsonNullCodec implements Codec<BsonNull>
{
    @Override
    public BsonNull decode(final BsonReader reader, final DecoderContext decoderContext) {
        reader.readNull();
        return BsonNull.VALUE;
    }
    
    @Override
    public void encode(final BsonWriter writer, final BsonNull value, final EncoderContext encoderContext) {
        writer.writeNull();
    }
    
    @Override
    public Class<BsonNull> getEncoderClass() {
        return BsonNull.class;
    }
}
