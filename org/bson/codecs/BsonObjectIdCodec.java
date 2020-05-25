package org.bson.codecs;

import org.bson.*;

public class BsonObjectIdCodec implements Codec<BsonObjectId>
{
    @Override
    public void encode(final BsonWriter writer, final BsonObjectId value, final EncoderContext encoderContext) {
        writer.writeObjectId(value.getValue());
    }
    
    @Override
    public BsonObjectId decode(final BsonReader reader, final DecoderContext decoderContext) {
        return new BsonObjectId(reader.readObjectId());
    }
    
    @Override
    public Class<BsonObjectId> getEncoderClass() {
        return BsonObjectId.class;
    }
}
