package com.mongodb;

import com.mongodb.assertions.*;
import java.util.*;

public class MongoClientURI
{
    private final ConnectionString proxied;
    private final MongoClientOptions.Builder builder;
    
    public MongoClientURI(final String uri) {
        this(uri, new MongoClientOptions.Builder());
    }
    
    public MongoClientURI(final String uri, final MongoClientOptions.Builder builder) {
        this.builder = Assertions.notNull("builder", builder);
        this.proxied = new ConnectionString(uri);
    }
    
    public String getUsername() {
        return this.proxied.getUsername();
    }
    
    public char[] getPassword() {
        return this.proxied.getPassword();
    }
    
    public List<String> getHosts() {
        return this.proxied.getHosts();
    }
    
    public String getDatabase() {
        return this.proxied.getDatabase();
    }
    
    public String getCollection() {
        return this.proxied.getCollection();
    }
    
    public String getURI() {
        return this.proxied.getConnectionString();
    }
    
    public MongoCredential getCredentials() {
        if (this.proxied.getCredentialList().isEmpty()) {
            return null;
        }
        return this.proxied.getCredentialList().get(0);
    }
    
    public MongoClientOptions getOptions() {
        if (this.proxied.getReadPreference() != null) {
            this.builder.readPreference(this.proxied.getReadPreference());
        }
        if (this.proxied.getReadConcern() != null) {
            this.builder.readConcern(this.proxied.getReadConcern());
        }
        if (this.proxied.getWriteConcern() != null) {
            this.builder.writeConcern(this.proxied.getWriteConcern());
        }
        if (this.proxied.getMaxConnectionPoolSize() != null) {
            this.builder.connectionsPerHost(this.proxied.getMaxConnectionPoolSize());
        }
        if (this.proxied.getMinConnectionPoolSize() != null) {
            this.builder.minConnectionsPerHost(this.proxied.getMinConnectionPoolSize());
        }
        if (this.proxied.getMaxWaitTime() != null) {
            this.builder.maxWaitTime(this.proxied.getMaxWaitTime());
        }
        if (this.proxied.getThreadsAllowedToBlockForConnectionMultiplier() != null) {
            this.builder.threadsAllowedToBlockForConnectionMultiplier(this.proxied.getThreadsAllowedToBlockForConnectionMultiplier());
        }
        if (this.proxied.getMaxConnectionIdleTime() != null) {
            this.builder.maxConnectionIdleTime(this.proxied.getMaxConnectionIdleTime());
        }
        if (this.proxied.getMaxConnectionLifeTime() != null) {
            this.builder.maxConnectionLifeTime(this.proxied.getMaxConnectionLifeTime());
        }
        if (this.proxied.getSocketTimeout() != null) {
            this.builder.socketTimeout(this.proxied.getSocketTimeout());
        }
        if (this.proxied.getConnectTimeout() != null) {
            this.builder.connectTimeout(this.proxied.getConnectTimeout());
        }
        if (this.proxied.getRequiredReplicaSetName() != null) {
            this.builder.requiredReplicaSetName(this.proxied.getRequiredReplicaSetName());
        }
        if (this.proxied.getSslEnabled() != null) {
            this.builder.sslEnabled(this.proxied.getSslEnabled());
        }
        if (this.proxied.getSslInvalidHostnameAllowed() != null) {
            this.builder.sslInvalidHostNameAllowed(this.proxied.getSslInvalidHostnameAllowed());
        }
        if (this.proxied.getServerSelectionTimeout() != null) {
            this.builder.serverSelectionTimeout(this.proxied.getServerSelectionTimeout());
        }
        if (this.proxied.getLocalThreshold() != null) {
            this.builder.localThreshold(this.proxied.getLocalThreshold());
        }
        if (this.proxied.getHeartbeatFrequency() != null) {
            this.builder.heartbeatFrequency(this.proxied.getHeartbeatFrequency());
        }
        if (this.proxied.getApplicationName() != null) {
            this.builder.applicationName(this.proxied.getApplicationName());
        }
        return this.builder.build();
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final MongoClientURI that = (MongoClientURI)o;
        if (!this.getHosts().equals(that.getHosts())) {
            return false;
        }
        Label_0080: {
            if (this.getDatabase() != null) {
                if (this.getDatabase().equals(that.getDatabase())) {
                    break Label_0080;
                }
            }
            else if (that.getDatabase() == null) {
                break Label_0080;
            }
            return false;
        }
        Label_0113: {
            if (this.getCollection() != null) {
                if (this.getCollection().equals(that.getCollection())) {
                    break Label_0113;
                }
            }
            else if (that.getCollection() == null) {
                break Label_0113;
            }
            return false;
        }
        if (this.getCredentials() != null) {
            if (this.getCredentials().equals(that.getCredentials())) {
                return this.getOptions().equals(that.getOptions());
            }
        }
        else if (that.getCredentials() == null) {
            return this.getOptions().equals(that.getOptions());
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int result = this.getOptions().hashCode();
        result = 31 * result + ((this.getCredentials() != null) ? this.getCredentials().hashCode() : 0);
        result = 31 * result + this.getHosts().hashCode();
        result = 31 * result + ((this.getDatabase() != null) ? this.getDatabase().hashCode() : 0);
        result = 31 * result + ((this.getCollection() != null) ? this.getCollection().hashCode() : 0);
        return result;
    }
    
    @Override
    public String toString() {
        return this.proxied.toString();
    }
}
