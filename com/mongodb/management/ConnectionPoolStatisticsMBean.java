package com.mongodb.management;

public interface ConnectionPoolStatisticsMBean
{
    String getHost();
    
    int getPort();
    
    int getMinSize();
    
    int getMaxSize();
    
    int getSize();
    
    int getCheckedOutCount();
    
    int getWaitQueueSize();
}
