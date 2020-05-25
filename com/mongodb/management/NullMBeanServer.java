package com.mongodb.management;

public class NullMBeanServer implements MBeanServer
{
    @Override
    public void unregisterMBean(final String mBeanName) {
    }
    
    @Override
    public void registerMBean(final Object mBean, final String mBeanName) {
    }
}
