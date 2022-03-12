package jmri.jmrix.loconet.hexfile;

import jmri.GlobalProgrammerManager;
import jmri.managers.ManagerDefaultSelector;

import javax.annotation.OverridingMethodsMustInvokeSuper;

/**
 * Lightweight class to denote that a system is "active" via a LocoNet hexfile emulator.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author Kevin Dickerson Copyright (C) 2010
 * @author Egbert Broerse (C) 2020
 */
public class HexFileSystemConnectionMemo extends jmri.jmrix.loconet.LocoNetSystemConnectionMemo {

    /**
     * Use the simulation (hexfile) LocoNet sensor manager instead of the standard LocoNet sensor manager.
     */
    @Override
    public LnSensorManager getSensorManager() {
        if (getDisabled()) {
            return null;
        }
        return (LnSensorManager) classObjectMap.computeIfAbsent(jmri.SensorManager.class, (Class<?> c) -> new LnSensorManager(this));
    }

    /**
     * Substitute the jmri.progdebugger.DebugProgrammerManager when this connection
     * is set to provide the default (Service Mode) GlobalProgrammerManager, replacing
     * the LocoNet LnProgrammerManager that the super class would return.
     * For setting up this connection the substitution was already done in
     * {@link HexFileFrame#configure()} to allow for debugging and mocking replies
     * from the layout connection.
     * Since the {@link ManagerDefaultSelector} directly calls this memo, and not
     * the InstanceManager, we prevent to return the default class which does not match
     * the active instance, creating an extra programmer that shows up as an extra
     * line in the Program over combo.
     *
     * @param <T>  Type of manager to get
     * @param type Type of manager to get
     * @return The manager or null if provides() is false for T
     * @see #provides(java.lang.Class)
     */
    @OverridingMethodsMustInvokeSuper
    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Class<?> type) {
        if (type.equals(GlobalProgrammerManager.class)) {
            log.debug("Hex memo returned Global(Ops)ModeProgrammer");
            return (T) getProgrammerManager();
        } else {
            return super.get(type);
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HexFileSystemConnectionMemo.class);

}
