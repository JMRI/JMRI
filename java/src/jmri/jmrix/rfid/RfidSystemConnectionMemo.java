package jmri.jmrix.rfid;

import java.util.Comparator;
import java.util.ResourceBundle;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.ReporterManager;
import jmri.SensorManager;
import jmri.jmrix.DefaultSystemConnectionMemo;
import jmri.jmrix.rfid.swing.RfidComponentFactory;
import jmri.jmrix.swing.ComponentFactory;
import jmri.util.NamedBeanComparator;

/**
 * Lightweight class to denote that a system is active, and provide general
 * information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
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
 * @author Bob Jacobsen Copyright (C) 2010
 * @author Matthew Harris Copyright (C) 2011
 */
public class RfidSystemConnectionMemo extends DefaultSystemConnectionMemo {

    private RfidTrafficController rt;
    private RfidProtocol protocol;

    public RfidSystemConnectionMemo(RfidTrafficController rt) {
        this();
        setRfidTrafficController(rt);
    }

    public RfidSystemConnectionMemo() {
        super("F", "Rfid");
        InstanceManager.store(this, RfidSystemConnectionMemo.class);

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

    public void configureManagers(RfidSensorManager sensorManager, RfidReporterManager reporterManager) {
        store(sensorManager, SensorManager.class);
        store(reporterManager, ReporterManager.class);
        InstanceManager.setSensorManager(sensorManager);
        InstanceManager.setReporterManager(reporterManager);
        register();
    }

    public RfidProtocol getProtocol() {
        return protocol;
    }

    public final void setProtocol(RfidProtocol protocol) {
        this.protocol = protocol;
    }

    public RfidSensorManager getSensorManager() {
        return get(SensorManager.class);
    }

    public RfidReporterManager getReporterManager() {
        return get(ReporterManager.class);
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.rfid.RfidActionListBundle");
    }

    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanComparator<>();
    }

    @Override
    public void dispose() {
        rt = null;
        InstanceManager.deregister(this, RfidSystemConnectionMemo.class);
        protocol = null;
        super.dispose();
    }

}
