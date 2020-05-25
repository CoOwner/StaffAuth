package com.mongodb.connection;

import java.io.*;
import java.util.concurrent.*;
import com.mongodb.async.*;

interface ConnectionPool extends Closeable
{
    InternalConnection get();
    
    InternalConnection get(final long p0, final TimeUnit p1);
    
    void getAsync(final SingleResultCallback<InternalConnection> p0);
    
    void invalidate();
    
    void close();
}
