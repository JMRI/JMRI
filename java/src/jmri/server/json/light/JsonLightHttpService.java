package jmri.server.json.light;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.Light;
import static jmri.server.json.JSON.COMMENT;
import static jmri.server.json.JSON.DATA;
import static jmri.server.json.JSON.INCONSISTENT;
import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.OFF;
import static jmri.server.json.JSON.ON;
import static jmri.server.json.JSON.STATE;
import static jmri.server.json.JSON.TYPE;
import static jmri.server.json.JSON.UNKNOWN;
import static jmri.server.json.JSON.USERNAME;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;
import static jmri.server.json.light.JsonLightServiceFactory.LIGHT;

/**
 *
 * @author Randall Wood
 */
class JsonLightHttpService extends JsonHttpService {

    public JsonLightHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public JsonNode doGet(String type, String name, Locale locale) throws JsonException {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, LIGHT);
        ObjectNode data = root.putObject(DATA);
        try {
            Light light = InstanceManager.lightManagerInstance().getLight(name);
            data.put(NAME, light.getSystemName());
            data.put(USERNAME, light.getUserName());
            data.put(COMMENT, light.getComment());
            switch (light.getState()) {
                case Light.ON:
                    data.put(STATE, ON);
                    break;
                case Light.OFF:
                    data.put(STATE, OFF);
                    break;
                case Light.INCONSISTENT:
                    data.put(STATE, INCONSISTENT);
                    break;
                case Light.UNKNOWN:
                default:
                    data.put(STATE, UNKNOWN);
                    break;
            }
        } catch (NullPointerException ex) {
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", LIGHT, name));
        }
        return root;
    }

    @Override
    public JsonNode doPost(String type, String name, JsonNode data, Locale locale) throws JsonException {
        try {
            Light light = InstanceManager.lightManagerInstance().getLight(name);
            if (data.path(USERNAME).isTextual()) {
                light.setUserName(data.path(USERNAME).asText());
            }
            if (data.path(COMMENT).isTextual()) {
                light.setComment(data.path(COMMENT).asText());
            }
            int state = data.path(STATE).asInt(UNKNOWN);
            switch (state) {
                case ON:
                    light.setState(Light.ON);
                    break;
                case OFF:
                    light.setState(Light.OFF);
                    break;
                case UNKNOWN:
                    // leave state alone in this case
                    break;
                default:
                    throw new JsonException(400, Bundle.getMessage(locale, "ErrorUnknownState", LIGHT, state));
            }
        } catch (NullPointerException ex) {
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", LIGHT, name));
        }
        return this.doGet(type, name, locale);
    }

    @Override
    public JsonNode doPut(String type, String name, JsonNode data, Locale locale) throws JsonException {
        try {
            InstanceManager.lightManagerInstance().provideLight(name);
        } catch (Exception ex) {
            throw new JsonException(500, Bundle.getMessage(locale, "ErrorCreatingObject", LIGHT, name));
        }
        return this.doPost(type, name, data, locale);
    }

    @Override
    public JsonNode doGetList(String type, Locale locale) throws JsonException {
        ArrayNode root = this.mapper.createArrayNode();
        for (String name : InstanceManager.lightManagerInstance().getSystemNameList()) {
            root.add(this.doGet(LIGHT, name, locale));
        }
        return root;

    }
}
