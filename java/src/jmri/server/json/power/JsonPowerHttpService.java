package jmri.server.json.power;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;
import static jmri.jmris.json.JSON.DATA;
import static jmri.jmris.json.JSON.OFF;
import static jmri.jmris.json.JSON.ON;
import static jmri.jmris.json.JSON.POWER;
import static jmri.jmris.json.JSON.STATE;
import static jmri.jmris.json.JSON.TYPE;
import static jmri.jmris.json.JSON.UNKNOWN;
import jmri.jmris.json.JsonException;
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
            switch (InstanceManager.powerManagerInstance().getPower()) {
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
                    InstanceManager.powerManagerInstance().setPower(PowerManager.OFF);
                    break;
                case ON:
                    InstanceManager.powerManagerInstance().setPower(PowerManager.ON);
                    break;
                case UNKNOWN:
                    // quietly ignore
                    break;
                default:
                    throw new JsonException(400, Bundle.getMessage(locale, "ErrorUnknownState", POWER, state));
            }
        } catch (JmriException ex) {
            throw new JsonException(500, ex);
        }
        return this.doGet(type, name, locale);
    }

    @Override
    public JsonNode doPut(String type, String name, JsonNode data, Locale locale) throws JsonException {
        return this.doPost(type, name, data, locale);
    }
    
    @Override
    public JsonNode doGetList(String type, Locale locale) throws JsonException {
        return this.doGet(type, type, locale);
    }
    
}
