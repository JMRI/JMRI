package jmri.server.json.turnout;

import static jmri.server.json.JSON.CLOSED;
import static jmri.server.json.JSON.INCONSISTENT;
import static jmri.server.json.JSON.INVERTED;
import static jmri.server.json.JSON.STATE;
import static jmri.server.json.JSON.THROWN;
import static jmri.server.json.JSON.UNKNOWN;
import static jmri.server.json.sensor.JsonSensor.SENSOR;
import static jmri.server.json.turnout.JsonTurnout.FEEDBACK_MODE;
import static jmri.server.json.turnout.JsonTurnout.FEEDBACK_MODES;
import static jmri.server.json.turnout.JsonTurnout.TURNOUT;
import static jmri.server.json.turnout.JsonTurnout.TURNOUTS;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.ProvidingManager;
import jmri.Sensor;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonNamedBeanHttpService;
import jmri.server.json.JsonRequest;
import jmri.server.json.sensor.JsonSensor;
import jmri.server.json.sensor.JsonSensorHttpService;

/**
 *
 * @author Randall Wood
 */
public class JsonTurnoutHttpService extends JsonNamedBeanHttpService<Turnout> {

    private final JsonSensorHttpService sensorService;

    public JsonTurnoutHttpService(ObjectMapper mapper) {
        super(mapper);
        sensorService = new JsonSensorHttpService(mapper);
    }

    @Override
    public ObjectNode doGet(Turnout turnout, String name, String type, JsonRequest request) throws JsonException {
        ObjectNode root = this.getNamedBean(turnout, name, getType(), request); // throws JsonException if turnout == null
        ObjectNode data = root.with(JSON.DATA);
        if (turnout != null) {
            data.put(INVERTED, turnout.getInverted());
            switch (turnout.getKnownState()) {
                case Turnout.THROWN:
                    data.put(STATE, THROWN);
                    break;
                case Turnout.CLOSED:
                    data.put(STATE, CLOSED);
                    break;
                case NamedBean.INCONSISTENT:
                    data.put(STATE, INCONSISTENT);
                    break;
                case NamedBean.UNKNOWN:
                default:
                    data.put(STATE, UNKNOWN);
                    break;
            }
            data.put(FEEDBACK_MODE, turnout.getFeedbackMode());
            ArrayNode modes = data.arrayNode();
            turnout.getValidFeedbackModes().forEach(modes::add);
            data.set(FEEDBACK_MODES, modes);
            ArrayNode sensors = data.arrayNode();
            Sensor sensor = turnout.getFirstSensor();
            sensors.add(sensor == null ? null : sensorService.doGet(sensor, sensor.getSystemName(), JsonSensor.SENSOR, request));
            sensor = turnout.getSecondSensor();
            sensors.add(sensor == null ? null : sensorService.doGet(sensor, sensor.getSystemName(), JsonSensor.SENSOR, request));
            data.set(SENSOR, sensors);
        }
        return root;
    }

    @Override
    public ObjectNode doPost(Turnout turnout, String name, String type, JsonNode data, JsonRequest request) throws JsonException {
        if (data.path(SENSOR).isArray()) {
            int number = 0;
            for (JsonNode node : data.path(SENSOR)) {
                if (node != null) {
                    this.addSensorToTurnout(turnout, node, number, request);
                }
                number++;
            }
        }
        if (data.path(FEEDBACK_MODE).isInt()) {
            try {
                turnout.setFeedbackMode(data.path(FEEDBACK_MODE).asInt());
            } catch (IllegalArgumentException ex) {
                throw new JsonException(400, Bundle.getMessage(request.locale, "ErrorInvalidProperty", FEEDBACK_MODE, type, name), request.id);
            }
        }
        if (data.path(INVERTED).isBoolean()) {
            turnout.setInverted(data.path(INVERTED).asBoolean());
        }
        turnout.setInverted(data.path(INVERTED).asBoolean(turnout.getInverted()));
        int state = data.path(STATE).asInt(UNKNOWN);
        switch (state) {
            case THROWN:
                turnout.setCommandedState(Turnout.THROWN);
                break;
            case CLOSED:
                turnout.setCommandedState(Turnout.CLOSED);
                break;
            case UNKNOWN:
                // leave state alone in this case
                break;
            default:
                throw new JsonException(400, Bundle.getMessage(request.locale, "ErrorUnknownState", TURNOUT, state), request.id);
        }
        return this.doGet(turnout, name, type, request);
    }

    private void addSensorToTurnout(@Nonnull Turnout turnout, @Nonnull JsonNode data, int number, @Nonnull JsonRequest request) throws JsonException {
        try {
            if (data.isNull()) {
                turnout.provideFeedbackSensor(null, number);
            } else {
                Sensor sensor = null;
                if (data.isTextual()) {
                    sensor = sensorService.getNamedBean(SENSOR, data.asText(), mapper.nullNode(), request);
                } else if (data.isObject()) {
                    sensor = sensorService.getNamedBean(SENSOR, data.path(JSON.NAME).asText(), data, request);
                } else {
                    throw new JsonException(400, Bundle.getMessage(request.locale, "ErrorInvalidProperty", SENSOR, TURNOUT, turnout.getSystemName()), request.id);
                }
                if (sensor != null) {
                    turnout.provideFeedbackSensor(sensor.getSystemName(), number);
                } else {
                    throw new JsonException(404,
                            Bundle.getMessage(request.locale, "ErrorNotFound", SENSOR, data.asText()), request.id);
                }
            }
        } catch (JmriException ex) {
            throw new JsonException(500, Bundle.getMessage(request.locale, "ErrorInternal", TURNOUT), request.id);
        }
    }

    @Override
    protected void doDelete(Turnout bean, String name, String type, JsonNode data, JsonRequest request)
            throws JsonException {
        deleteBean(bean, name, type, data, request);
    }

    @Override
    public JsonNode doSchema(String type, boolean server, JsonRequest request) throws JsonException {
        switch (type) {
            case TURNOUT:
            case TURNOUTS:
                return doSchema(type,
                        server,
                        "jmri/server/json/turnout/turnout-server.json",
                        "jmri/server/json/turnout/turnout-client.json",
                        request.id);
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(request.locale, JsonException.ERROR_UNKNOWN_TYPE, type), request.id);
        }
    }

    @Override
    protected String getType() {
        return TURNOUT;
    }

    @Override
    protected ProvidingManager<Turnout> getManager() {
        return InstanceManager.getDefault(TurnoutManager.class);
    }
}
