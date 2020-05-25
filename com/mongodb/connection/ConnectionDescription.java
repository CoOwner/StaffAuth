package com.mongodb.connection;

import com.mongodb.annotations.*;
import com.mongodb.assertions.*;
import com.mongodb.*;

@Immutable
public class ConnectionDescription
{
    private final ConnectionId connectionId;
    private final ServerVersion serverVersion;
    private final ServerType serverType;
    private final int maxBatchCount;
    private final int maxDocumentSize;
    private final int maxMessageSize;
    private static final int DEFAULT_MAX_MESSAGE_SIZE = 33554432;
    private static final int DEFAULT_MAX_WRITE_BATCH_SIZE = 512;
    
    public ConnectionDescription(final ServerId serverId) {
        this(new ConnectionId(serverId), new ServerVersion(), ServerType.UNKNOWN, 512, ServerDescription.getDefaultMaxDocumentSize(), 33554432);
    }
    
    public ConnectionDescription(final ConnectionId connectionId, final ServerVersion serverVersion, final ServerType serverType, final int maxBatchCount, final int maxDocumentSize, final int maxMessageSize) {
        this.connectionId = connectionId;
        this.serverType = serverType;
        this.maxBatchCount = maxBatchCount;
        this.maxDocumentSize = maxDocumentSize;
        this.maxMessageSize = maxMessageSize;
        this.serverVersion = serverVersion;
    }
    
    ConnectionDescription withConnectionId(final ConnectionId connectionId) {
        Assertions.notNull("connectionId", connectionId);
        return new ConnectionDescription(connectionId, this.serverVersion, this.serverType, this.maxBatchCount, this.maxDocumentSize, this.maxMessageSize);
    }
    
    public ServerAddress getServerAddress() {
        return this.connectionId.getServerId().getAddress();
    }
    
    public ConnectionId getConnectionId() {
        return this.connectionId;
    }
    
    public ServerVersion getServerVersion() {
        return this.serverVersion;
    }
    
    public ServerType getServerType() {
        return this.serverType;
    }
    
    public int getMaxBatchCount() {
        return this.maxBatchCount;
    }
    
    public int getMaxDocumentSize() {
        return this.maxDocumentSize;
    }
    
    public int getMaxMessageSize() {
        return this.maxMessageSize;
    }
    
    public static int getDefaultMaxMessageSize() {
        return 33554432;
    }
    
    public static int getDefaultMaxWriteBatchSize() {
        return 512;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final ConnectionDescription that = (ConnectionDescription)o;
        return this.maxBatchCount == that.maxBatchCount && this.maxDocumentSize == that.maxDocumentSize && this.maxMessageSize == that.maxMessageSize && this.connectionId.equals(that.connectionId) && this.serverType == that.serverType && this.serverVersion.equals(that.serverVersion);
    }
    
    @Override
    public int hashCode() {
        int result = this.connectionId.hashCode();
        result = 31 * result + this.serverVersion.hashCode();
        result = 31 * result + this.serverType.hashCode();
        result = 31 * result + this.maxBatchCount;
        result = 31 * result + this.maxDocumentSize;
        result = 31 * result + this.maxMessageSize;
        return result;
    }
}
