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
     * Standalone print of version string and exit.
     */
    static public void main(String[] args) {
        System.out.println(name());
    }
}
