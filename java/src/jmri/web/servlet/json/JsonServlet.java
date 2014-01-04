// JsonServlet.java
package jmri.web.servlet.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.implementation.QuietShutDownTask;
import jmri.jmris.JmriConnection;
import static jmri.jmris.json.JSON.CAR;
import static jmri.jmris.json.JSON.CARS;
import static jmri.jmris.json.JSON.CODE;
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
import static jmri.jmris.json.JSON.POWER;
import static jmri.jmris.json.JSON.RAILROAD;
import static jmri.jmris.json.JSON.REPORTER;
import static jmri.jmris.json.JSON.REPORTERS;
import static jmri.jmris.json.JSON.ROSTER;
import static jmri.jmris.json.JSON.ROSTER_ENTRY;
import static jmri.jmris.json.JSON.ROUTE;
import static jmri.jmris.json.JSON.ROUTES;
import static jmri.jmris.json.JSON.SENSOR;
import static jmri.jmris.json.JSON.SENSORS;
import static jmri.jmris.json.JSON.SIGNAL_HEAD;
import static jmri.jmris.json.JSON.SIGNAL_HEADS;
import static jmri.jmris.json.JSON.SIGNAL_MAST;
import static jmri.jmris.json.JSON.SIGNAL_MASTS;
import static jmri.jmris.json.JSON.STATE;
import static jmri.jmris.json.JSON.TRAIN;
import static jmri.jmris.json.JSON.TRAINS;
import static jmri.jmris.json.JSON.TURNOUT;
import static jmri.jmris.json.JSON.TURNOUTS;
import static jmri.jmris.json.JSON.TYPE;
import static jmri.jmris.json.JSON.VALUE;
import static jmri.jmris.json.JSON.XML;
import jmri.jmris.json.JsonClientHandler;
import jmri.jmris.json.JsonException;
import jmri.jmris.json.JsonServerManager;
import jmri.jmris.json.JsonUtil;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide JSON formatted responses for requests to requests for information
 * from the JMRI Web Server.
 *
 * Note that unlike the XMLIO server, this server does not monitor items in
 * response to a request, but does provide a WebSocket for clients capable of
 * using WebSockets to provide that capability.
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
    private final Set<JsonWebSocket> sockets = new CopyOnWriteArraySet<JsonWebSocket>();
    private static final Logger log = LoggerFactory.getLogger(JsonServlet.class);

    public JsonServlet() {
        super();
        InstanceManager.consistManagerInstance().requestUpdateFromLayout();
    }

    @Override
    public void init() throws ServletException {
        super.init();
        this.mapper = new ObjectMapper();
        InstanceManager.shutDownManagerInstance().register(new QuietShutDownTask("Close JSON web sockets") { // NOI18N
            @Override
            public boolean execute() {
                for (JsonWebSocket socket : sockets) {
                    try {
                        socket.wsConnection.sendMessage(socket.mapper.writeValueAsString(socket.mapper.createObjectNode().put(TYPE, GOODBYE)));
                    } catch (IOException e) {
                        log.warn("Unable to send goodbye while closing socket.\nError was {}", e.getMessage());
                    }
                    socket.wsConnection.close();
                }
                return true;
            }
        });
    }

    @Override
    public WebSocket doWebSocketConnect(HttpServletRequest hsr, String string) {
        log.debug("Creating WebSocket for {} at {}", hsr.getRemoteHost(), hsr.getRequestURL());
        return new JsonWebSocket();
    }

    /**
     * handle HTTP get requests for json data examples:
     * <ul>
     * <li>/json/sensor/IS22 (return data for sensor with systemname
     * "IS22")</li>
     * <li>/json/sensors (returns a list of all sensors known to JMRI)</li>
     * </ul>
     * sample responses:
     * <ul>
     * <li>{"type":"sensor","data":{"name":"IS22","userName":"FarEast","comment":null,"inverted":false,"state":4}}</li>
     * <li>[{"type":"sensor","data":{"name":"IS22","userName":"FarEast","comment":null,"inverted":false,"state":4}}]</li>
     * </ul>
     * note that data will vary for each type
     * @param request an HttpServletRequest object that contains the request the client has made of the servlet
     * @param response an HttpServletResponse object that contains the response the servlet sends to the client
     * @throws java.io.IOException if an input or output error is detected when the servlet handles the GET request
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Date now = new Date();
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader("Connection", "Keep-Alive"); // NOI18N
        response.setDateHeader("Date", now.getTime()); // NOI18N
        response.setDateHeader("Last-Modified", now.getTime()); // NOI18N
        response.setDateHeader("Expires", now.getTime()); // NOI18N

        String[] rest = request.getPathInfo().split("/"); // NOI18N
        String type = (rest.length > 1) ? rest[1] : null;
        if (type != null) {
            response.setContentType("application/json"); // NOI18N
            String name = (rest.length > 2) ? rest[2] : null;
            JsonNode reply;
            try {
                if (name == null) {
                    if (type.equals(CARS)) {
                        reply = JsonUtil.getCars();
                    } else if (type.equals(CONSISTS)) {
                        reply = JsonUtil.getConsists();
                    } else if (type.equals(ENGINES)) {
                        reply = JsonUtil.getEngines();
                    } else if (type.equals(LIGHTS)) {
                        reply = JsonUtil.getLights();
                    } else if (type.equals(LOCATIONS)) {
                        reply = JsonUtil.getLocations();
                    } else if (type.equals(MEMORIES)) {
                        reply = JsonUtil.getMemories();
                    } else if (type.equals(METADATA)) {
                        reply = JsonUtil.getMetadata();
                    } else if (type.equals(PANELS)) {
                        reply = JsonUtil.getPanels((request.getParameter(FORMAT) != null) ? request.getParameter(FORMAT) : XML);
                    } else if (type.equals(POWER)) {
                        reply = JsonUtil.getPower();
                    } else if (type.equals(RAILROAD)) {
                        reply = JsonUtil.getRailroad();
                    } else if (type.equals(REPORTERS)) {
                        reply = JsonUtil.getReporters();
                    } else if (type.equals(ROSTER)) {
                        reply = JsonUtil.getRoster();
                    } else if (type.equals(ROUTES)) {
                        reply = JsonUtil.getRoutes();
                    } else if (type.equals(SENSORS)) {
                        reply = JsonUtil.getSensors();
                    } else if (type.equals(SIGNAL_HEADS)) {
                        reply = JsonUtil.getSignalHeads();
                    } else if (type.equals(SIGNAL_MASTS)) {
                        reply = JsonUtil.getSignalMasts();
                    } else if (type.equals(TRAINS)) {
                        reply = JsonUtil.getTrains();
                    } else if (type.equals(TURNOUTS)) {
                        reply = JsonUtil.getTurnouts();
                    } else if (type.equals(HELLO)) {
                        reply = JsonUtil.getHello(JsonServerManager.getJsonServerPreferences().getHeartbeatInterval());
                    } else if (type.equals(NETWORK_SERVICES)) {
                        reply = JsonUtil.getNetworkServices();
                    } else if (type.equals(NODE)) {
                        reply = JsonUtil.getNode();
                    } else {
                        log.warn("Type {} unknown.", type);
                        reply = JsonUtil.getUnknown(type);
                    }
                } else {
                    if (type.equals(CAR)) {
                        reply = JsonUtil.getCar(name);
                    } else if (type.equals(CONSIST)) {
                        reply = JsonUtil.getConsist(JsonUtil.addressForString(name));
                    } else if (type.equals(ENGINE)) {
                        reply = JsonUtil.getEngine(name);
                    } else if (type.equals(LIGHT)) {
                        reply = JsonUtil.getLight(name);
                    } else if (type.equals(LOCATION)) {
                        reply = JsonUtil.getLocation(name);
                    } else if (type.equals(MEMORY)) {
                        reply = JsonUtil.getMemory(name);
                    } else if (type.equals(METADATA)) {
                        reply = JsonUtil.getMetadata(name);
                    } else if (type.equals(REPORTER)) {
                        reply = JsonUtil.getReporter(name);
                    } else if (type.equals(ROSTER_ENTRY) || type.equals(ROSTER)) {
                        reply = JsonUtil.getRosterEntry(name);
                    } else if (type.equals(ROUTE)) {
                        reply = JsonUtil.getRoute(name);
                    } else if (type.equals(SENSOR)) {
                        reply = JsonUtil.getSensor(name);
                    } else if (type.equals(SIGNAL_HEAD)) {
                        reply = JsonUtil.getSignalHead(name);
                    } else if (type.equals(SIGNAL_MAST)) {
                        reply = JsonUtil.getSignalMast(name);
                    } else if (type.equals(TRAIN)) {
                        reply = JsonUtil.getTrain(name);
                    } else if (type.equals(TURNOUT)) {
                        reply = JsonUtil.getTurnout(name);
                    } else {
                        log.warn("Type {} unknown.", type);
                        reply = JsonUtil.getUnknown(type);
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
            response.setContentType("text/html"); // NOI18N
            response.getWriter().println(String.format(ResourceBundle.getBundle("jmri.web.server.Html").getString("HeadFormat"), // NOI18N
                    ResourceBundle.getBundle("jmri.web.server.Html").getString("HTML5DocType"), // NOI18N
                    "JSON Console", // NOI18N
                    JsonServlet.class.getSimpleName(),
                    ResourceBundle.getBundle("jmri.web.servlet.json.JsonHtml").getString("HeadAdditions"))); // NOI18N
            response.getWriter().println(ResourceBundle.getBundle("jmri.web.servlet.json.JsonHtml").getString("BodyContent")); // NOI18N
            response.getWriter().println(String.format(ResourceBundle.getBundle("jmri.web.server.Html").getString("TailFormat"), "", "")); // NOI18N
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Date now = new Date();
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json"); // NOI18N
        response.setHeader("Connection", "Keep-Alive"); // NOI18N
        response.setDateHeader("Date", now.getTime()); // NOI18N
        response.setDateHeader("Last-Modified", now.getTime()); // NOI18N
        response.setDateHeader("Expires", now.getTime()); // NOI18N

        String[] rest = request.getPathInfo().split("/"); // NOI18N
        String type = (rest.length > 1) ? rest[1] : null;
        String name = (rest.length > 2) ? rest[2] : null;
        JsonNode data;
        JsonNode reply;
        try {
            if (request.getContentType().contains("application/json")) {
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
                if (name != null) {
                    if (type.equals(CONSIST)) {
                        JsonUtil.setConsist(JsonUtil.addressForString(name), data);
                        reply = JsonUtil.getConsist(JsonUtil.addressForString(name));
                    } else if (type.equals(LIGHT)) {
                        JsonUtil.setLight(name, data);
                        reply = JsonUtil.getLight(name);
                    } else if (type.equals(MEMORY)) {
                        JsonUtil.setMemory(name, data);
                        reply = JsonUtil.getMemory(name);
                    } else if (type.equals(POWER)) {
                        JsonUtil.setPower(data);
                        reply = JsonUtil.getPower();
                    } else if (type.equals(REPORTER)) {
                        JsonUtil.setReporter(name, data);
                        reply = JsonUtil.getReporter(name);
                    } else if (type.equals(ROUTE)) {
                        JsonUtil.setRoute(name, data);
                        reply = JsonUtil.getRoute(name);
                    } else if (type.equals(SENSOR)) {
                        JsonUtil.setSensor(name, data);
                        reply = JsonUtil.getSensor(name);
                    } else if (type.equals(SIGNAL_HEAD)) {
                        JsonUtil.setSignalHead(name, data);
                        reply = JsonUtil.getSignalHead(name);
                    } else if (type.equals(SIGNAL_MAST)) {
                        JsonUtil.setSignalMast(name, data);
                        reply = JsonUtil.getSignalMast(name);
                    } else if (type.equals(TRAIN)) {
                        JsonUtil.setTrain(name, data);
                        reply = JsonUtil.getTrain(name);
                    } else if (type.equals(TURNOUT)) {
                        JsonUtil.setTurnout(name, data);
                        reply = JsonUtil.getTurnout(name);
                    } else {
                        log.warn("Type {} unknown.", type);
                        reply = JsonUtil.getUnknown(type);
                    }
                } else {
                    log.error("Name must be defined.");
                    throw new JsonException(400, "Name must be defined."); // Need to I18N
                }
            } else {
                log.warn("Type not specified.");
                reply = JsonUtil.getUnknown(type);
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
        Date now = new Date();
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json"); // NOI18N
        response.setHeader("Connection", "Keep-Alive"); // NOI18N
        response.setDateHeader("Date", now.getTime()); // NOI18N
        response.setDateHeader("Last-Modified", now.getTime()); // NOI18N
        response.setDateHeader("Expires", now.getTime()); // NOI18N

        String[] rest = request.getPathInfo().split("/"); // NOI18N
        String type = (rest.length > 1) ? rest[1] : null;
        String name = (rest.length > 2) ? rest[2] : null;
        JsonNode data;
        JsonNode reply;
        try {
            if (request.getContentType().contains("application/json")) {
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
                    if (type.equals(CONSIST)) {
                        JsonUtil.putConsist(JsonUtil.addressForString(name), data);
                        reply = JsonUtil.getConsist(JsonUtil.addressForString(name));
                    } else if (type.equals(LIGHT)) {
                        JsonUtil.putLight(name, data);
                        reply = JsonUtil.getLight(name);
                    } else if (type.equals(MEMORY)) {
                        JsonUtil.putMemory(name, data);
                        reply = JsonUtil.getMemory(name);
                    } else if (type.equals(POWER)) {
                        JsonUtil.setPower(data);
                        reply = JsonUtil.getPower();
                    } else if (type.equals(REPORTER)) {
                        JsonUtil.putReporter(name, data);
                        reply = JsonUtil.getReporter(name);
                    } else if (type.equals(SENSOR)) {
                        JsonUtil.putSensor(name, data);
                        reply = JsonUtil.getSensor(name);
                    } else if (type.equals(TURNOUT)) {
                        JsonUtil.putTurnout(name, data);
                        reply = JsonUtil.getTurnout(name);
                    } else {
                        // not a creatable item
                        throw new JsonException(400, type + " is not a creatable type"); // need to I18N
                    }
                } else {
                    log.warn("Type {} unknown.", type);
                    reply = JsonUtil.getUnknown(type);
                }
            } else {
                log.warn("Type not specified.");
                reply = JsonUtil.getUnknown(type);
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
        Date now = new Date();
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json"); // NOI18N
        response.setHeader("Connection", "Keep-Alive"); // NOI18N
        response.setDateHeader("Date", now.getTime()); // NOI18N
        response.setDateHeader("Last-Modified", now.getTime()); // NOI18N
        response.setDateHeader("Expires", now.getTime()); // NOI18N

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
                    JsonUtil.delConsist(JsonUtil.addressForString(name));
                } else {
                    // not a deletable item
                    throw new JsonException(400, type + " is not a deletable type"); // need to I18N
                }
            } else {
                log.warn("Type not specified.");
                reply = JsonUtil.getUnknown(type);
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

    public class JsonWebSocket implements WebSocket.OnTextMessage {

        protected Connection wsConnection;
        protected JmriConnection jmriConnection;
        protected ObjectMapper mapper;
        protected JsonClientHandler handler;

        public void sendMessage(String message) throws IOException {
            this.wsConnection.sendMessage(message);
        }

        @Override
        public void onOpen(Connection cnctn) {
            log.debug("Opening connnection");
            this.wsConnection = cnctn;
            this.jmriConnection = new JmriConnection(this.wsConnection);
            this.wsConnection.setMaxIdleTime(JsonServerManager.getJsonServerPreferences().getHeartbeatInterval());
            this.mapper = new ObjectMapper();
            this.handler = new JsonClientHandler(this.jmriConnection, this.mapper);
            sockets.add(this);
            try {
                log.debug("Sending hello");
                this.handler.sendHello(this.wsConnection.getMaxIdleTime());
            } catch (IOException e) {
                log.warn("Error openning WebSocket:\n{}", e.getMessage(), e);
                this.wsConnection.close();
                sockets.remove(this);
            }
        }

        @Override
        public void onClose(int i, String string) {
            log.debug("Closing connection because {} ({})", string, i);
            this.handler.onClose();
            sockets.remove(this);
        }

        @Override
        public void onMessage(String string) {
            try {
                this.handler.onMessage(string);
            } catch (IOException e) {
                log.error("Error on WebSocket message:\n{}", e.getMessage(), e);
                this.wsConnection.close();
                sockets.remove(this);
            }
        }
    }
}