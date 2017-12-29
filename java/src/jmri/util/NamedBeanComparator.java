package jmri.util;

import jmri.NamedBean;
import jmri.Manager;

/**
 * Comparator for JMRI NamedBeans via their system Names.
 * <P>
 * A System Name is a system prefix followed by type letter then a suffix with a system-specific format. 
 * This class first compares on prefix, then if the prefixes are equal it 
 * compares the type letter, then if they're still equal it
 * does an {@lnk AlphanumComparater} compare on suffix.
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

    static AlphanumComparator ac = new AlphanumComparator();
    
    @Override
    public int compare(NamedBean ob1, NamedBean ob2) {
        String o1 = ob1.getSystemName();
        String o2 = ob2.getSystemName();
        
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
