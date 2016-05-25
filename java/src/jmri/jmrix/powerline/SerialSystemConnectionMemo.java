// SerialSystemConnectionMemo.java
package jmri.jmrix.powerline;

import java.util.ResourceBundle;
import jmri.InstanceManager;
import jmri.ProgrammerManager;

/**
 * Lightweight class to denote that a system is active, and provide general
 * information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author	Bob Jacobsen Copyright (C) 2010 copied from NCE into Powerline for
 * multiple connections by
 * @author	Ken Cameron Copyright (C) 2011
 * @version $Revision$
 */
public class SerialSystemConnectionMemo extends jmri.jmrix.SystemConnectionMemo {

    public SerialSystemConnectionMemo() {
        super("P", "Powerline");
        register(); // registers general type
        InstanceManager.store(this, SerialSystemConnectionMemo.class); // also register as specific type

        // create and register the ComponentFactory
        InstanceManager.store(componentFactory = new jmri.jmrix.powerline.swing.PowerlineComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);
    }

    jmri.jmrix.swing.ComponentFactory componentFactory = null;

    /**
     * Provides access to the TrafficController for this particular connection.
     */
    public SerialTrafficController getTrafficController() {
        return SerialTrafficController;
    }
    private SerialTrafficController SerialTrafficController;

    public void setTrafficController(SerialTrafficController tc) {
        SerialTrafficController = tc;
    }

    /**
     * Provide access to a SerialAddress for this particular connection
     */
    public SerialAddress getSerialAddress() {
        return SerialAddress;
    }
    private SerialAddress SerialAddress;

    public void setSerialAddress(SerialAddress sa) {
        SerialAddress = sa;
    }

    /**
     * Always null as powerline doesn't have a programmer
     */
    @SuppressWarnings("deprecation")
    public ProgrammerManager getProgrammerManager() {
        //Do not want to return a programmer ever
        return null;
    }

    @SuppressWarnings("deprecation")
    public void setProgrammerManager(ProgrammerManager p) {
        // no programmer supported, should I throw an Exception??
    }

    /**
     * Tells which managers this provides by class
     */
    public boolean provides(Class<?> type) {
        if (getDisabled()) {
            return false;
        }
        if (type.equals(jmri.SensorManager.class)) {
            return true;
        }
        if (type.equals(jmri.TurnoutManager.class)) {
            return true;
        }
        if (type.equals(jmri.LightManager.class)) {
            return true;
        }
        return false; // nothing, by default
    }

    /**
     * Provide manager by class
     */
    @SuppressWarnings("unchecked")
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

    private SerialTurnoutManager turnoutManager;
    private SerialLightManager lightManager;
    private SerialSensorManager sensorManager;

    /**
     * Configure the common managers for Powerline connections. This puts the
     * common manager config in one place.
     */
    public void configureManagers() {
        // now does nothing here, it's done by the specific class
    }

    public SerialTurnoutManager getTurnoutManager() {
        return turnoutManager;
    }

    public SerialLightManager getLightManager() {
        return lightManager;
    }

    public SerialSensorManager getSensorManager() {
        return sensorManager;
    }

    public void setTurnoutManager(SerialTurnoutManager m) {
        turnoutManager = m;
    }

    public void setLightManager(SerialLightManager m) {
        lightManager = m;
    }

    public void setSensorManager(SerialSensorManager m) {
        sensorManager = m;
    }

    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.powerline.PowerlineActionListBundle");
    }

    public void dispose() {
        SerialTrafficController = null;
        InstanceManager.deregister(this, SerialSystemConnectionMemo.class);
        if (turnoutManager != null) {
            InstanceManager.deregister(turnoutManager, jmri.jmrix.powerline.SerialTurnoutManager.class);
        }
        if (lightManager != null) {
            InstanceManager.deregister(lightManager, jmri.jmrix.powerline.SerialLightManager.class);
        }
        if (sensorManager != null) {
            InstanceManager.deregister(sensorManager, jmri.jmrix.powerline.SerialSensorManager.class);
        }
        super.dispose();
    }

}


/* @(#)SerialSystemConnectionMemo.java */
