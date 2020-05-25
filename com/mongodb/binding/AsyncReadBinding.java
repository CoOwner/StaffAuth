package com.mongodb.binding;

import com.mongodb.*;
import com.mongodb.async.*;

public interface AsyncReadBinding extends ReferenceCounted
{
    ReadPreference getReadPreference();
    
    void getReadConnectionSource(final SingleResultCallback<AsyncConnectionSource> p0);
    
    AsyncReadBinding retain();
}
