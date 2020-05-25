package com.mongodb.connection;

import com.mongodb.event.*;

class NoOpServerMonitorListener implements ServerMonitorListener
{
    @Override
    public void serverHearbeatStarted(final ServerHeartbeatStartedEvent event) {
    }
    
    @Override
    public void serverHeartbeatSucceeded(final ServerHeartbeatSucceededEvent event) {
    }
    
    @Override
    public void serverHeartbeatFailed(final ServerHeartbeatFailedEvent event) {
    }
}
