package jmri.server.json.light;

import static jmri.server.json.JSON.DATA;
import static jmri.server.json.JSON.INCONSISTENT;
import static jmri.server.json.JSON.OFF;
import static jmri.server.json.JSON.ON;
import static jmri.server.json.JSON.STATE;
import static jmri.server.json.JSON.TYPE;
import static jmri.server.json.JSON.UNKNOWN;
import static jmri.server.json.light.JsonLight.LIGHT;
import static jmri.server.json.light.JsonLight.LIGHTS;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.Light;
import jmri.server.json.JsonException;
import jmri.server.json.JsonNamedBeanHttpService;

/**
 *
 * @author Randall Wood
 */
public class JsonLightHttpService extends JsonNamedBeanHttpService {

    public JsonLightHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public JsonNode doGet(String type, String name, Locale locale) throws JsonException {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, LIGHT);
        Light light = InstanceManager.lightManagerInstance().getLight(name);
        ObjectNode data = this.getNamedBean(light, name, type, locale);
        root.set(DATA, data);
        if (light != null) {
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
        }
        return root;
    }

    @Override
    public JsonNode doPost(String type, String name, JsonNode data, Locale locale) throws JsonException {
        Light light = InstanceManager.lightManagerInstance().getLight(name);
        if (light == null) {
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", LIGHT, name));
        }
        this.postNamedBean(light, data, name, type, locale);
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
    public ArrayNode doGetList(String type, Locale locale) throws JsonException {
        ArrayNode root = this.mapper.createArrayNode();
        for (String name : InstanceManager.lightManagerInstance().getSystemNameList()) {
            root.add(this.doGet(LIGHT, name, locale));
        }
        return root;

    }

    @Override
    public JsonNode doSchema(String type, boolean server, Locale locale) throws JsonException {
        switch (type) {
            case LIGHT:
            case LIGHTS:
                return doSchema(type,
                        server,
                        "jmri/server/json/light/light-server.json",
                        "jmri/server/json/light/light-client.json");
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(locale, "ErrorUnknownType", type));
        }
    }
}
