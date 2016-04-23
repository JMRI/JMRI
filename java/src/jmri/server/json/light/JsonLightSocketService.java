package jmri.server.json.light;

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
import static jmri.server.json.JSON.METHOD;
import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.PUT;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
import jmri.server.json.JsonSocketService;
import static jmri.server.json.light.JsonLightServiceFactory.LIGHT;

/**
 *
 * @author Randall Wood
 */
class JsonLightSocketService extends JsonSocketService {

    private final JsonLightHttpService service;
    private final HashMap<String, LightListener> lights = new HashMap<>();
    private Locale locale;

    public JsonLightSocketService(JsonConnection connection) {
        super(connection);
        this.service = new JsonLightHttpService(connection.getObjectMapper());
    }

    @Override
    public void onMessage(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        this.locale = locale;
        String name = data.path(NAME).asText();
        if (data.path(METHOD).asText().equals(PUT)) {
            this.connection.sendMessage(this.service.doPut(type, name, data, locale));
        } else {
            this.connection.sendMessage(this.service.doPost(type, name, data, locale));
        }
        if (!this.lights.containsKey(name)) {
            Light light = InstanceManager.getDefault(LightManager.class).getLight(name);
            LightListener listener = new LightListener(light);
            light.addPropertyChangeListener(listener);
            this.lights.put(name, listener);
        }
    }

    @Override
    public void onList(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        this.locale = locale;
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
                        connection.sendMessage(service.doGet(LIGHT, this.light.getSystemName(), locale));
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
