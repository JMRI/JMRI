// UhlenbrockSystemConnectionMemo.java
package jmri.jmrix.loconet.uhlenbrock;

import jmri.InstanceManager;
import jmri.ProgrammerManager;
import jmri.jmrix.loconet.LnTrafficController;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.SlotManager;

/**
 * Lightweight class to denote that an Uhlenbrock IB-COM or Intellibox II is
 * active
 *
 * @author	Bob Jacobsen Copyright (C) 2010
 * @version $Revision: 18841 $
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
    public ProgrammerManager getProgrammerManager() {
        if (programmerManager == null) {
            setProgrammerManager(new UhlenbrockProgrammerManager(getSlotManager(), this));
        }
        return super.getProgrammerManager();
    }

    public void dispose() {
        InstanceManager.deregister(this, UhlenbrockSystemConnectionMemo.class);
        super.dispose();
    }

    public void configureManagers() {
        super.configureManagers();
        getTurnoutManager().setUhlenbrockMonitoring();
}

}

/* @(#)UhlenbrockSystemConnectionMemo.java */
