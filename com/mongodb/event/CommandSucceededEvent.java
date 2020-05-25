package com.mongodb.event;

import org.bson.*;
import com.mongodb.connection.*;
import com.mongodb.assertions.*;
import java.util.concurrent.*;

public final class CommandSucceededEvent extends CommandEvent
{
    private final BsonDocument response;
    private final long elapsedTimeNanos;
    
    public CommandSucceededEvent(final int requestId, final ConnectionDescription connectionDescription, final String commandName, final BsonDocument response, final long elapsedTimeNanos) {
        super(requestId, connectionDescription, commandName);
        this.response = response;
        Assertions.isTrueArgument("elapsed time is not negative", elapsedTimeNanos >= 0L);
        this.elapsedTimeNanos = elapsedTimeNanos;
    }
    
    public long getElapsedTime(final TimeUnit timeUnit) {
        return timeUnit.convert(this.elapsedTimeNanos, TimeUnit.NANOSECONDS);
    }
    
    public BsonDocument getResponse() {
        return this.response;
    }
}
