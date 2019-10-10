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
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.ProvidingManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonNamedBeanHttpService;

/**
 *
 * @author Randall Wood
 */
public class JsonTurnoutHttpService extends JsonNamedBeanHttpService<Turnout> {

    public JsonTurnoutHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public ObjectNode doGet(Turnout turnout, String name, String type, Locale locale, int id) throws JsonException {
        ObjectNode root = this.getNamedBean(turnout, name, type, locale, id); // throws JsonException if turnout == null
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
                case Turnout.INCONSISTENT:
                    data.put(STATE, INCONSISTENT);
                    break;
                case Turnout.UNKNOWN:
                default:
                    data.put(STATE, UNKNOWN);
                    break;
            }
        }
        return root;
    }

    @Override
    public ObjectNode doPost(Turnout turnout, String name, String type, JsonNode data, Locale locale, int id) throws JsonException {
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
                throw new JsonException(400, Bundle.getMessage(locale, "ErrorUnknownState", TURNOUT, state), id);
        }
        return this.doGet(turnout, name, type, locale, id);
    }

    @Override
    public JsonNode doSchema(String type, boolean server, Locale locale, int id) throws JsonException {
        switch (type) {
            case TURNOUT:
            case TURNOUTS:
                return doSchema(type,
                        server,
                        "jmri/server/json/turnout/turnout-server.json",
                        "jmri/server/json/turnout/turnout-client.json",
                        id);
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(locale, JsonException.ERROR_UNKNOWN_TYPE, type), id);
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
