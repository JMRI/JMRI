package jmri.jmrix.can.cbus;

import java.util.ResourceBundle;
import jmri.CabSignalManager;
import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;
import jmri.jmrix.can.cbus.eventtable.CbusEventTableDataModel;
import jmri.ThrottleManager;
import jmri.jmrix.can.CanSystemConnectionMemo;

/**
 * Does configuration for MERG CBUS CAN-based communications implementations.
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
 * @author Bob Jacobsen Copyright (C) 2009
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
        
        InstanceManager.store(getCbusPreferences(), CbusPreferences.class);

        InstanceManager.store(getPowerManager(), jmri.PowerManager.class);

        InstanceManager.setSensorManager(
                getSensorManager());

        InstanceManager.setTurnoutManager(
                getTurnoutManager());

        InstanceManager.setThrottleManager(
                getThrottleManager());

        if (getProgrammerManager().isAddressedModePossible()) {
            InstanceManager.store(getProgrammerManager(), jmri.AddressedProgrammerManager.class);
        }
        if (getProgrammerManager().isGlobalProgrammerAvailable()) {
            InstanceManager.store(getProgrammerManager(), GlobalProgrammerManager.class);
        }
        
        InstanceManager.store(getCbusNodeTableDataModel(), CbusNodeTableDataModel.class);

        InstanceManager.store(getCommandStation(), jmri.CommandStation.class);

        InstanceManager.setReporterManager(getReporterManager());

        InstanceManager.setLightManager(getLightManager());
        
        InstanceManager.setDefault(CabSignalManager.class,getCabSignalManager());
        
        InstanceManager.setDefault(CbusEventTableDataModel.class,getCbusEventTableDataModel());
        
    }

    /**
     * Tells which managers this class provides.
     *
     * @param type Class type to check
     * @return true if supported; false if not
     */
    @Override
    public boolean provides(Class<?> type) {
        if (adapterMemo.getDisabled()) {
            return false;
        } else if (type.equals(jmri.AddressedProgrammerManager.class)) {
            return getProgrammerManager().isAddressedModePossible();
        } else if (type.equals(jmri.GlobalProgrammerManager.class)) {
            return getProgrammerManager().isGlobalProgrammerAvailable();
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
        } else if (type.equals(jmri.MultiMeter.class)) {
            return true;
        } else if (type.equals(CbusPreferences.class)) {
            return true;
        } else if (type.equals(CbusNodeTableDataModel.class)) {
            return true;
        } else if (type.equals(CabSignalManager.class)) {
            return true;
        } else if (type.equals(CbusEventTableDataModel.class)) {
            return true;
        }
        
        return false; // nothing, by default
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Class<?> T) {
        if (adapterMemo.getDisabled()) {
            return null;
        } else if (T.equals(jmri.AddressedProgrammerManager.class)
                && getProgrammerManager().isAddressedModePossible()) {
            return (T) getProgrammerManager();
        } else if (T.equals(jmri.GlobalProgrammerManager.class)
                && getProgrammerManager().isGlobalProgrammerAvailable()) {
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
        } else if (T.equals(jmri.MultiMeter.class)) {
            return (T) getMultiMeter();
        } else if (T.equals(CbusPreferences.class)) {
            return (T) getCbusPreferences();
        } else if (T.equals(CbusNodeTableDataModel.class)) {
            return (T) getCbusNodeTableDataModel();
        } else if (T.equals(CabSignalManager.class)) {
            return (T) getCabSignalManager();
        } else if (T.equals(CbusEventTableDataModel.class)) {
            return (T) getCbusEventTableDataModel();
        }
        return null; // nothing, by default
    }

    private CbusDccProgrammerManager programmerManager;

    public CbusDccProgrammerManager getProgrammerManager() {
        if (programmerManager == null) {
            programmerManager = new CbusDccProgrammerManager(
                    new CbusDccProgrammer(adapterMemo.getTrafficController()), adapterMemo);
        }
        return programmerManager;
    }

    public void setProgrammerManager(CbusDccProgrammerManager p) {
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
    
    protected CbusMultiMeter multiMeter;
    
     public void enableMultiMeter(){
        jmri.InstanceManager.store( getMultiMeter(), jmri.MultiMeter.class );
    }
    
    // created on demand
    public CbusMultiMeter getMultiMeter() {
        if (adapterMemo.getDisabled()) {
            return null;
        }
        if (multiMeter == null) {
            multiMeter = new CbusMultiMeter(adapterMemo);
        }
        jmri.InstanceManager.store( multiMeter, jmri.MultiMeter.class );
        return multiMeter;
    }
    

    private CbusPreferences cbusPreferences = null;

    public CbusPreferences getCbusPreferences() {
        if (adapterMemo.getDisabled()) {
            return null;
        }
        if (cbusPreferences == null) {
            cbusPreferences = new CbusPreferences();
            jmri.InstanceManager.store( cbusPreferences, CbusPreferences.class );
        }
        return cbusPreferences;
    }

    private CbusNodeTableDataModel cbusNodeTableDataModel = null;

    public CbusNodeTableDataModel getCbusNodeTableDataModel() {
        if (adapterMemo.getDisabled()) {
            return null;
        }
        if (cbusNodeTableDataModel == null) {
            cbusNodeTableDataModel = new CbusNodeTableDataModel(adapterMemo, 2, CbusNodeTableDataModel.MAX_COLUMN);
            
            if ( cbusPreferences == null ){
                cbusPreferences = getCbusPreferences();
            }
            cbusNodeTableDataModel.startup();
        }
        return cbusNodeTableDataModel;
    }
    
    private CbusEventTableDataModel cbusEventTable = null;
    
    public CbusEventTableDataModel getCbusEventTableDataModel(){
        if (adapterMemo.getDisabled()) {
            return null;
        }
        if (cbusEventTable == null) {
            cbusEventTable = new CbusEventTableDataModel(adapterMemo, 5, CbusEventTableDataModel.MAX_COLUMN);
            InstanceManager.store(cbusEventTable, CbusEventTableDataModel.class);
        }
        return cbusEventTable;
    }
    
    protected CbusCabSignalManager cabSignalManager;

    public CbusCabSignalManager getCabSignalManager() {
        if ( adapterMemo.getDisabled() ) {
            return null;
        }
        if (cabSignalManager == null) {
            cabSignalManager = new CbusCabSignalManager(adapterMemo);
        }
        return cabSignalManager;
    }


    @Override
    public void dispose() {
        
        if ( cbusEventTable != null ){
            InstanceManager.deregister(cbusEventTable, CbusEventTableDataModel.class);
        }
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
        if (commandStation != null) {
            InstanceManager.deregister(commandStation, jmri.CommandStation.class);
        }
        if (multiMeter != null) {
            InstanceManager.deregister(multiMeter, jmri.MultiMeter.class);
        }
        if (cbusPreferences != null) {
            InstanceManager.deregister(cbusPreferences, jmri.jmrix.can.cbus.CbusPreferences.class);
        }
        if (cbusNodeTableDataModel != null) {
            InstanceManager.deregister(cbusNodeTableDataModel, CbusNodeTableDataModel.class);
        }
       InstanceManager.deregister(this, CbusConfigurationManager.class);
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.can.CanActionListBundle");
    }

}
