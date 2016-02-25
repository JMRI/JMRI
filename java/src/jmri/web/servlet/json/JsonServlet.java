// JsonServlet.java
package jmri.web.servlet.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.Light;
import jmri.Memory;
import jmri.NamedBean;
import jmri.Route;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.SignalMast;
import jmri.Turnout;
import jmri.implementation.QuietShutDownTask;
import static jmri.jmris.json.JSON.ASPECT;
import static jmri.jmris.json.JSON.CAR;
import static jmri.jmris.json.JSON.CARS;
import static jmri.jmris.json.JSON.CONSIST;
import static jmri.jmris.json.JSON.CONSISTS;
import static jmri.jmris.json.JSON.DATA;
import static jmri.jmris.json.JSON.ENGINE;
import static jmri.jmris.json.JSON.ENGINES;
import static jmri.jmris.json.JSON.FORMAT;
import static jmri.jmris.json.JSON.GOODBYE;
import static jmri.jmris.json.JSON.HELLO;
import static jmri.jmris.json.JSON.LIGHT;
import static jmri.jmris.json.JSON.LIGHTS;
import static jmri.jmris.json.JSON.LOCATION;
import static jmri.jmris.json.JSON.LOCATIONS;
import static jmri.jmris.json.JSON.MEMORIES;
import static jmri.jmris.json.JSON.MEMORY;
import static jmri.jmris.json.JSON.METADATA;
import static jmri.jmris.json.JSON.NAME;
import static jmri.jmris.json.JSON.NETWORK_SERVICES;
import static jmri.jmris.json.JSON.NODE;
import static jmri.jmris.json.JSON.PANELS;
import static jmri.jmris.json.JSON.RAILROAD;
import static jmri.jmris.json.JSON.REPORTER;
import static jmri.jmris.json.JSON.REPORTERS;
import static jmri.jmris.json.JSON.ROSTER;
import static jmri.jmris.json.JSON.ROSTER_ENTRY;
import static jmri.jmris.json.JSON.ROSTER_GROUP;
import static jmri.jmris.json.JSON.ROSTER_GROUPS;
import static jmri.jmris.json.JSON.ROUTE;
import static jmri.jmris.json.JSON.ROUTES;
import static jmri.jmris.json.JSON.SENSOR;
import static jmri.jmris.json.JSON.SENSORS;
import static jmri.jmris.json.JSON.SIGNAL_HEAD;
import static jmri.jmris.json.JSON.SIGNAL_HEADS;
import static jmri.jmris.json.JSON.SIGNAL_MAST;
import static jmri.jmris.json.JSON.SIGNAL_MASTS;
import static jmri.jmris.json.JSON.STATE;
import static jmri.jmris.json.JSON.SYSTEM_CONNECTIONS;
import static jmri.jmris.json.JSON.TIME;
import static jmri.jmris.json.JSON.TRAIN;
import static jmri.jmris.json.JSON.TRAINS;
import static jmri.jmris.json.JSON.TURNOUT;
import static jmri.jmris.json.JSON.TURNOUTS;
import static jmri.jmris.json.JSON.TYPE;
import static jmri.jmris.json.JSON.UNKNOWN;
import static jmri.jmris.json.JSON.VALUE;
import static jmri.jmris.json.JSON.XML;
import jmri.jmris.json.JsonClientHandler;
import jmri.jmris.json.JsonConnection;
import jmri.jmris.json.JsonServerPreferences;
import jmri.jmris.json.JsonUtil;
import jmri.jmrit.operations.trains.Train;
import static jmri.jmrit.operations.trains.Train.DEPARTURETIME_CHANGED_PROPERTY;
import static jmri.jmrit.operations.trains.Train.STATUS_CHANGED_PROPERTY;
import static jmri.jmrit.operations.trains.Train.TRAIN_LOCATION_CHANGED_PROPERTY;
import static jmri.jmrit.operations.trains.Train.TRAIN_MOVE_COMPLETE_CHANGED_PROPERTY;
import static jmri.jmrit.operations.trains.Train.TRAIN_REQUIREMENTS_CHANGED_PROPERTY;
import static jmri.jmrit.operations.trains.Train.TRAIN_ROUTE_CHANGED_PROPERTY;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.server.json.JsonAsyncHttpListener;
import jmri.server.json.JsonAsyncHttpService;
import jmri.server.json.JsonException;
import static jmri.server.json.JsonException.CODE;
import jmri.server.json.JsonHttpService;
import static jmri.server.json.power.JsonPowerServiceFactory.POWER;
import jmri.spi.JsonServiceFactory;
import jmri.util.FileUtil;
import jmri.web.servlet.ServletUtil;
import static jmri.web.servlet.ServletUtil.APPLICATION_JSON;
import static jmri.web.servlet.ServletUtil.UTF8_APPLICATION_JSON;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide JSON formatted responses for requests to requests for information
 * from the JMRI Web Server.
 *
 * This server supports long polling in some GET requests, but also provides a
 * WebSocket to provide a more extensive control and monitoring capability.
 *
 * This server responds to HTTP requests for objects in following manner:
 * <table>
 * <tr><th>Method</th><th>List</th><th>Object</th></tr>
 * <tr><th>GET</th><td>Returns the list</td><td>Returns the object <em>if it
 * already exists</em></td></tr>
 * <tr><th>POST</th><td>Invalid</td><td>Modifies the object <em>if it already
 * exists</em></td></tr>
 * <tr><th>PUT</th><td>Invalid</td><td>Modifies the object, creating it if
 * required</td></tr>
 * </table>
 *
 * @author rhwood Copyright (C) 2012, 2013
 */
public class JsonServlet extends WebSocketServlet {

    private static final long serialVersionUID = -671593634343578915L;
    private static final long longPollTimeout = 30000; // 5 minutes
    private ObjectMapper mapper;
    private final HashMap<String, HashSet<JsonHttpService>> services = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(JsonServlet.class);

    @Override
    public void init() throws ServletException {
        super.init();
        this.mapper = new ObjectMapper();
        for (JsonServiceFactory factory : ServiceLoader.load(JsonServiceFactory.class)) {
            for (String type : factory.getTypes()) {
                HashSet<JsonHttpService> set = this.services.get(type);
                if (set == null) {
                    this.services.put(type, new HashSet<>());
                    set = this.services.get(type);
                }
                set.add(factory.getHttpService(this.mapper));
            }
        }
    }

    @Override
    public void destroy() {
        super.destroy();
    }

    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.register(JsonServlet.JsonWebSocket.class);
    }

    /**
     * Handle HTTP get requests for JSON data. Examples:
     * <ul>
     * <li>/json/sensor/IS22 (return data for sensor with system name
     * "IS22")</li>
     * <li>/json/sensors (returns a list of all sensors known to JMRI)</li>
     * </ul>
     * sample responses:
     * <ul>
     * <li>{"type":"sensor","data":{"name":"IS22","userName":"FarEast","comment":null,"inverted":false,"state":4}}</li>
     * <li>[{"type":"sensor","data":{"name":"IS22","userName":"FarEast","comment":null,"inverted":false,"state":4}}]</li>
     * </ul>
     * Note that data will vary for each type.
     *
     * @param request  an HttpServletRequest object that contains the request
     *                 the client has made of the servlet
     * @param response an HttpServletResponse object that contains the response
     *                 the servlet sends to the client
     * @throws java.io.IOException if an input or output error is detected when
     *                             the servlet handles the GET request
     */
    @Override
    protected void doGet(final HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader("Connection", "Keep-Alive"); // NOI18N

        if (request.getAttribute("result") != null) {
            JsonNode result = (JsonNode) request.getAttribute("result");
            int code = result.path(DATA).path(CODE).asInt(200); // use HTTP error codes when possible
            if (code == 200) {
                response.getWriter().write(this.mapper.writeValueAsString(result));
            } else {
                this.sendError(response, code, this.mapper.writeValueAsString(result));
            }
            return;
        }

        String[] rest = request.getPathInfo().split("/"); // NOI18N
        String type = (rest.length > 1) ? rest[1] : null;
        if (type != null) {
            response.setContentType(UTF8_APPLICATION_JSON);
            ServletUtil.getInstance().setNonCachingHeaders(response);
            final String name = (rest.length > 2) ? URLDecoder.decode(rest[2], StandardCharsets.UTF_8.name()) : null;
            ObjectNode parameters = this.mapper.createObjectNode();
            for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
                parameters.put(entry.getKey(), URLDecoder.decode(entry.getValue()[0], "UTF-8"));
            }
            Boolean longPoll = !parameters.path(VALUE).isMissingNode();
            if (!parameters.path(STATE).isMissingNode()) {
                // JSON unknown state (0) != NamedBean unknown state (1)
                // unless its a SignalHead (where unknown state is 0)
                if (parameters.path(STATE).asInt() == UNKNOWN && !type.equals(SIGNAL_HEAD)) {
                    parameters.put(STATE, NamedBean.UNKNOWN);
                }
                longPoll = true;
            }
            JsonNode reply = null;
            try {
                if (name == null) {
                    switch (type) {
                        case CARS:
                            reply = JsonUtil.getCars(request.getLocale());
                            break;
                        case CONSISTS:
                            reply = JsonUtil.getConsists(request.getLocale());
                            break;
                        case ENGINES:
                            reply = JsonUtil.getEngines(request.getLocale());
                            break;
                        case LIGHTS:
                            reply = JsonUtil.getLights(request.getLocale());
                            break;
                        case LOCATIONS:
                            reply = JsonUtil.getLocations(request.getLocale());
                            break;
                        case MEMORIES:
                            reply = JsonUtil.getMemories(request.getLocale());
                            break;
                        case METADATA:
                            reply = JsonUtil.getMetadata(request.getLocale());
                            break;
                        case PANELS:
                            reply = JsonUtil.getPanels(request.getLocale(), (request.getParameter(FORMAT) != null) ? request.getParameter(FORMAT) : XML);
                            break;
                        case RAILROAD:
                            reply = JsonUtil.getRailroad(request.getLocale());
                            break;
                        case REPORTERS:
                            reply = JsonUtil.getReporters(request.getLocale());
                            break;
                        case ROSTER:
                            reply = JsonUtil.getRoster(request.getLocale(), parameters);
                            break;
                        case ROSTER_GROUPS:
                            reply = JsonUtil.getRosterGroups(request.getLocale());
                            break;
                        case ROUTES:
                            reply = JsonUtil.getRoutes(request.getLocale());
                            break;
                        case SENSORS:
                            reply = JsonUtil.getSensors(request.getLocale());
                            break;
                        case SIGNAL_HEADS:
                            reply = JsonUtil.getSignalHeads(request.getLocale());
                            break;
                        case SIGNAL_MASTS:
                            reply = JsonUtil.getSignalMasts(request.getLocale());
                            break;
                        case TIME:
                            if (longPoll) {
                                final AsyncContext context = request.startAsync(request, response);
                                context.setTimeout(longPollTimeout);
                                context.start(new TimePollingHandler(request.getLocale(), context));
                                return;
                            }
                            reply = JsonUtil.getTime(request.getLocale());
                            break;
                        case TRAINS:
                            reply = JsonUtil.getTrains(request.getLocale());
                            break;
                        case TURNOUTS:
                            reply = JsonUtil.getTurnouts(request.getLocale());
                            break;
                        case HELLO:
                            reply = JsonUtil.getHello(request.getLocale(), JsonServerPreferences.getDefault().getHeartbeatInterval());
                            break;
                        case NETWORK_SERVICES:
                            reply = JsonUtil.getNetworkServices(request.getLocale());
                            break;
                        case SYSTEM_CONNECTIONS:
                            reply = JsonUtil.getSystemConnections(request.getLocale());
                            break;
                        case NODE:
                            reply = JsonUtil.getNode(request.getLocale());
                            break;
                        default:
                            if (this.services.get(type) != null) {
                                ArrayNode array = this.mapper.createArrayNode();
                                for (JsonHttpService service : this.services.get(type)) {
                                    array.add(service.doGetList(type, request.getLocale()));
                                }
                                if (array.size() == 1) {
                                    reply = array.get(0);
                                } else {
                                    reply = array;
                                }
                            }
                            if (reply == null) {
                                log.warn("Type {} unknown.", type);
                                reply = JsonUtil.getUnknown(request.getLocale(), type);
                            }
                            break;
                    }
                } else {
                    switch (type) {
                        case CAR:
                            reply = JsonUtil.getCar(request.getLocale(), name);
                            break;
                        case CONSIST:
                            reply = JsonUtil.getConsist(request.getLocale(), JsonUtil.addressForString(name));
                            break;
                        case ENGINE:
                            reply = JsonUtil.getEngine(request.getLocale(), name);
                            break;
                        case LIGHT:
                            if (longPoll) {
                                Light light = InstanceManager.lightManagerInstance().getBySystemName(name);
                                if (light.getState() == parameters.path(STATE).asInt()) {
                                    final AsyncContext context = request.startAsync(request, response);
                                    context.setTimeout(longPollTimeout);
                                    context.start(new LightPollingHandler(request.getLocale(), light, parameters.path(STATE).asInt(), context));
                                    return;
                                }
                            }
                            reply = JsonUtil.getLight(request.getLocale(), name);
                            break;
                        case LOCATION:
                            reply = JsonUtil.getLocation(request.getLocale(), name);
                            break;
                        case MEMORY:
                            if (longPoll) {
                                Memory memory = InstanceManager.memoryManagerInstance().getBySystemName(name);
                                if (memory.getValue().toString().equals(parameters.path(VALUE).asText())) {
                                    final AsyncContext context = request.startAsync(request, response);
                                    context.setTimeout(longPollTimeout);
                                    context.start(new MemoryPollingHandler(request.getLocale(), memory, parameters.path(VALUE).asText(), context));
                                    return;
                                }
                            }
                            reply = JsonUtil.getMemory(request.getLocale(), name);
                            break;
                        case METADATA:
                            reply = JsonUtil.getMetadata(request.getLocale(), name);
                            break;
                        case REPORTER:
                            reply = JsonUtil.getReporter(request.getLocale(), name);
                            break;
                        case ROSTER_ENTRY:
                        case ROSTER:
                            reply = JsonUtil.getRosterEntry(request.getLocale(), name);
                            break;
                        case ROSTER_GROUP:
                            reply = JsonUtil.getRosterGroup(request.getLocale(), name);
                            break;
                        case ROUTE:
                            if (longPoll) {
                                Route route = InstanceManager.routeManagerInstance().getBySystemName(name);
                                if (InstanceManager.sensorManagerInstance().getBySystemName(route.getTurnoutsAlignedSensor()).getKnownState() == parameters.path(STATE).asInt()) {
                                    final AsyncContext context = request.startAsync(request, response);
                                    context.setTimeout(longPollTimeout);
                                    context.start(new RoutePollingHandler(request.getLocale(), route, parameters.path(STATE).asInt(), context));
                                    return;
                                }
                            }
                            reply = JsonUtil.getRoute(request.getLocale(), name);
                            break;
                        case SENSOR:
                            if (longPoll) {
                                Sensor sensor = InstanceManager.sensorManagerInstance().getBySystemName(name);
                                if (sensor.getKnownState() == parameters.path(STATE).asInt()) {
                                    final AsyncContext context = request.startAsync(request, response);
                                    context.setTimeout(longPollTimeout);
                                    context.start(new SensorPollingHandler(request.getLocale(), sensor, parameters.path(STATE).asInt(), context));
                                    return;
                                }
                            }
                            reply = JsonUtil.getSensor(request.getLocale(), name);
                            break;
                        case SIGNAL_HEAD:
                            if (longPoll) {
                                SignalHead signalHead = InstanceManager.signalHeadManagerInstance().getBySystemName(name);
                                if (signalHead.getAppearance() == parameters.path(STATE).asInt()) {
                                    final AsyncContext context = request.startAsync(request, response);
                                    context.setTimeout(longPollTimeout);
                                    context.start(new SignalHeadPollingHandler(request.getLocale(), signalHead, parameters.path(STATE).asInt(), context));
                                    return;
                                }
                            }
                            reply = JsonUtil.getSignalHead(request.getLocale(), name);
                            break;
                        case SIGNAL_MAST:
                            if (longPoll) {
                                SignalMast signalMast = InstanceManager.signalMastManagerInstance().getBySystemName(name);
                                if (signalMast.getAspect().equals(parameters.path(ASPECT).asText())) {
                                    final AsyncContext context = request.startAsync(request, response);
                                    context.setTimeout(longPollTimeout);
                                    context.start(new SignalMastPollingHandler(request.getLocale(), signalMast, parameters.path(ASPECT).asText(), context));
                                    return;
                                }
                            }
                            reply = JsonUtil.getSignalMast(request.getLocale(), name);
                            break;
                        case TRAIN:
                            if (longPoll) {
                                final AsyncContext context = request.startAsync(request, response);
                                context.setTimeout(longPollTimeout);
                                context.start(new TrainPollingHandler(request.getLocale(), TrainManager.instance().getTrainById(name), context));
                                return;
                            }
                            reply = JsonUtil.getTrain(request.getLocale(), name);
                            break;
                        case TURNOUT:
                            if (longPoll) {
                                Turnout turnout = InstanceManager.turnoutManagerInstance().getBySystemName(name);
                                if (turnout.getKnownState() == parameters.path(STATE).asInt()) {
                                    final AsyncContext context = request.startAsync(request, response);
                                    context.setTimeout(longPollTimeout);
                                    context.start(new TurnoutPollingHandler(request.getLocale(), turnout, parameters.path(STATE).asInt(), context));
                                    return;
                                }
                            }
                            reply = JsonUtil.getTurnout(request.getLocale(), name);
                            break;
                        default:
                            if (this.services.get(type) != null) {
                                for (JsonHttpService service : this.services.get(type)) {
                                    // TODO: take all replies and massage into single reply
                                    reply = service.doGet(type, name, request.getLocale());
                                }
                            }
                            if (reply == null) {
                                log.warn("Type {} unknown.", type);
                                reply = JsonUtil.getUnknown(request.getLocale(), type);
                            }
                            break;
                    }
                }
            } catch (JsonException ex) {
                reply = ex.getJsonMessage();
            }
            int code = reply.path(DATA).path(CODE).asInt(200); // use HTTP error codes when possible
            if (code == 200) {
                response.getWriter().write(this.mapper.writeValueAsString(reply));
            } else {
                this.sendError(response, code, this.mapper.writeValueAsString(reply));
            }
        } else {
            response.setContentType(ServletUtil.UTF8_TEXT_HTML); // NOI18N
            response.getWriter().print(String.format(request.getLocale(),
                    FileUtil.readURL(FileUtil.findURL(Bundle.getMessage(request.getLocale(), "Json.html"))),
                    String.format(request.getLocale(),
                            Bundle.getMessage(request.getLocale(), "HtmlTitle"),
                            ServletUtil.getInstance().getRailroadName(false),
                            Bundle.getMessage(request.getLocale(), "JsonTitle")
                    ),
                    ServletUtil.getInstance().getNavBar(request.getLocale(), request.getContextPath()),
                    ServletUtil.getInstance().getRailroadName(false),
                    ServletUtil.getInstance().getFooter(request.getLocale(), request.getContextPath())
            ));

        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(UTF8_APPLICATION_JSON);
        response.setHeader("Connection", "Keep-Alive"); // NOI18N
        ServletUtil.getInstance().setNonCachingHeaders(response);

        String[] rest = request.getPathInfo().split("/"); // NOI18N
        String type = (rest.length > 1) ? rest[1] : null;
        String name = (rest.length > 2) ? rest[2] : null;
        JsonNode data;
        JsonNode reply = null;
        try {
            if (request.getContentType().contains(APPLICATION_JSON)) {
                data = this.mapper.readTree(request.getReader());
                if (data.isArray()) {
                    AsyncContext context = request.startAsync(request, response);
                    context.setTimeout(longPollTimeout);
                    context.start(new JsonAsyncHttpHandler((ArrayNode) data, context));
                }
                if (!data.path(DATA).isMissingNode()) {
                    data = data.path(DATA);
                }
            } else {
                data = this.mapper.createObjectNode();
                if (request.getParameter(STATE) != null) {
                    ((ObjectNode) data).put(STATE, Integer.parseInt(request.getParameter(STATE)));
                } else if (request.getParameter(LOCATION) != null) {
                    ((ObjectNode) data).put(LOCATION, request.getParameter(LOCATION));
                } else if (request.getParameter(VALUE) != null) {
                    // values other than Strings should be sent in a JSON object
                    ((ObjectNode) data).put(VALUE, request.getParameter(VALUE));
                }
            }
            if (type != null) {
                if (type.equals(POWER)) {
                    name = POWER;
                } else if (name == null) {
                    name = data.path(NAME).asText();
                }
                log.debug("POST operation for {}/{} with {}", type, name, data);
                if (name != null) {
                    switch (type) {
                        case CONSIST:
                            JsonUtil.setConsist(request.getLocale(), JsonUtil.addressForString(name), data);
                            reply = JsonUtil.getConsist(request.getLocale(), JsonUtil.addressForString(name));
                            break;
                        case LIGHT:
                            JsonUtil.setLight(request.getLocale(), name, data);
                            reply = JsonUtil.getLight(request.getLocale(), name);
                            break;
                        case MEMORY:
                            JsonUtil.setMemory(request.getLocale(), name, data);
                            reply = JsonUtil.getMemory(request.getLocale(), name);
                            break;
                        case REPORTER:
                            JsonUtil.setReporter(request.getLocale(), name, data);
                            reply = JsonUtil.getReporter(request.getLocale(), name);
                            break;
                        case ROUTE:
                            JsonUtil.setRoute(request.getLocale(), name, data);
                            reply = JsonUtil.getRoute(request.getLocale(), name);
                            break;
                        case SENSOR:
                            JsonUtil.setSensor(request.getLocale(), name, data);
                            reply = JsonUtil.getSensor(request.getLocale(), name);
                            break;
                        case SIGNAL_HEAD:
                            JsonUtil.setSignalHead(request.getLocale(), name, data);
                            reply = JsonUtil.getSignalHead(request.getLocale(), name);
                            break;
                        case SIGNAL_MAST:
                            JsonUtil.setSignalMast(request.getLocale(), name, data);
                            reply = JsonUtil.getSignalMast(request.getLocale(), name);
                            break;
                        case TRAIN:
                            JsonUtil.setTrain(request.getLocale(), name, data);
                            reply = JsonUtil.getTrain(request.getLocale(), name);
                            break;
                        case TURNOUT:
                            JsonUtil.setTurnout(request.getLocale(), name, data);
                            reply = JsonUtil.getTurnout(request.getLocale(), name);
                            break;
                        default:
                            if (this.services.get(type) != null) {
                                log.debug("Using data: {}", data);
                                ArrayNode array = this.mapper.createArrayNode();
                                for (JsonHttpService service : this.services.get(type)) {
                                    array.add(service.doPost(type, name, data, request.getLocale()));
                                }
                                if (array.size() == 1) {
                                    reply = array.get(0);
                                } else {
                                    reply = array;
                                }
                            }
                            if (reply == null) {
                                log.warn("Type {} unknown.", type);
                                reply = JsonUtil.getUnknown(request.getLocale(), type);
                            }
                            break;
                    }
                } else {
                    log.error("Name must be defined.");
                    throw new JsonException(400, "Name must be defined."); // Need to I18N
                }
            } else {
                log.warn("Type not specified.");
                reply = JsonUtil.getUnknown(request.getLocale(), type);
            }
        } catch (JsonException ex) {
            reply = ex.getJsonMessage();
        }
        int code = reply.path(DATA).path(CODE).asInt(200); // use HTTP error codes when possible
        if (code == 200) {
            response.getWriter().write(this.mapper.writeValueAsString(reply));
        } else {
            this.sendError(response, code, this.mapper.writeValueAsString(reply));
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(UTF8_APPLICATION_JSON);
        response.setHeader("Connection", "Keep-Alive"); // NOI18N
        ServletUtil.getInstance().setNonCachingHeaders(response);

        String[] rest = request.getPathInfo().split("/"); // NOI18N
        String type = (rest.length > 1) ? rest[1] : null;
        String name = (rest.length > 2) ? rest[2] : null;
        JsonNode data;
        JsonNode reply = null;
        try {
            if (request.getContentType().contains(APPLICATION_JSON)) {
                data = this.mapper.readTree(request.getReader());
                if (!data.path(DATA).isMissingNode()) {
                    data = data.path(DATA);
                }
            } else {
                throw new JsonException(400, "PUT request must be a JSON object"); // need to I18N
            }
            if (type != null) {
                if (type.equals(POWER)) {
                    name = POWER;
                } else if (name == null) {
                    name = data.path(NAME).asText();
                }
                if (name != null) {
                    switch (type) {
                        case CONSIST:
                            JsonUtil.putConsist(request.getLocale(), JsonUtil.addressForString(name), data);
                            reply = JsonUtil.getConsist(request.getLocale(), JsonUtil.addressForString(name));
                            break;
                        case LIGHT:
                            JsonUtil.putLight(request.getLocale(), name, data);
                            reply = JsonUtil.getLight(request.getLocale(), name);
                            break;
                        case MEMORY:
                            JsonUtil.putMemory(request.getLocale(), name, data);
                            reply = JsonUtil.getMemory(request.getLocale(), name);
                            break;
                        case REPORTER:
                            JsonUtil.putReporter(request.getLocale(), name, data);
                            reply = JsonUtil.getReporter(request.getLocale(), name);
                            break;
                        case SENSOR:
                            JsonUtil.putSensor(request.getLocale(), name, data);
                            reply = JsonUtil.getSensor(request.getLocale(), name);
                            break;
                        case TURNOUT:
                            JsonUtil.putTurnout(request.getLocale(), name, data);
                            reply = JsonUtil.getTurnout(request.getLocale(), name);
                            break;
                        default:
                            if (this.services.get(type) != null) {
                                ArrayNode array = this.mapper.createArrayNode();
                                for (JsonHttpService service : this.services.get(type)) {
                                    array.add(service.doPut(type, name, data, request.getLocale()));
                                }
                                if (array.size() == 1) {
                                    reply = array.get(0);
                                } else {
                                    reply = array;
                                }
                            }
                            if (reply == null) {
                                // not a creatable item
                                throw new JsonException(400, type + " is not a creatable type"); // need to I18N
                            }
                            break;
                    }
                } else {
                    log.warn("Type {} unknown.", type);
                    reply = JsonUtil.getUnknown(request.getLocale(), type);
                }
            } else {
                log.warn("Type not specified.");
                reply = JsonUtil.getUnknown(request.getLocale(), type);
            }
        } catch (JsonException ex) {
            reply = ex.getJsonMessage();
        }
        int code = reply.path(DATA).path(CODE).asInt(200); // use HTTP error codes when possible
        if (code == 200) {
            response.getWriter().write(this.mapper.writeValueAsString(reply));
        } else {
            this.sendError(response, code, this.mapper.writeValueAsString(reply));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(UTF8_APPLICATION_JSON);
        response.setHeader("Connection", "Keep-Alive"); // NOI18N
        ServletUtil.getInstance().setNonCachingHeaders(response);

        String[] rest = request.getPathInfo().split("/"); // NOI18N
        String type = (rest.length > 1) ? rest[1] : null;
        String name = (rest.length > 2) ? rest[2] : null;
        JsonNode reply = mapper.createObjectNode();
        try {
            if (type != null) {
                if (name == null) {
                    throw new JsonException(400, "name must be specified"); // need to I18N
                }
                if (type.equals(CONSIST)) {
                    JsonUtil.delConsist(request.getLocale(), JsonUtil.addressForString(name));
                } else if (this.services.get(type) != null) {
                    for (JsonHttpService service : this.services.get(type)) {
                        service.doDelete(type, name, request.getLocale());
                    }
                } else {
                    // not a deletable item
                    throw new JsonException(400, type + " is not a deletable type"); // need to I18N
                }
            } else {
                log.warn("Type not specified.");
                reply = JsonUtil.getUnknown(request.getLocale(), type);
            }
        } catch (JsonException ex) {
            reply = ex.getJsonMessage();
        }
        int code = reply.path(DATA).path(CODE).asInt(200); // use HTTP error codes when possible
        // only include a response body if something went wrong
        if (code != 200) {
            this.sendError(response, code, this.mapper.writeValueAsString(reply));
        }
    }

    public void sendError(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(code);
        response.getWriter().write(message);
    }

    @WebSocket
    public static class JsonWebSocket {

        protected JsonConnection connection;
        protected ObjectMapper mapper;
        protected JsonClientHandler handler;
        protected QuietShutDownTask shutDownTask;

        public void sendMessage(String message) throws IOException {
            this.connection.sendMessage(message);
        }

        @OnWebSocketConnect
        public void onOpen(Session sn) {
            log.debug("Opening connection");
            this.connection = new JsonConnection(sn);
            sn.setIdleTimeout((long) (JsonServerPreferences.getDefault().getHeartbeatInterval() * 1.1));
            this.mapper = new ObjectMapper();
            this.handler = new JsonClientHandler(this.connection, this.mapper);
            this.shutDownTask = new QuietShutDownTask("Close open web socket") { // NOI18N
                @Override
                public boolean execute() {
                    try {
                        JsonWebSocket.this.sendMessage(
                                JsonWebSocket.this.mapper.writeValueAsString(
                                        JsonWebSocket.this.mapper.createObjectNode().put(TYPE, GOODBYE)));
                    } catch (IOException e) {
                        log.warn("Unable to send goodbye while closing socket.\nError was {}", e.getMessage());
                    }
                    JsonWebSocket.this.connection.getSession().close();
                    return true;
                }
            };
            try {
                log.debug("Sending hello");
                this.handler.sendHello(JsonServerPreferences.getDefault().getHeartbeatInterval());
            } catch (IOException e) {
                log.warn("Error opening WebSocket:\n{}", e.getMessage());
                sn.close();
            }
            InstanceManager.shutDownManagerInstance().register(this.shutDownTask);
        }

        @OnWebSocketClose
        public void onClose(int i, String string) {
            log.debug("Closing connection because {} ({})", string, i);
            this.handler.dispose();
            InstanceManager.shutDownManagerInstance().deregister(this.shutDownTask);
        }

        @OnWebSocketError
        public void onError(Throwable thrwbl) {
            if (thrwbl instanceof SocketTimeoutException) {
                log.error(thrwbl.getMessage());
                return;
            }
            log.error(thrwbl.getMessage(), thrwbl);
        }

        @OnWebSocketMessage
        public void onMessage(String string) {
            try {
                this.handler.onMessage(string);
            } catch (IOException e) {
                log.error("Error on WebSocket message:\n{}", e.getMessage());
                this.connection.getSession().close();
                InstanceManager.shutDownManagerInstance().deregister(this.shutDownTask);
            }
        }
    }

    private class JsonAsyncHttpHandler implements Runnable, AsyncListener, PropertyChangeListener {

        private final AsyncContext context;
        private final HashSet<JsonAsyncHttpListener> listeners = new HashSet<>();

        private JsonAsyncHttpHandler(ArrayNode data, AsyncContext context) {
            this.context = context;
            for (JsonNode object : data) {
                String type = object.path(TYPE).asText();
                String name = object.path(NAME).asText();
                for (JsonHttpService service : JsonServlet.this.services.get(type)) {
                    if (JsonAsyncHttpService.class.isInstance(service)) {
                        this.listeners.add(((JsonAsyncHttpService) service).getListener(type, name, object));
                    }
                }
            }
        }

        @Override
        public void run() {
            for (JsonAsyncHttpListener listener : this.listeners) {
                listener.addPropertyChangeListener(this);
                if (!listener.listen()) {
                    this.respond();
                    return;
                }
            }
        }

        private void respond() {
            if (this.context.getRequest().isAsyncStarted()) {
                ArrayNode array = JsonServlet.this.mapper.createArrayNode();
                Locale locale = this.context.getRequest().getLocale();
                this.listeners.stream().forEach((listener) -> {
                    try {
                        listener.stopListening();
                        listener.removePropertyChangeListener(this);
                        array.add(listener.doGet(locale));
                    } catch (JsonException ex) {
                        array.add(ex.getJsonMessage());
                    }
                });
                this.context.getRequest().setAttribute("result", array);
                this.context.dispatch();
            }
        }

        @Override
        public void onComplete(AsyncEvent ae) throws IOException {
            log.debug("context is complete");
        }

        @Override
        public void onTimeout(AsyncEvent ae) throws IOException {
            log.debug("context timed out");
            this.respond();
        }

        @Override
        public void onError(AsyncEvent ae) throws IOException {
            log.debug("context has error");
        }

        @Override
        public void onStartAsync(AsyncEvent ae) throws IOException {
            log.debug("context is starting");
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            log.debug("context property change");
            this.respond();
        }

    }

    private static abstract class JsonPollingHandler implements Runnable, AsyncListener {

        protected final int knownState;
        protected final String knownValue;
        protected final AsyncContext context;
        protected PropertyChangeListener listener;
        protected final Locale locale;

        @SuppressWarnings("LeakingThisInConstructor")
        public JsonPollingHandler(Locale locale, int expectedState, AsyncContext context) {
            this.knownState = expectedState;
            this.knownValue = null;
            this.context = context;
            this.locale = locale;
            context.addListener(this);
        }

        @SuppressWarnings("LeakingThisInConstructor")
        public JsonPollingHandler(Locale locale, String knownValue, AsyncContext context) {
            this.knownState = 0;
            this.knownValue = knownValue;
            this.context = context;
            this.locale = locale;
            context.addListener(this);
        }

        @Override
        public void onComplete(AsyncEvent ae) throws IOException {
            log.debug("context is complete");
        }

        @Override
        public void onTimeout(AsyncEvent ae) throws IOException {
            log.debug("context timed out");
            respond();
        }

        @Override
        public void onError(AsyncEvent ae) throws IOException {
            log.debug("context has error");
        }

        @Override
        public void onStartAsync(AsyncEvent ae) throws IOException {
            log.debug("context is starting");
        }

        protected abstract void respond();
    }

    private static class LightPollingHandler extends JsonPollingHandler {

        private final Light light;

        public LightPollingHandler(Locale locale, Light light, int knownState, AsyncContext context) {
            super(locale, knownState, context);
            this.light = light;
        }

        @Override
        protected void respond() {
            light.removePropertyChangeListener(listener);
            if (context.getRequest().isAsyncStarted()) {
                try {
                    context.getRequest().setAttribute("result", JsonUtil.getLight(locale, light.getSystemName()));
                } catch (JsonException ex) {
                    context.getRequest().setAttribute("result", ex.getJsonMessage());
                }
                context.dispatch();
            }
        }

        @Override
        public void run() {
            listener = (PropertyChangeEvent evt) -> {
                if (knownState != light.getState()) {
                    respond();
                }
            };
            light.addPropertyChangeListener(listener);
        }
    }

    private static class MemoryPollingHandler extends JsonPollingHandler {

        private final Memory memory;

        public MemoryPollingHandler(Locale locale, Memory memory, String knownValue, AsyncContext context) {
            super(locale, knownValue, context);
            this.memory = memory;
        }

        @Override
        protected void respond() {
            memory.removePropertyChangeListener(listener);
            if (context.getRequest().isAsyncStarted()) {
                try {
                    context.getRequest().setAttribute("result", JsonUtil.getMemory(locale, memory.getSystemName()));
                } catch (JsonException ex) {
                    context.getRequest().setAttribute("result", ex.getJsonMessage());
                }
                context.dispatch();
            }
        }

        @Override
        public void run() {
            listener = (PropertyChangeEvent evt) -> {
                if (!knownValue.equals(memory.getValue().toString())) {
                    respond();
                }
            };
            memory.addPropertyChangeListener(listener);
        }
    }

    private static class RoutePollingHandler extends JsonPollingHandler {

        private final Route route;

        public RoutePollingHandler(Locale locale, Route route, int knownState, AsyncContext context) {
            super(locale, knownState, context);
            this.route = route;
        }

        @Override
        protected void respond() {
            route.removePropertyChangeListener(listener);
            if (context.getRequest().isAsyncStarted()) {
                try {
                    context.getRequest().setAttribute("result", JsonUtil.getRoute(locale, route.getSystemName()));
                } catch (JsonException ex) {
                    context.getRequest().setAttribute("result", ex.getJsonMessage());
                }
                context.dispatch();
            }
        }

        @Override
        public void run() {
            listener = (PropertyChangeEvent evt) -> {
                if (knownState != InstanceManager.sensorManagerInstance().getBySystemName(route.getTurnoutsAlignedSensor()).getKnownState()) {
                    respond();
                }
            };
            route.addPropertyChangeListener(listener);
        }
    }

    private static class SensorPollingHandler extends JsonPollingHandler {

        private final Sensor sensor;

        public SensorPollingHandler(Locale locale, Sensor sensor, int knownState, AsyncContext context) {
            super(locale, knownState, context);
            this.sensor = sensor;
        }

        @Override
        protected void respond() {
            sensor.removePropertyChangeListener(listener);
            if (context.getRequest().isAsyncStarted()) {
                try {
                    context.getRequest().setAttribute("result", JsonUtil.getSensor(locale, sensor.getSystemName()));
                } catch (JsonException ex) {
                    context.getRequest().setAttribute("result", ex.getJsonMessage());
                }
                context.dispatch();
            }
        }

        @Override
        public void run() {
            listener = (PropertyChangeEvent evt) -> {
                if (knownState != sensor.getKnownState()) {
                    respond();
                }
            };
            sensor.addPropertyChangeListener(listener);
        }
    }

    private static class SignalHeadPollingHandler extends JsonPollingHandler {

        private final SignalHead signalHead;

        public SignalHeadPollingHandler(Locale locale, SignalHead signalHead, int knownState, AsyncContext context) {
            super(locale, knownState, context);
            this.signalHead = signalHead;
        }

        @Override
        protected void respond() {
            signalHead.removePropertyChangeListener(listener);
            if (context.getRequest().isAsyncStarted()) {
                try {
                    context.getRequest().setAttribute("result", JsonUtil.getSignalHead(locale, signalHead.getSystemName()));
                } catch (JsonException ex) {
                    context.getRequest().setAttribute("result", ex.getJsonMessage());
                }
                context.dispatch();
            }
        }

        @Override
        public void run() {
            listener = (PropertyChangeEvent evt) -> {
                if (knownState != signalHead.getAppearance()) {
                    respond();
                }
            };
            signalHead.addPropertyChangeListener(listener);
        }
    }

    private static class SignalMastPollingHandler extends JsonPollingHandler {

        private final SignalMast signalMast;

        public SignalMastPollingHandler(Locale locale, SignalMast signalMast, String knownValue, AsyncContext context) {
            super(locale, knownValue, context);
            this.signalMast = signalMast;
        }

        @Override
        protected void respond() {
            signalMast.removePropertyChangeListener(listener);
            if (context.getRequest().isAsyncStarted()) {
                try {
                    context.getRequest().setAttribute("result", JsonUtil.getSignalMast(locale, signalMast.getSystemName()));
                } catch (JsonException ex) {
                    context.getRequest().setAttribute("result", ex.getJsonMessage());
                }
                context.dispatch();
            }
        }

        @Override
        public void run() {
            listener = (PropertyChangeEvent evt) -> {
                if (!knownValue.equals(signalMast.getAspect())) {
                    respond();
                }
            };
            signalMast.addPropertyChangeListener(listener);
        }
    }

    private static class TimePollingHandler extends JsonPollingHandler {

        public TimePollingHandler(Locale locale, AsyncContext context) {
            super(locale, 0, context);
        }

        @Override
        protected void respond() {
            InstanceManager.timebaseInstance().removeMinuteChangeListener(listener);
            if (context.getRequest().isAsyncStarted()) {
                try {
                    context.getRequest().setAttribute("result", JsonUtil.getTime(locale));
                } catch (JsonException ex) {
                    context.getRequest().setAttribute("result", ex.getJsonMessage());
                }
                context.dispatch();
            }
        }

        @Override
        public void run() {
            listener = (PropertyChangeEvent evt) -> {
                if (evt.getPropertyName().equals("minutes")) {
                    respond();
                }
            };
            InstanceManager.timebaseInstance().addPropertyChangeListener(listener);
        }
    }

    private static class TrainPollingHandler extends JsonPollingHandler {

        private final Train train;

        public TrainPollingHandler(Locale locale, Train train, AsyncContext context) {
            super(locale, 0, context);
            this.train = train;
        }

        @Override
        protected void respond() {
            train.removePropertyChangeListener(listener);
            if (context.getRequest().isAsyncStarted()) {
                try {
                    context.getRequest().setAttribute("result", JsonUtil.getTrain(locale, train.getId()));
                } catch (JsonException ex) {
                    context.getRequest().setAttribute("result", ex.getJsonMessage());
                }
                context.dispatch();
            }
        }

        @Override
        public void run() {
            listener = (PropertyChangeEvent evt) -> {
                if (evt.getPropertyName().equals(STATUS_CHANGED_PROPERTY)
                        || evt.getPropertyName().equals(DEPARTURETIME_CHANGED_PROPERTY)
                        || evt.getPropertyName().equals(TRAIN_LOCATION_CHANGED_PROPERTY)
                        || evt.getPropertyName().equals(TRAIN_ROUTE_CHANGED_PROPERTY)
                        || evt.getPropertyName().equals(TRAIN_REQUIREMENTS_CHANGED_PROPERTY)
                        || evt.getPropertyName().equals(TRAIN_MOVE_COMPLETE_CHANGED_PROPERTY)) {
                    respond();
                }
            };
            train.addPropertyChangeListener(listener);
        }
    }

    private static class TurnoutPollingHandler extends JsonPollingHandler {

        private final Turnout turnout;

        public TurnoutPollingHandler(Locale locale, Turnout turnout, int knownState, AsyncContext context) {
            super(locale, knownState, context);
            this.turnout = turnout;
        }

        @Override
        protected void respond() {
            turnout.removePropertyChangeListener(listener);
            if (context.getRequest().isAsyncStarted()) {
                try {
                    context.getRequest().setAttribute("result", JsonUtil.getTurnout(locale, turnout.getSystemName()));
                } catch (JsonException ex) {
                    context.getRequest().setAttribute("result", ex.getJsonMessage());
                }
                context.dispatch();
            }
        }

        @Override
        public void run() {
            listener = (PropertyChangeEvent evt) -> {
                if (knownState != turnout.getKnownState()) {
                    respond();
                }
            };
            turnout.addPropertyChangeListener(listener);
        }
    }
}
