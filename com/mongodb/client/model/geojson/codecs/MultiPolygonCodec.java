package com.mongodb.client.model.geojson.codecs;

import org.bson.codecs.configuration.*;
import com.mongodb.assertions.*;
import java.util.*;
import com.mongodb.client.model.geojson.*;
import org.bson.*;
import org.bson.codecs.*;

public class MultiPolygonCodec implements Codec<MultiPolygon>
{
    private final CodecRegistry registry;
    
    public MultiPolygonCodec(final CodecRegistry registry) {
        this.registry = Assertions.notNull("registry", registry);
    }
    
    @Override
    public void encode(final BsonWriter writer, final MultiPolygon value, final EncoderContext encoderContext) {
        GeometryCodecHelper.encodeGeometry(writer, value, encoderContext, this.registry, new Runnable() {
            @Override
            public void run() {
                writer.writeStartArray();
                for (final PolygonCoordinates polygonCoordinates : value.getCoordinates()) {
                    GeometryCodecHelper.encodePolygonCoordinates(writer, polygonCoordinates);
                }
                writer.writeEndArray();
            }
        });
    }
    
    @Override
    public Class<MultiPolygon> getEncoderClass() {
        return MultiPolygon.class;
    }
    
    @Override
    public MultiPolygon decode(final BsonReader reader, final DecoderContext decoderContext) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }
}
