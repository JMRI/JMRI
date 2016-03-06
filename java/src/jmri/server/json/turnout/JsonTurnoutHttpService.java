package jmri.server.json.turnout;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.Turnout;
import static jmri.server.json.JSON.CLOSED;
import static jmri.server.json.JSON.COMMENT;
import static jmri.server.json.JSON.DATA;
import static jmri.server.json.JSON.INCONSISTENT;
import static jmri.server.json.JSON.INVERTED;
import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.STATE;
import static jmri.server.json.JSON.THROWN;
import static jmri.server.json.JSON.TYPE;
import static jmri.server.json.JSON.UNKNOWN;
import static jmri.server.json.JSON.USERNAME;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;
import static jmri.server.json.turnout.JsonTurnoutServiceFactory.TURNOUT;

/**
 *
 * @author Randall Wood
 */
class JsonTurnoutHttpService extends JsonHttpService {

    public JsonTurnoutHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public JsonNode doGet(String type, String name, Locale locale) throws JsonException {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, TURNOUT);
        ObjectNode data = root.putObject(DATA);
        try {
            Turnout turnout = InstanceManager.turnoutManagerInstance().getTurnout(name);
            data.put(NAME, turnout.getSystemName());
            data.put(USERNAME, turnout.getUserName());
            data.put(COMMENT, turnout.getComment());
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
        } catch (NullPointerException ex) {
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", TURNOUT, name));
        }
        return root;
    }

    @Override
    public JsonNode doPost(String type, String name, JsonNode data, Locale locale) throws JsonException {
        try {
            Turnout turnout = InstanceManager.turnoutManagerInstance().getTurnout(name);
            if (data.path(USERNAME).isTextual()) {
                turnout.setUserName(data.path(USERNAME).asText());
            }
            if (data.path(INVERTED).isBoolean()) {
                turnout.setInverted(data.path(INVERTED).asBoolean());
            }
            if (data.path(COMMENT).isTextual()) {
                turnout.setComment(data.path(COMMENT).asText());
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
                    throw new JsonException(400, Bundle.getMessage(locale, "ErrorUnknownState", TURNOUT, state));
            }
        } catch (NullPointerException ex) {
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", TURNOUT, name));
        }
        return this.doGet(type, name, locale);
    }

    @Override
    public JsonNode doPut(String type, String name, JsonNode data, Locale locale) throws JsonException {
        try {
            InstanceManager.turnoutManagerInstance().provideTurnout(name);
        } catch (Exception ex) {
            throw new JsonException(500, Bundle.getMessage(locale, "ErrorCreatingObject", TURNOUT, name));
        }
        return this.doPost(type, name, data, locale);
    }

    @Override
    public JsonNode doGetList(String type, Locale locale) throws JsonException {
        ArrayNode root = this.mapper.createArrayNode();
        for (String name : InstanceManager.turnoutManagerInstance().getSystemNameList()) {
            root.add(this.doGet(TURNOUT, name, locale));
        }
        return root;

    }
}
