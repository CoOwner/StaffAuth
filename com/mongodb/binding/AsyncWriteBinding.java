package com.mongodb.binding;

import com.mongodb.async.*;

public interface AsyncWriteBinding extends ReferenceCounted
{
    void getWriteConnectionSource(final SingleResultCallback<AsyncConnectionSource> p0);
    
    AsyncWriteBinding retain();
}
