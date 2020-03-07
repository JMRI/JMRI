package jmri.util;

import java.util.Comparator;

import jmri.NamedBean;

/**
 * Compare two NamedBeans using the {@link PreferNumericComparator} against
 * {@link NamedBean#getSystemName()} for each NamedBean.
 * <p>
 * If the requirement is that {@link Comparator#compare(Object, Object)} return
 * 0 for two numerically identical NamedBean System Names (i.e.
 * {@code IT42 == IT0042}), use {@link NamedBeanComparator}, but if the
 * requirement is that System Names should be numerically ordered, but that
 * non-identical representations of numbers should be different, (i.e.
 * {@code IT42 != IT0042}, but order should be
 * {@code IT3, IT4, IT5, IT42, IT0042, IT50}), use this Comparator.
 *
 * @author Randall Wood Copyright 2019
 * @param <B> the type of NamedBean to compare
 */
public class NamedBeanPreferNumericComparator<B extends NamedBean> extends NamedBeanComparator<B> {

    private final PreferNumericComparator comparator = new PreferNumericComparator();

    @Override
    public int compare(B n1, B n2) {
        return comparator.compare(n1.getSystemName(), n2.getSystemName());
    }

}
