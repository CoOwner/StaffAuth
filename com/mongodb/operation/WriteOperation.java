package com.mongodb.operation;

import com.mongodb.binding.*;

public interface WriteOperation<T>
{
    T execute(final WriteBinding p0);
}
