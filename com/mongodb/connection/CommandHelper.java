package com.mongodb.connection;

import com.mongodb.async.*;
import org.bson.*;
import org.bson.io.*;
import org.bson.codecs.*;
import com.mongodb.*;

final class CommandHelper
{
    static BsonDocument executeCommand(final String database, final BsonDocument command, final InternalConnection internalConnection) {
        return receiveCommandResult(internalConnection, sendMessage(database, command, internalConnection));
    }
    
    static void executeCommandAsync(final String database, final BsonDocument command, final InternalConnection internalConnection, final SingleResultCallback<BsonDocument> callback) {
        sendMessageAsync(database, command, internalConnection, new SingleResultCallback<CommandMessage>() {
            @Override
            public void onResult(final CommandMessage result, final Throwable t) {
                if (t != null) {
                    callback.onResult(null, t);
                }
                else {
                    receiveReplyAsync(internalConnection, result, new SingleResultCallback<ReplyMessage<BsonDocument>>() {
                        @Override
                        public void onResult(final ReplyMessage<BsonDocument> result, final Throwable t) {
                            if (t != null) {
                                callback.onResult(null, t);
                            }
                            else {
                                final BsonDocument reply = result.getDocuments().get(0);
                                if (!CommandHelper.isCommandOk(reply)) {
                                    callback.onResult(null, createCommandFailureException(reply, internalConnection));
                                }
                                else {
                                    callback.onResult(reply, null);
                                }
                            }
                        }
                    });
                }
            }
        });
    }
    
    static BsonDocument executeCommandWithoutCheckingForFailure(final String database, final BsonDocument command, final InternalConnection internalConnection) {
        return receiveCommandDocument(internalConnection, sendMessage(database, command, internalConnection));
    }
    
    static boolean isCommandOk(final BsonDocument response) {
        if (!response.containsKey("ok")) {
            return false;
        }
        final BsonValue okValue = response.get("ok");
        if (okValue.isBoolean()) {
            return okValue.asBoolean().getValue();
        }
        return okValue.isNumber() && okValue.asNumber().intValue() == 1;
    }
    
    private static CommandMessage sendMessage(final String database, final BsonDocument command, final InternalConnection internalConnection) {
        final ByteBufferBsonOutput bsonOutput = new ByteBufferBsonOutput(internalConnection);
        try {
            final CommandMessage message = new CommandMessage(new MongoNamespace(database, "$cmd").getFullName(), command, false, MessageSettings.builder().build());
            message.encode(bsonOutput);
            internalConnection.sendMessage(bsonOutput.getByteBuffers(), message.getId());
            return message;
        }
        finally {
            bsonOutput.close();
        }
    }
    
    private static void sendMessageAsync(final String database, final BsonDocument command, final InternalConnection internalConnection, final SingleResultCallback<CommandMessage> callback) {
        final ByteBufferBsonOutput bsonOutput = new ByteBufferBsonOutput(internalConnection);
        try {
            final CommandMessage message = new CommandMessage(new MongoNamespace(database, "$cmd").getFullName(), command, false, MessageSettings.builder().build());
            message.encode(bsonOutput);
            internalConnection.sendMessageAsync(bsonOutput.getByteBuffers(), message.getId(), new SingleResultCallback<Void>() {
                @Override
                public void onResult(final Void result, final Throwable t) {
                    bsonOutput.close();
                    if (t != null) {
                        callback.onResult(null, t);
                    }
                    else {
                        callback.onResult(message, null);
                    }
                }
            });
        }
        catch (Throwable t) {
            callback.onResult(null, t);
        }
    }
    
    private static BsonDocument receiveCommandResult(final InternalConnection internalConnection, final CommandMessage message) {
        final BsonDocument result = receiveReply(internalConnection, message).getDocuments().get(0);
        if (!isCommandOk(result)) {
            throw createCommandFailureException(result, internalConnection);
        }
        return result;
    }
    
    private static BsonDocument receiveCommandDocument(final InternalConnection internalConnection, final CommandMessage message) {
        return receiveReply(internalConnection, message).getDocuments().get(0);
    }
    
    private static ReplyMessage<BsonDocument> receiveReply(final InternalConnection internalConnection, final CommandMessage message) {
        final ResponseBuffers responseBuffers = internalConnection.receiveMessage(message.getId());
        if (responseBuffers == null) {
            throw new MongoInternalException(String.format("Response buffers received from %s should not be null", internalConnection));
        }
        try {
            return new ReplyMessage<BsonDocument>(responseBuffers, new BsonDocumentCodec(), message.getId());
        }
        finally {
            responseBuffers.close();
        }
    }
    
    private static void receiveReplyAsync(final InternalConnection internalConnection, final CommandMessage message, final SingleResultCallback<ReplyMessage<BsonDocument>> callback) {
        internalConnection.receiveMessageAsync(message.getId(), new SingleResultCallback<ResponseBuffers>() {
            @Override
            public void onResult(final ResponseBuffers result, final Throwable t) {
                try {
                    if (t != null) {
                        callback.onResult(null, t);
                    }
                    else {
                        callback.onResult(new ReplyMessage(result, (Decoder<Object>)new BsonDocumentCodec(), message.getId()), null);
                    }
                }
                finally {
                    if (result != null) {
                        result.close();
                    }
                }
            }
        });
    }
    
    private static MongoCommandException createCommandFailureException(final BsonDocument reply, final InternalConnection internalConnection) {
        return new MongoCommandException(reply, internalConnection.getDescription().getServerAddress());
    }
    
    private CommandHelper() {
    }
}
