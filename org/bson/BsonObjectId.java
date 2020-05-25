package org.bson;

import org.bson.types.*;

public class BsonObjectId extends BsonValue implements Comparable<BsonObjectId>
{
    private final ObjectId value;
    
    public BsonObjectId() {
        this(new ObjectId());
    }
    
    public BsonObjectId(final ObjectId value) {
        if (value == null) {
            throw new IllegalArgumentException("value may not be null");
        }
        this.value = value;
    }
    
    public ObjectId getValue() {
        return this.value;
    }
    
    @Override
    public BsonType getBsonType() {
        return BsonType.OBJECT_ID;
    }
    
    @Override
    public int compareTo(final BsonObjectId o) {
        return this.value.compareTo(o.value);
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final BsonObjectId that = (BsonObjectId)o;
        return this.value.equals(that.value);
    }
    
    @Override
    public int hashCode() {
        return this.value.hashCode();
    }
    
    @Override
    public String toString() {
        return "BsonObjectId{value=" + this.value.toHexString() + '}';
    }
}
