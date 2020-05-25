package com.mongodb.client.model.geojson;

import java.util.*;
import com.mongodb.assertions.*;

public final class Polygon extends Geometry
{
    private final PolygonCoordinates coordinates;
    
    public Polygon(final List<Position> exterior, final List<Position>... holes) {
        this(new PolygonCoordinates(exterior, holes));
    }
    
    public Polygon(final PolygonCoordinates coordinates) {
        this(null, coordinates);
    }
    
    public Polygon(final CoordinateReferenceSystem coordinateReferenceSystem, final PolygonCoordinates coordinates) {
        super(coordinateReferenceSystem);
        this.coordinates = Assertions.notNull("coordinates", coordinates);
    }
    
    @Override
    public GeoJsonObjectType getType() {
        return GeoJsonObjectType.POLYGON;
    }
    
    public PolygonCoordinates getCoordinates() {
        return this.coordinates;
    }
    
    public List<Position> getExterior() {
        return this.coordinates.getExterior();
    }
    
    public List<List<Position>> getHoles() {
        return this.coordinates.getHoles();
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        final Polygon polygon = (Polygon)o;
        return this.coordinates.equals(polygon.coordinates);
    }
    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + this.coordinates.hashCode();
        return result;
    }
    
    @Override
    public String toString() {
        return "Polygon{exterior=" + this.coordinates.getExterior() + (this.coordinates.getHoles().isEmpty() ? "" : (", holes=" + this.coordinates.getHoles())) + ((this.getCoordinateReferenceSystem() == null) ? "" : (", coordinateReferenceSystem=" + this.getCoordinateReferenceSystem())) + '}';
    }
}
