package com.mongodb.connection;

import com.mongodb.async.*;
import org.bson.*;
import java.util.*;
import com.mongodb.internal.async.*;
import com.mongodb.diagnostics.logging.*;

class UsageTrackingInternalConnection implements InternalConnection
{
    private static final Logger LOGGER;
    private volatile long openedAt;
    private volatile long lastUsedAt;
    private final int generation;
    private final InternalConnection wrapped;
    
    UsageTrackingInternalConnection(final InternalConnection wrapped, final int generation) {
        this.wrapped = wrapped;
        this.generation = generation;
        this.openedAt = Long.MAX_VALUE;
        this.lastUsedAt = this.openedAt;
    }
    
    @Override
    public void open() {
        this.wrapped.open();
        this.openedAt = System.currentTimeMillis();
        this.lastUsedAt = this.openedAt;
    }
    
    @Override
    public void openAsync(final SingleResultCallback<Void> callback) {
        this.wrapped.openAsync(new SingleResultCallback<Void>() {
            @Override
            public void onResult(final Void result, final Throwable t) {
                if (t != null) {
                    callback.onResult(null, t);
                }
                else {
                    UsageTrackingInternalConnection.this.openedAt = System.currentTimeMillis();
                    UsageTrackingInternalConnection.this.lastUsedAt = UsageTrackingInternalConnection.this.openedAt;
                    callback.onResult(null, null);
                }
            }
        });
    }
    
    @Override
    public void close() {
        this.wrapped.close();
    }
    
    @Override
    public boolean opened() {
        return this.wrapped.opened();
    }
    
    @Override
    public boolean isClosed() {
        return this.wrapped.isClosed();
    }
    
    @Override
    public ByteBuf getBuffer(final int size) {
        return this.wrapped.getBuffer(size);
    }
    
    @Override
    public void sendMessage(final List<ByteBuf> byteBuffers, final int lastRequestId) {
        this.wrapped.sendMessage(byteBuffers, lastRequestId);
        this.lastUsedAt = System.currentTimeMillis();
    }
    
    @Override
    public ResponseBuffers receiveMessage(final int responseTo) {
        final ResponseBuffers responseBuffers = this.wrapped.receiveMessage(responseTo);
        this.lastUsedAt = System.currentTimeMillis();
        return responseBuffers;
    }
    
    @Override
    public void sendMessageAsync(final List<ByteBuf> byteBuffers, final int lastRequestId, final SingleResultCallback<Void> callback) {
        final SingleResultCallback<Void> errHandlingCallback = ErrorHandlingResultCallback.errorHandlingCallback((SingleResultCallback<Void>)new SingleResultCallback<Void>() {
            @Override
            public void onResult(final Void result, final Throwable t) {
                UsageTrackingInternalConnection.this.lastUsedAt = System.currentTimeMillis();
                callback.onResult(result, t);
            }
        }, UsageTrackingInternalConnection.LOGGER);
        this.wrapped.sendMessageAsync(byteBuffers, lastRequestId, errHandlingCallback);
    }
    
    @Override
    public void receiveMessageAsync(final int responseTo, final SingleResultCallback<ResponseBuffers> callback) {
        final SingleResultCallback<ResponseBuffers> errHandlingCallback = ErrorHandlingResultCallback.errorHandlingCallback((SingleResultCallback<ResponseBuffers>)new SingleResultCallback<ResponseBuffers>() {
            @Override
            public void onResult(final ResponseBuffers result, final Throwable t) {
                UsageTrackingInternalConnection.this.lastUsedAt = System.currentTimeMillis();
                callback.onResult(result, t);
            }
        }, UsageTrackingInternalConnection.LOGGER);
        this.wrapped.receiveMessageAsync(responseTo, errHandlingCallback);
    }
    
    @Override
    public ConnectionDescription getDescription() {
        return this.wrapped.getDescription();
    }
    
    int getGeneration() {
        return this.generation;
    }
    
    long getOpenedAt() {
        return this.openedAt;
    }
    
    long getLastUsedAt() {
        return this.lastUsedAt;
    }
    
    static {
        LOGGER = Loggers.getLogger("connection");
    }
}
