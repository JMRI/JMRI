package jmri.jmrix.jmriclient.json;

import java.util.ResourceBundle;
import jmri.InstanceManager;
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

    protected void configureManagers() {
        log.debug("Configuring managers");
        InstanceManager.store(new JsonClientPowerManager(this), PowerManager.class);
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
