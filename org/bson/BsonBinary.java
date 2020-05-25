package org.bson;

import java.util.*;

public class BsonBinary extends BsonValue
{
    private final byte type;
    private final byte[] data;
    
    public BsonBinary(final byte[] data) {
        this(BsonBinarySubType.BINARY, data);
    }
    
    public BsonBinary(final BsonBinarySubType type, final byte[] data) {
        if (type == null) {
            throw new IllegalArgumentException("type may not be null");
        }
        if (data == null) {
            throw new IllegalArgumentException("data may not be null");
        }
        this.type = type.getValue();
        this.data = data;
    }
    
    public BsonBinary(final byte type, final byte[] data) {
        if (data == null) {
            throw new IllegalArgumentException("data may not be null");
        }
        this.type = type;
        this.data = data;
    }
    
    @Override
    public BsonType getBsonType() {
        return BsonType.BINARY;
    }
    
    public byte getType() {
        return this.type;
    }
    
    public byte[] getData() {
        return this.data;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final BsonBinary that = (BsonBinary)o;
        return Arrays.equals(this.data, that.data) && this.type == that.type;
    }
    
    @Override
    public int hashCode() {
        int result = this.type;
        result = 31 * result + Arrays.hashCode(this.data);
        return result;
    }
    
    @Override
    public String toString() {
        return "BsonBinary{type=" + this.type + ", data=" + Arrays.toString(this.data) + '}';
    }
    
    static BsonBinary clone(final BsonBinary from) {
        return new BsonBinary(from.type, from.data.clone());
    }
}
