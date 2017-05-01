package jmri.server.json.power;

import static jmri.server.json.JSON.DATA;
import static jmri.server.json.JSON.DEFAULT;
import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.OFF;
import static jmri.server.json.JSON.ON;
import static jmri.server.json.JSON.STATE;
import static jmri.server.json.JSON.TYPE;
import static jmri.server.json.JSON.UNKNOWN;
import static jmri.server.json.power.JsonPowerServiceFactory.POWER;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
            PowerManager manager = InstanceManager.getDefault(PowerManager.class);
            if (name != null && !name.isEmpty()) {
                for (PowerManager pm : InstanceManager.getList(PowerManager.class)) {
                    if (pm.getUserName().equals(name)) {
                        manager = pm;
                        data.put(NAME, name);
                    }
                }
            }
            switch (manager.getPower()) {
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
            data.put(DEFAULT, false);
            if (manager.equals(InstanceManager.getDefault(PowerManager.class))) {
                data.put(DEFAULT, true);
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
            PowerManager manager = InstanceManager.getDefault(PowerManager.class);
            if (name != null && !name.isEmpty()) {
                for (PowerManager pm : InstanceManager.getList(PowerManager.class)) {
                    if (pm.getUserName().equals(name)) {
                        manager = pm;
                    }
                }
            }
            switch (state) {
                case OFF:
                    manager.setPower(PowerManager.OFF);
                    break;
                case ON:
                    manager.setPower(PowerManager.ON);
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
    public ArrayNode doGetList(String type, Locale locale) throws JsonException {
        ArrayNode root = this.mapper.createArrayNode();
        for (PowerManager manager : InstanceManager.getList(PowerManager.class)) {
            root.add(this.doGet(type, manager.getUserName(), locale));
        }
        return root;
    }
}
