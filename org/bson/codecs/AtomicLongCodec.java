package org.bson.codecs;

import java.util.concurrent.atomic.*;
import org.bson.*;

public class AtomicLongCodec implements Codec<AtomicLong>
{
    @Override
    public void encode(final BsonWriter writer, final AtomicLong value, final EncoderContext encoderContext) {
        writer.writeInt64(value.longValue());
    }
    
    @Override
    public AtomicLong decode(final BsonReader reader, final DecoderContext decoderContext) {
        return new AtomicLong(reader.readInt64());
    }
    
    @Override
    public Class<AtomicLong> getEncoderClass() {
        return AtomicLong.class;
    }
}
