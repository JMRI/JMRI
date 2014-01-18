//AbstractLightServer.java
package jmri.jmris;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Light;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract interface between the a JMRI Light and a network connection
 *
 * @author Paul Bender Copyright (C) 2010
 * @author Randall Wood Copyright (C) 2013, 2014
 * @version $Revision$
 */
abstract public class AbstractLightServer {

    private final HashMap<String, LightListener> lights;
    static Logger log = LoggerFactory.getLogger(AbstractLightServer.class.getName());

    public AbstractLightServer() {
        lights = new HashMap<String, LightListener>();
    }

    /*
     * Protocol Specific Abstract Functions
     */
    abstract public void sendStatus(String lightName, int Status) throws IOException;

    abstract public void sendErrorStatus(String lightName) throws IOException;

    abstract public void parseStatus(String statusString) throws JmriException, IOException;

    synchronized protected void addLightToList(String lightName) {
        if (!lights.containsKey(lightName)) {
            lights.put(lightName, new LightListener(lightName));
            InstanceManager.lightManagerInstance().getLight(lightName).addPropertyChangeListener(lights.get(lightName));
        }
    }

    synchronized protected void removeLightFromList(String lightName) {
        if (lights.containsKey(lightName)) {
            InstanceManager.lightManagerInstance().getLight(lightName).removePropertyChangeListener(lights.get(lightName));
            lights.remove(lightName);
        }
    }

    public Light initLight(String lightName) {
        Light light = InstanceManager.lightManagerInstance().provideLight(lightName);
        this.addLightToList(lightName);
        return light;
    }

    public void lightOff(String lightName) {
        Light light;
        // load address from switchAddrTextField
        try {

            addLightToList(lightName);
            light = InstanceManager.lightManagerInstance().getLight(lightName);
            if (light == null) {
                log.error("Light {} is not available", lightName);
            } else {
                log.debug("about to command OFF");
                // and set state to OFF
                light.setState(Light.OFF);
            }
        } catch (Exception ex) {
            log.error("light off", ex);
        }
    }

    public void lightOn(String lightName) {
        Light light;
        // load address from switchAddrTextField
        try {
            addLightToList(lightName);
            light = InstanceManager.lightManagerInstance().getLight(lightName);

            if (light == null) {
                log.error("Light {} is not available", lightName);
            } else {
                log.debug("about to command ON");
                // and set state to ON
                light.setState(Light.ON);
            }
        } catch (Exception ex) {
            log.error("light on", ex);
        }
    }

    public void dispose() {
        for (Map.Entry<String, LightListener> light : this.lights.entrySet()) {
            InstanceManager.lightManagerInstance().getLight(light.getKey()).removePropertyChangeListener(light.getValue());
        }
        this.lights.clear();
    }

    class LightListener implements PropertyChangeListener {

        LightListener(String lightName) {
            name = lightName;
            light = InstanceManager.lightManagerInstance().getLight(lightName);
        }

        // update state as state of light changes
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            // If the Commanded State changes, show transition state as "<inconsistent>"
            if (e.getPropertyName().equals("KnownState")) {
                int now = ((Integer) e.getNewValue()).intValue();
                try {
                    sendStatus(name, now);
                } catch (IOException ie) {
                    log.debug("Error Sending Status");
                    // if we get an error, de-register
                    light.removePropertyChangeListener(this);
                    removeLightFromList(name);
                }
            }
        }
        Light light = null;
        String name = null;
    }
}
