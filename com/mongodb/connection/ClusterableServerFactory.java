package com.mongodb.connection;

import com.mongodb.*;
import com.mongodb.event.*;

interface ClusterableServerFactory
{
    ClusterableServer create(final ServerAddress p0, final ServerListener p1);
    
    ServerSettings getSettings();
}
