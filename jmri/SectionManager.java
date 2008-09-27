// SectionManager.java

package jmri;

import java.util.List;

/**
 * Basic Implementation of a SectionManager.
 * <P>
 * This doesn't have a "new" interface, since Sections are 
 * independently implemented, instead of being system-specific.
 * <P>
 * Note that Section system names must begin with IY, and be followed by a 
 * string, usually, but not always, a number. All alphabetic characters
 * in a Section system name must be upper case. This is enforced when a Section
 * is created.
 * <P>
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
 * @author      Dave Duchamp Copyright (C) 2008
 * @version	$Revision: 1.1 $
 */
public class SectionManager extends AbstractManager
    implements java.beans.PropertyChangeListener {

    public SectionManager() {
        super();
    }

    public char systemLetter() { return 'I'; }
    public char typeLetter() { return 'Y'; }
    
    /**
     * Method to create a new Section if the Section does not exist
     *   Returns null if a Section with the same systemName or userName
     *       already exists, or if there is trouble creating a new Section.
     */
    public Section createNewSection(String systemName, String userName) {
		// check system name
		if ( (systemName==null) || (systemName.length()<1) ) {
			// no valid system name entered, return without creating
			return null;
		}
		String sysName = systemName;
		if ( (sysName.length()<2) || (!sysName.substring(0,2).equals("IY")) ) {
			sysName = "IY"+sysName;
		}
        // Check that Section does not already exist
        Section y;
        if (userName!= null && !userName.equals("")) {
            y = getByUserName(userName);
            if (y!=null) return null;
        }
		String sName = sysName.toUpperCase().trim();
        y = getBySystemName(sysName);
		if (y==null) y = getBySystemName(sName);
        if (y!=null) return null;
        // Section does not exist, create a new Section
        y = new Section(sName,userName);
        if (y!=null) {
            // save in the maps
            register(y);
        }
        return y;
    }

    /**
     * Remove an existing Section 	 
	 */
    public void deleteSection(Section y) {
		// delete the Section				
        deregister(y);
		y.dispose();
    }

    /** 
     * Method to get an existing Section.  First looks up assuming that
     *      name is a User Name.  If this fails looks up assuming
     *      that name is a System Name.  If both fail, returns null.
     */
    public Section getSection(String name) {
        Section y = getByUserName(name);
        if (y!=null) return y;
        return getBySystemName(name);
    }

    public Section getBySystemName(String name) {
		String key = name.toUpperCase();
        return (Section)_tsys.get(key);
    }

    public Section getByUserName(String key) {
        return (Section)_tuser.get(key);
    }
	
    static SectionManager _instance = null;
    static public SectionManager instance() {
        if (_instance == null) {
            _instance = new SectionManager();
        }
        return (_instance);
    }
	

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SectionManager.class.getName());
}

/* @(#)SectionManager.java */
