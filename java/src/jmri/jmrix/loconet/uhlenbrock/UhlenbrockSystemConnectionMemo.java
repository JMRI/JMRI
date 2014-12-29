// UhlenbrockSystemConnectionMemo.java

package jmri.jmrix.loconet.uhlenbrock;

import jmri.InstanceManager;
import jmri.ProgrammerManager;
import jmri.jmrix.loconet.*;

/**
 * Lightweight class to denote that an Uhlenbrock IB-COM or Intellibox II is active
 *
 * @author		Bob Jacobsen  Copyright (C) 2010
 * @version             $Revision: 18841 $
 */
public class UhlenbrockSystemConnectionMemo extends LocoNetSystemConnectionMemo  {

    public UhlenbrockSystemConnectionMemo(LnTrafficController lt,
                                        SlotManager sm) {
        super(lt, sm);
    }
    
    public UhlenbrockSystemConnectionMemo() {
        super();
    }
    
    @Override
    public ProgrammerManager getProgrammerManager() {
        if (programmerManager == null)
            setProgrammerManager(new UhlenbrockProgrammerManager(getSlotManager(), this));
        return super.getProgrammerManager();
    }
    
    private UhlenbrockLnThrottleManager throttleUhlManager;
    
    @Override
    public UhlenbrockLnThrottleManager getThrottleManager() { 
        if (getDisabled())
            return null;
        if (throttleUhlManager == null)
            throttleUhlManager = new jmri.jmrix.loconet.uhlenbrock.UhlenbrockLnThrottleManager(this);
        return throttleUhlManager;
    }
        
    public void dispose() {
        InstanceManager.deregister(this, UhlenbrockSystemConnectionMemo.class);
        if (throttleUhlManager != null) {
            InstanceManager.deregister(throttleUhlManager, jmri.jmrix.loconet.uhlenbrock.UhlenbrockLnThrottleManager.class);
        }
        super.dispose();
    }

}


/* @(#)UhlenbrockSystemConnectionMemo.java */
