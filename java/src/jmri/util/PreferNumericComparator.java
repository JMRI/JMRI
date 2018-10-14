package jmri.util;

/**
 * Originally, this attempted to compare as Integer, and then alphanumeric.
 * It's now deprecated in favor of the more general AlphanumComparator
 * @deprecated 4.11.1 use AlphanumComparator
 *
 * @author	Bob Jacobsen Copyright (C) 2013, 2017
 */
@Deprecated // in 4.11.1 use AlphanumComparator
public class PreferNumericComparator extends AlphanumComparator {
}
