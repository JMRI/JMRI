package jmri.jmrit.logix;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.beans.PropertyChangeListener;

import jmri.Path;
import jmri.Sensor;
import jmri.SignalHead;


/**
 * Extends jmri.Block to be used in Logix Conditionals.  Adds an additional state,
 * ALLOCATED that can be set from ConditionalAction. 
 *
 *<P>
 * Restricts Path objects to be contained within the Block boundaries.
 *
 *<P>
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
 * @author	Pete Cressman (C) 2009
 */
public class OBlock extends jmri.Block {

    ArrayList <Portal> _portals = new ArrayList <Portal>();     // portals to this block

    static final public int ALLOCATED = 0x10;   // reserve the block for subsequent use by a train
    static final public int SHORTED = 0x08;     // duplicate of INCONSISTENT = 0x08
    static final public int DARK = 0x20;        // Block has no Sensor
    
    public OBlock(String systemName) {
        super(systemName);
        if (getSensor()==null) {
            setState(DARK);
        }
    }

    public OBlock(String systemName, String userName) {
        super(systemName, userName);
        if (getSensor()==null) {
            setState(DARK);
        }
    }

    /**
    *  Test that block is not occupied and and not allocated 
    */
    public boolean isFree() {
        int state = getState();
        return ((state & ALLOCATED)==0 && (state & OCCUPIED)==0);
    }

    /**
    * Allocate (reserves) the block for the Warrant that is the 'value' object
    * Note the block may be OCCUPIED by a non-warranted train.
    * @return false if block is already allocated to another warrant
    */
    public String allocate(Object value) {
        if (value==null) {
            return "Error";
        } else if (getValue()!=null && !value.equals(getValue())) {
            // allocated to another warrant
            if (getValue() instanceof jmri.implementation.AbstractNamedBean) {
                return ((jmri.implementation.AbstractNamedBean)getValue()).getDisplayName();
            } else {
                return getValue().getClass().getName();
            }
        }
        setState(getState() | ALLOCATED);
        setValue(value);
        // firePropertyChange signaled in super.setState()
        return null;
    }

    /**
    * Remove allocation state
    * Remove listener regardless of ownership
    */
    public void deAllocate(Object value) {
        if (value!=null) {
            Sensor sensor = getSensor();
            if (sensor != null ) {
                sensor.removePropertyChangeListener((PropertyChangeListener)value);
            }
            if (value.equals(getValue())) {  // allocated to caller, so deallocate
                setValue(null);
                if (sensor != null)  {
                    setState(sensor.getState());  // unset allocated bit
                }
            }
        }
    }

    /**
    * Enforce unique portal names.  Portals are not managed beans and multiple
    * config loads will create multiple Portal Objects that are "duplicates".
    */
    public void addPortal(Portal portal) {
        String name = getDisplayName();
        if (!name.equals(portal.getFromBlockName()) && !name.equals(portal.getToBlockName())) {
            log.warn(portal.toString()+" not in block "+getDisplayName());
            return;
        }
        String pName = portal.getName();
        for (int i=0; i<_portals.size(); i++) {
            if (pName.equals(_portals.get(i).getName())) {
                return;
            }
        }
        _portals.add(portal);
        if (log.isDebugEnabled()) log.debug("addPortal: portal= \""+portal.getName()+
                                            "\", to Block= \""+getDisplayName()+"\"." ); 
    }

    /**
    * Remove portal from block and stub all paths using this portal to
    * be dead end spurs.
    */
    public void removePortal(Portal portal) {
        int oldSize = _portals.size();
        ArrayList <Path> list = (ArrayList <Path>)getPaths();
        int oldPathSize = list.size();
        if (portal != null){
            String name = portal.getName();
            Iterator <Path> iter = getPaths().iterator();
            while (iter.hasNext()) {
                OPath path = (OPath)iter.next();
                if (name.equals(path.getFromPortalName())) {
                    path.setFromPortalName(null);
                    if (log.isDebugEnabled()) log.debug("removed Portal "+name+" from Path "+
                              path.getName()+" in block "+getSystemName());
                }
                if (name.equals(path.getToPortalName())) {
                    path.setToPortalName(null);
                    if (log.isDebugEnabled()) log.debug("removed Portal "+name+" from Path "+
                              path.getName()+" in block "+getSystemName());
                }
                if (path.getFromPortalName()==null && path.getToPortalName()==null) {
                    removePath(path);
                    if (log.isDebugEnabled()) log.debug("removed Path "+
                                              path.getName()+" in block "+getSystemName());
                }
            }
            //_portals.remove(portal);
            for (int i=0; i < _portals.size(); i++) {
                if (name.equals(_portals.get(i).getName())) {
                    _portals.remove(i);
                    log.debug("removed portal "+name+" from block "+getSystemName());
                    i--;
                }
            }
        }
        log.debug("removePortal: block "+getSystemName()+" portals decreased from "
                  +oldSize+" to "+_portals.size()+" - paths decreased from "+
                  oldPathSize+" to "+getPaths().size());
        //firePropertyChange("portalCount", new Integer(oldSize), new Integer(_portals.size()));
    }

    public Portal getPortalByName(String name) {
        for (int i=0; i<_portals.size(); i++)  {
            if (_portals.get(i).getName().equals(name)) {
                if (log.isDebugEnabled()) log.debug("getPortalByName: portal= \""+
                                                    _portals.get(i).getName()+"\"." ); 
                return _portals.get(i);
            }
        }
        return null;
    }

    public List <Portal> getPortals() {
        return _portals;
    }

    public OPath getPathByName(String name) {
        Iterator <Path> iter = getPaths().iterator();
        while (iter.hasNext()) {
            OPath path = (OPath)iter.next();
            if (path.getName().equals(name)) {
                return path;
            }
        }
        return null;
    }

    /**
    * Enforce unique path names within block
    */
    public void addPath(OPath path) {
        String pName = path.getName();
        List <Path> list = getPaths();
        for (int i=0; i<list.size(); i++) {
            if (pName.equals(((OPath)list.get(i)).getName())) {
                return;
            }
        }
        path.setBlock(this);
        Portal portal = getPortalByName(path.getFromPortalName());
        if (portal!=null) {
            portal.addPath(path);
        }
        portal = getPortalByName(path.getToPortalName());
        if (portal!=null) {
            portal.addPath(path);
        }
        int oldSize = list.size();
        super.addPath(path);
        firePropertyChange("pathCount", new Integer(oldSize), new Integer(getPaths().size()));
    }

    public void removePath(Path path) {
        if (!getSystemName().equals(path.getBlock().getSystemName())) {
            return;
        }
        path.clearSettings();
        int oldSize = getPaths().size();
        super.removePath(path);
        firePropertyChange("pathCount", new Integer(oldSize), new Integer(getPaths().size()));
    }

    /**
    * Set Turnouts for the path
    *
    */
    public void setPath(String pathName, int delay) {
        OPath path = getPathByName(pathName);
        if (path==null) {
            log.error("Path \""+pathName+"\" not found in Block \""+getDisplayName()+"\".");
        } else {
            path.setTurnouts(delay);
        }
    }

    /**
    * set (incoming) outward facing signals RED, (outgoing) inward facing signals YELLOW
    * Blocks all incoming paths prior to setting a specific clear or approch for a given
    * path.
    */
    public void setSignalProtection() {
        for (int i=0; i<_portals.size(); i++)  {
            Portal portal = _portals.get(i);
            portal.setOpposingSignal(this, SignalHead.RED);
            portal.setSignal(this, SignalHead.YELLOW);
        }
    }

    public void releaseSignalProtection() {
        for (int i=0; i<_portals.size(); i++)  {
            Portal portal = _portals.get(i);
            portal.setOpposingSignal(this, SignalHead.YELLOW);
            portal.setSignal(this, SignalHead.YELLOW);
        }
    }

    /**
     * (Override)  Handles Block sensor going INACTIVE: this block is empty.
     * Called by handleSensorChange
     */
    public void goingInactive() {
        if (!(getValue() instanceof Warrant)) {
            // this block is not under warrant, comply with old Block code.
            super.goingInactive();
            return;
        }
        //int state = ((Warrant)getValue()).goingInactive(this);
        if (log.isDebugEnabled()) log.debug("Allocated OBlock "+getSystemName()+" goes UNOCCUPIED");
        setState(getState() & ~OCCUPIED);
    }

     /**
     * (Override) Handles Block sensor going ACTIVE: this block is now occupied, 
     * figure out from who and copy their value. Called by handleSensorChange
     */
	public void goingActive() {
        if (!(getValue() instanceof Warrant)) {
            // this block is not under warrant, comply with old Block code.
            super.goingActive();
            return;
        }
        //((Warrant)getValue()).goingActive(this);
        int state = getState();
        super.setState(OCCUPIED | state);
        if (log.isDebugEnabled()) {
            log.debug("OBlock \""+getUserName()+"\" ("+getSystemName()+
                         ") is going OCCUPIED from current state= "+state);
        }
    }
   
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(OBlock.class.getName());
}
