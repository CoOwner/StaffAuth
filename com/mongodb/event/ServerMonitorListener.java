package com.mongodb.event;

import java.util.*;

public interface ServerMonitorListener extends EventListener
{
    void serverHearbeatStarted(final ServerHeartbeatStartedEvent p0);
    
    void serverHeartbeatSucceeded(final ServerHeartbeatSucceededEvent p0);
    
    void serverHeartbeatFailed(final ServerHeartbeatFailedEvent p0);
}
