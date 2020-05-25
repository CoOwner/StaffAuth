package com.mongodb.event;

import com.mongodb.connection.*;
import com.mongodb.assertions.*;

public final class ServerHeartbeatStartedEvent
{
    private final ConnectionId connectionId;
    
    public ServerHeartbeatStartedEvent(final ConnectionId connectionId) {
        this.connectionId = Assertions.notNull("connectionId", connectionId);
    }
    
    public ConnectionId getConnectionId() {
        return this.connectionId;
    }
    
    @Override
    public String toString() {
        return "ServerHeartbeatStartedEvent{connectionId=" + this.connectionId + "} " + super.toString();
    }
}
