package com.mongodb.client.model.geojson;

import com.mongodb.assertions.*;

public final class Point extends Geometry
{
    private final Position coordinate;
    
    public Point(final Position coordinate) {
        this(null, coordinate);
    }
    
    public Point(final CoordinateReferenceSystem coordinateReferenceSystem, final Position coordinate) {
        super(coordinateReferenceSystem);
        this.coordinate = Assertions.notNull("coordinates", coordinate);
    }
    
    @Override
    public GeoJsonObjectType getType() {
        return GeoJsonObjectType.POINT;
    }
    
    public Position getCoordinates() {
        return this.coordinate;
    }
    
    public Position getPosition() {
        return this.coordinate;
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
        final Point point = (Point)o;
        return this.coordinate.equals(point.coordinate);
    }
    
    @Override
    public int hashCode() {
        final int result = super.hashCode();
        return 31 * result + this.coordinate.hashCode();
    }
    
    @Override
    public String toString() {
        return "Point{coordinate=" + this.coordinate + ((this.getCoordinateReferenceSystem() == null) ? "" : (", coordinateReferenceSystem=" + this.getCoordinateReferenceSystem())) + '}';
    }
}
