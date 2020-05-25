package org.bson;

public class BsonString extends BsonValue implements Comparable<BsonString>
{
    private final String value;
    
    public BsonString(final String value) {
        if (value == null) {
            throw new IllegalArgumentException("Value can not be null");
        }
        this.value = value;
    }
    
    @Override
    public int compareTo(final BsonString o) {
        return this.value.compareTo(o.value);
    }
    
    @Override
    public BsonType getBsonType() {
        return BsonType.STRING;
    }
    
    public String getValue() {
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
        final BsonString that = (BsonString)o;
        return this.value.equals(that.value);
    }
    
    @Override
    public int hashCode() {
        return this.value.hashCode();
    }
    
    @Override
    public String toString() {
        return "BsonString{value='" + this.value + '\'' + '}';
    }
}
