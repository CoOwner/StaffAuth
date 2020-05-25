package com.mongodb.connection;

import com.mongodb.annotations.*;
import java.util.concurrent.*;
import com.mongodb.*;

@Immutable
public class SocketSettings
{
    private final long connectTimeoutMS;
    private final long readTimeoutMS;
    private final boolean keepAlive;
    private final int receiveBufferSize;
    private final int sendBufferSize;
    
    public static Builder builder() {
        return new Builder();
    }
    
    public int getConnectTimeout(final TimeUnit timeUnit) {
        return (int)timeUnit.convert(this.connectTimeoutMS, TimeUnit.MILLISECONDS);
    }
    
    public int getReadTimeout(final TimeUnit timeUnit) {
        return (int)timeUnit.convert(this.readTimeoutMS, TimeUnit.MILLISECONDS);
    }
    
    public boolean isKeepAlive() {
        return this.keepAlive;
    }
    
    public int getReceiveBufferSize() {
        return this.receiveBufferSize;
    }
    
    public int getSendBufferSize() {
        return this.sendBufferSize;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final SocketSettings that = (SocketSettings)o;
        return this.connectTimeoutMS == that.connectTimeoutMS && this.keepAlive == that.keepAlive && this.readTimeoutMS == that.readTimeoutMS && this.receiveBufferSize == that.receiveBufferSize && this.sendBufferSize == that.sendBufferSize;
    }
    
    @Override
    public int hashCode() {
        int result = (int)(this.connectTimeoutMS ^ this.connectTimeoutMS >>> 32);
        result = 31 * result + (int)(this.readTimeoutMS ^ this.readTimeoutMS >>> 32);
        result = 31 * result + (this.keepAlive ? 1 : 0);
        result = 31 * result + this.receiveBufferSize;
        result = 31 * result + this.sendBufferSize;
        return result;
    }
    
    @Override
    public String toString() {
        return "SocketSettings{connectTimeoutMS=" + this.connectTimeoutMS + ", readTimeoutMS=" + this.readTimeoutMS + ", keepAlive=" + this.keepAlive + ", receiveBufferSize=" + this.receiveBufferSize + ", sendBufferSize=" + this.sendBufferSize + '}';
    }
    
    SocketSettings(final Builder builder) {
        this.connectTimeoutMS = builder.connectTimeoutMS;
        this.readTimeoutMS = builder.readTimeoutMS;
        this.keepAlive = builder.keepAlive;
        this.receiveBufferSize = builder.receiveBufferSize;
        this.sendBufferSize = builder.sendBufferSize;
    }
    
    public static class Builder
    {
        private long connectTimeoutMS;
        private long readTimeoutMS;
        private boolean keepAlive;
        private int receiveBufferSize;
        private int sendBufferSize;
        
        public Builder() {
            this.connectTimeoutMS = 10000L;
        }
        
        public Builder connectTimeout(final int connectTimeout, final TimeUnit timeUnit) {
            this.connectTimeoutMS = TimeUnit.MILLISECONDS.convert(connectTimeout, timeUnit);
            return this;
        }
        
        public Builder readTimeout(final int readTimeout, final TimeUnit timeUnit) {
            this.readTimeoutMS = TimeUnit.MILLISECONDS.convert(readTimeout, timeUnit);
            return this;
        }
        
        public Builder keepAlive(final boolean keepAlive) {
            this.keepAlive = keepAlive;
            return this;
        }
        
        public Builder receiveBufferSize(final int receiveBufferSize) {
            this.receiveBufferSize = receiveBufferSize;
            return this;
        }
        
        public Builder sendBufferSize(final int sendBufferSize) {
            this.sendBufferSize = sendBufferSize;
            return this;
        }
        
        public Builder applyConnectionString(final ConnectionString connectionString) {
            if (connectionString.getConnectTimeout() != null) {
                this.connectTimeout(connectionString.getConnectTimeout(), TimeUnit.MILLISECONDS);
            }
            if (connectionString.getSocketTimeout() != null) {
                this.readTimeout(connectionString.getSocketTimeout(), TimeUnit.MILLISECONDS);
            }
            return this;
        }
        
        public SocketSettings build() {
            return new SocketSettings(this);
        }
    }
}
