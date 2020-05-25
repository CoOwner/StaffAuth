package com.mongodb.event;

import com.mongodb.annotations.*;
import com.mongodb.connection.*;
import com.mongodb.assertions.*;

@Beta
public final class ConnectionPoolClosedEvent
{
    private final ServerId serverId;
    
    public ConnectionPoolClosedEvent(final ServerId serverId) {
        this.serverId = Assertions.notNull("serverId", serverId);
    }
    
    public ServerId getServerId() {
        return this.serverId;
    }
    
    @Override
    public String toString() {
        return "ConnectionPoolClosedEvent{serverId=" + this.serverId + '}';
    }
}
