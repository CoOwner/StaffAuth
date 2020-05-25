package com.mongodb.connection;

import com.mongodb.client.*;
import com.mongodb.*;
import com.mongodb.event.*;
import java.util.*;

class DefaultClusterableServerFactory implements ClusterableServerFactory
{
    private final ClusterId clusterId;
    private final ClusterSettings clusterSettings;
    private final ServerSettings settings;
    private final ConnectionPoolSettings connectionPoolSettings;
    private final StreamFactory streamFactory;
    private final List<MongoCredential> credentialList;
    private final ConnectionPoolListener connectionPoolListener;
    private final ConnectionListener connectionListener;
    private final StreamFactory heartbeatStreamFactory;
    private final CommandListener commandListener;
    private final String applicationName;
    private final MongoDriverInformation mongoDriverInformation;
    
    public DefaultClusterableServerFactory(final ClusterId clusterId, final ClusterSettings clusterSettings, final ServerSettings settings, final ConnectionPoolSettings connectionPoolSettings, final StreamFactory streamFactory, final StreamFactory heartbeatStreamFactory, final List<MongoCredential> credentialList, final ConnectionListener connectionListener, final ConnectionPoolListener connectionPoolListener, final CommandListener commandListener, final String applicationName, final MongoDriverInformation mongoDriverInformation) {
        this.clusterId = clusterId;
        this.clusterSettings = clusterSettings;
        this.settings = settings;
        this.connectionPoolSettings = connectionPoolSettings;
        this.streamFactory = streamFactory;
        this.credentialList = credentialList;
        this.connectionPoolListener = connectionPoolListener;
        this.connectionListener = connectionListener;
        this.heartbeatStreamFactory = heartbeatStreamFactory;
        this.commandListener = commandListener;
        this.applicationName = applicationName;
        this.mongoDriverInformation = mongoDriverInformation;
    }
    
    @Override
    public ClusterableServer create(final ServerAddress serverAddress, final ServerListener serverListener) {
        final ConnectionPool connectionPool = new DefaultConnectionPool(new ServerId(this.clusterId, serverAddress), new InternalStreamConnectionFactory(this.streamFactory, this.credentialList, this.connectionListener, this.applicationName, this.mongoDriverInformation), this.connectionPoolSettings, this.connectionPoolListener);
        final ServerMonitorFactory serverMonitorFactory = new DefaultServerMonitorFactory(new ServerId(this.clusterId, serverAddress), this.settings, new InternalStreamConnectionFactory(this.heartbeatStreamFactory, this.credentialList, this.connectionListener, this.applicationName, this.mongoDriverInformation), connectionPool);
        final List<ServerListener> serverListeners = new ArrayList<ServerListener>();
        if (serverListener != null) {
            serverListeners.add(serverListener);
        }
        serverListeners.addAll(this.settings.getServerListeners());
        return new DefaultServer(new ServerId(this.clusterId, serverAddress), this.clusterSettings.getMode(), connectionPool, new DefaultConnectionFactory(), serverMonitorFactory, serverListeners, this.commandListener);
    }
    
    @Override
    public ServerSettings getSettings() {
        return this.settings;
    }
}
