package jmri.server.json.light;

import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.PUT;
import static jmri.server.json.light.JsonLight.LIGHT;

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

/**
 *
 * @author Randall Wood
 */
public class JsonLightSocketService extends JsonSocketService<JsonLightHttpService> {

    protected final HashMap<String, LightListener> lights = new HashMap<>();

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
        if (!this.lights.containsKey(name)) {
            Light light = InstanceManager.getDefault(LightManager.class).getLight(name);
            if (light != null) {
                LightListener listener = new LightListener(light);
                light.addPropertyChangeListener(listener);
                this.lights.put(name, listener);
            }
        }
    }

    @Override
    public void onList(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        this.setLocale(locale);
        this.connection.sendMessage(this.service.doGetList(type, locale));
    }

    @Override
    public void onClose() {
        lights.values().stream().forEach((light) -> {
            light.light.removePropertyChangeListener(light);
        });
        lights.clear();
    }

    private class LightListener implements PropertyChangeListener {

        protected final Light light;

        public LightListener(Light light) {
            this.light = light;
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            // If the Commanded State changes, show transition state as "<inconsistent>"
            if (e.getPropertyName().equals("KnownState")) {
                try {
                    try {
                        connection.sendMessage(service.doGet(LIGHT, this.light.getSystemName(), getLocale()));
                    } catch (JsonException ex) {
                        connection.sendMessage(ex.getJsonMessage());
                    }
                } catch (IOException ex) {
                    // if we get an error, de-register
                    light.removePropertyChangeListener(this);
                    lights.remove(this.light.getSystemName());
                }
            }
        }
    }

}
