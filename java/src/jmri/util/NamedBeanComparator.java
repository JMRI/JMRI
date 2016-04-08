package jmri.util;

import jmri.NamedBean;

/**
 * Comparator for JMRI System Names.
 * <P>
 * A System Name is two letters followed by either an alpha name or a number. In
 * the number case, this does a numeric comparison. If the number is appended
 * with letters, does the numeric sort on the digits followed by a lexigraphic
 * sort on the remainder.
 *
 * @author	Pete Cressman Copyright (C) 2009
 *
 */
public class NamedBeanComparator extends SystemNameComparator {

    public NamedBeanComparator() {
    }

    public int compare(Object ob1, Object ob2) {
        String uName1 = ((NamedBean) ob1).getUserName();
        String uName2 = ((NamedBean) ob2).getUserName();
        if (uName2 == null || uName2.trim().length() == 0) {
            if (uName1 == null || uName1.trim().length() == 0) {
                return super.compare(ob1, ob2);
            } else {
                return -1;
            }
        } else {
            if (uName1 == null || uName1.trim().length() == 0) {
                return 1;
            } else {
                return uName1.compareTo(uName2);
            }
        }
    }
}
