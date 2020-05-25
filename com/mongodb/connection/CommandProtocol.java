package com.mongodb.connection;

import com.mongodb.event.*;
import com.mongodb.assertions.*;
import org.bson.*;
import org.bson.codecs.*;
import com.mongodb.async.*;
import org.bson.io.*;
import com.mongodb.*;
import com.mongodb.diagnostics.logging.*;
import java.util.*;

class CommandProtocol<T> implements Protocol<T>
{
    public static final Logger LOGGER;
    private static final Set<String> SECURITY_SENSITIVE_COMMANDS;
    private final MongoNamespace namespace;
    private final BsonDocument command;
    private final Decoder<T> commandResultDecoder;
    private final FieldNameValidator fieldNameValidator;
    private boolean slaveOk;
    private CommandListener commandListener;
    private volatile String commandName;
    
    public CommandProtocol(final String database, final BsonDocument command, final FieldNameValidator fieldNameValidator, final Decoder<T> commandResultDecoder) {
        Assertions.notNull("database", database);
        this.namespace = new MongoNamespace(database, "$cmd");
        this.command = Assertions.notNull("command", command);
        this.commandResultDecoder = Assertions.notNull("commandResultDecoder", commandResultDecoder);
        this.fieldNameValidator = Assertions.notNull("fieldNameValidator", fieldNameValidator);
    }
    
    public boolean isSlaveOk() {
        return this.slaveOk;
    }
    
    public CommandProtocol<T> slaveOk(final boolean slaveOk) {
        this.slaveOk = slaveOk;
        return this;
    }
    
    @Override
    public T execute(final InternalConnection connection) {
        if (CommandProtocol.LOGGER.isDebugEnabled()) {
            CommandProtocol.LOGGER.debug(String.format("Sending command {%s : %s} to database %s on connection [%s] to server %s", this.getCommandName(), this.command.values().iterator().next(), this.namespace.getDatabaseName(), connection.getDescription().getConnectionId(), connection.getDescription().getServerAddress()));
        }
        final long startTimeNanos = System.nanoTime();
        final CommandMessage commandMessage = new CommandMessage(this.namespace.getFullName(), this.command, this.slaveOk, this.fieldNameValidator, ProtocolHelper.getMessageSettings(connection.getDescription()));
        ResponseBuffers responseBuffers = null;
        try {
            this.sendMessage(commandMessage, connection);
            responseBuffers = connection.receiveMessage(commandMessage.getId());
            if (!ProtocolHelper.isCommandOk(new BsonBinaryReader(new ByteBufferBsonInput(responseBuffers.getBodyByteBuffer())))) {
                throw ProtocolHelper.getCommandFailureException(getResponseDocument(responseBuffers, commandMessage, (Decoder<BsonDocument>)new BsonDocumentCodec()), connection.getDescription().getServerAddress());
            }
            final T retval = getResponseDocument(responseBuffers, commandMessage, this.commandResultDecoder);
            if (this.commandListener != null) {
                this.sendSucceededEvent(connection.getDescription(), startTimeNanos, commandMessage, getResponseDocument(responseBuffers, commandMessage, (Decoder<BsonDocument>)new RawBsonDocumentCodec()));
            }
            CommandProtocol.LOGGER.debug("Command execution completed");
            return retval;
        }
        catch (RuntimeException e) {
            this.sendFailedEvent(connection.getDescription(), startTimeNanos, commandMessage, e);
            throw e;
        }
        finally {
            if (responseBuffers != null) {
                responseBuffers.close();
            }
        }
    }
    
    private static <D> D getResponseDocument(final ResponseBuffers responseBuffers, final CommandMessage commandMessage, final Decoder<D> decoder) {
        responseBuffers.reset();
        final ReplyMessage<D> replyMessage = new ReplyMessage<D>(responseBuffers, decoder, commandMessage.getId());
        return replyMessage.getDocuments().get(0);
    }
    
    @Override
    public void executeAsync(final InternalConnection connection, final SingleResultCallback<T> callback) {
        final long startTimeNanos = System.nanoTime();
        final CommandMessage message = new CommandMessage(this.namespace.getFullName(), this.command, this.slaveOk, this.fieldNameValidator, ProtocolHelper.getMessageSettings(connection.getDescription()));
        boolean sentStartedEvent = false;
        try {
            if (CommandProtocol.LOGGER.isDebugEnabled()) {
                CommandProtocol.LOGGER.debug(String.format("Asynchronously sending command {%s : %s} to database %s on connection [%s] to server %s", this.getCommandName(), this.command.values().iterator().next(), this.namespace.getDatabaseName(), connection.getDescription().getConnectionId(), connection.getDescription().getServerAddress()));
            }
            final ByteBufferBsonOutput bsonOutput = new ByteBufferBsonOutput(connection);
            final int documentPosition = ProtocolHelper.encodeMessageWithMetadata(message, bsonOutput).getFirstDocumentPosition();
            this.sendStartedEvent(connection, bsonOutput, message, documentPosition);
            sentStartedEvent = true;
            final SingleResultCallback<ResponseBuffers> receiveCallback = new CommandResultCallback(callback, message, connection.getDescription(), startTimeNanos);
            connection.sendMessageAsync(bsonOutput.getByteBuffers(), message.getId(), new SendMessageCallback<Object>(connection, bsonOutput, message, this.getCommandName(), startTimeNanos, this.commandListener, callback, receiveCallback));
        }
        catch (Throwable t) {
            if (sentStartedEvent) {
                this.sendFailedEvent(connection.getDescription(), startTimeNanos, message, t);
            }
            callback.onResult(null, t);
        }
    }
    
    @Override
    public void setCommandListener(final CommandListener commandListener) {
        this.commandListener = commandListener;
    }
    
    private String getCommandName() {
        return (this.commandName != null) ? this.commandName : this.command.keySet().iterator().next();
    }
    
    private void sendMessage(final CommandMessage message, final InternalConnection connection) {
        final ByteBufferBsonOutput bsonOutput = new ByteBufferBsonOutput(connection);
        try {
            final int documentPosition = message.encodeWithMetadata(bsonOutput).getFirstDocumentPosition();
            this.sendStartedEvent(connection, bsonOutput, message, documentPosition);
            connection.sendMessage(bsonOutput.getByteBuffers(), message.getId());
        }
        finally {
            bsonOutput.close();
        }
    }
    
    private void sendStartedEvent(final InternalConnection connection, final ByteBufferBsonOutput bsonOutput, final CommandMessage message, final int documentPosition) {
        if (this.commandListener != null) {
            final ByteBufBsonDocument byteBufBsonDocument = ByteBufBsonDocument.createOne(bsonOutput, documentPosition);
            if (byteBufBsonDocument.containsKey("$query")) {
                final BsonDocument commandDocument = byteBufBsonDocument.getDocument("$query");
                this.commandName = commandDocument.keySet().iterator().next();
            }
            else {
                final BsonDocument commandDocument = byteBufBsonDocument;
                this.commandName = byteBufBsonDocument.getFirstKey();
            }
            BsonDocument commandDocument;
            final BsonDocument commandDocumentForEvent = CommandProtocol.SECURITY_SENSITIVE_COMMANDS.contains(this.commandName) ? new BsonDocument() : commandDocument;
            ProtocolHelper.sendCommandStartedEvent(message, this.namespace.getDatabaseName(), this.commandName, commandDocumentForEvent, connection.getDescription(), this.commandListener);
        }
    }
    
    private void sendSucceededEvent(final ConnectionDescription connectionDescription, final long startTimeNanos, final CommandMessage commandMessage, final BsonDocument response) {
        if (this.commandListener != null) {
            final BsonDocument responseDocumentForEvent = CommandProtocol.SECURITY_SENSITIVE_COMMANDS.contains(this.getCommandName()) ? new BsonDocument() : response;
            ProtocolHelper.sendCommandSucceededEvent(commandMessage, this.getCommandName(), responseDocumentForEvent, connectionDescription, startTimeNanos, this.commandListener);
        }
    }
    
    private void sendFailedEvent(final ConnectionDescription connectionDescription, final long startTimeNanos, final CommandMessage commandMessage, final Throwable t) {
        if (this.commandListener != null) {
            Throwable commandEventException = t;
            if (t instanceof MongoCommandException && CommandProtocol.SECURITY_SENSITIVE_COMMANDS.contains(this.getCommandName())) {
                commandEventException = new MongoCommandException(new BsonDocument(), connectionDescription.getServerAddress());
            }
            ProtocolHelper.sendCommandFailedEvent(commandMessage, this.getCommandName(), connectionDescription, startTimeNanos, commandEventException, this.commandListener);
        }
    }
    
    static {
        LOGGER = Loggers.getLogger("protocol.command");
        SECURITY_SENSITIVE_COMMANDS = new HashSet<String>(Arrays.asList("authenticate", "saslStart", "saslContinue", "getnonce", "createUser", "updateUser", "copydbgetnonce", "copydbsaslstart", "copydb"));
    }
    
    class CommandResultCallback extends ResponseCallback
    {
        private final SingleResultCallback<T> callback;
        private final CommandMessage message;
        private final ConnectionDescription connectionDescription;
        private final long startTimeNanos;
        
        CommandResultCallback(final SingleResultCallback<T> callback, final CommandMessage message, final ConnectionDescription connectionDescription, final long startTimeNanos) {
            super(message.getId(), connectionDescription.getServerAddress());
            this.callback = callback;
            this.message = message;
            this.connectionDescription = connectionDescription;
            this.startTimeNanos = startTimeNanos;
        }
        
        @Override
        protected void callCallback(final ResponseBuffers responseBuffers, final Throwable throwableFromCallback) {
            try {
                if (throwableFromCallback != null) {
                    throw throwableFromCallback;
                }
                if (CommandProtocol.LOGGER.isDebugEnabled()) {
                    CommandProtocol.LOGGER.debug("Command execution completed");
                }
                if (!ProtocolHelper.isCommandOk(new BsonBinaryReader(new ByteBufferBsonInput(responseBuffers.getBodyByteBuffer())))) {
                    throw ProtocolHelper.getCommandFailureException((BsonDocument)getResponseDocument(responseBuffers, this.message, (Decoder<Object>)new BsonDocumentCodec()), this.connectionDescription.getServerAddress());
                }
                if (CommandProtocol.this.commandListener != null) {
                    CommandProtocol.this.sendSucceededEvent(this.connectionDescription, this.startTimeNanos, this.message, (BsonDocument)getResponseDocument(responseBuffers, this.message, (Decoder<Object>)new RawBsonDocumentCodec()));
                }
                this.callback.onResult((T)getResponseDocument(responseBuffers, this.message, (Decoder<Object>)CommandProtocol.this.commandResultDecoder), null);
            }
            catch (Throwable t) {
                CommandProtocol.this.sendFailedEvent(this.connectionDescription, this.startTimeNanos, this.message, t);
                this.callback.onResult(null, t);
            }
            finally {
                if (responseBuffers != null) {
                    responseBuffers.close();
                }
            }
        }
    }
}
