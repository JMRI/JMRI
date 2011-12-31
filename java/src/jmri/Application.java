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
 * @version     $Revision$
 */
public class Application {

    private static String applicationName = null;

    /**
     * Return the current JMRI application name
     * @return String containing JMRI application name or "JMRI" if name has not been set.
     */
    public static String getApplicationName() {
        if (Application.applicationName == null) {
            return "JMRI";
        }
        return Application.applicationName;
    }

    /**
     * Set the current JMRI application name
     * @param applicationName String containing the JMRI application name
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public static void setApplicationName(String applicationName) throws IllegalAccessException, IllegalArgumentException {
        if (Application.applicationName == null) {
            if (applicationName != null) {
                Application.applicationName = applicationName;
            } else {
                throw new IllegalArgumentException("Application name cannot be null.");
            }
        } else {
            throw new IllegalAccessException("Application name cannot be modified once set.");
        }
    }

}

/* @(#)Application.java */