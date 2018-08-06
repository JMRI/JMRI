package jmri.server.json.light;

import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.PUT;
import static jmri.server.json.light.JsonLight.LIGHT;
import static jmri.server.json.light.JsonLight.LIGHTS;

import com.fasterxml.jackson.databind.JsonNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Light;
import jmri.LightManager;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
import jmri.server.json.JsonSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood
 */
public class JsonLightSocketService extends JsonSocketService<JsonLightHttpService> {

    protected final HashMap<String, LightListener> lightListeners = new HashMap<>();
    private final LightsListener lightsListener = new LightsListener();
    private final static Logger log = LoggerFactory.getLogger(JsonLightSocketService.class);

    public JsonLightSocketService(JsonConnection connection) {
        super(connection, new JsonLightHttpService(connection.getObjectMapper()));
    }

    @Override
    public void onMessage(String type, JsonNode data, String method, Locale locale) throws IOException, JmriException, JsonException {
        this.setLocale(locale);
        String name = data.path(NAME).asText();
        if (method.equals(PUT)) {
            this.connection.sendMessage(this.service.doPut(type, name, data, locale));
        } else {
            this.connection.sendMessage(this.service.doPost(type, name, data, locale));
        }
        if (!this.lightListeners.containsKey(name)) {
            Light light = InstanceManager.getDefault(LightManager.class).getLight(name);
            if (light != null) {
                LightListener listener = new LightListener(light);
                light.addPropertyChangeListener(listener);
                this.lightListeners.put(name, listener);
            }
        }
    }

    @Override
    public void onList(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        this.setLocale(locale);
        this.connection.sendMessage(this.service.doGetList(type, locale));
        log.debug("adding LightsListener");
        InstanceManager.getDefault(LightManager.class).addPropertyChangeListener(lightsListener); //add parent listener
        addListenersToChildren();
    }

    private void addListenersToChildren() {
        InstanceManager.getDefault(LightManager.class).getSystemNameList().stream().forEach((ln) -> { //add listeners to each child (if not already)
            if (!lightListeners.containsKey(ln)) {
                log.debug("adding LightListener for Light '{}'", ln);
                Light l = InstanceManager.getDefault(LightManager.class).getLight(ln);
                if (l != null) {
                    lightListeners.put(ln, new LightListener(l));
                    l.addPropertyChangeListener(this.lightListeners.get(ln));
                }
            }
        });
    }    

    @Override
    public void onClose() {
        lightListeners.values().stream().forEach((light) -> {
            light.light.removePropertyChangeListener(light);
        });
        lightListeners.clear();
    }

    private class LightListener implements PropertyChangeListener {

        protected final Light light;

        public LightListener(Light light) {
            this.light = light;
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
//            if (e.getPropertyName().equals("KnownState")) {
                try {
                    try {
                        connection.sendMessage(service.doGet(LIGHT, this.light.getSystemName(), getLocale()));
                    } catch (JsonException ex) {
                        connection.sendMessage(ex.getJsonMessage());
                    }
                } catch (IOException ex) {
                    // if we get an error, de-register
                    light.removePropertyChangeListener(this);
                    lightListeners.remove(this.light.getSystemName());
                }
//            }
        }
    }
    
    private class LightsListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            log.debug("in LightsListener for '{}' ('{}' => '{}')", evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());

            try {
                try {
                 // send the new list
                    connection.sendMessage(service.doGetList(LIGHTS, getLocale()));
                    //child added or removed, reset listeners
                    if (evt.getPropertyName().equals("length")) { // NOI18N
                        addListenersToChildren();
                    }
                } catch (JsonException ex) {
                    log.warn("json error sending Lights: {}", ex.getJsonMessage());
                    connection.sendMessage(ex.getJsonMessage());
                }
            } catch (IOException ex) {
                // if we get an error, de-register
                log.debug("deregistering lightsListener due to IOException");
                InstanceManager.getDefault(LightManager.class).removePropertyChangeListener(lightsListener);
            }
        }
    }

}
