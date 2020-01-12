package jmri.jmrix.rfid.merg.concentrator;

import jmri.InstanceManager;
import jmri.jmrix.rfid.RfidReporterManager;
import jmri.jmrix.rfid.RfidSensorManager;
import jmri.jmrix.rfid.RfidSystemConnectionMemo;

/**
 * Lightweight class to denote that a system is active, and provide general
 * information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
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
 * @author Matthew Harris Copyright (C) 2011
 * @since 2.11.4
 */
public class ConcentratorSystemConnectionMemo extends RfidSystemConnectionMemo {

    public ConcentratorSystemConnectionMemo() {
        super();
    }

    /**
     * Configure the common managers for Rfid connections. This puts the common
     * manager config in one place.
     */
    @Override
    public void configureManagers(RfidSensorManager sensorManager, RfidReporterManager reporterManager) {
        this.sensorManager = new ConcentratorSensorManager(this);
        InstanceManager.setSensorManager(getSensorManager());
        this.reporterManager = new ConcentratorReporterManager(this);
        InstanceManager.setReporterManager(getReporterManager());
    }

    @Override
    public void dispose() {
        InstanceManager.deregister(this, ConcentratorSystemConnectionMemo.class);
        super.dispose();
    }

}
