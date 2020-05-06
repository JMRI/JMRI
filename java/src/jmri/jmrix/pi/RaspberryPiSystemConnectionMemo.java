package jmri.jmrix.pi;

import java.util.Comparator;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import jmri.InstanceManager;
import jmri.LightManager;
import jmri.NamedBean;
import jmri.SensorManager;
import jmri.TurnoutManager;
import jmri.util.NamedBeanComparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lightweight class to denote that a system is active
 * and provide general information.
 * <p>
 * Objects of specific subtypes are registered in the 
 * instance manager to activate their particular system.
 *
 * @author   Paul Bender Copyright (C) 2015
 */
public class RaspberryPiSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public RaspberryPiSystemConnectionMemo(@Nonnull String prefix, @Nonnull String name) {
        super(prefix, name); // NOI18N

        register(); // registers general type
        InstanceManager.store(this, RaspberryPiSystemConnectionMemo.class); // also register as specific type
        log.debug("Created RaspberryPiSystemConnectionMemo");
    }

    public RaspberryPiSystemConnectionMemo(){
        this("P", "RaspberryPi");
    }

    /*
     * Provides access to the SensorManager for this particular connection.
     * NOTE: SensorManager defaults to NULL
     */
    public SensorManager getSensorManager(){
        return sensorManager;

    }
    public void setSensorManager(SensorManager s){
         InstanceManager.setSensorManager(s);
         sensorManager = s;
    }

    private SensorManager sensorManager = null;

    /*
     * Provides access to the TurnoutManager for this particular connection.
     * NOTE: TurnoutManager defaults to NULL
     */
    public TurnoutManager getTurnoutManager(){
        return turnoutManager;
    }

    public void setTurnoutManager(TurnoutManager t){
         InstanceManager.setTurnoutManager(t);
         turnoutManager = t;
    }

    private TurnoutManager turnoutManager = null;

    /*
     * Provides access to the LightManager for this particular connection.
     * NOTE: Light manager defaults to NULL
     */
    public LightManager getLightManager(){
        return lightManager;

    }
    public void setLightManager(LightManager l){
         lightManager = l;
    }

    private LightManager lightManager = null;

    public void configureManagers(){
       setTurnoutManager(new RaspberryPiTurnoutManager(this));
       setSensorManager(new RaspberryPiSensorManager(this));
    }
    
    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled()) {
            return false;
        }
        if (type.equals(jmri.SensorManager.class)) {
            return true;
        } else if (type.equals(jmri.TurnoutManager.class)) {
            return true;
        } else if (type.equals(jmri.LightManager.class)) {
            return false;  // implement LightManager later.
        } else {
            return false; // nothing, by default
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Class<?> T) {
         if (getDisabled()) {
             return null;
         }
         if (T.equals(jmri.SensorManager.class)) {
             return (T) getSensorManager();
         }
         if (T.equals(jmri.TurnoutManager.class)) {
             return (T) getTurnoutManager();
         }
         if (T.equals(jmri.LightManager.class)) {
             return (T) getLightManager();
         }
         return null; // nothing, by default
     }

    @Override
    protected ResourceBundle getActionModelResourceBundle(){
        return ResourceBundle.getBundle("jmri.jmrix.pi.RaspberryPiActionListBundle");
    }

    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanComparator<>();
    }

    @Override
    public void dispose() {
        InstanceManager.deregister(this, RaspberryPiSystemConnectionMemo.class);
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(RaspberryPiSystemConnectionMemo.class);

}
