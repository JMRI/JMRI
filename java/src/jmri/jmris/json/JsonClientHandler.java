package jmri.jmris.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import jmri.JmriException;
import jmri.jmris.JmriConnection;
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
import static jmri.jmris.json.JSON.LOCATIONS;
import static jmri.jmris.json.JSON.MEMORIES;
import static jmri.jmris.json.JSON.MEMORY;
import static jmri.jmris.json.JSON.METADATA;
import static jmri.jmris.json.JSON.NAME;
import static jmri.jmris.json.JSON.NETWORK_SERVICES;
import static jmri.jmris.json.JSON.OPERATIONS;
import static jmri.jmris.json.JSON.PANELS;
import static jmri.jmris.json.JSON.PING;
import static jmri.jmris.json.JSON.PONG;
import static jmri.jmris.json.JSON.POWER;
import static jmri.jmris.json.JSON.PROGRAMMER;
import static jmri.jmris.json.JSON.REPORTER;
import static jmri.jmris.json.JSON.REPORTERS;
import static jmri.jmris.json.JSON.ROSTER;
import static jmri.jmris.json.JSON.ROSTER_ENTRY;
import static jmri.jmris.json.JSON.ROSTER_GROUPS;
import static jmri.jmris.json.JSON.ROUTE;
import static jmri.jmris.json.JSON.ROUTES;
import static jmri.jmris.json.JSON.SENSOR;
import static jmri.jmris.json.JSON.SENSORS;
import static jmri.jmris.json.JSON.SIGNAL_HEAD;
import static jmri.jmris.json.JSON.SIGNAL_HEADS;
import static jmri.jmris.json.JSON.SIGNAL_MAST;
import static jmri.jmris.json.JSON.SIGNAL_MASTS;
import static jmri.jmris.json.JSON.THROTTLE;
import static jmri.jmris.json.JSON.TIME;
import static jmri.jmris.json.JSON.TRAINS;
import static jmri.jmris.json.JSON.TURNOUT;
import static jmri.jmris.json.JSON.TURNOUTS;
import static jmri.jmris.json.JSON.TYPE;
import static jmri.jmris.json.JSON.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonClientHandler {

    private final JsonConsistServer consistServer;
    private final JsonLightServer lightServer;
    private final JsonMemoryServer memoryServer;
    private final JsonOperationsServer operationsServer;
    private final JsonPowerServer powerServer;
    private final JsonProgrammerServer programmerServer;
    private final JsonReporterServer reporterServer;
    private final JsonRouteServer routeServer;
    private final JsonSensorServer sensorServer;
    private final JsonSignalHeadServer signalHeadServer;
    private final JsonSignalMastServer signalMastServer;
    private final JsonThrottleServer throttleServer;
    private final JsonTimeServer timeServer;
    private final JsonTurnoutServer turnoutServer;
    private final JmriConnection connection;
    private final ObjectMapper mapper;
    private static final Logger log = LoggerFactory.getLogger(JsonClientHandler.class);

    public JsonClientHandler(JmriConnection connection, ObjectMapper mapper) {
        this.connection = connection;
        this.mapper = mapper;
        this.consistServer = new JsonConsistServer(this.connection);
        this.lightServer = new JsonLightServer(this.connection);
        this.memoryServer = new JsonMemoryServer(this.connection);
        this.operationsServer = new JsonOperationsServer(this.connection);
        this.powerServer = new JsonPowerServer(this.connection);
        this.programmerServer = new JsonProgrammerServer(this.connection);
        this.reporterServer = new JsonReporterServer(this.connection);
        this.routeServer = new JsonRouteServer(this.connection);
        this.sensorServer = new JsonSensorServer(this.connection);
        this.signalHeadServer = new JsonSignalHeadServer(this.connection);
        this.signalMastServer = new JsonSignalMastServer(this.connection);
        this.throttleServer = new JsonThrottleServer(this.connection);
        this.timeServer = new JsonTimeServer(this.connection);
        this.turnoutServer = new JsonTurnoutServer(this.connection);
    }

    public void dispose() {
        this.throttleServer.dispose();
        this.consistServer.dispose();
        this.lightServer.dispose();
        this.memoryServer.dispose();
        this.operationsServer.dispose();
        this.powerServer.dispose();
        this.programmerServer.dispose();
        this.reporterServer.dispose();
        this.routeServer.dispose();
        this.sensorServer.dispose();
        this.signalHeadServer.dispose();
        this.signalMastServer.dispose();
        this.timeServer.dispose();
        this.turnoutServer.dispose();
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
     * with the value <em>post</em> is included in the <em>data</em> node:
     * <code>{"type":"turnout","data":{"name":"LT14","method":"put"}}</code>
     * Note that not all types support this.</li></ul>
     * </li><li>a heartbeat in the form
     * <code>{"type":"ping"}</code>. The heartbeat gets a
     * <code>{"type":"pong"}</code> response.</li> <li>a sign off in the form:
     * <code>{"type":"goodbye"}</code> to which an identical response is sent
     * before the connection gets closed.</li>
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
            this.sendErrorMessage(500, Bundle.getMessage("ErrorProcessingJSON", pe.getLocalizedMessage()));
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
            log.debug("Processing {} with {}", type, data);
            if (type.equals(PING)) {
                this.connection.sendMessage(this.mapper.writeValueAsString(this.mapper.createObjectNode().put(TYPE, PONG)));
            } else if (type.equals(GOODBYE)) {
                this.connection.sendMessage(this.mapper.writeValueAsString(this.mapper.createObjectNode().put(TYPE, GOODBYE)));
                this.connection.close();
            } else if (type.equals(HELLO)) {
                this.sendHello(JsonServerManager.getJsonServerPreferences().getHeartbeatInterval());
            } else if (type.equals(LIST)) {
                JsonNode reply;
                String list = root.path(LIST).asText();
                if (list.equals(CARS)) {
                    reply = JsonUtil.getCars();
                } else if (list.equals(CONSISTS)) {
                    reply = JsonUtil.getConsists();
                } else if (list.equals(ENGINES)) {
                    reply = JsonUtil.getEngines();
                } else if (list.equals(LIGHTS)) {
                    reply = JsonUtil.getLights();
                } else if (list.equals(LOCATIONS)) {
                    reply = JsonUtil.getLocations();
                } else if (list.equals(MEMORIES)) {
                    reply = JsonUtil.getMemories();
                } else if (list.equals(METADATA)) {
                    reply = JsonUtil.getMetadata();
                } else if (list.equals(PANELS)) {
                    reply = JsonUtil.getPanels((data.path(FORMAT).isMissingNode()) ? XML : data.path(FORMAT).asText());
                } else if (list.equals(REPORTERS)) {
                    reply = JsonUtil.getReporters();
                } else if (list.equals(ROSTER)) {
                    reply = JsonUtil.getRoster(data);
                } else if (list.equals(ROSTER_GROUPS)) {
                    reply = JsonUtil.getRosterGroups();
                } else if (list.equals(ROUTES)) {
                    reply = JsonUtil.getRoutes();
                } else if (list.equals(SENSORS)) {
                    reply = JsonUtil.getSensors();
                } else if (list.equals(SIGNAL_HEADS)) {
                    reply = JsonUtil.getSignalHeads();
                } else if (list.equals(SIGNAL_MASTS)) {
                    reply = JsonUtil.getSignalMasts();
                } else if (list.equals(TRAINS)) {
                    reply = JsonUtil.getTrains();
                } else if (list.equals(TURNOUTS)) {
                    reply = JsonUtil.getTurnouts();
                } else if (list.equals(NETWORK_SERVICES)) {
                    reply = JsonUtil.getNetworkServices();
                } else {
                    this.sendErrorMessage(404, Bundle.getMessage("ErrorUnknownList", list));
                    return;
                }
                //if (log.isDebugEnabled()) log.debug("Sending to client: " + this.mapper.writeValueAsString(reply));
                this.connection.sendMessage(this.mapper.writeValueAsString(reply));
            } else if (!data.isMissingNode()) {
                if (type.equals(CONSIST)) {
                    this.consistServer.parseRequest(data);
                } else if (type.equals(LIGHT)) {
                    this.lightServer.parseRequest(data);
                } else if (type.equals(MEMORY)) {
                    this.memoryServer.parseRequest(data);
                } else if (type.equals(METADATA)) {
                    this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.getMetadata(data.path(NAME).asText())));
                } else if (type.equals(OPERATIONS)) {
                    this.operationsServer.parseRequest(data);
                } else if (type.equals(POWER)) {
                    this.powerServer.parseRequest(data);
                } else if (type.equals(PROGRAMMER)) {
                    this.programmerServer.parseRequest(data);
                } else if (type.equals(SENSOR)) {
                    this.sensorServer.parseRequest(data);
                } else if (type.equals(SIGNAL_HEAD)) {
                    this.signalHeadServer.parseRequest(data);
                } else if (type.equals(SIGNAL_MAST)) {
                    this.signalMastServer.parseRequest(data);
                } else if (type.equals(REPORTER)) {
                    this.reporterServer.parseRequest(data);
                } else if (type.equals(ROSTER_ENTRY)) {
                    this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.getRosterEntry(data.path(NAME).asText())));
                } else if (type.equals(ROUTE)) {
                    this.routeServer.parseRequest(data);
                } else if (type.equals(THROTTLE)) {
                    this.throttleServer.parseRequest(data);
                } else if (type.equals(TIME)) {
                    this.timeServer.parseRequest(data);
                } else if (type.equals(TURNOUT)) {
                    this.turnoutServer.parseRequest(data);
                } else {
                    this.sendErrorMessage(404, Bundle.getMessage("ErrorUnknownType", type));
                }
            } else {
                this.sendErrorMessage(400, Bundle.getMessage("ErrorMissingData"));
            }
        } catch (JmriException je) {
            this.sendErrorMessage(500, Bundle.getMessage("ErrorUnsupportedOperation", je.getLocalizedMessage()));
        } catch (JsonException je) {
            this.sendErrorMessage(je);
        }
    }

    public void sendHello(int heartbeat) throws IOException {
        this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.getHello(heartbeat)));
    }

    private void sendErrorMessage(int code, String message) throws IOException {
        JsonException ex = new JsonException(code, message);
        this.sendErrorMessage(ex);
    }

    private void sendErrorMessage(JsonException ex) throws IOException {
        this.connection.sendMessage(this.mapper.writeValueAsString(ex.getJsonMessage()));
    }
}
