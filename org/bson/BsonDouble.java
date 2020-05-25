package org.bson;

import org.bson.types.*;
import java.math.*;

public class BsonDouble extends BsonNumber implements Comparable<BsonDouble>
{
    private final double value;
    
    public BsonDouble(final double value) {
        this.value = value;
    }
    
    @Override
    public int compareTo(final BsonDouble o) {
        return Double.compare(this.value, o.value);
    }
    
    @Override
    public BsonType getBsonType() {
        return BsonType.DOUBLE;
    }
    
    public double getValue() {
        return this.value;
    }
    
    @Override
    public int intValue() {
        return (int)this.value;
    }
    
    @Override
    public long longValue() {
        return (long)this.value;
    }
    
    @Override
    public Decimal128 decimal128Value() {
        if (Double.isNaN(this.value)) {
            return Decimal128.NaN;
        }
        if (Double.isInfinite(this.value)) {
            return (this.value > 0.0) ? Decimal128.POSITIVE_INFINITY : Decimal128.NEGATIVE_INFINITY;
        }
        return new Decimal128(new BigDecimal(this.value));
    }
    
    @Override
    public double doubleValue() {
        return this.value;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final BsonDouble that = (BsonDouble)o;
        return Double.compare(that.value, this.value) == 0;
    }
    
    @Override
    public int hashCode() {
        final long temp = Double.doubleToLongBits(this.value);
        return (int)(temp ^ temp >>> 32);
    }
    
    @Override
    public String toString() {
        return "BsonDouble{value=" + this.value + '}';
    }
}
