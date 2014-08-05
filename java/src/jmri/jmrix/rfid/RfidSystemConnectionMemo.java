// RfidSystemConnectionMemo.java

package jmri.jmrix.rfid;

import jmri.InstanceManager;
import jmri.jmrix.rfid.swing.RfidComponentFactory;
import jmri.jmrix.swing.ComponentFactory;
import java.util.ResourceBundle;

/**
 * Lightweight class to denote that a system is active,
 * and provide general information.
 * <p>
 * Objects of specific subtypes are registered
 * in the instance manager to activate their
 * particular system.
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
 * @author      Bob Jacobsen    Copyright (C) 2010
 * @author      Matthew Harris  Copyright (C) 2011
 * @version     $Revision$
 */
public class RfidSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    private RfidTrafficController rt;
    private RfidSensorManager sensorManager;
    private RfidReporterManager reporterManager;
    private RfidProtocol protocol;

    public RfidSystemConnectionMemo(RfidTrafficController rt) {
        this();
        setRfidTrafficController(rt);
    }

    public RfidSystemConnectionMemo() {
        super("F", "Rfid");
        register(); // registers general type
        InstanceManager.store(this, RfidSystemConnectionMemo.class); // also register as specific type

        // Create and register the ComponentFactory
        InstanceManager.store(new RfidComponentFactory(this),
                ComponentFactory.class);
    }

    public RfidTrafficController getTrafficController() {
        return rt;
    }

    public final void setRfidTrafficController(RfidTrafficController rt) {
        this.rt = rt;
        rt.setAdapterMemo(this);
    }

    public void configureManagers() {
//        sensorManager = new RfidSensorManager(getTrafficController(), getSystemPrefix());
//        reporterManager = new RfidReporterManager(getTrafficController(), getSystemPrefix());
    }

    public RfidProtocol getProtocol() {
        return protocol;
    }

    public final void setProtocol(RfidProtocol protocol) {
        this.protocol = protocol;
    }

    public RfidSensorManager getSensorManager() {
        return sensorManager;
    }

    public RfidReporterManager getReporterManager() {
        return reporterManager;
    }

    /**
     * Tells which managers this provides by class
     * @param type manager type to check
     * @return true if provided
     */
    @Override
    public boolean provides(Class<?> type) {
        if (type.equals(jmri.SensorManager.class))
            return true;
        if (type.equals((jmri.ReporterManager.class)))
            return true;
        // Delegate to super class
        return super.provides(type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Class<?> T) {
        if (getDisabled())
            return null;
        if (T.equals(jmri.SensorManager.class))
            return (T) getSensorManager();
        if (T.equals(jmri.ReporterManager.class))
            return (T) getReporterManager();
        // nothing, by default
        return null;
    }
    
    @Override
    protected ResourceBundle getActionModelResourceBundle(){
        return ResourceBundle.getBundle("jmri.jmrix.rfid.RfidActionListBundle");
    }

    @Override
    public void dispose() {
        rt = null;
        InstanceManager.deregister(this, RfidSystemConnectionMemo.class);
        if (reporterManager != null)
            InstanceManager.deregister(reporterManager, RfidReporterManager.class);
        if (sensorManager != null)
            InstanceManager.deregister(sensorManager, RfidSensorManager.class);
        protocol = null;
        super.dispose();
    }

}

/* @(#)RfidSystemConnectionMemo.java */