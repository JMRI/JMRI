package jmri.server.json.light;

import static jmri.server.json.JSON.DATA;
import static jmri.server.json.JSON.INCONSISTENT;
import static jmri.server.json.JSON.OFF;
import static jmri.server.json.JSON.ON;
import static jmri.server.json.JSON.STATE;
import static jmri.server.json.JSON.UNKNOWN;
import static jmri.server.json.light.JsonLight.LIGHT;
import static jmri.server.json.light.JsonLight.LIGHTS;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.Light;
import jmri.LightManager;
import jmri.ProvidingManager;
import jmri.server.json.JsonException;
import jmri.server.json.JsonNamedBeanHttpService;

/**
 *
 * @author Randall Wood
 */
public class JsonLightHttpService extends JsonNamedBeanHttpService<Light> {

    public JsonLightHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public ObjectNode doGet(Light light, String name, String type, Locale locale) throws JsonException {
        ObjectNode root = this.getNamedBean(light, name, type, locale);
        ObjectNode data = root.with(DATA);
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
    public ObjectNode doPost(Light light, String name, String type, JsonNode data, Locale locale) throws JsonException {
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
        return this.doGet(light, name, type, locale);
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

    @Override
    protected String getType() {
        return LIGHT;
    }

    @Override
    protected ProvidingManager<Light> getManager() throws UnsupportedOperationException {
        return InstanceManager.getDefault(LightManager.class);
    }
}
