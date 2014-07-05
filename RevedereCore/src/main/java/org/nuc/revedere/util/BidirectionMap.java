package org.nuc.revedere.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class BidirectionMap<K, V> {
    private Map<K, V> directMap;
    private Map<V, K> reverseMap;

    public BidirectionMap() {
        directMap = new HashMap<>();
        reverseMap = new HashMap<>();
    }
    
    public void put(K key, V value) {
        directMap.put(key, value);
        reverseMap.put(value, key);
    }
    
    public V getValue(K key) {
        return directMap.get(key);
    }
    
    public K getKey(V value) {
        return reverseMap.get(value);
    }
    
    public V removeKey(K key) {
        final V value = directMap.remove(key);
        if (value!= null) {
            reverseMap.remove(value);
        }
        return value;
    }
    
    public K removeValue(V value) {
        final K key = reverseMap.remove(value);
        if (key != null) {
            directMap.remove(key);
        }
        return key;
    }
    
    public int size(){
        return directMap.size();
    }
    
    public boolean containsKey(K key) {
        return directMap.containsKey(key);
    }
    
    public boolean containsValue(V value) {
        return reverseMap.containsKey(value);
    }

    public Collection<V> values() {
        return directMap.values();
    }

    public Collection<K> keys() {
        return reverseMap.values();
    }
    
    public void clear() {
        directMap.clear();
        reverseMap.clear();
    }
}
