package com.mongodb.connection;

import com.mongodb.annotations.*;
import java.util.concurrent.atomic.*;
import com.mongodb.assertions.*;

@Immutable
public final class ConnectionId
{
    private static final AtomicInteger INCREMENTING_ID;
    private final ServerId serverId;
    private final int localValue;
    private final Integer serverValue;
    private final String stringValue;
    
    ConnectionId(final ServerId serverId) {
        this(serverId, ConnectionId.INCREMENTING_ID.incrementAndGet(), null);
    }
    
    private ConnectionId(final ServerId serverId, final int localValue, final Integer serverValue) {
        this.serverId = Assertions.notNull("serverId", serverId);
        this.localValue = localValue;
        this.serverValue = serverValue;
        if (serverValue == null) {
            this.stringValue = String.format("connectionId{localValue:%s}", localValue);
        }
        else {
            this.stringValue = String.format("connectionId{localValue:%s, serverValue:%s}", localValue, serverValue);
        }
    }
    
    ConnectionId withServerValue(final int serverValue) {
        Assertions.isTrue("server value is null", this.serverValue == null);
        return new ConnectionId(this.serverId, this.localValue, serverValue);
    }
    
    public ServerId getServerId() {
        return this.serverId;
    }
    
    public int getLocalValue() {
        return this.localValue;
    }
    
    public Integer getServerValue() {
        return this.serverValue;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final ConnectionId that = (ConnectionId)o;
        if (this.localValue != that.localValue) {
            return false;
        }
        if (!this.serverId.equals(that.serverId)) {
            return false;
        }
        if (this.serverValue != null) {
            if (this.serverValue.equals(that.serverValue)) {
                return true;
            }
        }
        else if (that.serverValue == null) {
            return true;
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int result = this.serverId.hashCode();
        result = 31 * result + this.localValue;
        result = 31 * result + ((this.serverValue != null) ? this.serverValue.hashCode() : 0);
        return result;
    }
    
    @Override
    public String toString() {
        return this.stringValue;
    }
    
    static {
        INCREMENTING_ID = new AtomicInteger();
    }
}
