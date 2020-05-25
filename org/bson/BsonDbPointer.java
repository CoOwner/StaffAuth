package org.bson;

import org.bson.types.*;

public class BsonDbPointer extends BsonValue
{
    private final String namespace;
    private final ObjectId id;
    
    public BsonDbPointer(final String namespace, final ObjectId id) {
        if (namespace == null) {
            throw new IllegalArgumentException("namespace can not be null");
        }
        if (id == null) {
            throw new IllegalArgumentException("id can not be null");
        }
        this.namespace = namespace;
        this.id = id;
    }
    
    @Override
    public BsonType getBsonType() {
        return BsonType.DB_POINTER;
    }
    
    public String getNamespace() {
        return this.namespace;
    }
    
    public ObjectId getId() {
        return this.id;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final BsonDbPointer dbPointer = (BsonDbPointer)o;
        return this.id.equals(dbPointer.id) && this.namespace.equals(dbPointer.namespace);
    }
    
    @Override
    public int hashCode() {
        int result = this.namespace.hashCode();
        result = 31 * result + this.id.hashCode();
        return result;
    }
    
    @Override
    public String toString() {
        return "BsonDbPointer{namespace='" + this.namespace + '\'' + ", id=" + this.id + '}';
    }
}
