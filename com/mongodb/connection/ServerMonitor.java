package com.mongodb.connection;

interface ServerMonitor
{
    void start();
    
    void connect();
    
    void close();
}
