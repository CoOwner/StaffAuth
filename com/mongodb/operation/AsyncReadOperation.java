package com.mongodb.operation;

import com.mongodb.binding.*;
import com.mongodb.async.*;

public interface AsyncReadOperation<T>
{
    void executeAsync(final AsyncReadBinding p0, final SingleResultCallback<T> p1);
}
