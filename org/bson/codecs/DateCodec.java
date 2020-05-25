package org.bson.codecs;

import java.util.*;
import org.bson.*;

public class DateCodec implements Codec<Date>
{
    @Override
    public void encode(final BsonWriter writer, final Date value, final EncoderContext encoderContext) {
        writer.writeDateTime(value.getTime());
    }
    
    @Override
    public Date decode(final BsonReader reader, final DecoderContext decoderContext) {
        return new Date(reader.readDateTime());
    }
    
    @Override
    public Class<Date> getEncoderClass() {
        return Date.class;
    }
}
