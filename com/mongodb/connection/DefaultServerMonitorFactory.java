package com.mongodb.connection;

import com.mongodb.assertions.*;

class DefaultServerMonitorFactory implements ServerMonitorFactory
{
    private final ServerId serverId;
    private final ServerSettings settings;
    private final InternalConnectionFactory internalConnectionFactory;
    private final ConnectionPool connectionPool;
    
    DefaultServerMonitorFactory(final ServerId serverId, final ServerSettings settings, final InternalConnectionFactory internalConnectionFactory, final ConnectionPool connectionPool) {
        this.serverId = Assertions.notNull("serverId", serverId);
        this.settings = Assertions.notNull("settings", settings);
        this.internalConnectionFactory = Assertions.notNull("internalConnectionFactory", internalConnectionFactory);
        this.connectionPool = Assertions.notNull("connectionPool", connectionPool);
    }
    
    @Override
    public ServerMonitor create(final ChangeListener<ServerDescription> serverStateListener) {
        return new DefaultServerMonitor(this.serverId, this.settings, serverStateListener, this.internalConnectionFactory, this.connectionPool);
    }
}
