package com.mongodb.operation;

import com.mongodb.binding.*;
import com.mongodb.async.*;

public interface AsyncWriteOperation<T>
{
    void executeAsync(final AsyncWriteBinding p0, final SingleResultCallback<T> p1);
}
