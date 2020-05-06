package jmri.jmrix.powerline;

import java.util.Comparator;
import java.util.ResourceBundle;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.util.NamedBeanComparator;

/**
 * Lightweight class to denote that a system is active, and provide general
 * information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author Bob Jacobsen Copyright (C) 2010 copied from NCE into Powerline for
 * multiple connections by
 * @author Ken Cameron Copyright (C) 2011
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
     *
     * @return tc
     */
    public SerialTrafficController getTrafficController() {
        return serialTrafficController;
    }
    private SerialTrafficController serialTrafficController;

    public void setTrafficController(SerialTrafficController tc) {
        serialTrafficController = tc;
    }

    /**
     * Provide access to a serialAddress for this particular connection
     *
     * @return serialAddress
     */
    public SerialAddress getSerialAddress() {
        return serialAddress;
    }
    private SerialAddress serialAddress;

    public void setSerialAddress(SerialAddress sa) {
        serialAddress = sa;
    }

    /**
     * Tells which managers this class provides.
     */
    @Override
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

    protected SerialTurnoutManager turnoutManager;
    protected SerialLightManager lightManager;
    protected SerialSensorManager sensorManager;

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

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.powerline.PowerlineActionListBundle");
    }

    @Override
    public <B extends NamedBean> Comparator<B> getNamedBeanComparator(Class<B> type) {
        return new NamedBeanComparator<>();
    }

    @Override
    public void dispose() {
        serialTrafficController = null;
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
