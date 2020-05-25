package com.mongodb.connection;

import java.util.concurrent.atomic.*;
import org.bson.io.*;
import org.bson.*;
import org.bson.codecs.*;
import org.bson.codecs.configuration.*;

abstract class RequestMessage
{
    static final AtomicInteger REQUEST_ID;
    private static final int QUERY_DOCUMENT_HEADROOM = 16384;
    private static final CodecRegistry REGISTRY;
    private final String collectionName;
    private final MessageSettings settings;
    private final int id;
    private final OpCode opCode;
    
    public static int getCurrentGlobalId() {
        return RequestMessage.REQUEST_ID.get();
    }
    
    public RequestMessage(final OpCode opCode, final MessageSettings settings) {
        this(null, opCode, settings);
    }
    
    public RequestMessage(final String collectionName, final OpCode opCode, final MessageSettings settings) {
        this.collectionName = collectionName;
        this.settings = settings;
        this.id = RequestMessage.REQUEST_ID.getAndIncrement();
        this.opCode = opCode;
    }
    
    public int getId() {
        return this.id;
    }
    
    public OpCode getOpCode() {
        return this.opCode;
    }
    
    public String getNamespace() {
        return (this.getCollectionName() != null) ? this.getCollectionName() : null;
    }
    
    public MessageSettings getSettings() {
        return this.settings;
    }
    
    public RequestMessage encode(final BsonOutput bsonOutput) {
        return this.encodeWithMetadata(bsonOutput).getNextMessage();
    }
    
    public EncodingMetadata encodeWithMetadata(final BsonOutput bsonOutput) {
        final int messageStartPosition = bsonOutput.getPosition();
        this.writeMessagePrologue(bsonOutput);
        final EncodingMetadata encodingMetadata = this.encodeMessageBodyWithMetadata(bsonOutput, messageStartPosition);
        this.backpatchMessageLength(messageStartPosition, bsonOutput);
        return encodingMetadata;
    }
    
    protected void writeMessagePrologue(final BsonOutput bsonOutput) {
        bsonOutput.writeInt32(0);
        bsonOutput.writeInt32(this.id);
        bsonOutput.writeInt32(0);
        bsonOutput.writeInt32(this.opCode.getValue());
    }
    
    protected abstract RequestMessage encodeMessageBody(final BsonOutput p0, final int p1);
    
    protected abstract EncodingMetadata encodeMessageBodyWithMetadata(final BsonOutput p0, final int p1);
    
    protected <T> void addDocument(final BsonDocument document, final BsonOutput bsonOutput, final FieldNameValidator validator) {
        this.addDocument(document, this.getCodec(document), EncoderContext.builder().build(), bsonOutput, validator, this.settings.getMaxDocumentSize() + 16384);
    }
    
    protected void addCollectibleDocument(final BsonDocument document, final BsonOutput bsonOutput, final FieldNameValidator validator) {
        this.addDocument(document, this.getCodec(document), EncoderContext.builder().isEncodingCollectibleDocument(true).build(), bsonOutput, validator, this.settings.getMaxDocumentSize());
    }
    
    protected void backpatchMessageLength(final int startPosition, final BsonOutput bsonOutput) {
        final int messageLength = bsonOutput.getPosition() - startPosition;
        bsonOutput.writeInt32(bsonOutput.getPosition() - messageLength, messageLength);
    }
    
    protected String getCollectionName() {
        return this.collectionName;
    }
    
    Codec<BsonDocument> getCodec(final BsonDocument document) {
        return RequestMessage.REGISTRY.get(document.getClass());
    }
    
    private <T> void addDocument(final T obj, final Encoder<T> encoder, final EncoderContext encoderContext, final BsonOutput bsonOutput, final FieldNameValidator validator, final int maxDocumentSize) {
        final BsonBinaryWriter writer = new BsonBinaryWriter(new BsonWriterSettings(), new BsonBinaryWriterSettings(maxDocumentSize), bsonOutput, validator);
        try {
            encoder.encode(writer, obj, encoderContext);
        }
        finally {
            writer.close();
        }
    }
    
    static {
        REQUEST_ID = new AtomicInteger(1);
        REGISTRY = CodecRegistries.fromProviders(new BsonValueCodecProvider());
    }
    
    static class EncodingMetadata
    {
        private final RequestMessage nextMessage;
        private final int firstDocumentPosition;
        
        EncodingMetadata(final RequestMessage nextMessage, final int firstDocumentPosition) {
            this.nextMessage = nextMessage;
            this.firstDocumentPosition = firstDocumentPosition;
        }
        
        public RequestMessage getNextMessage() {
            return this.nextMessage;
        }
        
        public int getFirstDocumentPosition() {
            return this.firstDocumentPosition;
        }
    }
    
    enum OpCode
    {
        OP_REPLY(1), 
        OP_MSG(1000), 
        OP_UPDATE(2001), 
        OP_INSERT(2002), 
        OP_QUERY(2004), 
        OP_GETMORE(2005), 
        OP_DELETE(2006), 
        OP_KILL_CURSORS(2007);
        
        private final int value;
        
        private OpCode(final int value) {
            this.value = value;
        }
        
        public int getValue() {
            return this.value;
        }
    }
}
