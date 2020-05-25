package com.mongodb.event;

import com.mongodb.connection.*;
import com.mongodb.assertions.*;
import java.util.concurrent.*;

public final class ServerHeartbeatFailedEvent
{
    private final ConnectionId connectionId;
    private final long elapsedTimeNanos;
    private final Throwable throwable;
    
    public ServerHeartbeatFailedEvent(final ConnectionId connectionId, final long elapsedTimeNanos, final Throwable throwable) {
        this.connectionId = Assertions.notNull("connectionId", connectionId);
        Assertions.isTrueArgument("elapsed time is not negative", elapsedTimeNanos >= 0L);
        this.elapsedTimeNanos = elapsedTimeNanos;
        this.throwable = Assertions.notNull("throwable", throwable);
    }
    
    public ConnectionId getConnectionId() {
        return this.connectionId;
    }
    
    public long getElapsedTime(final TimeUnit timeUnit) {
        return timeUnit.convert(this.elapsedTimeNanos, TimeUnit.NANOSECONDS);
    }
    
    public Throwable getThrowable() {
        return this.throwable;
    }
    
    @Override
    public String toString() {
        return "ServerHeartbeatFailedEvent{connectionId=" + this.connectionId + ", elapsedTimeNanos=" + this.elapsedTimeNanos + ", throwable=" + this.throwable + "} " + super.toString();
    }
}
