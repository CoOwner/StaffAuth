package com.mongodb.connection;

import com.mongodb.event.*;
import java.util.concurrent.*;
import com.mongodb.annotations.*;
import java.util.*;
import com.mongodb.assertions.*;
import com.mongodb.*;

@Immutable
public class ServerSettings
{
    private final long heartbeatFrequencyMS;
    private final long minHeartbeatFrequencyMS;
    private final List<ServerListener> serverListeners;
    private final List<ServerMonitorListener> serverMonitorListeners;
    
    public static Builder builder() {
        return new Builder();
    }
    
    public long getHeartbeatFrequency(final TimeUnit timeUnit) {
        return timeUnit.convert(this.heartbeatFrequencyMS, TimeUnit.MILLISECONDS);
    }
    
    public long getMinHeartbeatFrequency(final TimeUnit timeUnit) {
        return timeUnit.convert(this.minHeartbeatFrequencyMS, TimeUnit.MILLISECONDS);
    }
    
    public List<ServerListener> getServerListeners() {
        return Collections.unmodifiableList((List<? extends ServerListener>)this.serverListeners);
    }
    
    public List<ServerMonitorListener> getServerMonitorListeners() {
        return Collections.unmodifiableList((List<? extends ServerMonitorListener>)this.serverMonitorListeners);
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final ServerSettings that = (ServerSettings)o;
        return this.heartbeatFrequencyMS == that.heartbeatFrequencyMS && this.minHeartbeatFrequencyMS == that.minHeartbeatFrequencyMS && this.serverListeners.equals(that.serverListeners) && this.serverMonitorListeners.equals(that.serverMonitorListeners);
    }
    
    @Override
    public int hashCode() {
        int result = (int)(this.heartbeatFrequencyMS ^ this.heartbeatFrequencyMS >>> 32);
        result = 31 * result + (int)(this.minHeartbeatFrequencyMS ^ this.minHeartbeatFrequencyMS >>> 32);
        result = 31 * result + this.serverListeners.hashCode();
        result = 31 * result + this.serverMonitorListeners.hashCode();
        return result;
    }
    
    @Override
    public String toString() {
        return "ServerSettings{heartbeatFrequencyMS=" + this.heartbeatFrequencyMS + ", minHeartbeatFrequencyMS=" + this.minHeartbeatFrequencyMS + ", serverListeners='" + this.serverListeners + '\'' + ", serverMonitorListeners='" + this.serverMonitorListeners + '\'' + '}';
    }
    
    ServerSettings(final Builder builder) {
        this.heartbeatFrequencyMS = builder.heartbeatFrequencyMS;
        this.minHeartbeatFrequencyMS = builder.minHeartbeatFrequencyMS;
        this.serverListeners = builder.serverListeners;
        this.serverMonitorListeners = builder.serverMonitorListeners;
    }
    
    @NotThreadSafe
    public static class Builder
    {
        private long heartbeatFrequencyMS;
        private long minHeartbeatFrequencyMS;
        private final List<ServerListener> serverListeners;
        private final List<ServerMonitorListener> serverMonitorListeners;
        
        public Builder() {
            this.heartbeatFrequencyMS = 10000L;
            this.minHeartbeatFrequencyMS = 500L;
            this.serverListeners = new ArrayList<ServerListener>();
            this.serverMonitorListeners = new ArrayList<ServerMonitorListener>();
        }
        
        public Builder heartbeatFrequency(final long heartbeatFrequency, final TimeUnit timeUnit) {
            this.heartbeatFrequencyMS = TimeUnit.MILLISECONDS.convert(heartbeatFrequency, timeUnit);
            return this;
        }
        
        public Builder minHeartbeatFrequency(final long minHeartbeatFrequency, final TimeUnit timeUnit) {
            this.minHeartbeatFrequencyMS = TimeUnit.MILLISECONDS.convert(minHeartbeatFrequency, timeUnit);
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
        
        public Builder applyConnectionString(final ConnectionString connectionString) {
            if (connectionString.getHeartbeatFrequency() != null) {
                this.heartbeatFrequencyMS = connectionString.getHeartbeatFrequency();
            }
            return this;
        }
        
        public ServerSettings build() {
            return new ServerSettings(this);
        }
    }
}
