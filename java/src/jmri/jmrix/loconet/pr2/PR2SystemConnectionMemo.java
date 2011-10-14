// PR2SystemConnectionMemo.java

package jmri.jmrix.loconet.pr2;

import jmri.InstanceManager;
import jmri.jmrix.loconet.*;

/**
 * Lightweight class to denote that a PR2 is active
 *
 * @author		Bob Jacobsen  Copyright (C) 2010
 * @version             $Revision$
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
        jmri.InstanceManager.setPowerManager(getPowerPr2Manager());

        /* jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.loconet.LnTurnoutManager()); */

        /* jmri.InstanceManager.setLightManager(new jmri.jmrix.loconet.LnLightManager()); */

        /* jmri.InstanceManager.setSensorManager(new jmri.jmrix.loconet.LnSensorManager()); */

        jmri.InstanceManager.setThrottleManager(getPr2ThrottleManager());

        jmri.InstanceManager.setProgrammerManager(
            new jmri.jmrix.loconet.LnProgrammerManager(getSlotManager(), this));

        /* jmri.InstanceManager.setReporterManager(new jmri.jmrix.loconet.LnReporterManager()); */

    }
    
    private LnPr2PowerManager powerPr2Manager;
    
    public LnPr2PowerManager getPowerPr2Manager() { 
        if (getDisabled())
            return null;
        if (powerPr2Manager == null)
            powerPr2Manager = new jmri.jmrix.loconet.pr2.LnPr2PowerManager(this);
        return powerPr2Manager;
    }
    
    private LnPr2ThrottleManager throttlePr2Manager;
    
    public LnPr2ThrottleManager getPr2ThrottleManager() { 
        if (getDisabled())
            return null;
        if (throttlePr2Manager == null)
            throttlePr2Manager = new jmri.jmrix.loconet.LnPr2ThrottleManager(this);
        return throttlePr2Manager;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Class<?> T) {
        if (getDisabled())
            return null;
        if (T.equals(jmri.PowerManager.class))
            return (T)getPowerPr2Manager();
        if (T.equals(jmri.ThrottleManager.class))
            return (T)getPr2ThrottleManager();
        if (T.equals(jmri.ProgrammerManager.class))
            return (T)getProgrammerManager();
        return null;
    }
    
    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled())
            return false;
        if (type.equals(jmri.PowerManager.class))
            return true;
        if (type.equals(jmri.ThrottleManager.class))
            return true;
        if (type.equals(jmri.ProgrammerManager.class))
            return true;
        return false;
    }
    
    public void dispose() {
        InstanceManager.deregister(this, PR2SystemConnectionMemo.class);
        super.dispose();
    }

}


/* @(#)PR2SystemConnectionMemo.java */
