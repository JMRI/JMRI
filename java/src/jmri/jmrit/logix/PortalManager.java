package jmri.jmrit.logix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.managers.AbstractManager;

/**
 * Basic Implementation of a PortalManager.
 * <P>
 * Note that this does not enforce any particular system naming convention.
 * <P>
 * Note this is an 'after thought' manager.  Portals have been in use since 2009.  Their use has now
 * expanded well beyond what was expected.  A Portal factory is needed for development to continue.
 * 
 * Portal system names will be numbers and they will not be shown to users.  The UI will treat Portal names
 * as it does now as user names.
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
 *
 * @author      Pete Cressman Copyright (C) 2014
 * @version     $Revision: 22821 $
 */
public class PortalManager extends AbstractManager
    implements java.beans.PropertyChangeListener, jmri.InstanceManagerAutoDefault {
	
	private static int _nextSName = 1;

    public PortalManager() {
        super();
    }
/*    
    public void setNextSysNum(int next) {
    	_nextSName = next;
    }
    public int getNextSysNum() {
    	return _nextSName;
    }
*/    
    public int getXMLOrder(){
        return jmri.Manager.OBLOCKS;
    }

    public String getSystemPrefix() { return "I"; }
    public char typeLetter() { return 'P'; }
    
    /**
     * Method to create a new Portal if it does not exist
     * Returns null if a Portal with the same systemName or userName
     * already exists, or if there is trouble creating a new Portal.
     * Generate a systemName if called with sName==null
     */
    public Portal createNewPortal(String sName, String userName) {
        // Check that Portal does not already exist
        Portal portal;
        if (userName!= null && userName.trim().length()>0) {
        	portal = getByUserName(userName);
            if (portal!=null) return null;
        } else {  // must have a user name for backward compatibility
        	return null;
        }
        if (sName==null) {
        	sName = generateSystemName();
        }
        if (!sName.startsWith("IP")) {
        	sName = "IP"+sName;
        }
        if (sName.length() < 3) {
            return null;
        }
        portal = getBySystemName(sName);
        if (portal!=null) return null;
        // Portal does not exist, create a new Portal
        portal = new Portal(sName,userName);
        // save in the maps
        register(portal);
        return portal;
    }
    
    public String generateSystemName() {
    	String name;
    	do {
        	name = "IP"+Integer.toString(_nextSName++);    		
    	} while (getBySystemName(name)!=null);
    	return name;    		
    }

    /** 
     * Method to get an existing Portal.  First looks up assuming that
     *      name is a User Name.  If this fails looks up assuming
     *      that name is a System Name.  If both fail, returns null.
     */
    public Portal getPortal(String name) {
        Portal portal = getByUserName(name);
        if (portal!=null) return portal;
        return getBySystemName(name);
    }

    public Portal getBySystemName(String name) {
        if (name==null || name.trim().length()==0) { return null; }
        return (Portal)_tsys.get(name);
    }

    public Portal getByUserName(String key) {
        if (key==null || key.trim().length()==0) { return null; }
        return (Portal)_tuser.get(key);
    }

    public Portal providePortal(String name) {
        if (name==null || name.trim().length()==0) { return null; }
        Portal portal = getByUserName(name);
        if (portal==null) {
        	portal = createNewPortal(null, name);
        }
        return portal;
    }
    
    protected void registerSelf() {
    	// Override, don't register, OBlockManager does store and load of Portals
    }
    
    static PortalManager _instance = null;
    static public PortalManager instance() {
        if (_instance == null) {
            _instance = new PortalManager();
        }
        return (_instance);
    }

    static Logger log = LoggerFactory.getLogger(PortalManager.class.getName());
}

/* @(#)PortalManager.java */
