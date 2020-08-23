package jmri.util;

import java.util.Comparator;

/**
 * Perform an comparison using {@link AlphanumComparator}, followed up with a
 * standard String comparison if
 * {@link AlphanumComparator#compare(String, String)} returns 0.
 * <p>
 * If the requirement is that {@link Comparator#compare(Object, Object)} return
 * 0 for two numerically identical Strings (i.e. {@code 42 == 0042}), use
 * {@link AlphanumComparator}, but if the requirement is that Strings should be
 * numerically ordered, but that non-identical representations should be
 * different, (i.e. {@code 42 != 0042}, but order should be
 * {@code 3, 4, 5, 42, 0042, 50}), use this Comparator, since the standard
 * String comparator will not order numbers correctly.
 * 
 * @author Randall Wood Copyright 2019
 */
public class PreferNumericComparator extends AlphanumComparator {

    @Override
    public int compare(String s1, String s2) {
        int comparison = super.compare(s1, s2);
        if (comparison == 0) {
            return s1.compareTo(s2);
        }
        return comparison;
    }
}
