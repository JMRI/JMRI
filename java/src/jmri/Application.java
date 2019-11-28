package jmri;

/**
 * A lightweight class that provides a methods to retrieve the current JMRI
 * application name and icon.
 * <p>
 * The current name is set when a given JMRI application is launched.
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Matthew Harris Copyright (C) 2011
 */
public class Application {

    private static String name = null;
    private static String logo = "resources/logo.gif";
    private static String URL = "http://jmri.org";

    /**
     * Return the current JMRI application name.
     *
     * @return String containing JMRI application name or "JMRI" if name has not
     *         been set.
     */
    public static String getApplicationName() {
        if (Application.name == null) {
            return "JMRI";
        }
        return Application.name;
    }

    /**
     * Set the current JMRI application name.
     *
     * @param applicationName String containing the JMRI application name
     * @throws IllegalAccessException   if attempting to modify once set
     * @throws IllegalArgumentException if a null name passed
     */
    public static void setApplicationName(String applicationName) throws IllegalAccessException, IllegalArgumentException {
        if (Application.name == null) {
            if (applicationName != null) {
                Application.name = applicationName;
            } else {
                throw new IllegalArgumentException("Application name cannot be null.");
            }
        } else {
            throw new IllegalAccessException("Application name cannot be modified once set.");
        }
    }

    /**
     * Return the current JMRI application logo path. This path is relative to
     * the JMRI application installation path. If the application does not have
     * its own icon, return the JMRI default icon.
     *
     * @return String containing the application icon path
     */
    public static String getLogo() {
        return logo;
    }

    /**
     * Set the current JMRI application logo path.
     *
     * @param logo String with the relative path to the JMRI application icon
     */
    public static void setLogo(String logo) {
        if (logo == null) {
            logo = "resources/logo.gif";
        }
        Application.logo = logo;
    }

    /**
     * @return the URL
     */
    public static String getURL() {
        return Application.URL;
    }

    /**
     * @param URL the URL to set
     */
    public static void setURL(String URL) {
        if (URL == null) {
            URL = "http://jmri.org";
        }
        Application.URL = URL;
    }

}
