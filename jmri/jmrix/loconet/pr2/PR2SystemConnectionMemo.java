// PR2SystemConnectionMemo.java

package jmri.jmrix.loconet.pr2;

import jmri.InstanceManager;
import jmri.jmrix.loconet.*;

/**
 * Lightweight class to denote that a PR2 is active
 *
 * @author		Bob Jacobsen  Copyright (C) 2010
 * @version             $Revision: 1.6 $
 */
public class PR2SystemConnectionMemo extends LocoNetSystemConnectionMemo  {

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
    public void configureManagers() {
        jmri.InstanceManager.setPowerManager(new jmri.jmrix.loconet.pr2.LnPr2PowerManager(this));

        /* jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.loconet.LnTurnoutManager()); */

        /* jmri.InstanceManager.setLightManager(new jmri.jmrix.loconet.LnLightManager()); */

        /* jmri.InstanceManager.setSensorManager(new jmri.jmrix.loconet.LnSensorManager()); */

        jmri.InstanceManager.setThrottleManager(new jmri.jmrix.loconet.LnPr2ThrottleManager());

        jmri.InstanceManager.setProgrammerManager(
            new jmri.jmrix.loconet.LnProgrammerManager(getSlotManager()));

        /* jmri.InstanceManager.setReporterManager(new jmri.jmrix.loconet.LnReporterManager()); */

    }
    
    public void dispose() {
        InstanceManager.deregister(this, PR2SystemConnectionMemo.class);
        super.dispose();
    }

}


/* @(#)PR2SystemConnectionMemo.java */
