package com.mongodb.client.model.geojson.codecs;

import org.bson.codecs.configuration.*;
import com.mongodb.assertions.*;
import java.util.*;
import com.mongodb.client.model.geojson.*;
import org.bson.*;
import org.bson.codecs.*;

public class LineStringCodec implements Codec<LineString>
{
    private final CodecRegistry registry;
    
    public LineStringCodec(final CodecRegistry registry) {
        this.registry = Assertions.notNull("registry", registry);
    }
    
    @Override
    public void encode(final BsonWriter writer, final LineString value, final EncoderContext encoderContext) {
        GeometryCodecHelper.encodeGeometry(writer, value, encoderContext, this.registry, new Runnable() {
            @Override
            public void run() {
                writer.writeStartArray();
                for (final Position position : value.getCoordinates()) {
                    GeometryCodecHelper.encodePosition(writer, position);
                }
                writer.writeEndArray();
            }
        });
    }
    
    @Override
    public Class<LineString> getEncoderClass() {
        return LineString.class;
    }
    
    @Override
    public LineString decode(final BsonReader reader, final DecoderContext decoderContext) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }
}
