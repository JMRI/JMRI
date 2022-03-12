package jmri.server.json.sensor;

import static jmri.server.json.sensor.JsonSensor.SENSOR;
import static jmri.server.json.sensor.JsonSensor.SENSORS;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.ProvidingManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonNamedBeanHttpService;
import jmri.server.json.JsonRequest;

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
    public ObjectNode doGet(Sensor sensor, String name, String type, JsonRequest request) throws JsonException {
        ObjectNode root = this.getNamedBean(sensor, name, getType(), request); // throws JsonException if sensor == null
        ObjectNode data = root.with(JSON.DATA);
        data.put(JSON.INVERTED, sensor.getInverted());
        switch (sensor.getKnownState()) {
            case Sensor.ACTIVE:
                data.put(JSON.STATE, JSON.ACTIVE);
                break;
            case Sensor.INACTIVE:
                data.put(JSON.STATE, JSON.INACTIVE);
                break;
            case NamedBean.INCONSISTENT:
                data.put(JSON.STATE, JSON.INCONSISTENT);
                break;
            case NamedBean.UNKNOWN:
                data.put(JSON.STATE, JSON.UNKNOWN);
                break;
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        Bundle.getMessage(request.locale, "ErrorInternal", type), request.id); // NOI18N
        }
        return root;
    }

    @Override
    public ObjectNode doPost(Sensor sensor, String name, String type, JsonNode data, JsonRequest request) throws JsonException {
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
                    throw new JsonException(400, Bundle.getMessage(request.locale, "ErrorUnknownState", SENSOR, state), request.id);
            }
        } catch (JmriException ex) {
            throw new JsonException(500, ex, request.id);
        }
        return this.doGet(sensor, name, type, request);
    }

    @Override
    protected void doDelete(Sensor bean, String name, String type, JsonNode data, JsonRequest request)
            throws JsonException {
        deleteBean(bean, name, type, data, request);
    }

    @Override
    public JsonNode doSchema(String type, boolean server, JsonRequest request) throws JsonException {
        switch (type) {
            case SENSOR:
            case SENSORS:
                return doSchema(type,
                        server,
                        "jmri/server/json/sensor/sensor-server.json",
                        "jmri/server/json/sensor/sensor-client.json",
                        request.id);
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        Bundle.getMessage(request.locale, JsonException.ERROR_UNKNOWN_TYPE, type), request.id);
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
