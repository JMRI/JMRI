// Application.java

package jmri;

/**
 * A lightweight class that provides a methods to retrieve the current
 * JMRI application name.
 * <P>
 * The current name is set via reflection when a given JMRI application
 * is launched.
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
 *
 * @author      Matthew Harris  Copyright (C) 2011
 * @version     $Revision: 1.1 $
 */
public class Application {

//    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="MS_CANNOT_BE_FINAL",
//                                            justification="only one application")
    private static String name = "JMRI";

    /**
     * Return the current JMRI application name
     * @return String containing JMRI application name
     */
    public static String getApplicationName() {
        return name;
    }

//    /**
//     * Set the current JMRI application name
//     * @param name String containing JMRI application name
//     */
//    abstract protected void setApplicationName(String name);
//
}

/* @(#)Application.java */