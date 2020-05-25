package com.mongodb;

import javax.net.*;
import java.util.concurrent.*;

@Deprecated
public class MongoOptions
{
    public String description;
    public int connectionsPerHost;
    public int threadsAllowedToBlockForConnectionMultiplier;
    public int maxWaitTime;
    public int connectTimeout;
    public int socketTimeout;
    public boolean socketKeepAlive;
    public ReadPreference readPreference;
    public DBDecoderFactory dbDecoderFactory;
    public DBEncoderFactory dbEncoderFactory;
    public boolean safe;
    public int w;
    public int wtimeout;
    public boolean fsync;
    public boolean j;
    public SocketFactory socketFactory;
    public boolean cursorFinalizerEnabled;
    public WriteConcern writeConcern;
    public boolean alwaysUseMBeans;
    String requiredReplicaSetName;
    
    @Deprecated
    public MongoOptions() {
        this.reset();
    }
    
    @Deprecated
    public MongoOptions(final MongoClientOptions options) {
        this.connectionsPerHost = options.getConnectionsPerHost();
        this.threadsAllowedToBlockForConnectionMultiplier = options.getThreadsAllowedToBlockForConnectionMultiplier();
        this.maxWaitTime = options.getMaxWaitTime();
        this.connectTimeout = options.getConnectTimeout();
        this.socketFactory = options.getSocketFactory();
        this.socketTimeout = options.getSocketTimeout();
        this.socketKeepAlive = options.isSocketKeepAlive();
        this.readPreference = options.getReadPreference();
        this.dbDecoderFactory = options.getDbDecoderFactory();
        this.dbEncoderFactory = options.getDbEncoderFactory();
        this.description = options.getDescription();
        this.writeConcern = options.getWriteConcern();
        this.alwaysUseMBeans = options.isAlwaysUseMBeans();
        this.requiredReplicaSetName = options.getRequiredReplicaSetName();
    }
    
    public void reset() {
        this.connectionsPerHost = 10;
        this.threadsAllowedToBlockForConnectionMultiplier = 5;
        this.maxWaitTime = 120000;
        this.connectTimeout = 10000;
        this.socketFactory = SocketFactory.getDefault();
        this.socketTimeout = 0;
        this.socketKeepAlive = false;
        this.readPreference = null;
        this.writeConcern = null;
        this.safe = false;
        this.w = 0;
        this.wtimeout = 0;
        this.fsync = false;
        this.j = false;
        this.dbDecoderFactory = DefaultDBDecoder.FACTORY;
        this.dbEncoderFactory = DefaultDBEncoder.FACTORY;
        this.description = null;
        this.cursorFinalizerEnabled = true;
        this.alwaysUseMBeans = false;
        this.requiredReplicaSetName = null;
    }
    
    public MongoOptions copy() {
        final MongoOptions m = new MongoOptions();
        m.connectionsPerHost = this.connectionsPerHost;
        m.threadsAllowedToBlockForConnectionMultiplier = this.threadsAllowedToBlockForConnectionMultiplier;
        m.maxWaitTime = this.maxWaitTime;
        m.connectTimeout = this.connectTimeout;
        m.socketFactory = this.socketFactory;
        m.socketTimeout = this.socketTimeout;
        m.socketKeepAlive = this.socketKeepAlive;
        m.readPreference = this.readPreference;
        m.writeConcern = this.writeConcern;
        m.safe = this.safe;
        m.w = this.w;
        m.wtimeout = this.wtimeout;
        m.fsync = this.fsync;
        m.j = this.j;
        m.dbDecoderFactory = this.dbDecoderFactory;
        m.dbEncoderFactory = this.dbEncoderFactory;
        m.description = this.description;
        m.cursorFinalizerEnabled = this.cursorFinalizerEnabled;
        m.alwaysUseMBeans = this.alwaysUseMBeans;
        m.requiredReplicaSetName = this.requiredReplicaSetName;
        return m;
    }
    
    MongoClientOptions toClientOptions() {
        final MongoClientOptions.Builder builder = MongoClientOptions.builder().requiredReplicaSetName(this.requiredReplicaSetName).connectionsPerHost(this.connectionsPerHost).connectTimeout(this.connectTimeout).dbDecoderFactory(this.dbDecoderFactory).dbEncoderFactory(this.dbEncoderFactory).description(this.description).maxWaitTime(this.maxWaitTime).socketFactory(this.socketFactory).socketKeepAlive(this.socketKeepAlive).socketTimeout(this.socketTimeout).threadsAllowedToBlockForConnectionMultiplier(this.threadsAllowedToBlockForConnectionMultiplier).cursorFinalizerEnabled(this.cursorFinalizerEnabled).alwaysUseMBeans(this.alwaysUseMBeans);
        builder.writeConcern(this.getWriteConcern());
        if (this.readPreference != null) {
            builder.readPreference(this.getReadPreference());
        }
        return builder.build();
    }
    
    public WriteConcern getWriteConcern() {
        WriteConcern retVal;
        if (this.writeConcern != null) {
            retVal = this.writeConcern;
        }
        else if (this.w != 0 || this.wtimeout != 0 || (this.fsync | this.j)) {
            retVal = WriteConcern.ACKNOWLEDGED;
            if (this.w != 0) {
                retVal = retVal.withW(this.w);
            }
            if (this.wtimeout != 0) {
                retVal = retVal.withWTimeout(this.wtimeout, TimeUnit.MILLISECONDS);
            }
            if (this.fsync) {
                retVal = retVal.withFsync(this.fsync);
            }
            if (this.j) {
                retVal = retVal.withJ(this.j);
            }
        }
        else if (this.safe) {
            retVal = WriteConcern.ACKNOWLEDGED;
        }
        else {
            retVal = WriteConcern.UNACKNOWLEDGED;
        }
        return retVal;
    }
    
    public void setWriteConcern(final WriteConcern writeConcern) {
        this.writeConcern = writeConcern;
    }
    
    public synchronized SocketFactory getSocketFactory() {
        return this.socketFactory;
    }
    
    public synchronized void setSocketFactory(final SocketFactory factory) {
        this.socketFactory = factory;
    }
    
    public synchronized String getDescription() {
        return this.description;
    }
    
    public synchronized void setDescription(final String desc) {
        this.description = desc;
    }
    
    public synchronized int getConnectionsPerHost() {
        return this.connectionsPerHost;
    }
    
    public synchronized void setConnectionsPerHost(final int connections) {
        this.connectionsPerHost = connections;
    }
    
    public synchronized int getThreadsAllowedToBlockForConnectionMultiplier() {
        return this.threadsAllowedToBlockForConnectionMultiplier;
    }
    
    public synchronized void setThreadsAllowedToBlockForConnectionMultiplier(final int threads) {
        this.threadsAllowedToBlockForConnectionMultiplier = threads;
    }
    
    public synchronized int getMaxWaitTime() {
        return this.maxWaitTime;
    }
    
    public synchronized void setMaxWaitTime(final int timeMS) {
        this.maxWaitTime = timeMS;
    }
    
    public synchronized int getConnectTimeout() {
        return this.connectTimeout;
    }
    
    public synchronized void setConnectTimeout(final int timeoutMS) {
        this.connectTimeout = timeoutMS;
    }
    
    public synchronized int getSocketTimeout() {
        return this.socketTimeout;
    }
    
    public synchronized void setSocketTimeout(final int timeoutMS) {
        this.socketTimeout = timeoutMS;
    }
    
    public synchronized boolean isSocketKeepAlive() {
        return this.socketKeepAlive;
    }
    
    public synchronized void setSocketKeepAlive(final boolean keepAlive) {
        this.socketKeepAlive = keepAlive;
    }
    
    public synchronized DBDecoderFactory getDbDecoderFactory() {
        return this.dbDecoderFactory;
    }
    
    public synchronized void setDbDecoderFactory(final DBDecoderFactory factory) {
        this.dbDecoderFactory = factory;
    }
    
    public synchronized DBEncoderFactory getDbEncoderFactory() {
        return this.dbEncoderFactory;
    }
    
    public synchronized void setDbEncoderFactory(final DBEncoderFactory factory) {
        this.dbEncoderFactory = factory;
    }
    
    public synchronized boolean isSafe() {
        return this.safe;
    }
    
    public synchronized void setSafe(final boolean isSafe) {
        this.safe = isSafe;
    }
    
    public synchronized int getW() {
        return this.w;
    }
    
    public synchronized void setW(final int val) {
        this.w = val;
    }
    
    public synchronized int getWtimeout() {
        return this.wtimeout;
    }
    
    public synchronized void setWtimeout(final int timeoutMS) {
        this.wtimeout = timeoutMS;
    }
    
    public synchronized boolean isFsync() {
        return this.fsync;
    }
    
    public synchronized void setFsync(final boolean sync) {
        this.fsync = sync;
    }
    
    public synchronized boolean isJ() {
        return this.j;
    }
    
    public synchronized void setJ(final boolean safe) {
        this.j = safe;
    }
    
    public ReadPreference getReadPreference() {
        return this.readPreference;
    }
    
    public void setReadPreference(final ReadPreference readPreference) {
        this.readPreference = readPreference;
    }
    
    public boolean isCursorFinalizerEnabled() {
        return this.cursorFinalizerEnabled;
    }
    
    public void setCursorFinalizerEnabled(final boolean cursorFinalizerEnabled) {
        this.cursorFinalizerEnabled = cursorFinalizerEnabled;
    }
    
    public boolean isAlwaysUseMBeans() {
        return this.alwaysUseMBeans;
    }
    
    public void setAlwaysUseMBeans(final boolean alwaysUseMBeans) {
        this.alwaysUseMBeans = alwaysUseMBeans;
    }
    
    public String getRequiredReplicaSetName() {
        return this.requiredReplicaSetName;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final MongoOptions options = (MongoOptions)o;
        if (this.alwaysUseMBeans != options.alwaysUseMBeans) {
            return false;
        }
        if (this.connectTimeout != options.connectTimeout) {
            return false;
        }
        if (this.connectionsPerHost != options.connectionsPerHost) {
            return false;
        }
        if (this.cursorFinalizerEnabled != options.cursorFinalizerEnabled) {
            return false;
        }
        if (this.fsync != options.fsync) {
            return false;
        }
        if (this.j != options.j) {
            return false;
        }
        if (this.maxWaitTime != options.maxWaitTime) {
            return false;
        }
        if (this.safe != options.safe) {
            return false;
        }
        if (this.socketKeepAlive != options.socketKeepAlive) {
            return false;
        }
        if (this.socketTimeout != options.socketTimeout) {
            return false;
        }
        if (this.threadsAllowedToBlockForConnectionMultiplier != options.threadsAllowedToBlockForConnectionMultiplier) {
            return false;
        }
        if (this.w != options.w) {
            return false;
        }
        if (this.wtimeout != options.wtimeout) {
            return false;
        }
        Label_0231: {
            if (this.dbDecoderFactory != null) {
                if (this.dbDecoderFactory.equals(options.dbDecoderFactory)) {
                    break Label_0231;
                }
            }
            else if (options.dbDecoderFactory == null) {
                break Label_0231;
            }
            return false;
        }
        Label_0264: {
            if (this.dbEncoderFactory != null) {
                if (this.dbEncoderFactory.equals(options.dbEncoderFactory)) {
                    break Label_0264;
                }
            }
            else if (options.dbEncoderFactory == null) {
                break Label_0264;
            }
            return false;
        }
        Label_0297: {
            if (this.description != null) {
                if (this.description.equals(options.description)) {
                    break Label_0297;
                }
            }
            else if (options.description == null) {
                break Label_0297;
            }
            return false;
        }
        Label_0330: {
            if (this.readPreference != null) {
                if (this.readPreference.equals(options.readPreference)) {
                    break Label_0330;
                }
            }
            else if (options.readPreference == null) {
                break Label_0330;
            }
            return false;
        }
        Label_0363: {
            if (this.socketFactory != null) {
                if (this.socketFactory.equals(options.socketFactory)) {
                    break Label_0363;
                }
            }
            else if (options.socketFactory == null) {
                break Label_0363;
            }
            return false;
        }
        Label_0396: {
            if (this.writeConcern != null) {
                if (this.writeConcern.equals(options.writeConcern)) {
                    break Label_0396;
                }
            }
            else if (options.writeConcern == null) {
                break Label_0396;
            }
            return false;
        }
        if (this.requiredReplicaSetName != null) {
            if (this.requiredReplicaSetName.equals(options.requiredReplicaSetName)) {
                return true;
            }
        }
        else if (options.requiredReplicaSetName == null) {
            return true;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int result = (this.description != null) ? this.description.hashCode() : 0;
        result = 31 * result + this.connectionsPerHost;
        result = 31 * result + this.threadsAllowedToBlockForConnectionMultiplier;
        result = 31 * result + this.maxWaitTime;
        result = 31 * result + this.connectTimeout;
        result = 31 * result + this.socketTimeout;
        result = 31 * result + (this.socketKeepAlive ? 1 : 0);
        result = 31 * result + ((this.readPreference != null) ? this.readPreference.hashCode() : 0);
        result = 31 * result + ((this.dbDecoderFactory != null) ? this.dbDecoderFactory.hashCode() : 0);
        result = 31 * result + ((this.dbEncoderFactory != null) ? this.dbEncoderFactory.hashCode() : 0);
        result = 31 * result + (this.safe ? 1 : 0);
        result = 31 * result + this.w;
        result = 31 * result + this.wtimeout;
        result = 31 * result + (this.fsync ? 1 : 0);
        result = 31 * result + (this.j ? 1 : 0);
        result = 31 * result + ((this.socketFactory != null) ? this.socketFactory.hashCode() : 0);
        result = 31 * result + (this.cursorFinalizerEnabled ? 1 : 0);
        result = 31 * result + ((this.writeConcern != null) ? this.writeConcern.hashCode() : 0);
        result = 31 * result + (this.alwaysUseMBeans ? 1 : 0);
        result = 31 * result + ((this.requiredReplicaSetName != null) ? this.requiredReplicaSetName.hashCode() : 0);
        return result;
    }
    
    @Override
    public String toString() {
        return "MongoOptions{description='" + this.description + '\'' + ", connectionsPerHost=" + this.connectionsPerHost + ", threadsAllowedToBlockForConnectionMultiplier=" + this.threadsAllowedToBlockForConnectionMultiplier + ", maxWaitTime=" + this.maxWaitTime + ", connectTimeout=" + this.connectTimeout + ", socketTimeout=" + this.socketTimeout + ", socketKeepAlive=" + this.socketKeepAlive + ", readPreference=" + this.readPreference + ", dbDecoderFactory=" + this.dbDecoderFactory + ", dbEncoderFactory=" + this.dbEncoderFactory + ", safe=" + this.safe + ", w=" + this.w + ", wtimeout=" + this.wtimeout + ", fsync=" + this.fsync + ", j=" + this.j + ", socketFactory=" + this.socketFactory + ", cursorFinalizerEnabled=" + this.cursorFinalizerEnabled + ", writeConcern=" + this.writeConcern + ", alwaysUseMBeans=" + this.alwaysUseMBeans + ", requiredReplicaSetName=" + this.requiredReplicaSetName + '}';
    }
}
