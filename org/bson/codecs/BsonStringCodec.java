package org.bson.codecs;

import org.bson.*;

public class BsonStringCodec implements Codec<BsonString>
{
    @Override
    public BsonString decode(final BsonReader reader, final DecoderContext decoderContext) {
        return new BsonString(reader.readString());
    }
    
    @Override
    public void encode(final BsonWriter writer, final BsonString value, final EncoderContext encoderContext) {
        writer.writeString(value.getValue());
    }
    
    @Override
    public Class<BsonString> getEncoderClass() {
        return BsonString.class;
    }
}
