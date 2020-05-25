package com.mongodb;

import com.mongodb.assertions.*;

public enum ReadConcernLevel
{
    LOCAL("local"), 
    MAJORITY("majority"), 
    LINEARIZABLE("linearizable");
    
    private final String value;
    
    private ReadConcernLevel(final String readConcernLevel) {
        this.value = readConcernLevel;
    }
    
    public String getValue() {
        return this.value;
    }
    
    public static ReadConcernLevel fromString(final String readConcernLevel) {
        Assertions.notNull("readConcernLevel", readConcernLevel);
        for (final ReadConcernLevel level : values()) {
            if (readConcernLevel.equalsIgnoreCase(level.value)) {
                return level;
            }
        }
        throw new IllegalArgumentException(String.format("'%s' is not a valid readConcernLevel", readConcernLevel));
    }
}
