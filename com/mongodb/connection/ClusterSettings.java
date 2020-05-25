package com.mongodb.connection;

import com.mongodb.selector.*;
import com.mongodb.event.*;
import java.util.concurrent.*;
import com.mongodb.assertions.*;
import com.mongodb.annotations.*;
import java.util.*;
import com.mongodb.*;

@Immutable
public final class ClusterSettings
{
    private final List<ServerAddress> hosts;
    private final ClusterConnectionMode mode;
    private final ClusterType requiredClusterType;
    private final String requiredReplicaSetName;
    private final ServerSelector serverSelector;
    private final String description;
    private final long serverSelectionTimeoutMS;
    private final int maxWaitQueueSize;
    private final List<ClusterListener> clusterListeners;
    
    public static Builder builder() {
        return new Builder();
    }
    
    public String getDescription() {
        return this.description;
    }
    
    public List<ServerAddress> getHosts() {
        return this.hosts;
    }
    
    public ClusterConnectionMode getMode() {
        return this.mode;
    }
    
    public ClusterType getRequiredClusterType() {
        return this.requiredClusterType;
    }
    
    public String getRequiredReplicaSetName() {
        return this.requiredReplicaSetName;
    }
    
    public ServerSelector getServerSelector() {
        return this.serverSelector;
    }
    
    public long getServerSelectionTimeout(final TimeUnit timeUnit) {
        return timeUnit.convert(this.serverSelectionTimeoutMS, TimeUnit.MILLISECONDS);
    }
    
    public int getMaxWaitQueueSize() {
        return this.maxWaitQueueSize;
    }
    
    public List<ClusterListener> getClusterListeners() {
        return Collections.unmodifiableList((List<? extends ClusterListener>)this.clusterListeners);
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final ClusterSettings that = (ClusterSettings)o;
        if (this.maxWaitQueueSize != that.maxWaitQueueSize) {
            return false;
        }
        if (this.serverSelectionTimeoutMS != that.serverSelectionTimeoutMS) {
            return false;
        }
        Label_0089: {
            if (this.description != null) {
                if (this.description.equals(that.description)) {
                    break Label_0089;
                }
            }
            else if (that.description == null) {
                break Label_0089;
            }
            return false;
        }
        if (!this.hosts.equals(that.hosts)) {
            return false;
        }
        if (this.mode != that.mode) {
            return false;
        }
        if (this.requiredClusterType != that.requiredClusterType) {
            return false;
        }
        Label_0166: {
            if (this.requiredReplicaSetName != null) {
                if (this.requiredReplicaSetName.equals(that.requiredReplicaSetName)) {
                    break Label_0166;
                }
            }
            else if (that.requiredReplicaSetName == null) {
                break Label_0166;
            }
            return false;
        }
        if (this.serverSelector != null) {
            if (this.serverSelector.equals(that.serverSelector)) {
                return this.clusterListeners.equals(that.clusterListeners);
            }
        }
        else if (that.serverSelector == null) {
            return this.clusterListeners.equals(that.clusterListeners);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int result = this.hosts.hashCode();
        result = 31 * result + this.mode.hashCode();
        result = 31 * result + this.requiredClusterType.hashCode();
        result = 31 * result + ((this.requiredReplicaSetName != null) ? this.requiredReplicaSetName.hashCode() : 0);
        result = 31 * result + ((this.serverSelector != null) ? this.serverSelector.hashCode() : 0);
        result = 31 * result + ((this.description != null) ? this.description.hashCode() : 0);
        result = 31 * result + (int)(this.serverSelectionTimeoutMS ^ this.serverSelectionTimeoutMS >>> 32);
        result = 31 * result + this.maxWaitQueueSize;
        result = 31 * result + this.clusterListeners.hashCode();
        return result;
    }
    
    @Override
    public String toString() {
        return "{hosts=" + this.hosts + ", mode=" + this.mode + ", requiredClusterType=" + this.requiredClusterType + ", requiredReplicaSetName='" + this.requiredReplicaSetName + '\'' + ", serverSelector='" + this.serverSelector + '\'' + ", clusterListeners='" + this.clusterListeners + '\'' + ", serverSelectionTimeout='" + this.serverSelectionTimeoutMS + " ms" + '\'' + ", maxWaitQueueSize=" + this.maxWaitQueueSize + ", description='" + this.description + '\'' + '}';
    }
    
    public String getShortDescription() {
        return "{hosts=" + this.hosts + ", mode=" + this.mode + ", requiredClusterType=" + this.requiredClusterType + ", serverSelectionTimeout='" + this.serverSelectionTimeoutMS + " ms" + '\'' + ", maxWaitQueueSize=" + this.maxWaitQueueSize + ((this.requiredReplicaSetName == null) ? "" : (", requiredReplicaSetName='" + this.requiredReplicaSetName + '\'')) + ((this.description == null) ? "" : (", description='" + this.description + '\'')) + '}';
    }
    
    private ClusterSettings(final Builder builder) {
        Assertions.notNull("hosts", builder.hosts);
        Assertions.isTrueArgument("hosts size > 0", builder.hosts.size() > 0);
        if (builder.hosts.size() > 1 && builder.requiredClusterType == ClusterType.STANDALONE) {
            throw new IllegalArgumentException("Multiple hosts cannot be specified when using ClusterType.STANDALONE.");
        }
        if (builder.mode == ClusterConnectionMode.SINGLE && builder.hosts.size() > 1) {
            throw new IllegalArgumentException("Can not directly connect to more than one server");
        }
        if (builder.requiredReplicaSetName != null) {
            if (builder.requiredClusterType == ClusterType.UNKNOWN) {
                builder.requiredClusterType = ClusterType.REPLICA_SET;
            }
            else if (builder.requiredClusterType != ClusterType.REPLICA_SET) {
                throw new IllegalArgumentException("When specifying a replica set name, only ClusterType.UNKNOWN and ClusterType.REPLICA_SET are valid.");
            }
        }
        this.description = builder.description;
        this.hosts = builder.hosts;
        this.mode = builder.mode;
        this.requiredReplicaSetName = builder.requiredReplicaSetName;
        this.requiredClusterType = builder.requiredClusterType;
        this.serverSelector = builder.serverSelector;
        this.serverSelectionTimeoutMS = builder.serverSelectionTimeoutMS;
        this.maxWaitQueueSize = builder.maxWaitQueueSize;
        this.clusterListeners = builder.clusterListeners;
    }
    
    @NotThreadSafe
    public static final class Builder
    {
        private List<ServerAddress> hosts;
        private ClusterConnectionMode mode;
        private ClusterType requiredClusterType;
        private String requiredReplicaSetName;
        private ServerSelector serverSelector;
        private String description;
        private long serverSelectionTimeoutMS;
        private int maxWaitQueueSize;
        private final List<ClusterListener> clusterListeners;
        
        private Builder() {
            this.mode = ClusterConnectionMode.MULTIPLE;
            this.requiredClusterType = ClusterType.UNKNOWN;
            this.serverSelectionTimeoutMS = TimeUnit.MILLISECONDS.convert(30L, TimeUnit.SECONDS);
            this.maxWaitQueueSize = 500;
            this.clusterListeners = new ArrayList<ClusterListener>();
        }
        
        public Builder description(final String description) {
            this.description = description;
            return this;
        }
        
        public Builder hosts(final List<ServerAddress> hosts) {
            Assertions.notNull("hosts", hosts);
            if (hosts.isEmpty()) {
                throw new IllegalArgumentException("hosts list may not be empty");
            }
            final Set<ServerAddress> hostsSet = new LinkedHashSet<ServerAddress>(hosts.size());
            for (final ServerAddress host : hosts) {
                Assertions.notNull("host", host);
                hostsSet.add(new ServerAddress(host.getHost(), host.getPort()));
            }
            this.hosts = Collections.unmodifiableList((List<? extends ServerAddress>)new ArrayList<ServerAddress>(hostsSet));
            return this;
        }
        
        public Builder mode(final ClusterConnectionMode mode) {
            this.mode = Assertions.notNull("mode", mode);
            return this;
        }
        
        public Builder requiredReplicaSetName(final String requiredReplicaSetName) {
            this.requiredReplicaSetName = requiredReplicaSetName;
            return this;
        }
        
        public Builder requiredClusterType(final ClusterType requiredClusterType) {
            this.requiredClusterType = Assertions.notNull("requiredClusterType", requiredClusterType);
            return this;
        }
        
        public Builder serverSelector(final ServerSelector serverSelector) {
            this.serverSelector = serverSelector;
            return this;
        }
        
        public Builder serverSelectionTimeout(final long serverSelectionTimeout, final TimeUnit timeUnit) {
            this.serverSelectionTimeoutMS = TimeUnit.MILLISECONDS.convert(serverSelectionTimeout, timeUnit);
            return this;
        }
        
        public Builder maxWaitQueueSize(final int maxWaitQueueSize) {
            this.maxWaitQueueSize = maxWaitQueueSize;
            return this;
        }
        
        public Builder addClusterListener(final ClusterListener clusterListener) {
            Assertions.notNull("clusterListener", clusterListener);
            this.clusterListeners.add(clusterListener);
            return this;
        }
        
        public Builder applyConnectionString(final ConnectionString connectionString) {
            if (connectionString.getHosts().size() == 1 && connectionString.getRequiredReplicaSetName() == null) {
                this.mode(ClusterConnectionMode.SINGLE).hosts(Collections.singletonList(new ServerAddress(connectionString.getHosts().get(0))));
            }
            else {
                final List<ServerAddress> seedList = new ArrayList<ServerAddress>();
                for (final String cur : connectionString.getHosts()) {
                    seedList.add(new ServerAddress(cur));
                }
                this.mode(ClusterConnectionMode.MULTIPLE).hosts(seedList);
            }
            this.requiredReplicaSetName(connectionString.getRequiredReplicaSetName());
            final int maxSize = (connectionString.getMaxConnectionPoolSize() != null) ? connectionString.getMaxConnectionPoolSize() : 100;
            final int waitQueueMultiple = (connectionString.getThreadsAllowedToBlockForConnectionMultiplier() != null) ? connectionString.getThreadsAllowedToBlockForConnectionMultiplier() : 5;
            this.maxWaitQueueSize(waitQueueMultiple * maxSize);
            if (connectionString.getServerSelectionTimeout() != null) {
                this.serverSelectionTimeout(connectionString.getServerSelectionTimeout(), TimeUnit.MILLISECONDS);
            }
            return this;
        }
        
        public ClusterSettings build() {
            return new ClusterSettings(this, null);
        }
    }
}
