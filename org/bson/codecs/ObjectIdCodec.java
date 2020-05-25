package org.bson.codecs;

import org.bson.types.*;
import org.bson.*;

public class ObjectIdCodec implements Codec<ObjectId>
{
    @Override
    public void encode(final BsonWriter writer, final ObjectId value, final EncoderContext encoderContext) {
        writer.writeObjectId(value);
    }
    
    @Override
    public ObjectId decode(final BsonReader reader, final DecoderContext decoderContext) {
        return reader.readObjectId();
    }
    
    @Override
    public Class<ObjectId> getEncoderClass() {
        return ObjectId.class;
    }
}
