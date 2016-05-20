//AbstractLightServer.java
package jmri.jmris;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Light;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract interface between the a JMRI Light and a network connection
 *
 * @author Paul Bender Copyright (C) 2010
 * @author Randall Wood Copyright (C) 2013
 * @version $Revision$
 */
abstract public class AbstractLightServer {

    public AbstractLightServer() {
        lights = new ArrayList<String>();
    }

    /*
     * Protocol Specific Abstract Functions
     */
    abstract public void sendStatus(String lightName, int Status) throws IOException;

    abstract public void sendErrorStatus(String lightName) throws IOException;

    abstract public void parseStatus(String statusString) throws JmriException, IOException;

    synchronized protected void addLightToList(String lightName) {
        if (!lights.contains(lightName)) {
            lights.add(lightName);
            InstanceManager.lightManagerInstance().getLight(lightName)
                    .addPropertyChangeListener(new LightListener(lightName));
        }
    }

    synchronized protected void removeLightFromList(String lightName) {
        if (lights.contains(lightName)) {
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
    protected ArrayList<String> lights = null;
    String newState = "";
    static Logger log = LoggerFactory.getLogger(AbstractLightServer.class.getName());
}
