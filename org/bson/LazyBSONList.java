package org.bson;

import java.util.*;

public class LazyBSONList extends LazyBSONObject implements List
{
    public LazyBSONList(final byte[] bytes, final LazyBSONCallback callback) {
        super(bytes, callback);
    }
    
    public LazyBSONList(final byte[] bytes, final int offset, final LazyBSONCallback callback) {
        super(bytes, offset, callback);
    }
    
    @Override
    public int size() {
        return this.keySet().size();
    }
    
    @Override
    public boolean contains(final Object o) {
        return this.indexOf(o) > -1;
    }
    
    @Override
    public Iterator iterator() {
        return new LazyBSONListIterator();
    }
    
    @Override
    public boolean containsAll(final Collection collection) {
        final Set<Object> values = new HashSet<Object>();
        for (final Object o : this) {
            values.add(o);
        }
        return values.containsAll(collection);
    }
    
    @Override
    public Object get(final int index) {
        return this.get(String.valueOf(index));
    }
    
    @Override
    public int indexOf(final Object o) {
        final Iterator it = this.iterator();
        int pos = 0;
        while (it.hasNext()) {
            if (o.equals(it.next())) {
                return pos;
            }
            ++pos;
        }
        return -1;
    }
    
    @Override
    public int lastIndexOf(final Object o) {
        int lastFound = -1;
        final Iterator it = this.iterator();
        int pos = 0;
        while (it.hasNext()) {
            if (o.equals(it.next())) {
                lastFound = pos;
            }
            ++pos;
        }
        return lastFound;
    }
    
    @Override
    public ListIterator listIterator() {
        throw new UnsupportedOperationException("Operation is not supported instance of this type");
    }
    
    @Override
    public ListIterator listIterator(final int index) {
        throw new UnsupportedOperationException("Operation is not supported instance of this type");
    }
    
    @Override
    public boolean add(final Object o) {
        throw new UnsupportedOperationException("Object is read only");
    }
    
    @Override
    public boolean remove(final Object o) {
        throw new UnsupportedOperationException("Object is read only");
    }
    
    @Override
    public boolean addAll(final Collection c) {
        throw new UnsupportedOperationException("Object is read only");
    }
    
    @Override
    public boolean addAll(final int index, final Collection c) {
        throw new UnsupportedOperationException("Object is read only");
    }
    
    @Override
    public boolean removeAll(final Collection c) {
        throw new UnsupportedOperationException("Object is read only");
    }
    
    @Override
    public boolean retainAll(final Collection c) {
        throw new UnsupportedOperationException("Object is read only");
    }
    
    @Override
    public void clear() {
        throw new UnsupportedOperationException("Object is read only");
    }
    
    @Override
    public Object set(final int index, final Object element) {
        throw new UnsupportedOperationException("Object is read only");
    }
    
    @Override
    public void add(final int index, final Object element) {
        throw new UnsupportedOperationException("Object is read only");
    }
    
    @Override
    public Object remove(final int index) {
        throw new UnsupportedOperationException("Object is read only");
    }
    
    @Override
    public List subList(final int fromIndex, final int toIndex) {
        throw new UnsupportedOperationException("Operation is not supported");
    }
    
    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException("Operation is not supported");
    }
    
    @Override
    public Object[] toArray(final Object[] a) {
        throw new UnsupportedOperationException("Operation is not supported");
    }
    
    public class LazyBSONListIterator implements Iterator
    {
        private final BsonBinaryReader reader;
        private BsonType cachedBsonType;
        
        public LazyBSONListIterator() {
            (this.reader = LazyBSONList.this.getBsonReader()).readStartDocument();
        }
        
        @Override
        public boolean hasNext() {
            if (this.cachedBsonType == null) {
                this.cachedBsonType = this.reader.readBsonType();
            }
            return this.cachedBsonType != BsonType.END_OF_DOCUMENT;
        }
        
        @Override
        public Object next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            }
            this.cachedBsonType = null;
            this.reader.readName();
            return LazyBSONList.this.readValue(this.reader);
        }
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException("Operation is not supported");
        }
    }
}
