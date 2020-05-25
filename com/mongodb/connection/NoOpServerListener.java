package com.mongodb.connection;

import com.mongodb.event.*;

class NoOpServerListener implements ServerListener
{
    @Override
    public void serverOpening(final ServerOpeningEvent event) {
    }
    
    @Override
    public void serverClosed(final ServerClosedEvent event) {
    }
    
    @Override
    public void serverDescriptionChanged(final ServerDescriptionChangedEvent event) {
    }
}
