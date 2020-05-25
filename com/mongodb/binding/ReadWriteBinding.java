package com.mongodb.binding;

public interface ReadWriteBinding extends ReadBinding, WriteBinding, ReferenceCounted
{
    ReadWriteBinding retain();
}
