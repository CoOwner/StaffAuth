package com.mongodb.event;

import com.mongodb.connection.*;
import org.bson.*;
import com.mongodb.assertions.*;
import java.util.concurrent.*;

public final class ServerHeartbeatSucceededEvent
{
    private final ConnectionId connectionId;
    private final BsonDocument reply;
    private final long elapsedTimeNanos;
    
    public ServerHeartbeatSucceededEvent(final ConnectionId connectionId, final BsonDocument reply, final long elapsedTimeNanos) {
        this.connectionId = Assertions.notNull("connectionId", connectionId);
        this.reply = Assertions.notNull("reply", reply);
        Assertions.isTrueArgument("elapsed time is not negative", elapsedTimeNanos >= 0L);
        this.elapsedTimeNanos = elapsedTimeNanos;
    }
    
    public ConnectionId getConnectionId() {
        return this.connectionId;
    }
    
    public BsonDocument getReply() {
        return this.reply;
    }
    
    public long getElapsedTime(final TimeUnit timeUnit) {
        return timeUnit.convert(this.elapsedTimeNanos, TimeUnit.NANOSECONDS);
    }
    
    @Override
    public String toString() {
        return "ServerHeartbeatSucceededEvent{connectionId=" + this.connectionId + ", reply=" + this.reply + ", elapsedTimeNanos=" + this.elapsedTimeNanos + "} ";
    }
}
