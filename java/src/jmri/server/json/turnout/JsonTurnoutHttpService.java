package jmri.server.json.turnout;

import static jmri.server.json.JSON.CLOSED;
import static jmri.server.json.JSON.COMMENT;
import static jmri.server.json.JSON.INCONSISTENT;
import static jmri.server.json.JSON.INVERTED;
import static jmri.server.json.JSON.STATE;
import static jmri.server.json.JSON.THROWN;
import static jmri.server.json.JSON.UNKNOWN;
import static jmri.server.json.JSON.USERNAME;
import static jmri.server.json.turnout.JsonTurnoutServiceFactory.TURNOUT;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.Turnout;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonNamedBeanHttpService;

/**
 *
 * @author Randall Wood
 */
public class JsonTurnoutHttpService extends JsonNamedBeanHttpService {

    public JsonTurnoutHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public JsonNode doGet(String type, String name, Locale locale) throws JsonException {
        ObjectNode root = mapper.createObjectNode();
        root.put(JSON.TYPE, TURNOUT);
        Turnout turnout = InstanceManager.turnoutManagerInstance().getTurnout(name);
        ObjectNode data = this.getNamedBean(turnout, name, type, locale); // throws JsonException if turnout == null
        root.put(JSON.DATA, data);
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
    public JsonNode doPost(String type, String name, JsonNode data, Locale locale) throws JsonException {
        Turnout turnout = InstanceManager.turnoutManagerInstance().getTurnout(name);
        if (turnout == null) {
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", TURNOUT, name));
        }
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
        return this.doGet(type, name, locale);
    }

    @Override
    public JsonNode doPut(String type, String name, JsonNode data, Locale locale) throws JsonException {
        try {
            InstanceManager.turnoutManagerInstance().provideTurnout(name);
        } catch (IllegalArgumentException ex) {
            throw new JsonException(500, Bundle.getMessage(locale, "ErrorCreatingObject", TURNOUT, name));
        }
        return this.doPost(type, name, data, locale);
    }

    @Override
    public ArrayNode doGetList(String type, Locale locale) throws JsonException {
        ArrayNode root = this.mapper.createArrayNode();
        for (String name : InstanceManager.turnoutManagerInstance().getSystemNameList()) {
            root.add(this.doGet(TURNOUT, name, locale));
        }
        return root;

    }
}
