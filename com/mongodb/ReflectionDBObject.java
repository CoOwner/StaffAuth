package com.mongodb;

import org.bson.*;
import java.lang.reflect.*;
import java.util.*;

public abstract class ReflectionDBObject implements DBObject
{
    JavaWrapper _wrapper;
    Object _id;
    private static final Map<Class, JavaWrapper> _wrappers;
    private static final Set<String> IGNORE_FIELDS;
    
    @Override
    public Object get(final String key) {
        return this.getWrapper().get(this, key);
    }
    
    @Override
    public Set<String> keySet() {
        return this.getWrapper().keySet();
    }
    
    @Deprecated
    @Override
    public boolean containsKey(final String key) {
        return this.containsField(key);
    }
    
    @Override
    public boolean containsField(final String fieldName) {
        return this.getWrapper().containsKey(fieldName);
    }
    
    @Override
    public Object put(final String key, final Object v) {
        return this.getWrapper().set(this, key, v);
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
    
    public Object get_id() {
        return this._id;
    }
    
    public void set_id(final Object id) {
        this._id = id;
    }
    
    @Override
    public boolean isPartialObject() {
        return false;
    }
    
    @Override
    public Map toMap() {
        final Map m = new HashMap();
        for (final Object s : this.keySet()) {
            m.put(s, this.get(s + ""));
        }
        return m;
    }
    
    @Override
    public void markAsPartialObject() {
        throw new RuntimeException("ReflectionDBObjects can't be partial");
    }
    
    @Override
    public Object removeField(final String key) {
        throw new UnsupportedOperationException("can't remove from a ReflectionDBObject");
    }
    
    JavaWrapper getWrapper() {
        if (this._wrapper != null) {
            return this._wrapper;
        }
        return this._wrapper = getWrapper(this.getClass());
    }
    
    public static JavaWrapper getWrapperIfReflectionObject(final Class c) {
        if (ReflectionDBObject.class.isAssignableFrom(c)) {
            return getWrapper(c);
        }
        return null;
    }
    
    public static JavaWrapper getWrapper(final Class c) {
        JavaWrapper w = ReflectionDBObject._wrappers.get(c);
        if (w == null) {
            w = new JavaWrapper(c);
            ReflectionDBObject._wrappers.put(c, w);
        }
        return w;
    }
    
    static {
        _wrappers = Collections.synchronizedMap(new HashMap<Class, JavaWrapper>());
        (IGNORE_FIELDS = new HashSet<String>()).add("Int");
    }
    
    public static class JavaWrapper
    {
        final Class clazz;
        final String name;
        final Map<String, FieldInfo> fields;
        final Set<String> keys;
        
        JavaWrapper(final Class c) {
            this.clazz = c;
            this.name = c.getName();
            this.fields = new TreeMap<String, FieldInfo>();
            for (final Method m : c.getMethods()) {
                if (m.getName().startsWith("get") || m.getName().startsWith("set")) {
                    final String name = m.getName().substring(3);
                    if (name.length() != 0) {
                        if (!ReflectionDBObject.IGNORE_FIELDS.contains(name)) {
                            final Class type = m.getName().startsWith("get") ? m.getReturnType() : m.getParameterTypes()[0];
                            FieldInfo fi = this.fields.get(name);
                            if (fi == null) {
                                fi = new FieldInfo(name, type);
                                this.fields.put(name, fi);
                            }
                            if (m.getName().startsWith("get")) {
                                fi.getter = m;
                            }
                            else {
                                fi.setter = m;
                            }
                        }
                    }
                }
            }
            final Set<String> names = new HashSet<String>(this.fields.keySet());
            for (final String name2 : names) {
                if (!this.fields.get(name2).ok()) {
                    this.fields.remove(name2);
                }
            }
            this.keys = Collections.unmodifiableSet((Set<? extends String>)this.fields.keySet());
        }
        
        public Set<String> keySet() {
            return this.keys;
        }
        
        @Deprecated
        public boolean containsKey(final String key) {
            return this.keys.contains(key);
        }
        
        public Object get(final ReflectionDBObject document, final String fieldName) {
            final FieldInfo i = this.fields.get(fieldName);
            if (i == null) {
                return null;
            }
            try {
                return i.getter.invoke(document, new Object[0]);
            }
            catch (Exception e) {
                throw new RuntimeException("could not invoke getter for [" + fieldName + "] on [" + this.name + "]", e);
            }
        }
        
        public Object set(final ReflectionDBObject document, final String fieldName, final Object value) {
            final FieldInfo i = this.fields.get(fieldName);
            if (i == null) {
                throw new IllegalArgumentException("no field [" + fieldName + "] on [" + this.name + "]");
            }
            try {
                return i.setter.invoke(document, value);
            }
            catch (Exception e) {
                throw new RuntimeException("could not invoke setter for [" + fieldName + "] on [" + this.name + "]", e);
            }
        }
        
        Class<? extends DBObject> getInternalClass(final List<String> path) {
            final String cur = path.get(0);
            final FieldInfo fi = this.fields.get(cur);
            if (fi == null) {
                return null;
            }
            if (path.size() == 1) {
                return fi.clazz;
            }
            final JavaWrapper w = ReflectionDBObject.getWrapperIfReflectionObject(fi.clazz);
            if (w == null) {
                return null;
            }
            return w.getInternalClass(path.subList(1, path.size()));
        }
    }
    
    static class FieldInfo
    {
        final String name;
        final Class<? extends DBObject> clazz;
        Method getter;
        Method setter;
        
        FieldInfo(final String name, final Class<? extends DBObject> clazz) {
            this.name = name;
            this.clazz = clazz;
        }
        
        boolean ok() {
            return this.getter != null && this.setter != null;
        }
    }
}
