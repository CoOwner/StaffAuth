package com.mongodb.connection;

import com.mongodb.event.*;
import com.mongodb.internal.connection.*;
import com.mongodb.*;
import org.bson.*;
import com.mongodb.async.*;
import com.mongodb.bulk.*;
import org.bson.codecs.*;
import org.bson.io.*;
import com.mongodb.diagnostics.logging.*;

abstract class WriteCommandProtocol implements Protocol<BulkWriteResult>
{
    private final MongoNamespace namespace;
    private final boolean ordered;
    private final WriteConcern writeConcern;
    private final Boolean bypassDocumentValidation;
    private CommandListener commandListener;
    
    public WriteCommandProtocol(final MongoNamespace namespace, final boolean ordered, final WriteConcern writeConcern, final Boolean bypassDocumentValidation) {
        this.namespace = namespace;
        this.ordered = ordered;
        this.writeConcern = writeConcern;
        this.bypassDocumentValidation = bypassDocumentValidation;
    }
    
    @Override
    public void setCommandListener(final CommandListener commandListener) {
        this.commandListener = commandListener;
    }
    
    public WriteConcern getWriteConcern() {
        return this.writeConcern;
    }
    
    public Boolean getBypassDocumentValidation() {
        return this.bypassDocumentValidation;
    }
    
    @Override
    public BulkWriteResult execute(final InternalConnection connection) {
        BaseWriteCommandMessage message = this.createRequestMessage(ProtocolHelper.getMessageSettings(connection.getDescription()));
        long startTimeNanos = System.nanoTime();
        try {
            final BulkWriteBatchCombiner bulkWriteBatchCombiner = new BulkWriteBatchCombiner(connection.getDescription().getServerAddress(), this.ordered, this.writeConcern);
            int batchNum = 0;
            int currentRangeStartIndex = 0;
            do {
                ++batchNum;
                startTimeNanos = System.nanoTime();
                final BaseWriteCommandMessage nextMessage = this.sendMessage(connection, message, batchNum);
                final int itemCount = (nextMessage != null) ? (message.getItemCount() - nextMessage.getItemCount()) : message.getItemCount();
                final IndexMap indexMap = IndexMap.create(currentRangeStartIndex, itemCount);
                final BsonDocument result = this.receiveMessage(connection, message);
                if ((nextMessage != null || batchNum > 1) && this.getLogger().isDebugEnabled()) {
                    this.getLogger().debug(String.format("Received response for batch %d", batchNum));
                }
                if (WriteCommandResultHelper.hasError(result)) {
                    final MongoBulkWriteException bulkWriteException = WriteCommandResultHelper.getBulkWriteException(this.getType(), result, connection.getDescription().getServerAddress());
                    bulkWriteBatchCombiner.addErrorResult(bulkWriteException, indexMap);
                }
                else {
                    bulkWriteBatchCombiner.addResult(WriteCommandResultHelper.getBulkWriteResult(this.getType(), result), indexMap);
                }
                this.sendSucceededEvent(connection, message, startTimeNanos, result);
                currentRangeStartIndex += itemCount;
                message = nextMessage;
            } while (message != null && !bulkWriteBatchCombiner.shouldStopSendingMoreBatches());
            return bulkWriteBatchCombiner.getResult();
        }
        catch (MongoBulkWriteException e) {
            throw e;
        }
        catch (RuntimeException e2) {
            this.sendFailedEvent(connection, message, startTimeNanos, e2);
            throw e2;
        }
    }
    
    @Override
    public void executeAsync(final InternalConnection connection, final SingleResultCallback<BulkWriteResult> callback) {
        this.executeBatchesAsync(connection, this.createRequestMessage(ProtocolHelper.getMessageSettings(connection.getDescription())), new BulkWriteBatchCombiner(connection.getDescription().getServerAddress(), this.ordered, this.writeConcern), 0, 0, callback);
    }
    
    private void executeBatchesAsync(final InternalConnection connection, final BaseWriteCommandMessage message, final BulkWriteBatchCombiner bulkWriteBatchCombiner, final int batchNum, final int currentRangeStartIndex, final SingleResultCallback<BulkWriteResult> callback) {
        final long startTimeNanos = System.nanoTime();
        boolean sentStartedEvent = false;
        try {
            if (message != null && !bulkWriteBatchCombiner.shouldStopSendingMoreBatches()) {
                final ByteBufferBsonOutput bsonOutput = new ByteBufferBsonOutput(connection);
                final RequestMessage.EncodingMetadata metadata = message.encodeWithMetadata(bsonOutput);
                this.sendStartedEvent(connection, message, bsonOutput, metadata);
                sentStartedEvent = true;
                final BaseWriteCommandMessage nextMessage = (BaseWriteCommandMessage)metadata.getNextMessage();
                final int itemCount = (nextMessage != null) ? (message.getItemCount() - nextMessage.getItemCount()) : message.getItemCount();
                final IndexMap indexMap = IndexMap.create(currentRangeStartIndex, itemCount);
                final int nextBatchNum = batchNum + 1;
                final int nextRangeStartIndex = currentRangeStartIndex + itemCount;
                if (nextBatchNum > 1 && this.getLogger().isDebugEnabled()) {
                    this.getLogger().debug(String.format("Asynchronously sending batch %d", batchNum));
                }
                this.sendMessageAsync(connection, bsonOutput, message, startTimeNanos, callback, new SingleResultCallback<BsonDocument>() {
                    @Override
                    public void onResult(final BsonDocument result, final Throwable t) {
                        bsonOutput.close();
                        if (t != null) {
                            WriteCommandProtocol.this.sendFailedEvent(connection, message, startTimeNanos, t);
                            callback.onResult(null, t);
                        }
                        else {
                            WriteCommandProtocol.this.sendSucceededEvent(connection, message, startTimeNanos, result);
                            if (nextBatchNum > 1 && WriteCommandProtocol.this.getLogger().isDebugEnabled()) {
                                WriteCommandProtocol.this.getLogger().debug(String.format("Asynchronously received response for batch %d", batchNum));
                            }
                            if (WriteCommandResultHelper.hasError(result)) {
                                bulkWriteBatchCombiner.addErrorResult(WriteCommandResultHelper.getBulkWriteException(WriteCommandProtocol.this.getType(), result, connection.getDescription().getServerAddress()), indexMap);
                            }
                            else {
                                bulkWriteBatchCombiner.addResult(WriteCommandResultHelper.getBulkWriteResult(WriteCommandProtocol.this.getType(), result), indexMap);
                            }
                            WriteCommandProtocol.this.executeBatchesAsync(connection, nextMessage, bulkWriteBatchCombiner, nextBatchNum, nextRangeStartIndex, callback);
                        }
                    }
                });
            }
            else if (bulkWriteBatchCombiner.hasErrors()) {
                callback.onResult(null, bulkWriteBatchCombiner.getError());
            }
            else {
                callback.onResult(bulkWriteBatchCombiner.getResult(), null);
            }
        }
        catch (Throwable t) {
            if (sentStartedEvent) {
                this.sendFailedEvent(connection, message, startTimeNanos, t);
            }
            callback.onResult(null, t);
        }
    }
    
    protected abstract WriteRequest.Type getType();
    
    protected abstract BaseWriteCommandMessage createRequestMessage(final MessageSettings p0);
    
    private BaseWriteCommandMessage sendMessage(final InternalConnection connection, final BaseWriteCommandMessage message, final int batchNum) {
        final ByteBufferBsonOutput bsonOutput = new ByteBufferBsonOutput(connection);
        try {
            final RequestMessage.EncodingMetadata encodingMetadata = message.encodeWithMetadata(bsonOutput);
            final BaseWriteCommandMessage nextMessage = (BaseWriteCommandMessage)encodingMetadata.getNextMessage();
            this.sendStartedEvent(connection, message, bsonOutput, encodingMetadata);
            if ((nextMessage != null || batchNum > 1) && this.getLogger().isDebugEnabled()) {
                this.getLogger().debug(String.format("Sending batch %d", batchNum));
            }
            connection.sendMessage(bsonOutput.getByteBuffers(), message.getId());
            return nextMessage;
        }
        finally {
            bsonOutput.close();
        }
    }
    
    private void sendStartedEvent(final InternalConnection connection, final BaseWriteCommandMessage message, final ByteBufferBsonOutput bsonOutput, final RequestMessage.EncodingMetadata encodingMetadata) {
        if (this.commandListener != null) {
            ProtocolHelper.sendCommandStartedEvent(message, this.namespace.getDatabaseName(), message.getCommandName(), ByteBufBsonDocument.createOne(bsonOutput, encodingMetadata.getFirstDocumentPosition()), connection.getDescription(), this.commandListener);
        }
    }
    
    private void sendSucceededEvent(final InternalConnection connection, final BaseWriteCommandMessage message, final long startTimeNanos, final BsonDocument result) {
        if (this.commandListener != null) {
            ProtocolHelper.sendCommandSucceededEvent(message, message.getCommandName(), result, connection.getDescription(), startTimeNanos, this.commandListener);
        }
    }
    
    private void sendFailedEvent(final InternalConnection connection, final BaseWriteCommandMessage message, final long startTimeNanos, final Throwable t) {
        if (this.commandListener != null) {
            ProtocolHelper.sendCommandFailedEvent(message, message.getCommandName(), connection.getDescription(), startTimeNanos, t, this.commandListener);
        }
    }
    
    private BsonDocument receiveMessage(final InternalConnection connection, final RequestMessage message) {
        final ResponseBuffers responseBuffers = connection.receiveMessage(message.getId());
        try {
            final ReplyMessage<BsonDocument> replyMessage = new ReplyMessage<BsonDocument>(responseBuffers, new BsonDocumentCodec(), message.getId());
            final BsonDocument result = replyMessage.getDocuments().get(0);
            if (!ProtocolHelper.isCommandOk(result)) {
                throw ProtocolHelper.getCommandFailureException(result, connection.getDescription().getServerAddress());
            }
            return result;
        }
        finally {
            responseBuffers.close();
        }
    }
    
    private void sendMessageAsync(final InternalConnection connection, final ByteBufferBsonOutput buffer, final BaseWriteCommandMessage message, final long startTimeNanos, final SingleResultCallback<BulkWriteResult> clientCallback, final SingleResultCallback<BsonDocument> callback) {
        final SingleResultCallback<ResponseBuffers> receiveCallback = new CommandResultCallback<Object>(callback, new BsonDocumentCodec(), message.getId(), connection.getDescription().getServerAddress());
        connection.sendMessageAsync(buffer.getByteBuffers(), message.getId(), new SendMessageCallback<Object>(connection, buffer, message, message.getCommandName(), startTimeNanos, this.commandListener, clientCallback, receiveCallback));
    }
    
    public MongoNamespace getNamespace() {
        return this.namespace;
    }
    
    protected abstract Logger getLogger();
    
    protected boolean isOrdered() {
        return this.ordered;
    }
}
