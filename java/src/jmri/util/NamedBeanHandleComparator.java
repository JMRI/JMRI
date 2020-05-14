package jmri.util;

import java.util.Comparator;

import jmri.*;

/**
 * Comparator for JMRI NamedBeanHandle of NamedBeans via their System Names.
 * <p>
 * Uses the built-in Comparable interface of the named beans.
 * <p>
 * A System Name is a system prefix followed by type letter then a suffix with a
 * system-specific format. This class first compares on prefix, then if the
 * prefixes are equal it compares the type letter, then if they're still equal
 * it does an {@link AlphanumComparator} compare on suffix.
 * <p>
 * This sorts on the information in the NamedBean itself, including using the
 * actual type by deferring prefix comparison into the specific NamedBean
 * subclass. This is different from the (deprecated) SystemNameComparator, which
 * only does a common lexical sort. See the
 * <a href="http://jmri.org/help/en/html/doc/Technical/Names.shtml">Names
 * documentation page</a>.
 * 
 * @param <B> supported type of NamedBean
 * @param <H> NamedBeanHandle of B
 */
public class NamedBeanHandleComparator<H extends NamedBeanHandle> implements Comparator<H> {

    @Override
    public int compare(H n1, H n2) {
        return n1.getBean().compareTo(n2.getBean());
    }
}
