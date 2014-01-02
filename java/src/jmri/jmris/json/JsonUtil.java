// JsonUtil.java
package jmri.jmris.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import jmri.Consist;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Light;
import jmri.Memory;
import jmri.Metadata;
import jmri.PowerManager;
import jmri.Reporter;
import jmri.Route;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.SignalHead;
import jmri.SignalMast;
import jmri.Turnout;
import static jmri.jmris.json.JSON.ACTIVE;
import static jmri.jmris.json.JSON.ADDRESS;
import static jmri.jmris.json.JSON.APPEARANCE;
import static jmri.jmris.json.JSON.APPEARANCE_NAME;
import static jmri.jmris.json.JSON.ASPECT;
import static jmri.jmris.json.JSON.ASPECT_DARK;
import static jmri.jmris.json.JSON.ASPECT_HELD;
import static jmri.jmris.json.JSON.ASPECT_UNKNOWN;
import static jmri.jmris.json.JSON.CABOOSE;
import static jmri.jmris.json.JSON.CAR;
import static jmri.jmris.json.JSON.CARS;
import static jmri.jmris.json.JSON.CLOSED;
import static jmri.jmris.json.JSON.CODE;
import static jmri.jmris.json.JSON.COLOR;
import static jmri.jmris.json.JSON.COMMENT;
import static jmri.jmris.json.JSON.CONSIST;
import static jmri.jmris.json.JSON.CONTROL_PANEL;
import static jmri.jmris.json.JSON.DATA;
import static jmri.jmris.json.JSON.DEPARTURE_LOCATION;
import static jmri.jmris.json.JSON.DEPARTURE_TIME;
import static jmri.jmris.json.JSON.DESCRIPTION;
import static jmri.jmris.json.JSON.DESTINATION;
import static jmri.jmris.json.JSON.DESTINATION_TRACK;
import static jmri.jmris.json.JSON.DIRECTION;
import static jmri.jmris.json.JSON.ENGINE;
import static jmri.jmris.json.JSON.ENGINES;
import static jmri.jmris.json.JSON.ERROR;
import static jmri.jmris.json.JSON.EXPECTED_ARRIVAL;
import static jmri.jmris.json.JSON.EXPECTED_DEPARTURE;
import static jmri.jmris.json.JSON.F;
import static jmri.jmris.json.JSON.FORWARD;
import static jmri.jmris.json.JSON.FUNCTION_KEYS;
import static jmri.jmris.json.JSON.HEARTBEAT;
import static jmri.jmris.json.JSON.HELLO;
import static jmri.jmris.json.JSON.ID;
import static jmri.jmris.json.JSON.IMAGE_FILE_NAME;
import static jmri.jmris.json.JSON.IMAGE_ICON_NAME;
import static jmri.jmris.json.JSON.INACTIVE;
import static jmri.jmris.json.JSON.INCONSISTENT;
import static jmri.jmris.json.JSON.INVERTED;
import static jmri.jmris.json.JSON.IS_LONG_ADDRESS;
import static jmri.jmris.json.JSON.JMRI;
import static jmri.jmris.json.JSON.JSON;
import static jmri.jmris.json.JSON.JSON_PROTOCOL_VERSION;
import static jmri.jmris.json.JSON.LABEL;
import static jmri.jmris.json.JSON.LAST_REPORT;
import static jmri.jmris.json.JSON.LAYOUT_PANEL;
import static jmri.jmris.json.JSON.LEAD_ENGINE;
import static jmri.jmris.json.JSON.LENGTH;
import static jmri.jmris.json.JSON.LIGHT;
import static jmri.jmris.json.JSON.LIT;
import static jmri.jmris.json.JSON.LOAD;
import static jmri.jmris.json.JSON.LOCATION;
import static jmri.jmris.json.JSON.LOCATIONS;
import static jmri.jmris.json.JSON.LOCATION_ID;
import static jmri.jmris.json.JSON.LOCATION_TRACK;
import static jmri.jmris.json.JSON.LOCKABLE;
import static jmri.jmris.json.JSON.MAX_SPD_PCT;
import static jmri.jmris.json.JSON.MEMORY;
import static jmri.jmris.json.JSON.MESSAGE;
import static jmri.jmris.json.JSON.METADATA;
import static jmri.jmris.json.JSON.MFG;
import static jmri.jmris.json.JSON.MODEL;
import static jmri.jmris.json.JSON.NAME;
import static jmri.jmris.json.JSON.NODE;
import static jmri.jmris.json.JSON.NUMBER;
import static jmri.jmris.json.JSON.OFF;
import static jmri.jmris.json.JSON.ON;
import static jmri.jmris.json.JSON.PANEL;
import static jmri.jmris.json.JSON.PANEL_PANEL;
import static jmri.jmris.json.JSON.PORT;
import static jmri.jmris.json.JSON.POSITION;
import static jmri.jmris.json.JSON.POWER;
import static jmri.jmris.json.JSON.RAILROAD;
import static jmri.jmris.json.JSON.REPORT;
import static jmri.jmris.json.JSON.REPORTER;
import static jmri.jmris.json.JSON.ROAD;
import static jmri.jmris.json.JSON.ROSTER_ENTRY;
import static jmri.jmris.json.JSON.ROUTE;
import static jmri.jmris.json.JSON.ROUTE_ID;
import static jmri.jmris.json.JSON.SENSOR;
import static jmri.jmris.json.JSON.SEQUENCE;
import static jmri.jmris.json.JSON.SIGNAL_HEAD;
import static jmri.jmris.json.JSON.SIGNAL_MAST;
import static jmri.jmris.json.JSON.SIZE_LIMIT;
import static jmri.jmris.json.JSON.STATE;
import static jmri.jmris.json.JSON.STATUS;
import static jmri.jmris.json.JSON.TERMINATES_LOCATION;
import static jmri.jmris.json.JSON.THROWN;
import static jmri.jmris.json.JSON.TOGGLE;
import static jmri.jmris.json.JSON.TOKEN_HELD;
import static jmri.jmris.json.JSON.TRAIN;
import static jmri.jmris.json.JSON.TURNOUT;
import static jmri.jmris.json.JSON.TYPE;
import static jmri.jmris.json.JSON.UNKNOWN;
import static jmri.jmris.json.JSON.URL;
import static jmri.jmris.json.JSON.USERNAME;
import static jmri.jmris.json.JSON.VALUE;
import static jmri.jmris.json.JSON.WEIGHT;
import jmri.jmrit.consisttool.ConsistFile;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.panelEditor.PanelEditor;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.node.NodeIdentity;
import jmri.util.JmriJFrame;
import jmri.util.zeroconf.ZeroConfService;
import jmri.web.server.WebServerManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A set of static methods for converting certain objects to/from JSON
 * representations
 *
 * @author rhwood
 */
public class JsonUtil {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(JsonUtil.class);

    static public JsonNode getCar(String id) {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, CAR);
        ObjectNode data = root.putObject(DATA);
        Car car = CarManager.instance().getById(id);
        data.put(ID, car.getId());
        data.put(ROAD, car.getRoadName());
        data.put(NUMBER, car.getNumber());
        data.put(LOAD, car.getLoadName());
        data.put(LOCATION, car.getRouteLocationId());
        data.put(LOCATION_TRACK, car.getTrackName());
        data.put(DESTINATION, car.getRouteDestinationId());
        data.put(DESTINATION_TRACK, car.getDestinationTrackName());
        data.put(TYPE, car.getTypeName());
        data.put(LENGTH, car.getLength());
        data.put(COLOR, car.getColor());
        data.put(COMMENT, car.getComment());
        return root;
    }

    static public JsonNode getCars() {
        ArrayNode root = mapper.createArrayNode();
        for (RollingStock rs : CarManager.instance().getByIdList()) {
            root.add(getCar(rs.getId()));
        }
        return root;
    }

    /**
     * Delete the consist at the given address.
     *
     * @param address The address of the consist to delete.
     * @throws JsonException This exception has code 404 if the consist does not
     * exist.
     */
    static public void delConsist(DccLocoAddress address) throws JsonException {
        if (InstanceManager.consistManagerInstance().getConsistList().contains(address)) {
            InstanceManager.consistManagerInstance().delConsist(address);
        } else {
            throw new JsonException(404, Bundle.getMessage("ErrorObject", CONSIST, address.toString()));
        }
    }

    /**
     * Get the JSON representation of a consist.
     *
     * The JSON representation is an object with the following data attributes:
     * <ul>
     * <li>address - integer address</li>
     * <li>isLongAddress - boolean true if address is long, false if short</li>
     * <li>type - integer, see {@link jmri.Consist#getConsistType() }</li>
     * <li>id - string with consist Id</li>
     * <li>sizeLimit - the maximum number of locomotives the consist can
     * contain</li>
     * <li>engines - array listing every locomotive in the consist. Each entry
     * in the array contains the following attributes:
     * <ul>
     * <li>address - integer address</li>
     * <li>isLongAddress - boolean true if address is long, false if short</li>
     * <li>forward - boolean true if the locomotive running is forward in the
     * consists</li>
     * <li>position - integer locomotive's position in the consist</li>
     * </ul>
     * </ul>
     *
     * @param address The address of the consist to get.
     * @return The JSON representation of the consist.
     * @throws JsonException This exception has code 404 if the consist does not
     * exist.
     */
    static public JsonNode getConsist(DccLocoAddress address) throws JsonException {
        if (InstanceManager.consistManagerInstance().getConsistList().contains(address)) {
            ObjectNode root = mapper.createObjectNode();
            root.put(TYPE, CONSIST);
            ObjectNode data = root.putObject(DATA);
            Consist consist = InstanceManager.consistManagerInstance().getConsist(address);
            data.put(ADDRESS, consist.getConsistAddress().getNumber());
            data.put(IS_LONG_ADDRESS, consist.getConsistAddress().isLongAddress());
            data.put(TYPE, consist.getConsistType());
            ArrayNode engines = data.putArray(ENGINES);
            for (DccLocoAddress l : consist.getConsistList()) {
                ObjectNode engine = mapper.createObjectNode();
                engine.put(ADDRESS, l.getNumber());
                engine.put(IS_LONG_ADDRESS, l.isLongAddress());
                engine.put(FORWARD, consist.getLocoDirection(l));
                engine.put(POSITION, consist.getPosition(l));
                engines.add(engine);
            }
            data.put(ID, consist.getConsistID());
            data.put(SIZE_LIMIT, consist.sizeLimit());
            return root;
        } else {
            throw new JsonException(404, Bundle.getMessage("ErrorObject", CONSIST, address.toString()));
        }
    }

    /**
     * Add a consist.
     *
     * Adds a consist, populating it with information from data.
     *
     * @param address The address of the new consist.
     * @param data The JSON representation of the consist. See
     * {@link #getConsist(jmri.DccLocoAddress)} for the JSON structure.
     * @throws JsonException
     */
    static public void putConsist(DccLocoAddress address, JsonNode data) throws JsonException {
        if (!InstanceManager.consistManagerInstance().getConsistList().contains(address)) {
            InstanceManager.consistManagerInstance().getConsist(address);
            setConsist(address, data);
        }
    }

    /**
     * Get a list of consists.
     *
     * @return JSON array of consists as in the structure returned by
     * {@link #getConsist(jmri.DccLocoAddress)}
     * @throws JsonException
     */
    static public JsonNode getConsists() throws JsonException {
        ArrayNode root = mapper.createArrayNode();
        for (DccLocoAddress address : InstanceManager.consistManagerInstance().getConsistList()) {
            root.add(getConsist(address));
        }
        return root;
    }

    /**
     * Change the properties and locomotives of a consist.
     *
     * This method takes as input the JSON representation of a consist as
     * provided by {@link #getConsist(jmri.DccLocoAddress) }.
     *
     * If present in the JSON, this method sets the following consist
     * properties:
     * <ul>
     * <li>consistID</li>
     * <li>consistType</li>
     * <li>locomotives (<em>engines</em> in the JSON representation)<br>
     * <strong>NOTE</strong> Since this method adds, repositions, and deletes
     * locomotives, the JSON representation must contain <em>every</em>
     * locomotive that should be in the consist, if it contains the engines
     * node.</li>
     * </ul>
     *
     * @param address - the consist address
     * @param data - the consist as a JsonObject
     * @throws JsonException
     */
    static public void setConsist(DccLocoAddress address, JsonNode data) throws JsonException {
        if (InstanceManager.consistManagerInstance().getConsistList().contains(address)) {
            Consist consist = InstanceManager.consistManagerInstance().getConsist(address);
            if (data.path(ID).isTextual()) {
                consist.setConsistID(data.path(ID).asText());
            }
            if (data.path(TYPE).isInt()) {
                consist.setConsistType(data.path(TYPE).asInt());
            }
            if (data.path(ENGINES).isArray()) {
                ArrayList<DccLocoAddress> engines = new ArrayList<DccLocoAddress>();
                // add every engine in
                for (JsonNode engine : data.path(ENGINES)) {
                    DccLocoAddress engineAddress = new DccLocoAddress(engine.path(ADDRESS).asInt(), engine.path(IS_LONG_ADDRESS).asBoolean());
                    if (!consist.contains(engineAddress)) {
                        consist.add(engineAddress, engine.path(FORWARD).asBoolean());
                    }
                    consist.setPosition(engineAddress, engine.path(POSITION).asInt());
                    engines.add(engineAddress);
                }
                @SuppressWarnings("unchecked")
                ArrayList<DccLocoAddress> consistEngines = (ArrayList<DccLocoAddress>) consist.getConsistList().clone();
                for (DccLocoAddress engineAddress : consistEngines) {
                    if (!engines.contains(engineAddress)) {
                        consist.remove(engineAddress);
                    }
                }
            }
            try {
                (new ConsistFile()).writeFile(InstanceManager.consistManagerInstance().getConsistList());
            } catch (IOException ex) {
                throw new JsonException(500, ex.getLocalizedMessage());
            }
        }
    }

    static public JsonNode getEngine(String id) {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, ENGINE);
        ObjectNode data = root.putObject(DATA);
        Engine engine = EngineManager.instance().getById(id);
        data.put(ID, engine.getId());
        data.put(ROAD, engine.getRoadName());
        data.put(NUMBER, engine.getNumber());
        data.put(LOCATION, engine.getRouteLocationId());
        data.put(LOCATION_TRACK, engine.getTrackName());
        data.put(DESTINATION, engine.getRouteDestinationId());
        data.put(DESTINATION_TRACK, engine.getDestinationTrackName());
        data.put(MODEL, engine.getModel());
        data.put(LENGTH, engine.getLength());
        data.put(COMMENT, engine.getComment());
        return root;
    }

    static public JsonNode getEngines() {
        ArrayNode root = mapper.createArrayNode();
        for (RollingStock rs : EngineManager.instance().getByIdList()) {
            root.add(getEngine(rs.getId()));
        }
        return root;
    }

    static public JsonNode getLight(String name) throws JsonException {
        try {
            ObjectNode root = mapper.createObjectNode();
            root.put(TYPE, LIGHT);
            ObjectNode data = root.putObject(DATA);
            Light light = InstanceManager.lightManagerInstance().getLight(name);
            data.put(NAME, light.getSystemName());
            data.put(USERNAME, light.getUserName());
            data.put(COMMENT, light.getComment());
            switch (light.getState()) {
                case Light.OFF:
                    data.put(STATE, OFF);
                    break;
                case Light.ON:
                    data.put(STATE, ON);
                    break;
                default:
                    data.put(STATE, UNKNOWN);
                    break;
            }
            return root;
        } catch (NullPointerException e) {
            log.error("Unable to get light [{}].", name);
            throw new JsonException(404, Bundle.getMessage("ErrorObject", LIGHT, name));
        }
    }

    static public JsonNode getLights() throws JsonException {
        ArrayNode root = mapper.createArrayNode();
        for (String name : InstanceManager.lightManagerInstance().getSystemNameList()) {
            root.add(getLight(name));
        }
        return root;
    }

    static public void putLight(String name, JsonNode data) throws JsonException {
        try {
            InstanceManager.lightManagerInstance().provideLight(name);
        } catch (Exception ex) {
            throw new JsonException(500, Bundle.getMessage("ErrorCreatingObject", LIGHT, name));
        }
        setLight(name, data);
    }

    static public void setLight(String name, JsonNode data) throws JsonException {
        try {
            Light light = InstanceManager.lightManagerInstance().getBySystemName(name);
            if (data.path(USERNAME).isTextual()) {
                light.setUserName(data.path(USERNAME).asText());
            }
            if (data.path(COMMENT).isTextual()) {
                light.setComment(data.path(COMMENT).asText());
            }
            int state = data.path(STATE).asInt(UNKNOWN);
            switch (state) {
                case OFF:
                    InstanceManager.lightManagerInstance().getLight(name).setState(Light.OFF);
                    break;
                case ON:
                    InstanceManager.lightManagerInstance().getLight(name).setState(Light.ON);
                    break;
                case UNKNOWN:
                    // silently ignore
                    break;
                default:
                    throw new JsonException(400, Bundle.getMessage("ErrorUnknownState", LIGHT, state));
            }
        } catch (NullPointerException e) {
            log.error("Unable to get light [{}].", name);
            throw new JsonException(404, Bundle.getMessage("ErrorObject", LIGHT, name));
        }
    }

    static public JsonNode getLocation(String id) throws JsonException {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, LOCATION);
        ObjectNode data = root.putObject(DATA);
        try {
            Location location = LocationManager.instance().getLocationById(id);
            data.put(NAME, location.getName());
            data.put(ID, location.getId());
            data.put(LENGTH, location.getLength());
            data.put(COMMENT, location.getComment());
        } catch (NullPointerException e) {
            log.error("Unable to get location id [{}].", id);
            throw new JsonException(404, Bundle.getMessage("ErrorObject", LOCATION, id));
        }
        return root;
    }

    static public JsonNode getLocations() throws JsonException {
        ArrayNode root = mapper.createArrayNode();
        for (Location location : LocationManager.instance().getLocationsByIdList()) {
            root.add(getLocation(location.getId()));
        }
        return root;
    }

    static public JsonNode getMemories() throws JsonException {
        ArrayNode root = mapper.createArrayNode();
        for (String name : InstanceManager.memoryManagerInstance().getSystemNameList()) {
            root.add(getMemory(name));
        }
        return root;
    }

    static public JsonNode getMemory(String name) throws JsonException {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, MEMORY);
        ObjectNode data = root.putObject(DATA);
        Memory memory = InstanceManager.memoryManagerInstance().getMemory(name);
        try {
            data.put(NAME, memory.getSystemName());
            data.put(USERNAME, memory.getUserName());
            data.put(COMMENT, memory.getComment());
            if (memory.getValue() == null) {
                data.putNull(VALUE);
            } else {
                data.put(VALUE, memory.getValue().toString());
            }
        } catch (NullPointerException e) {
            log.error("Unable to get memory [{}].", name);
            throw new JsonException(404, Bundle.getMessage("ErrorObject", MEMORY, name));
        }
        return root;
    }

    static public void putMemory(String name, JsonNode data) throws JsonException {
        try {
            InstanceManager.memoryManagerInstance().provideMemory(name);
        } catch (Exception ex) {
            throw new JsonException(500, Bundle.getMessage("ErrorCreatingObject", MEMORY, name));
        }
        setMemory(name, data);
    }

    static public void setMemory(String name, JsonNode data) throws JsonException {
        try {
            Memory memory = InstanceManager.memoryManagerInstance().getMemory(name);
            if (data.path(USERNAME).isTextual()) {
                memory.setUserName(data.path(USERNAME).asText());
            }
            if (data.path(COMMENT).isTextual()) {
                memory.setComment(data.path(COMMENT).asText());
            }
            if (!data.path(VALUE).isMissingNode()) {
                if (data.path(VALUE).isNull()) {
                    memory.setValue(null);
                } else {
                    memory.setValue(data.path(VALUE).asText());
                }
            }
        } catch (NullPointerException ex) {
            log.error("Unable to get memory [{}].", name);
            throw new JsonException(404, Bundle.getMessage("ErrorObject", MEMORY, name));
        }
    }

    static public JsonNode getMetadata(String name) throws JsonException {
        String metadata = Metadata.getBySystemName(name);
        ObjectNode root;
        if (metadata != null) {
            root = mapper.createObjectNode();
            root.put(TYPE, METADATA);
            ObjectNode data = root.putObject(DATA);
            data.put(NAME, name);
            data.put(VALUE, Metadata.getBySystemName(name));
        } else {
            log.error("Unable to get metadata [{}].", name);
            throw new JsonException(404, Bundle.getMessage("ErrorObject", METADATA, name));
        }
        return root;
    }

    static public JsonNode getMetadata() throws JsonException {
        ArrayNode root = mapper.createArrayNode();
        List<String> names = Metadata.getSystemNameList();
        for (String name : names) {
            root.add(getMetadata(name));
        }
        return root;
    }

    static public JsonNode getPanels(String format) {
        List<String> disallowedFrames = WebServerManager.getWebServerPreferences().getDisallowedFrames();
        ArrayNode root = mapper.createArrayNode();
        // list loaded Panels (ControlPanelEditor, PanelEditor, LayoutEditor)
        List<JmriJFrame> frames = JmriJFrame.getFrameList(ControlPanelEditor.class);
        for (JmriJFrame frame : frames) {
            if (frame.getAllowInFrameServlet()) {
                String title = ((JmriJFrame) ((Editor) frame).getTargetPanel().getTopLevelAncestor()).getTitle();
                if (!title.equals("") && !disallowedFrames.contains(title)) {
                    ObjectNode panel = mapper.createObjectNode();
                    panel.put(TYPE, PANEL);
                    ObjectNode data = panel.putObject(DATA);
                    data.put(NAME, "ControlPanel/" + title.replaceAll(" ", "%20").replaceAll("#", "%23")); // NOI18N
                    data.put(URL, "/panel/" + data.path(NAME).asText() + "?format=" + format); // NOI18N
                    data.put(USERNAME, title);
                    data.put(TYPE, CONTROL_PANEL);
                    root.add(data);
                }
            }
        }
        frames = JmriJFrame.getFrameList(PanelEditor.class);
        for (JmriJFrame frame : frames) {
            if (frame.getAllowInFrameServlet() && !(LayoutEditor.class.isInstance(frame))) {  //skip LayoutEditor panels, as they will be added next
                String title = ((JmriJFrame) ((Editor) frame).getTargetPanel().getTopLevelAncestor()).getTitle();
                if (!title.equals("") && !disallowedFrames.contains(title)) {
                    ObjectNode panel = mapper.createObjectNode();
                    panel.put(TYPE, PANEL);
                    ObjectNode data = panel.putObject(DATA);
                    data.put(NAME, "Panel/" + title.replaceAll(" ", "%20").replaceAll("#", "%23")); // NOI18N
                    data.put(URL, "/panel/" + data.path(NAME).asText() + "?format=" + format); // NOI18N
                    data.put(USERNAME, title);
                    data.put(TYPE, PANEL_PANEL);
                    root.add(data);
                }
            }
        }
        frames = JmriJFrame.getFrameList(LayoutEditor.class);
        for (JmriJFrame frame : frames) {
            if (frame.getAllowInFrameServlet()) {
                String title = ((JmriJFrame) ((Editor) frame).getTargetPanel().getTopLevelAncestor()).getTitle();
                if (!title.equals("") && !disallowedFrames.contains(title)) {
                    ObjectNode panel = mapper.createObjectNode();
                    panel.put(TYPE, PANEL);
                    ObjectNode data = panel.putObject(DATA);
                    data.put(NAME, "Layout/" + title.replaceAll(" ", "%20").replaceAll("#", "%23")); // NOI18N
                    data.put(URL, "/panel/" + data.path(NAME).asText() + "?format=" + format); // NOI18N
                    data.put(USERNAME, title);
                    data.put(TYPE, LAYOUT_PANEL);
                    root.add(data);
                }
            }
        }
        return root;
    }

    static public JsonNode getPower() throws JsonException {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, POWER);
        ObjectNode data = root.putObject(DATA);
        try {
            switch (InstanceManager.powerManagerInstance().getPower()) {
                case PowerManager.OFF:
                    data.put(STATE, OFF);
                    break;
                case PowerManager.ON:
                    data.put(STATE, ON);
                    break;
                default:
                    data.put(STATE, UNKNOWN);
                    break;
            }
        } catch (JmriException e) {
            log.error("Unable to get Power state.", e);
            throw new JsonException(500, Bundle.getMessage("ErrorPower"));
        }
        return root;
    }

    static public void setPower(JsonNode data) throws JsonException {
        int state = data.path(STATE).asInt(UNKNOWN);
        try {
            switch (state) {
                case OFF:
                    InstanceManager.powerManagerInstance().setPower(PowerManager.OFF);
                    break;
                case ON:
                    InstanceManager.powerManagerInstance().setPower(PowerManager.ON);
                    break;
                case UNKNOWN:
                    // quietly ignore
                    break;
                default:
                    throw new JsonException(400, Bundle.getMessage("ErrorUnknownState", POWER, state));
            }
        } catch (JmriException ex) {
            throw new JsonException(500, ex);
        }
    }

    static public JsonNode getRailroad() {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, RAILROAD);
        ObjectNode data = root.putObject(DATA);
        data.put(NAME, WebServerManager.getWebServerPreferences().getRailRoadName());
        return root;
    }

    static public JsonNode getReporter(String name) {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, REPORTER);
        ObjectNode data = root.putObject(DATA);
        Reporter reporter = InstanceManager.reporterManagerInstance().getReporter(name);
        data.put(NAME, reporter.getSystemName());
        data.put(USERNAME, reporter.getUserName());
        data.put(STATE, reporter.getState());
        data.put(COMMENT, reporter.getComment());
        data.put(REPORT, (reporter.getCurrentReport() != null) ? reporter.getCurrentReport().toString() : null);
        data.put(LAST_REPORT, (reporter.getLastReport() != null) ? reporter.getLastReport().toString() : null);
        return root;
    }

    static public JsonNode getReporters() {
        ArrayNode root = mapper.createArrayNode();
        for (String name : InstanceManager.reporterManagerInstance().getSystemNameList()) {
            root.add(getReporter(name));
        }
        return root;
    }

    static public void putReporter(String name, JsonNode data) throws JsonException {
        try {
            InstanceManager.reporterManagerInstance().provideReporter(name);
        } catch (Exception ex) {
            throw new JsonException(500, Bundle.getMessage("ErrorCreatingObject", REPORTER, name));
        }
        setReporter(name, data);
    }

    static public void setReporter(String name, JsonNode data) throws JsonException {
        try {
            Reporter reporter = InstanceManager.reporterManagerInstance().getBySystemName(name);
            if (data.path(USERNAME).isTextual()) {
                reporter.setUserName(data.path(USERNAME).asText());
            }
            if (data.path(COMMENT).isTextual()) {
                reporter.setComment(data.path(COMMENT).asText());
            }
            if (data.path(REPORT).isNull()) {
                InstanceManager.reporterManagerInstance().getReporter(name).setReport(null);
            } else {
                InstanceManager.reporterManagerInstance().getReporter(name).setReport(data.path(REPORT).asText());
            }
        } catch (NullPointerException ex) {
            throw new JsonException(404, Bundle.getMessage("ErrorObject", REPORTER, name));
        }
    }

    static public JsonNode getRosterEntry(String id) {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, ROSTER_ENTRY);
        ObjectNode entry = root.putObject(DATA);
        RosterEntry re = Roster.instance().getEntryForId(id);
        entry.put(NAME, re.getId());
        entry.put(ADDRESS, re.getDccAddress());
        entry.put(IS_LONG_ADDRESS, re.isLongAddress());
        entry.put(ROAD, re.getRoadName());
        entry.put(NUMBER, re.getRoadNumber());
        entry.put(MFG, re.getMfg());
        entry.put(MODEL, re.getModel());
        entry.put(COMMENT, re.getComment());
        entry.put(MAX_SPD_PCT, Integer.valueOf(re.getMaxSpeedPCT()).toString());
        File file = new File(re.getImagePath());
        entry.put(IMAGE_FILE_NAME, file.getName());
        file = new File(re.getIconPath());
        entry.put(IMAGE_ICON_NAME, file.getName());
        ArrayNode labels = entry.putArray(FUNCTION_KEYS);
        for (int i = 0; i < re.getMAXFNNUM(); i++) {
            ObjectNode label = mapper.createObjectNode();
            label.put(NAME, F + i);
            label.put(LABEL, (re.getFunctionLabel(i) != null) ? re.getFunctionLabel(i) : F + i);
            label.put(LOCKABLE, re.getFunctionLockable(i));
            labels.add(label);
        }
        return root;
    }

    static public JsonNode getRoster() {
        ArrayNode root = mapper.createArrayNode();
        for (RosterEntry re : Roster.instance().matchingList(null, null, null, null, null, null, null)) {
            root.add(getRosterEntry(re.getId()));
        }
        return root;
    }

    static public JsonNode getRoute(String name) throws JsonException {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, ROUTE);
        ObjectNode data = root.putObject(DATA);
        try {
            Route route = InstanceManager.routeManagerInstance().getRoute(name);
            SensorManager s = InstanceManager.sensorManagerInstance();
            data.put(NAME, route.getSystemName());
            data.put(USERNAME, route.getUserName());
            data.put(COMMENT, route.getComment());
            Sensor sensor = s.getSensor(route.getTurnoutsAlignedSensor());
            if (sensor != null) {
                switch (sensor.getKnownState()) {
                    case Sensor.ACTIVE:
                        data.put(STATE, ACTIVE);
                        break;
                    case Sensor.INACTIVE:
                        data.put(STATE, INACTIVE);
                        break;
                    case Sensor.INCONSISTENT:
                        data.put(STATE, INCONSISTENT);
                        break;
                    case Sensor.UNKNOWN:
                    default:
                        data.put(STATE, UNKNOWN);
                        break;
                }
            } else {
                data.put(STATE, UNKNOWN);
            }
        } catch (NullPointerException e) {
            log.error("Unable to get route [{}].", name);
            throw new JsonException(404, Bundle.getMessage("ErrorObject", ROUTE, name));
        }
        return root;
    }

    static public JsonNode getRoutes() throws JsonException {
        ArrayNode root = mapper.createArrayNode();
        for (String name : InstanceManager.routeManagerInstance().getSystemNameList()) {
            root.add(getRoute(name));
        }
        return root;
    }

    /**
     * Routes can be set by passing a JsonNode with the node <em>state</em>
     * equal to <em>8</em> (the aspect of {@link jmri.Route#TOGGLE}).
     *
     * @param name The name of the route
     * @param data A JsonNode containing route attributes to set
     * @throws JsonException
     * @see jmri.Route#TOGGLE
     */
    static public void setRoute(String name, JsonNode data) throws JsonException {
        try {
            Route route = InstanceManager.routeManagerInstance().getRoute(name);
            if (data.path(USERNAME).isTextual()) {
                route.setUserName(data.path(USERNAME).asText());
            }
            if (data.path(COMMENT).isTextual()) {
                route.setComment(data.path(COMMENT).asText());
            }
            int state = data.path(STATE).asInt(UNKNOWN);
            switch (state) {
                case ACTIVE:
                case TOGGLE:
                    route.setRoute();
                    break;
                case INACTIVE:
                case UNKNOWN:
                    // silently ignore
                    break;
                default:
                    throw new JsonException(400, Bundle.getMessage("ErrorUnknownState", ROUTE, state));
            }
        } catch (NullPointerException ex) {
            log.error("Unable to get route [{}].", name);
            throw new JsonException(404, Bundle.getMessage("ErrorObject", ROUTE, name));
        }
    }

    static public JsonNode getSensor(String name) throws JsonException {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, SENSOR);
        ObjectNode data = root.putObject(DATA);
        try {
            Sensor sensor = InstanceManager.sensorManagerInstance().getSensor(name);
            data.put(NAME, name);
            data.put(USERNAME, sensor.getUserName());
            data.put(COMMENT, sensor.getComment());
            data.put(INVERTED, sensor.getInverted());
            switch (sensor.getKnownState()) {
                case Sensor.ACTIVE:
                    data.put(STATE, ACTIVE);
                    break;
                case Sensor.INACTIVE:
                    data.put(STATE, INACTIVE);
                    break;
                case Sensor.INCONSISTENT:
                    data.put(STATE, INCONSISTENT);
                    break;
                case Sensor.UNKNOWN:
                default:
                    data.put(STATE, UNKNOWN);
                    break;
            }
        } catch (NullPointerException e) {
            log.error("Unable to get sensor [{}].", name);
            throw new JsonException(404, Bundle.getMessage("ErrorObject", SENSOR, name));
        }
        return root;
    }

    static public JsonNode getSensors() throws JsonException {
        ArrayNode root = mapper.createArrayNode();
        for (String name : InstanceManager.sensorManagerInstance().getSystemNameList()) {
            root.add(getSensor(name));
        }
        return root;
    }

    static public void putSensor(String name, JsonNode data) throws JsonException {
        try {
            InstanceManager.sensorManagerInstance().provideSensor(name);
        } catch (Exception ex) {
            throw new JsonException(500, Bundle.getMessage("ErrorCreatingObject", TURNOUT, name));
        }
        setTurnout(name, data);
    }

    static public void setSensor(String name, JsonNode data) throws JsonException {
        try {
            Sensor sensor = InstanceManager.sensorManagerInstance().getSensor(name);
            if (data.path(USERNAME).isTextual()) {
                sensor.setUserName(data.path(USERNAME).asText());
            }
            if (data.path(INVERTED).isBoolean()) {
                sensor.setInverted(data.path(INVERTED).asBoolean());
            }
            if (data.path(COMMENT).isTextual()) {
                sensor.setComment(data.path(COMMENT).asText());
            }
            int state = data.path(STATE).asInt(UNKNOWN);
            switch (state) {
                case Sensor.ACTIVE:
                    sensor.setKnownState(Sensor.ACTIVE);
                    break;
                case INACTIVE:
                    sensor.setKnownState(Sensor.INACTIVE);
                    break;
                case UNKNOWN:
                    // silently ignore
                    break;
                default:
                    throw new JsonException(400, Bundle.getMessage("ErrorUnknownState", SENSOR, state));
            }
        } catch (NullPointerException e) {
            log.error("Unable to get sensor [{}].", name);
            throw new JsonException(404, Bundle.getMessage("ErrorObject", SENSOR, name));
        } catch (JmriException ex) {
            throw new JsonException(500, ex);
        }
    }

    static public JsonNode getSignalHead(String name) throws JsonException {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, SIGNAL_HEAD);
        ObjectNode data = root.putObject(DATA);
        SignalHead signalHead = InstanceManager.signalHeadManagerInstance().getSignalHead(name);
        try {
            data.put(NAME, name);
            data.put(USERNAME, signalHead.getUserName());
            data.put(COMMENT, signalHead.getComment());
            data.put(LIT, signalHead.getLit());
            data.put(APPEARANCE, signalHead.getAppearance());
            data.put(TOKEN_HELD, signalHead.getHeld());
            //state is appearance, plus a flag for held status
            if (signalHead.getHeld()) {
                data.put(STATE, SignalHead.HELD);
            } else {
                data.put(STATE, signalHead.getAppearance());
            }
            data.put(APPEARANCE_NAME, signalHead.getAppearanceName());
        } catch (NullPointerException e) {
            log.error("Unable to get signalHead [{}].", name);
            throw new JsonException(404, Bundle.getMessage("ErrorObject", SIGNAL_HEAD, name));
        }
        return root;
    }

    static public JsonNode getSignalHeads() throws JsonException {
        ArrayNode root = mapper.createArrayNode();
        for (String name : InstanceManager.signalHeadManagerInstance().getSystemNameList()) {
            root.add(getSignalHead(name));
        }
        return root;
    }

    static public void setSignalHead(String name, JsonNode data) throws JsonException {
        try {
            SignalHead signalHead = InstanceManager.signalHeadManagerInstance().getSignalHead(name);
            if (data.path(USERNAME).isTextual()) {
                signalHead.setUserName(data.path(USERNAME).asText());
            }
            if (data.path(COMMENT).isTextual()) {
                signalHead.setComment(data.path(COMMENT).asText());
            }
            int state = data.path(STATE).asInt(UNKNOWN);
            boolean isValid = false;
            for (int validState : signalHead.getValidStates()) {
                if (state == validState) {
                    isValid = true;
                    break;
                }
            }
            if (isValid && state != INCONSISTENT && state != UNKNOWN) {
                // TODO: completely insulate JSON state from SignalHead state
                signalHead.setAppearance(state);
            } else {
                throw new JsonException(400, Bundle.getMessage("ErrorUnknownState", SIGNAL_HEAD, state));
            }
        } catch (NullPointerException e) {
            log.error("Unable to get signal head [{}].", name);
            throw new JsonException(404, Bundle.getMessage("ErrorObject", SIGNAL_HEAD, name));
        }
    }

    static public JsonNode getSignalMast(String name) throws JsonException {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, SIGNAL_MAST);
        ObjectNode data = root.putObject(DATA);
        SignalMast signalMast = InstanceManager.signalMastManagerInstance().getSignalMast(name);
        try {
            data.put(NAME, name);
            data.put(USERNAME, signalMast.getUserName());
            if (signalMast.getComment() != null) {
                data.put(COMMENT, signalMast.getComment());
            }
            String aspect = signalMast.getAspect();
            if (aspect == null) {
                aspect = ASPECT_UNKNOWN; //if null, set aspect to "Unknown"   
            }
            data.put(ASPECT, aspect);
            data.put(LIT, signalMast.getLit());
            data.put(TOKEN_HELD, signalMast.getHeld());
            //state is appearance, plus flags for held and dark statii
            if ((signalMast.getHeld()) && (signalMast.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.HELD) != null)) {
                data.put(STATE, ASPECT_HELD);
            } else if ((signalMast.getLit()) && (signalMast.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.DARK) != null)) {
                data.put(STATE, ASPECT_DARK);
            } else {
                data.put(STATE, aspect);
            }
        } catch (NullPointerException e) {
            log.error("Unable to get signalMast [{}].", name);
            throw new JsonException(404, Bundle.getMessage("ErrorObject", SIGNAL_MAST, name));
        }
        return root;
    }

    static public JsonNode getSignalMasts() throws JsonException {
        ArrayNode root = mapper.createArrayNode();
        for (String name : InstanceManager.signalMastManagerInstance().getSystemNameList()) {
            root.add(getSignalMast(name));
        }
        return root;
    }

    // TODO: test for HELD and DARK aspects
    static public void setSignalMast(String name, JsonNode data) throws JsonException {
        try {
            SignalMast signalMast = InstanceManager.signalMastManagerInstance().getSignalMast(name);
            if (data.path(USERNAME).isTextual()) {
                signalMast.setUserName(data.path(USERNAME).asText());
            }
            if (data.path(COMMENT).isTextual()) {
                signalMast.setComment(data.path(COMMENT).asText());
            }
            String aspect = data.path(ASPECT).asText();
            if (signalMast.getValidAspects().contains(aspect)) {
                signalMast.setAspect(aspect);
            } else {
                throw new JsonException(400, Bundle.getMessage("ErrorUnknownState", SIGNAL_MAST, aspect));
            }
        } catch (NullPointerException e) {
            log.error("Unable to get signal mast [{}].", name);
            throw new JsonException(404, Bundle.getMessage("ErrorObject", SIGNAL_MAST, name));
        }
    }

    static public JsonNode getTrain(String id) throws JsonException {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, TRAIN);
        ObjectNode data = root.putObject(DATA);
        try {
            Train train = TrainManager.instance().getTrainById(id);
            data.put(NAME, train.getName());
            data.put(ID, train.getId());
            data.put(DEPARTURE_TIME, train.getFormatedDepartureTime());
            data.put(DESCRIPTION, train.getDescription());
            data.put(COMMENT, train.getComment());
            data.put(ROUTE, train.getRoute().getName());
            data.put(ROUTE_ID, train.getRoute().getId());
            data.put(LOCATIONS, getRouteLocationsForTrain(train));
            data.put(ENGINES, getEnginesForTrain(train));
            data.put(CARS, getCarsForTrain(train));
            if (train.getTrainDepartsName() != null) {
                data.put(DEPARTURE_LOCATION, train.getTrainDepartsName());
            }
            if (train.getTrainTerminatesName() != null) {
                data.put(TERMINATES_LOCATION, train.getTrainTerminatesName());
            }
            data.put(LOCATION, train.getCurrentLocationName());
            if (train.getCurrentLocation()  != null) {
            	data.put(LOCATION_ID, train.getCurrentLocation().getId());
            }
            data.put(STATUS, train.getStatus());
            data.put(LENGTH, train.getTrainLength());
            data.put(WEIGHT, train.getTrainWeight());
            if (train.getLeadEngine() != null) {
                data.put(LEAD_ENGINE, train.getLeadEngine().toString());
            }
            if (train.getCabooseRoadAndNumber() != null) {
                data.put(CABOOSE, train.getCabooseRoadAndNumber());
            }

        } catch (NullPointerException e) {
            log.error("Unable to get train id [{}].", id);
            throw new JsonException(404, Bundle.getMessage("ErrorObject", TRAIN, id));
        }
        return root;
    }

    static public JsonNode getTrains() throws JsonException {
        ArrayNode root = mapper.createArrayNode();
        for (Train train : TrainManager.instance().getTrainsByNameList()) {
            root.add(getTrain(train.getId()));
        }
        return root;
    }

    static public void setTrain(String id, JsonNode data) {
        Train train = TrainManager.instance().getTrainById(id);
        train.move(data.path(id).asText());
    }

    static public JsonNode getTurnout(String name) throws JsonException {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, TURNOUT);
        ObjectNode data = root.putObject(DATA);
        try {
            Turnout turnout = InstanceManager.turnoutManagerInstance().getTurnout(name);
            data.put(NAME, turnout.getSystemName());
            data.put(USERNAME, turnout.getUserName());
            data.put(COMMENT, turnout.getComment());
            data.put(INVERTED, turnout.getInverted());
            switch (turnout.getKnownState()) {
                case Turnout.THROWN:
                    data.put(STATE, THROWN);
                    break;
                case Turnout.CLOSED:
                    data.put(STATE, CLOSED);
                    break;
                case Turnout.INCONSISTENT:
                    data.put(STATE, INCONSISTENT);
                    break;
                case Turnout.UNKNOWN:
                default:
                    data.put(STATE, UNKNOWN);
                    break;
            }
        } catch (NullPointerException e) {
            log.error("Unable to get turnout [{}].", name);
            throw new JsonException(404, Bundle.getMessage("ErrorObject", TURNOUT, name));
        }
        return root;
    }

    static public JsonNode getTurnouts() throws JsonException {
        ArrayNode root = mapper.createArrayNode();
        for (String name : InstanceManager.turnoutManagerInstance().getSystemNameList()) {
            root.add(getTurnout(name));
        }
        return root;
    }

    static public void putTurnout(String name, JsonNode data) throws JsonException {
        try {
            InstanceManager.turnoutManagerInstance().provideTurnout(name);
        } catch (Exception ex) {
            throw new JsonException(500, Bundle.getMessage("ErrorCreatingObject", TURNOUT, name));
        }
        setTurnout(name, data);
    }

    static public void setTurnout(String name, JsonNode data) throws JsonException {
        try {
            Turnout turnout = InstanceManager.turnoutManagerInstance().getTurnout(name);
            if (data.path(USERNAME).isTextual()) {
                turnout.setUserName(data.path(USERNAME).asText());
            }
            if (data.path(INVERTED).isBoolean()) {
                turnout.setInverted(data.path(INVERTED).asBoolean());
            }
            if (data.path(COMMENT).isTextual()) {
                turnout.setComment(data.path(COMMENT).asText());
            }
            int state = data.path(STATE).asInt(UNKNOWN);
            switch (state) {
                case THROWN:
                    turnout.setCommandedState(Turnout.THROWN);
                    break;
                case CLOSED:
                    turnout.setCommandedState(Turnout.CLOSED);
                    break;
                case UNKNOWN:
                    // leave state alone in this case
                    break;
                default:
                    throw new JsonException(400, Bundle.getMessage("ErrorUnknownState", TURNOUT, state));
            }
        } catch (NullPointerException ex) {
            log.error("Unable to get turnout [{}].", name);
            throw new JsonException(404, Bundle.getMessage("ErrorObject", TURNOUT, name));
        }
    }

    static public JsonNode getUnknown(String type) {
        return handleError(404, Bundle.getMessage("ErrorUnknownType", type));
    }

    static private ArrayNode getCarsForTrain(Train train) {
        ArrayNode clan = mapper.createArrayNode();
        CarManager carManager = CarManager.instance();
        List<Car> carList = carManager.getByTrainDestinationList(train);
        for (int k = 0; k < carList.size(); k++) {
            clan.add(getCar(carList.get(k).getId()).get(DATA)); //add each car's data to the carList array
        }
        return clan;  //return array of car data
    }

    static private ArrayNode getEnginesForTrain(Train train) {
        ArrayNode elan = mapper.createArrayNode();
        EngineManager engineManager = EngineManager.instance();
        List<RollingStock> engineList = engineManager.getByTrainList(train);
        for (int k = 0; k < engineList.size(); k++) {
            elan.add(getEngine(engineList.get(k).getId()).get(DATA)); //add each engine's data to the engineList array
        }
        return elan;  //return array of engine data
    }

    static private ArrayNode getRouteLocationsForTrain(Train train) throws JsonException {
        ArrayNode rlan = mapper.createArrayNode();
        List<String> routeList = train.getRoute().getLocationsBySequenceList();
        for (int r = 0; r < routeList.size(); r++) {
            ObjectNode rln = mapper.createObjectNode();
            RouteLocation rl = train.getRoute().getLocationById(routeList.get(r));
            rln.put(ID, rl.getId());
            rln.put(NAME, rl.getName());
            rln.put(DIRECTION, rl.getTrainDirectionString());
            rln.put(COMMENT, rl.getComment());
            rln.put(SEQUENCE, rl.getSequenceId());
            rln.put(EXPECTED_ARRIVAL, train.getExpectedArrivalTime(rl));
            rln.put(EXPECTED_DEPARTURE, train.getExpectedDepartureTime(rl));
            rln.put(LOCATION, getLocation(rl.getLocation().getId()).get(DATA));
            rlan.add(rln); //add this routeLocation to the routeLocation array
        }
        return rlan;  //return array of routeLocations
    }

    static public JsonNode getHello(int heartbeat) {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, HELLO);
        ObjectNode data = root.putObject(DATA);
        data.put(JMRI, jmri.Version.name());
        data.put(JSON, JSON_PROTOCOL_VERSION);
        data.put(HEARTBEAT, Math.round(heartbeat * 0.9f));
        data.put(RAILROAD, WebServerManager.getWebServerPreferences().getRailRoadName());
        data.put(NODE, NodeIdentity.identity());
        return root;
    }

    static public JsonNode getNetworkServices() {
        ArrayNode root = mapper.createArrayNode();
        for (ZeroConfService service : ZeroConfService.allServices()) {
            ObjectNode ns = mapper.createObjectNode();
            ns.put(NAME, service.name());
            ns.put(PORT, service.serviceInfo().getPort());
            ns.put(TYPE, service.type());
            Enumeration<String> pe = service.serviceInfo().getPropertyNames();
            while (pe.hasMoreElements()) {
                String pn = pe.nextElement();
                ns.put(pn, service.serviceInfo().getPropertyString(pn));
            }
            root.add(ns);
        }
        return root;
    }

    static protected ObjectNode handleError(int code, String message) {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, ERROR);
        ObjectNode data = root.putObject(DATA);
        data.put(CODE, code);
        data.put(MESSAGE, message);
        return root;
    }

    /**
     * Gets the {@link jmri.DccLocoAddress} for a String in the form
     * <code>number(type)</code> or
     * <code>number</code>.
     *
     * Type may be
     * <code>L</code> for long or
     * <code>S</code> for short. If the type is not specified, type is assumed
     * to be short.
     *
     * @param address
     * @return The DccLocoAddress for address.
     */
    static public DccLocoAddress addressForString(String address) {
        String[] components = address.split("[()]");
        int number = Integer.parseInt(components[0]);
        boolean isLong = false;
        if (components.length > 1 && "L".equals(components[1].toUpperCase())) {
            isLong = true;
        }
        return new DccLocoAddress(number, isLong);
    }
}
