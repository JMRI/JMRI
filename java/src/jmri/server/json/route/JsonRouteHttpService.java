package jmri.server.json.route;

import static jmri.server.json.route.JsonRouteServiceFactory.ROUTE;
import static jmri.server.json.route.JsonRouteServiceFactory.ROUTES;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.ProvidingManager;
import jmri.Route;
import jmri.RouteManager;
import jmri.Sensor;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonNamedBeanHttpService;
import jmri.server.json.JsonRequest;

/**
 * Provide JSON HTTP services for managing {@link jmri.Route}s.
 *
 * @author Randall Wood Copyright 2016, 2018
 */
public class JsonRouteHttpService extends JsonNamedBeanHttpService<Route> {

    public JsonRouteHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public ObjectNode doGet(Route route, String name, String type, JsonRequest request) throws JsonException {
        ObjectNode root = this.getNamedBean(route, name, type, request); // throws JsonException if route == null
        ObjectNode data = root.with(JSON.DATA);
        if (route != null) {
            switch (route.getState()) {
                case Sensor.ACTIVE:
                    data.put(JSON.STATE, JSON.ACTIVE);
                    break;
                case Sensor.INACTIVE:
                    data.put(JSON.STATE, JSON.INACTIVE);
                    break;
                case NamedBean.INCONSISTENT:
                    data.put(JSON.STATE, JSON.INCONSISTENT);
                    break;
                case NamedBean.UNKNOWN:
                default:
                    data.put(JSON.STATE, JSON.UNKNOWN);
                    break;
            }
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
     * @param name   the name of the requested route
     * @param data   JSON data set of attributes of the requested route to be
     *               updated
     * @param request the JSON request
     * @return a JSON description of the requested route. Since a route changes
     *         state on a separate thread, this may return a route in the state
     *         prior to this call, the target state, or an intermediate state.
     */
    @Override
    public ObjectNode doPost(Route route, String name, String type, JsonNode data, JsonRequest request) throws JsonException {
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
                throw new JsonException(400, Bundle.getMessage(request.locale, "ErrorUnknownState", ROUTE, state), request.id); // NOI18N
        }
        return this.doGet(route, name, type, request);
    }

    @Override
    public JsonNode doPut(String type, String name, JsonNode data, JsonRequest request) throws JsonException {
        throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, Bundle.getMessage(request.locale, "PutNotAllowed", type), request.id);
    }

    @Override
    public JsonNode doSchema(String type, boolean server, JsonRequest request) throws JsonException {
        switch (type) {
            case ROUTE:
            case ROUTES:
                return doSchema(type,
                        server,
                        "jmri/server/json/route/route-server.json",
                        "jmri/server/json/route/route-client.json",
                        request.id);
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(request.locale, JsonException.ERROR_UNKNOWN_TYPE, type), request.id);
        }
    }

    @Override
    protected String getType() {
        return ROUTE;
    }

    @Override
    protected ProvidingManager<Route> getManager() {
        return InstanceManager.getDefault(RouteManager.class);
    }
}
