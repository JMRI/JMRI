// PR3SystemConnectionMemo.java
package jmri.jmrix.loconet.pr3;

import jmri.InstanceManager;
import jmri.ThrottleManager;
import jmri.jmrix.loconet.LnPowerManager;
import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.LocoNetThrottledTransmitter;
import jmri.jmrix.loconet.SlotManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lightweight class to denote that a PR3 is active
 *
 * @author	Bob Jacobsen Copyright (C) 2010
 * @version $Revision$
 */
public class PR3SystemConnectionMemo extends LocoNetSystemConnectionMemo {

    public PR3SystemConnectionMemo(LnTrafficController lt,
            SlotManager sm) {
        super(lt, sm);
    }

    public PR3SystemConnectionMemo() {
        super();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Class<?> T) {
        if (getDisabled()) {
            return null;
        }
        if (mode == MS100MODE) {
            return (T) super.get(T);
        }
        if (T.equals(jmri.ProgrammerManager.class)) {
            return (T) getProgrammerManager();
        }
        if (T.equals(jmri.GlobalProgrammerManager.class)) {
            return (T) getProgrammerManager();
        }
        if (T.equals(jmri.AddressedProgrammerManager.class)) {
            return (T) getProgrammerManager();
        }

        if (T.equals(jmri.ThrottleManager.class)) {
            return (T) getThrottleManager();
        }
        if (T.equals(jmri.PowerManager.class)) {
            return (T) getPowerManager();
        }
        return null; // nothing, by default
    }

    final static int PR3MODE = 0x00;
    final static int MS100MODE = 0x01;

    int mode = PR3MODE;

    /**
     * Configure the subset of LocoNet managers valid for the PR3 in PR2 mode.
     */
    public void configureManagersPR2() {
        mode = PR3MODE;
        InstanceManager.setPowerManager(
                getPowerManager());

        InstanceManager.setThrottleManager(
                getThrottleManager());

        jmri.InstanceManager.setProgrammerManager(
                getProgrammerManager());

    }

    public ThrottleManager getThrottleManager() {
        if (super.getDisabled()) {
            return null;
        }
        if (mode == MS100MODE) {
            return super.getThrottleManager();
        }
        if (throttleManager == null) {
            throttleManager = new jmri.jmrix.loconet.LnPr2ThrottleManager(this);
        }
        return throttleManager;
    }

    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled()) {
            return false;
        }
        if (mode == MS100MODE) {
            return super.provides(type);
        }
        if (type.equals(jmri.ProgrammerManager.class)) {
            return true;
        }
        if (type.equals(jmri.GlobalProgrammerManager.class)) {
            return getProgrammerManager().isGlobalProgrammerAvailable();
        }
        if (type.equals(jmri.AddressedProgrammerManager.class)) {
            return getProgrammerManager().isAddressedModePossible();
        }

        if (type.equals(jmri.ThrottleManager.class)) {
            return true;
        }
        if (type.equals(jmri.PowerManager.class)) {
            return true;
        }
        return false;
    }
    //private jmri.jmrix.loconet.pr2.LnPr2PowerManager powerManager;

    public LnPowerManager getPowerManager() {
        if (getDisabled()) {
            return null;
        }
        if (mode == MS100MODE) {
            return super.getPowerManager();
        }
        if (powerManager == null) {
            powerManager = new jmri.jmrix.loconet.pr2.LnPr2PowerManager(this);
        }
        return powerManager;
    }

    /**
     * Configure the subset of LocoNet managers valid for the PR3 in MS100 mode.
     */
    public void configureManagersMS100() {
        mode = MS100MODE;

        tm = new LocoNetThrottledTransmitter(getLnTrafficController(), mTurnoutExtraSpace);
        log.debug("ThrottleTransmitted configured with :" + mTurnoutExtraSpace);

        InstanceManager.setPowerManager(super.getPowerManager());

        InstanceManager.setTurnoutManager(getTurnoutManager());

        InstanceManager.setLightManager(getLightManager());

        InstanceManager.setSensorManager(getSensorManager());

        InstanceManager.setThrottleManager(super.getThrottleManager());

        jmri.InstanceManager.setProgrammerManager(getProgrammerManager());

        InstanceManager.setReporterManager(getReporterManager());

        InstanceManager.addClockControl(getClockControl());

    }

    public void dispose() {
        InstanceManager.deregister(this, PR3SystemConnectionMemo.class);
        super.dispose();
    }
    private final static Logger log = LoggerFactory.getLogger(PR3SystemConnectionMemo.class.getName());
}

/* @(#)PR3SystemConnectionMemo.java */
