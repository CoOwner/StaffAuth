package com.mongodb.operation;

import com.mongodb.binding.*;

public interface ReadOperation<T>
{
    T execute(final ReadBinding p0);
}
