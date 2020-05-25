package org.bson;

import org.bson.conversions.*;
import org.bson.assertions.*;
import org.bson.codecs.configuration.*;
import org.bson.types.*;
import org.bson.json.*;
import java.io.*;
import org.bson.codecs.*;
import java.util.*;

public class Document implements Map<String, Object>, Serializable, Bson
{
    private static final long serialVersionUID = 6297731997167536582L;
    private final LinkedHashMap<String, Object> documentAsMap;
    
    public Document() {
        this.documentAsMap = new LinkedHashMap<String, Object>();
    }
    
    public Document(final String key, final Object value) {
        (this.documentAsMap = new LinkedHashMap<String, Object>()).put(key, value);
    }
    
    public Document(final Map<String, Object> map) {
        this.documentAsMap = new LinkedHashMap<String, Object>(map);
    }
    
    public static Document parse(final String json) {
        return parse(json, new DocumentCodec());
    }
    
    public static Document parse(final String json, final Decoder<Document> decoder) {
        Assertions.notNull("codec", decoder);
        final JsonReader bsonReader = new JsonReader(json);
        return decoder.decode(bsonReader, DecoderContext.builder().build());
    }
    
    @Override
    public <C> BsonDocument toBsonDocument(final Class<C> documentClass, final CodecRegistry codecRegistry) {
        return new BsonDocumentWrapper<Object>(this, codecRegistry.get(Document.class));
    }
    
    public Document append(final String key, final Object value) {
        this.documentAsMap.put(key, value);
        return this;
    }
    
    public <T> T get(final Object key, final Class<T> clazz) {
        Assertions.notNull("clazz", clazz);
        return clazz.cast(this.documentAsMap.get(key));
    }
    
    public Integer getInteger(final Object key) {
        return (Integer)this.get(key);
    }
    
    public int getInteger(final Object key, final int defaultValue) {
        final Object value = this.get(key);
        return (int)((value == null) ? defaultValue : value);
    }
    
    public Long getLong(final Object key) {
        return (Long)this.get(key);
    }
    
    public Double getDouble(final Object key) {
        return (Double)this.get(key);
    }
    
    public String getString(final Object key) {
        return (String)this.get(key);
    }
    
    public Boolean getBoolean(final Object key) {
        return (Boolean)this.get(key);
    }
    
    public boolean getBoolean(final Object key, final boolean defaultValue) {
        final Object value = this.get(key);
        return (boolean)((value == null) ? defaultValue : value);
    }
    
    public ObjectId getObjectId(final Object key) {
        return (ObjectId)this.get(key);
    }
    
    public Date getDate(final Object key) {
        return (Date)this.get(key);
    }
    
    public String toJson() {
        return this.toJson(new JsonWriterSettings());
    }
    
    public String toJson(final JsonWriterSettings writerSettings) {
        return this.toJson(writerSettings, new DocumentCodec());
    }
    
    public String toJson(final Encoder<Document> encoder) {
        return this.toJson(new JsonWriterSettings(), encoder);
    }
    
    public String toJson(final JsonWriterSettings writerSettings, final Encoder<Document> encoder) {
        final JsonWriter writer = new JsonWriter(new StringWriter(), writerSettings);
        encoder.encode(writer, this, EncoderContext.builder().isEncodingCollectibleDocument(true).build());
        return writer.getWriter().toString();
    }
    
    @Override
    public int size() {
        return this.documentAsMap.size();
    }
    
    @Override
    public boolean isEmpty() {
        return this.documentAsMap.isEmpty();
    }
    
    @Override
    public boolean containsValue(final Object value) {
        return this.documentAsMap.containsValue(value);
    }
    
    @Override
    public boolean containsKey(final Object key) {
        return this.documentAsMap.containsKey(key);
    }
    
    @Override
    public Object get(final Object key) {
        return this.documentAsMap.get(key);
    }
    
    @Override
    public Object put(final String key, final Object value) {
        return this.documentAsMap.put(key, value);
    }
    
    @Override
    public Object remove(final Object key) {
        return this.documentAsMap.remove(key);
    }
    
    @Override
    public void putAll(final Map<? extends String, ?> map) {
        this.documentAsMap.putAll((Map<?, ?>)map);
    }
    
    @Override
    public void clear() {
        this.documentAsMap.clear();
    }
    
    @Override
    public Set<String> keySet() {
        return this.documentAsMap.keySet();
    }
    
    @Override
    public Collection<Object> values() {
        return this.documentAsMap.values();
    }
    
    @Override
    public Set<Entry<String, Object>> entrySet() {
        return this.documentAsMap.entrySet();
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final Document document = (Document)o;
        return this.documentAsMap.equals(document.documentAsMap);
    }
    
    @Override
    public int hashCode() {
        return this.documentAsMap.hashCode();
    }
    
    @Override
    public String toString() {
        return "Document{" + this.documentAsMap + '}';
    }
}
