package com.mongodb.connection;

import java.nio.*;
import java.util.*;
import org.bson.json.*;
import org.bson.io.*;
import org.bson.*;
import org.bson.codecs.*;
import java.io.*;
import org.bson.codecs.configuration.*;

class ByteBufBsonDocument extends BsonDocument implements Cloneable
{
    private static final long serialVersionUID = 1L;
    private static final CodecRegistry REGISTRY;
    private final transient ByteBuf byteBuf;
    
    static List<ByteBufBsonDocument> create(final ResponseBuffers responseBuffers) {
        final int numDocuments = responseBuffers.getReplyHeader().getNumberReturned();
        final ByteBuf documentsBuffer = responseBuffers.getBodyByteBuffer();
        documentsBuffer.order(ByteOrder.LITTLE_ENDIAN);
        final List<ByteBufBsonDocument> documents = new ArrayList<ByteBufBsonDocument>(numDocuments);
        while (documents.size() < numDocuments) {
            final int documentSizeInBytes = documentsBuffer.getInt();
            documentsBuffer.position(documentsBuffer.position() - 4);
            final ByteBuf documentBuffer = documentsBuffer.duplicate();
            documentBuffer.limit(documentBuffer.position() + documentSizeInBytes);
            documents.add(new ByteBufBsonDocument(documentBuffer));
            documentsBuffer.position(documentsBuffer.position() + documentSizeInBytes);
        }
        return documents;
    }
    
    static ByteBufBsonDocument createOne(final ByteBufferBsonOutput bsonOutput, final int startPosition) {
        return create(bsonOutput, startPosition).get(0);
    }
    
    static List<ByteBufBsonDocument> create(final ByteBufferBsonOutput bsonOutput, final int startPosition) {
        final CompositeByteBuf outputByteBuf = new CompositeByteBuf(bsonOutput.getByteBuffers());
        outputByteBuf.position(startPosition);
        final List<ByteBufBsonDocument> documents = new ArrayList<ByteBufBsonDocument>();
        int curDocumentStartPosition = startPosition;
        while (outputByteBuf.hasRemaining()) {
            final int documentSizeInBytes = outputByteBuf.getInt();
            final ByteBuf slice = outputByteBuf.duplicate();
            slice.position(curDocumentStartPosition);
            slice.limit(curDocumentStartPosition + documentSizeInBytes);
            documents.add(new ByteBufBsonDocument(slice));
            curDocumentStartPosition += documentSizeInBytes;
            outputByteBuf.position(outputByteBuf.position() + documentSizeInBytes - 4);
        }
        return documents;
    }
    
    @Override
    public void clear() {
        throw new UnsupportedOperationException("RawBsonDocument instances are immutable");
    }
    
    @Override
    public BsonValue put(final String key, final BsonValue value) {
        throw new UnsupportedOperationException("RawBsonDocument instances are immutable");
    }
    
    @Override
    public BsonDocument append(final String key, final BsonValue value) {
        throw new UnsupportedOperationException("RawBsonDocument instances are immutable");
    }
    
    @Override
    public void putAll(final Map<? extends String, ? extends BsonValue> m) {
        throw new UnsupportedOperationException("RawBsonDocument instances are immutable");
    }
    
    @Override
    public BsonValue remove(final Object key) {
        throw new UnsupportedOperationException("RawBsonDocument instances are immutable");
    }
    
    @Override
    public boolean isEmpty() {
        return this.findInDocument((Finder<Boolean>)new Finder<Boolean>() {
            @Override
            public Boolean find(final BsonReader bsonReader) {
                return false;
            }
            
            @Override
            public Boolean notFound() {
                return true;
            }
        });
    }
    
    @Override
    public int size() {
        return this.findInDocument((Finder<Integer>)new Finder<Integer>() {
            private int size;
            
            @Override
            public Integer find(final BsonReader bsonReader) {
                ++this.size;
                bsonReader.readName();
                bsonReader.skipValue();
                return null;
            }
            
            @Override
            public Integer notFound() {
                return this.size;
            }
        });
    }
    
    @Override
    public Set<Map.Entry<String, BsonValue>> entrySet() {
        return this.toBsonDocument().entrySet();
    }
    
    @Override
    public Collection<BsonValue> values() {
        return this.toBsonDocument().values();
    }
    
    @Override
    public Set<String> keySet() {
        return this.toBsonDocument().keySet();
    }
    
    @Override
    public boolean containsKey(final Object key) {
        if (key == null) {
            throw new IllegalArgumentException("key can not be null");
        }
        return this.findInDocument((Finder<Boolean>)new Finder<Boolean>() {
            @Override
            public Boolean find(final BsonReader bsonReader) {
                if (bsonReader.readName().equals(key)) {
                    return true;
                }
                bsonReader.skipValue();
                return null;
            }
            
            @Override
            public Boolean notFound() {
                return false;
            }
        });
    }
    
    @Override
    public boolean containsValue(final Object value) {
        return this.findInDocument((Finder<Boolean>)new Finder<Boolean>() {
            @Override
            public Boolean find(final BsonReader bsonReader) {
                bsonReader.skipName();
                if (ByteBufBsonDocument.this.deserializeBsonValue(bsonReader).equals(value)) {
                    return true;
                }
                return null;
            }
            
            @Override
            public Boolean notFound() {
                return false;
            }
        });
    }
    
    @Override
    public BsonValue get(final Object key) {
        if (key == null) {
            throw new IllegalArgumentException("key can not be null");
        }
        return this.findInDocument((Finder<BsonValue>)new Finder<BsonValue>() {
            @Override
            public BsonValue find(final BsonReader bsonReader) {
                if (bsonReader.readName().equals(key)) {
                    return ByteBufBsonDocument.this.deserializeBsonValue(bsonReader);
                }
                bsonReader.skipValue();
                return null;
            }
            
            @Override
            public BsonValue notFound() {
                return null;
            }
        });
    }
    
    @Override
    public String toJson() {
        return this.toJson(new JsonWriterSettings());
    }
    
    @Override
    public String toJson(final JsonWriterSettings settings) {
        final StringWriter stringWriter = new StringWriter();
        final JsonWriter jsonWriter = new JsonWriter(stringWriter, settings);
        final ByteBuf duplicate = this.byteBuf.duplicate();
        final BsonBinaryReader reader = new BsonBinaryReader(new ByteBufferBsonInput(duplicate));
        try {
            jsonWriter.pipe(reader);
            return stringWriter.toString();
        }
        finally {
            duplicate.release();
            reader.close();
        }
    }
    
    public String getFirstKey() {
        return this.findInDocument((Finder<String>)new Finder<String>() {
            @Override
            public String find(final BsonReader bsonReader) {
                return bsonReader.readName();
            }
            
            @Override
            public String notFound() {
                return null;
            }
        });
    }
    
    private <T> T findInDocument(final Finder<T> finder) {
        final ByteBuf duplicateByteBuf = this.byteBuf.duplicate();
        final BsonBinaryReader bsonReader = new BsonBinaryReader(new ByteBufferBsonInput(duplicateByteBuf));
        try {
            bsonReader.readStartDocument();
            while (bsonReader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                final T found = finder.find(bsonReader);
                if (found != null) {
                    return found;
                }
            }
            bsonReader.readEndDocument();
        }
        finally {
            duplicateByteBuf.release();
            bsonReader.close();
        }
        return finder.notFound();
    }
    
    @Override
    public BsonDocument clone() {
        final byte[] clonedBytes = new byte[this.byteBuf.remaining()];
        this.byteBuf.get(this.byteBuf.position(), clonedBytes);
        return new RawBsonDocument(clonedBytes);
    }
    
    @Override
    public boolean equals(final Object o) {
        return this == o || this.toBsonDocument().equals(o);
    }
    
    @Override
    public int hashCode() {
        return this.toBsonDocument().hashCode();
    }
    
    private BsonDocument toBsonDocument() {
        final ByteBuf duplicateByteBuf = this.byteBuf.duplicate();
        final BsonBinaryReader bsonReader = new BsonBinaryReader(new ByteBufferBsonInput(duplicateByteBuf));
        try {
            return new BsonDocumentCodec().decode((BsonReader)bsonReader, DecoderContext.builder().build());
        }
        finally {
            duplicateByteBuf.release();
            bsonReader.close();
        }
    }
    
    private BsonValue deserializeBsonValue(final BsonReader bsonReader) {
        return ByteBufBsonDocument.REGISTRY.get(BsonValueCodecProvider.getClassForBsonType(bsonReader.getCurrentBsonType())).decode(bsonReader, DecoderContext.builder().build());
    }
    
    ByteBufBsonDocument(final ByteBuf byteBuf) {
        this.byteBuf = byteBuf;
    }
    
    private Object writeReplace() {
        return this.toBsonDocument();
    }
    
    private void readObject(final ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }
    
    static {
        REGISTRY = CodecRegistries.fromProviders(new BsonValueCodecProvider());
    }
    
    private interface Finder<T>
    {
        T find(final BsonReader p0);
        
        T notFound();
    }
}
