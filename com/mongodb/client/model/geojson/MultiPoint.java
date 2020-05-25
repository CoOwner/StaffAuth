package com.mongodb.client.model.geojson;

import com.mongodb.assertions.*;
import java.util.*;

public final class MultiPoint extends Geometry
{
    private final List<Position> coordinates;
    
    public MultiPoint(final List<Position> coordinates) {
        this(null, coordinates);
    }
    
    public MultiPoint(final CoordinateReferenceSystem coordinateReferenceSystem, final List<Position> coordinates) {
        super(coordinateReferenceSystem);
        Assertions.notNull("coordinates", coordinates);
        Assertions.isTrueArgument("coordinates contains only non-null positions", !coordinates.contains(null));
        this.coordinates = Collections.unmodifiableList((List<? extends Position>)coordinates);
    }
    
    @Override
    public GeoJsonObjectType getType() {
        return GeoJsonObjectType.MULTI_POINT;
    }
    
    public List<Position> getCoordinates() {
        return this.coordinates;
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
        final MultiPoint multiPoint = (MultiPoint)o;
        return this.coordinates.equals(multiPoint.coordinates);
    }
    
    @Override
    public int hashCode() {
        final int result = super.hashCode();
        return 31 * result + this.coordinates.hashCode();
    }
    
    @Override
    public String toString() {
        return "MultiPoint{coordinates=" + this.coordinates + ((this.getCoordinateReferenceSystem() == null) ? "" : (", coordinateReferenceSystem=" + this.getCoordinateReferenceSystem())) + '}';
    }
}
