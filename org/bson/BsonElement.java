package org.bson;

public class BsonElement
{
    private final String name;
    private final BsonValue value;
    
    public BsonElement(final String name, final BsonValue value) {
        this.name = name;
        this.value = value;
    }
    
    public String getName() {
        return this.name;
    }
    
    public BsonValue getValue() {
        return this.value;
    }
}
