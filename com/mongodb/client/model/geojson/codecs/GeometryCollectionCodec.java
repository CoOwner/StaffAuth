package com.mongodb.client.model.geojson.codecs;

import org.bson.codecs.configuration.*;
import com.mongodb.assertions.*;
import com.mongodb.client.model.geojson.*;
import java.util.*;
import org.bson.*;
import org.bson.codecs.*;

public class GeometryCollectionCodec implements Codec<GeometryCollection>
{
    private final CodecRegistry registry;
    
    public GeometryCollectionCodec(final CodecRegistry registry) {
        this.registry = Assertions.notNull("registry", registry);
    }
    
    @Override
    public void encode(final BsonWriter writer, final GeometryCollection value, final EncoderContext encoderContext) {
        writer.writeStartDocument();
        GeometryCodecHelper.encodeType(writer, value);
        writer.writeName("geometries");
        writer.writeStartArray();
        for (final Geometry geometry : value.getGeometries()) {
            this.encodeGeometry(writer, geometry, encoderContext);
        }
        writer.writeEndArray();
        GeometryCodecHelper.encodeCoordinateReferenceSystem(writer, value, encoderContext, this.registry);
        writer.writeEndDocument();
    }
    
    private void encodeGeometry(final BsonWriter writer, final Geometry geometry, final EncoderContext encoderContext) {
        final Codec codec = this.registry.get(geometry.getClass());
        encoderContext.encodeWithChildContext(codec, writer, geometry);
    }
    
    @Override
    public Class<GeometryCollection> getEncoderClass() {
        return GeometryCollection.class;
    }
    
    @Override
    public GeometryCollection decode(final BsonReader reader, final DecoderContext decoderContext) {
        throw new UnsupportedOperationException("Not implemented yet!");
    }
}
