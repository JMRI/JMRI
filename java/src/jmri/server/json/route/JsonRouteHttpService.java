package jmri.server.json.route;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.Route;
import jmri.RouteManager;
import jmri.Sensor;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;
import static jmri.server.json.route.JsonRouteServiceFactory.ROUTE;

/**
 * Provide JSON HTTP services for managing {@link jmri.Route}s.
 *
 * @author Randall Wood
 */
public class JsonRouteHttpService extends JsonHttpService {

    public JsonRouteHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public JsonNode doGet(String type, String name, Locale locale) throws JsonException {
        ObjectNode root = mapper.createObjectNode();
        root.put(JSON.TYPE, ROUTE);
        ObjectNode data = root.putObject(JSON.DATA);
        Route route = InstanceManager.getDefault(RouteManager.class).getRoute(name);
        if (route == null) {
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", ROUTE, name)); // NOI18N
        }
        data.put(JSON.NAME, route.getSystemName());
        data.put(JSON.USERNAME, route.getUserName());
        data.put(JSON.COMMENT, route.getComment());
        switch (route.getState()) {
            case Sensor.ACTIVE:
                data.put(JSON.STATE, JSON.ACTIVE);
                break;
            case Sensor.INACTIVE:
                data.put(JSON.STATE, JSON.INACTIVE);
                break;
            case Sensor.INCONSISTENT:
                data.put(JSON.STATE, JSON.INCONSISTENT);
                break;
            case Sensor.UNKNOWN:
            default:
                data.put(JSON.STATE, JSON.UNKNOWN);
                break;
        }
        return root;
    }

    /**
     * Respond to an HTTP POST request for the requested route.
     * <p>
     * This method throws a 404 Not Found error if the named route does not
     * exist.
     * <p>
     * <strong>Note:</strong> attempting to set a state of
     * {@link jmri.server.json.JSON#INACTIVE} or
     * {@link jmri.server.json.JSON#UNKNOWN} has no effect. Setting a state of
     * {@link jmri.server.json.JSON#TOGGLE} has the same effect as setting a
     * state of {@link jmri.server.json.JSON#ACTIVE}. Any other states throw a
     * 400 Invalid Request error.
     *
     * @param type   one of
     *               {@link jmri.server.json.route.JsonRouteServiceFactory#ROUTE}
     *               or
     *               {@link jmri.server.json.route.JsonRouteServiceFactory#ROUTES}
     * @param name   the name of the requested route.
     * @param data   JSON data set of attributes of the requested route to be
     *               updated.
     * @param locale the requesting client's Locale.
     * @return a JSON description of the requested route. Since a route changes
     *         state on a separete thread, this may return a route in the state
     *         prior to this call, the target state, or an intermediate state.
     * @throws JsonException
     */
    @Override
    public JsonNode doPost(String type, String name, JsonNode data, Locale locale) throws JsonException {
        Route route = InstanceManager.getDefault(RouteManager.class).getRoute(name);
        if (route == null) {
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", ROUTE, name));
        }
        if (data.path(JSON.USERNAME).isTextual()) {
            route.setUserName(data.path(JSON.USERNAME).asText());
        }
        if (data.path(JSON.COMMENT).isTextual()) {
            route.setComment(data.path(JSON.COMMENT).asText());
        }
        int state = data.path(JSON.STATE).asInt(JSON.UNKNOWN);
        switch (state) {
            case JSON.ACTIVE:
            case JSON.TOGGLE:
                route.setRoute();
                break;
            case JSON.INACTIVE:
            case JSON.UNKNOWN:
                // leave state alone in this case
                break;
            default:
                throw new JsonException(400, Bundle.getMessage(locale, "ErrorUnknownState", ROUTE, state)); // NOI18N
        }
        return this.doGet(type, name, locale);
    }

    /* We need more information than currently gathered to create a route, so comment out for now
    @Override
    public JsonNode doPut(String type, String name, JsonNode data, Locale locale) throws JsonException {
        String username = data.path(USERNAME).asText();
        if (username == null || username.isEmpty()) {
            throw new JsonException(400, Bundle.getMessage(locale, "ErrorMissingAttribute", USERNAME)); // NOI18N
        }
        try {
            InstanceManager.getDefault(RouteManager.class).provideRoute(name, username);
        } catch (Exception ex) {
            throw new JsonException(500, Bundle.getMessage(locale, "ErrorCreatingObject", ROUTE, name)); // NOI18N
        }
        return this.doPost(type, name, data, locale);
    }
     */
    @Override
    public JsonNode doGetList(String type, Locale locale) throws JsonException {
        ArrayNode root = this.mapper.createArrayNode();
        for (String name : InstanceManager.getDefault(RouteManager.class).getSystemNameList()) {
            root.add(this.doGet(ROUTE, name, locale));
        }
        return root;

    }
}
