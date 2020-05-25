package com.mongodb.connection;

import com.mongodb.async.*;
import com.mongodb.*;

abstract class ResponseCallback implements SingleResultCallback<ResponseBuffers>
{
    private volatile boolean closed;
    private final ServerAddress serverAddress;
    private final long requestId;
    
    ResponseCallback(final long requestId, final ServerAddress serverAddress) {
        this.serverAddress = serverAddress;
        this.requestId = requestId;
    }
    
    protected ServerAddress getServerAddress() {
        return this.serverAddress;
    }
    
    protected long getRequestId() {
        return this.requestId;
    }
    
    @Override
    public void onResult(final ResponseBuffers responseBuffers, final Throwable t) {
        if (this.closed) {
            throw new MongoInternalException("Callback should not be invoked more than once", null);
        }
        this.closed = true;
        if (responseBuffers != null) {
            this.callCallback(responseBuffers, t);
        }
        else {
            this.callCallback(null, t);
        }
    }
    
    protected abstract void callCallback(final ResponseBuffers p0, final Throwable p1);
}
