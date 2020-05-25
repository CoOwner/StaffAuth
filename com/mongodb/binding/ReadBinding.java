package com.mongodb.binding;

import com.mongodb.*;

public interface ReadBinding extends ReferenceCounted
{
    ReadPreference getReadPreference();
    
    ConnectionSource getReadConnectionSource();
    
    ReadBinding retain();
}
