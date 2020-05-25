package com.mongodb.binding;

public interface AsyncReadWriteBinding extends AsyncReadBinding, AsyncWriteBinding, ReferenceCounted
{
    AsyncReadWriteBinding retain();
}
