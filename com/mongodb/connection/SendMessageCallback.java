package com.mongodb.connection;

import com.mongodb.async.*;
import org.bson.io.*;
import com.mongodb.event.*;

class SendMessageCallback<T> implements SingleResultCallback<Void>
{
    private final OutputBuffer buffer;
    private final InternalConnection connection;
    private final SingleResultCallback<ResponseBuffers> receiveMessageCallback;
    private final int requestId;
    private final RequestMessage message;
    private final CommandListener commandListener;
    private final long startTimeNanos;
    private final SingleResultCallback<T> callback;
    private final String commandName;
    
    SendMessageCallback(final InternalConnection connection, final OutputBuffer buffer, final RequestMessage message, final String commandName, final long startTimeNanos, final CommandListener commandListener, final SingleResultCallback<T> callback, final SingleResultCallback<ResponseBuffers> receiveMessageCallback) {
        this(connection, buffer, message, message.getId(), commandName, startTimeNanos, commandListener, callback, receiveMessageCallback);
    }
    
    SendMessageCallback(final InternalConnection connection, final OutputBuffer buffer, final RequestMessage message, final int requestId, final String commandName, final long startTimeNanos, final CommandListener commandListener, final SingleResultCallback<T> callback, final SingleResultCallback<ResponseBuffers> receiveMessageCallback) {
        this.buffer = buffer;
        this.connection = connection;
        this.message = message;
        this.commandName = commandName;
        this.commandListener = commandListener;
        this.startTimeNanos = startTimeNanos;
        this.callback = callback;
        this.receiveMessageCallback = receiveMessageCallback;
        this.requestId = requestId;
    }
    
    @Override
    public void onResult(final Void result, final Throwable t) {
        this.buffer.close();
        if (t != null) {
            if (this.commandListener != null) {
                ProtocolHelper.sendCommandFailedEvent(this.message, this.commandName, this.connection.getDescription(), this.startTimeNanos, t, this.commandListener);
            }
            this.callback.onResult(null, t);
        }
        else {
            this.connection.receiveMessageAsync(this.requestId, this.receiveMessageCallback);
        }
    }
}
