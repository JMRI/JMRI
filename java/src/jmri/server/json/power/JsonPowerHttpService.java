package jmri.server.json.power;

import static jmri.server.json.JSON.DEFAULT;
import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.OFF;
import static jmri.server.json.JSON.ON;
import static jmri.server.json.JSON.PREFIX;
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
import jmri.SystemConnectionMemo;
import jmri.jmrix.SystemConnectionMemoManager;
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
        PowerManager manager = resolvePowerManager(name, parameters, request);
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
            data.put(DEFAULT, manager.equals(InstanceManager.getDefault(PowerManager.class)));
            String managerPrefix = getPrefixForManager(manager);
            if (managerPrefix != null) {
                data.put(PREFIX, managerPrefix);
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
                PowerManager manager = resolvePowerManager(name, data, request);
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

    /**
     * Resolves the PowerManager to use for a request. Checks for a
     * {@code prefix} field in {@code data} first (connection-specific routing),
     * then falls back to matching by {@code name} (user name), then falls back
     * to the default PowerManager. When {@code prefix} is present but does not
     * match a known connection, throws a {@link JsonException} with HTTP 400.
     *
     * @param name    the power manager user name, or empty/null for default
     * @param data    the JSON data node; may contain an optional {@code prefix} field
     * @param request the originating request, used for locale and id in error messages
     * @return the resolved PowerManager, or null if none is configured
     * @throws JsonException if a prefix is supplied but does not match any known connection
     */
    @CheckForNull
    private PowerManager resolvePowerManager(@CheckForNull String name, JsonNode data, JsonRequest request)
            throws JsonException {
        String prefix = data.path(PREFIX).asText();
        if (!prefix.isEmpty()) {
            SystemConnectionMemo memo = SystemConnectionMemoManager.getDefault()
                    .getSystemConnectionMemoForSystemPrefix(prefix);
            if (memo != null && memo.provides(PowerManager.class)) {
                return memo.get(PowerManager.class);
            }
            throw new JsonException(HttpServletResponse.SC_BAD_REQUEST,
                    Bundle.getMessage(request.locale, "ErrorUnknownPrefix", prefix), request.id);
        }
        if (name != null && !name.isEmpty()) {
            for (PowerManager pm : InstanceManager.getList(PowerManager.class)) {
                if (pm.getUserName().equals(name)) {
                    return pm;
                }
            }
        }
        return InstanceManager.getNullableDefault(PowerManager.class);
    }

    /**
     * Returns the system prefix of the connection that provides the given
     * PowerManager, or null if no connection can be found for it.
     *
     * @param manager the PowerManager to look up
     * @return system prefix string, or null
     */
    @CheckForNull
    private String getPrefixForManager(PowerManager manager) {
        for (SystemConnectionMemo memo : InstanceManager.getList(SystemConnectionMemo.class)) {
            if (memo.provides(PowerManager.class) && manager.equals(memo.get(PowerManager.class))) {
                return memo.getSystemPrefix();
            }
        }
        return null;
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
