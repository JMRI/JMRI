package jmri.util;

import jmri.NamedBean;

/**
 * Comparator for JMRI NamedBeans via their User Names.
 * <p>
 * If the User Names are both non-null and are not equal, uses the {@link AlphanumComparator},
 * otherwise uses the {@link NamedBeanComparator}.
 * 
 * @param <B> supported type of NamedBean
 */
public class NamedBeanUserNameComparator<B extends NamedBean> implements java.util.Comparator<B> {

    public NamedBeanUserNameComparator() {
    }

    @Override
    public int compare(B n1, B n2) {
        String s1 = n1.getUserName();
        String s2 = n2.getUserName();
        int comparison = 0;
        AlphanumComparator comparator = new AlphanumComparator();
        if (s1 == null || s2 == null || (comparison = comparator.compare(s1, s2)) == 0) {
            return n1.compareTo(n2);
        }
        return comparison;
    }
}
