package com.mongodb.connection;

import java.util.*;
import com.mongodb.assertions.*;
import com.mongodb.async.*;
import com.mongodb.diagnostics.logging.*;
import com.mongodb.*;
import com.mongodb.internal.async.*;
import com.mongodb.event.*;

class DefaultServer implements ClusterableServer
{
    private static final Logger LOGGER;
    private final ServerId serverId;
    private final ConnectionPool connectionPool;
    private final ClusterConnectionMode clusterConnectionMode;
    private final ConnectionFactory connectionFactory;
    private final ServerMonitor serverMonitor;
    private final ChangeListener<ServerDescription> serverStateListener;
    private final ServerListener serverListener;
    private final CommandListener commandListener;
    private volatile ServerDescription description;
    private volatile boolean isClosed;
    
    public DefaultServer(final ServerId serverId, final ClusterConnectionMode clusterConnectionMode, final ConnectionPool connectionPool, final ConnectionFactory connectionFactory, final ServerMonitorFactory serverMonitorFactory, final List<ServerListener> serverListeners, final CommandListener commandListener) {
        Assertions.notNull("serverListeners", serverListeners);
        this.serverListener = (serverListeners.isEmpty() ? new NoOpServerListener() : new ServerEventMulticaster(serverListeners));
        this.commandListener = commandListener;
        Assertions.notNull("serverAddress", serverId);
        Assertions.notNull("serverMonitorFactory", serverMonitorFactory);
        this.clusterConnectionMode = Assertions.notNull("clusterConnectionMode", clusterConnectionMode);
        this.connectionFactory = Assertions.notNull("connectionFactory", connectionFactory);
        this.connectionPool = Assertions.notNull("connectionPool", connectionPool);
        this.serverStateListener = new DefaultServerStateListener();
        this.serverId = serverId;
        this.serverListener.serverOpening(new ServerOpeningEvent(this.serverId));
        this.description = ServerDescription.builder().state(ServerConnectionState.CONNECTING).address(serverId.getAddress()).build();
        (this.serverMonitor = serverMonitorFactory.create(this.serverStateListener)).start();
    }
    
    @Override
    public Connection getConnection() {
        Assertions.isTrue("open", !this.isClosed());
        try {
            return this.connectionFactory.create(this.connectionPool.get(), new DefaultServerProtocolExecutor(), this.clusterConnectionMode);
        }
        catch (MongoSecurityException e) {
            this.invalidate();
            throw e;
        }
    }
    
    @Override
    public void getConnectionAsync(final SingleResultCallback<AsyncConnection> callback) {
        Assertions.isTrue("open", !this.isClosed());
        this.connectionPool.getAsync(new SingleResultCallback<InternalConnection>() {
            @Override
            public void onResult(final InternalConnection result, final Throwable t) {
                if (t instanceof MongoSecurityException) {
                    DefaultServer.this.invalidate();
                }
                if (t != null) {
                    callback.onResult(null, t);
                }
                else {
                    callback.onResult(DefaultServer.this.connectionFactory.createAsync(result, new DefaultServerProtocolExecutor(), DefaultServer.this.clusterConnectionMode), null);
                }
            }
        });
    }
    
    @Override
    public ServerDescription getDescription() {
        Assertions.isTrue("open", !this.isClosed());
        return this.description;
    }
    
    @Override
    public void invalidate() {
        Assertions.isTrue("open", !this.isClosed());
        this.serverStateListener.stateChanged(new ChangeEvent<ServerDescription>(this.description, ServerDescription.builder().state(ServerConnectionState.CONNECTING).address(this.serverId.getAddress()).build()));
        this.connectionPool.invalidate();
        this.connect();
    }
    
    @Override
    public void close() {
        if (!this.isClosed()) {
            this.connectionPool.close();
            this.serverMonitor.close();
            this.isClosed = true;
            this.serverListener.serverClosed(new ServerClosedEvent(this.serverId));
        }
    }
    
    @Override
    public boolean isClosed() {
        return this.isClosed;
    }
    
    @Override
    public void connect() {
        this.serverMonitor.connect();
    }
    
    ConnectionPool getConnectionPool() {
        return this.connectionPool;
    }
    
    private void handleThrowable(final Throwable t) {
        if ((t instanceof MongoSocketException && !(t instanceof MongoSocketReadTimeoutException)) || t instanceof MongoNotPrimaryException || t instanceof MongoNodeIsRecoveringException) {
            this.invalidate();
        }
    }
    
    static {
        LOGGER = Loggers.getLogger("connection");
    }
    
    private class DefaultServerProtocolExecutor implements ProtocolExecutor
    {
        @Override
        public <T> T execute(final Protocol<T> protocol, final InternalConnection connection) {
            try {
                protocol.setCommandListener(DefaultServer.this.commandListener);
                return protocol.execute(connection);
            }
            catch (MongoException e) {
                DefaultServer.this.handleThrowable(e);
                throw e;
            }
        }
        
        @Override
        public <T> void executeAsync(final Protocol<T> protocol, final InternalConnection connection, final SingleResultCallback<T> callback) {
            protocol.setCommandListener(DefaultServer.this.commandListener);
            protocol.executeAsync(connection, ErrorHandlingResultCallback.errorHandlingCallback((SingleResultCallback<T>)new SingleResultCallback<T>() {
                @Override
                public void onResult(final T result, final Throwable t) {
                    if (t != null) {
                        DefaultServer.this.handleThrowable(t);
                    }
                    callback.onResult(result, t);
                }
            }, DefaultServer.LOGGER));
        }
    }
    
    private final class DefaultServerStateListener implements ChangeListener<ServerDescription>
    {
        @Override
        public void stateChanged(final ChangeEvent<ServerDescription> event) {
            final ServerDescription oldDescription = DefaultServer.this.description;
            DefaultServer.this.description = event.getNewValue();
            DefaultServer.this.serverListener.serverDescriptionChanged(new ServerDescriptionChangedEvent(DefaultServer.this.serverId, DefaultServer.this.description, oldDescription));
        }
    }
}
