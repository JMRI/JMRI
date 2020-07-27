package jmri.jmrix.loconet.pr2;

import jmri.GlobalProgrammerManager;
import jmri.InstanceManager;
import jmri.PowerManager;
import jmri.ThrottleManager;
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

        jmri.InstanceManager.setThrottleManager(getPr2ThrottleManager());

        if (getProgrammerManager().isAddressedModePossible()) {
            InstanceManager.store(getProgrammerManager(), jmri.AddressedProgrammerManager.class);
        }
        if (getProgrammerManager().isGlobalProgrammerAvailable()) {
            InstanceManager.store(getProgrammerManager(), GlobalProgrammerManager.class);
        }

        register();
    }

    public LnPr2PowerManager getPowerPr2Manager() {
        if (getDisabled()) {
            return null;
        }
        return (LnPr2PowerManager) classObjectMap.computeIfAbsent(PowerManager.class, (Class c) -> new LnPr2PowerManager(this));
    }

    public LnPr2ThrottleManager getPr2ThrottleManager() {
        if (getDisabled()) {
            return null;
        }
        return (LnPr2ThrottleManager) classObjectMap.computeIfAbsent(ThrottleManager.class, (Class c) -> new LnPr2ThrottleManager(this));
    }

    @Override
    public void dispose() {
        InstanceManager.deregister(this, PR2SystemConnectionMemo.class);

        LnPr2PowerManager powerPr2Manager = get(PowerManager.class);
        if (powerPr2Manager != null) {
            powerPr2Manager.dispose();
            InstanceManager.deregister(powerPr2Manager, LnPowerManager.class);
            deregister(powerPr2Manager,PowerManager.class);
        }

        super.dispose();
    }

}
