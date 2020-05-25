package com.mongodb.connection;

import org.bson.types.*;
import com.mongodb.assertions.*;

public final class ClusterId
{
    private final String value;
    private final String description;
    
    public ClusterId() {
        this(null);
    }
    
    public ClusterId(final String description) {
        this.value = new ObjectId().toHexString();
        this.description = description;
    }
    
    ClusterId(final String value, final String description) {
        this.value = Assertions.notNull("value", value);
        this.description = description;
    }
    
    public String getValue() {
        return this.value;
    }
    
    public String getDescription() {
        return this.description;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final ClusterId clusterId = (ClusterId)o;
        if (!this.value.equals(clusterId.value)) {
            return false;
        }
        if (this.description != null) {
            if (this.description.equals(clusterId.description)) {
                return true;
            }
        }
        else if (clusterId.description == null) {
            return true;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int result = this.value.hashCode();
        result = 31 * result + ((this.description != null) ? this.description.hashCode() : 0);
        return result;
    }
    
    @Override
    public String toString() {
        return "ClusterId{value='" + this.value + '\'' + ", description='" + this.description + '\'' + '}';
    }
}
