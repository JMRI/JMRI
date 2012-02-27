// UhlenbrockSystemConnectionMemo.java

package jmri.jmrix.loconet.uhlenbrock;

import jmri.InstanceManager;
import jmri.jmrix.loconet.*;

/**
 * Lightweight class to denote that a Uhlenbrock is active
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
    
    
   /**
     * Configure the subset of LocoNet managers valid for the Uhlenbrock.
     */
    public void configureManagers() {
        jmri.InstanceManager.setPowerManager(
            getPowerManager());

        jmri.InstanceManager.setTurnoutManager(
            getTurnoutManager());

        jmri.InstanceManager.setLightManager(
            getLightManager());

        jmri.InstanceManager.setSensorManager(
            getSensorManager());

        jmri.InstanceManager.setThrottleManager(
            getThrottleManager());
        
        InstanceManager.setConsistManager(
            getConsistManager());

        InstanceManager.addClockControl(
            getClockControl());

        /*jmri.InstanceManager.setProgrammerManager(
            new jmri.jmrix.loconet.LnProgrammerManager(getSlotManager(), this));*/

        /*jmri.InstanceManager.setReporterManager(new jmri.jmrix.loconet.LnReporterManager());*/

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
    
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Class<?> T) {
        if (getDisabled())
            return null;
        if (T.equals(jmri.ThrottleManager.class))
            return (T)getThrottleManager();
        if (T.equals(jmri.PowerManager.class))
            return (T)getPowerManager();
        if (T.equals(jmri.SensorManager.class))
            return (T)getSensorManager();
        if (T.equals(jmri.TurnoutManager.class))
            return (T)getTurnoutManager();
        if (T.equals(jmri.LightManager.class))
            return (T)getLightManager();
        if (T.equals(jmri.ClockControl.class))
            return (T)getClockControl();
        if (T.equals(jmri.ConsistManager.class))
            return (T)getConsistManager();
        if (T.equals(jmri.CommandStation.class))
            return (T)getSlotManager();
        return null;
    }
    
    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled())
            return false;
        if (type.equals(jmri.ThrottleManager.class))
            return true;
        if (type.equals(jmri.PowerManager.class))
            return true;
        if (type.equals(jmri.SensorManager.class))
            return true;
        if (type.equals(jmri.TurnoutManager.class))
            return true;
        if (type.equals(jmri.LightManager.class))
            return true;
        if (type.equals(jmri.ConsistManager.class))
            return true;
        if (type.equals(jmri.ClockControl.class))
            return true;
        if (type.equals(jmri.CommandStation.class))
            return true;
        return false;
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
