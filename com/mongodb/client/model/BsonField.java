package com.mongodb.client.model;

import org.bson.conversions.*;
import com.mongodb.assertions.*;

public final class BsonField
{
    private final String name;
    private final Bson value;
    
    public BsonField(final String name, final Bson value) {
        this.name = Assertions.notNull("name", name);
        this.value = Assertions.notNull("value", value);
    }
    
    public String getName() {
        return this.name;
    }
    
    public Bson getValue() {
        return this.value;
    }
    
    @Override
    public String toString() {
        return "Field{name='" + this.name + '\'' + ", value=" + this.value + '}';
    }
}
