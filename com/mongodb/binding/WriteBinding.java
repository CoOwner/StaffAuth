package com.mongodb.binding;

public interface WriteBinding extends ReferenceCounted
{
    ConnectionSource getWriteConnectionSource();
    
    WriteBinding retain();
}
