package com.mongodb.binding;

public interface ReferenceCounted
{
    int getCount();
    
    ReferenceCounted retain();
    
    void release();
}
