// PR3SystemConnectionMemo.java

package jmri.jmrix.loconet.pr3;

import jmri.InstanceManager;
import jmri.jmrix.loconet.*;

/**
 * Lightweight class to denote that a PR3 is active
 *
 * @author		Bob Jacobsen  Copyright (C) 2010
 * @version             $Revision: 1.4 $
 */
public class PR3SystemConnectionMemo extends LocoNetSystemConnectionMemo  {

    public PR3SystemConnectionMemo(LnTrafficController lt,
                                        SlotManager sm) {
        super(lt, sm);
    }
    
   /**
     * Configure the subset of LocoNet managers valid for the PR3 in PR2 mode.
     */
    public void configureManagersPR2(LnTrafficController controller) {
        
        InstanceManager.setPowerManager(new jmri.jmrix.loconet.pr2.LnPr2PowerManager(getSlotManager(), controller));

        InstanceManager.setThrottleManager(new jmri.jmrix.loconet.LnPr2ThrottleManager());
    }

   /**
     * Configure the subset of LocoNet managers valid for the PR3 in MS100 mode.
     */
    public void configureManagersMS100(LnTrafficController controller) {
        
        LocoNetThrottledTransmitter tm = new LocoNetThrottledTransmitter(controller);

        InstanceManager.setPowerManager(new jmri.jmrix.loconet.LnPowerManager(controller));

        InstanceManager.setTurnoutManager(new jmri.jmrix.loconet.LnTurnoutManager(controller, tm, getSystemPrefix()));

        InstanceManager.setLightManager(new jmri.jmrix.loconet.LnLightManager());

        InstanceManager.setSensorManager(new jmri.jmrix.loconet.LnSensorManager());

        InstanceManager.setThrottleManager(new jmri.jmrix.loconet.LnThrottleManager(getSlotManager()));

        InstanceManager.setReporterManager(new jmri.jmrix.loconet.LnReporterManager(controller));

        InstanceManager.addClockControl(new jmri.jmrix.loconet.LnClockControl(getSlotManager(), controller));

    }

}


/* @(#)PR3SystemConnectionMemo.java */
