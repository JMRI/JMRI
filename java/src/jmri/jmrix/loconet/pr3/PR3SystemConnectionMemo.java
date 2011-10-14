// PR3SystemConnectionMemo.java

package jmri.jmrix.loconet.pr3;

import jmri.InstanceManager;
import jmri.jmrix.loconet.*;

/**
 * Lightweight class to denote that a PR3 is active
 *
 * @author		Bob Jacobsen  Copyright (C) 2010
 * @version             $Revision$
 */
public class PR3SystemConnectionMemo extends LocoNetSystemConnectionMemo  {

    public PR3SystemConnectionMemo(LnTrafficController lt,
                                        SlotManager sm) {
        super(lt, sm);
    }
    
    public PR3SystemConnectionMemo() {
        super();
    }
    
   /**
     * Configure the subset of LocoNet managers valid for the PR3 in PR2 mode.
     */
    public void configureManagersPR2() {
        
        InstanceManager.setPowerManager(new jmri.jmrix.loconet.pr2.LnPr2PowerManager(this));

        InstanceManager.setThrottleManager(new jmri.jmrix.loconet.LnPr2ThrottleManager(this));
        
        jmri.InstanceManager.setProgrammerManager(
            new jmri.jmrix.loconet.LnProgrammerManager(getSlotManager(), this));

    }

   /**
     * Configure the subset of LocoNet managers valid for the PR3 in MS100 mode.
     */
    public void configureManagersMS100() {
        
        LocoNetThrottledTransmitter tm = new LocoNetThrottledTransmitter(getLnTrafficController());

        InstanceManager.setPowerManager(new jmri.jmrix.loconet.LnPowerManager(this));

        InstanceManager.setTurnoutManager(new jmri.jmrix.loconet.LnTurnoutManager(getLnTrafficController(), tm, getSystemPrefix()));

        InstanceManager.setLightManager(new jmri.jmrix.loconet.LnLightManager(getLnTrafficController(), getSystemPrefix()));

        InstanceManager.setSensorManager(new jmri.jmrix.loconet.LnSensorManager(getLnTrafficController(), getSystemPrefix()));

        InstanceManager.setThrottleManager(new jmri.jmrix.loconet.LnThrottleManager(getSlotManager(), this));

        jmri.InstanceManager.setProgrammerManager(
            new jmri.jmrix.loconet.LnProgrammerManager(getSlotManager(), this));

        InstanceManager.setReporterManager(new jmri.jmrix.loconet.LnReporterManager(getLnTrafficController(), getSystemPrefix()));

        InstanceManager.addClockControl(new jmri.jmrix.loconet.LnClockControl(getSlotManager(), getLnTrafficController()));

    }
    
    public void dispose() {
        InstanceManager.deregister(this, PR3SystemConnectionMemo.class);
        super.dispose();
    }

}


/* @(#)PR3SystemConnectionMemo.java */
