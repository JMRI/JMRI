package jmri.jmrix.loconet.uhlenbrock;

import jmri.InstanceManager;
import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.SlotManager;
import jmri.managers.DefaultProgrammerManager;

/**
 * Lightweight class to denote that an Uhlenbrock IB-COM or Intellibox II is
 * active
 *
 * @author Bob Jacobsen Copyright (C) 2010
 */
public class UhlenbrockSystemConnectionMemo extends LocoNetSystemConnectionMemo {

    public UhlenbrockSystemConnectionMemo(LnTrafficController lt,
            SlotManager sm) {
        super(lt, sm);
    }

    public UhlenbrockSystemConnectionMemo() {
        super();
    }

    @Override
    public DefaultProgrammerManager getProgrammerManager() {
        if (programmerManager == null) {
            setProgrammerManager(new UhlenbrockProgrammerManager(this));
        }
        return super.getProgrammerManager();
    }

    @Override
    public void dispose() {
        InstanceManager.deregister(this, UhlenbrockSystemConnectionMemo.class);
        super.dispose();
    }

    @Override
    public void configureManagers() {
        super.configureManagers();
        getTurnoutManager().setUhlenbrockMonitoring();
    }

}
