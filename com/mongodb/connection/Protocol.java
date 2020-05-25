package com.mongodb.connection;

import com.mongodb.async.*;
import com.mongodb.event.*;

interface Protocol<T>
{
    T execute(final InternalConnection p0);
    
    void executeAsync(final InternalConnection p0, final SingleResultCallback<T> p1);
    
    void setCommandListener(final CommandListener p0);
}
