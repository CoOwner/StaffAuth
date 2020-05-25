package com.mongodb.operation;

import com.mongodb.*;
import com.mongodb.async.*;

public interface AsyncOperationExecutor
{
     <T> void execute(final AsyncReadOperation<T> p0, final ReadPreference p1, final SingleResultCallback<T> p2);
    
     <T> void execute(final AsyncWriteOperation<T> p0, final SingleResultCallback<T> p1);
}
