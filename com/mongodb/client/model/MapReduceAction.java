package com.mongodb.client.model;

public enum MapReduceAction
{
    REPLACE("replace"), 
    MERGE("merge"), 
    REDUCE("reduce");
    
    private final String value;
    
    private MapReduceAction(final String value) {
        this.value = value;
    }
    
    public String getValue() {
        return this.value;
    }
}
