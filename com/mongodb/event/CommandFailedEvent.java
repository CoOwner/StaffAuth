package com.mongodb.event;

import com.mongodb.connection.*;
import com.mongodb.assertions.*;
import java.util.concurrent.*;

public final class CommandFailedEvent extends CommandEvent
{
    private final long elapsedTimeNanos;
    private final Throwable throwable;
    
    public CommandFailedEvent(final int requestId, final ConnectionDescription connectionDescription, final String commandName, final long elapsedTimeNanos, final Throwable throwable) {
        super(requestId, connectionDescription, commandName);
        Assertions.isTrueArgument("elapsed time is not negative", elapsedTimeNanos >= 0L);
        this.elapsedTimeNanos = elapsedTimeNanos;
        this.throwable = throwable;
    }
    
    public long getElapsedTime(final TimeUnit timeUnit) {
        return timeUnit.convert(this.elapsedTimeNanos, TimeUnit.NANOSECONDS);
    }
    
    public Throwable getThrowable() {
        return this.throwable;
    }
}
