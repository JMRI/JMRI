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
import javax.servlet.http.HttpServletResponse;

import jmri.DigitalIO;
import jmri.InstanceManager;
import jmri.Light;
import jmri.LightManager;
import jmri.NamedBean;
import jmri.ProvidingManager;
import jmri.server.json.JsonException;
import jmri.server.json.JsonNamedBeanHttpService;
import jmri.server.json.JsonRequest;

/**
 *
 * @author Randall Wood
 */
public class JsonLightHttpService extends JsonNamedBeanHttpService<Light> {

    public JsonLightHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public ObjectNode doGet(Light light, String name, String type, JsonRequest request) throws JsonException {
        ObjectNode root = this.getNamedBean(light, name, getType(), request);
        ObjectNode data = root.with(DATA);
        if (light != null) {
            switch (light.getState()) {
                case DigitalIO.ON:
                    data.put(STATE, ON);
                    break;
                case DigitalIO.OFF:
                    data.put(STATE, OFF);
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
    public ObjectNode doPost(Light light, String name, String type, JsonNode data, JsonRequest request) throws JsonException {
        int state = data.path(STATE).asInt(UNKNOWN);
        switch (state) {
            case ON:
                light.setState(DigitalIO.ON);
                break;
            case OFF:
                light.setState(DigitalIO.OFF);
                break;
            case UNKNOWN:
                // leave state alone in this case
                break;
            default:
                throw new JsonException(400, Bundle.getMessage(request.locale, "ErrorUnknownState", LIGHT, state), request.id);
        }
        return this.doGet(light, name, type, request);
    }

    @Override
    public JsonNode doSchema(String type, boolean server, JsonRequest request) throws JsonException {
        switch (type) {
            case LIGHT:
            case LIGHTS:
                return doSchema(type,
                        server,
                        "jmri/server/json/light/light-server.json",
                        "jmri/server/json/light/light-client.json",
                        request.id);
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(request.locale, JsonException.ERROR_UNKNOWN_TYPE, type), request.id);
        }
    }

    @Override
    protected String getType() {
        return LIGHT;
    }

    @Override
    protected ProvidingManager<Light> getManager() {
        return InstanceManager.getDefault(LightManager.class);
    }
}
