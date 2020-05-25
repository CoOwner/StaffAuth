package com.mongodb.event;

import com.mongodb.annotations.*;
import com.mongodb.connection.*;
import com.mongodb.assertions.*;

@Beta
public final class ConnectionRemovedEvent
{
    private final ConnectionId connectionId;
    
    public ConnectionRemovedEvent(final ConnectionId connectionId) {
        this.connectionId = Assertions.notNull("connectionId", connectionId);
    }
    
    public ConnectionId getConnectionId() {
        return this.connectionId;
    }
    
    @Override
    public String toString() {
        return "ConnectionRemovedEvent{connectionId=" + this.connectionId + '}';
    }
}
