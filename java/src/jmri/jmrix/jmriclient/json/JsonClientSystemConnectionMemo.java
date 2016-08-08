package jmri.jmrix.jmriclient.json;

import java.util.ResourceBundle;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.LightManager;
import jmri.PowerManager;
import jmri.server.json.JSON;
import jmri.jmrix.SystemConnectionMemo;
import jmri.jmrix.jmriclient.json.swing.JsonClientComponentFactory;
import jmri.jmrix.swing.ComponentFactory;
import jmri.util.node.NodeIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood 2014
 */
public class JsonClientSystemConnectionMemo extends SystemConnectionMemo {

    private final ComponentFactory componentFactory;
    private JsonClientTrafficController trafficController;
    private JsonClientLightManager lightManager = null;
    private JsonClientPowerManager powerManager = null;
    private String transmitPrefix = null;
    private String nodeIdentity = NodeIdentity.identity();
    private final static Logger log = LoggerFactory.getLogger(JsonClientSystemConnectionMemo.class);

    public JsonClientSystemConnectionMemo() {
        this(new JsonClientTrafficController());
    }

    public JsonClientSystemConnectionMemo(JsonClientTrafficController trafficController) {
        super("json", "JSON Client");
        this.trafficController = trafficController;
        this.componentFactory = new JsonClientComponentFactory(this);
        this.register(); // registers general type
        this.registerInstance(); // registers specific type
        // create and register the JMRIClientComponentFactory
        InstanceManager.store(this.componentFactory, ComponentFactory.class);
    }

    private void registerInstance() {
        InstanceManager.store(this, JsonClientSystemConnectionMemo.class);
    }

    @Override
    public void dispose() {
        this.trafficController = null;
        InstanceManager.deregister(this, JsonClientSystemConnectionMemo.class);
        if (this.componentFactory != null) {
            InstanceManager.deregister(this.componentFactory, ComponentFactory.class);
        }
        super.dispose();
    }

    @Override
    protected ResourceBundle getActionModelResourceBundle() {
        return null;
    }

    /*
     * Register managers to listen to the trafficController here instead of
     * in their constructors so the constructors cannot leak their objects
     * before returning. See http://stackoverflow.com/a/23069096/176160
     */
    protected void configureManagers() {
        log.debug("Configuring managers");
        InstanceManager.setLightManager(this.getLightManager());
        InstanceManager.store(this.getPowerManager(), PowerManager.class);
        // make initial information requests from server to initialize listeners
        this.getList(JSON.LIGHTS, this.lightManager);
        try {
            // setting power to unknown will cause a JSON server to return current power state
            powerManager.setPower(PowerManager.UNKNOWN);
        } catch (JmriException ex) {
            log.error("Unable to request system state: {}", ex.getMessage());
            log.debug("Complete exception:\n", ex);
        }
    }

    private JsonClientLightManager getLightManager() {
        if (this.lightManager == null) {
            this.lightManager = new JsonClientLightManager(this);
            this.getTrafficController().addJsonClientListener(this.lightManager);
        }
        return this.lightManager;
    }

    private JsonClientPowerManager getPowerManager() {
        if (this.powerManager == null) {
            this.powerManager = new JsonClientPowerManager(this);
            this.getTrafficController().addJsonClientListener(this.powerManager);
        }
        return this.powerManager;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Class<?> T) {
        if (getDisabled()) {
            return null;
        }
        if (T.equals(jmri.PowerManager.class)) {
            return (T) getPowerManager();
        }
        if (T.equals(jmri.LightManager.class)) {
            return (T) getLightManager();
        }
        return null; // nothing, by default
    }

    @Override
    public boolean provides(Class<?> c) {
        if (getDisabled()) {
            return false;
        } else if (c.equals(LightManager.class)) {
            return true;
        } else {
            return c.equals(PowerManager.class);
        }
    }

    /**
     * @return the trafficController
     */
    public JsonClientTrafficController getTrafficController() {
        return trafficController;
    }

    /**
     * @param trafficController the trafficController to set
     */
    public void setTrafficController(JsonClientTrafficController trafficController) {
        this.trafficController = trafficController;
    }

    public void setTransmitPrefix(String text) {
        this.transmitPrefix = text;
    }

    public String getTransmitPrefix() {
        if (transmitPrefix == null) {
            return this.getSystemPrefix();
        }
        return this.transmitPrefix;
    }

    public String getNodeIdentity() {
        if (this.nodeIdentity == null) {
            return NodeIdentity.identity();
        }
        return this.nodeIdentity;
    }

    public void setNodeIdentity(String value) {
        this.nodeIdentity = value;
        log.debug("Connection node identity is {}", this.nodeIdentity);
    }

    private void getList(String type, JsonClientListener listener) {
        this.getTrafficController().sendJsonClientMessage(
                new JsonClientMessage(
                        this.getTrafficController().mapper.createObjectNode().put(JSON.LIST, type)
                ), listener);
    }
}
