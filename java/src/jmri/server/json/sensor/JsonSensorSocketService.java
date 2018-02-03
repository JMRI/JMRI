package jmri.server.json.sensor;

import static jmri.server.json.sensor.JsonSensor.SENSOR;
import static jmri.server.json.sensor.JsonSensor.SENSORS;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON Socket service for {@link jmri.Sensor}s.
 *
 * @author Randall Wood
 */
public class JsonSensorSocketService extends JsonSocketService<JsonSensorHttpService> {

    private final HashMap<String, SensorListener> sensorListeners = new HashMap<>();
    private final SensorsListener sensorsListener = new SensorsListener();
    private final static Logger log = LoggerFactory.getLogger(JsonSensorSocketService.class);


    public JsonSensorSocketService(JsonConnection connection) {
        super(connection, new JsonSensorHttpService(connection.getObjectMapper()));
    }

    @Override
    public void onMessage(String type, JsonNode data, String method, Locale locale) throws IOException, JmriException, JsonException {
        this.setLocale(locale);
        String name = data.path(JSON.NAME).asText();
        if (method.equals(JSON.PUT)) {
            this.connection.sendMessage(this.service.doPut(type, name, data, locale));
        } else {
            this.connection.sendMessage(this.service.doPost(type, name, data, locale));
        }
        if (!this.sensorListeners.containsKey(name)) {
            Sensor sensor = InstanceManager.getDefault(SensorManager.class).getSensor(name);
            if (sensor != null) {
                SensorListener listener = new SensorListener(sensor);
                sensor.addPropertyChangeListener(listener);
                this.sensorListeners.put(name, listener);
            }
        }
    }

    @Override
    public void onList(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        this.setLocale(locale);
        this.connection.sendMessage(this.service.doGetList(type, locale));
        log.debug("adding SensorsListener");
        InstanceManager.getDefault(SensorManager.class).addPropertyChangeListener(sensorsListener); //add parent listener
        addListenersToChildren();
    }

    private void addListenersToChildren() {
        InstanceManager.getDefault(SensorManager.class).getSystemNameList().stream().forEach((sn) -> { //add listeners to each child (if not already)
            if (!sensorListeners.containsKey(sn)) {
                log.debug("adding SensorListener for Sensor {}", sn);
                Sensor s = InstanceManager.getDefault(SensorManager.class).getSensor(sn);
                if (s != null) {
                    sensorListeners.put(sn, new SensorListener(s));
                    s.addPropertyChangeListener(this.sensorListeners.get(sn));
                }
            }
        });
    }

    @Override
    public void onClose() {
        sensorListeners.values().stream().forEach((listener) -> {
            listener.sensor.removePropertyChangeListener(listener);
        });
        sensorListeners.clear();
        InstanceManager.getDefault(SensorManager.class).removePropertyChangeListener(sensorsListener);
    }

    private class SensorListener implements PropertyChangeListener {

        protected final Sensor sensor;

        public SensorListener(Sensor sensor) {
            this.sensor = sensor;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            log.debug("in SensorListener for '{}' '{}' ('{}'=>'{}')", this.sensor.getSystemName(), evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
            try {
                try {
                    connection.sendMessage(service.doGet(SENSOR, this.sensor.getSystemName(), getLocale()));
                } catch (JsonException ex) {
                    connection.sendMessage(ex.getJsonMessage());
                }
            } catch (IOException ex) {
                // if we get an error, de-register
                sensor.removePropertyChangeListener(this);
                sensorListeners.remove(this.sensor.getSystemName());
            }
        }
    }

    private class SensorsListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            log.debug("in SensorsListener for '{}' ('{}' => '{}')", evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());

            try {
                try {
                 // send the new list
                    connection.sendMessage(service.doGetList(SENSORS, getLocale()));
                    //child added or removed, reset listeners
                    if (evt.getPropertyName().equals("length")) { // NOI18N
                        addListenersToChildren();
                    }
                } catch (JsonException ex) {
                    log.warn("json error sending Sensors: {}", ex.getJsonMessage());
                    connection.sendMessage(ex.getJsonMessage());
                }
            } catch (IOException ex) {
                // if we get an error, de-register
                log.debug("deregistering sensorsListener due to IOException");
                InstanceManager.getDefault(SensorManager.class).removePropertyChangeListener(sensorsListener);
            }
        }
    }

}
