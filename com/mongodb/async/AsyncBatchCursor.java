package com.mongodb.async;

import java.io.*;
import java.util.*;

public interface AsyncBatchCursor<T> extends Closeable
{
    void next(final SingleResultCallback<List<T>> p0);
    
    void setBatchSize(final int p0);
    
    int getBatchSize();
    
    boolean isClosed();
    
    void close();
}
