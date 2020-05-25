package org.bson.codecs;

import org.bson.*;

public class BsonInt32Codec implements Codec<BsonInt32>
{
    @Override
    public BsonInt32 decode(final BsonReader reader, final DecoderContext decoderContext) {
        return new BsonInt32(reader.readInt32());
    }
    
    @Override
    public void encode(final BsonWriter writer, final BsonInt32 value, final EncoderContext encoderContext) {
        writer.writeInt32(value.getValue());
    }
    
    @Override
    public Class<BsonInt32> getEncoderClass() {
        return BsonInt32.class;
    }
}
