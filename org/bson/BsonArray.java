package org.bson;

import org.bson.json.*;
import org.bson.codecs.*;
import java.util.*;

public class BsonArray extends BsonValue implements List<BsonValue>, Cloneable
{
    private final List<BsonValue> values;
    
    public BsonArray(final List<? extends BsonValue> values) {
        this.values = new ArrayList<BsonValue>(values);
    }
    
    public BsonArray() {
        this.values = new ArrayList<BsonValue>();
    }
    
    public static BsonArray parse(final String json) {
        return new BsonArrayCodec().decode((BsonReader)new JsonReader(json), DecoderContext.builder().build());
    }
    
    public List<BsonValue> getValues() {
        return Collections.unmodifiableList((List<? extends BsonValue>)this.values);
    }
    
    @Override
    public BsonType getBsonType() {
        return BsonType.ARRAY;
    }
    
    @Override
    public int size() {
        return this.values.size();
    }
    
    @Override
    public boolean isEmpty() {
        return this.values.isEmpty();
    }
    
    @Override
    public boolean contains(final Object o) {
        return this.values.contains(o);
    }
    
    @Override
    public Iterator<BsonValue> iterator() {
        return this.values.iterator();
    }
    
    @Override
    public Object[] toArray() {
        return this.values.toArray();
    }
    
    @Override
    public <T> T[] toArray(final T[] a) {
        return this.values.toArray(a);
    }
    
    @Override
    public boolean add(final BsonValue bsonValue) {
        return this.values.add(bsonValue);
    }
    
    @Override
    public boolean remove(final Object o) {
        return this.values.remove(o);
    }
    
    @Override
    public boolean containsAll(final Collection<?> c) {
        return this.values.containsAll(c);
    }
    
    @Override
    public boolean addAll(final Collection<? extends BsonValue> c) {
        return this.values.addAll(c);
    }
    
    @Override
    public boolean addAll(final int index, final Collection<? extends BsonValue> c) {
        return this.values.addAll(index, c);
    }
    
    @Override
    public boolean removeAll(final Collection<?> c) {
        return this.values.removeAll(c);
    }
    
    @Override
    public boolean retainAll(final Collection<?> c) {
        return this.values.retainAll(c);
    }
    
    @Override
    public void clear() {
        this.values.clear();
    }
    
    @Override
    public BsonValue get(final int index) {
        return this.values.get(index);
    }
    
    @Override
    public BsonValue set(final int index, final BsonValue element) {
        return this.values.set(index, element);
    }
    
    @Override
    public void add(final int index, final BsonValue element) {
        this.values.add(index, element);
    }
    
    @Override
    public BsonValue remove(final int index) {
        return this.values.remove(index);
    }
    
    @Override
    public int indexOf(final Object o) {
        return this.values.indexOf(o);
    }
    
    @Override
    public int lastIndexOf(final Object o) {
        return this.values.lastIndexOf(o);
    }
    
    @Override
    public ListIterator<BsonValue> listIterator() {
        return this.values.listIterator();
    }
    
    @Override
    public ListIterator<BsonValue> listIterator(final int index) {
        return this.values.listIterator(index);
    }
    
    @Override
    public List<BsonValue> subList(final int fromIndex, final int toIndex) {
        return this.values.subList(fromIndex, toIndex);
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final BsonArray that = (BsonArray)o;
        return this.values.equals(that.values);
    }
    
    @Override
    public int hashCode() {
        return this.values.hashCode();
    }
    
    @Override
    public String toString() {
        return "BsonArray{values=" + this.values + '}';
    }
    
    public BsonArray clone() {
        final BsonArray to = new BsonArray();
        for (final BsonValue cur : this) {
            switch (cur.getBsonType()) {
                case DOCUMENT: {
                    to.add(cur.asDocument().clone());
                    continue;
                }
                case ARRAY: {
                    to.add(cur.asArray().clone());
                    continue;
                }
                case BINARY: {
                    to.add(BsonBinary.clone(cur.asBinary()));
                    continue;
                }
                case JAVASCRIPT_WITH_SCOPE: {
                    to.add(BsonJavaScriptWithScope.clone(cur.asJavaScriptWithScope()));
                    continue;
                }
                default: {
                    to.add(cur);
                    continue;
                }
            }
        }
        return to;
    }
}
