package com.mongodb.connection;

import java.io.*;
import java.util.*;
import org.bson.*;
import com.mongodb.*;

public interface Stream extends BufferProvider
{
    void open() throws IOException;
    
    void openAsync(final AsyncCompletionHandler<Void> p0);
    
    void write(final List<ByteBuf> p0) throws IOException;
    
    ByteBuf read(final int p0) throws IOException;
    
    void writeAsync(final List<ByteBuf> p0, final AsyncCompletionHandler<Void> p1);
    
    void readAsync(final int p0, final AsyncCompletionHandler<ByteBuf> p1);
    
    ServerAddress getAddress();
    
    void close();
    
    boolean isClosed();
}
