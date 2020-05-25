package com.mongodb.connection;

import java.io.*;
import com.mongodb.selector.*;
import com.mongodb.async.*;

public interface Cluster extends Closeable
{
    ClusterSettings getSettings();
    
    ClusterDescription getDescription();
    
    Server selectServer(final ServerSelector p0);
    
    void selectServerAsync(final ServerSelector p0, final SingleResultCallback<Server> p1);
    
    void close();
    
    boolean isClosed();
}
