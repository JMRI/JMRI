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
        InstanceManager.store(this, XBeeConnectionMemo.class); // also register as specific type
    }

    @Override
    protected void init() {
        // create and register the XBeeComponentFactory
        InstanceManager.store(componentFactory = new jmri.jmrix.ieee802154.xbee.swing.XBeeComponentFactory(this),
                jmri.jmrix.swing.ComponentFactory.class);
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
        setXBeeNodeManager(new XBeeNodeManager(cont));

        setSensorManager(new XBeeSensorManager(this));
        jmri.InstanceManager.setSensorManager(getSensorManager());
        setLightManager(new XBeeLightManager(this));
        jmri.InstanceManager.setLightManager(getLightManager());
        setTurnoutManager(new XBeeTurnoutManager(this));
        jmri.InstanceManager.setTurnoutManager(getTurnoutManager());
        register();
    }

    /*
     * get the Node Manager
     */
    public XBeeNodeManager getXBeeNodeManager() {
        return get(XBeeNodeManager.class);
    }
    /*
     * set the Node Manager
     */

    public void setXBeeNodeManager(XBeeNodeManager manager) {
        store(manager,XBeeNodeManager.class);
    }

    /*
     * Provides access to the SensorManager for this particular connection.
     * NOTE: SensorManager defaults to NULL
     */
    public SensorManager getSensorManager() {
        return get(SensorManager.class);

    }

    public void setSensorManager(SensorManager s) {
        store(s,SensorManager.class);
    }

    /*
     * Provides access to the LightManager for this particular connection.
     * NOTE: LightManager defaults to NULL
     */
    public LightManager getLightManager() {
        return get(LightManager.class);

    }

    public void setLightManager(LightManager s) {
        store(s,LightManager.class);
    }

    /*
     * Provides access to the TurnoutManager for this particular connection.
     * NOTE: TurnoutManager defaults to NULL
     */
    public TurnoutManager getTurnoutManager() {
        return get(TurnoutManager.class);

    }

    public void setTurnoutManager(TurnoutManager s) {
        store(s,TurnoutManager.class);
    }

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



