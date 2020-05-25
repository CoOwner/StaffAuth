package com.mongodb;

public interface Function<T, R>
{
    R apply(final T p0);
}
