package org.bson;

import org.bson.codecs.configuration.*;
import java.util.*;
import org.bson.codecs.*;
import java.io.*;

public final class BsonDocumentWrapper<T> extends BsonDocument
{
    private static final long serialVersionUID = 1L;
    private final transient T wrappedDocument;
    private final transient Encoder<T> encoder;
    private BsonDocument unwrapped;
    
    public static BsonDocument asBsonDocument(final Object document, final CodecRegistry codecRegistry) {
        if (document == null) {
            return null;
        }
        if (document instanceof BsonDocument) {
            return (BsonDocument)document;
        }
        return new BsonDocumentWrapper<Object>(document, codecRegistry.get(document.getClass()));
    }
    
    public BsonDocumentWrapper(final T wrappedDocument, final Encoder<T> encoder) {
        if (wrappedDocument == null) {
            throw new IllegalArgumentException("Document can not be null");
        }
        this.wrappedDocument = wrappedDocument;
        this.encoder = encoder;
    }
    
    public T getWrappedDocument() {
        return this.wrappedDocument;
    }
    
    public Encoder<T> getEncoder() {
        return this.encoder;
    }
    
    public boolean isUnwrapped() {
        return this.unwrapped != null;
    }
    
    @Override
    public int size() {
        return this.getUnwrapped().size();
    }
    
    @Override
    public boolean isEmpty() {
        return this.getUnwrapped().isEmpty();
    }
    
    @Override
    public boolean containsKey(final Object key) {
        return this.getUnwrapped().containsKey(key);
    }
    
    @Override
    public boolean containsValue(final Object value) {
        return this.getUnwrapped().containsValue(value);
    }
    
    @Override
    public BsonValue get(final Object key) {
        return this.getUnwrapped().get(key);
    }
    
    @Override
    public BsonValue put(final String key, final BsonValue value) {
        return this.getUnwrapped().put(key, value);
    }
    
    @Override
    public BsonValue remove(final Object key) {
        return this.getUnwrapped().remove(key);
    }
    
    @Override
    public void putAll(final Map<? extends String, ? extends BsonValue> m) {
        super.putAll(m);
    }
    
    @Override
    public void clear() {
        super.clear();
    }
    
    @Override
    public Set<String> keySet() {
        return this.getUnwrapped().keySet();
    }
    
    @Override
    public Collection<BsonValue> values() {
        return this.getUnwrapped().values();
    }
    
    @Override
    public Set<Map.Entry<String, BsonValue>> entrySet() {
        return this.getUnwrapped().entrySet();
    }
    
    @Override
    public boolean equals(final Object o) {
        return this.getUnwrapped().equals(o);
    }
    
    @Override
    public int hashCode() {
        return this.getUnwrapped().hashCode();
    }
    
    @Override
    public String toString() {
        return this.getUnwrapped().toString();
    }
    
    @Override
    public BsonDocument clone() {
        return this.getUnwrapped().clone();
    }
    
    private BsonDocument getUnwrapped() {
        if (this.encoder == null) {
            throw new BsonInvalidOperationException("Can not unwrap a BsonDocumentWrapper with no Encoder");
        }
        if (this.unwrapped == null) {
            final BsonDocument unwrapped = new BsonDocument();
            final BsonWriter writer = new BsonDocumentWriter(unwrapped);
            this.encoder.encode(writer, this.wrappedDocument, EncoderContext.builder().build());
            this.unwrapped = unwrapped;
        }
        return this.unwrapped;
    }
    
    private Object writeReplace() {
        return this.getUnwrapped();
    }
    
    private void readObject(final ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }
}
