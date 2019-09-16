package jmri.jmrix.loconet.pr3;

import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.ShutDownTask;
import jmri.ThrottleManager;
import jmri.implementation.QuietShutDownTask;
import jmri.jmrix.loconet.LnPowerManager;
import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.SlotManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lightweight class to denote that a PR3 is active
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @author B. Milhaupt Copyright (C) 2017
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
        if(T.equals(jmri.ConsistManager.class)){
           return (T) getConsistManager();
        }
        return null;
    }

    final static int PR3MODE = 0x00;
    final static int MS100MODE = 0x01;

    int mode = PR3MODE;
    private ShutDownTask restoreToLocoNetInterfaceModeTask;

    /**
     * Configure the subset of LocoNet managers valid for the PR3 in PR2 mode.
     */
    public void configureManagersPR2() {
        mode = PR3MODE;
        InstanceManager.store(getPowerManager(), jmri.PowerManager.class);

        InstanceManager.setThrottleManager(
                getThrottleManager());

        if (getProgrammerManager().isAddressedModePossible()) {
            InstanceManager.store(getProgrammerManager(), jmri.AddressedProgrammerManager.class);
        }
        if (getProgrammerManager().isGlobalProgrammerAvailable()) {
            InstanceManager.store(getProgrammerManager(), GlobalProgrammerManager.class);
        }
        // Establish a ShutDownTask so that the PR3 should be be returned to
        // LocoNet Interface mode at shutdown
        // Finally, create and register a shutdown task to ensure clean exit
        if (restoreToLocoNetInterfaceModeTask == null) {
            restoreToLocoNetInterfaceModeTask = new QuietShutDownTask("Restore PR3 to LocoNet Interface Mode") {    // NOI18N
                @Override
                public boolean execute() {

                    if (mode == PR3MODE) {
                        // try to change from "standalone programmer" to "LocoNet interface" mode
                        LnTrafficController tc;
                        tc = getLnTrafficController();
                        if (tc != null) {
                            LocoNetMessage msg = new LocoNetMessage(6);
                            msg.setOpCode(0xD3);
                            msg.setElement(1, 0x10);
                            msg.setElement(2, 0);  // set MS100, no power
                            msg.setElement(3, 0);
                            msg.setElement(4, 0);
                            tc.sendLocoNetMessage(msg);
                            log.info("Configuring PR3 for 'LocoNet Interface' mode"); // NOI18N
                        }
                    }
                    return true;
                }
            };
            InstanceManager.getDefault(jmri.ShutDownManager.class).register(restoreToLocoNetInterfaceModeTask);
        }
    }

    @Override
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
        if(type.equals(jmri.ConsistManager.class)){
           return(getConsistManager()!=null);
        } 
        return false;
    }
    //private jmri.jmrix.loconet.pr2.LnPr2PowerManager powerManager;

    /**
     * Get the connection's LnPowerManager.
     *
     * @return the LocoNet power manager; may be null in some circumstances
     */
    @Override
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
     * Configure the LocoNet managers valid for the PR3 in MS100 mode, same as super, flag the interface type.
     */
    public void configureManagersMS100() {
        mode = MS100MODE;
        super.configureManagers();
    }

    @Override
    public void dispose() {
        InstanceManager.deregister(this, PR3SystemConnectionMemo.class);
        if(tm!=null){
           tm.dispose();
        }
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(PR3SystemConnectionMemo.class);

}
