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
        // handle both usernames being null or empty
        if ((s1 == null || s1.isEmpty()) && (s2 == null || s2.isEmpty())) {
            return n1.compareTo(n2);
        }
        if (s1 == null || s1.isEmpty()) {
            s1 = n1.getSystemName();
        }
        if (s2 == null || s2.isEmpty()) {
            s2 = n1.getSystemName();
        }
        return comparison != 0 ? comparison : comparator.compare(s1, s2);
    }
}
