package com.mongodb.client.model.geojson;

import com.mongodb.annotations.*;
import com.mongodb.assertions.*;
import java.util.*;

@Immutable
public final class Position
{
    private final List<Double> values;
    
    public Position(final List<Double> values) {
        Assertions.notNull("values", values);
        Assertions.isTrueArgument("value contains only non-null elements", !values.contains(null));
        Assertions.isTrueArgument("value must contain at least two elements", values.size() >= 2);
        this.values = Collections.unmodifiableList((List<? extends Double>)values);
    }
    
    public Position(final double first, final double second, final double... remaining) {
        final List<Double> values = new ArrayList<Double>();
        values.add(first);
        values.add(second);
        for (final double cur : remaining) {
            values.add(cur);
        }
        this.values = Collections.unmodifiableList((List<? extends Double>)values);
    }
    
    public List<Double> getValues() {
        return this.values;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final Position that = (Position)o;
        return this.values.equals(that.values);
    }
    
    @Override
    public int hashCode() {
        return this.values.hashCode();
    }
    
    @Override
    public String toString() {
        return "Position{values=" + this.values + '}';
    }
}
