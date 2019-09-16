package jmri.jmrix.ieee802154.xbee;

import java.util.ResourceBundle;
import jmri.InstanceManager;
import jmri.LightManager;
import jmri.SensorManager;
import jmri.TurnoutManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Lightweight class to denote that a system is active, and provide general
 * information.
 * <p>
 * Objects of specific subtypes are registered in the instance manager to
 * activate their particular system.
 *
 * @author Bob Jacobsen Copyright (C) 2010 copied from NCE into powerline for
 * multiple connections by
 * @author Ken Cameron Copyright (C) 2011 copied from powerline into IEEE802154
 * for multiple connections by
 * @author Paul Bender Copyright (C) 2013
 */
public class XBeeConnectionMemo extends jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo {

    jmri.jmrix.swing.ComponentFactory componentFactory = null;

    public XBeeConnectionMemo() {
        super("Z", "XBee");
        register(); // registers the general type
        InstanceManager.store(this, XBeeConnectionMemo.class); // also register as specific type
    }

    @Override
    protected void init() {
        // create and register the XBeeComponentFactory
        InstanceManager.store(componentFactory = new jmri.jmrix.ieee802154.xbee.swing.XBeeComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);
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
        if (type.equals(jmri.LightManager.class)) {
            return true;
        }
        if (type.equals(jmri.TurnoutManager.class)) {
            return true;
        }
        return false; // nothing, by default
    }

    /**
     * Provide manager by class
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Class<?> T) {
        if (getDisabled()) {
            return null;
        }
        if (T.equals(jmri.SensorManager.class)) {
            return (T) getSensorManager();
        }
        if (T.equals(jmri.LightManager.class)) {
            return (T) getLightManager();
        }
        if (T.equals(jmri.TurnoutManager.class)) {
            return (T) getTurnoutManager();
        }
        return null; // nothing, by default
    }

    /**
     * Configure the common managers for XBee connections. This puts the common
     * manager config in one place.
     */
    @Override
    public void configureManagers() {
        log.debug("Configuring Managers for XBee Connection");

        XBeeTrafficController cont = (XBeeTrafficController) getTrafficController();
        // the start the managers.
        _NodeManager = new XBeeNodeManager(cont);

        setSensorManager(new XBeeSensorManager(this));
        jmri.InstanceManager.setSensorManager(getSensorManager());
        setLightManager(new XBeeLightManager(this));
        jmri.InstanceManager.setLightManager(getLightManager());
        setTurnoutManager(new XBeeTurnoutManager(this));
        jmri.InstanceManager.setTurnoutManager(getTurnoutManager());
    }

    /*
     * get the Node Manager
     */
    public XBeeNodeManager getXBeeNodeManager() {
        return _NodeManager;
    }
    /*
     * set the Node Manager
     */

    public void setXBeeNodeManager(XBeeNodeManager manager) {
        _NodeManager = manager;
    }

    private XBeeNodeManager _NodeManager = null;

    /*
     * Provides access to the SensorManager for this particular connection.
     * NOTE: SensorManager defaults to NULL
     */
    public SensorManager getSensorManager() {
        return sensorManager;

    }

    public void setSensorManager(SensorManager s) {
        sensorManager = s;
    }

    private SensorManager sensorManager = null;

    /*
     * Provides access to the LightManager for this particular connection.
     * NOTE: LightManager defaults to NULL
     */
    public LightManager getLightManager() {
        return lightManager;

    }

    public void setLightManager(LightManager s) {
        lightManager = s;
    }

    private LightManager lightManager = null;

    /*
     * Provides access to the TurnoutManager for this particular connection.
     * NOTE: TurnoutManager defaults to NULL
     */
    public TurnoutManager getTurnoutManager() {
        return turnoutManager;

    }

    public void setTurnoutManager(TurnoutManager s) {
        turnoutManager = s;
    }

    private TurnoutManager turnoutManager = null;

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return ResourceBundle.getBundle("jmri.jmrix.ieee802154.IEEE802154ActionListBundle");
    }

    @Override
    public void dispose() {
        InstanceManager.deregister(this, XBeeConnectionMemo.class);
        super.dispose();
    }

    private final static Logger log = LoggerFactory.getLogger(XBeeConnectionMemo.class);

}



