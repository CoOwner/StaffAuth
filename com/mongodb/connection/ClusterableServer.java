package com.mongodb.connection;

interface ClusterableServer extends Server
{
    void invalidate();
    
    void close();
    
    boolean isClosed();
    
    void connect();
}
