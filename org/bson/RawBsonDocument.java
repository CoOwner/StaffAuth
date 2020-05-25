package org.bson;

import org.bson.assertions.*;
import java.nio.*;
import java.util.*;
import org.bson.json.*;
import org.bson.io.*;
import org.bson.codecs.*;
import org.bson.codecs.configuration.*;
import java.io.*;

public final class RawBsonDocument extends BsonDocument
{
    private static final long serialVersionUID = 1L;
    private static final int MIN_BSON_DOCUMENT_SIZE = 5;
    private static final CodecRegistry REGISTRY;
    private final byte[] bytes;
    private final int offset;
    private final int length;
    
    public static RawBsonDocument parse(final String json) {
        Assertions.notNull("json", json);
        return new RawBsonDocumentCodec().decode((BsonReader)new JsonReader(json), DecoderContext.builder().build());
    }
    
    public RawBsonDocument(final byte[] bytes) {
        this(Assertions.notNull("bytes", bytes), 0, bytes.length);
    }
    
    public RawBsonDocument(final byte[] bytes, final int offset, final int length) {
        Assertions.notNull("bytes", bytes);
        Assertions.isTrueArgument("offset >= 0", offset >= 0);
        Assertions.isTrueArgument("offset < bytes.length", offset < bytes.length);
        Assertions.isTrueArgument("length <= bytes.length - offset", length <= bytes.length - offset);
        Assertions.isTrueArgument("length >= 5", length >= 5);
        this.bytes = bytes;
        this.offset = offset;
        this.length = length;
    }
    
    public <T> RawBsonDocument(final T document, final Codec<T> codec) {
        Assertions.notNull("document", document);
        Assertions.notNull("codec", codec);
        final BasicOutputBuffer buffer = new BasicOutputBuffer();
        final BsonBinaryWriter writer = new BsonBinaryWriter(buffer);
        try {
            codec.encode(writer, document, EncoderContext.builder().build());
            this.bytes = buffer.getInternalBuffer();
            this.offset = 0;
            this.length = buffer.getPosition();
        }
        finally {
            writer.close();
        }
    }
    
    public ByteBuf getByteBuffer() {
        final ByteBuffer buffer = ByteBuffer.wrap(this.bytes, this.offset, this.length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return new ByteBufNIO(buffer);
    }
    
    public <T> T decode(final Codec<T> codec) {
        final BsonBinaryReader reader = this.createReader();
        try {
            return codec.decode(reader, DecoderContext.builder().build());
        }
        finally {
            reader.close();
        }
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
        final BsonBinaryReader bsonReader = this.createReader();
        try {
            bsonReader.readStartDocument();
            if (bsonReader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                return false;
            }
            bsonReader.readEndDocument();
        }
        finally {
            bsonReader.close();
        }
        return true;
    }
    
    @Override
    public int size() {
        int size = 0;
        final BsonBinaryReader bsonReader = this.createReader();
        try {
            bsonReader.readStartDocument();
            while (bsonReader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                ++size;
                bsonReader.readName();
                bsonReader.skipValue();
            }
            bsonReader.readEndDocument();
        }
        finally {
            bsonReader.close();
        }
        return size;
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
        final BsonBinaryReader bsonReader = this.createReader();
        try {
            bsonReader.readStartDocument();
            while (bsonReader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                if (bsonReader.readName().equals(key)) {
                    return true;
                }
                bsonReader.skipValue();
            }
            bsonReader.readEndDocument();
        }
        finally {
            bsonReader.close();
        }
        return false;
    }
    
    @Override
    public boolean containsValue(final Object value) {
        final BsonBinaryReader bsonReader = this.createReader();
        try {
            bsonReader.readStartDocument();
            while (bsonReader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                bsonReader.skipName();
                if (this.deserializeBsonValue(bsonReader).equals(value)) {
                    return true;
                }
            }
            bsonReader.readEndDocument();
        }
        finally {
            bsonReader.close();
        }
        return false;
    }
    
    @Override
    public BsonValue get(final Object key) {
        Assertions.notNull("key", key);
        final BsonBinaryReader bsonReader = this.createReader();
        try {
            bsonReader.readStartDocument();
            while (bsonReader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                if (bsonReader.readName().equals(key)) {
                    return this.deserializeBsonValue(bsonReader);
                }
                bsonReader.skipValue();
            }
            bsonReader.readEndDocument();
        }
        finally {
            bsonReader.close();
        }
        return null;
    }
    
    @Override
    public String toJson() {
        return this.toJson(new JsonWriterSettings());
    }
    
    @Override
    public String toJson(final JsonWriterSettings settings) {
        final StringWriter writer = new StringWriter();
        new RawBsonDocumentCodec().encode((BsonWriter)new JsonWriter(writer, settings), this, EncoderContext.builder().build());
        return writer.toString();
    }
    
    @Override
    public boolean equals(final Object o) {
        return this.toBsonDocument().equals(o);
    }
    
    @Override
    public int hashCode() {
        return this.toBsonDocument().hashCode();
    }
    
    @Override
    public BsonDocument clone() {
        return new RawBsonDocument(this.bytes.clone(), this.offset, this.length);
    }
    
    private BsonValue deserializeBsonValue(final BsonBinaryReader bsonReader) {
        return RawBsonDocument.REGISTRY.get(BsonValueCodecProvider.getClassForBsonType(bsonReader.getCurrentBsonType())).decode(bsonReader, DecoderContext.builder().build());
    }
    
    private BsonBinaryReader createReader() {
        return new BsonBinaryReader(new ByteBufferBsonInput(this.getByteBuffer()));
    }
    
    private BsonDocument toBsonDocument() {
        final BsonBinaryReader bsonReader = this.createReader();
        try {
            return new BsonDocumentCodec().decode((BsonReader)bsonReader, DecoderContext.builder().build());
        }
        finally {
            bsonReader.close();
        }
    }
    
    private Object writeReplace() {
        return new SerializationProxy(this.bytes, this.offset, this.length);
    }
    
    private void readObject(final ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }
    
    static {
        REGISTRY = CodecRegistries.fromProviders(new BsonValueCodecProvider());
    }
    
    private static class SerializationProxy implements Serializable
    {
        private static final long serialVersionUID = 1L;
        private final byte[] bytes;
        
        public SerializationProxy(final byte[] bytes, final int offset, final int length) {
            if (bytes.length == length) {
                this.bytes = bytes;
            }
            else {
                System.arraycopy(bytes, offset, this.bytes = new byte[length], 0, length);
            }
        }
        
        private Object readResolve() {
            return new RawBsonDocument(this.bytes);
        }
    }
}
