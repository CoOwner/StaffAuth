package com.mongodb.operation;

public enum OrderBy
{
    ASC(1), 
    DESC(-1);
    
    private final int intRepresentation;
    
    private OrderBy(final int intRepresentation) {
        this.intRepresentation = intRepresentation;
    }
    
    public int getIntRepresentation() {
        return this.intRepresentation;
    }
    
    public static OrderBy fromInt(final int intRepresentation) {
        switch (intRepresentation) {
            case 1: {
                return OrderBy.ASC;
            }
            case -1: {
                return OrderBy.DESC;
            }
            default: {
                throw new IllegalArgumentException(intRepresentation + " is not a valid index Order");
            }
        }
    }
}
