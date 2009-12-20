package jmri.util;

import java.util.*;

/**
 * Hashtable that preserves order for later access.
 *
 * @author Bob Jacobsen
 * @version $Revision: 1.3 $
 */

public class OrderedHashtable<K, V> extends Hashtable<K, V> {

    public V put(K key, V value) {
        keys.add(key);
        return super.put(key, value);
    }
    
    public V remove(Object key) {
        keys.remove(key);
        return super.remove(key);
    }
    
    public Enumeration<K> keys() {
        return new LocalEnumeration();
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
