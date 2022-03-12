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
import javax.annotation.CheckForNull;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;
import jmri.server.json.JsonRequest;

/**
 * @author Randall Wood Copyright 2016, 2018
 */
public class JsonPowerHttpService extends JsonHttpService {

    public JsonPowerHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    // Nullable to override inherited NonNull requirement
    public JsonNode doGet(String type, @CheckForNull String name, JsonNode parameters, JsonRequest request)
            throws JsonException {
        ObjectNode data = mapper.createObjectNode();
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
        return message(POWER, data, request.id);
    }

    @Override
    public JsonNode doPost(String type, String name, JsonNode data, JsonRequest request) throws JsonException {
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
                                    Bundle.getMessage(request.locale, "ErrorUnknownState", POWER, state), request.id);
                    }
                }
            } catch (JmriException ex) {
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex, request.id);
            }
        }
        return this.doGet(type, name, data, request);
    }

    @Override
    public JsonNode doGetList(String type, JsonNode data, JsonRequest request) throws JsonException {
        ArrayNode array = this.mapper.createArrayNode();
        for (PowerManager manager : InstanceManager.getList(PowerManager.class)) {
            array.add(this.doGet(type, manager.getUserName(), data, request));
        }
        return message(array, request.id);
    }

    @Override
    public JsonNode doSchema(String type, boolean server, JsonRequest request) throws JsonException {
        if (POWER.equals(type)) {
            return doSchema(type,
                    server,
                    "jmri/server/json/power/power-server.json",
                    "jmri/server/json/power/power-client.json",
                    request.id);
        } else {
            throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    Bundle.getMessage(request.locale, JsonException.ERROR_UNKNOWN_TYPE, type), request.id);
        }
    }
}
