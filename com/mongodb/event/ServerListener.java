package com.mongodb.event;

import java.util.*;

public interface ServerListener extends EventListener
{
    void serverOpening(final ServerOpeningEvent p0);
    
    void serverClosed(final ServerClosedEvent p0);
    
    void serverDescriptionChanged(final ServerDescriptionChangedEvent p0);
}
