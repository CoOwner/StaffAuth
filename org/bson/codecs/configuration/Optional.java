package org.bson.codecs.configuration;

import java.util.*;

abstract class Optional<T>
{
    private static final Optional<Object> NONE;
    
    public static <T> Optional<T> empty() {
        return (Optional<T>)Optional.NONE;
    }
    
    public static <T> Optional<T> of(final T it) {
        if (it == null) {
            return (Optional<T>)Optional.NONE;
        }
        return new Some<T>(it);
    }
    
    public abstract T get();
    
    public abstract boolean isEmpty();
    
    @Override
    public String toString() {
        return "None";
    }
    
    public boolean isDefined() {
        return !this.isEmpty();
    }
    
    static {
        NONE = new Optional<Object>() {
            @Override
            public Object get() {
                throw new NoSuchElementException(".get call on None!");
            }
            
            @Override
            public boolean isEmpty() {
                return true;
            }
        };
    }
    
    public static class Some<T> extends Optional<T>
    {
        private final T value;
        
        Some(final T value) {
            this.value = value;
        }
        
        @Override
        public T get() {
            return this.value;
        }
        
        @Override
        public boolean isEmpty() {
            return false;
        }
        
        @Override
        public String toString() {
            return String.format("Some(%s)", this.value);
        }
    }
}
