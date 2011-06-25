package jmri;


/**
 * Defines a simple place to get the JMRI version string.
 *<P>
 * These JavaDocs are for Version 2.11.8 of JMRI.
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 * @author  Bob Jacobsen   Copyright 2000 - 2011
 * @version $Revision: 1.165 $
 */

public class Version {

    /**
     * Major number changes with large incompatible
     * changes in requirements or API.
     */
     static final public int major = 2;
     
    /**
     * Minor number changes with each production release.
     * Odd is development, even is production.
     */
     static final public int minor = 11;
     
    /* Test number changes with individual releases,
     * general fastest for test releases.
     */
     static final public int test = 9;
     
    /**
     * Modifier is used to denote specific builds.
     * It should be the empty string "" for released versions.
     */
     static final boolean released = false;
     static final public String modifier = released?"."+test:"."+test+"+dev";

    /**
     * Provide the current version string in I.J.Kmod format.
     * <P>
     * This is manually maintained by updating it before each
     * release is built.
     *
     * @return The current version string
     */
     static public String name() { 
        return ""+major+"."+minor+modifier; 
     }
     
     /**
      * Standalone print of version string and exit.
      */
    static public void main(String[] args) {
        System.out.println(name());
    }
}
