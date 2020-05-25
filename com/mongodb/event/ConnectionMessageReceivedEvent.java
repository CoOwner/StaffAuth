package com.mongodb.event;

import com.mongodb.annotations.*;
import com.mongodb.connection.*;
import org.bson.assertions.*;

@Beta
public final class ConnectionMessageReceivedEvent
{
    private final int responseTo;
    private final int size;
    private final ConnectionId connectionId;
    
    public ConnectionMessageReceivedEvent(final ConnectionId connectionId, final int responseTo, final int size) {
        this.connectionId = Assertions.notNull("connectionId", connectionId);
        this.responseTo = responseTo;
        this.size = size;
    }
    
    public int getResponseTo() {
        return this.responseTo;
    }
    
    public int getSize() {
        return this.size;
    }
    
    public ConnectionId getConnectionId() {
        return this.connectionId;
    }
    
    @Override
    public String toString() {
        return "ConnectionMessageReceivedEvent{responseTo=" + this.responseTo + ", size=" + this.size + ", connectionId=" + this.connectionId + '}';
    }
}
