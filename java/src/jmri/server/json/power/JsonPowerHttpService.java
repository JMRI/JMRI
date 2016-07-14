package jmri.server.json.power;

import static jmri.jmris.json.JSON.DATA;
import static jmri.jmris.json.JSON.OFF;
import static jmri.jmris.json.JSON.ON;
import static jmri.jmris.json.JSON.STATE;
import static jmri.jmris.json.JSON.TYPE;
import static jmri.jmris.json.JSON.UNKNOWN;
import static jmri.server.json.power.JsonPowerServiceFactory.POWER;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood
 */
public class JsonPowerHttpService extends JsonHttpService {

    private static final Logger log = LoggerFactory.getLogger(JsonPowerHttpService.class);

    public JsonPowerHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public JsonNode doGet(String type, String name, Locale locale) throws JsonException {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, POWER);
        ObjectNode data = root.putObject(DATA);
        try {
            switch (InstanceManager.getDefault(jmri.PowerManager.class).getPower()) {
                case PowerManager.OFF:
                    data.put(STATE, OFF);
                    break;
                case PowerManager.ON:
                    data.put(STATE, ON);
                    break;
                default:
                    data.put(STATE, UNKNOWN);
                    break;
            }
        } catch (JmriException e) {
            log.error("Unable to get Power state.", e);
            throw new JsonException(500, Bundle.getMessage(locale, "ErrorPower"));
        } catch (NullPointerException e) {
            // No PowerManager is defined; just report it as UNKNOWN
            data.put(STATE, UNKNOWN);
        }
        return root;
    }

    @Override
    public JsonNode doPost(String type, String name, JsonNode data, Locale locale) throws JsonException {
        int state = data.path(STATE).asInt(UNKNOWN);
        try {
            switch (state) {
                case OFF:
                    InstanceManager.getDefault(jmri.PowerManager.class).setPower(PowerManager.OFF);
                    break;
                case ON:
                    InstanceManager.getDefault(jmri.PowerManager.class).setPower(PowerManager.ON);
                    break;
                case UNKNOWN:
                    // quietly ignore
                    break;
                default:
                    throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, Bundle.getMessage(locale, "ErrorUnknownState", POWER, state));
            }
        } catch (JmriException ex) {
            throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex);
        }
        return this.doGet(type, name, locale);
    }

    @Override
    public JsonNode doGetList(String type, Locale locale) throws JsonException {
        throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, Bundle.getMessage(locale, "UnlistableService", POWER));
    }
}
