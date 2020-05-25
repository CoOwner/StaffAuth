package com.mongodb.management;

public interface MBeanServer
{
    void unregisterMBean(final String p0);
    
    void registerMBean(final Object p0, final String p1);
}
