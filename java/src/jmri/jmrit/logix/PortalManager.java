package jmri.jmrit.logix;

import jmri.InstanceManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.managers.AbstractManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic Implementation of a PortalManager.
 * <p>
 * Note that this does not enforce any particular system naming convention.
 * <p>
 * Note this is an 'after thought' manager. Portals have been in use since 2009.
 * Their use has now expanded well beyond what was expected. A Portal factory is
 * needed for development to continue.
 *
 * Portal system names will be numbers and they will not be shown to users. The
 * UI will treat Portal names as it does now as user names.
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
 * @author Pete Cressman Copyright (C) 2014
 */
public class PortalManager extends AbstractManager<Portal>
        implements jmri.InstanceManagerAutoDefault {

    private int _nextSName = 1;

    public PortalManager() {
        super(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
    }

    @Override
    public int getXMLOrder() {
        return jmri.Manager.OBLOCKS;
    }

    @Override
    public char typeLetter() {
        return 'P';
    }

    /*
     * Method to create a new Portal. Returns null if a
     * Portal with the same systemName or userName already exists. 
     *
     * Generate a systemName if called with sName == null and 
     * non null userName.
     */
    public Portal createNewPortal(String sName, String userName) {
        // Check that Portal does not already exist
        Portal portal;
        if (userName != null && userName.trim().length() > 0) {
            portal = getByUserName(userName);
            if (portal != null) {
                return null;
            }
        } else {  // must have a user name for backward compatibility
            return null;
        }
        if (sName == null) {
            sName = generateSystemName();
        } else {
            if (log.isDebugEnabled()) log.debug("createNewPortal called with system name \"{}\"", sName);
        }
        if (!sName.startsWith("IP")) {
            sName = "IP" + sName;
        }
        if (sName.length() < 3) {
            return null;
        }
        portal = getBySystemName(sName);
        if (portal != null) {
            return null;
        }
        // Portal does not exist, create a new Portal
        portal = new Portal(sName, userName);
        // save in the maps
        register(portal);
        return portal;
    }

    public String generateSystemName() {
        String name;
        do {
            name = "IP" + Integer.toString(_nextSName++);
        } while (getBySystemName(name) != null);
        if (log.isDebugEnabled()) log.debug("generateSystemName \"{}\"", name);
       return name;
    }

    /**
     * Method to get an existing Portal. First looks up assuming that name is a
     * User Name. If this fails looks up assuming that name is a System Name. If
     * both fail, returns null.
     * @param name  either System name or user name
     * @return Portal, if found
     */
    public Portal getPortal(String name) {
        if (name == null) {
            return null;
        }
        Portal portal = getByUserName(name);
        if (portal != null) {
            if (log.isDebugEnabled()) log.debug("getPortal with User Name \"{}\"", name);
            return portal;
        }
        if (name.length() > 2 && name.startsWith("IP")) {
            portal = getBySystemName(name);
            if (portal != null) {
                if (log.isDebugEnabled()) log.debug("getPortal with System Name \"{}\"", name);
                return portal;
            }
        }
        return null;
    }

    public Portal getBySystemName(String name) {
        if (name == null || name.trim().length() == 0) {
            return null;
        }
        return _tsys.get(name);
    }

    public Portal getByUserName(String key) {
        if (key == null || key.trim().length() == 0) {
            return null;
        }
        return _tuser.get(key);
    }

    public Portal providePortal(String name) {
        if (name == null || name.trim().length() == 0) {
            return null;
        }
        Portal portal = getPortal(name);
        if (portal == null) {
            portal = createNewPortal(null, name);
        }
        return portal;
    }

    @Override
    protected void registerSelf() {
        // Override, don't register, OBlockManager does store and load of Portals
    }

    @Override
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNamePortals" : "BeanNamePortal");
    }

    private final static Logger log = LoggerFactory.getLogger(PortalManager.class);
}


