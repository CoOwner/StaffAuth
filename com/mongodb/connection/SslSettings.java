package com.mongodb.connection;

import com.mongodb.annotations.*;
import com.mongodb.*;

@Immutable
public class SslSettings
{
    private final boolean enabled;
    private final boolean invalidHostNameAllowed;
    
    public static Builder builder() {
        return new Builder();
    }
    
    public boolean isEnabled() {
        return this.enabled;
    }
    
    public boolean isInvalidHostNameAllowed() {
        return this.invalidHostNameAllowed;
    }
    
    SslSettings(final Builder builder) {
        this.enabled = builder.enabled;
        this.invalidHostNameAllowed = builder.invalidHostNameAllowed;
        if (this.enabled && !this.invalidHostNameAllowed && System.getProperty("java.version").startsWith("1.6.")) {
            throw new MongoInternalException("By default, SSL connections are only supported on Java 7 or later.  If the application must run on Java 6, you must set the SslSettings.invalidHostNameAllowed property to false");
        }
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final SslSettings that = (SslSettings)o;
        return this.enabled == that.enabled && this.invalidHostNameAllowed == that.invalidHostNameAllowed;
    }
    
    @Override
    public int hashCode() {
        int result = this.enabled ? 1 : 0;
        result = 31 * result + (this.invalidHostNameAllowed ? 1 : 0);
        return result;
    }
    
    @Override
    public String toString() {
        return "SslSettings{enabled=" + this.enabled + ", invalidHostNameAllowed=" + this.invalidHostNameAllowed + '}';
    }
    
    @NotThreadSafe
    public static class Builder
    {
        private boolean enabled;
        private boolean invalidHostNameAllowed;
        
        public Builder enabled(final boolean enabled) {
            this.enabled = enabled;
            return this;
        }
        
        public Builder invalidHostNameAllowed(final boolean invalidHostNameAllowed) {
            this.invalidHostNameAllowed = invalidHostNameAllowed;
            return this;
        }
        
        public Builder applyConnectionString(final ConnectionString connectionString) {
            if (connectionString.getSslEnabled() != null) {
                this.enabled = connectionString.getSslEnabled();
            }
            if (connectionString.getSslInvalidHostnameAllowed() != null) {
                this.invalidHostNameAllowed = connectionString.getSslInvalidHostnameAllowed();
            }
            return this;
        }
        
        public SslSettings build() {
            return new SslSettings(this);
        }
    }
}
