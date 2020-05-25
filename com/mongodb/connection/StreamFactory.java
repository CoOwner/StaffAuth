package com.mongodb.connection;

import com.mongodb.*;

public interface StreamFactory
{
    Stream create(final ServerAddress p0);
}
