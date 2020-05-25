package com.mongodb.event;

import com.mongodb.annotations.*;
import com.mongodb.connection.*;

@Beta
public final class ConnectionPoolWaitQueueEnteredEvent
{
    private final ServerId serverId;
    private final long threadId;
    
    public ConnectionPoolWaitQueueEnteredEvent(final ServerId serverId, final long threadId) {
        this.serverId = serverId;
        this.threadId = threadId;
    }
    
    public ServerId getServerId() {
        return this.serverId;
    }
    
    public long getThreadId() {
        return this.threadId;
    }
    
    @Override
    public String toString() {
        return "ConnectionPoolWaitQueueEnteredEvent{serverId=" + this.serverId + ", threadId=" + this.threadId + '}';
    }
}
