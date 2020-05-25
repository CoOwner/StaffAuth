package com.mongodb.client.model.geojson.codecs;

import org.bson.codecs.configuration.*;
import com.mongodb.assertions.*;
import java.util.*;
import com.mongodb.client.model.geojson.*;
import org.bson.*;
import org.bson.codecs.*;

public class MultiLineStringCodec implements Codec<MultiLineString>
{
    private final CodecRegistry registry;
    
    public MultiLineStringCodec(final CodecRegistry registry) {
        this.registry = Assertions.notNull("registry", registry);
    }
    
    @Override
    public void encode(final BsonWriter writer, final MultiLineString value, final EncoderContext encoderContext) {
        GeometryCodecHelper.encodeGeometry(writer, value, encoderContext, this.registry, new Runnable() {
            @Override
            public void run() {
                writer.writeStartArray();
                for (final List<Position> ring : value.getCoordinates()) {
                    writer.writeStartArray();
                    for (final Position position : ring) {
                        GeometryCodecHelper.encodePosition(writer, position);
                    }
                    writer.writeEndArray();
                }
                writer.writeEndArray();
            }
        });
    }
    
    @Override
    public Class<MultiLineString> getEncoderClass() {
        return MultiLineString.class;
    }
    
    @Override
    public MultiLineString decode(final BsonReader reader, final DecoderContext decoderContext) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }
}
