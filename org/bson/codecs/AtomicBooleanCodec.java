package org.bson.codecs;

import java.util.concurrent.atomic.*;
import org.bson.*;

public class AtomicBooleanCodec implements Codec<AtomicBoolean>
{
    @Override
    public void encode(final BsonWriter writer, final AtomicBoolean value, final EncoderContext encoderContext) {
        writer.writeBoolean(value.get());
    }
    
    @Override
    public AtomicBoolean decode(final BsonReader reader, final DecoderContext decoderContext) {
        return new AtomicBoolean(reader.readBoolean());
    }
    
    @Override
    public Class<AtomicBoolean> getEncoderClass() {
        return AtomicBoolean.class;
    }
}
