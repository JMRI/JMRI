package jmri.server.json.sensor;

import static jmri.server.json.sensor.JsonSensor.SENSOR;
import static jmri.server.json.sensor.JsonSensor.SENSORS;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.ProvidingManager;
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
public class JsonSensorHttpService extends JsonNamedBeanHttpService<Sensor> {

    public JsonSensorHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public ObjectNode doGet(Sensor sensor, String name, String type, Locale locale, int id) throws JsonException {
        ObjectNode root = this.getNamedBean(sensor, name, type, locale, id); // throws JsonException if sensor == null
        ObjectNode data = root.with(JSON.DATA);
        data.put(JSON.INVERTED, sensor.getInverted());
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
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        Bundle.getMessage(locale, "ErrorInternal", type), id); // NOI18N
        }
        return root;
    }

    @Override
    public ObjectNode doPost(Sensor sensor, String name, String type, JsonNode data, Locale locale, int id) throws JsonException {
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
                    throw new JsonException(400, Bundle.getMessage(locale, "ErrorUnknownState", SENSOR, state), id);
            }
        } catch (JmriException ex) {
            throw new JsonException(500, ex, id);
        }
        return this.doGet(sensor, name, type, locale, id);
    }

    @Override
    protected void doDelete(Sensor bean, String name, String type, JsonNode data, Locale locale, int id)
            throws JsonException {
        deleteBean(bean, name, type, data, locale, id);
    }

    @Override
    public JsonNode doSchema(String type, boolean server, Locale locale, int id) throws JsonException {
        switch (type) {
            case SENSOR:
            case SENSORS:
                return doSchema(type,
                        server,
                        "jmri/server/json/sensor/sensor-server.json",
                        "jmri/server/json/sensor/sensor-client.json",
                        id);
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        Bundle.getMessage(locale, JsonException.ERROR_UNKNOWN_TYPE, type), id);
        }
    }

    @Override
    protected String getType() {
        return SENSOR;
    }

    @Override
    protected ProvidingManager<Sensor> getManager() {
        return InstanceManager.getDefault(SensorManager.class);
    }
}
