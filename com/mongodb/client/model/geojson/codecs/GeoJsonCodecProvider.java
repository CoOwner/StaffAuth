package com.mongodb.client.model.geojson.codecs;

import org.bson.codecs.configuration.*;
import org.bson.codecs.*;
import com.mongodb.client.model.geojson.*;

public class GeoJsonCodecProvider implements CodecProvider
{
    @Override
    public <T> Codec<T> get(final Class<T> clazz, final CodecRegistry registry) {
        if (clazz.equals(Polygon.class)) {
            return (Codec<T>)new PolygonCodec(registry);
        }
        if (clazz.equals(Point.class)) {
            return (Codec<T>)new PointCodec(registry);
        }
        if (clazz.equals(LineString.class)) {
            return (Codec<T>)new LineStringCodec(registry);
        }
        if (clazz.equals(MultiPoint.class)) {
            return (Codec<T>)new MultiPointCodec(registry);
        }
        if (clazz.equals(MultiLineString.class)) {
            return (Codec<T>)new MultiLineStringCodec(registry);
        }
        if (clazz.equals(MultiPolygon.class)) {
            return (Codec<T>)new MultiPolygonCodec(registry);
        }
        if (clazz.equals(GeometryCollection.class)) {
            return (Codec<T>)new GeometryCollectionCodec(registry);
        }
        if (clazz.equals(NamedCoordinateReferenceSystem.class)) {
            return (Codec<T>)new NamedCoordinateReferenceSystemCodec();
        }
        return null;
    }
}
