package jmri.jmrix.can.cbus;

import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.cbus.simulator.CbusSimulator;
import jmri.jmrix.can.cbus.node.CbusNodeTableDataModel;
import jmri.jmrix.can.cbus.eventtable.CbusEventTableDataModel;
import jmri.jmrix.can.cbus.swing.cbusslotmonitor.CbusSlotMonitorDataModel;

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
 * @author Steve Young Copyright (C) 2022
 */
public class CbusConfigurationManager extends jmri.jmrix.can.ConfigurationManager implements Disposable {

    /**
     * Create a new CbusConfigurationManager.
     * A Supporting class to configure the {@link jmri.jmrix.can.CanSystemConnectionMemo}
     * for the {@link jmri.jmrix.can.cbus} classes.
     * @param memo Connection to configure.
     */
    public CbusConfigurationManager(@Nonnull CanSystemConnectionMemo memo) {
        super(memo);
        storeToMemoAndInstance(CbusConfigurationManager.this, CbusConfigurationManager.class);
        cf = new jmri.jmrix.can.cbus.swing.CbusComponentFactory(adapterMemo);
        InstanceManager.store(cf, jmri.jmrix.swing.ComponentFactory.class);
    }

    private final jmri.jmrix.swing.ComponentFactory cf;

    // configureManagers() startup order
    private static final List<Class<?>> DEFAULT_CLASSES = List.of(
        CbusPreferences.class, 
        PowerManager.class,
        SensorManager.class,
        // SensorManager before TurnoutManager so that listener can be added
        CommandStation.class,
        // CommandStation before TurnoutManager so that Raw Turnout Operator available
        TurnoutManager.class,
        ThrottleManager.class,
        CbusPredefinedMeters.class,
        ReporterManager.class,
        LightManager.class,
        CabSignalManager.class,
        // Clock Control initialised last so CbusSensorManager exists, otherwise
        // InternalSensorManager is deafult SensorManager when ISCLOCKRUNNING is provided.
        ClockControl.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public void configureManagers() {

        for (Class<?> listClass : DEFAULT_CLASSES ) {
            provide(listClass);
        }

        // We register a programmer based on whether the hardware is available,
        // not whether the functionality is available
        CbusDccProgrammerManager pm = getProgrammerManager();
        if ( pm !=null ) {
            if (pm.isAddressedModeHardwareAvailable()) {
                storeToMemoAndInstance(pm, AddressedProgrammerManager.class);
            }
            if (pm.isGlobalProgrammerHardwareAvailable()) {
                storeToMemoAndInstance(pm, GlobalProgrammerManager.class);
            }
        }

        if (getConsistManager() != null) {
            storeToMemoAndInstance(getConsistManager(), ConsistManager.class);
        }

        // kick-start cbus sim tools ( Dummy Command Station etc. ) if using loopback connection
        if ( adapterMemo.getTrafficController() instanceof jmri.jmrix.can.adapters.loopback.LoopbackTrafficController) {
            adapterMemo.get( CbusSimulator.class);
        }

    }

    /**
     * Tells which managers this class provides.
     * {@inheritDoc}
     * @param type Class type to check
     * @return true if supported; false if not
     */
    @Override
    public boolean provides(Class<?> type) {
        if (adapterMemo.getDisabled()) {
            return false;
        } else if (type.equals(AddressedProgrammerManager.class)) {
            return getProgrammerManager().isAddressedModePossible();
        } else if (type.equals(GlobalProgrammerManager.class)) {
            return getProgrammerManager().isGlobalProgrammerAvailable();
        } else if (type.equals(ConsistManager.class)) {
            return true;
        } else if (type.equals(CbusSimulator.class)) {
            return true;
        } else if (type.equals(CbusSlotMonitorDataModel.class)) {
            return true;
        } else {
            return DEFAULT_CLASSES.contains(type);
        }
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Class<?> T) {
        if (adapterMemo.getDisabled()) {
            return null;
        } else if (T.equals(AddressedProgrammerManager.class)
                && getProgrammerManager().isAddressedModePossible()) {
            return (T) getProgrammerManager();
        } else if (T.equals(GlobalProgrammerManager.class)
                && getProgrammerManager().isGlobalProgrammerAvailable()) {
            return (T) getProgrammerManager();
        } else if (T.equals(ConsistManager.class)) {
            return (T) getConsistManager();
        } else if (T.equals(CbusSimulator.class)) {
            return provide(T);
        } else if (T.equals(CbusSlotMonitorDataModel.class)) {
            return provide(T);
        } else if ( DEFAULT_CLASSES.contains(T) ) {
            return provide(T);
        }
        return null; // nothing, by default
    }

    private CbusDccProgrammerManager programmerManager;

    private CbusDccProgrammerManager getProgrammerManager() {
        if (programmerManager == null && !adapterMemo.getDisabled()) {
            programmerManager = new CbusDccProgrammerManager(
                    new CbusDccProgrammer(adapterMemo), adapterMemo);
        }
        return programmerManager;
    }

    protected CbusConsistManager consistManager = null;

    /**
     * Get the ConsistManager, creating one if neccessary.
     * 
     * Only enable it if we definitely have a command station.
     * 
     * @return ConsistManager object
     */
    private ConsistManager getConsistManager() {
        if ( adapterMemo.getDisabled() ) {
            return null;
        }
        if (consistManager == null) {
            consistManager = new CbusConsistManager(get(CommandStation.class));
            if (adapterMemo.getProgModeSwitch() == ProgModeSwitch.EITHER) {
                // Could be either programmer or command station
                if (getProgrammerManager().isAddressedModePossible()) {
                    // We have a command station so enable the ConsistManager
                    consistManager.setEnabled(true);
                } else {
                    // Disable for now, may be enabled later if user switches modes, avoid returning a null manager
                    consistManager.setEnabled(false);
                }
            } else {
                // Command station is always avaliable
                consistManager.setEnabled(true);
            }
        }
        return consistManager;
    }

    /**
     * Provide a new Class instance.
     * <p>
     * NOT for general use outside of this class, although public so that
     * classes like CbusEventTablePane can get a CbusEventTableDataModel
     * when started.
     * <p>
     * If a class is NOT auto-created by the normal get,
     * it can be provided with this method.
     * Adds provided class to memo class object map,
     * AND InstanceManager.
     * @param <T> class type.
     * @param T class type.
     * @return class object, or null if unavailable.
     */
    public <T> T provide(@Nonnull Class<?> T){
        if (adapterMemo.getDisabled()) {
            return null;
        }
        T existing = adapterMemo.getFromMap(T); // if already in object map, use it
        if ( existing !=null ) {
            return existing;
        }
        if (T.equals(CbusNodeTableDataModel.class)) {
            storeToMemoAndInstance(new CbusNodeTableDataModel(adapterMemo,10), CbusNodeTableDataModel.class);
        } else if (T.equals(CbusEventTableDataModel.class)) {
            storeToMemoAndInstance(new CbusEventTableDataModel(adapterMemo,10), CbusEventTableDataModel.class);
        } else if (T.equals(CbusPreferences.class)) {
            storeToMemoAndInstance(new CbusPreferences(), CbusPreferences.class);
        } else if (T.equals(PowerManager.class)) {
            storeToMemoAndInstance(new CbusPowerManager(adapterMemo), PowerManager.class);
        } else if (T.equals(CommandStation.class)) {
            storeToMemoAndInstance(new CbusCommandStation(adapterMemo), CommandStation.class);
        } else if (T.equals(ThrottleManager.class)) {
            storeToMemoAndInstance(new CbusThrottleManager(adapterMemo), ThrottleManager.class);
        } else if (T.equals(CabSignalManager.class)) {
            storeToMemoAndInstanceDefault(new CbusCabSignalManager(adapterMemo), CabSignalManager.class);
        } else if (T.equals(ClockControl.class) ) {
            storeToMemoAndInstanceDefault(new CbusClockControl(adapterMemo), ClockControl.class);
        } else if (T.equals(SensorManager.class) ) {
            adapterMemo.store(new CbusSensorManager(adapterMemo), SensorManager.class);
            InstanceManager.setSensorManager(adapterMemo.getFromMap(T));
        } else if (T.equals(TurnoutManager.class) ) {
            adapterMemo.store(new CbusTurnoutManager(adapterMemo), TurnoutManager.class);
            InstanceManager.setTurnoutManager(adapterMemo.getFromMap(T));
        } else if (T.equals(ReporterManager.class) ) {
            adapterMemo.store(new CbusReporterManager(adapterMemo), ReporterManager.class);
            InstanceManager.setReporterManager(adapterMemo.getFromMap(T));
        } else if (T.equals(LightManager.class) ) {
            adapterMemo.store(new CbusLightManager(adapterMemo), LightManager.class);
            InstanceManager.setLightManager(adapterMemo.getFromMap(T));
        } else if (T.equals(CbusPredefinedMeters.class) ) {
            InstanceManager.setMeterManager(new jmri.managers.AbstractMeterManager(adapterMemo));
            storeToMemoAndInstance(new CbusPredefinedMeters(adapterMemo), CbusPredefinedMeters.class);
        }
        else if (T.equals(CbusSimulator.class)) {
            storeToMemoAndInstance(new CbusSimulator(adapterMemo), CbusSimulator.class);
        }
        else if (T.equals(CbusSlotMonitorDataModel.class)) {
            storeToMemoAndInstance(new CbusSlotMonitorDataModel(adapterMemo), CbusSlotMonitorDataModel.class);
        }
        return adapterMemo.getFromMap(T); // if class not in map, class not provided.
    }

    private <T> void storeToMemoAndInstance(@Nonnull T item, @Nonnull Class<T> type){
        adapterMemo.store(item, type); // store with memo
        InstanceManager.store(item, type); // and with InstanceManager
    }

    private <T> void storeToMemoAndInstanceDefault(@Nonnull T item, @Nonnull Class<T> type){
        adapterMemo.store(item, type); // store with memo
        InstanceManager.setDefault( type, item); // and with InstanceManager
    }

    public  <T> void disposeOf(@Nonnull T item, @Nonnull Class<T> type ) {
        InstanceManager.deregister(item, type);
        adapterMemo.deregister(item, type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {

        // classed stored in the memo classObjectMap will be deregisted from
        // InstanceManager on memo disposal, and will also have their 
        // dispose method called if they implement jmri.Disposable.
        
        InstanceManager.deregister(cf, jmri.jmrix.swing.ComponentFactory.class);

        if (consistManager != null) {
            InstanceManager.deregister(consistManager, ConsistManager.class);
        }
        if (programmerManager != null) {
            programmerManager.dispose();
        }
        InstanceManager.deregister(this, CbusConfigurationManager.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.can.CanActionListBundle");
    }

    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CbusConfigurationManager.class);

}
