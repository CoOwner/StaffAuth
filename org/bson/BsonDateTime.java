package org.bson;

public class BsonDateTime extends BsonValue implements Comparable<BsonDateTime>
{
    private final long value;
    
    public BsonDateTime(final long value) {
        this.value = value;
    }
    
    @Override
    public int compareTo(final BsonDateTime o) {
        return Long.valueOf(this.value).compareTo(o.value);
    }
    
    @Override
    public BsonType getBsonType() {
        return BsonType.DATE_TIME;
    }
    
    public long getValue() {
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
        final BsonDateTime that = (BsonDateTime)o;
        return this.value == that.value;
    }
    
    @Override
    public int hashCode() {
        return (int)(this.value ^ this.value >>> 32);
    }
    
    @Override
    public String toString() {
        return "BsonDateTime{value=" + this.value + '}';
    }
}
