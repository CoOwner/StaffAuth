package com.mongodb.client.model.geojson.codecs;

import org.bson.codecs.configuration.*;
import com.mongodb.assertions.*;
import com.mongodb.client.model.geojson.*;
import org.bson.*;
import org.bson.codecs.*;

public class PolygonCodec implements Codec<Polygon>
{
    private final CodecRegistry registry;
    
    public PolygonCodec(final CodecRegistry registry) {
        this.registry = Assertions.notNull("registry", registry);
    }
    
    @Override
    public void encode(final BsonWriter writer, final Polygon value, final EncoderContext encoderContext) {
        GeometryCodecHelper.encodeGeometry(writer, value, encoderContext, this.registry, new Runnable() {
            @Override
            public void run() {
                GeometryCodecHelper.encodePolygonCoordinates(writer, value.getCoordinates());
            }
        });
    }
    
    @Override
    public Class<Polygon> getEncoderClass() {
        return Polygon.class;
    }
    
    @Override
    public Polygon decode(final BsonReader reader, final DecoderContext decoderContext) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }
}
