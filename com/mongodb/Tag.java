package com.mongodb;

import com.mongodb.annotations.*;
import com.mongodb.assertions.*;

@Immutable
public final class Tag
{
    private final String name;
    private final String value;
    
    public Tag(final String name, final String value) {
        this.name = Assertions.notNull("name", name);
        this.value = Assertions.notNull("value", value);
    }
    
    public String getName() {
        return this.name;
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
        final Tag that = (Tag)o;
        return this.name.equals(that.name) && this.value.equals(that.value);
    }
    
    @Override
    public int hashCode() {
        int result = this.name.hashCode();
        result = 31 * result + this.value.hashCode();
        return result;
    }
    
    @Override
    public String toString() {
        return "Tag{name='" + this.name + '\'' + ", value='" + this.value + '\'' + '}';
    }
}
