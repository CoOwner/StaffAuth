package com.mongodb.async;

public interface SingleResultCallback<T>
{
    void onResult(final T p0, final Throwable p1);
}
