package jmri.server.json.sensor;

import com.fasterxml.jackson.databind.JsonNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
import jmri.server.json.JsonSocketService;
import static jmri.server.json.sensor.JsonSensorServiceFactory.SENSOR;

/**
 * JSON Socket service for {@link jmri.Sensor}s.
 * 
 * @author Randall Wood
 */
public class JsonSensorSocketService extends JsonSocketService {

    private final JsonSensorHttpService service;
    private final HashMap<String, SensorListener> sensors = new HashMap<>();
    private Locale locale;

    public JsonSensorSocketService(JsonConnection connection) {
        super(connection);
        this.service = new JsonSensorHttpService(connection.getObjectMapper());
    }

    @Override
    public void onMessage(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        this.locale = locale;
        String name = data.path(JSON.NAME).asText();
        if (data.path(JSON.METHOD).asText().equals(JSON.PUT)) {
            this.connection.sendMessage(this.service.doPut(type, name, data, locale));
        } else {
            this.connection.sendMessage(this.service.doPost(type, name, data, locale));
        }
        if (!this.sensors.containsKey(name)) {
            Sensor sensor = InstanceManager.getDefault(SensorManager.class).getSensor(name);
            if (sensor != null) {
                SensorListener listener = new SensorListener(sensor);
                sensor.addPropertyChangeListener(listener);
                this.sensors.put(name, listener);
            }
        }
    }

    @Override
    public void onList(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        this.locale = locale;
        this.connection.sendMessage(this.service.doGetList(type, locale));
    }

    @Override
    public void onClose() {
        sensors.values().stream().forEach((sensor) -> {
            sensor.sensor.removePropertyChangeListener(sensor);
        });
        sensors.clear();
    }

    private class SensorListener implements PropertyChangeListener {

        protected final Sensor sensor;

        public SensorListener(Sensor sensor) {
            this.sensor = sensor;
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            // If the Commanded State changes, show transition state as "<inconsistent>"
            if (e.getPropertyName().equals("KnownState")) {
                try {
                    try {
                        connection.sendMessage(service.doGet(SENSOR, this.sensor.getSystemName(), locale));
                    } catch (JsonException ex) {
                        connection.sendMessage(ex.getJsonMessage());
                    }
                } catch (IOException ex) {
                    // if we get an error, de-register
                    sensor.removePropertyChangeListener(this);
                    sensors.remove(this.sensor.getSystemName());
                }
            }
        }
    }

}
