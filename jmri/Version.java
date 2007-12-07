package jmri;

/**
 * Defines a simple place to get the JMRI version string.
 *<P>
 * These JavaDocs are for Version 1.9.2 of JMRI.
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
 * @author  Bob Jacobsen   Copyright 2000, 2001, 2002, 2003, 2004, 2005, 2006, 2007
 * @version $Revision: 1.73 $
 */

public class Version {
    /**
     * Provide the current version string in I.J.K format.
     * <P>
     * This is manually maintained by updating it before each
     * release is built.
     *
     * @return The current version string
     */
    static public String name() { return "1.9.2"; }
}
