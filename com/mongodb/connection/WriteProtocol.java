package com.mongodb.connection;

import com.mongodb.event.*;
import org.bson.codecs.*;
import com.mongodb.async.*;
import org.bson.io.*;
import com.mongodb.*;
import org.bson.*;
import java.util.*;
import com.mongodb.diagnostics.logging.*;

abstract class WriteProtocol implements Protocol<WriteConcernResult>
{
    private final MongoNamespace namespace;
    private final boolean ordered;
    private final WriteConcern writeConcern;
    private CommandListener commandListener;
    
    public WriteProtocol(final MongoNamespace namespace, final boolean ordered, final WriteConcern writeConcern) {
        this.namespace = namespace;
        this.ordered = ordered;
        this.writeConcern = writeConcern;
    }
    
    @Override
    public void setCommandListener(final CommandListener commandListener) {
        this.commandListener = commandListener;
    }
    
    @Override
    public WriteConcernResult execute(final InternalConnection connection) {
        WriteConcernResult writeConcernResult = null;
        RequestMessage requestMessage = null;
        do {
            final long startTimeNanos = System.nanoTime();
            boolean sentCommandStartedEvent = false;
            final ByteBufferBsonOutput bsonOutput = new ByteBufferBsonOutput(connection);
            RequestMessage.EncodingMetadata encodingMetadata;
            int messageId;
            try {
                if (requestMessage == null) {
                    requestMessage = this.createRequestMessage(ProtocolHelper.getMessageSettings(connection.getDescription()));
                }
                encodingMetadata = requestMessage.encodeWithMetadata(bsonOutput);
                this.sendStartedEvent(connection, requestMessage, encodingMetadata, bsonOutput);
                sentCommandStartedEvent = true;
                messageId = requestMessage.getId();
                if (this.shouldAcknowledge(encodingMetadata.getNextMessage())) {
                    final CommandMessage getLastErrorMessage = new CommandMessage(new MongoNamespace(this.getNamespace().getDatabaseName(), "$cmd").getFullName(), this.createGetLastErrorCommandDocument(), false, ProtocolHelper.getMessageSettings(connection.getDescription()));
                    getLastErrorMessage.encode(bsonOutput);
                    messageId = getLastErrorMessage.getId();
                }
                connection.sendMessage(bsonOutput.getByteBuffers(), messageId);
            }
            catch (RuntimeException e) {
                this.sendFailedEvent(connection, requestMessage, sentCommandStartedEvent, e, startTimeNanos);
                throw e;
            }
            finally {
                bsonOutput.close();
            }
            if (this.shouldAcknowledge(encodingMetadata.getNextMessage())) {
                ResponseBuffers responseBuffers = null;
                try {
                    responseBuffers = connection.receiveMessage(messageId);
                    final ReplyMessage<BsonDocument> replyMessage = new ReplyMessage<BsonDocument>(responseBuffers, new BsonDocumentCodec(), messageId);
                    writeConcernResult = ProtocolHelper.getWriteResult(replyMessage.getDocuments().get(0), connection.getDescription().getServerAddress());
                }
                catch (WriteConcernException e2) {
                    this.sendSucceededEvent(connection, requestMessage, encodingMetadata.getNextMessage(), e2, startTimeNanos);
                    if (this.writeConcern.isAcknowledged()) {
                        throw e2;
                    }
                    if (this.ordered) {
                        break;
                    }
                }
                catch (RuntimeException e3) {
                    this.sendFailedEvent(connection, requestMessage, sentCommandStartedEvent, e3, startTimeNanos);
                    throw e3;
                }
                finally {
                    if (responseBuffers != null) {
                        responseBuffers.close();
                    }
                }
            }
            this.sendSucceededEvent(connection, requestMessage, encodingMetadata.getNextMessage(), writeConcernResult, startTimeNanos);
            requestMessage = encodingMetadata.getNextMessage();
        } while (requestMessage != null);
        return this.writeConcern.isAcknowledged() ? writeConcernResult : WriteConcernResult.unacknowledged();
    }
    
    protected abstract void appendToWriteCommandResponseDocument(final RequestMessage p0, final RequestMessage p1, final WriteConcernResult p2, final BsonDocument p3);
    
    @Override
    public void executeAsync(final InternalConnection connection, final SingleResultCallback<WriteConcernResult> callback) {
        this.executeAsync(this.createRequestMessage(ProtocolHelper.getMessageSettings(connection.getDescription())), connection, callback);
    }
    
    private void executeAsync(final RequestMessage requestMessage, final InternalConnection connection, final SingleResultCallback<WriteConcernResult> callback) {
        final long startTimeNanos = System.nanoTime();
        boolean sentCommandStartedEvent = false;
        try {
            final ByteBufferBsonOutput bsonOutput = new ByteBufferBsonOutput(connection);
            final RequestMessage.EncodingMetadata encodingMetadata = ProtocolHelper.encodeMessageWithMetadata(requestMessage, bsonOutput);
            this.sendStartedEvent(connection, requestMessage, encodingMetadata, bsonOutput);
            sentCommandStartedEvent = true;
            if (this.shouldAcknowledge(encodingMetadata.getNextMessage())) {
                final CommandMessage getLastErrorMessage = new CommandMessage(new MongoNamespace(this.getNamespace().getDatabaseName(), "$cmd").getFullName(), this.createGetLastErrorCommandDocument(), false, ProtocolHelper.getMessageSettings(connection.getDescription()));
                ProtocolHelper.encodeMessage(getLastErrorMessage, bsonOutput);
                final SingleResultCallback<ResponseBuffers> receiveCallback = new WriteResultCallback(callback, new BsonDocumentCodec(), requestMessage, encodingMetadata.getNextMessage(), getLastErrorMessage.getId(), connection, startTimeNanos);
                connection.sendMessageAsync(bsonOutput.getByteBuffers(), getLastErrorMessage.getId(), new SendMessageCallback<Object>(connection, bsonOutput, requestMessage, getLastErrorMessage.getId(), this.getCommandName(requestMessage), startTimeNanos, this.commandListener, callback, receiveCallback));
            }
            else {
                connection.sendMessageAsync(bsonOutput.getByteBuffers(), requestMessage.getId(), new UnacknowledgedWriteResultCallback(callback, requestMessage, encodingMetadata.getNextMessage(), bsonOutput, connection, startTimeNanos));
            }
        }
        catch (Throwable t) {
            this.sendFailedEvent(connection, requestMessage, sentCommandStartedEvent, t, startTimeNanos);
            callback.onResult(null, t);
        }
    }
    
    protected abstract BsonDocument getAsWriteCommand(final ByteBufferBsonOutput p0, final int p1);
    
    protected BsonDocument getBaseCommandDocument(final String commandName) {
        final BsonDocument baseCommandDocument = new BsonDocument(commandName, new BsonString(this.getNamespace().getCollectionName())).append("ordered", BsonBoolean.valueOf(this.isOrdered()));
        if (!this.writeConcern.isServerDefault()) {
            baseCommandDocument.append("writeConcern", this.writeConcern.asDocument());
        }
        return baseCommandDocument;
    }
    
    protected String getCommandName(final RequestMessage message) {
        switch (message.getOpCode()) {
            case OP_INSERT: {
                return "insert";
            }
            case OP_UPDATE: {
                return "update";
            }
            case OP_DELETE: {
                return "delete";
            }
            default: {
                throw new MongoInternalException("Unexpected op code for write: " + message.getOpCode());
            }
        }
    }
    
    private void sendStartedEvent(final InternalConnection connection, final RequestMessage message, final RequestMessage.EncodingMetadata encodingMetadata, final ByteBufferBsonOutput bsonOutput) {
        if (this.commandListener != null) {
            ProtocolHelper.sendCommandStartedEvent(message, this.namespace.getDatabaseName(), this.getCommandName(message), this.getAsWriteCommand(bsonOutput, encodingMetadata.getFirstDocumentPosition()), connection.getDescription(), this.commandListener);
        }
    }
    
    private void sendSucceededEvent(final InternalConnection connection, final RequestMessage message, final RequestMessage nextMessage, final WriteConcernException e, final long startTimeNanos) {
        if (this.commandListener != null) {
            this.sendSucceededEvent(connection, message, this.getResponseDocument(message, nextMessage, e.getWriteConcernResult(), e), startTimeNanos);
        }
    }
    
    private void sendSucceededEvent(final InternalConnection connection, final RequestMessage message, final RequestMessage nextMessage, final WriteConcernResult writeConcernResult, final long startTimeNanos) {
        if (this.commandListener != null) {
            this.sendSucceededEvent(connection, message, this.getResponseDocument(message, nextMessage, writeConcernResult, null), startTimeNanos);
        }
    }
    
    private void sendSucceededEvent(final InternalConnection connection, final RequestMessage message, final BsonDocument responseDocument, final long startTimeNanos) {
        if (this.commandListener != null) {
            ProtocolHelper.sendCommandSucceededEvent(message, this.getCommandName(message), responseDocument, connection.getDescription(), startTimeNanos, this.commandListener);
        }
    }
    
    private void sendFailedEvent(final InternalConnection connection, final RequestMessage message, final boolean sentCommandStartedEvent, final Throwable t, final long startTimeNanos) {
        if (this.commandListener != null && sentCommandStartedEvent) {
            ProtocolHelper.sendCommandFailedEvent(message, this.getCommandName(message), connection.getDescription(), startTimeNanos, t, this.commandListener);
        }
    }
    
    private BsonDocument getResponseDocument(final RequestMessage curMessage, final RequestMessage nextMessage, final WriteConcernResult writeConcernResult, final WriteConcernException writeConcernException) {
        final BsonDocument response = new BsonDocument("ok", new BsonInt32(1));
        if (this.writeConcern.isAcknowledged()) {
            if (writeConcernException == null) {
                this.appendToWriteCommandResponseDocument(curMessage, nextMessage, writeConcernResult, response);
            }
            else {
                response.put("n", new BsonInt32(0));
                final BsonDocument writeErrorDocument = new BsonDocument("index", new BsonInt32(0)).append("code", new BsonInt32(writeConcernException.getErrorCode()));
                if (writeConcernException.getErrorMessage() != null) {
                    writeErrorDocument.append("errmsg", new BsonString(writeConcernException.getErrorMessage()));
                }
                response.put("writeErrors", new BsonArray(Collections.singletonList(writeErrorDocument)));
            }
        }
        return response;
    }
    
    private boolean shouldAcknowledge(final RequestMessage nextMessage) {
        return this.writeConcern.isAcknowledged() || (this.isOrdered() && nextMessage != null);
    }
    
    private BsonDocument createGetLastErrorCommandDocument() {
        final BsonDocument command = new BsonDocument("getlasterror", new BsonInt32(1));
        command.putAll(this.writeConcern.asDocument());
        return command;
    }
    
    protected abstract RequestMessage createRequestMessage(final MessageSettings p0);
    
    protected MongoNamespace getNamespace() {
        return this.namespace;
    }
    
    protected boolean isOrdered() {
        return this.ordered;
    }
    
    protected WriteConcern getWriteConcern() {
        return this.writeConcern;
    }
    
    protected abstract Logger getLogger();
    
    private final class WriteResultCallback extends CommandResultBaseCallback<BsonDocument>
    {
        private final SingleResultCallback<WriteConcernResult> callback;
        private final RequestMessage message;
        private final RequestMessage nextMessage;
        private final InternalConnection connection;
        private final long startTimeNanos;
        
        public WriteResultCallback(final SingleResultCallback<WriteConcernResult> callback, final Decoder<BsonDocument> decoder, final RequestMessage message, final RequestMessage nextMessage, final long requestId, final InternalConnection connection, final long startTimeNanos) {
            super(decoder, requestId, connection.getDescription().getServerAddress());
            this.callback = callback;
            this.message = message;
            this.nextMessage = nextMessage;
            this.connection = connection;
            this.startTimeNanos = startTimeNanos;
        }
        
        @Override
        protected void callCallback(final BsonDocument result, final Throwable throwableFromCallback) {
            if (throwableFromCallback != null) {
                WriteProtocol.this.sendFailedEvent(this.connection, this.message, true, throwableFromCallback, this.startTimeNanos);
                this.callback.onResult(null, throwableFromCallback);
            }
            else {
                try {
                    try {
                        WriteConcernResult writeConcernResult = null;
                        boolean shouldWriteNextMessage = true;
                        try {
                            writeConcernResult = ProtocolHelper.getWriteResult(result, this.connection.getDescription().getServerAddress());
                        }
                        catch (WriteConcernException e) {
                            if (WriteProtocol.this.writeConcern.isAcknowledged()) {
                                throw e;
                            }
                            if (WriteProtocol.this.ordered) {
                                shouldWriteNextMessage = false;
                            }
                        }
                        WriteProtocol.this.sendSucceededEvent(this.connection, this.message, this.nextMessage, writeConcernResult, this.startTimeNanos);
                        if (shouldWriteNextMessage && this.nextMessage != null) {
                            WriteProtocol.this.executeAsync(this.nextMessage, this.connection, this.callback);
                        }
                        else {
                            this.callback.onResult(writeConcernResult, null);
                        }
                    }
                    catch (WriteConcernException e2) {
                        WriteProtocol.this.sendSucceededEvent(this.connection, this.message, this.nextMessage, e2, this.startTimeNanos);
                        throw e2;
                    }
                    catch (RuntimeException e3) {
                        WriteProtocol.this.sendFailedEvent(this.connection, this.message, true, e3, this.startTimeNanos);
                        throw e3;
                    }
                }
                catch (Throwable t) {
                    this.callback.onResult(null, t);
                }
            }
        }
    }
    
    private final class UnacknowledgedWriteResultCallback implements SingleResultCallback<Void>
    {
        private final SingleResultCallback<WriteConcernResult> callback;
        private final RequestMessage message;
        private final RequestMessage nextMessage;
        private final OutputBuffer writtenBuffer;
        private final InternalConnection connection;
        private final long startTimeNanos;
        
        UnacknowledgedWriteResultCallback(final SingleResultCallback<WriteConcernResult> callback, final RequestMessage message, final RequestMessage nextMessage, final OutputBuffer writtenBuffer, final InternalConnection connection, final long startTimeNanos) {
            this.callback = callback;
            this.message = message;
            this.nextMessage = nextMessage;
            this.connection = connection;
            this.writtenBuffer = writtenBuffer;
            this.startTimeNanos = startTimeNanos;
        }
        
        @Override
        public void onResult(final Void result, final Throwable t) {
            this.writtenBuffer.close();
            if (t != null) {
                WriteProtocol.this.sendFailedEvent(this.connection, this.message, true, t, this.startTimeNanos);
                this.callback.onResult(null, t);
            }
            else {
                WriteProtocol.this.sendSucceededEvent(this.connection, this.message, this.nextMessage, null, this.startTimeNanos);
                if (this.nextMessage != null) {
                    WriteProtocol.this.executeAsync(this.nextMessage, this.connection, this.callback);
                }
                else {
                    this.callback.onResult(WriteConcernResult.unacknowledged(), null);
                }
            }
        }
    }
}
