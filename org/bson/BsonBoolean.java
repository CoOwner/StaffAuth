package org.bson;

public final class BsonBoolean extends BsonValue implements Comparable<BsonBoolean>
{
    private final boolean value;
    public static final BsonBoolean TRUE;
    public static final BsonBoolean FALSE;
    
    public static BsonBoolean valueOf(final boolean value) {
        return value ? BsonBoolean.TRUE : BsonBoolean.FALSE;
    }
    
    public BsonBoolean(final boolean value) {
        this.value = value;
    }
    
    @Override
    public int compareTo(final BsonBoolean o) {
        return Boolean.valueOf(this.value).compareTo(Boolean.valueOf(o.value));
    }
    
    @Override
    public BsonType getBsonType() {
        return BsonType.BOOLEAN;
    }
    
    public boolean getValue() {
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
        final BsonBoolean that = (BsonBoolean)o;
        return this.value == that.value;
    }
    
    @Override
    public int hashCode() {
        return this.value ? 1 : 0;
    }
    
    @Override
    public String toString() {
        return "BsonBoolean{value=" + this.value + '}';
    }
    
    static {
        TRUE = new BsonBoolean(true);
        FALSE = new BsonBoolean(false);
    }
}
