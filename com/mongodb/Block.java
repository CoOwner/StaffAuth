package com.mongodb;

public interface Block<T>
{
    void apply(final T p0);
}
