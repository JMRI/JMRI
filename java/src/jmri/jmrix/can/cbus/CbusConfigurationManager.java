// ConfigurationManager.java
package jmri.jmrix.can.cbus;

import java.util.ResourceBundle;
import jmri.InstanceManager;
import jmri.ProgrammerManager;
import jmri.ThrottleManager;
import jmri.jmrix.can.CanSystemConnectionMemo;

/**
 * Does configuration for various CAN-based communications implementations.
 * <p>
 * It would be good to replace this with properties-based method for redirecting
 * to classes in particular subpackages.
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 *
 * @author Bob Jacobsen Copyright (C) 2009
 * @version $Revision$
 */
public class CbusConfigurationManager extends jmri.jmrix.can.ConfigurationManager {

    @SuppressWarnings("LeakingThisInConstructor")
    public CbusConfigurationManager(CanSystemConnectionMemo memo) {
        super(memo);
        InstanceManager.store(this, CbusConfigurationManager.class);
        InstanceManager.store(cf = new jmri.jmrix.can.cbus.swing.CbusComponentFactory(adapterMemo),
                jmri.jmrix.swing.ComponentFactory.class);
    }

    jmri.jmrix.swing.ComponentFactory cf = null;

    @Override
    public void configureManagers() {

        InstanceManager.setPowerManager(
                getPowerManager());

        InstanceManager.setSensorManager(
                getSensorManager());

        InstanceManager.setTurnoutManager(
                getTurnoutManager());

        InstanceManager.setThrottleManager(
                getThrottleManager());

        jmri.InstanceManager.setProgrammerManager(
                getProgrammerManager());

        jmri.InstanceManager.setCommandStation(getCommandStation());

        jmri.InstanceManager.setReporterManager(getReporterManager());

        jmri.InstanceManager.setLightManager(getLightManager());

        jmri.jmrix.can.cbus.ActiveFlag.setActive();
    }

    /**
     * Tells which managers this provides by class
     *
     * @param type Class type to check
     * @return true if supported; false if not
     */
    @Override
    public boolean provides(Class<?> type) {
        if (adapterMemo.getDisabled()) {
            return false;
        } else if (type.equals(jmri.ProgrammerManager.class)) {
            return true;
        } else if (type.equals(jmri.ThrottleManager.class)) {
            return true;
        } else if (type.equals(jmri.PowerManager.class)) {
            return true;
        } else if (type.equals(jmri.SensorManager.class)) {
            return true;
        } else if (type.equals(jmri.TurnoutManager.class)) {
            return true;
        } else if (type.equals(jmri.ReporterManager.class)) {
            return true;
        } else if (type.equals(jmri.LightManager.class)) {
            return true;
        } else if (type.equals(jmri.CommandStation.class)) {
            return true;
        }
        return false; // nothing, by default
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Class<?> T) {
        if (adapterMemo.getDisabled()) {
            return null;
        } else if (T.equals(jmri.ProgrammerManager.class)) {
            return (T) getProgrammerManager();
        } else if (T.equals(jmri.ThrottleManager.class)) {
            return (T) getThrottleManager();
        } else if (T.equals(jmri.PowerManager.class)) {
            return (T) getPowerManager();
        } else if (T.equals(jmri.SensorManager.class)) {
            return (T) getSensorManager();
        } else if (T.equals(jmri.TurnoutManager.class)) {
            return (T) getTurnoutManager();
        } else if (T.equals(jmri.ReporterManager.class)) {
            return (T) getReporterManager();
        } else if (T.equals(jmri.LightManager.class)) {
            return (T) getLightManager();
        } else if (T.equals(jmri.CommandStation.class)) {
            return (T) getCommandStation();
        }

        return null; // nothing, by default
    }

    private ProgrammerManager programmerManager;

    public ProgrammerManager getProgrammerManager() {
        if (programmerManager == null) {
            programmerManager = new CbusDccProgrammerManager(
                    new jmri.jmrix.can.cbus.CbusDccProgrammer(adapterMemo.getTrafficController()), adapterMemo);
        }
        return programmerManager;
    }

    public void setProgrammerManager(ProgrammerManager p) {
        programmerManager = p;
    }

    protected CbusPowerManager powerManager;

    public CbusPowerManager getPowerManager() {
        if (adapterMemo.getDisabled()) {
            return null;
        }
        if (powerManager == null) {
            powerManager = new CbusPowerManager(adapterMemo);
        }
        return powerManager;
    }

    protected ThrottleManager throttleManager;

    public ThrottleManager getThrottleManager() {
        if (adapterMemo.getDisabled()) {
            return null;
        }
        if (throttleManager == null) {
            throttleManager = new CbusThrottleManager(adapterMemo);
        }
        return throttleManager;
    }

    public void setThrottleManager(ThrottleManager t) {
        throttleManager = t;
    }

    protected CbusTurnoutManager turnoutManager;

    public CbusTurnoutManager getTurnoutManager() {
        if (adapterMemo.getDisabled()) {
            return null;
        }
        if (turnoutManager == null) {
            turnoutManager = new CbusTurnoutManager(adapterMemo);
        }
        return turnoutManager;
    }

    protected CbusSensorManager sensorManager;

    public CbusSensorManager getSensorManager() {
        if (adapterMemo.getDisabled()) {
            return null;
        }
        if (sensorManager == null) {
            sensorManager = new CbusSensorManager(adapterMemo);
        }
        return sensorManager;
    }

    protected CbusReporterManager reporterManager = null;

    public CbusReporterManager getReporterManager() {
        if (adapterMemo.getDisabled()) {
            return null;
        }
        if (reporterManager == null) {
            reporterManager = new CbusReporterManager(adapterMemo);
        }
        return reporterManager;
    }

    protected CbusLightManager lightManager = null;

    public CbusLightManager getLightManager() {
        if (adapterMemo.getDisabled()) {
            return null;
        }
        if (lightManager == null) {
            lightManager = new CbusLightManager(adapterMemo);
        }
        return lightManager;
    }

    protected CbusCommandStation commandStation;

    public CbusCommandStation getCommandStation() {
        if (adapterMemo.getDisabled()) {
            return null;
        }
        if (commandStation == null) {
            commandStation = new CbusCommandStation(adapterMemo);
        }
        return commandStation;
    }

    @Override
    public void dispose() {
        if (cf != null) {
            InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);
        }
        if (powerManager != null) {
            InstanceManager.deregister(powerManager, jmri.jmrix.can.cbus.CbusPowerManager.class);
        }
        if (turnoutManager != null) {
            InstanceManager.deregister(turnoutManager, jmri.jmrix.can.cbus.CbusTurnoutManager.class);
        }
        if (sensorManager != null) {
            InstanceManager.deregister(sensorManager, jmri.jmrix.can.cbus.CbusSensorManager.class);
        }
        if (reporterManager != null) {
            InstanceManager.deregister(reporterManager, jmri.jmrix.can.cbus.CbusReporterManager.class);
        }
        if (lightManager != null) {
            InstanceManager.deregister(lightManager, jmri.jmrix.can.cbus.CbusLightManager.class);
        }
        if (throttleManager != null) {
            InstanceManager.deregister((CbusThrottleManager) throttleManager, jmri.jmrix.can.cbus.CbusThrottleManager.class);
        }
        InstanceManager.deregister(this, CbusConfigurationManager.class);
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.can.CanActionListBundle");
    }

}

/* @(#)ConfigurationManager.java */
