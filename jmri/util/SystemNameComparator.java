package jmri.util;

import java.util.*;

/**
 * Comparator for JMRI System Names.  
 * <P> A System Name is two letters
 * followed by either an alpha name or a number.
 * In the number case, this does a numeric comparison.
 *
 * @author	Bob Jacobsen   Copyright (C) 2004
 * @author  Howard Penny
 * @version	$Revision: 1.1 $
 */

public class SystemNameComparator implements Comparator {
    public SystemNameComparator() {}
    
    public int compare(Object o1, Object o2) {
        if (o1.toString().length() <= 3 && o2.toString().length() <= 3) {
            return o1.toString().compareTo(o2.toString());
        } else if (!o1.toString().regionMatches(0,o2.toString(),0,2))
            {
                return o1.toString().compareTo(o2.toString());
            } else {
                try {
                    return Integer.parseInt(o1.toString().substring(2)) -
                        Integer.parseInt(o2.toString().substring(2));
                } catch (NumberFormatException e) {
                    return o1.toString().compareTo(o2.toString());
                }
            }
    }
}
