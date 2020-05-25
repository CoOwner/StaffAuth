package org.bson;

public final class BsonUndefined extends BsonValue
{
    @Override
    public BsonType getBsonType() {
        return BsonType.UNDEFINED;
    }
    
    @Override
    public boolean equals(final Object o) {
        return this == o || (o != null && this.getClass() == o.getClass());
    }
    
    @Override
    public int hashCode() {
        return 0;
    }
}
