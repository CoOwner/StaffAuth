package org.bson;

import java.util.*;
import org.bson.types.*;

public class LazyBSONCallback extends EmptyBSONCallback
{
    private Object root;
    
    @Override
    public void reset() {
        this.root = null;
    }
    
    @Override
    public Object get() {
        return this.getRoot();
    }
    
    @Override
    public void gotBinary(final String name, final byte type, final byte[] data) {
        this.setRoot(this.createObject(data, 0));
    }
    
    public Object createObject(final byte[] bytes, final int offset) {
        return new LazyBSONObject(bytes, offset, this);
    }
    
    public List createArray(final byte[] bytes, final int offset) {
        return new LazyBSONList(bytes, offset, this);
    }
    
    public Object createDBRef(final String ns, final ObjectId id) {
        return new BasicBSONObject("$ns", ns).append("$id", id);
    }
    
    private Object getRoot() {
        return this.root;
    }
    
    private void setRoot(final Object root) {
        this.root = root;
    }
}
