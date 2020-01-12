package jmri;

import java.util.Arrays;
import java.util.List;
import jmri.profile.ProfileManager;

/**
 * Meta data concerning the JMRI application.
 * <p>
 * Meta data is static information concerning the JMRI application. This class
 * provides a single container for listing and storing JMRI meta data.
 *
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
 * @author Randall Wood Copyright (C) 2011
 */
public interface Metadata {

    public static final String JMRIVERSION = "JMRIVERSION"; // NOI18N
    public static final String JMRIVERCANON = "JMRIVERCANON"; // NOI18N
    public static final String JMRIVERMAJOR = "JMRIVERMAJOR"; // NOI18N
    public static final String JMRIVERMINOR = "JMRIVERMINOR"; // NOI18N
    public static final String JMRIVERTEST = "JMRIVERTEST"; // NOI18N
    public static final String JVMVERSION = "JVMVERSION"; // NOI18N
    public static final String JVMVENDOR = "JVMVENDOR"; // NOI18N
    public static final String ACTIVEPROFILE = "activeProfile"; // NOI18N
    public static final String COPYRIGHT = "copyright"; // NOI18N

    /**
     * Return the value of the named meta data, or any valid system property.
     *
     * @param name name of meta data or property to return
     * @return String value of requested data or null
     */
    public static String getBySystemName(String name) {
        if (name.equalsIgnoreCase(JMRIVERSION)) {
            return jmri.Version.name();
        } else if (name.equalsIgnoreCase(JMRIVERCANON)) {
            return jmri.Version.getCanonicalVersion();
        } else if (name.equalsIgnoreCase(JMRIVERMAJOR)) {
            return Integer.toString(jmri.Version.major);
        } else if (name.equalsIgnoreCase(JMRIVERMINOR)) {
            return Integer.toString(jmri.Version.minor);
        } else if (name.equalsIgnoreCase(JMRIVERTEST)) {
            return Integer.toString(jmri.Version.test);
        } else if (name.equalsIgnoreCase(JVMVERSION)) {
            return System.getProperty("java.version", "<unknown>"); // NOI18N
        } else if (name.equalsIgnoreCase(JVMVENDOR)) {
            return System.getProperty("java.vendor", "<unknown>"); // NOI18N
        } else if (name.equalsIgnoreCase(ACTIVEPROFILE)) {
            return ProfileManager.getDefault().getActiveProfileName();
        } else if (name.equalsIgnoreCase(COPYRIGHT)) {
            return jmri.Version.getCopyright();
        }
        // returns null if name is not a system property
        return System.getProperty(name);
    }

    /**
     * An array of known meta data names.
     *
     * @return String[]
     */
    public static String[] getSystemNameArray() {
        String[] names = {JMRIVERSION,
            JMRIVERCANON,
            JMRIVERMAJOR,
            JMRIVERMINOR,
            JMRIVERTEST,
            JVMVERSION,
            JVMVENDOR,
            ACTIVEPROFILE,
            COPYRIGHT};
        return names;
    }

    /**
     * Get the list of known meta-data names.
     * @return the list of names
     */
    public static List<String> getSystemNameList() {
        return Arrays.asList(Metadata.getSystemNameArray());
    }

}
