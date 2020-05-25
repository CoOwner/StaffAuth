package com.mongodb;

import org.bson.types.*;
import org.bson.*;
import org.bson.codecs.*;

public class BSONTimestampCodec implements Codec<BSONTimestamp>
{
    @Override
    public void encode(final BsonWriter writer, final BSONTimestamp value, final EncoderContext encoderContext) {
        writer.writeTimestamp(new BsonTimestamp(value.getTime(), value.getInc()));
    }
    
    @Override
    public BSONTimestamp decode(final BsonReader reader, final DecoderContext decoderContext) {
        final BsonTimestamp timestamp = reader.readTimestamp();
        return new BSONTimestamp(timestamp.getTime(), timestamp.getInc());
    }
    
    @Override
    public Class<BSONTimestamp> getEncoderClass() {
        return BSONTimestamp.class;
    }
}
