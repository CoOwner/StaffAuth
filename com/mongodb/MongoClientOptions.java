package com.mongodb;

import javax.net.*;
import org.bson.codecs.configuration.*;
import com.mongodb.connection.*;
import com.mongodb.event.*;
import java.util.concurrent.*;
import javax.net.ssl.*;
import com.mongodb.annotations.*;
import java.util.*;
import java.nio.charset.*;
import com.mongodb.assertions.*;

@Immutable
public class MongoClientOptions
{
    private static final SocketFactory DEFAULT_SSL_SOCKET_FACTORY;
    private static final SocketFactory DEFAULT_SOCKET_FACTORY;
    private final String description;
    private final String applicationName;
    private final ReadPreference readPreference;
    private final WriteConcern writeConcern;
    private final ReadConcern readConcern;
    private final CodecRegistry codecRegistry;
    private final int minConnectionsPerHost;
    private final int maxConnectionsPerHost;
    private final int threadsAllowedToBlockForConnectionMultiplier;
    private final int serverSelectionTimeout;
    private final int maxWaitTime;
    private final int maxConnectionIdleTime;
    private final int maxConnectionLifeTime;
    private final int connectTimeout;
    private final int socketTimeout;
    private final boolean socketKeepAlive;
    private final boolean sslEnabled;
    private final boolean sslInvalidHostNameAllowed;
    private final boolean alwaysUseMBeans;
    private final int heartbeatFrequency;
    private final int minHeartbeatFrequency;
    private final int heartbeatConnectTimeout;
    private final int heartbeatSocketTimeout;
    private final int localThreshold;
    private final String requiredReplicaSetName;
    private final DBDecoderFactory dbDecoderFactory;
    private final DBEncoderFactory dbEncoderFactory;
    private final SocketFactory socketFactory;
    private final boolean cursorFinalizerEnabled;
    private final ConnectionPoolSettings connectionPoolSettings;
    private final SocketSettings socketSettings;
    private final ServerSettings serverSettings;
    private final SocketSettings heartbeatSocketSettings;
    private final SslSettings sslSettings;
    private final List<CommandListener> commandListeners;
    private final List<ClusterListener> clusterListeners;
    private final List<ServerListener> serverListeners;
    private final List<ServerMonitorListener> serverMonitorListeners;
    
    private MongoClientOptions(final Builder builder) {
        this.description = builder.description;
        this.applicationName = builder.applicationName;
        this.minConnectionsPerHost = builder.minConnectionsPerHost;
        this.maxConnectionsPerHost = builder.maxConnectionsPerHost;
        this.threadsAllowedToBlockForConnectionMultiplier = builder.threadsAllowedToBlockForConnectionMultiplier;
        this.serverSelectionTimeout = builder.serverSelectionTimeout;
        this.maxWaitTime = builder.maxWaitTime;
        this.maxConnectionIdleTime = builder.maxConnectionIdleTime;
        this.maxConnectionLifeTime = builder.maxConnectionLifeTime;
        this.connectTimeout = builder.connectTimeout;
        this.socketTimeout = builder.socketTimeout;
        this.socketKeepAlive = builder.socketKeepAlive;
        this.readPreference = builder.readPreference;
        this.writeConcern = builder.writeConcern;
        this.readConcern = builder.readConcern;
        this.codecRegistry = builder.codecRegistry;
        this.sslEnabled = builder.sslEnabled;
        this.sslInvalidHostNameAllowed = builder.sslInvalidHostNameAllowed;
        this.alwaysUseMBeans = builder.alwaysUseMBeans;
        this.heartbeatFrequency = builder.heartbeatFrequency;
        this.minHeartbeatFrequency = builder.minHeartbeatFrequency;
        this.heartbeatConnectTimeout = builder.heartbeatConnectTimeout;
        this.heartbeatSocketTimeout = builder.heartbeatSocketTimeout;
        this.localThreshold = builder.localThreshold;
        this.requiredReplicaSetName = builder.requiredReplicaSetName;
        this.dbDecoderFactory = builder.dbDecoderFactory;
        this.dbEncoderFactory = builder.dbEncoderFactory;
        this.socketFactory = builder.socketFactory;
        this.cursorFinalizerEnabled = builder.cursorFinalizerEnabled;
        this.commandListeners = builder.commandListeners;
        this.clusterListeners = builder.clusterListeners;
        this.serverListeners = builder.serverListeners;
        this.serverMonitorListeners = builder.serverMonitorListeners;
        this.connectionPoolSettings = ConnectionPoolSettings.builder().minSize(this.getMinConnectionsPerHost()).maxSize(this.getConnectionsPerHost()).maxWaitQueueSize(this.getThreadsAllowedToBlockForConnectionMultiplier() * this.getConnectionsPerHost()).maxWaitTime(this.getMaxWaitTime(), TimeUnit.MILLISECONDS).maxConnectionIdleTime(this.getMaxConnectionIdleTime(), TimeUnit.MILLISECONDS).maxConnectionLifeTime(this.getMaxConnectionLifeTime(), TimeUnit.MILLISECONDS).build();
        this.socketSettings = SocketSettings.builder().connectTimeout(this.getConnectTimeout(), TimeUnit.MILLISECONDS).readTimeout(this.getSocketTimeout(), TimeUnit.MILLISECONDS).keepAlive(this.isSocketKeepAlive()).build();
        this.heartbeatSocketSettings = SocketSettings.builder().connectTimeout(this.getHeartbeatConnectTimeout(), TimeUnit.MILLISECONDS).readTimeout(this.getHeartbeatSocketTimeout(), TimeUnit.MILLISECONDS).keepAlive(this.isSocketKeepAlive()).build();
        final ServerSettings.Builder serverSettingsBuilder = ServerSettings.builder().heartbeatFrequency(this.getHeartbeatFrequency(), TimeUnit.MILLISECONDS).minHeartbeatFrequency(this.getMinHeartbeatFrequency(), TimeUnit.MILLISECONDS);
        for (final ServerListener cur : this.serverListeners) {
            serverSettingsBuilder.addServerListener(cur);
        }
        for (final ServerMonitorListener cur2 : this.serverMonitorListeners) {
            serverSettingsBuilder.addServerMonitorListener(cur2);
        }
        this.serverSettings = serverSettingsBuilder.build();
        try {
            this.sslSettings = SslSettings.builder().enabled(this.sslEnabled).invalidHostNameAllowed(this.sslInvalidHostNameAllowed).build();
        }
        catch (MongoInternalException e) {
            throw new MongoInternalException("By default, SSL connections are only supported on Java 7 or later.  If the application must run on Java 6, you must set the MongoClientOptions.sslInvalidHostNameAllowed property to false");
        }
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static Builder builder(final MongoClientOptions options) {
        return new Builder(options);
    }
    
    public String getDescription() {
        return this.description;
    }
    
    public String getApplicationName() {
        return this.applicationName;
    }
    
    public int getConnectionsPerHost() {
        return this.maxConnectionsPerHost;
    }
    
    public int getMinConnectionsPerHost() {
        return this.minConnectionsPerHost;
    }
    
    public int getThreadsAllowedToBlockForConnectionMultiplier() {
        return this.threadsAllowedToBlockForConnectionMultiplier;
    }
    
    public int getServerSelectionTimeout() {
        return this.serverSelectionTimeout;
    }
    
    public int getMaxWaitTime() {
        return this.maxWaitTime;
    }
    
    public int getMaxConnectionIdleTime() {
        return this.maxConnectionIdleTime;
    }
    
    public int getMaxConnectionLifeTime() {
        return this.maxConnectionLifeTime;
    }
    
    public int getConnectTimeout() {
        return this.connectTimeout;
    }
    
    public int getSocketTimeout() {
        return this.socketTimeout;
    }
    
    public boolean isSocketKeepAlive() {
        return this.socketKeepAlive;
    }
    
    public int getHeartbeatFrequency() {
        return this.heartbeatFrequency;
    }
    
    public int getMinHeartbeatFrequency() {
        return this.minHeartbeatFrequency;
    }
    
    public int getHeartbeatConnectTimeout() {
        return this.heartbeatConnectTimeout;
    }
    
    public int getHeartbeatSocketTimeout() {
        return this.heartbeatSocketTimeout;
    }
    
    public int getLocalThreshold() {
        return this.localThreshold;
    }
    
    public String getRequiredReplicaSetName() {
        return this.requiredReplicaSetName;
    }
    
    public boolean isSslEnabled() {
        return this.sslEnabled;
    }
    
    public boolean isSslInvalidHostNameAllowed() {
        return this.sslInvalidHostNameAllowed;
    }
    
    public ReadPreference getReadPreference() {
        return this.readPreference;
    }
    
    public WriteConcern getWriteConcern() {
        return this.writeConcern;
    }
    
    public ReadConcern getReadConcern() {
        return this.readConcern;
    }
    
    public CodecRegistry getCodecRegistry() {
        return this.codecRegistry;
    }
    
    public List<CommandListener> getCommandListeners() {
        return Collections.unmodifiableList((List<? extends CommandListener>)this.commandListeners);
    }
    
    public List<ClusterListener> getClusterListeners() {
        return Collections.unmodifiableList((List<? extends ClusterListener>)this.clusterListeners);
    }
    
    public List<ServerListener> getServerListeners() {
        return Collections.unmodifiableList((List<? extends ServerListener>)this.serverListeners);
    }
    
    public List<ServerMonitorListener> getServerMonitorListeners() {
        return Collections.unmodifiableList((List<? extends ServerMonitorListener>)this.serverMonitorListeners);
    }
    
    public DBDecoderFactory getDbDecoderFactory() {
        return this.dbDecoderFactory;
    }
    
    public DBEncoderFactory getDbEncoderFactory() {
        return this.dbEncoderFactory;
    }
    
    public boolean isAlwaysUseMBeans() {
        return this.alwaysUseMBeans;
    }
    
    public SocketFactory getSocketFactory() {
        if (this.socketFactory != null) {
            return this.socketFactory;
        }
        if (this.getSslSettings().isEnabled()) {
            return MongoClientOptions.DEFAULT_SSL_SOCKET_FACTORY;
        }
        return MongoClientOptions.DEFAULT_SOCKET_FACTORY;
    }
    
    public boolean isCursorFinalizerEnabled() {
        return this.cursorFinalizerEnabled;
    }
    
    ConnectionPoolSettings getConnectionPoolSettings() {
        return this.connectionPoolSettings;
    }
    
    SocketSettings getSocketSettings() {
        return this.socketSettings;
    }
    
    ServerSettings getServerSettings() {
        return this.serverSettings;
    }
    
    SocketSettings getHeartbeatSocketSettings() {
        return this.heartbeatSocketSettings;
    }
    
    SslSettings getSslSettings() {
        return this.sslSettings;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final MongoClientOptions that = (MongoClientOptions)o;
        if (this.localThreshold != that.localThreshold) {
            return false;
        }
        if (this.alwaysUseMBeans != that.alwaysUseMBeans) {
            return false;
        }
        if (this.connectTimeout != that.connectTimeout) {
            return false;
        }
        if (this.cursorFinalizerEnabled != that.cursorFinalizerEnabled) {
            return false;
        }
        if (this.minHeartbeatFrequency != that.minHeartbeatFrequency) {
            return false;
        }
        if (this.heartbeatConnectTimeout != that.heartbeatConnectTimeout) {
            return false;
        }
        if (this.heartbeatFrequency != that.heartbeatFrequency) {
            return false;
        }
        if (this.heartbeatSocketTimeout != that.heartbeatSocketTimeout) {
            return false;
        }
        if (this.maxConnectionIdleTime != that.maxConnectionIdleTime) {
            return false;
        }
        if (this.maxConnectionLifeTime != that.maxConnectionLifeTime) {
            return false;
        }
        if (this.maxConnectionsPerHost != that.maxConnectionsPerHost) {
            return false;
        }
        if (this.serverSelectionTimeout != that.serverSelectionTimeout) {
            return false;
        }
        if (this.maxWaitTime != that.maxWaitTime) {
            return false;
        }
        if (this.minConnectionsPerHost != that.minConnectionsPerHost) {
            return false;
        }
        if (this.socketKeepAlive != that.socketKeepAlive) {
            return false;
        }
        if (this.socketTimeout != that.socketTimeout) {
            return false;
        }
        if (this.sslEnabled != that.sslEnabled) {
            return false;
        }
        if (this.sslInvalidHostNameAllowed != that.sslInvalidHostNameAllowed) {
            return false;
        }
        if (this.threadsAllowedToBlockForConnectionMultiplier != that.threadsAllowedToBlockForConnectionMultiplier) {
            return false;
        }
        Label_0309: {
            if (this.dbDecoderFactory != null) {
                if (this.dbDecoderFactory.equals(that.dbDecoderFactory)) {
                    break Label_0309;
                }
            }
            else if (that.dbDecoderFactory == null) {
                break Label_0309;
            }
            return false;
        }
        Label_0342: {
            if (this.dbEncoderFactory != null) {
                if (this.dbEncoderFactory.equals(that.dbEncoderFactory)) {
                    break Label_0342;
                }
            }
            else if (that.dbEncoderFactory == null) {
                break Label_0342;
            }
            return false;
        }
        Label_0375: {
            if (this.description != null) {
                if (this.description.equals(that.description)) {
                    break Label_0375;
                }
            }
            else if (that.description == null) {
                break Label_0375;
            }
            return false;
        }
        Label_0408: {
            if (this.applicationName != null) {
                if (this.applicationName.equals(that.applicationName)) {
                    break Label_0408;
                }
            }
            else if (that.applicationName == null) {
                break Label_0408;
            }
            return false;
        }
        if (!this.readPreference.equals(that.readPreference)) {
            return false;
        }
        if (!this.writeConcern.equals(that.writeConcern)) {
            return false;
        }
        if (!this.readConcern.equals(that.readConcern)) {
            return false;
        }
        if (!this.codecRegistry.equals(that.codecRegistry)) {
            return false;
        }
        if (!this.commandListeners.equals(that.commandListeners)) {
            return false;
        }
        if (!this.clusterListeners.equals(that.clusterListeners)) {
            return false;
        }
        if (!this.serverListeners.equals(that.serverListeners)) {
            return false;
        }
        if (!this.serverMonitorListeners.equals(that.serverMonitorListeners)) {
            return false;
        }
        Label_0577: {
            if (this.requiredReplicaSetName != null) {
                if (this.requiredReplicaSetName.equals(that.requiredReplicaSetName)) {
                    break Label_0577;
                }
            }
            else if (that.requiredReplicaSetName == null) {
                break Label_0577;
            }
            return false;
        }
        if (this.socketFactory != null) {
            if (this.socketFactory.equals(that.socketFactory)) {
                return true;
            }
        }
        else if (that.socketFactory == null) {
            return true;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int result = (this.description != null) ? this.description.hashCode() : 0;
        result = 31 * result + ((this.applicationName != null) ? this.applicationName.hashCode() : 0);
        result = 31 * result + this.readPreference.hashCode();
        result = 31 * result + this.writeConcern.hashCode();
        result = 31 * result + ((this.readConcern != null) ? this.readConcern.hashCode() : 0);
        result = 31 * result + this.codecRegistry.hashCode();
        result = 31 * result + this.commandListeners.hashCode();
        result = 31 * result + this.clusterListeners.hashCode();
        result = 31 * result + this.serverListeners.hashCode();
        result = 31 * result + this.serverMonitorListeners.hashCode();
        result = 31 * result + this.minConnectionsPerHost;
        result = 31 * result + this.maxConnectionsPerHost;
        result = 31 * result + this.threadsAllowedToBlockForConnectionMultiplier;
        result = 31 * result + this.serverSelectionTimeout;
        result = 31 * result + this.maxWaitTime;
        result = 31 * result + this.maxConnectionIdleTime;
        result = 31 * result + this.maxConnectionLifeTime;
        result = 31 * result + this.connectTimeout;
        result = 31 * result + this.socketTimeout;
        result = 31 * result + (this.socketKeepAlive ? 1 : 0);
        result = 31 * result + (this.sslEnabled ? 1 : 0);
        result = 31 * result + (this.sslInvalidHostNameAllowed ? 1 : 0);
        result = 31 * result + (this.alwaysUseMBeans ? 1 : 0);
        result = 31 * result + this.heartbeatFrequency;
        result = 31 * result + this.minHeartbeatFrequency;
        result = 31 * result + this.heartbeatConnectTimeout;
        result = 31 * result + this.heartbeatSocketTimeout;
        result = 31 * result + this.localThreshold;
        result = 31 * result + ((this.requiredReplicaSetName != null) ? this.requiredReplicaSetName.hashCode() : 0);
        result = 31 * result + ((this.dbDecoderFactory != null) ? this.dbDecoderFactory.hashCode() : 0);
        result = 31 * result + ((this.dbEncoderFactory != null) ? this.dbEncoderFactory.hashCode() : 0);
        result = 31 * result + (this.cursorFinalizerEnabled ? 1 : 0);
        result = 31 * result + ((this.socketFactory != null) ? this.socketFactory.hashCode() : 0);
        return result;
    }
    
    @Override
    public String toString() {
        return "MongoClientOptions{description='" + this.description + '\'' + ", applicationName='" + this.applicationName + '\'' + ", readPreference=" + this.readPreference + ", writeConcern=" + this.writeConcern + ", readConcern=" + this.readConcern + ", codecRegistry=" + this.codecRegistry + ", commandListeners=" + this.commandListeners + ", clusterListeners=" + this.clusterListeners + ", serverListeners=" + this.serverListeners + ", serverMonitorListeners=" + this.serverMonitorListeners + ", minConnectionsPerHost=" + this.minConnectionsPerHost + ", maxConnectionsPerHost=" + this.maxConnectionsPerHost + ", threadsAllowedToBlockForConnectionMultiplier=" + this.threadsAllowedToBlockForConnectionMultiplier + ", serverSelectionTimeout=" + this.serverSelectionTimeout + ", maxWaitTime=" + this.maxWaitTime + ", maxConnectionIdleTime=" + this.maxConnectionIdleTime + ", maxConnectionLifeTime=" + this.maxConnectionLifeTime + ", connectTimeout=" + this.connectTimeout + ", socketTimeout=" + this.socketTimeout + ", socketKeepAlive=" + this.socketKeepAlive + ", sslEnabled=" + this.sslEnabled + ", sslInvalidHostNamesAllowed=" + this.sslInvalidHostNameAllowed + ", alwaysUseMBeans=" + this.alwaysUseMBeans + ", heartbeatFrequency=" + this.heartbeatFrequency + ", minHeartbeatFrequency=" + this.minHeartbeatFrequency + ", heartbeatConnectTimeout=" + this.heartbeatConnectTimeout + ", heartbeatSocketTimeout=" + this.heartbeatSocketTimeout + ", localThreshold=" + this.localThreshold + ", requiredReplicaSetName='" + this.requiredReplicaSetName + '\'' + ", dbDecoderFactory=" + this.dbDecoderFactory + ", dbEncoderFactory=" + this.dbEncoderFactory + ", socketFactory=" + this.socketFactory + ", cursorFinalizerEnabled=" + this.cursorFinalizerEnabled + ", connectionPoolSettings=" + this.connectionPoolSettings + ", socketSettings=" + this.socketSettings + ", serverSettings=" + this.serverSettings + ", heartbeatSocketSettings=" + this.heartbeatSocketSettings + '}';
    }
    
    static {
        DEFAULT_SSL_SOCKET_FACTORY = SSLSocketFactory.getDefault();
        DEFAULT_SOCKET_FACTORY = SocketFactory.getDefault();
    }
    
    @NotThreadSafe
    public static class Builder
    {
        private String description;
        private String applicationName;
        private ReadPreference readPreference;
        private WriteConcern writeConcern;
        private ReadConcern readConcern;
        private CodecRegistry codecRegistry;
        private final List<CommandListener> commandListeners;
        private final List<ClusterListener> clusterListeners;
        private final List<ServerListener> serverListeners;
        private final List<ServerMonitorListener> serverMonitorListeners;
        private int minConnectionsPerHost;
        private int maxConnectionsPerHost;
        private int threadsAllowedToBlockForConnectionMultiplier;
        private int serverSelectionTimeout;
        private int maxWaitTime;
        private int maxConnectionIdleTime;
        private int maxConnectionLifeTime;
        private int connectTimeout;
        private int socketTimeout;
        private boolean socketKeepAlive;
        private boolean sslEnabled;
        private boolean sslInvalidHostNameAllowed;
        private boolean alwaysUseMBeans;
        private int heartbeatFrequency;
        private int minHeartbeatFrequency;
        private int heartbeatConnectTimeout;
        private int heartbeatSocketTimeout;
        private int localThreshold;
        private String requiredReplicaSetName;
        private DBDecoderFactory dbDecoderFactory;
        private DBEncoderFactory dbEncoderFactory;
        private SocketFactory socketFactory;
        private boolean cursorFinalizerEnabled;
        
        public Builder() {
            this.readPreference = ReadPreference.primary();
            this.writeConcern = WriteConcern.ACKNOWLEDGED;
            this.readConcern = ReadConcern.DEFAULT;
            this.codecRegistry = MongoClient.getDefaultCodecRegistry();
            this.commandListeners = new ArrayList<CommandListener>();
            this.clusterListeners = new ArrayList<ClusterListener>();
            this.serverListeners = new ArrayList<ServerListener>();
            this.serverMonitorListeners = new ArrayList<ServerMonitorListener>();
            this.maxConnectionsPerHost = 100;
            this.threadsAllowedToBlockForConnectionMultiplier = 5;
            this.serverSelectionTimeout = 30000;
            this.maxWaitTime = 120000;
            this.connectTimeout = 10000;
            this.socketTimeout = 0;
            this.socketKeepAlive = false;
            this.sslEnabled = false;
            this.sslInvalidHostNameAllowed = false;
            this.alwaysUseMBeans = false;
            this.heartbeatFrequency = 10000;
            this.minHeartbeatFrequency = 500;
            this.heartbeatConnectTimeout = 20000;
            this.heartbeatSocketTimeout = 20000;
            this.localThreshold = 15;
            this.dbDecoderFactory = DefaultDBDecoder.FACTORY;
            this.dbEncoderFactory = DefaultDBEncoder.FACTORY;
            this.cursorFinalizerEnabled = true;
            this.heartbeatFrequency(Integer.parseInt(System.getProperty("com.mongodb.updaterIntervalMS", "10000")));
            this.minHeartbeatFrequency(Integer.parseInt(System.getProperty("com.mongodb.updaterIntervalNoMasterMS", "500")));
            this.heartbeatConnectTimeout(Integer.parseInt(System.getProperty("com.mongodb.updaterConnectTimeoutMS", "20000")));
            this.heartbeatSocketTimeout(Integer.parseInt(System.getProperty("com.mongodb.updaterSocketTimeoutMS", "20000")));
            this.localThreshold(Integer.parseInt(System.getProperty("com.mongodb.slaveAcceptableLatencyMS", "15")));
        }
        
        public Builder(final MongoClientOptions options) {
            this.readPreference = ReadPreference.primary();
            this.writeConcern = WriteConcern.ACKNOWLEDGED;
            this.readConcern = ReadConcern.DEFAULT;
            this.codecRegistry = MongoClient.getDefaultCodecRegistry();
            this.commandListeners = new ArrayList<CommandListener>();
            this.clusterListeners = new ArrayList<ClusterListener>();
            this.serverListeners = new ArrayList<ServerListener>();
            this.serverMonitorListeners = new ArrayList<ServerMonitorListener>();
            this.maxConnectionsPerHost = 100;
            this.threadsAllowedToBlockForConnectionMultiplier = 5;
            this.serverSelectionTimeout = 30000;
            this.maxWaitTime = 120000;
            this.connectTimeout = 10000;
            this.socketTimeout = 0;
            this.socketKeepAlive = false;
            this.sslEnabled = false;
            this.sslInvalidHostNameAllowed = false;
            this.alwaysUseMBeans = false;
            this.heartbeatFrequency = 10000;
            this.minHeartbeatFrequency = 500;
            this.heartbeatConnectTimeout = 20000;
            this.heartbeatSocketTimeout = 20000;
            this.localThreshold = 15;
            this.dbDecoderFactory = DefaultDBDecoder.FACTORY;
            this.dbEncoderFactory = DefaultDBEncoder.FACTORY;
            this.cursorFinalizerEnabled = true;
            this.description = options.getDescription();
            this.applicationName = options.getApplicationName();
            this.minConnectionsPerHost = options.getMinConnectionsPerHost();
            this.maxConnectionsPerHost = options.getConnectionsPerHost();
            this.threadsAllowedToBlockForConnectionMultiplier = options.getThreadsAllowedToBlockForConnectionMultiplier();
            this.serverSelectionTimeout = options.getServerSelectionTimeout();
            this.maxWaitTime = options.getMaxWaitTime();
            this.maxConnectionIdleTime = options.getMaxConnectionIdleTime();
            this.maxConnectionLifeTime = options.getMaxConnectionLifeTime();
            this.connectTimeout = options.getConnectTimeout();
            this.socketTimeout = options.getSocketTimeout();
            this.socketKeepAlive = options.isSocketKeepAlive();
            this.readPreference = options.getReadPreference();
            this.writeConcern = options.getWriteConcern();
            this.readConcern = options.getReadConcern();
            this.codecRegistry = options.getCodecRegistry();
            this.sslEnabled = options.isSslEnabled();
            this.sslInvalidHostNameAllowed = options.isSslInvalidHostNameAllowed();
            this.alwaysUseMBeans = options.isAlwaysUseMBeans();
            this.heartbeatFrequency = options.getHeartbeatFrequency();
            this.minHeartbeatFrequency = options.getMinHeartbeatFrequency();
            this.heartbeatConnectTimeout = options.getHeartbeatConnectTimeout();
            this.heartbeatSocketTimeout = options.getHeartbeatSocketTimeout();
            this.localThreshold = options.getLocalThreshold();
            this.requiredReplicaSetName = options.getRequiredReplicaSetName();
            this.dbDecoderFactory = options.getDbDecoderFactory();
            this.dbEncoderFactory = options.getDbEncoderFactory();
            this.socketFactory = options.socketFactory;
            this.cursorFinalizerEnabled = options.isCursorFinalizerEnabled();
            this.commandListeners.addAll(options.getCommandListeners());
            this.clusterListeners.addAll(options.getClusterListeners());
            this.serverListeners.addAll(options.getServerListeners());
            this.serverMonitorListeners.addAll(options.getServerMonitorListeners());
        }
        
        public Builder description(final String description) {
            this.description = description;
            return this;
        }
        
        public Builder applicationName(final String applicationName) {
            if (applicationName != null) {
                Assertions.isTrueArgument("applicationName UTF-8 encoding length <= 128", applicationName.getBytes(Charset.forName("UTF-8")).length <= 128);
            }
            this.applicationName = applicationName;
            return this;
        }
        
        public Builder minConnectionsPerHost(final int minConnectionsPerHost) {
            Assertions.isTrueArgument("minConnectionsPerHost must be >= 0", minConnectionsPerHost >= 0);
            this.minConnectionsPerHost = minConnectionsPerHost;
            return this;
        }
        
        public Builder connectionsPerHost(final int connectionsPerHost) {
            Assertions.isTrueArgument("connectionPerHost must be > 0", connectionsPerHost > 0);
            this.maxConnectionsPerHost = connectionsPerHost;
            return this;
        }
        
        public Builder threadsAllowedToBlockForConnectionMultiplier(final int threadsAllowedToBlockForConnectionMultiplier) {
            Assertions.isTrueArgument("threadsAllowedToBlockForConnectionMultiplier must be > 0", threadsAllowedToBlockForConnectionMultiplier > 0);
            this.threadsAllowedToBlockForConnectionMultiplier = threadsAllowedToBlockForConnectionMultiplier;
            return this;
        }
        
        public Builder serverSelectionTimeout(final int serverSelectionTimeout) {
            this.serverSelectionTimeout = serverSelectionTimeout;
            return this;
        }
        
        public Builder maxWaitTime(final int maxWaitTime) {
            this.maxWaitTime = maxWaitTime;
            return this;
        }
        
        public Builder maxConnectionIdleTime(final int maxConnectionIdleTime) {
            this.maxConnectionIdleTime = maxConnectionIdleTime;
            return this;
        }
        
        public Builder maxConnectionLifeTime(final int maxConnectionLifeTime) {
            this.maxConnectionLifeTime = maxConnectionLifeTime;
            return this;
        }
        
        public Builder connectTimeout(final int connectTimeout) {
            Assertions.isTrueArgument("connectTimeout must be >= 0", connectTimeout >= 0);
            this.connectTimeout = connectTimeout;
            return this;
        }
        
        public Builder socketTimeout(final int socketTimeout) {
            this.socketTimeout = socketTimeout;
            return this;
        }
        
        public Builder socketKeepAlive(final boolean socketKeepAlive) {
            this.socketKeepAlive = socketKeepAlive;
            return this;
        }
        
        public Builder sslEnabled(final boolean sslEnabled) {
            this.sslEnabled = sslEnabled;
            return this;
        }
        
        public Builder sslInvalidHostNameAllowed(final boolean sslInvalidHostNameAllowed) {
            this.sslInvalidHostNameAllowed = sslInvalidHostNameAllowed;
            return this;
        }
        
        public Builder readPreference(final ReadPreference readPreference) {
            this.readPreference = Assertions.notNull("readPreference", readPreference);
            return this;
        }
        
        public Builder writeConcern(final WriteConcern writeConcern) {
            this.writeConcern = Assertions.notNull("writeConcern", writeConcern);
            return this;
        }
        
        public Builder readConcern(final ReadConcern readConcern) {
            this.readConcern = Assertions.notNull("readConcern", readConcern);
            return this;
        }
        
        public Builder codecRegistry(final CodecRegistry codecRegistry) {
            this.codecRegistry = Assertions.notNull("codecRegistry", codecRegistry);
            return this;
        }
        
        public Builder addCommandListener(final CommandListener commandListener) {
            Assertions.notNull("commandListener", commandListener);
            this.commandListeners.add(commandListener);
            return this;
        }
        
        public Builder addClusterListener(final ClusterListener clusterListener) {
            Assertions.notNull("clusterListener", clusterListener);
            this.clusterListeners.add(clusterListener);
            return this;
        }
        
        public Builder addServerListener(final ServerListener serverListener) {
            Assertions.notNull("serverListener", serverListener);
            this.serverListeners.add(serverListener);
            return this;
        }
        
        public Builder addServerMonitorListener(final ServerMonitorListener serverMonitorListener) {
            Assertions.notNull("serverMonitorListener", serverMonitorListener);
            this.serverMonitorListeners.add(serverMonitorListener);
            return this;
        }
        
        public Builder socketFactory(final SocketFactory socketFactory) {
            this.socketFactory = socketFactory;
            return this;
        }
        
        public Builder cursorFinalizerEnabled(final boolean cursorFinalizerEnabled) {
            this.cursorFinalizerEnabled = cursorFinalizerEnabled;
            return this;
        }
        
        public Builder alwaysUseMBeans(final boolean alwaysUseMBeans) {
            this.alwaysUseMBeans = alwaysUseMBeans;
            return this;
        }
        
        public Builder dbDecoderFactory(final DBDecoderFactory dbDecoderFactory) {
            if (dbDecoderFactory == null) {
                throw new IllegalArgumentException("null is not a legal value");
            }
            this.dbDecoderFactory = dbDecoderFactory;
            return this;
        }
        
        public Builder dbEncoderFactory(final DBEncoderFactory dbEncoderFactory) {
            if (dbEncoderFactory == null) {
                throw new IllegalArgumentException("null is not a legal value");
            }
            this.dbEncoderFactory = dbEncoderFactory;
            return this;
        }
        
        public Builder heartbeatFrequency(final int heartbeatFrequency) {
            Assertions.isTrueArgument("heartbeatFrequency must be > 0", heartbeatFrequency > 0);
            this.heartbeatFrequency = heartbeatFrequency;
            return this;
        }
        
        public Builder minHeartbeatFrequency(final int minHeartbeatFrequency) {
            Assertions.isTrueArgument("minHeartbeatFrequency must be > 0", minHeartbeatFrequency > 0);
            this.minHeartbeatFrequency = minHeartbeatFrequency;
            return this;
        }
        
        public Builder heartbeatConnectTimeout(final int connectTimeout) {
            this.heartbeatConnectTimeout = connectTimeout;
            return this;
        }
        
        public Builder heartbeatSocketTimeout(final int socketTimeout) {
            this.heartbeatSocketTimeout = socketTimeout;
            return this;
        }
        
        public Builder localThreshold(final int localThreshold) {
            Assertions.isTrueArgument("localThreshold must be >= 0", localThreshold >= 0);
            this.localThreshold = localThreshold;
            return this;
        }
        
        public Builder requiredReplicaSetName(final String requiredReplicaSetName) {
            this.requiredReplicaSetName = requiredReplicaSetName;
            return this;
        }
        
        public Builder legacyDefaults() {
            this.connectionsPerHost(10).writeConcern(WriteConcern.UNACKNOWLEDGED);
            return this;
        }
        
        public MongoClientOptions build() {
            return new MongoClientOptions(this, null);
        }
    }
}
