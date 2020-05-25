package org.bson;

import org.bson.conversions.*;
import org.bson.codecs.configuration.*;
import java.util.*;
import org.bson.json.*;
import org.bson.codecs.*;
import java.io.*;
import org.bson.io.*;
import java.nio.*;

public class BsonDocument extends BsonValue implements Map<String, BsonValue>, Cloneable, Bson, Serializable
{
    private static final long serialVersionUID = 1L;
    private final Map<String, BsonValue> map;
    
    public static BsonDocument parse(final String json) {
        return new BsonDocumentCodec().decode((BsonReader)new JsonReader(json), DecoderContext.builder().build());
    }
    
    public BsonDocument(final List<BsonElement> bsonElements) {
        this.map = new LinkedHashMap<String, BsonValue>();
        for (final BsonElement cur : bsonElements) {
            this.put(cur.getName(), cur.getValue());
        }
    }
    
    public BsonDocument(final String key, final BsonValue value) {
        this.map = new LinkedHashMap<String, BsonValue>();
        this.put(key, value);
    }
    
    public BsonDocument() {
        this.map = new LinkedHashMap<String, BsonValue>();
    }
    
    @Override
    public <C> BsonDocument toBsonDocument(final Class<C> documentClass, final CodecRegistry codecRegistry) {
        return this;
    }
    
    @Override
    public BsonType getBsonType() {
        return BsonType.DOCUMENT;
    }
    
    @Override
    public int size() {
        return this.map.size();
    }
    
    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }
    
    @Override
    public boolean containsKey(final Object key) {
        return this.map.containsKey(key);
    }
    
    @Override
    public boolean containsValue(final Object value) {
        return this.map.containsValue(value);
    }
    
    @Override
    public BsonValue get(final Object key) {
        return this.map.get(key);
    }
    
    public BsonDocument getDocument(final Object key) {
        this.throwIfKeyAbsent(key);
        return this.get(key).asDocument();
    }
    
    public BsonArray getArray(final Object key) {
        this.throwIfKeyAbsent(key);
        return this.get(key).asArray();
    }
    
    public BsonNumber getNumber(final Object key) {
        this.throwIfKeyAbsent(key);
        return this.get(key).asNumber();
    }
    
    public BsonInt32 getInt32(final Object key) {
        this.throwIfKeyAbsent(key);
        return this.get(key).asInt32();
    }
    
    public BsonInt64 getInt64(final Object key) {
        this.throwIfKeyAbsent(key);
        return this.get(key).asInt64();
    }
    
    public BsonDecimal128 getDecimal128(final Object key) {
        this.throwIfKeyAbsent(key);
        return this.get(key).asDecimal128();
    }
    
    public BsonDouble getDouble(final Object key) {
        this.throwIfKeyAbsent(key);
        return this.get(key).asDouble();
    }
    
    public BsonBoolean getBoolean(final Object key) {
        this.throwIfKeyAbsent(key);
        return this.get(key).asBoolean();
    }
    
    public BsonString getString(final Object key) {
        this.throwIfKeyAbsent(key);
        return this.get(key).asString();
    }
    
    public BsonDateTime getDateTime(final Object key) {
        this.throwIfKeyAbsent(key);
        return this.get(key).asDateTime();
    }
    
    public BsonTimestamp getTimestamp(final Object key) {
        this.throwIfKeyAbsent(key);
        return this.get(key).asTimestamp();
    }
    
    public BsonObjectId getObjectId(final Object key) {
        this.throwIfKeyAbsent(key);
        return this.get(key).asObjectId();
    }
    
    public BsonRegularExpression getRegularExpression(final Object key) {
        this.throwIfKeyAbsent(key);
        return this.get(key).asRegularExpression();
    }
    
    public BsonBinary getBinary(final Object key) {
        this.throwIfKeyAbsent(key);
        return this.get(key).asBinary();
    }
    
    public boolean isNull(final Object key) {
        return this.containsKey(key) && this.get(key).isNull();
    }
    
    public boolean isDocument(final Object key) {
        return this.containsKey(key) && this.get(key).isDocument();
    }
    
    public boolean isArray(final Object key) {
        return this.containsKey(key) && this.get(key).isArray();
    }
    
    public boolean isNumber(final Object key) {
        return this.containsKey(key) && this.get(key).isNumber();
    }
    
    public boolean isInt32(final Object key) {
        return this.containsKey(key) && this.get(key).isInt32();
    }
    
    public boolean isInt64(final Object key) {
        return this.containsKey(key) && this.get(key).isInt64();
    }
    
    public boolean isDecimal128(final Object key) {
        return this.containsKey(key) && this.get(key).isDecimal128();
    }
    
    public boolean isDouble(final Object key) {
        return this.containsKey(key) && this.get(key).isDouble();
    }
    
    public boolean isBoolean(final Object key) {
        return this.containsKey(key) && this.get(key).isBoolean();
    }
    
    public boolean isString(final Object key) {
        return this.containsKey(key) && this.get(key).isString();
    }
    
    public boolean isDateTime(final Object key) {
        return this.containsKey(key) && this.get(key).isDateTime();
    }
    
    public boolean isTimestamp(final Object key) {
        return this.containsKey(key) && this.get(key).isTimestamp();
    }
    
    public boolean isObjectId(final Object key) {
        return this.containsKey(key) && this.get(key).isObjectId();
    }
    
    public boolean isBinary(final Object key) {
        return this.containsKey(key) && this.get(key).isBinary();
    }
    
    public BsonValue get(final Object key, final BsonValue defaultValue) {
        final BsonValue value = this.get(key);
        return (value != null) ? value : defaultValue;
    }
    
    public BsonDocument getDocument(final Object key, final BsonDocument defaultValue) {
        if (!this.containsKey(key)) {
            return defaultValue;
        }
        return this.get(key).asDocument();
    }
    
    public BsonArray getArray(final Object key, final BsonArray defaultValue) {
        if (!this.containsKey(key)) {
            return defaultValue;
        }
        return this.get(key).asArray();
    }
    
    public BsonNumber getNumber(final Object key, final BsonNumber defaultValue) {
        if (!this.containsKey(key)) {
            return defaultValue;
        }
        return this.get(key).asNumber();
    }
    
    public BsonInt32 getInt32(final Object key, final BsonInt32 defaultValue) {
        if (!this.containsKey(key)) {
            return defaultValue;
        }
        return this.get(key).asInt32();
    }
    
    public BsonInt64 getInt64(final Object key, final BsonInt64 defaultValue) {
        if (!this.containsKey(key)) {
            return defaultValue;
        }
        return this.get(key).asInt64();
    }
    
    public BsonDecimal128 getDecimal128(final Object key, final BsonDecimal128 defaultValue) {
        if (!this.containsKey(key)) {
            return defaultValue;
        }
        return this.get(key).asDecimal128();
    }
    
    public BsonDouble getDouble(final Object key, final BsonDouble defaultValue) {
        if (!this.containsKey(key)) {
            return defaultValue;
        }
        return this.get(key).asDouble();
    }
    
    public BsonBoolean getBoolean(final Object key, final BsonBoolean defaultValue) {
        if (!this.containsKey(key)) {
            return defaultValue;
        }
        return this.get(key).asBoolean();
    }
    
    public BsonString getString(final Object key, final BsonString defaultValue) {
        if (!this.containsKey(key)) {
            return defaultValue;
        }
        return this.get(key).asString();
    }
    
    public BsonDateTime getDateTime(final Object key, final BsonDateTime defaultValue) {
        if (!this.containsKey(key)) {
            return defaultValue;
        }
        return this.get(key).asDateTime();
    }
    
    public BsonTimestamp getTimestamp(final Object key, final BsonTimestamp defaultValue) {
        if (!this.containsKey(key)) {
            return defaultValue;
        }
        return this.get(key).asTimestamp();
    }
    
    public BsonObjectId getObjectId(final Object key, final BsonObjectId defaultValue) {
        if (!this.containsKey(key)) {
            return defaultValue;
        }
        return this.get(key).asObjectId();
    }
    
    public BsonBinary getBinary(final Object key, final BsonBinary defaultValue) {
        if (!this.containsKey(key)) {
            return defaultValue;
        }
        return this.get(key).asBinary();
    }
    
    public BsonRegularExpression getRegularExpression(final Object key, final BsonRegularExpression defaultValue) {
        if (!this.containsKey(key)) {
            return defaultValue;
        }
        return this.get(key).asRegularExpression();
    }
    
    @Override
    public BsonValue put(final String key, final BsonValue value) {
        if (value == null) {
            throw new IllegalArgumentException(String.format("The value for key %s can not be null", key));
        }
        if (key.contains("\u0000")) {
            throw new BSONException(String.format("BSON cstring '%s' is not valid because it contains a null character at index %d", key, key.indexOf(0)));
        }
        return this.map.put(key, value);
    }
    
    @Override
    public BsonValue remove(final Object key) {
        return this.map.remove(key);
    }
    
    @Override
    public void putAll(final Map<? extends String, ? extends BsonValue> m) {
        for (final Entry<? extends String, ? extends BsonValue> cur : m.entrySet()) {
            this.put((String)cur.getKey(), (BsonValue)cur.getValue());
        }
    }
    
    @Override
    public void clear() {
        this.map.clear();
    }
    
    @Override
    public Set<String> keySet() {
        return this.map.keySet();
    }
    
    @Override
    public Collection<BsonValue> values() {
        return this.map.values();
    }
    
    @Override
    public Set<Entry<String, BsonValue>> entrySet() {
        return this.map.entrySet();
    }
    
    public BsonDocument append(final String key, final BsonValue value) {
        this.put(key, value);
        return this;
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BsonDocument)) {
            return false;
        }
        final BsonDocument that = (BsonDocument)o;
        return this.entrySet().equals(that.entrySet());
    }
    
    @Override
    public int hashCode() {
        return this.entrySet().hashCode();
    }
    
    public String toJson() {
        return this.toJson(new JsonWriterSettings());
    }
    
    public String toJson(final JsonWriterSettings settings) {
        final StringWriter writer = new StringWriter();
        new BsonDocumentCodec().encode((BsonWriter)new JsonWriter(writer, settings), this, EncoderContext.builder().build());
        return writer.toString();
    }
    
    @Override
    public String toString() {
        return this.toJson();
    }
    
    public BsonDocument clone() {
        final BsonDocument to = new BsonDocument();
        for (final Entry<String, BsonValue> cur : this.entrySet()) {
            switch (cur.getValue().getBsonType()) {
                case DOCUMENT: {
                    to.put(cur.getKey(), cur.getValue().asDocument().clone());
                    continue;
                }
                case ARRAY: {
                    to.put(cur.getKey(), cur.getValue().asArray().clone());
                    continue;
                }
                case BINARY: {
                    to.put(cur.getKey(), BsonBinary.clone(cur.getValue().asBinary()));
                    continue;
                }
                case JAVASCRIPT_WITH_SCOPE: {
                    to.put(cur.getKey(), BsonJavaScriptWithScope.clone(cur.getValue().asJavaScriptWithScope()));
                    continue;
                }
                default: {
                    to.put(cur.getKey(), cur.getValue());
                    continue;
                }
            }
        }
        return to;
    }
    
    private void throwIfKeyAbsent(final Object key) {
        if (!this.containsKey(key)) {
            throw new BsonInvalidOperationException("Document does not contain key " + key);
        }
    }
    
    private Object writeReplace() {
        return new SerializationProxy(this);
    }
    
    private void readObject(final ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }
    
    private static class SerializationProxy implements Serializable
    {
        private static final long serialVersionUID = 1L;
        private final byte[] bytes;
        
        public SerializationProxy(final BsonDocument document) {
            final BasicOutputBuffer buffer = new BasicOutputBuffer();
            new BsonDocumentCodec().encode((BsonWriter)new BsonBinaryWriter(buffer), document, EncoderContext.builder().build());
            this.bytes = new byte[buffer.size()];
            int curPos = 0;
            for (final ByteBuf cur : buffer.getByteBuffers()) {
                System.arraycopy(cur.array(), cur.position(), this.bytes, curPos, cur.limit());
                curPos += cur.position();
            }
        }
        
        private Object readResolve() {
            return new BsonDocumentCodec().decode((BsonReader)new BsonBinaryReader(ByteBuffer.wrap(this.bytes).order(ByteOrder.LITTLE_ENDIAN)), DecoderContext.builder().build());
        }
    }
}
