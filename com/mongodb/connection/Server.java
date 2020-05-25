package com.mongodb.connection;

import com.mongodb.annotations.*;
import com.mongodb.async.*;

@ThreadSafe
public interface Server
{
    ServerDescription getDescription();
    
    Connection getConnection();
    
    void getConnectionAsync(final SingleResultCallback<AsyncConnection> p0);
}
