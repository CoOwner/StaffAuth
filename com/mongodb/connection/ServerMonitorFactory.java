package com.mongodb.connection;

interface ServerMonitorFactory
{
    ServerMonitor create(final ChangeListener<ServerDescription> p0);
}
