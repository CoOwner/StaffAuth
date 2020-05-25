package com.mongodb.client.model.geojson;

import com.mongodb.assertions.*;
import java.util.*;

public final class MultiPolygon extends Geometry
{
    private final List<PolygonCoordinates> coordinates;
    
    public MultiPolygon(final List<PolygonCoordinates> coordinates) {
        this(null, coordinates);
    }
    
    public MultiPolygon(final CoordinateReferenceSystem coordinateReferenceSystem, final List<PolygonCoordinates> coordinates) {
        super(coordinateReferenceSystem);
        Assertions.notNull("coordinates", coordinates);
        Assertions.isTrueArgument("coordinates has no null elements", !coordinates.contains(null));
        this.coordinates = Collections.unmodifiableList((List<? extends PolygonCoordinates>)coordinates);
    }
    
    @Override
    public GeoJsonObjectType getType() {
        return GeoJsonObjectType.MULTI_POLYGON;
    }
    
    public List<PolygonCoordinates> getCoordinates() {
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
        final MultiPolygon that = (MultiPolygon)o;
        return this.coordinates.equals(that.coordinates);
    }
    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + this.coordinates.hashCode();
        return result;
    }
    
    @Override
    public String toString() {
        return "MultiPolygon{coordinates=" + this.coordinates + ((this.getCoordinateReferenceSystem() == null) ? "" : (", coordinateReferenceSystem=" + this.getCoordinateReferenceSystem())) + '}';
    }
}
