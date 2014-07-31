package jmri.jmrix.jmriclient.json;

import java.util.ResourceBundle;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.jmrix.SystemConnectionMemo;
import jmri.jmrix.jmriclient.json.swing.JsonClientComponentFactory;
import jmri.jmrix.swing.ComponentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood 2014
 */
public class JsonClientSystemConnectionMemo extends SystemConnectionMemo {

    private final ComponentFactory componentFactory;
    private JsonClientTrafficController trafficController;
    private String transmitPrefix = null;
    private final static Logger log = LoggerFactory.getLogger(JsonClientSystemConnectionMemo.class);

    public JsonClientSystemConnectionMemo() {
        this(new JsonClientTrafficController());
    }

    public JsonClientSystemConnectionMemo(JsonClientTrafficController trafficController) {
        super("J", "JMRIClient");
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
     * in their constructors so the constructors do not leak their objects
     * before returning. See http://stackoverflow.com/a/23069096/176160
     */
    protected void configureManagers() {
        log.debug("Configuring managers");
        JsonClientPowerManager powerManager = new JsonClientPowerManager(this);
        this.getTrafficController().addJsonClientListener(powerManager);
        InstanceManager.store(powerManager, PowerManager.class);
        // trigger current state requests as appropriate
        try {
            // setting power to unknown will cause a JSON server to return current power state
            powerManager.setPower(PowerManager.UNKNOWN);
        } catch (JmriException ex) {
            log.error("Unable to request system state: {}", ex.getMessage());
            log.debug("Complete exception:\n", ex);
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

}
