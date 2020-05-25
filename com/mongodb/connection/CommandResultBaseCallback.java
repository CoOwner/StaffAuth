package com.mongodb.connection;

import org.bson.codecs.*;
import com.mongodb.*;
import com.mongodb.diagnostics.logging.*;

abstract class CommandResultBaseCallback<T> extends ResponseCallback
{
    public static final Logger LOGGER;
    private final Decoder<T> decoder;
    
    CommandResultBaseCallback(final Decoder<T> decoder, final long requestId, final ServerAddress serverAddress) {
        super(requestId, serverAddress);
        this.decoder = decoder;
    }
    
    @Override
    protected void callCallback(final ResponseBuffers responseBuffers, final Throwable t) {
        try {
            if (t != null || responseBuffers == null) {
                this.callCallback((T)null, t);
            }
            else {
                final ReplyMessage<T> replyMessage = new ReplyMessage<T>(responseBuffers, this.decoder, this.getRequestId());
                this.callCallback(replyMessage.getDocuments().get(0), null);
            }
        }
        finally {
            try {
                if (responseBuffers != null) {
                    responseBuffers.close();
                }
            }
            catch (Throwable t2) {
                CommandResultBaseCallback.LOGGER.debug("GetMore ResponseBuffer close exception", t2);
            }
        }
    }
    
    protected abstract void callCallback(final T p0, final Throwable p1);
    
    static {
        LOGGER = Loggers.getLogger("protocol.command");
    }
}
