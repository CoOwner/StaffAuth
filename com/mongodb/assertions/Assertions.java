package com.mongodb.assertions;

import com.mongodb.async.*;

public final class Assertions
{
    public static <T> T notNull(final String name, final T value) {
        if (value == null) {
            throw new IllegalArgumentException(name + " can not be null");
        }
        return value;
    }
    
    public static <T> T notNull(final String name, final T value, final SingleResultCallback<?> callback) {
        if (value == null) {
            final IllegalArgumentException exception = new IllegalArgumentException(name + " can not be null");
            callback.onResult(null, exception);
            throw exception;
        }
        return value;
    }
    
    public static void isTrue(final String name, final boolean condition) {
        if (!condition) {
            throw new IllegalStateException("state should be: " + name);
        }
    }
    
    public static void isTrue(final String name, final boolean condition, final SingleResultCallback<?> callback) {
        if (!condition) {
            final IllegalStateException exception = new IllegalStateException("state should be: " + name);
            callback.onResult(null, exception);
            throw exception;
        }
    }
    
    public static void isTrueArgument(final String name, final boolean condition) {
        if (!condition) {
            throw new IllegalArgumentException("state should be: " + name);
        }
    }
    
    private Assertions() {
    }
}
