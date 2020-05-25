package org.bson;

import org.bson.types.*;
import org.bson.assertions.*;

public final class BsonDecimal128 extends BsonNumber
{
    private final Decimal128 value;
    
    public BsonDecimal128(final Decimal128 value) {
        Assertions.notNull("value", value);
        this.value = value;
    }
    
    @Override
    public BsonType getBsonType() {
        return BsonType.DECIMAL128;
    }
    
    public Decimal128 getValue() {
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
        final BsonDecimal128 that = (BsonDecimal128)o;
        return this.value.equals(that.value);
    }
    
    @Override
    public int hashCode() {
        return this.value.hashCode();
    }
    
    @Override
    public String toString() {
        return "BsonDecimal128{value=" + this.value + '}';
    }
    
    @Override
    public int intValue() {
        return this.value.bigDecimalValue().intValue();
    }
    
    @Override
    public long longValue() {
        return this.value.bigDecimalValue().longValue();
    }
    
    @Override
    public double doubleValue() {
        return this.value.bigDecimalValue().doubleValue();
    }
    
    @Override
    public Decimal128 decimal128Value() {
        return this.value;
    }
}
