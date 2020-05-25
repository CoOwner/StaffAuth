package com.mongodb.client.model.geojson;

import com.mongodb.annotations.*;
import com.mongodb.assertions.*;

@Immutable
public final class NamedCoordinateReferenceSystem extends CoordinateReferenceSystem
{
    public static final NamedCoordinateReferenceSystem EPSG_4326;
    public static final NamedCoordinateReferenceSystem CRS_84;
    public static final NamedCoordinateReferenceSystem EPSG_4326_STRICT_WINDING;
    private final String name;
    
    public NamedCoordinateReferenceSystem(final String name) {
        this.name = Assertions.notNull("name", name);
    }
    
    @Override
    public CoordinateReferenceSystemType getType() {
        return CoordinateReferenceSystemType.NAME;
    }
    
    public String getName() {
        return this.name;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final NamedCoordinateReferenceSystem that = (NamedCoordinateReferenceSystem)o;
        return this.name.equals(that.name);
    }
    
    @Override
    public int hashCode() {
        return this.name.hashCode();
    }
    
    @Override
    public String toString() {
        return "NamedCoordinateReferenceSystem{name='" + this.name + '\'' + '}';
    }
    
    static {
        EPSG_4326 = new NamedCoordinateReferenceSystem("EPSG:4326");
        CRS_84 = new NamedCoordinateReferenceSystem("urn:ogc:def:crs:OGC:1.3:CRS84");
        EPSG_4326_STRICT_WINDING = new NamedCoordinateReferenceSystem("urn:x-mongodb:crs:strictwinding:EPSG:4326");
    }
}
