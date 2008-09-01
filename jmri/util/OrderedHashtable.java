package jmri.util;

import java.util.*;

/**
 * Hashtable that preserves order for later access.
 *
 * @author Bob Jacobsen
 * @version $Revision: 1.1 $
 */

public class OrderedHashtable extends Hashtable {

    public Object put(Object key, Object value) {
        keys.add(key);
        return super.put(key, value);
    }
    
    public Object remove(Object key) {
        keys.remove(key);
        return super.remove(key);
    }
    
    public Enumeration keys() {
        return new LocalEnumeration();
    }
    
    ArrayList keys = new ArrayList();
    
    class LocalEnumeration implements Enumeration {
        public boolean hasMoreElements() {
            return (i<keys.size());
        }
        public Object nextElement() {
            return keys.get(i++);
        }
        int i = 0;
    }
}
