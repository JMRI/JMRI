//RaspberryPiSystemConnectionMemo.java

package jmri.jmrix.pi;

import java.util.ResourceBundle;
import jmri.InstanceManager;
import jmri.LightManager;
import jmri.SensorManager;
import jmri.TurnoutManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lightweight class to denote that a system is active
 * and provide general information
 * <p>
 * Objects of specific subtypes are registered in the 
 * instance manager to activate their particular system.
 *
 * @author   Paul Bender Copyright (C) 2015
 * @version  $Revision$
 */

public class RaspberryPiSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

   public RaspberryPiSystemConnectionMemo(){
     super("PI","RaspberryPi");
     register(); // registers general type
     InstanceManager.store(this,RaspberryPiSystemConnectionMemo.class); // also register as specific type
     if(log.isDebugEnabled()) log.debug("Created RaspberryPiSystemConnectionMemo");
   }


    /*
     * Provides access to the Sensor Manager for this particular connection.
     * NOTE: Sensor manager defaults to NULL
     */
    public SensorManager getSensorManager(){
        return sensorManager;

    }
    public void setSensorManager(SensorManager s){
         InstanceManager.setSensorManager(s);
         sensorManager = s;
    }

    private SensorManager sensorManager=null;

    /*
     * Provides access to the Turnout Manager for this particular connection.
     * NOTE: Turnout manager defaults to NULL
     */
    public TurnoutManager getTurnoutManager(){
        return turnoutManager;
    }

    public void setTurnoutManager(TurnoutManager t){
         InstanceManager.setTurnoutManager(t);
         turnoutManager = t;
    }

    private TurnoutManager turnoutManager=null;

    /*
     * Provides access to the Light Manager for this particular connection.
     * NOTE: Light manager defaults to NULL
     */
    public LightManager getLightManager(){
        return lightManager;

    }
    public void setLightManager(LightManager l){
         lightManager = l;
    }

    private LightManager lightManager=null;

    public void configureManagers(){
       setTurnoutManager(new RaspberryPiTurnoutManager(getSystemPrefix()));
       setSensorManager(new RaspberryPiSensorManager(getSystemPrefix()));
    }
    
    public boolean provides(Class<?> type) {
        if (getDisabled())
            return false;
        if (type.equals(jmri.SensorManager.class))
            return true;
        else if (type.equals(jmri.TurnoutManager.class))
            return true;
        else if (type.equals(jmri.LightManager.class))
            return false; // implement light manager later.
        else return false; // nothing, by default
    }

     @SuppressWarnings("unchecked")
     public <T> T get(Class<?> T) {
         if (getDisabled())
             return null;
         if (T.equals(jmri.SensorManager.class))
             return (T)getSensorManager();
         if (T.equals(jmri.TurnoutManager.class))
             return (T)getTurnoutManager();
         if (T.equals(jmri.LightManager.class))
             return (T)getLightManager();
         return null; // nothing, by default
     }

    protected ResourceBundle getActionModelResourceBundle(){
        return ResourceBundle.getBundle("jmri.jmrix.pi.RaspberryPiActionListBundle");
    }

    public void dispose() {
        InstanceManager.deregister(this, RaspberryPiSystemConnectionMemo.class);
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(RaspberryPiSystemConnectionMemo.class.getName());


}
/* @(#)XNetSystemConnectionMemo.java */
