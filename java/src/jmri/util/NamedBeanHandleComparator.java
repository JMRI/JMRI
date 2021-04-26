package jmri.util;

import java.util.Comparator;

import jmri.*;

/**
 * Comparator for JMRI NamedBeanHandle of NamedBeans via their System Names.
 * <p>
 * Uses the built-in Comparable interface of the named beans.
 * <p>
 * Note that unlike the NamedBean itself, the sytem name of a NamedBean within
 * a particular NamedBeanHandle is <u>not</u> immutable. The handle can point
 * at a different NamedBean after a rename operation. 
 *
 * @param <H> NamedBeanHandle of a supported type of NamedBean
 *
 * @see NamedBean
 * @see NamedBeanHandle
 * @see NamedBeanHandleManager
 * 
 */
public class NamedBeanHandleComparator<H extends NamedBeanHandle> implements Comparator<H> {

    @Override
    public int compare(H n1, H n2) {
        return n1.getBean().compareTo(n2.getBean());
    }
}
