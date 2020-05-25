package com.mongodb.connection;

import org.bson.codecs.*;
import org.bson.*;
import org.bson.io.*;
import com.mongodb.*;
import com.mongodb.event.*;
import com.mongodb.diagnostics.logging.*;
import org.bson.codecs.configuration.*;

final class ProtocolHelper
{
    private static final Logger PROTOCOL_EVENT_LOGGER;
    private static final CodecRegistry REGISTRY;
    
    static WriteConcernResult getWriteResult(final BsonDocument result, final ServerAddress serverAddress) {
        if (!isCommandOk(result)) {
            throw getCommandFailureException(result, serverAddress);
        }
        if (hasWriteError(result)) {
            throwWriteException(result, serverAddress);
        }
        return createWriteResult(result);
    }
    
    private static WriteConcernResult createWriteResult(final BsonDocument result) {
        final BsonBoolean updatedExisting = result.getBoolean("updatedExisting", BsonBoolean.FALSE);
        return WriteConcernResult.acknowledged(result.getNumber("n", new BsonInt32(0)).intValue(), updatedExisting.getValue(), result.get("upserted"));
    }
    
    static boolean isCommandOk(final BsonDocument response) {
        final BsonValue okValue = response.get("ok");
        return isCommandOk(okValue);
    }
    
    static boolean isCommandOk(final BsonReader bsonReader) {
        return isCommandOk(getField(bsonReader, "ok"));
    }
    
    private static BsonValue getField(final BsonReader bsonReader, final String fieldName) {
        bsonReader.readStartDocument();
        while (bsonReader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            if (bsonReader.readName().equals(fieldName)) {
                return ProtocolHelper.REGISTRY.get(BsonValueCodecProvider.getClassForBsonType(bsonReader.getCurrentBsonType())).decode(bsonReader, DecoderContext.builder().build());
            }
            bsonReader.skipValue();
        }
        bsonReader.readEndDocument();
        return null;
    }
    
    private static boolean isCommandOk(final BsonValue okValue) {
        if (okValue == null) {
            return false;
        }
        if (okValue.isBoolean()) {
            return okValue.asBoolean().getValue();
        }
        return okValue.isNumber() && okValue.asNumber().intValue() == 1;
    }
    
    static MongoException getCommandFailureException(final BsonDocument response, final ServerAddress serverAddress) {
        final MongoException specialException = createSpecialException(response, serverAddress, "errmsg");
        if (specialException != null) {
            return specialException;
        }
        return new MongoCommandException(response, serverAddress);
    }
    
    static int getErrorCode(final BsonDocument response) {
        return response.getNumber("code", new BsonInt32(-1)).intValue();
    }
    
    static String getErrorMessage(final BsonDocument response, final String errorMessageFieldName) {
        return response.getString(errorMessageFieldName, new BsonString("")).getValue();
    }
    
    static MongoException getQueryFailureException(final BsonDocument errorDocument, final ServerAddress serverAddress) {
        final MongoException specialException = createSpecialException(errorDocument, serverAddress, "$err");
        if (specialException != null) {
            return specialException;
        }
        return new MongoQueryException(serverAddress, getErrorCode(errorDocument), getErrorMessage(errorDocument, "$err"));
    }
    
    static MessageSettings getMessageSettings(final ConnectionDescription connectionDescription) {
        return MessageSettings.builder().maxDocumentSize(connectionDescription.getMaxDocumentSize()).maxMessageSize(connectionDescription.getMaxMessageSize()).maxBatchCount(connectionDescription.getMaxBatchCount()).build();
    }
    
    static RequestMessage encodeMessage(final RequestMessage message, final BsonOutput bsonOutput) {
        try {
            return message.encode(bsonOutput);
        }
        catch (RuntimeException e) {
            bsonOutput.close();
            throw e;
        }
        catch (Error e2) {
            bsonOutput.close();
            throw e2;
        }
    }
    
    static RequestMessage.EncodingMetadata encodeMessageWithMetadata(final RequestMessage message, final BsonOutput bsonOutput) {
        try {
            return message.encodeWithMetadata(bsonOutput);
        }
        catch (RuntimeException e) {
            bsonOutput.close();
            throw e;
        }
        catch (Error e2) {
            bsonOutput.close();
            throw e2;
        }
    }
    
    private static MongoException createSpecialException(final BsonDocument response, final ServerAddress serverAddress, final String errorMessageFieldName) {
        if (ErrorCategory.fromErrorCode(getErrorCode(response)) == ErrorCategory.EXECUTION_TIMEOUT) {
            return new MongoExecutionTimeoutException(getErrorCode(response), getErrorMessage(response, errorMessageFieldName));
        }
        if (getErrorMessage(response, errorMessageFieldName).startsWith("not master")) {
            return new MongoNotPrimaryException(serverAddress);
        }
        if (getErrorMessage(response, errorMessageFieldName).startsWith("node is recovering")) {
            return new MongoNodeIsRecoveringException(serverAddress);
        }
        return null;
    }
    
    private static boolean hasWriteError(final BsonDocument response) {
        final String err = WriteConcernException.extractErrorMessage(response);
        return err != null && err.length() > 0;
    }
    
    private static void throwWriteException(final BsonDocument result, final ServerAddress serverAddress) {
        final MongoException specialException = createSpecialException(result, serverAddress, "err");
        if (specialException != null) {
            throw specialException;
        }
        final int code = WriteConcernException.extractErrorCode(result);
        if (ErrorCategory.fromErrorCode(code) == ErrorCategory.DUPLICATE_KEY) {
            throw new DuplicateKeyException(result, serverAddress, createWriteResult(result));
        }
        throw new WriteConcernException(result, serverAddress, createWriteResult(result));
    }
    
    static void sendCommandStartedEvent(final RequestMessage message, final String databaseName, final String commandName, final BsonDocument command, final ConnectionDescription connectionDescription, final CommandListener commandListener) {
        try {
            commandListener.commandStarted(new CommandStartedEvent(message.getId(), connectionDescription, databaseName, commandName, command));
        }
        catch (Exception e) {
            if (ProtocolHelper.PROTOCOL_EVENT_LOGGER.isWarnEnabled()) {
                ProtocolHelper.PROTOCOL_EVENT_LOGGER.warn(String.format("Exception thrown raising command started event to listener %s", commandListener), e);
            }
        }
    }
    
    static void sendCommandSucceededEvent(final RequestMessage message, final String commandName, final BsonDocument response, final ConnectionDescription connectionDescription, final long startTimeNanos, final CommandListener commandListener) {
        try {
            commandListener.commandSucceeded(new CommandSucceededEvent(message.getId(), connectionDescription, commandName, response, System.nanoTime() - startTimeNanos));
        }
        catch (Exception e) {
            if (ProtocolHelper.PROTOCOL_EVENT_LOGGER.isWarnEnabled()) {
                ProtocolHelper.PROTOCOL_EVENT_LOGGER.warn(String.format("Exception thrown raising command succeeded event to listener %s", commandListener), e);
            }
        }
    }
    
    static void sendCommandFailedEvent(final RequestMessage message, final String commandName, final ConnectionDescription connectionDescription, final long startTimeNanos, final Throwable throwable, final CommandListener commandListener) {
        try {
            commandListener.commandFailed(new CommandFailedEvent(message.getId(), connectionDescription, commandName, System.nanoTime() - startTimeNanos, throwable));
        }
        catch (Exception e) {
            if (ProtocolHelper.PROTOCOL_EVENT_LOGGER.isWarnEnabled()) {
                ProtocolHelper.PROTOCOL_EVENT_LOGGER.warn(String.format("Exception thrown raising command failed event to listener %s", commandListener), e);
            }
        }
    }
    
    private ProtocolHelper() {
    }
    
    static {
        PROTOCOL_EVENT_LOGGER = Loggers.getLogger("protocol.event");
        REGISTRY = CodecRegistries.fromProviders(new BsonValueCodecProvider());
    }
}
