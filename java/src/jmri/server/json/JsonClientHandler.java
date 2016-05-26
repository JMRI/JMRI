package jmri.server.json;

import static jmri.server.json.JSON.CARS;
import static jmri.server.json.JSON.CONSIST;
import static jmri.server.json.JSON.CONSISTS;
import static jmri.server.json.JSON.DATA;
import static jmri.server.json.JSON.ENGINES;
import static jmri.server.json.JSON.FORMAT;
import static jmri.server.json.JSON.GOODBYE;
import static jmri.server.json.JSON.HELLO;
import static jmri.server.json.JSON.LIGHTS;
import static jmri.server.json.JSON.LIST;
import static jmri.server.json.JSON.LOCALE;
import static jmri.server.json.JSON.LOCATIONS;
import static jmri.server.json.JSON.METADATA;
import static jmri.server.json.JSON.METHOD;
import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.NETWORK_SERVICES;
import static jmri.server.json.JSON.PANELS;
import static jmri.server.json.JSON.PING;
import static jmri.server.json.JSON.PONG;
import static jmri.server.json.JSON.PROGRAMMER;
import static jmri.server.json.JSON.REPORTER;
import static jmri.server.json.JSON.REPORTERS;
import static jmri.server.json.JSON.SENSOR;
import static jmri.server.json.JSON.SENSORS;
import static jmri.server.json.JSON.SIGNAL_HEAD;
import static jmri.server.json.JSON.SIGNAL_HEADS;
import static jmri.server.json.JSON.SIGNAL_MAST;
import static jmri.server.json.JSON.SIGNAL_MASTS;
import static jmri.server.json.JSON.SYSTEM_CONNECTIONS;
import static jmri.server.json.JSON.THROTTLE;
import static jmri.server.json.JSON.TRAIN;
import static jmri.server.json.JSON.TRAINS;
import static jmri.server.json.JSON.TYPE;
import static jmri.server.json.JSON.XML;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.ServiceLoader;
import jmri.JmriException;
import jmri.jmris.json.JsonConsistServer;
import jmri.jmris.json.JsonOperationsServer;
import jmri.jmris.json.JsonProgrammerServer;
import jmri.jmris.json.JsonReporterServer;
import jmri.jmris.json.JsonSensorServer;
import jmri.jmris.json.JsonServerPreferences;
import jmri.jmris.json.JsonSignalHeadServer;
import jmri.jmris.json.JsonSignalMastServer;
import jmri.jmris.json.JsonThrottleServer;
import jmri.jmris.json.JsonUtil;
import jmri.spi.JsonServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonClientHandler {

    private final JsonConsistServer consistServer;
    private final JsonOperationsServer operationsServer;
    private final JsonProgrammerServer programmerServer;
    private final JsonReporterServer reporterServer;
    private final JsonSensorServer sensorServer;
    private final JsonSignalHeadServer signalHeadServer;
    private final JsonSignalMastServer signalMastServer;
    private final JsonThrottleServer throttleServer;
    private final JsonConnection connection;
    private final HashMap<String, HashSet<JsonSocketService>> services = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(JsonClientHandler.class);

    public JsonClientHandler(JsonConnection connection) {
        this.connection = connection;
        this.consistServer = new JsonConsistServer(this.connection);
        this.operationsServer = new JsonOperationsServer(this.connection);
        this.programmerServer = new JsonProgrammerServer(this.connection);
        this.reporterServer = new JsonReporterServer(this.connection);
        this.sensorServer = new JsonSensorServer(this.connection);
        this.signalHeadServer = new JsonSignalHeadServer(this.connection);
        this.signalMastServer = new JsonSignalMastServer(this.connection);
        this.throttleServer = new JsonThrottleServer(this.connection);
        for (JsonServiceFactory factory : ServiceLoader.load(JsonServiceFactory.class)) {
            for (String type : factory.getTypes()) {
                JsonSocketService service = factory.getSocketService(connection);
                if (service != null) {
                    HashSet<JsonSocketService> set = this.services.get(type);
                    if (set == null) {
                        this.services.put(type, new HashSet<>());
                        set = this.services.get(type);
                    }
                    set.add(service);
                }
            }
        }
    }

    public void dispose() {
        this.throttleServer.dispose();
        this.consistServer.dispose();
        this.operationsServer.dispose();
        this.programmerServer.dispose();
        this.reporterServer.dispose();
        this.sensorServer.dispose();
        this.signalHeadServer.dispose();
        this.signalMastServer.dispose();
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
     */
    public void onMessage(String string) throws IOException {
        log.debug("Received from client: {}", string);
        try {
            this.onMessage(this.connection.getObjectMapper().readTree(string));
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
                this.connection.sendMessage(this.connection.getObjectMapper().createObjectNode().put(TYPE, PONG));
            } else if (type.equals(GOODBYE)) {
                this.connection.sendMessage(this.connection.getObjectMapper().createObjectNode().put(TYPE, GOODBYE));
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
                    case METADATA:
                        reply = JsonUtil.getMetadata(this.connection.getLocale());
                        break;
                    case PANELS:
                        reply = JsonUtil.getPanels(this.connection.getLocale(), (data.path(FORMAT).isMissingNode()) ? XML : data.path(FORMAT).asText());
                        break;
                    case REPORTERS:
                        reply = JsonUtil.getReporters(this.connection.getLocale());
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
                            this.sendErrorMessage(404, Bundle.getMessage(this.connection.getLocale(), "ErrorUnknownType", list));
                            return;
                        }
                }
                this.connection.sendMessage(this.connection.getObjectMapper().writeValueAsString(reply));
            } else if (!data.isMissingNode()) {
                switch (type) {
                    case CONSIST:
                        this.consistServer.parseRequest(this.connection.getLocale(), data);
                        break;
                    case METADATA:
                        this.connection.sendMessage(JsonUtil.getMetadata(this.connection.getLocale(), data.path(NAME).asText()));
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
                    case THROTTLE:
                        this.throttleServer.parseRequest(this.connection.getLocale(), data);
                        break;
                    case TRAIN:
                        this.operationsServer.parseTrainRequest(this.connection.getLocale(), data);
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
        this.connection.sendMessage(JsonUtil.getHello(this.connection.getLocale(), heartbeat));
    }

    private void sendErrorMessage(int code, String message) throws IOException {
        JsonException ex = new JsonException(code, message);
        this.sendErrorMessage(ex);
    }

    private void sendErrorMessage(JsonException ex) throws IOException {
        this.connection.sendMessage(ex.getJsonMessage());
    }
}
