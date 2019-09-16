package jmri.util;

import java.util.Comparator;

import jmri.Manager;

/**
 * Comparator for JMRI System Names.
 * <p>
 * A System Name is a system prefix followed by type letter then a suffix with a system-specific format. 
 * This class first compares on prefix, then if the prefixes are equal it 
 * compares the type letter, then if they're still equal it
 * does an {@link AlphanumComparator} compare on suffix.
 * <p>
 * Note it's better to use {@link NamedBeanComparator} if possible, as that 
 * can do type-specific comparison of the suffix part of complicated names.
 * See the <a href="http://jmri.org/help/en/html/doc/Technical/Names.shtml">Names documentation page</a>.
 * <p>
 * Note this is intended to take names as provided:  It does not do normalization or
 * expansion.
 *
 * @author	Bob Jacobsen Copyright (C) 2004, 2009, 2017
 * @deprecated 4.13.1 Use NamedBean comparison instead
 */
@Deprecated
public class SystemNameComparator implements Comparator<String> {

    public SystemNameComparator() {
    }

    static AlphanumComparator ac = new AlphanumComparator();
    
    @Override
    public int compare(String o1, String o2) {

        int p1len = Manager.getSystemPrefixLength(o1);
        int p2len = Manager.getSystemPrefixLength(o2);

        int comp = ac.compare(o1.substring(0, p1len), o2.substring(0, p2len));
        if (comp != 0) return comp;

        char c1 = o1.charAt(p1len);
        char c2 = o2.charAt(p2len);
           
        if (c1 == c2) return ac.compare(o1.substring(p1len+1), o2.substring(p2len+1));
        else return (c1 > c2) ? +1 : -1 ;
    }
}
