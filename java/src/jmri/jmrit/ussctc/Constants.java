package jmri.jmrit.ussctc;

/**
 * Define common constants for the ussctc package.
 * <p>
 * Names automatically created by the package have a common
 * start which consists of a prefix and suffix
 * defined here, sandwiched around the name of the class creating them.
 * e.g. "USS CTC:OSINDICATOR:1:CTC TC 57" for something for "CTC TC 57"
 * created by the OsIndicator class.
 *
 * @author Bob Jacobsen Copyright (C) 2007
 */
public interface Constants {

    final static String nameDivider = ":";
    final static String pkgPrefix = "USS CTC";

    final static String commonNamePrefix = pkgPrefix + nameDivider;
    final static String commonNameSuffix = nameDivider + "1" + nameDivider;
    // the "1" is a placeholder for later, in case more that one machine/code line 
    // is needed

}
