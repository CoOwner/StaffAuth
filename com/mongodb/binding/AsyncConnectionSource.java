package com.mongodb.binding;

import com.mongodb.async.*;
import com.mongodb.connection.*;

public interface AsyncConnectionSource extends ReferenceCounted
{
    ServerDescription getServerDescription();
    
    void getConnection(final SingleResultCallback<AsyncConnection> p0);
    
    AsyncConnectionSource retain();
}
