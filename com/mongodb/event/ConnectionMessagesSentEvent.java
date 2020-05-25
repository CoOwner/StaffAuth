package com.mongodb.event;

import com.mongodb.annotations.*;
import com.mongodb.connection.*;
import org.bson.assertions.*;

@Beta
public final class ConnectionMessagesSentEvent
{
    private final ConnectionId connectionId;
    private final int requestId;
    private final int size;
    
    public ConnectionMessagesSentEvent(final ConnectionId connectionId, final int requestId, final int size) {
        this.connectionId = Assertions.notNull("connectionId", connectionId);
        this.requestId = requestId;
        this.size = size;
    }
    
    public ConnectionId getConnectionId() {
        return this.connectionId;
    }
    
    public int getRequestId() {
        return this.requestId;
    }
    
    public int getSize() {
        return this.size;
    }
    
    @Override
    public String toString() {
        return "ConnectionMessagesSentEvent{requestId=" + this.requestId + ", size=" + this.size + ", connectionId=" + this.connectionId + '}';
    }
}
