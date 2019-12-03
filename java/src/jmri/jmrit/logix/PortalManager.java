package jmri.jmrit.logix;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Hashtable;

import javax.annotation.OverridingMethodsMustInvokeSuper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic implementation of a PortalManager.
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
public class PortalManager implements jmri.InstanceManagerAutoDefault, PropertyChangeListener {
    
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private ArrayList<Portal> _nameList = new ArrayList<>();   // stores Portal in loaded order
    private Hashtable<String, Portal> _portalMap = new Hashtable<>();   // stores portal by current name
    private Integer _nextIndex = Integer.valueOf(1);

    public PortalManager() {
    }

    public int getPortalCount() {
        return _nameList.size();
    }

    public Portal getPortal(int idx) {
        return _nameList.get(idx);
    }

    public int getIndexOf(Portal portal) {
        return _nameList.indexOf(portal);
    }

    public Portal getPortal(String name) {
        return _portalMap.get(name);
    }

    public Collection<Portal> getPortalSet() {
        return Collections.unmodifiableCollection(_nameList);
    }

    public Portal createNewPortal(String userName) {
        // Check that Portal does not already exist
        Portal portal;
        if (userName != null && userName.trim().length() > 0) {
            portal = _portalMap.get(userName);
            if (portal != null) {
                return null;
            }
        } else {  // must have a user name for backward compatibility
            return null;
        }
        // Portal does not exist, create a new Portal
        portal = new Portal(userName);
        // save in the maps
        _nameList.add(portal);
        _portalMap.put(userName, portal);
        _nextIndex = Integer.valueOf(_nextIndex.intValue()+1);
        pcs.firePropertyChange("numPortals", null, _nameList.size());
        // listen for name and state changes to forward
        portal.addPropertyChangeListener(this);
        return portal;
    }

    public Portal providePortal(String name) {
        if (name == null || name.trim().length() == 0) {
            return null;
        }
        Portal portal = getPortal(name);
        if (portal == null) {
            portal = createNewPortal(name);
        }
        return portal;
    }

    private synchronized void deletePortal(Portal portal) {
        String name = portal.getName();
        _nameList.remove(portal);
        _portalMap.remove(name);
        pcs.firePropertyChange("numPortals", portal, _nameList.size());
    }

    @OverridingMethodsMustInvokeSuper
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    @OverridingMethodsMustInvokeSuper
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    public void propertyChange(PropertyChangeEvent e) {
        if (!(e.getSource() instanceof Portal)) {
            return;
        }
        Portal portal = (Portal)e.getSource();
        String propertyName = e.getPropertyName();
        log.debug("property = {}", propertyName);
        if (propertyName.equals("portalDelete")) {
            deletePortal(portal);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(PortalManager.class);
}


