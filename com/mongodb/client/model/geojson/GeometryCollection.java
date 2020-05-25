package com.mongodb.client.model.geojson;

import com.mongodb.assertions.*;
import java.util.*;

public final class GeometryCollection extends Geometry
{
    private final List<? extends Geometry> geometries;
    
    public GeometryCollection(final List<? extends Geometry> geometries) {
        this(null, geometries);
    }
    
    public GeometryCollection(final CoordinateReferenceSystem coordinateReferenceSystem, final List<? extends Geometry> geometries) {
        super(coordinateReferenceSystem);
        Assertions.notNull("geometries", geometries);
        Assertions.isTrueArgument("geometries contains only non-null elements", !geometries.contains(null));
        this.geometries = Collections.unmodifiableList(geometries);
    }
    
    @Override
    public GeoJsonObjectType getType() {
        return GeoJsonObjectType.GEOMETRY_COLLECTION;
    }
    
    public List<? extends Geometry> getGeometries() {
        return this.geometries;
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
        final GeometryCollection that = (GeometryCollection)o;
        return this.geometries.equals(that.geometries);
    }
    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + this.geometries.hashCode();
        return result;
    }
    
    @Override
    public String toString() {
        return "GeometryCollection{geometries=" + this.geometries + ((this.getCoordinateReferenceSystem() == null) ? "" : (", coordinateReferenceSystem=" + this.getCoordinateReferenceSystem())) + '}';
    }
}
