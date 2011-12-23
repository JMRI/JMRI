package jmri;


/**
 * Defines a simple place to get the JMRI version string.
 *<P>
 * These JavaDocs are for Version 2.11.9 of JMRI.
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
 * @version $Revision: 17977 $
 */

public class Version {

    /**
     * Major number changes with large incompatible
     * changes in requirements or API.
     */
    static final public int major = @@release.major@@;
     
    /**
     * Minor number changes with each production release.
     * Odd is development, even is production.
     */
    static final public int minor = @@release.minor@@;
     
    /* Test number changes with individual releases,
     * general fastest for test releases. Set 0 for production
     */
    static final public int test = @@release.build@@;

    /* The user who built this release */
    static final public String buildUser = "@@release.build_user@@";

    /* The SVN revision ID for this release (if known) */
    static final public String revisionId = "@@release.revision_id@@";

    /* The date/time of this build */
    static final public String buildDate = "@@release.build_date@@";

    /* Has this build been created as a possible "official" release? */
    static final public boolean official = @@release.official@@;

    /* Has this build been created from a branch in Subversion? */
    static final public boolean branched = @@release.is_branched@@;

    public static String getModifier() {
	StringBuilder modifier = new StringBuilder("");

	if (test != 0) {
	    modifier.append("." + test);
	}
	
	if (branched && !official) {
	    modifier.append("ish");
        }

	return modifier.toString();
    }


    /**
     * Provide the current version string in I.J.Kmod format.
     * <P>
     * This is manually maintained by updating it before each
     * release is built.
     *
     * @return The current version string
     */
    static public String name() { 
        String releaseName;
	if (branched) {
	    String addOn;
	    if ("unknown".equals(revisionId)) {
		addOn = buildDate + "-" + buildUser;
	    }
	    else {
		addOn = "r" + revisionId;
	    }
	    releaseName = major + "." + minor + getModifier() + "-" + addOn;
	}
	else if (revisionId.equals("unknown")) {
	    releaseName = buildDate + "-" + buildUser;
	}
	else {
	    releaseName = "r" + revisionId;
	}
        return releaseName;
    }
     
    /**
     * Tests that a string contains a canonical version string.
     * <p>
     * A canonical version string is a string in the form x.y.z and is different
     * than the version string displayed using {@link #name() }. The canonical
     * version string for a JMRI instance is available using {@link #getCanonicalVersion() }.
     * 
     * @param version
     * @return true if version is a canonical version string
     */
    static public boolean isCanonicalVersion(String version) {
        String[] parts = version.split(".");
        if (parts.length != 3) {
            return false;
        }
        for (String part : parts) {
            if (Integer.getInteger(part) == null || Integer.getInteger(part) < 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Compares a canonical version string to the JMRI canonical version and returns
     * an integer indicating if the string is less than, equal to, or greater than the
     * JMRI canonical version.
     * 
     * @param version
     * @return -1, 0, or 1 if version is less than, equal to, or greater than JMRI canonical version
     * @throws IllegalArgumentException if version is not a canonical version string
     * @see java.lang.Comparable#compareTo(java.lang.Object) 
     */
    static public int compareCanonicalVersions(String version) throws IllegalArgumentException {
        return compareCanonicalVersions(version, getCanonicalVersion());
    }

    /**
     * Compares two canonical version strings and returns an integer indicating if
     * the first string is less than, equal to, or greater than the second string.
     * 
     * @param version1 a canonical version string
     * @param version2 a canonical version string
     * @return -1, 0, or 1 if version1 is less than, equal to, or greater than version2 
     * @throws IllegalArgumentException if either version string is not a canonical version string
     * @see java.lang.Comparable#compareTo(java.lang.Object) 
     */
    static public int compareCanonicalVersions(String version1, String version2) throws IllegalArgumentException {
        int result = 0;
        if (!isCanonicalVersion(version1)) {
            throw new IllegalArgumentException("Parameter version1 is not a canonical version string.");
        }
        if (!isCanonicalVersion(version2)) {
            throw new IllegalArgumentException("Parameter version2 is not a canonical version string.");
        }
        String[] p1 = version1.split(".");
        String[] p2 = version2.split(".");
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
     * Standalone print of version string and exit.
     */
    static public void main(String[] args) {
        System.out.println(name());
    }
}
