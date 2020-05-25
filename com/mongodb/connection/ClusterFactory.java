package com.mongodb.connection;

import java.util.*;
import com.mongodb.*;
import com.mongodb.event.*;

public interface ClusterFactory
{
    Cluster create(final ClusterSettings p0, final ServerSettings p1, final ConnectionPoolSettings p2, final StreamFactory p3, final StreamFactory p4, final List<MongoCredential> p5, final ClusterListener p6, final ConnectionPoolListener p7, final ConnectionListener p8);
}
