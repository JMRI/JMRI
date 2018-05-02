package jmri.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Hashtable that preserves order for later access.
 *
 * @author Bob Jacobsen
 * @param <K> key class
 * @param <V> value class
 * @deprecated since 4.7.2; use {@link java.util.LinkedHashMap} instead.
 */
@Deprecated
public class OrderedHashtable<K, V> extends Hashtable<K, V> {

    @Override
    public V put(K key, V value) {
        keys.add(key);
        return super.put(key, value);
    }

    @Override
    public V remove(Object key) {
        keys.remove(key);
        return super.remove(key);
    }

    @Override
    public Enumeration<K> keys() {
        return new LocalEnumeration();
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        if (o instanceof OrderedHashtable<?, ?>) {
            // check order of keys; contents (without order) known to be equal from above
            return this.keys.equals(((OrderedHashtable<?, ?>) o).keys);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    ArrayList<K> keys = new ArrayList<>();

    class LocalEnumeration implements Enumeration<K> {

        @Override
        public boolean hasMoreElements() {
            return (i < keys.size());
        }

        @Override
        public K nextElement() {
            return keys.get(i++);
        }
        int i = 0;
    }
}
