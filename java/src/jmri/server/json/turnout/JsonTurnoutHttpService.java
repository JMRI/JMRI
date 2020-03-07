package jmri.server.json.turnout;

import static jmri.server.json.JSON.CLOSED;
import static jmri.server.json.JSON.INCONSISTENT;
import static jmri.server.json.JSON.INVERTED;
import static jmri.server.json.JSON.STATE;
import static jmri.server.json.JSON.THROWN;
import static jmri.server.json.JSON.UNKNOWN;
import static jmri.server.json.sensor.JsonSensor.SENSOR;
import static jmri.server.json.turnout.JsonTurnout.FEEDBACK_MODE;
import static jmri.server.json.turnout.JsonTurnout.TURNOUT;
import static jmri.server.json.turnout.JsonTurnout.TURNOUTS;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.ProvidingManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonNamedBeanHttpService;
import jmri.server.json.JsonRequest;

/**
 *
 * @author Randall Wood
 */
public class JsonTurnoutHttpService extends JsonNamedBeanHttpService<Turnout> {

    public JsonTurnoutHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public ObjectNode doGet(Turnout turnout, String name, String type, JsonRequest request) throws JsonException {
        ObjectNode root = this.getNamedBean(turnout, name, type, request); // throws JsonException if turnout == null
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
            Sensor sensor = turnout.getFirstSensor();
            data.put(SENSOR + 1, sensor != null ? sensor.getSystemName() : null);
            sensor = turnout.getSecondSensor();
            data.put(SENSOR + 2, sensor != null ? sensor.getSystemName() : null);
        }
        return root;
    }

    @Override
    public ObjectNode doPost(Turnout turnout, String name, String type, JsonNode data, Locale locale, int id) throws JsonException {
        if (!data.path(SENSOR + 1).isMissingNode()) {
            JsonNode node = data.path(SENSOR + 1);
            try {
                if (node.isNull()) {
                    turnout.provideFirstFeedbackSensor(null);
                } else {
                    Sensor sensor = InstanceManager.getDefault(SensorManager.class).getBySystemName(node.asText());
                    if (sensor != null) {
                        turnout.provideFirstFeedbackSensor(sensor.getSystemName());
                    } else {
                        throw new JsonException(404,
                                Bundle.getMessage(locale, "ErrorNotFound", SENSOR, node.asText()), id);
                    }
                }
            } catch (JmriException ex) {
                throw new JsonException(500, Bundle.getMessage(locale, "ErrorInternal", type), id);
            }
        }
        if (!data.path(SENSOR + 2).isMissingNode()) {
            JsonNode node = data.path(SENSOR + 2);
            try {
                if (node.isNull()) {
                    turnout.provideSecondFeedbackSensor(null);
                } else {
                    Sensor sensor = InstanceManager.getDefault(SensorManager.class).getBySystemName(node.asText());
                    if (sensor != null) {
                        turnout.provideSecondFeedbackSensor(sensor.getSystemName());
                    } else {
                        throw new JsonException(404,
                                Bundle.getMessage(locale, "ErrorNotFound", SENSOR, node.asText()), id);
                    }
                }
            } catch (JmriException ex) {
                throw new JsonException(500, Bundle.getMessage(locale, "ErrorInternal", type), id);
            }
    public ObjectNode doPost(Turnout turnout, String name, String type, JsonNode data, JsonRequest request) throws JsonException {
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

    @Override
    protected void doDelete(Turnout bean, String name, String type, JsonNode data, Locale locale, int id)
            throws JsonException {
        deleteBean(bean, name, type, data, locale, id);
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
