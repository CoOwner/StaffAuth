package com.mongodb.internal.async;

import com.mongodb.async.*;
import com.mongodb.diagnostics.logging.*;
import com.mongodb.assertions.*;

public class ErrorHandlingResultCallback<T> implements SingleResultCallback<T>
{
    private final SingleResultCallback<T> wrapped;
    private final Logger logger;
    
    public static <T> SingleResultCallback<T> errorHandlingCallback(final SingleResultCallback<T> callback, final Logger logger) {
        if (callback instanceof ErrorHandlingResultCallback) {
            return callback;
        }
        return new ErrorHandlingResultCallback<T>(callback, logger);
    }
    
    ErrorHandlingResultCallback(final SingleResultCallback<T> wrapped, final Logger logger) {
        this.wrapped = Assertions.notNull("wrapped", wrapped);
        this.logger = Assertions.notNull("logger", logger);
    }
    
    @Override
    public void onResult(final T result, final Throwable t) {
        try {
            this.wrapped.onResult(result, t);
        }
        catch (Throwable e) {
            this.logger.warn("Callback onResult call produced an error", e);
        }
    }
}
