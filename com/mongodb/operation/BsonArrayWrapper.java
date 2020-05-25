package com.mongodb.operation;

import org.bson.assertions.*;
import org.bson.*;
import java.util.*;

class BsonArrayWrapper<T> extends BsonArray
{
    private final List<T> wrappedArray;
    
    public BsonArrayWrapper(final List<T> wrappedArray) {
        this.wrappedArray = Assertions.notNull("wrappedArray", wrappedArray);
    }
    
    public List<T> getWrappedArray() {
        return this.wrappedArray;
    }
    
    @Override
    public List<BsonValue> getValues() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public int size() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean contains(final Object o) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Iterator<BsonValue> iterator() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public <T> T[] toArray(final T[] a) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean add(final BsonValue bsonValue) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean remove(final Object o) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean containsAll(final Collection<?> c) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean addAll(final Collection<? extends BsonValue> c) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean addAll(final int index, final Collection<? extends BsonValue> c) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean removeAll(final Collection<?> c) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean retainAll(final Collection<?> c) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public BsonValue get(final int index) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public BsonValue set(final int index, final BsonValue element) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void add(final int index, final BsonValue element) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public BsonValue remove(final int index) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public int indexOf(final Object o) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public int lastIndexOf(final Object o) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ListIterator<BsonValue> listIterator() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public ListIterator<BsonValue> listIterator(final int index) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public List<BsonValue> subList(final int fromIndex, final int toIndex) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final BsonArrayWrapper<?> that = (BsonArrayWrapper<?>)o;
        return this.wrappedArray.equals(that.wrappedArray);
    }
    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + this.wrappedArray.hashCode();
        return result;
    }
    
    @Override
    public String toString() {
        return "BsonArrayWrapper{wrappedArray=" + this.wrappedArray + '}';
    }
    
    @Override
    public BsonArray clone() {
        throw new UnsupportedOperationException("This should never be called on an instance of this type");
    }
}
