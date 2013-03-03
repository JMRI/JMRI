package jmri.jmris.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import jmri.JmriException;
import jmri.jmris.JmriConnection;
import static jmri.jmris.json.JSON.*;
import jmri.web.server.WebServerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonClientHandler {

    private JsonLightServer lightServer;
    private JsonMemoryServer memoryServer;
    private JsonOperationsServer operationsServer;
    private JsonPowerServer powerServer;
    private JsonProgrammerServer programmerServer;
    private JsonReporterServer reporterServer;
    private JsonRouteServer routeServer;
    private JsonSensorServer sensorServer;
    private JsonSignalHeadServer signalHeadServer;
    private JsonSignalMastServer signalMastServer;
    private JsonThrottleServer throttleServer;
    private JsonTurnoutServer turnoutServer;
    private JmriConnection connection;
    private ObjectMapper mapper;
    private static Logger log = LoggerFactory.getLogger(JsonClientHandler.class);

    public JsonClientHandler(JmriConnection connection) {
        this.connection = connection;
        this.mapper = new ObjectMapper();
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
        this.turnoutServer = new JsonTurnoutServer(this.connection);
    }

    public void onClose() {
        this.throttleServer.onClose();
    }

    /**
     * Process a JSON string and handle appropriately.
     *
     * Currently JSON strings in four different forms are handled by this
     * method:<ul> <li>list requests in the form:
     * <code>{"type":"list","list":"trains"}</code> that are passed to the
     * JsonLister for handling.</li> <li>individual item state requests in the
     * form:
     * <code>{"type":"turnout","data":{"name":"LT14"}}</code> that are passed to
     * type-specific handlers. In addition to the initial response, these
     * requests will initiate "listeners", which will send updated responses
     * every time the item's state changes.</li> <li>a heartbeat in the form
     * <code>*</code> or
     * <code>{"type":"ping"}</code>. The
     * <code>*</code> heartbeat gets no response, while the JSON heartbeat
     * causes a
     * <code>{"type":"pong"}</code> response.</li> <li>a signoff in the form:
     * <code>{"type":"goodbye"}</code> to which an identical response is sent
     * before the connection gets closed.</li>
     *
     * @param string
     * @throws IOException
     */
    public void onMessage(String string) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Received from client: " + string);
        }
        // silently accept '*' as a single-character heartbeat without replying to client
        if (string.equals("*")) { // NOI18N
            return;
        }
        try {
            JsonNode root = this.mapper.readTree(string);
            String type = root.path(TYPE).asText();
            JsonNode data = root.path(DATA);
            if (type.equals(PING)) {
                this.connection.sendMessage(this.mapper.writeValueAsString(this.mapper.createObjectNode().put(TYPE, PONG)));
            } else if (type.equals(GOODBYE)) {
                this.connection.sendMessage(this.mapper.writeValueAsString(this.mapper.createObjectNode().put(TYPE, GOODBYE)));
                this.connection.close();
            } else if (type.equals(LIST)) {
                JsonNode reply;
                String list = root.path(LIST).asText();
                if (list.equals(CARS)) {
                    reply = JsonLister.getCars();
                } else if (list.equals(ENGINES)) {
                    reply = JsonLister.getEngines();
                } else if (list.equals(LIGHTS)) {
                    reply = JsonLister.getLights();
                } else if (list.equals(LOCATIONS)) {
                    reply = JsonLister.getLocations();
                } else if (list.equals(MEMORIES)) {
                    reply = JsonLister.getMemories();
                } else if (list.equals(METADATA)) {
                    reply = JsonLister.getMetadata();
                } else if (list.equals(PANELS)) {
                    reply = JsonLister.getPanels();
                } else if (list.equals(ROSTER)) {
                    reply = JsonLister.getRoster();
                } else if (list.equals(ROUTES)) {
                    reply = JsonLister.getRoutes();
                } else if (list.equals(SENSORS)) {
                    reply = JsonLister.getSensors();
                } else if (list.equals(SIGNAL_HEADS)) {
                    reply = JsonLister.getSignalHeads();
                } else if (list.equals(SIGNAL_MASTS)) {
                    reply = JsonLister.getSignalMasts();
                } else if (list.equals(TRAINS)) {
                    reply = JsonLister.getTrains();
                } else if (list.equals(TURNOUTS)) {
                    reply = JsonLister.getTurnouts();
                } else {
                    this.sendErrorMessage(404, Bundle.getMessage("ErrorUnknownList", list));
                    return;
                }
                //if (log.isDebugEnabled()) log.debug("Sending to client: " + this.mapper.writeValueAsString(reply));
                this.connection.sendMessage(this.mapper.writeValueAsString(reply));
            } else if (!data.isMissingNode()) {
                if (type.equals(LIGHT)) {
                    this.lightServer.parseRequest(data);
                } else if (type.equals(MEMORY)) {
                    this.memoryServer.parseRequest(data);
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
                    this.connection.sendMessage(this.mapper.writeValueAsString(JsonLister.getRosterEntry(data.path(NAME).asText())));
                } else if (type.equals(ROUTE)) {
                    this.routeServer.parseRequest(data);
                } else if (type.equals(THROTTLE)) {
                    this.throttleServer.parseRequest(data);
                } else if (type.equals(TURNOUT)) {
                    this.turnoutServer.parseRequest(data);
                } else {
                    this.sendErrorMessage(404, Bundle.getMessage("ErrorUnknownType", type));
                }
            } else {
                this.sendErrorMessage(500, Bundle.getMessage("ErrorMissingData"));
            }
        } catch (JsonProcessingException pe) {
            log.warn("Exception processing \"" + string + "\"\n" + pe.getMessage());
            this.sendErrorMessage(500, Bundle.getMessage("ErrorProcessingJSON", pe.getLocalizedMessage()));
        } catch (JmriException je) {
            this.sendErrorMessage(500, Bundle.getMessage("ErrorUnsupportedOperation", je.getLocalizedMessage()));
        }
    }

    public void sendHello(int heartbeat) throws IOException {
        ObjectNode root = this.mapper.createObjectNode();
        root.put(TYPE, HELLO);
        ObjectNode data = root.putObject(DATA);
        data.put(JMRI, jmri.Version.name());
        data.put(HEARTBEAT, Math.round(heartbeat * 0.9f));
        data.put(RAILROAD, WebServerManager.getWebServerPreferences().getRailRoadName());
        this.connection.sendMessage(this.mapper.writeValueAsString(root));
    }

    private void sendErrorMessage(int code, String message) throws IOException {
        ObjectNode root = this.mapper.createObjectNode();
        root.put(TYPE, ERROR);
        ObjectNode data = root.putObject(ERROR);
        data.put(CODE, code);
        data.put(MESSAGE, message);
        this.connection.sendMessage(this.mapper.writeValueAsString(root));
    }
}
