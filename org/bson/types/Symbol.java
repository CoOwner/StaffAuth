package org.bson.types;

import java.io.*;

public class Symbol implements Serializable
{
    private static final long serialVersionUID = 1326269319883146072L;
    private final String symbol;
    
    public Symbol(final String symbol) {
        this.symbol = symbol;
    }
    
    public String getSymbol() {
        return this.symbol;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final Symbol symbol1 = (Symbol)o;
        return this.symbol.equals(symbol1.symbol);
    }
    
    @Override
    public int hashCode() {
        return this.symbol.hashCode();
    }
    
    @Override
    public String toString() {
        return this.symbol;
    }
}
