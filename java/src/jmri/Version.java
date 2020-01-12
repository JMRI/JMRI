package jmri;

import java.util.ResourceBundle;

/**
 * Defines a simple place to get the JMRI version string.
 * <p>
 * JMRI version strings are of the form x.y.z-m:
 * <ul>
 * <li>x, called the "major" number, is a small integer that increases with time
 * <li>y, called the "minor" number, is a small integer that increases with time
 * <li>z, called the "test" or "build" number, is a small integer increasing
 * with time, perhaps followed by a couple of modifier characters. As a special
 * case, this is omitted for Production Releases.
 * <li>m, called the modifier, is a string that further describes the build. A
 * common modifier is "ish" which denotes an unofficial build.
 * </ul>
 * Hence you expect to see JMRI versions called things like "4.7.2", "4.6",
 * "4.7.3ish", "4.7.2-pjc", "4.7.2ish-pjc".
 * <p>
 * The version string shown by a JMRI program or used to label a download comes
 * in two forms, depending on whether it was built by an "official" process or
 * not, which in turn is determined by the "release.official" property:
 * <dl>
 * <dt>Official<dd>
 * <ul>
 * <li>If the revision number e.g. 123abc (git hash) is available in
 * release.revision_id, then "4.1.1+R123abc". Note the "R".
 * <li>Else "4.1.1+(date)", where the date comes from the release.build_date
 * property.
 * </ul>
 * <dt>Unofficial<dd>
 * Unofficial releases are marked by "ish" after the version number, and
 * inclusion of the building user's ID.
 * <ul>
 * <li>If the revision number e.g. 123abc (git hash) is available in
 * release.revision_id, then "4.1.1ish+(user)+(date)+R123abc". Note the "R".
 * <li>Else "4.1.1+(user)+(date)", where the date comes from the
 * release.build_date property.
 * </ul>
 * </dl>
 * The release.revision_id, release.build_user and release.build_date properties
 * are set at build time by Ant.
 * <p>
 * Generally, JMRI updates its version string in the code repository right
 * <b>after</b> a release. Between formal release 1.2.3 and 1.2.4, the string
 * will be 1.2.4ish.
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
 * @author Bob Jacobsen Copyright 1997-2016
 */
public class Version {

    static final private ResourceBundle VERSION_BUNDLE = ResourceBundle.getBundle("jmri.Version"); // NOI18N

    /**
     * Major number changes with large incompatible changes in requirements or
     * API.
     */
    static final public int major = Integer.parseInt(VERSION_BUNDLE.getString("release.major")); // NOI18N

    /**
     * Minor number changes with each production versionBundle. Odd is
     * development, even is production.
     */
    static final public int minor = Integer.parseInt(VERSION_BUNDLE.getString("release.minor")); // NOI18N

    /**
     * Test number changes with individual releases, generally fastest for test
     * releases. In production releases, if non-zero, indicates a bug fix only
     * release.
     */
    static final public int test = Integer.parseInt(VERSION_BUNDLE.getString("release.build")); // NOI18N

    /**
     * The additional MODIFIER for the release. Used to indicate a parallel
     * release of a feature that has not been accepted into main stream
     * development.
     */
    static final public String MODIFIER = VERSION_BUNDLE.getString("release.modifier"); // NOI18N

    /**
     * Descriptor for non-official build. Included in {@link #name()}, but not
     * in {@link #getCanonicalVersion()}.
     */
    static final public String NON_OFFICIAL = "ish"; // NOI18N

    /**
     * The user who built this versionBundle, as determined by the build
     * machine.
     */
    static final public String buildUser = VERSION_BUNDLE.getString("release.build_user"); // NOI18N

    /**
     * The Git revision ID for this versionBundle (if known).
     */
    static final public String revisionId = VERSION_BUNDLE.getString("release.revision_id"); // NOI18N

    /**
     * The date/time of this build.
     */
    static final public String buildDate = VERSION_BUNDLE.getString("release.build_date"); // NOI18N

    /**
     * Has this build been created as a possible "official" versionBundle?
     */
    static final public boolean official = Boolean.parseBoolean(VERSION_BUNDLE.getString("release.official")); // NOI18N

    /**
     * Get the MODIFIER in the 1.2.3-MODIFIER version name. Non-official
     * versions include {@value #NON_OFFICIAL} in the MODIFIER.
     *
     * @return the third term, possibly an empty String if {@link #test} is 0
     */
    public static String getModifier() {
        StringBuilder modifier = new StringBuilder();
        if (!official) {
            modifier.append(NON_OFFICIAL);
        }
        if (!MODIFIER.isEmpty()) {
            modifier.append("-").append(MODIFIER); // NOI18N
        }
        return modifier.toString().replace("--", "-");
    }

    /**
     * Provide the current version string.
     * <p>
     * This string is built using various known build parameters, including the
     * versionBundle.{major,minor,build} values, the MODIFIER, the Git revision
     * ID (if known) and the official property
     *
     * @return The current version string
     */
    static public String name() {
        String version = major + "." + minor;
        if (test != 0) {
            version = version + "." + test;
        }
        String addOn;
        if (official) {
            if ("unknown".equals(revisionId)) {
                addOn = buildDate;
            } else {
                addOn = "R" + revisionId;
            }
        } else { // not official, so a development build that gets a user name
            if ("unknown".equals(revisionId)) {
                addOn = buildUser + "+" + buildDate;
            } else {
                addOn = buildUser + "+" + buildDate + "+R" + revisionId;
            }
        }
        return version + getModifier() + "+" + addOn;
    }

    /**
     * Tests that a string contains a canonical version string.
     * <p>
     * A canonical version string is a string in the form x.y.z[-a[-b[-...]]]
     * where parts x, y, and z are integers and parts a, b, ... are free-form
     * text and is different than the version string displayed using
     * {@link #name()}. The canonical version string for a JMRI instance is
     * available using {@link #getCanonicalVersion()}. The canonical version
     * will not include official indicators or build metadata.
     *
     * @param version version string to check
     * @return true if version is a canonical version string
     */
    static public boolean isCanonicalVersion(String version) {
        String[] parts = version.split("\\+");
        if (parts.length > 1) {
            return false;
        }
        parts = version.split("-");
        String[] versions = parts[0].split("\\.");
        if (versions.length != 3) {
            return false;
        }
        try {
            for (String part : versions) {
                if (Integer.parseInt(part) < 0) {
                    return false;
                }
            }
        } catch (NumberFormatException ex) {
            return false;
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
     * string. This comparison ignores modifiers.
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
        String[] p1 = version1.split("-");
        String[] p2 = version2.split("-");
        String[] v1 = p1[0].split("\\.");
        String[] v2 = p2[0].split("\\.");
        for (int i = 0; i < 3; i++) {
            result = v1[i].compareTo(v2[i]);
            if (result != 0) {
                return result;
            }
        }
        return result;
    }

    /**
     * Return the version as major.minor.test-modifiers. The test value is
     * always present. The {@value #NON_OFFICIAL} modifier is not present in the
     * canonical version.
     *
     * @return the canonical version
     */
    static public String getCanonicalVersion() {
        String version = major + "." + minor + "." + test;
        String modifiers = getModifier().replace(NON_OFFICIAL, ""); // remove "ish"
        if (!modifiers.isEmpty()) {
            version = version + modifiers;
        }
        if (version.endsWith("-")) {
            version = version.substring(0, version.length() - 2);
        }
        return version;
    }

    /**
     * Return the application copyright as a String.
     *
     * @return the copyright
     */
    static public String getCopyright() {
        return Bundle.getMessage("Copyright", VERSION_BUNDLE.getString("jmri.copyright.year"));
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
