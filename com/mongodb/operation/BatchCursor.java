package com.mongodb.operation;

import java.util.*;
import java.io.*;
import com.mongodb.annotations.*;
import com.mongodb.*;

@NotThreadSafe
public interface BatchCursor<T> extends Iterator<List<T>>, Closeable
{
    void close();
    
    boolean hasNext();
    
    List<T> next();
    
    void setBatchSize(final int p0);
    
    int getBatchSize();
    
    List<T> tryNext();
    
    ServerCursor getServerCursor();
    
    ServerAddress getServerAddress();
}
