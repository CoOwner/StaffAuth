package org.bson;

import org.bson.types.*;

public final class BsonInt32 extends BsonNumber implements Comparable<BsonInt32>
{
    private final int value;
    
    public BsonInt32(final int value) {
        this.value = value;
    }
    
    @Override
    public int compareTo(final BsonInt32 o) {
        return (this.value < o.value) ? -1 : ((this.value == o.value) ? 0 : 1);
    }
    
    @Override
    public BsonType getBsonType() {
        return BsonType.INT32;
    }
    
    public int getValue() {
        return this.value;
    }
    
    @Override
    public int intValue() {
        return this.value;
    }
    
    @Override
    public long longValue() {
        return this.value;
    }
    
    @Override
    public Decimal128 decimal128Value() {
        return new Decimal128(this.value);
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
        final BsonInt32 bsonInt32 = (BsonInt32)o;
        return this.value == bsonInt32.value;
    }
    
    @Override
    public int hashCode() {
        return this.value;
    }
    
    @Override
    public String toString() {
        return "BsonInt32{value=" + this.value + '}';
    }
}
