package jmri.util;

import jmri.NamedBean;

/**
 * Comparator for JMRI NamedBeans via their system Names.
 * <p>
 * Uses the in-built Comparable interface of the named beans.
 * <P>
 * A System Name is a system prefix followed by type letter then a suffix with a system-specific format. 
 * This class first compares on prefix, then if the prefixes are equal it 
 * compares the type letter, then if they're still equal it
 * does an {@link AlphanumComparator} compare on suffix.
 * <p>
 * This sorts on the information in the NamedBean itself, including using 
 * the actual type by deferring prefix comparison into the specific NamedBean subclass.
 * This different from {@link SystemNameComparator}, which only does a common
 * lexical sort.  
 * See the <a href="http://jmri.org/help/en/html/doc/Technical/Names.shtml">Names documentation page</a>.
 *
 *
 */
public class NamedBeanComparator implements java.util.Comparator<NamedBean> {

    public NamedBeanComparator() {
    }

    @Override
    public int compare(NamedBean n1, NamedBean n2) {
        return n1.compareTo(n2);
    }
}
