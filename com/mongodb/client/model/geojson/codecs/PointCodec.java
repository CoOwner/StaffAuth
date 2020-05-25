package com.mongodb.client.model.geojson.codecs;

import org.bson.codecs.configuration.*;
import com.mongodb.assertions.*;
import com.mongodb.client.model.geojson.*;
import org.bson.*;
import org.bson.codecs.*;

public class PointCodec implements Codec<Point>
{
    private final CodecRegistry registry;
    
    public PointCodec(final CodecRegistry registry) {
        this.registry = Assertions.notNull("registry", registry);
    }
    
    @Override
    public void encode(final BsonWriter writer, final Point value, final EncoderContext encoderContext) {
        GeometryCodecHelper.encodeGeometry(writer, value, encoderContext, this.registry, new Runnable() {
            @Override
            public void run() {
                GeometryCodecHelper.encodePosition(writer, value.getPosition());
            }
        });
    }
    
    @Override
    public Class<Point> getEncoderClass() {
        return Point.class;
    }
    
    @Override
    public Point decode(final BsonReader reader, final DecoderContext decoderContext) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }
}
