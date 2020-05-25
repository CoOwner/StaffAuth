package org.bson;

import org.bson.codecs.*;
import java.util.regex.*;
import org.bson.types.*;
import org.bson.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.io.*;
import com.mongodb.util.*;
import java.util.*;

public class LazyBSONObject implements BSONObject
{
    private final byte[] bytes;
    private final int offset;
    private final LazyBSONCallback callback;
    
    public LazyBSONObject(final byte[] bytes, final LazyBSONCallback callback) {
        this(bytes, 0, callback);
    }
    
    public LazyBSONObject(final byte[] bytes, final int offset, final LazyBSONCallback callback) {
        this.bytes = bytes;
        this.callback = callback;
        this.offset = offset;
    }
    
    protected int getOffset() {
        return this.offset;
    }
    
    protected byte[] getBytes() {
        return this.bytes;
    }
    
    @Override
    public Object get(final String key) {
        final BsonBinaryReader reader = this.getBsonReader();
        Object value;
        try {
            reader.readStartDocument();
            value = null;
            while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                if (key.equals(reader.readName())) {
                    value = this.readValue(reader);
                    break;
                }
                reader.skipValue();
            }
        }
        finally {
            reader.close();
        }
        return value;
    }
    
    @Deprecated
    @Override
    public boolean containsKey(final String key) {
        return this.containsField(key);
    }
    
    @Override
    public boolean containsField(final String s) {
        final BsonBinaryReader reader = this.getBsonReader();
        try {
            reader.readStartDocument();
            while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                if (reader.readName().equals(s)) {
                    return true;
                }
                reader.skipValue();
            }
        }
        finally {
            reader.close();
        }
        return false;
    }
    
    @Override
    public Set<String> keySet() {
        final Set<String> keys = new LinkedHashSet<String>();
        final BsonBinaryReader reader = this.getBsonReader();
        try {
            reader.readStartDocument();
            while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                keys.add(reader.readName());
                reader.skipValue();
            }
            reader.readEndDocument();
        }
        finally {
            reader.close();
        }
        return Collections.unmodifiableSet((Set<? extends String>)keys);
    }
    
    Object readValue(final BsonBinaryReader reader) {
        switch (reader.getCurrentBsonType()) {
            case DOCUMENT: {
                return this.readDocument(reader);
            }
            case ARRAY: {
                return this.readArray(reader);
            }
            case DOUBLE: {
                return reader.readDouble();
            }
            case STRING: {
                return reader.readString();
            }
            case BINARY: {
                final byte binarySubType = reader.peekBinarySubType();
                if (BsonBinarySubType.isUuid(binarySubType) && reader.peekBinarySize() == 16) {
                    return new UuidCodec().decode((BsonReader)reader, DecoderContext.builder().build());
                }
                final BsonBinary binary = reader.readBinaryData();
                if (binarySubType == BsonBinarySubType.BINARY.getValue() || binarySubType == BsonBinarySubType.OLD_BINARY.getValue()) {
                    return binary.getData();
                }
                return new Binary(binary.getType(), binary.getData());
            }
            case NULL: {
                reader.readNull();
                return null;
            }
            case UNDEFINED: {
                reader.readUndefined();
                return null;
            }
            case OBJECT_ID: {
                return reader.readObjectId();
            }
            case BOOLEAN: {
                return reader.readBoolean();
            }
            case DATE_TIME: {
                return new Date(reader.readDateTime());
            }
            case REGULAR_EXPRESSION: {
                final BsonRegularExpression regularExpression = reader.readRegularExpression();
                return Pattern.compile(regularExpression.getPattern(), BSON.regexFlags(regularExpression.getOptions()));
            }
            case DB_POINTER: {
                final BsonDbPointer dbPointer = reader.readDBPointer();
                return this.callback.createDBRef(dbPointer.getNamespace(), dbPointer.getId());
            }
            case JAVASCRIPT: {
                return new Code(reader.readJavaScript());
            }
            case SYMBOL: {
                return new Symbol(reader.readSymbol());
            }
            case JAVASCRIPT_WITH_SCOPE: {
                return new CodeWScope(reader.readJavaScriptWithScope(), (BSONObject)this.readJavaScriptWithScopeDocument(reader));
            }
            case INT32: {
                return reader.readInt32();
            }
            case TIMESTAMP: {
                final BsonTimestamp timestamp = reader.readTimestamp();
                return new BSONTimestamp(timestamp.getTime(), timestamp.getInc());
            }
            case INT64: {
                return reader.readInt64();
            }
            case DECIMAL128: {
                return reader.readDecimal128();
            }
            case MIN_KEY: {
                reader.readMinKey();
                return new MinKey();
            }
            case MAX_KEY: {
                reader.readMaxKey();
                return new MaxKey();
            }
            default: {
                throw new IllegalArgumentException("unhandled BSON type: " + reader.getCurrentBsonType());
            }
        }
    }
    
    private Object readArray(final BsonBinaryReader reader) {
        final int position = reader.getBsonInput().getPosition();
        reader.skipValue();
        return this.callback.createArray(this.bytes, this.offset + position);
    }
    
    private Object readDocument(final BsonBinaryReader reader) {
        final int position = reader.getBsonInput().getPosition();
        reader.skipValue();
        return this.callback.createObject(this.bytes, this.offset + position);
    }
    
    private Object readJavaScriptWithScopeDocument(final BsonBinaryReader reader) {
        final int position = reader.getBsonInput().getPosition();
        reader.readStartDocument();
        while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
            reader.skipName();
            reader.skipValue();
        }
        reader.readEndDocument();
        return this.callback.createObject(this.bytes, this.offset + position);
    }
    
    BsonBinaryReader getBsonReader() {
        final ByteBuffer buffer = this.getBufferForInternalBytes();
        return new BsonBinaryReader(new ByteBufferBsonInput(new ByteBufNIO(buffer)));
    }
    
    private ByteBuffer getBufferForInternalBytes() {
        final ByteBuffer buffer = ByteBuffer.wrap(this.bytes, this.offset, this.bytes.length - this.offset).slice();
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.limit(buffer.getInt());
        buffer.rewind();
        return buffer;
    }
    
    public boolean isEmpty() {
        return this.keySet().size() == 0;
    }
    
    public int getBSONSize() {
        return this.getBufferForInternalBytes().getInt();
    }
    
    public int pipe(final OutputStream os) throws IOException {
        final WritableByteChannel channel = Channels.newChannel(os);
        return channel.write(this.getBufferForInternalBytes());
    }
    
    public Set<Map.Entry<String, Object>> entrySet() {
        final List<Map.Entry<String, Object>> entries = new ArrayList<Map.Entry<String, Object>>();
        final BsonBinaryReader reader = this.getBsonReader();
        try {
            reader.readStartDocument();
            while (reader.readBsonType() != BsonType.END_OF_DOCUMENT) {
                entries.add(new AbstractMap.SimpleImmutableEntry<String, Object>(reader.readName(), this.readValue(reader)));
            }
            reader.readEndDocument();
        }
        finally {
            reader.close();
        }
        return new Set<Map.Entry<String, Object>>() {
            @Override
            public int size() {
                return entries.size();
            }
            
            @Override
            public boolean isEmpty() {
                return entries.isEmpty();
            }
            
            @Override
            public Iterator<Map.Entry<String, Object>> iterator() {
                return entries.iterator();
            }
            
            @Override
            public Object[] toArray() {
                return entries.toArray();
            }
            
            @Override
            public <T> T[] toArray(final T[] a) {
                return entries.toArray(a);
            }
            
            @Override
            public boolean contains(final Object o) {
                return entries.contains(o);
            }
            
            @Override
            public boolean containsAll(final Collection<?> c) {
                return entries.containsAll(c);
            }
            
            @Override
            public boolean add(final Map.Entry<String, Object> stringObjectEntry) {
                throw new UnsupportedOperationException();
            }
            
            @Override
            public boolean remove(final Object o) {
                throw new UnsupportedOperationException();
            }
            
            @Override
            public boolean addAll(final Collection<? extends Map.Entry<String, Object>> c) {
                throw new UnsupportedOperationException();
            }
            
            @Override
            public boolean retainAll(final Collection<?> c) {
                throw new UnsupportedOperationException();
            }
            
            @Override
            public boolean removeAll(final Collection<?> c) {
                throw new UnsupportedOperationException();
            }
            
            @Override
            public void clear() {
                throw new UnsupportedOperationException();
            }
        };
    }
    
    @Override
    public int hashCode() {
        int result = 1;
        for (int size = this.getBSONSize(), i = this.offset; i < this.offset + size; ++i) {
            result = 31 * result + this.bytes[i];
        }
        return result;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final LazyBSONObject other = (LazyBSONObject)o;
        if (this.bytes == other.bytes && this.offset == other.offset) {
            return true;
        }
        if (this.bytes == null || other.bytes == null) {
            return false;
        }
        if (this.bytes.length == 0 || other.bytes.length == 0) {
            return false;
        }
        final int length = this.bytes[this.offset];
        if (other.bytes[other.offset] != length) {
            return false;
        }
        for (int i = 0; i < length; ++i) {
            if (this.bytes[this.offset + i] != other.bytes[other.offset + i]) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public String toString() {
        return JSON.serialize(this);
    }
    
    @Override
    public Object put(final String key, final Object v) {
        throw new UnsupportedOperationException("Object is read only");
    }
    
    @Override
    public void putAll(final BSONObject o) {
        throw new UnsupportedOperationException("Object is read only");
    }
    
    @Override
    public void putAll(final Map m) {
        throw new UnsupportedOperationException("Object is read only");
    }
    
    @Override
    public Object removeField(final String key) {
        throw new UnsupportedOperationException("Object is read only");
    }
    
    @Override
    public Map toMap() {
        final Map<String, Object> map = new LinkedHashMap<String, Object>();
        for (final Map.Entry<String, Object> entry : this.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        return Collections.unmodifiableMap((Map<?, ?>)map);
    }
}
