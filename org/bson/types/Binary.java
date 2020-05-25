package org.bson.types;

import java.io.*;
import org.bson.*;
import java.util.*;

public class Binary implements Serializable
{
    private static final long serialVersionUID = 7902997490338209467L;
    private final byte type;
    private final byte[] data;
    
    public Binary(final byte[] data) {
        this(BsonBinarySubType.BINARY, data);
    }
    
    public Binary(final BsonBinarySubType type, final byte[] data) {
        this(type.getValue(), data);
    }
    
    public Binary(final byte type, final byte[] data) {
        this.type = type;
        this.data = data.clone();
    }
    
    public byte getType() {
        return this.type;
    }
    
    public byte[] getData() {
        return this.data.clone();
    }
    
    public int length() {
        return this.data.length;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final Binary binary = (Binary)o;
        return this.type == binary.type && Arrays.equals(this.data, binary.data);
    }
    
    @Override
    public int hashCode() {
        int result = this.type;
        result = 31 * result + Arrays.hashCode(this.data);
        return result;
    }
}
