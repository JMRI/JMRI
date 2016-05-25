package jmri.jmrit.ussctc;

/**
 * Define common constants for the ussctc package
 *
 * @author	Bob Jacobsen Copyright (C) 2007
 */
public interface Constants {

    final static String nameDivider = ":";
    final static String pkgPrefix = "USS CTC";
    // Must be upper case by convention

    final static String commonNamePrefix = pkgPrefix + nameDivider;
    final static String commonNameSuffix = nameDivider + "1" + nameDivider;
    // the "1" is a placeholder for later, in case more that one machine/code line 
    // is needed

}
