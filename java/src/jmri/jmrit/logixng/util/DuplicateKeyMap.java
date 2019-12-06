package jmri.jmrit.logixng.util;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A map that may contain multiple items with same key
 */
public class DuplicateKeyMap<K, V> implements Map<K, V> {

    Map<K, List<V>> _internalMap = new HashMap<>();
    
    @Override
    public int size() {
        int c = 0;
        for (List<V> l : _internalMap.values()) {
            c += l.size();
        }
        return c;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return _internalMap.containsKey(key);
    }

    // Map.containsValue() requires that the parameter is of type Object
    // The SuppressWarnings is because l.contains(value) expects a value of type V
    @SuppressWarnings({"unchecked", "element-type-mismatch"})
    @Override
    public boolean containsValue(Object value) {
        for (List<V> l : _internalMap.values()) {
            if (l.contains(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(Object key) {
        throw new UnsupportedOperationException("Not supported");
    }

    /**
     * Get all items in the map that has the key 'key'
     * @param key the key whose associated values is to be returned
     * @return an unmodifiable list of all the items
     */
    public List<V> getAll(K key) {
        List<V> list = _internalMap.get(key);
        if (list == null) list = new ArrayList<>();
        return Collections.unmodifiableList(list);
    }

    /**
     * {@inheritDoc}
     * @return always null
     */
    @Override
    public V put(K key, V value) {
        List<V> l = _internalMap.get(key);
        if (l == null) {
            l = new ArrayList<>();
            _internalMap.put(key, l);
        }
        if (! l.contains(value)) {
            l.add(value);
        }
        return null;
    }

    @Override
    public void putAll(Map m) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException("Not supported");
    }

    /**
     * Remove a value.
     * @param key the key
     * @param value the value
     */
    public void removeValue(K key, V value) {
        List<V> l = _internalMap.get(key);
        if (l != null) {
            l.remove(value);
        }
    }

    @Override
    public void clear() {
        // Empty the lists since others may have indirect references to the list
        // after calling the method getAll()
        for (List<V> l : _internalMap.values()) {
            l.clear();
        }
        _internalMap.clear();
    }

    @Override
    public Set<K> keySet() {
        return Collections.unmodifiableSet(_internalMap.keySet());
    }

    @Override
    public Collection<V> values() {
        List<V> list = new ArrayList<>();
        for (List<V> l : _internalMap.values()) {
            list.addAll(l);
        }
        return Collections.unmodifiableList(list);
    }

    @Override
    public Set<Map.Entry<K,V>> entrySet() {
        throw new UnsupportedOperationException("Not supported");
    }

}
