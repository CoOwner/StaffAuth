package org.bson.types;

import org.bson.*;
import java.util.*;

public class BasicBSONList extends ArrayList<Object> implements BSONObject
{
    private static final long serialVersionUID = -4415279469780082174L;
    
    @Override
    public Object put(final String key, final Object v) {
        return this.put(this._getInt(key), v);
    }
    
    public Object put(final int key, final Object value) {
        while (key >= this.size()) {
            this.add(null);
        }
        this.set(key, value);
        return value;
    }
    
    @Override
    public void putAll(final Map m) {
        for (final Map.Entry entry : m.entrySet()) {
            this.put(entry.getKey().toString(), entry.getValue());
        }
    }
    
    @Override
    public void putAll(final BSONObject o) {
        for (final String k : o.keySet()) {
            this.put(k, o.get(k));
        }
    }
    
    @Override
    public Object get(final String key) {
        final int i = this._getInt(key);
        if (i < 0) {
            return null;
        }
        if (i >= this.size()) {
            return null;
        }
        return this.get(i);
    }
    
    @Override
    public Object removeField(final String key) {
        final int i = this._getInt(key);
        if (i < 0) {
            return null;
        }
        if (i >= this.size()) {
            return null;
        }
        return this.remove(i);
    }
    
    @Deprecated
    @Override
    public boolean containsKey(final String key) {
        return this.containsField(key);
    }
    
    @Override
    public boolean containsField(final String key) {
        final int i = this._getInt(key, false);
        return i >= 0 && i >= 0 && i < this.size();
    }
    
    @Override
    public Set<String> keySet() {
        return new StringRangeSet(this.size());
    }
    
    @Override
    public Map toMap() {
        final Map m = new HashMap();
        for (final Object s : this.keySet()) {
            m.put(s, this.get(String.valueOf(s)));
        }
        return m;
    }
    
    int _getInt(final String s) {
        return this._getInt(s, true);
    }
    
    int _getInt(final String s, final boolean err) {
        try {
            return Integer.parseInt(s);
        }
        catch (Exception e) {
            if (err) {
                throw new IllegalArgumentException("BasicBSONList can only work with numeric keys, not: [" + s + "]");
            }
            return -1;
        }
    }
}
