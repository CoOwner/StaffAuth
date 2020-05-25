package com.mongodb.client.model.geojson;

import com.mongodb.assertions.*;
import java.util.*;

public final class PolygonCoordinates
{
    private final List<Position> exterior;
    private final List<List<Position>> holes;
    
    public PolygonCoordinates(final List<Position> exterior, final List<Position>... holes) {
        Assertions.notNull("exteriorRing", exterior);
        Assertions.isTrueArgument("ring contains only non-null positions", !exterior.contains(null));
        Assertions.isTrueArgument("ring must contain at least four positions", exterior.size() >= 4);
        Assertions.isTrueArgument("first and last position must be the same", exterior.get(0).equals(exterior.get(exterior.size() - 1)));
        this.exterior = Collections.unmodifiableList((List<? extends Position>)exterior);
        final List<List<Position>> holesList = new ArrayList<List<Position>>(holes.length);
        for (final List<Position> hole : holes) {
            Assertions.notNull("interiorRing", hole);
            Assertions.isTrueArgument("ring contains only non-null positions", !hole.contains(null));
            Assertions.isTrueArgument("ring must contain at least four positions", hole.size() >= 4);
            Assertions.isTrueArgument("first and last position must be the same", hole.get(0).equals(hole.get(hole.size() - 1)));
            holesList.add(Collections.unmodifiableList((List<? extends Position>)hole));
        }
        this.holes = Collections.unmodifiableList((List<? extends List<Position>>)holesList);
    }
    
    public List<Position> getExterior() {
        return this.exterior;
    }
    
    public List<List<Position>> getHoles() {
        return this.holes;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final PolygonCoordinates that = (PolygonCoordinates)o;
        return this.exterior.equals(that.exterior) && this.holes.equals(that.holes);
    }
    
    @Override
    public int hashCode() {
        int result = this.exterior.hashCode();
        result = 31 * result + this.holes.hashCode();
        return result;
    }
    
    @Override
    public String toString() {
        return "PolygonCoordinates{exterior=" + this.exterior + (this.holes.isEmpty() ? "" : (", holes=" + this.holes)) + '}';
    }
}
