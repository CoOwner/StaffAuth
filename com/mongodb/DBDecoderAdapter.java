package com.mongodb;

import org.bson.codecs.*;
import com.mongodb.connection.*;
import org.bson.*;
import org.bson.io.*;
import java.io.*;

class DBDecoderAdapter implements Decoder<DBObject>
{
    private final DBDecoder decoder;
    private final DBCollection collection;
    private final BufferProvider bufferProvider;
    
    public DBDecoderAdapter(final DBDecoder decoder, final DBCollection collection, final BufferProvider bufferProvider) {
        this.decoder = decoder;
        this.collection = collection;
        this.bufferProvider = bufferProvider;
    }
    
    @Override
    public DBObject decode(final BsonReader reader, final DecoderContext decoderContext) {
        final ByteBufferBsonOutput bsonOutput = new ByteBufferBsonOutput(this.bufferProvider);
        final BsonBinaryWriter binaryWriter = new BsonBinaryWriter(bsonOutput);
        try {
            binaryWriter.pipe(reader);
            final BufferExposingByteArrayOutputStream byteArrayOutputStream = new BufferExposingByteArrayOutputStream(binaryWriter.getBsonOutput().getSize());
            bsonOutput.pipe(byteArrayOutputStream);
            return this.decoder.decode(byteArrayOutputStream.getInternalBytes(), this.collection);
        }
        catch (IOException e) {
            throw new MongoInternalException("An unlikely IOException thrown.", e);
        }
        finally {
            binaryWriter.close();
            bsonOutput.close();
        }
    }
    
    private static class BufferExposingByteArrayOutputStream extends ByteArrayOutputStream
    {
        BufferExposingByteArrayOutputStream(final int size) {
            super(size);
        }
        
        byte[] getInternalBytes() {
            return this.buf;
        }
    }
}
