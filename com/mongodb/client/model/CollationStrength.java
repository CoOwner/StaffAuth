package com.mongodb.client.model;

public enum CollationStrength
{
    PRIMARY(1), 
    SECONDARY(2), 
    TERTIARY(3), 
    QUATERNARY(4), 
    IDENTICAL(5);
    
    private final int intRepresentation;
    
    private CollationStrength(final int intRepresentation) {
        this.intRepresentation = intRepresentation;
    }
    
    public int getIntRepresentation() {
        return this.intRepresentation;
    }
    
    public static CollationStrength fromInt(final int intRepresentation) {
        switch (intRepresentation) {
            case 1: {
                return CollationStrength.PRIMARY;
            }
            case 2: {
                return CollationStrength.SECONDARY;
            }
            case 3: {
                return CollationStrength.TERTIARY;
            }
            case 4: {
                return CollationStrength.QUATERNARY;
            }
            case 5: {
                return CollationStrength.IDENTICAL;
            }
            default: {
                throw new IllegalArgumentException(intRepresentation + " is not a valid collation strength");
            }
        }
    }
}
