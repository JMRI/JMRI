package jmri.jmrix.loconet.pr2;

import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.jmrix.debugthrottle.DebugThrottleManager;
import jmri.jmrix.loconet.LnPr2ThrottleManager;
import jmri.jmrix.loconet.LnPowerManager;
import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.SlotManager;

/**
 * Lightweight class to denote that a PR2 is active
 *
 * @author Bob Jacobsen Copyright (C) 2010
 */
public class PR2SystemConnectionMemo extends LocoNetSystemConnectionMemo {

    public PR2SystemConnectionMemo(LnTrafficController lt,
            SlotManager sm) {
        super(lt, sm);
    }

    public PR2SystemConnectionMemo() {
        super();
    }

    /**
     * Configure the subset of LocoNet managers valid for the PR2.
     */
    @Override
    public void configureManagers() {
        jmri.InstanceManager.store(getPowerPr2Manager(), jmri.PowerManager.class);

        // jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.loconet.LnTurnoutManager());

        // jmri.InstanceManager.setLightManager(new jmri.jmrix.loconet.LnLightManager());

        // jmri.InstanceManager.setSensorManager(new jmri.jmrix.loconet.LnSensorManager());
        jmri.InstanceManager.setThrottleManager(getPr2ThrottleManager());

        if (getProgrammerManager().isAddressedModePossible()) {
            InstanceManager.store(getProgrammerManager(), jmri.AddressedProgrammerManager.class);
        }
        if (getProgrammerManager().isGlobalProgrammerAvailable()) {
            InstanceManager.store(getProgrammerManager(), GlobalProgrammerManager.class);
        }

        // jmri.InstanceManager.setReporterManager(new jmri.jmrix.loconet.LnReporterManager());
    }

    private LnPr2PowerManager powerPr2Manager;

    public LnPr2PowerManager getPowerPr2Manager() {
        if (getDisabled()) {
            return null;
        }
        if (powerPr2Manager == null) {
            powerPr2Manager = new jmri.jmrix.loconet.pr2.LnPr2PowerManager(this);
        }
        return powerPr2Manager;
    }

    private LnPr2ThrottleManager throttlePr2Manager;

    public LnPr2ThrottleManager getPr2ThrottleManager() {
        if (getDisabled()) {
            return null;
        }
        if (throttlePr2Manager == null) {
            throttlePr2Manager = new jmri.jmrix.loconet.LnPr2ThrottleManager(this);
        }
        return throttlePr2Manager;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Class<?> type) {
        if (getDisabled()) {
            return null;
        }
        if (type.equals(jmri.PowerManager.class)) {
            return (T) getPowerPr2Manager();
        }
        if (type.equals(jmri.ThrottleManager.class)) {
            return (T) getPr2ThrottleManager();
        }
        if (type.equals(jmri.GlobalProgrammerManager.class)) {
            return (T) getProgrammerManager();
        }
        if (type.equals(jmri.AddressedProgrammerManager.class)) {
            return (T) getProgrammerManager();
        }
        if(type.equals(jmri.ConsistManager.class)){
           return (T) getConsistManager();
        }
        return null;
    }

    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled()) {
            return false;
        }
        if (type.equals(jmri.PowerManager.class)) {
            return true;
        }
        if (type.equals(jmri.ThrottleManager.class)) {
            return true;
        }
        if (type.equals(jmri.GlobalProgrammerManager.class)) {
            return getProgrammerManager().isGlobalProgrammerAvailable();
        }
        if (type.equals(jmri.AddressedProgrammerManager.class)) {
            return getProgrammerManager().isAddressedModePossible();
        }
        if(type.equals(jmri.ConsistManager.class)){
           return(getConsistManager()!=null);
        } 
        return false;
    }

    @Override
    public void dispose() {
        InstanceManager.deregister(this, PR2SystemConnectionMemo.class);

        if (powerPr2Manager != null) {
            powerPr2Manager.dispose();
            InstanceManager.deregister(powerPr2Manager, LnPowerManager.class);
        }

        super.dispose();
    }

}
