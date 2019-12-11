package jmri.server.json.turnout;

import static jmri.server.json.JSON.CLOSED;
import static jmri.server.json.JSON.INCONSISTENT;
import static jmri.server.json.JSON.INVERTED;
import static jmri.server.json.JSON.STATE;
import static jmri.server.json.JSON.THROWN;
import static jmri.server.json.JSON.UNKNOWN;
import static jmri.server.json.turnout.JsonTurnoutServiceFactory.TURNOUT;
import static jmri.server.json.turnout.JsonTurnoutServiceFactory.TURNOUTS;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.ProvidingManager;
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
        }
        return root;
    }

    @Override
    public ObjectNode doPost(Turnout turnout, String name, String type, JsonNode data, JsonRequest request) throws JsonException {
        if (data.path(INVERTED).isBoolean()) {
            turnout.setInverted(data.path(INVERTED).asBoolean());
        }
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
