//AnymaDMX_SystemConnectionMemo.java
package jmri.jmrix.anyma_dmx;

import java.util.ResourceBundle;
import jmri.InstanceManager;
import jmri.LightManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lightweight class to denote that a system is active and provide general
 * information
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author Paul Bender Copyright (C) 2015
 * @author George Warner Copyright (C) 2017
 * @since 4.9.6
 */
public class AnymaDMX_SystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public AnymaDMX_SystemConnectionMemo() {
        super("DX", "AnymaDMX");
        register(); // registers general type
        InstanceManager.store(this, AnymaDMX_SystemConnectionMemo.class); // also register as specific type
        if (log.isDebugEnabled()) {
            log.info("*Created AnymaDMX_SystemConnectionMemo");
        }
    }

    /*
     * Provides access to the Light Manager for this particular connection.
     * NOTE: Light manager defaults to NULL
     */
    private LightManager lightManager = null;

    public LightManager getLightManager() {
        return lightManager;

    }

    public void setLightManager(LightManager l) {
        lightManager = l;
    }

    public void configureManagers() {
//        setTurnoutManager(new AnymaDMX_TurnoutManager(getSystemPrefix()));
//        setSensorManager(new AnymaDMX_SensorManager(getSystemPrefix()));
//        setLightManager(new AnymaDMX_LightManager(getSystemPrefix()));
    }

    @Override
    public boolean provides(Class<?> type) {
        if (getDisabled()) {
            return false;
        }
        if (type.equals(jmri.LightManager.class)) {
            return false; // implement light manager later.
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
        if (T.equals(jmri.LightManager.class)) {
            return (T) getLightManager();
        }
        return null; // nothing, by default
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.anyma_dmx.AnymaDMX_ActionListBundle");
    }

    @Override
    public void dispose() {
        InstanceManager.deregister(this, AnymaDMX_SystemConnectionMemo.class);
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(AnymaDMX_SystemConnectionMemo.class);

}
