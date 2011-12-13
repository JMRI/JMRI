package jmri.util;

import java.util.*;

/**
 * Hashtable that preserves order for later access.
 *
 * @author Bob Jacobsen
 * @version $Revision$
 */

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
        if (! super.equals(o)) return false;
        if (o instanceof OrderedHashtable<?, ?>)
            return this.keys.equals( ((OrderedHashtable<?, ?>)o).keys);
        else return false;
    }
    
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
    ArrayList<K> keys = new ArrayList<K>();
    
    class LocalEnumeration implements Enumeration<K> {
        public boolean hasMoreElements() {
            return (i<keys.size());
        }
        public K nextElement() {
            return keys.get(i++);
        }
        int i = 0;
    }
}
