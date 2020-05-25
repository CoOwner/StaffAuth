package com.mongodb.connection;

import com.mongodb.async.*;

interface ProtocolExecutor
{
     <T> T execute(final Protocol<T> p0, final InternalConnection p1);
    
     <T> void executeAsync(final Protocol<T> p0, final InternalConnection p1, final SingleResultCallback<T> p2);
}
