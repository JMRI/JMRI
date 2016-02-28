// JsonServlet.java
package jmri.web.servlet.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.implementation.QuietShutDownTask;
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
import static jmri.jmris.json.JSON.TRAIN;
import static jmri.jmris.json.JSON.TRAINS;
import static jmri.jmris.json.JSON.TYPE;
import static jmri.jmris.json.JSON.VALUE;
import static jmri.jmris.json.JSON.XML;
import jmri.jmris.json.JsonServerPreferences;
import jmri.jmris.json.JsonUtil;
import jmri.server.json.JsonClientHandler;
import jmri.server.json.JsonConnection;
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
    private ObjectMapper mapper;
    private final HashMap<String, HashSet<JsonHttpService>> services = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(JsonServlet.class);

    @Override
    public void init() throws ServletException {
        super.init();
        this.mapper = new ObjectMapper();
        for (JsonServiceFactory factory : ServiceLoader.load(JsonServiceFactory.class)) {
            JsonHttpService service = factory.getHttpService(this.mapper);
            if (service != null) {
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
                        case TRAINS:
                            reply = JsonUtil.getTrains(request.getLocale());
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
                            reply = JsonUtil.getLight(request.getLocale(), name);
                            break;
                        case LOCATION:
                            reply = JsonUtil.getLocation(request.getLocale(), name);
                            break;
                        case MEMORY:
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
                            reply = JsonUtil.getRoute(request.getLocale(), name);
                            break;
                        case SENSOR:
                            reply = JsonUtil.getSensor(request.getLocale(), name);
                            break;
                        case SIGNAL_HEAD:
                            reply = JsonUtil.getSignalHead(request.getLocale(), name);
                            break;
                        case SIGNAL_MAST:
                            reply = JsonUtil.getSignalMast(request.getLocale(), name);
                            break;
                        case TRAIN:
                            reply = JsonUtil.getTrain(request.getLocale(), name);
                            break;
                        default:
                            if (this.services.get(type) != null) {
                                ArrayNode array = this.mapper.createArrayNode();
                                for (JsonHttpService service : this.services.get(type)) {
                                    array.add(service.doGet(type, name, request.getLocale()));
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
    private static class JsonWebSocket {

        protected JsonConnection connection;
        protected JsonClientHandler handler;
        protected QuietShutDownTask shutDownTask;

        @OnWebSocketConnect
        public void onOpen(Session sn) {
            log.debug("Opening connection");
            this.connection = new JsonConnection(sn);
            sn.setIdleTimeout((long) (JsonServerPreferences.getDefault().getHeartbeatInterval() * 1.1));
            this.handler = new JsonClientHandler(this.connection);
            this.shutDownTask = new QuietShutDownTask("Close open web socket") { // NOI18N
                @Override
                public boolean execute() {
                    try {
                        JsonWebSocket.this.connection.sendMessage(
                                JsonWebSocket.this.connection.getObjectMapper().createObjectNode().put(TYPE, GOODBYE));
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
}
