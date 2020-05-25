package com.mongodb.binding;

import com.mongodb.connection.*;

public interface ConnectionSource extends ReferenceCounted
{
    ServerDescription getServerDescription();
    
    Connection getConnection();
    
    ConnectionSource retain();
}
