package org.bson.types;

import com.mongodb.assertions.*;
import java.lang.reflect.*;
import java.util.*;

class StringRangeSet implements Set<String>
{
    private static final String[] STRINGS;
    private final int size;
    
    StringRangeSet(final int size) {
        Assertions.isTrue("size >= 0", size >= 0);
        this.size = size;
    }
    
    @Override
    public int size() {
        return this.size;
    }
    
    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }
    
    @Override
    public boolean contains(final Object o) {
        if (!(o instanceof String)) {
            return false;
        }
        final int i = Integer.parseInt((String)o);
        return i >= 0 && i < this.size();
    }
    
    @Override
    public Iterator<String> iterator() {
        return new Iterator<String>() {
            private int cur = 0;
            
            @Override
            public boolean hasNext() {
                return this.cur < StringRangeSet.this.size;
            }
            
            @Override
            public String next() {
                if (!this.hasNext()) {
                    throw new NoSuchElementException();
                }
                return StringRangeSet.this.intToString(this.cur++);
            }
            
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
    
    @Override
    public Object[] toArray() {
        final Object[] retVal = new Object[this.size()];
        for (int i = 0; i < this.size(); ++i) {
            retVal[i] = this.intToString(i);
        }
        return retVal;
    }
    
    @Override
    public <T> T[] toArray(final T[] a) {
        final T[] retVal = (T[])((a.length >= this.size()) ? a : ((Object[])Array.newInstance(a.getClass().getComponentType(), this.size)));
        for (int i = 0; i < this.size(); ++i) {
            retVal[i] = (T)this.intToString(i);
        }
        if (a.length > this.size()) {
            a[this.size] = null;
        }
        return retVal;
    }
    
    @Override
    public boolean add(final String integer) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean remove(final Object o) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean containsAll(final Collection<?> c) {
        for (final Object e : c) {
            if (!this.contains(e)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public boolean addAll(final Collection<? extends String> c) {
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
    
    private String intToString(final int i) {
        return (i < StringRangeSet.STRINGS.length) ? StringRangeSet.STRINGS[i] : Integer.toString(i);
    }
    
    static {
        STRINGS = new String[1024];
        for (int i = 0; i < StringRangeSet.STRINGS.length; ++i) {
            StringRangeSet.STRINGS[i] = String.valueOf(i);
        }
    }
}
