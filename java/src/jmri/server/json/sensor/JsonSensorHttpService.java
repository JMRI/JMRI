package jmri.server.json.sensor;

import static jmri.server.json.sensor.JsonSensor.SENSOR;
import static jmri.server.json.sensor.JsonSensor.SENSORS;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonNamedBeanHttpService;

/**
 * JSON HTTP Service for {@link jmri.Sensor}s.
 *
 * @author Randall Wood Copyright 2016, 2018
 */
public class JsonSensorHttpService extends JsonNamedBeanHttpService {

    public JsonSensorHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public JsonNode doGet(String type, String name, Locale locale) throws JsonException {
        ObjectNode root = mapper.createObjectNode();
        root.put(JSON.TYPE, SENSOR);
        Sensor sensor = InstanceManager.getDefault(SensorManager.class).getSensor(name);
        ObjectNode data = this.getNamedBean(sensor, name, type, locale); // throws JsonException if sensor == null
        if (sensor != null) {
            root.set(JSON.DATA, data);
            switch (sensor.getKnownState()) {
                case Sensor.ACTIVE:
                    data.put(JSON.STATE, JSON.ACTIVE);
                    break;
                case Sensor.INACTIVE:
                    data.put(JSON.STATE, JSON.INACTIVE);
                    break;
                case Sensor.INCONSISTENT:
                    data.put(JSON.STATE, JSON.INCONSISTENT);
                    break;
                case Sensor.UNKNOWN:
                    data.put(JSON.STATE, JSON.UNKNOWN);
                    break;
                default:
                    throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(locale, "ErrorInternal", type)); // NOI18N
            }
        }
        return root;
    }

    @Override
    public JsonNode doPost(String type, String name, JsonNode data, Locale locale) throws JsonException {
        Sensor sensor = InstanceManager.getDefault(SensorManager.class).getSensor(name);
        if (sensor == null) {
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", SENSOR, name));
        }
        this.postNamedBean(sensor, data, name, type, locale);
        if (data.path(JSON.INVERTED).isBoolean()) {
            sensor.setInverted(data.path(JSON.INVERTED).asBoolean());
        }
        int state = data.path(JSON.STATE).asInt(JSON.UNKNOWN);
        try {
            switch (state) {
                case JSON.ACTIVE:
                    sensor.setKnownState(Sensor.ACTIVE);
                    break;
                case JSON.INACTIVE:
                    sensor.setKnownState(Sensor.INACTIVE);
                    break;
                case JSON.INCONSISTENT:
                case JSON.UNKNOWN:
                    // silently ignore
                    break;
                default:
                    throw new JsonException(400, Bundle.getMessage(locale, "ErrorUnknownState", SENSOR, state));
            }
        } catch (JmriException ex) {
            throw new JsonException(500, ex);
        }
        return this.doGet(type, name, locale);
    }

    @Override
    public JsonNode doPut(String type, String name, JsonNode data, Locale locale) throws JsonException {
        try {
            InstanceManager.getDefault(SensorManager.class).provideSensor(name);
        } catch (Exception ex) {
            throw new JsonException(500, Bundle.getMessage(locale, "ErrorCreatingObject", SENSOR, name));
        }
        return this.doPost(type, name, data, locale);
    }

    @Override
    public ArrayNode doGetList(String type, Locale locale) throws JsonException {
        ArrayNode root = this.mapper.createArrayNode();
        for (String name : InstanceManager.getDefault(SensorManager.class).getSystemNameList()) {
            root.add(this.doGet(SENSOR, name, locale));
        }
        return root;

    }

    @Override
    public JsonNode doSchema(String type, boolean server, Locale locale) throws JsonException {
        switch (type) {
            case SENSOR:
            case SENSORS:
                return doSchema(type,
                        server,
                        "jmri/server/json/sensor/sensor-server.json",
                        "jmri/server/json/sensor/sensor-client.json");
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(locale, "ErrorUnknownType", type));
        }
    }
}
