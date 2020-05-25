package org.bson.util;

import java.util.concurrent.*;
import com.mongodb.assertions.*;
import java.util.*;

final class ComputingMap<K, V> implements Map<K, V>, Function<K, V>
{
    private final ConcurrentMap<K, V> map;
    private final Function<K, V> function;
    
    public static <K, V> Map<K, V> create(final Function<K, V> function) {
        return new ComputingMap<K, V>((ConcurrentMap<K, V>)CopyOnWriteMap.newHashMap(), function);
    }
    
    ComputingMap(final ConcurrentMap<K, V> map, final Function<K, V> function) {
        this.map = Assertions.notNull("map", map);
        this.function = Assertions.notNull("function", function);
    }
    
    @Override
    public V get(final Object key) {
        while (true) {
            final V v = this.map.get(key);
            if (v != null) {
                return v;
            }
            final K k = (K)key;
            final V value = this.function.apply(k);
            if (value == null) {
                return null;
            }
            this.map.putIfAbsent(k, value);
        }
    }
    
    @Override
    public V apply(final K k) {
        return this.get(k);
    }
    
    @Override
    public V putIfAbsent(final K key, final V value) {
        return this.map.putIfAbsent(key, value);
    }
    
    @Override
    public boolean remove(final Object key, final Object value) {
        return this.map.remove(key, value);
    }
    
    @Override
    public boolean replace(final K key, final V oldValue, final V newValue) {
        return this.map.replace(key, oldValue, newValue);
    }
    
    @Override
    public V replace(final K key, final V value) {
        return this.map.replace(key, value);
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
    public V put(final K key, final V value) {
        return this.map.put(key, value);
    }
    
    @Override
    public V remove(final Object key) {
        return this.map.remove(key);
    }
    
    @Override
    public void putAll(final Map<? extends K, ? extends V> m) {
        this.map.putAll((Map<?, ?>)m);
    }
    
    @Override
    public void clear() {
        this.map.clear();
    }
    
    @Override
    public Set<K> keySet() {
        return this.map.keySet();
    }
    
    @Override
    public Collection<V> values() {
        return this.map.values();
    }
    
    @Override
    public Set<Entry<K, V>> entrySet() {
        return this.map.entrySet();
    }
    
    @Override
    public boolean equals(final Object o) {
        return this.map.equals(o);
    }
    
    @Override
    public int hashCode() {
        return this.map.hashCode();
    }
}
