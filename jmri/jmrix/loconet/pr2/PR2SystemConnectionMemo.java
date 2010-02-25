// PR2SystemConnectionMemo.java

package jmri.jmrix.loconet.pr2;

import jmri.InstanceManager;
import jmri.jmrix.loconet.*;

/**
 * Lightweight class to denote that a PR2 is active
 *
 * @author		Bob Jacobsen  Copyright (C) 2010
 * @version             $Revision: 1.1 $
 */
public class PR2SystemConnectionMemo extends LocoNetSystemConnectionMemo  {

    public PR2SystemConnectionMemo(LnTrafficController lt,
                                        SlotManager sm) {
        super(lt, sm);
    }
    
   /**
     * Configure the subset of LocoNet managers valid for the PR2.
     */
    public void configureManagers(jmri.jmrix.loconet.pr2.LnPacketizer packets) {
        jmri.InstanceManager.setPowerManager(new jmri.jmrix.loconet.pr2.LnPr2PowerManager());

        /* jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.loconet.LnTurnoutManager()); */

        /* jmri.InstanceManager.setLightManager(new jmri.jmrix.loconet.LnLightManager()); */

        /* jmri.InstanceManager.setSensorManager(new jmri.jmrix.loconet.LnSensorManager()); */

        jmri.InstanceManager.setThrottleManager(new jmri.jmrix.loconet.LnPr2ThrottleManager());

        /* jmri.InstanceManager.setReporterManager(new jmri.jmrix.loconet.LnReporterManager()); */

    }

}


/* @(#)PR2SystemConnectionMemo.java */
