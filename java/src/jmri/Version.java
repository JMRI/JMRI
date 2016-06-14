package jmri;

import java.util.ResourceBundle;

/**
 * Defines a simple place to get the JMRI version string.
 * <p>
 * JMRI's version string comes in two forms, depending on whether it was built by an "official"
 * process or not, which in turn is determined by the "release.official" property:
 *<dl>
 *<dt>Official<dd>
 *<ul>
 *<li>If the revision number e.g. 123abc (git hash) is available in release.revision_id, then 
 * "4.1.1-R123abc". Note the "R".
 *<li>Else "4.1.1-(date)", where the date comes from the release.build_date property.
 *</ul>
 *<dt>Unofficial<dd>
 * Unofficial releases are marked by "ish" after the version number, and inclusion of the building user's ID.
 *<ul>
 *<li>If the revision number e.g. 123abc (git hash) is available in release.revision_id, then 
 * "4.1.1ish-(user)-(date)-R123abc". Note the "R".
 *<li>Else "4.1.1-(user)-(date)", where the date comes from the release.build_date property.
 *</ul>
 *</dl>
 * The release.revision_id, release.build_user and release.build_date properties are set at build time by Ant.
 * <p>
 * Generally, JMRI updates its version string in the code repository right <b>after</b> a release. 
 * Between formal release 1.2.3 and 1.2.4, the string will be 1.2.4ish. 
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 * @author Bob Jacobsen Copyright 1997-2016
 */
public class Version {

    static final private ResourceBundle versionBundle = ResourceBundle.getBundle("jmri.Version"); // NOI18N

    /**
     * Major number changes with large incompatible changes in requirements or
     * API.
     */
    static final public int major = Integer.parseInt(versionBundle.getString("release.major")); // NOI18N

    /**
     * Minor number changes with each production versionBundle. Odd is
     * development, even is production.
     */
    static final public int minor = Integer.parseInt(versionBundle.getString("release.minor")); // NOI18N;

    /* Test number changes with individual releases,
     * generally fastest for test releases. Set 0 for production
     */
    static final public int test = Integer.parseInt(versionBundle.getString("release.build")); // NOI18N;

    /* The user who built this versionBundle */
    static final public String buildUser = versionBundle.getString("release.build_user"); // NOI18N;

    /* The SVN revision ID for this versionBundle (if known) */
    static final public String revisionId = versionBundle.getString("release.revision_id"); // NOI18N;

    /* The date/time of this build */
    static final public String buildDate = versionBundle.getString("release.build_date"); // NOI18N;

    /* Has this build been created as a possible "official" versionBundle? */
    static final public boolean official = Boolean.parseBoolean(versionBundle.getString("release.official")); // NOI18N

    /**
     * The Modifier is the third term in the
     * 1.2.3 version name.  It's not present in production
     * versions that set it to zero.
     * Non-official versions get an "ish"
     */
    public static String getModifier() {
        StringBuilder modifier = new StringBuilder("");

        if (test != 0) {
            modifier.append(".").append(test);
        }

        if (! official) {
            modifier.append("ish");
        }

        return modifier.toString();
    }

    /**
     * Provide the current version string.
     * <P>
     * This string is built using various known build parameters, including the
     * versionBundle.{major,minor,build} values, the Git revision ID (if known)
     * and the official property
     *
     * @return The current version string
     */
    static public String name() {
        String releaseName;
        if (official) {
            String addOn;
            if ("unknown".equals(revisionId)) {
                addOn = buildDate;
            } else {
                addOn = "R" + revisionId;
            }
            releaseName = major + "." + minor + getModifier() + "-" + addOn;
        } else { // not official, so a development build that gets a user name
            String addOn;
            if ("unknown".equals(revisionId)) {
                addOn = buildDate + "-" + buildUser;
            } else {
                addOn = buildDate + "-" + buildUser + "-R" + revisionId;
            }
            releaseName = major + "." + minor + getModifier() + "-" + addOn;
        }
        return releaseName;
    }

    /**
     * Tests that a string contains a canonical version string.
     * <p>
     * A canonical version string is a string in the form x.y.z and is different
     * than the version string displayed using {@link #name() }. The canonical
     * version string for a JMRI instance is available using {@link #getCanonicalVersion()
     * }.
     *
     * @param version version string to check
     * @return true if version is a canonical version string
     */
    static public boolean isCanonicalVersion(String version) {
        String[] parts = version.split("\\.");
        if (parts.length != 3) {
            return false;
        }
        for (String part : parts) {
            if (Integer.parseInt(part) < 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compares a canonical version string to the JMRI canonical version and
     * returns an integer indicating if the string is less than, equal to, or
     * greater than the JMRI canonical version.
     *
     * @param version version string to compare
     * @return -1, 0, or 1 if version is less than, equal to, or greater than
     *         JMRI canonical version
     * @throws IllegalArgumentException if version is not a canonical version
     *                                  string
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    static public int compareCanonicalVersions(String version) throws IllegalArgumentException {
        return compareCanonicalVersions(version, getCanonicalVersion());
    }

    /**
     * Compares two canonical version strings and returns an integer indicating
     * if the first string is less than, equal to, or greater than the second
     * string.
     *
     * @param version1 a canonical version string
     * @param version2 a canonical version string
     * @return -1, 0, or 1 if version1 is less than, equal to, or greater than
     *         version2
     * @throws IllegalArgumentException if either version string is not a
     *                                  canonical version string
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    static public int compareCanonicalVersions(String version1, String version2) throws IllegalArgumentException {
        int result = 0;
        if (!isCanonicalVersion(version1)) {
            throw new IllegalArgumentException("Parameter version1 (" + version1 + ") is not a canonical version string.");
        }
        if (!isCanonicalVersion(version2)) {
            throw new IllegalArgumentException("Parameter version2 (" + version2 + ") is not a canonical version string.");
        }
        String[] p1 = version1.split("\\.");
        String[] p2 = version2.split("\\.");
        for (int i = 0; i < 3; i++) {
            result = p1[i].compareTo(p2[i]);
            if (result != 0) {
                return result;
            }
        }
        return result;
    }

    /**
     * Return the version as a major.minor.test String.
     *
     * @return The version
     */
    static public String getCanonicalVersion() {
        return major + "." + minor + "." + test;
    }

    /**
     * Return the application copyright as a String.
     *
     * @return The copyright
     */
    static public String getCopyright() {
        // TODO Internatonalize with Bundle.getMessage()
        return "Copyright \u00a9 " + versionBundle.getString("jmri.copyright.year") + " JMRI Community";
    }

    /**
     * Standalone print of version string and exit.
     *
     * This is used in the build.xml to generate parts of the installer
     * versionBundle file name, so take care in altering this code to make sure
     * the ant recipes are also suitably modified.
     *
     * @param args command-line arguments
     */
    static public void main(String[] args) {
        System.out.println(name());
    }

}
