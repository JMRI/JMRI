// JsonServlet.java
package jmri.web.servlet.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static jmri.jmris.json.JSON.*;
import jmri.jmris.json.JsonClientHandler;
import jmri.jmris.json.JsonException;
import jmri.jmris.json.JsonLister;
import jmri.jmris.json.JsonServerManager;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide JSON formatted responses for requests to GET requests for information
 * from the JMRI Web Server.
 *
 * Note that unlike the XMLIO server, this server does not monitor items in
 * response to a GET or POST request, but does provide a WebSocket for clients
 * capable of using WebSockets to provide that capability.
 *
 * @author rhwood Copyright (C) 2012, 2013
 */
// TODO: add POST handler for creating new objects in collections
public class JsonServlet extends WebSocketServlet {

    private static final long serialVersionUID = -671593634343578915L;
    private ObjectMapper mapper;
    private final Set<JsonWebSocket> sockets = new CopyOnWriteArraySet<JsonWebSocket>();
    private static Logger log = LoggerFactory.getLogger(JsonServlet.class);

    public JsonServlet() {
        super();
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
                    } catch (Exception e) {
                        log.warn("Unable to send goodbye while closing socket.\n" + e.getMessage());
                    }
                    socket.wsConnection.close();
                }
                return true;
            }
        });
    }

    @Override
    public WebSocket doWebSocketConnect(HttpServletRequest hsr, String string) {
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
     * <li>{"type":"list","list":[{"type":"sensor","data":{"name":"IS22","userName":"FarEast","comment":null,"inverted":false,"state":4}}]}</li>
     * </ul>
     * note that data will vary for each type
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
            if (name == null) {
                if (type.equals(CARS)) {
                    reply = JsonLister.getCars();
                } else if (type.equals(ENGINES)) {
                    reply = JsonLister.getEngines();
                } else if (type.equals(LIGHTS)) {
                    reply = JsonLister.getLights();
                } else if (type.equals(LOCATIONS)) {
                    reply = JsonLister.getLocations();
                } else if (type.equals(MEMORIES)) {
                    reply = JsonLister.getMemories();
                } else if (type.equals(METADATA)) {
                    reply = JsonLister.getMetadata();
                } else if (type.equals(PANELS)) {
                    reply = JsonLister.getPanels();
                } else if (type.equals(POWER)) {
                    reply = JsonLister.getPower();
                } else if (type.equals(RAILROAD)) {
                    reply = JsonLister.getRailroad();
                } else if (type.equals(REPORTERS)) {
                    reply = JsonLister.getReporters();
                } else if (type.equals(ROSTER)) {
                    reply = JsonLister.getRoster();
                } else if (type.equals(ROUTES)) {
                    reply = JsonLister.getRoutes();
                } else if (type.equals(SENSORS)) {
                    reply = JsonLister.getSensors();
                } else if (type.equals(SIGNAL_HEADS)) {
                    reply = JsonLister.getSignalHeads();
                } else if (type.equals(SIGNAL_MASTS)) {
                    reply = JsonLister.getSignalMasts();
                } else if (type.equals(TRAINS)) {
                    reply = JsonLister.getTrains();
                } else if (type.equals(TURNOUTS)) {
                    reply = JsonLister.getTurnouts();
                } else {
                    log.warn("Type \"" + type + "\" unknown.");
                    reply = JsonLister.getUnknown(type);
                }
            } else {
                // NOTE: use of singular paths is depricated and will be removed after 3.4 is released
                if (type.equals(CAR) || type.equals(CARS)) {
                    reply = JsonLister.getCar(name);
                } else if (type.equals(ENGINE) || type.equals(ENGINES)) {
                    reply = JsonLister.getEngine(name);
                } else if (type.equals(LIGHT) || type.equals(LIGHTS)) {
                    reply = JsonLister.getLight(name);
                } else if (type.equals(LOCATION) || type.equals(LOCATIONS)) {
                    reply = JsonLister.getLocation(name);
                } else if (type.equals(MEMORY) || type.equals(MEMORIES)) {
                    reply = JsonLister.getMemory(name);
                } else if (type.equals(REPORTER) || type.equals(REPORTERS)) {
                    reply = JsonLister.getReporter(name);
                } else if (type.equals(ROSTER_ENTRY) || type.equals(ROSTER)) {
                    reply = JsonLister.getRosterEntry(name);
                } else if (type.equals(ROUTE) || type.equals(ROUTES)) {
                    reply = JsonLister.getRoute(name);
                } else if (type.equals(SENSOR) || type.equals(SENSORS)) {
                    reply = JsonLister.getSensor(name);
                } else if (type.equals(SIGNAL_HEAD) || type.equals(SIGNAL_HEADS)) {
                    reply = JsonLister.getSignalHead(name);
                } else if (type.equals(SIGNAL_MAST) || type.equals(SIGNAL_MASTS)) {
                    reply = JsonLister.getSignalMast(name);
                } else if (type.equals(TRAIN) || type.equals(TRAINS)) {
                    reply = JsonLister.getTrain(name);
                } else if (type.equals(TURNOUT) || type.equals(TURNOUTS)) {
                    reply = JsonLister.getTurnout(name);
                } else {
                    log.warn("Type \"" + type + "\" unknown.");
                    reply = JsonLister.getUnknown(type);
                }
            }
            int code = reply.path(DATA).path(CODE).asInt(200); // use HTTP error codes when possible
            if (code == 200) {
                response.getWriter().write(this.mapper.writeValueAsString(reply));
            } else {
                response.sendError(code, this.mapper.writeValueAsString(reply));
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
    // TODO: need to build JSON object from request parameters, and pass that to the setter instead
    // this will allow setters to modify any attribute of the object that the JsonLister is aware of
    // and make the POST/create object support so much simpler
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Date now = new Date();
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json"); // NOI18N
        response.setHeader("Connection", "Keep-Alive"); // NOI18N
        response.setDateHeader("Date", now.getTime()); // NOI18N
        response.setDateHeader("Last-Modified", now.getTime()); // NOI18N
        response.setDateHeader("Expires", now.getTime()); // NOI18N

        String[] rest = request.getPathInfo().split("/"); // NOI18N
        int state = -1;
        if (request.getParameter(STATE) != null) {
            state = Integer.parseInt(request.getParameter(STATE));
        }
        String value = request.getParameter(VALUE);
        String location = request.getParameter(LOCATION);
        String valueType = request.getParameter(TYPE);
        String type = (rest.length > 1) ? rest[1] : null;
        if (type != null) {
            String name = (rest.length > 2) ? rest[2] : null;
            JsonNode reply;
            if (type.equals(POWER)) {
                // power is uniquely global, so a name is not required
                try {
                    if (state != -1) {
                        JsonLister.setPower(state);
                    }
                    reply = JsonLister.getPower();
                } catch (JsonException ex) {
                    reply = ex.getJsonMessage();
                }
            } else if (name != null) {
                try {
                    if (state != -1) {
                        if (type.equals(LIGHTS)) {
                            JsonLister.setLight(name, state);
                        } else if (type.equals(ROUTES)) {
                            JsonLister.setRoute(name, state);
                        } else if (type.equals(SENSORS)) {
                            JsonLister.setSensor(name, state);
                        } else if (type.equals(SIGNAL_HEADS)) {
                            JsonLister.setSignalHead(name, state);
                        } else if (type.equals(TURNOUTS)) {
                            JsonLister.setTurnout(name, state);
                        } else {
                            // not a settable item
                            throw new JsonException(400, type + " is not a settable type"); // need to I18N
                        }
                    } else if (value != null) {
                        if (type.equals(MEMORIES)) {
                            JsonLister.setMemory(name, value, valueType);
                        } else if (type.equals(REPORTERS)) {
                            JsonLister.setReporter(name, value, valueType);
                        } else if (type.equals(SIGNAL_MASTS)) {
                            JsonLister.setSignalMast(name, value, valueType);
                        } else {
                            // not a settable item
                            throw new JsonException(400, type + " is not a settable type"); // need to I18N
                        }
                    } else if (location != null) {
                        if (type.equals(TRAINS)) {
                            JsonLister.setTrainLocation(name, location);
                        } else {
                            // not a settable item
                            throw new JsonException(400, "location cannot be set for type " + type); // need to I18N
                        }
                    }
                    if (type.equals(LIGHTS)) {
                        reply = JsonLister.getLight(name);
                    } else if (type.equals(MEMORIES)) {
                        reply = JsonLister.getMemory(name);
                    } else if (type.equals(REPORTERS)) {
                        reply = JsonLister.getReporter(name);
                    } else if (type.equals(ROUTES)) {
                        reply = JsonLister.getRoute(name);
                    } else if (type.equals(SENSORS)) {
                        reply = JsonLister.getSensor(name);
                    } else if (type.equals(SIGNAL_HEADS)) {
                        reply = JsonLister.getSignalHead(name);
                    } else if (type.equals(SIGNAL_MASTS)) {
                        reply = JsonLister.getSignalMast(name);
                    } else if (type.equals(TRAINS)) {
                        reply = JsonLister.getTrain(name);
                    } else if (type.equals(TURNOUTS)) {
                        reply = JsonLister.getTurnout(name);
                    } else {
                        log.warn("Type \"" + type + "\" unknown.");
                        reply = JsonLister.getUnknown(type);
                    }
                } catch (JsonException ex) {
                    reply = ex.getJsonMessage();
                }
            } else {
                log.warn("Type \"" + type + "\" unknown.");
                reply = JsonLister.getUnknown(type);
            }
            int code = reply.path(DATA).path(CODE).asInt(200); // use HTTP error codes when possible
            if (code == 200) {
                response.getWriter().write(this.mapper.writeValueAsString(reply));
            } else {
                response.sendError(code, this.mapper.writeValueAsString(reply));
            }
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
                throw new JsonException(400, "POST request must be a JSON object"); // need to I18N
            }
            if (name == null || type == null) {
                log.warn("Type \"" + type + "\" unknown.");
                reply = JsonLister.getUnknown(type);
            } else {
                if (type.equals(TURNOUTS)) {
                    JsonLister.putTurnout(name, data);
                } else {
                    // not a creatable item
                    throw new JsonException(400, type + " is not a creatable type"); // need to I18N
                }
                if (type.equals(TURNOUTS)) {
                    reply = JsonLister.getTurnout(name);
                } else {
                    log.warn("Type \"" + type + "\" unknown.");
                    reply = JsonLister.getUnknown(type);
                }
            }
        } catch (JsonException ex) {
            reply = ex.getJsonMessage();
        }
        int code = reply.path(DATA).path(CODE).asInt(200); // use HTTP error codes when possible
        if (code == 200) {
            response.getWriter().write(this.mapper.writeValueAsString(reply));
        } else {
            response.sendError(code, this.mapper.writeValueAsString(reply));
        }
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
            this.wsConnection = cnctn;
            this.jmriConnection = new JmriConnection(this.wsConnection);
            this.wsConnection.setMaxIdleTime(JsonServerManager.getJsonServerPreferences().getHeartbeatInterval());
            this.mapper = new ObjectMapper();
            this.handler = new JsonClientHandler(this.jmriConnection);
            sockets.add(this);
            try {
                this.handler.sendHello(this.wsConnection.getMaxIdleTime());
            } catch (IOException e) {
                log.warn(e.getMessage(), e);
                this.wsConnection.close();
                sockets.remove(this);
            }
        }

        @Override
        public void onClose(int i, String string) {
            this.handler.onClose();
            sockets.remove(this);
        }

        @Override
        public void onMessage(String string) {
            try {
                this.handler.onMessage(string);
            } catch (IOException e) {
                log.warn(e.getMessage(), e);
                this.wsConnection.close();
                sockets.remove(this);
            }
        }
    }
}
