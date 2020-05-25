package com.mongodb.client;

import java.util.*;
import java.io.*;
import com.mongodb.annotations.*;
import com.mongodb.*;

@NotThreadSafe
public interface MongoCursor<TResult> extends Iterator<TResult>, Closeable
{
    void close();
    
    boolean hasNext();
    
    TResult next();
    
    TResult tryNext();
    
    ServerCursor getServerCursor();
    
    ServerAddress getServerAddress();
}
