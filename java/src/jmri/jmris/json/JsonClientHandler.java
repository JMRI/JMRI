package jmri.jmris.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.ServiceLoader;
import jmri.JmriException;
import static jmri.jmris.json.JSON.CARS;
import static jmri.jmris.json.JSON.CONSIST;
import static jmri.jmris.json.JSON.CONSISTS;
import static jmri.jmris.json.JSON.DATA;
import static jmri.jmris.json.JSON.ENGINES;
import static jmri.jmris.json.JSON.FORMAT;
import static jmri.jmris.json.JSON.GOODBYE;
import static jmri.jmris.json.JSON.HELLO;
import static jmri.jmris.json.JSON.LIGHT;
import static jmri.jmris.json.JSON.LIGHTS;
import static jmri.jmris.json.JSON.LIST;
import static jmri.jmris.json.JSON.LOCALE;
import static jmri.jmris.json.JSON.LOCATIONS;
import static jmri.jmris.json.JSON.MEMORIES;
import static jmri.jmris.json.JSON.MEMORY;
import static jmri.jmris.json.JSON.METADATA;
import static jmri.jmris.json.JSON.METHOD;
import static jmri.jmris.json.JSON.NAME;
import static jmri.jmris.json.JSON.NETWORK_SERVICES;
import static jmri.jmris.json.JSON.PANELS;
import static jmri.jmris.json.JSON.PING;
import static jmri.jmris.json.JSON.PONG;
import static jmri.jmris.json.JSON.PROGRAMMER;
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
import static jmri.jmris.json.JSON.SYSTEM_CONNECTIONS;
import static jmri.jmris.json.JSON.THROTTLE;
import static jmri.jmris.json.JSON.TRAIN;
import static jmri.jmris.json.JSON.TRAINS;
import static jmri.jmris.json.JSON.TURNOUT;
import static jmri.jmris.json.JSON.TURNOUTS;
import static jmri.jmris.json.JSON.TYPE;
import static jmri.jmris.json.JSON.XML;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
import jmri.server.json.JsonSocketService;
import jmri.spi.JsonServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonClientHandler {

    private final JsonConsistServer consistServer;
    private final JsonLightServer lightServer;
    private final JsonMemoryServer memoryServer;
    private final JsonOperationsServer operationsServer;
    private final JsonProgrammerServer programmerServer;
    private final JsonReporterServer reporterServer;
    private final JsonRosterServer rosterServer;
    private final JsonRouteServer routeServer;
    private final JsonSensorServer sensorServer;
    private final JsonSignalHeadServer signalHeadServer;
    private final JsonSignalMastServer signalMastServer;
    private final JsonThrottleServer throttleServer;
    private final JsonTurnoutServer turnoutServer;
    private final JsonConnection connection;
    private final ObjectMapper mapper;
    private final HashMap<String, HashSet<JsonSocketService>> services = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(JsonClientHandler.class);

    public JsonClientHandler(JsonConnection connection, ObjectMapper mapper) {
        this.connection = connection;
        this.mapper = mapper;
        this.consistServer = new JsonConsistServer(this.connection);
        this.lightServer = new JsonLightServer(this.connection);
        this.memoryServer = new JsonMemoryServer(this.connection);
        this.operationsServer = new JsonOperationsServer(this.connection);
        this.programmerServer = new JsonProgrammerServer(this.connection);
        this.reporterServer = new JsonReporterServer(this.connection);
        this.rosterServer = new JsonRosterServer(this.connection);
        this.routeServer = new JsonRouteServer(this.connection);
        this.sensorServer = new JsonSensorServer(this.connection);
        this.signalHeadServer = new JsonSignalHeadServer(this.connection);
        this.signalMastServer = new JsonSignalMastServer(this.connection);
        this.throttleServer = new JsonThrottleServer(this.connection);
        this.turnoutServer = new JsonTurnoutServer(this.connection);
        for (JsonServiceFactory factory : ServiceLoader.load(JsonServiceFactory.class)) {
            for (String type : factory.getTypes()) {
                HashSet<JsonSocketService> set = this.services.get(type);
                if (set == null) {
                    this.services.put(type, new HashSet<>());
                    set = this.services.get(type);
                }
                set.add(factory.getSocketService(connection));
            }
        }
    }

    public void dispose() {
        this.throttleServer.dispose();
        this.consistServer.dispose();
        this.lightServer.dispose();
        this.memoryServer.dispose();
        this.operationsServer.dispose();
        this.programmerServer.dispose();
        this.reporterServer.dispose();
        this.rosterServer.dispose();
        this.routeServer.dispose();
        this.sensorServer.dispose();
        this.signalHeadServer.dispose();
        this.signalMastServer.dispose();
        this.turnoutServer.dispose();
        services.values().stream().forEach((set) -> {
            set.stream().forEach((service) -> {
                service.onClose();
            });
        });
    }

    /**
     * Process a JSON string and handle appropriately.
     *
     * Currently JSON strings in four different forms are handled by this
     * method:<ul> <li>list requests in the form:
     * <code>{"type":"list","list":"trains"}</code> or
     * <code>{"list":"trains"}</code> that are passed to the JsonUtil for
     * handling.</li> <li>individual item state requests in the form:
     * <code>{"type":"turnout","data":{"name":"LT14"}}</code> that are passed to
     * type-specific handlers. In addition to the initial response, these
     * requests will initiate "listeners", which will send updated responses
     * every time the item's state changes.<ul>
     * <li>an item's state can be set by adding a <strong>state</strong> node to
     * the <em>data</em> node:
     * <code>{"type":"turnout","data":{"name":"LT14","state":4}}</code>
     * <li>individual types can be created if a <strong>method</strong> node
     * with the value <em>put</em> is included in message:
     * <code>{"type":"turnout","method":"put","data":{"name":"LT14"}}</code>.
     * The <em>method</em> node may be included in the <em>data</em> node:
     * <code>{"type":"turnout","data":{"name":"LT14","method":"put"}}</code>
     * Note that not all types support this.</li></ul>
     * </li><li>a heartbeat in the form <code>{"type":"ping"}</code>. The
     * heartbeat gets a <code>{"type":"pong"}</code> response.</li> <li>a sign
     * off in the form: <code>{"type":"goodbye"}</code> to which an identical
     * response is sent before the connection gets closed.</li></ul>
     *
     * @param string
     * @throws IOException
     */
    public void onMessage(String string) throws IOException {
        log.debug("Received from client: {}", string);
        try {
            this.onMessage(this.mapper.readTree(string));
        } catch (JsonProcessingException pe) {
            log.warn("Exception processing \"{}\"\n{}", string, pe.getMessage());
            this.sendErrorMessage(500, Bundle.getMessage(this.connection.getLocale(), "ErrorProcessingJSON", pe.getLocalizedMessage()));
        }
    }

    /**
     * Process a JSON node and handle appropriately.
     *
     * See {@link #onMessage(java.lang.String) } for expected JSON objects.
     *
     * @see #onMessage(java.lang.String)
     * @param root
     * @throws IOException
     */
    public void onMessage(JsonNode root) throws IOException {
        try {
            String type = root.path(TYPE).asText();
            if (root.path(TYPE).isMissingNode() && root.path(LIST).isValueNode()) {
                type = LIST;
            }
            JsonNode data = root.path(DATA);
            if (data.path(METHOD).isMissingNode() && root.path(METHOD).isValueNode()) {
                ((ObjectNode) data).put(METHOD, root.path(METHOD).asText());
            }
            log.debug("Processing {} with {}", type, data);
            if (type.equals(PING)) {
                this.connection.sendMessage(this.mapper.writeValueAsString(this.mapper.createObjectNode().put(TYPE, PONG)));
            } else if (type.equals(GOODBYE)) {
                this.connection.sendMessage(this.mapper.writeValueAsString(this.mapper.createObjectNode().put(TYPE, GOODBYE)));
                this.connection.close();
            } else if (type.equals(HELLO)) {
                this.receiveHello(data);
                this.sendHello(JsonServerPreferences.getDefault().getHeartbeatInterval());
            } else if (type.equals(LOCALE)) {
                this.receiveHello(data);
            } else if (type.equals(LIST)) {
                JsonNode reply;
                String list = root.path(LIST).asText();
                switch (list) {
                    case CARS:
                        reply = JsonUtil.getCars(this.connection.getLocale());
                        break;
                    case CONSISTS:
                        reply = JsonUtil.getConsists(this.connection.getLocale());
                        break;
                    case ENGINES:
                        reply = JsonUtil.getEngines(this.connection.getLocale());
                        break;
                    case LIGHTS:
                        reply = JsonUtil.getLights(this.connection.getLocale());
                        break;
                    case LOCATIONS:
                        reply = JsonUtil.getLocations(this.connection.getLocale());
                        break;
                    case MEMORIES:
                        reply = JsonUtil.getMemories(this.connection.getLocale());
                        break;
                    case METADATA:
                        reply = JsonUtil.getMetadata(this.connection.getLocale());
                        break;
                    case PANELS:
                        reply = JsonUtil.getPanels(this.connection.getLocale(), (data.path(FORMAT).isMissingNode()) ? XML : data.path(FORMAT).asText());
                        break;
                    case REPORTERS:
                        reply = JsonUtil.getReporters(this.connection.getLocale());
                        break;
                    case ROSTER:
                        reply = JsonUtil.getRoster(this.connection.getLocale(), data);
                        this.rosterServer.listen();
                        break;
                    case ROSTER_GROUPS:
                        reply = JsonUtil.getRosterGroups(this.connection.getLocale());
                        this.rosterServer.listen();
                        break;
                    case ROUTES:
                        reply = JsonUtil.getRoutes(this.connection.getLocale());
                        break;
                    case SENSORS:
                        reply = JsonUtil.getSensors(this.connection.getLocale());
                        break;
                    case SIGNAL_HEADS:
                        reply = JsonUtil.getSignalHeads(this.connection.getLocale());
                        break;
                    case SIGNAL_MASTS:
                        reply = JsonUtil.getSignalMasts(this.connection.getLocale());
                        break;
                    case TRAINS:
                        reply = JsonUtil.getTrains(this.connection.getLocale());
                        break;
                    case TURNOUTS:
                        reply = JsonUtil.getTurnouts(this.connection.getLocale());
                        break;
                    case NETWORK_SERVICES:
                        reply = JsonUtil.getNetworkServices(this.connection.getLocale());
                        break;
                    case SYSTEM_CONNECTIONS:
                        reply = JsonUtil.getSystemConnections(this.connection.getLocale());
                        break;
                    default:
                        if (this.services.get(list) != null) {
                            for (JsonSocketService service : this.services.get(list)) {
                                service.onList(list, data, this.connection.getLocale());
                            }
                            return;
                        } else {
                            this.sendErrorMessage(404, Bundle.getMessage(this.connection.getLocale(), "ErrorUnknownList", list));
                            return;
                        }
                }
                //log.debug("Sending to client: {}", this.mapper.writeValueAsString(reply));
                this.connection.sendMessage(this.mapper.writeValueAsString(reply));
            } else if (!data.isMissingNode()) {
                switch (type) {
                    case CONSIST:
                        this.consistServer.parseRequest(this.connection.getLocale(), data);
                        break;
                    case LIGHT:
                        this.lightServer.parseRequest(this.connection.getLocale(), data);
                        break;
                    case MEMORY:
                        this.memoryServer.parseRequest(this.connection.getLocale(), data);
                        break;
                    case METADATA:
                        this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.getMetadata(this.connection.getLocale(), data.path(NAME).asText())));
                        break;
                    case PROGRAMMER:
                        this.programmerServer.parseRequest(this.connection.getLocale(), data);
                        break;
                    case SENSOR:
                        this.sensorServer.parseRequest(this.connection.getLocale(), data);
                        break;
                    case SIGNAL_HEAD:
                        this.signalHeadServer.parseRequest(this.connection.getLocale(), data);
                        break;
                    case SIGNAL_MAST:
                        this.signalMastServer.parseRequest(this.connection.getLocale(), data);
                        break;
                    case REPORTER:
                        this.reporterServer.parseRequest(this.connection.getLocale(), data);
                        break;
                    case ROSTER_ENTRY:
                        this.rosterServer.parseRosterEntryRequest(this.connection.getLocale(), data);
                        break;
                    case ROSTER_GROUP:
                        this.rosterServer.parseRosterGroupRequest(this.connection.getLocale(), data);
                        break;
                    case ROUTE:
                        this.routeServer.parseRequest(this.connection.getLocale(), data);
                        break;
                    case THROTTLE:
                        this.throttleServer.parseRequest(this.connection.getLocale(), data);
                        break;
                    case TRAIN:
                        this.operationsServer.parseTrainRequest(this.connection.getLocale(), data);
                        break;
                    case TURNOUT:
                        this.turnoutServer.parseRequest(this.connection.getLocale(), data);
                        break;
                    default:
                        if (this.services.get(type) != null) {
                            for (JsonSocketService service : this.services.get(type)) {
                                service.onMessage(type, data, this.connection.getLocale());
                            }
                        } else {
                            this.sendErrorMessage(404, Bundle.getMessage(this.connection.getLocale(), "ErrorUnknownType", type));
                        }
                        break;
                }
            } else {
                this.sendErrorMessage(400, Bundle.getMessage(this.connection.getLocale(), "ErrorMissingData"));
            }
        } catch (JmriException je) {
            this.sendErrorMessage(500, Bundle.getMessage(this.connection.getLocale(), "ErrorUnsupportedOperation", je.getLocalizedMessage()));
        } catch (JsonException je) {
            this.sendErrorMessage(je);
        }
    }

    private void receiveHello(JsonNode data) {
        if (!data.path(LOCALE).isMissingNode()) {
            this.connection.setLocale(Locale.forLanguageTag(data.path(LOCALE).asText()));
        }
    }

    public void sendHello(int heartbeat) throws IOException {
        this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.getHello(this.connection.getLocale(), heartbeat)));
    }

    private void sendErrorMessage(int code, String message) throws IOException {
        JsonException ex = new JsonException(code, message);
        this.sendErrorMessage(ex);
    }

    private void sendErrorMessage(JsonException ex) throws IOException {
        this.connection.sendMessage(this.mapper.writeValueAsString(ex.getJsonMessage()));
    }
}
