package jmri.server.json.power;

import static jmri.server.json.JSON.DEFAULT;
import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.OFF;
import static jmri.server.json.JSON.ON;
import static jmri.server.json.JSON.STATE;
import static jmri.server.json.JSON.UNKNOWN;
import static jmri.server.json.power.JsonPowerServiceFactory.POWER;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Locale;
import javax.annotation.CheckForNull;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Randall Wood Copyright 2016, 2018
 */
public class JsonPowerHttpService extends JsonHttpService {

    private static final Logger log = LoggerFactory.getLogger(JsonPowerHttpService.class);

    public JsonPowerHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    // Nullable to override inherited NonNull requirement
    public JsonNode doGet(String type, @CheckForNull String name, JsonNode parameters, Locale locale, int id)
            throws JsonException {
        ObjectNode data = mapper.createObjectNode();
        try {
            PowerManager manager = InstanceManager.getNullableDefault(PowerManager.class);
            if (name != null && !name.isEmpty()) {
                for (PowerManager pm : InstanceManager.getList(PowerManager.class)) {
                    if (pm.getUserName().equals(name)) {
                        manager = pm;
                    }
                }
            }
            if (manager != null) {
                data.put(NAME, manager.getUserName());
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
            } else {
                // No PowerManager is defined; just report it as UNKNOWN
                data.put(STATE, UNKNOWN);
                data.put(NAME, "");
                data.put(DEFAULT, false);
            }
        } catch (JmriException e) {
            log.error("Unable to get Power state.", e);
            throw new JsonException(500, Bundle.getMessage(locale, "ErrorPower"), id);
        }
        return message(POWER, data, id);
    }

    @Override
    public JsonNode doPost(String type, String name, JsonNode data, Locale locale, int id) throws JsonException {
        int state = data.path(STATE).asInt(UNKNOWN);
        if (state != UNKNOWN) {
            try {
                PowerManager manager = InstanceManager.getNullableDefault(PowerManager.class);
                if (!name.isEmpty()) {
                    for (PowerManager pm : InstanceManager.getList(PowerManager.class)) {
                        if (pm.getUserName().equals(name)) {
                            manager = pm;
                        }
                    }
                }
                if (manager != null) {
                    switch (state) {
                        case OFF:
                            manager.setPower(PowerManager.OFF);
                            break;
                        case ON:
                            manager.setPower(PowerManager.ON);
                            break;
                        default:
                            throw new JsonException(HttpServletResponse.SC_BAD_REQUEST,
                                    Bundle.getMessage(locale, "ErrorUnknownState", POWER, state), id);
                    }
                }
            } catch (JmriException ex) {
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex, id);
            }
        }
        return this.doGet(type, name, data, locale, id);
    }

    @Override
    public JsonNode doGetList(String type, JsonNode data, Locale locale, int id) throws JsonException {
        ArrayNode array = this.mapper.createArrayNode();
        for (PowerManager manager : InstanceManager.getList(PowerManager.class)) {
            array.add(this.doGet(type, manager.getUserName(), data, locale, id));
        }
        return message(array, id);
    }

    @Override
    public JsonNode doSchema(String type, boolean server, Locale locale, int id) throws JsonException {
        if (POWER.equals(type)) {
            return doSchema(type,
                    server,
                    "jmri/server/json/power/power-server.json",
                    "jmri/server/json/power/power-client.json",
                    id);
        } else {
            throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    Bundle.getMessage(locale, JsonException.ERROR_UNKNOWN_TYPE, type), id);
        }
    }
}
