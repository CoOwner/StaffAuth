package com.mongodb.internal.connection;

import java.util.concurrent.*;

interface Pool<T>
{
    T get();
    
    T get(final long p0, final TimeUnit p1);
    
    void release(final T p0);
    
    void close();
    
    void release(final T p0, final boolean p1);
}
