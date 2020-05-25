package com.mongodb.client.model.geojson;

import com.mongodb.assertions.*;
import java.util.*;

public final class MultiLineString extends Geometry
{
    private final List<List<Position>> coordinates;
    
    public MultiLineString(final List<List<Position>> coordinates) {
        this(null, coordinates);
    }
    
    public MultiLineString(final CoordinateReferenceSystem coordinateReferenceSystem, final List<List<Position>> coordinates) {
        super(coordinateReferenceSystem);
        Assertions.notNull("coordinates", coordinates);
        for (final List<Position> line : coordinates) {
            Assertions.notNull("line", line);
            Assertions.isTrueArgument("line contains only non-null positions", !line.contains(null));
        }
        this.coordinates = Collections.unmodifiableList((List<? extends List<Position>>)coordinates);
    }
    
    @Override
    public GeoJsonObjectType getType() {
        return GeoJsonObjectType.MULTI_LINE_STRING;
    }
    
    public List<List<Position>> getCoordinates() {
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
        final MultiLineString polygon = (MultiLineString)o;
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
        return "MultiLineString{coordinates=" + this.coordinates + ((this.getCoordinateReferenceSystem() == null) ? "" : (", coordinateReferenceSystem=" + this.getCoordinateReferenceSystem())) + '}';
    }
}
