package com.mongodb.client.model.geojson.codecs;

import org.bson.*;
import org.bson.codecs.configuration.*;
import org.bson.codecs.*;
import com.mongodb.client.model.geojson.*;
import java.util.*;

final class GeometryCodecHelper
{
    static void encodeGeometry(final BsonWriter writer, final Geometry geometry, final EncoderContext encoderContext, final CodecRegistry registry, final Runnable coordinatesEncoder) {
        writer.writeStartDocument();
        encodeType(writer, geometry);
        writer.writeName("coordinates");
        coordinatesEncoder.run();
        encodeCoordinateReferenceSystem(writer, geometry, encoderContext, registry);
        writer.writeEndDocument();
    }
    
    static void encodeType(final BsonWriter writer, final Geometry geometry) {
        writer.writeString("type", geometry.getType().getTypeName());
    }
    
    static void encodeCoordinateReferenceSystem(final BsonWriter writer, final Geometry geometry, final EncoderContext encoderContext, final CodecRegistry registry) {
        if (geometry.getCoordinateReferenceSystem() != null) {
            writer.writeName("crs");
            final Codec codec = registry.get(geometry.getCoordinateReferenceSystem().getClass());
            encoderContext.encodeWithChildContext(codec, writer, geometry.getCoordinateReferenceSystem());
        }
    }
    
    static void encodePolygonCoordinates(final BsonWriter writer, final PolygonCoordinates polygonCoordinates) {
        writer.writeStartArray();
        encodeLinearRing(polygonCoordinates.getExterior(), writer);
        for (final List<Position> ring : polygonCoordinates.getHoles()) {
            encodeLinearRing(ring, writer);
        }
        writer.writeEndArray();
    }
    
    private static void encodeLinearRing(final List<Position> ring, final BsonWriter writer) {
        writer.writeStartArray();
        for (final Position position : ring) {
            encodePosition(writer, position);
        }
        writer.writeEndArray();
    }
    
    static void encodePosition(final BsonWriter writer, final Position value) {
        writer.writeStartArray();
        for (final double number : value.getValues()) {
            writer.writeDouble(number);
        }
        writer.writeEndArray();
    }
    
    private GeometryCodecHelper() {
    }
}
