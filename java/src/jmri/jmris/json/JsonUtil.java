// JsonUtil.java
package jmri.jmris.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
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
import static jmri.jmris.json.JSON.ACTIVE_PROFILE;
import static jmri.jmris.json.JSON.ADDRESS;
import static jmri.jmris.json.JSON.ADD_COMMENT;
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
import static jmri.jmris.json.JSON.DECODER_FAMILY;
import static jmri.jmris.json.JSON.DECODER_MODEL;
import static jmri.jmris.json.JSON.DEPARTURE_LOCATION;
import static jmri.jmris.json.JSON.DEPARTURE_TIME;
import static jmri.jmris.json.JSON.DESCRIPTION;
import static jmri.jmris.json.JSON.DESTINATION;
import static jmri.jmris.json.JSON.DIRECTION;
import static jmri.jmris.json.JSON.ENGINE;
import static jmri.jmris.json.JSON.ENGINES;
import static jmri.jmris.json.JSON.ERROR;
import static jmri.jmris.json.JSON.EXPECTED_ARRIVAL;
import static jmri.jmris.json.JSON.EXPECTED_DEPARTURE;
import static jmri.jmris.json.JSON.F;
import static jmri.jmris.json.JSON.FINAL_DESTINATION;
import static jmri.jmris.json.JSON.FORMER_NODES;
import static jmri.jmris.json.JSON.FORWARD;
import static jmri.jmris.json.JSON.FUNCTION_KEYS;
import static jmri.jmris.json.JSON.GROUP;
import static jmri.jmris.json.JSON.HAZARDOUS;
import static jmri.jmris.json.JSON.HEARTBEAT;
import static jmri.jmris.json.JSON.HELLO;
import static jmri.jmris.json.JSON.ICON;
import static jmri.jmris.json.JSON.ICON_NAME;
import static jmri.jmris.json.JSON.ID;
import static jmri.jmris.json.JSON.IMAGE;
import static jmri.jmris.json.JSON.INACTIVE;
import static jmri.jmris.json.JSON.INCONSISTENT;
import static jmri.jmris.json.JSON.INVERTED;
import static jmri.jmris.json.JSON.IS_LONG_ADDRESS;
import static jmri.jmris.json.JSON.JMRI;
import static jmri.jmris.json.JSON.JSON;
import static jmri.jmris.json.JSON.JSON_PROTOCOL_VERSION;
import static jmri.jmris.json.JSON.KERNEL;
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
import static jmri.jmris.json.JSON.LOCKABLE;
import static jmri.jmris.json.JSON.MAX_SPD_PCT;
import static jmri.jmris.json.JSON.MEMORY;
import static jmri.jmris.json.JSON.MESSAGE;
import static jmri.jmris.json.JSON.METADATA;
import static jmri.jmris.json.JSON.MFG;
import static jmri.jmris.json.JSON.MODEL;
import static jmri.jmris.json.JSON.NAME;
import static jmri.jmris.json.JSON.NETWORK_SERVICE;
import static jmri.jmris.json.JSON.NODE;
import static jmri.jmris.json.JSON.NULL;
import static jmri.jmris.json.JSON.NUMBER;
import static jmri.jmris.json.JSON.OFF;
import static jmri.jmris.json.JSON.ON;
import static jmri.jmris.json.JSON.OWNER;
import static jmri.jmris.json.JSON.PANEL;
import static jmri.jmris.json.JSON.PORT;
import static jmri.jmris.json.JSON.POSITION;
import static jmri.jmris.json.JSON.POWER;
import static jmri.jmris.json.JSON.PREFIX;
import static jmri.jmris.json.JSON.RAILROAD;
import static jmri.jmris.json.JSON.RATE;
import static jmri.jmris.json.JSON.REMOVE_COMMENT;
import static jmri.jmris.json.JSON.REPORT;
import static jmri.jmris.json.JSON.REPORTER;
import static jmri.jmris.json.JSON.RETURN_WHEN_EMPTY;
import static jmri.jmris.json.JSON.ROAD;
import static jmri.jmris.json.JSON.ROSTER;
import static jmri.jmris.json.JSON.ROSTER_ENTRY;
import static jmri.jmris.json.JSON.ROSTER_GROUP;
import static jmri.jmris.json.JSON.ROSTER_GROUPS;
import static jmri.jmris.json.JSON.ROUTE;
import static jmri.jmris.json.JSON.ROUTE_ID;
import static jmri.jmris.json.JSON.SELECTED_ICON;
import static jmri.jmris.json.JSON.SENSOR;
import static jmri.jmris.json.JSON.SEQUENCE;
import static jmri.jmris.json.JSON.SHUNTING_FUNCTION;
import static jmri.jmris.json.JSON.SIGNAL_HEAD;
import static jmri.jmris.json.JSON.SIGNAL_MAST;
import static jmri.jmris.json.JSON.SIZE_LIMIT;
import static jmri.jmris.json.JSON.STATE;
import static jmri.jmris.json.JSON.STATUS;
import static jmri.jmris.json.JSON.STATUS_CODE;
import static jmri.jmris.json.JSON.SYSTEM_CONNECTION;
import static jmri.jmris.json.JSON.TERMINATES_LOCATION;
import static jmri.jmris.json.JSON.THROWN;
import static jmri.jmris.json.JSON.TIME;
import static jmri.jmris.json.JSON.TOGGLE;
import static jmri.jmris.json.JSON.TOKEN_HELD;
import static jmri.jmris.json.JSON.TRACK;
import static jmri.jmris.json.JSON.TRAIN;
import static jmri.jmris.json.JSON.TURNOUT;
import static jmri.jmris.json.JSON.TYPE;
import static jmri.jmris.json.JSON.UNKNOWN;
import static jmri.jmris.json.JSON.URL;
import static jmri.jmris.json.JSON.USERNAME;
import static jmri.jmris.json.JSON.UTILITY;
import static jmri.jmris.json.JSON.VALUE;
import static jmri.jmris.json.JSON.WEIGHT;
import jmri.jmrit.consisttool.ConsistFile;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.panelEditor.PanelEditor;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.RollingStock;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.trains.Train;
import static jmri.jmrit.operations.trains.TrainCommon.splitString;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.rostergroup.RosterGroup;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.ConnectionConfigManager;
import jmri.jmrix.SystemConnectionMemo;
import jmri.profile.ProfileManager;
import jmri.util.ConnectionNameFromSystemName;
import jmri.util.JmriJFrame;
import jmri.util.node.NodeIdentity;
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

    static public JsonNode getCar(Locale locale, String id) {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, CAR);
        root.put(DATA, JsonUtil.getCar(CarManager.instance().getById(id)));
        return root;
    }

    static public JsonNode getCars(Locale locale) {
        ArrayNode root = mapper.createArrayNode();
        for (RollingStock rs : CarManager.instance().getByIdList()) {
            root.add(JsonUtil.getCar(locale, rs.getId()));
        }
        return root;
    }

    /**
     * Delete the consist at the given address.
     *
     * @param locale  The locale to format exceptions in
     * @param address The address of the consist to delete.
     * @throws JsonException This exception has code 404 if the consist does not
     *                       exist.
     */
    static public void delConsist(Locale locale, DccLocoAddress address) throws JsonException {
        try {
            if (InstanceManager.getDefault(jmri.ConsistManager.class).getConsistList().contains(address)) {
                InstanceManager.getDefault(jmri.ConsistManager.class).delConsist(address);
            } else {
                throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", CONSIST, address.toString()));
            }
        } catch (NullPointerException ex) {
            throw new JsonException(503, Bundle.getMessage(locale, "ErrorNoConsistManager")); // NOI18N
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
     * @param locale  The locale to throw exceptions in.
     * @param address The address of the consist to get.
     * @return The JSON representation of the consist.
     * @throws JsonException This exception has code 404 if the consist does not
     *                       exist.
     */
    static public JsonNode getConsist(Locale locale, DccLocoAddress address) throws JsonException {
        try {
            if (InstanceManager.getDefault(jmri.ConsistManager.class).getConsistList().contains(address)) {
                ObjectNode root = mapper.createObjectNode();
                root.put(TYPE, CONSIST);
                ObjectNode data = root.putObject(DATA);
                Consist consist = InstanceManager.getDefault(jmri.ConsistManager.class).getConsist(address);
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
                throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", CONSIST, address.toString()));
            }
        } catch (NullPointerException ex) {
            throw new JsonException(503, Bundle.getMessage(locale, "ErrorNoConsistManager")); // NOI18N
        }
    }

    /**
     * Add a consist.
     *
     * Adds a consist, populating it with information from data.
     *
     * @param locale  The locale to throw exceptions in.
     * @param address The address of the new consist.
     * @param data    The JSON representation of the consist. See
     * {@link #getConsist(Locale, jmri.DccLocoAddress) } for the
     *                JSON structure.
     * @throws JsonException
     */
    static public void putConsist(Locale locale, DccLocoAddress address, JsonNode data) throws JsonException {
        try {
            if (!InstanceManager.getDefault(jmri.ConsistManager.class).getConsistList().contains(address)) {
                InstanceManager.getDefault(jmri.ConsistManager.class).getConsist(address);
                setConsist(locale, address, data);
            }
        } catch (NullPointerException ex) {
            throw new JsonException(503, Bundle.getMessage(locale, "ErrorNoConsistManager")); // NOI18N
        }
    }

    /**
     * Get a list of consists.
     *
     * @param locale The locale to throw exceptions in.
     * @return JSON array of consists as in the structure returned by
     * {@link #getConsist(Locale, jmri.DccLocoAddress) }
     * @throws JsonException
     */
    static public JsonNode getConsists(Locale locale) throws JsonException {
        try {
            ArrayNode root = mapper.createArrayNode();
            for (DccLocoAddress address : InstanceManager.getDefault(jmri.ConsistManager.class).getConsistList()) {
                root.add(getConsist(locale, address));
            }
            return root;
        } catch (NullPointerException ex) {
            throw new JsonException(503, Bundle.getMessage(locale, "ErrorNoConsistManager")); // NOI18N
        }
    }

    /**
     * Change the properties and locomotives of a consist.
     *
     * This method takes as input the JSON representation of a consist as
     * provided by {@link #getConsist(Locale, jmri.DccLocoAddress) }.
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
     * @param locale  the locale to throw exceptions in
     * @param address the consist address
     * @param data    the consist as a JsonObject
     * @throws JsonException
     */
    static public void setConsist(Locale locale, DccLocoAddress address, JsonNode data) throws JsonException {
        try {
            if (InstanceManager.getDefault(jmri.ConsistManager.class).getConsistList().contains(address)) {
                Consist consist = InstanceManager.getDefault(jmri.ConsistManager.class).getConsist(address);
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
                    (new ConsistFile()).writeFile(InstanceManager.getDefault(jmri.ConsistManager.class).getConsistList());
                } catch (IOException ex) {
                    throw new JsonException(500, ex.getLocalizedMessage());
                }
            }
        } catch (NullPointerException ex) {
            throw new JsonException(503, Bundle.getMessage(locale, "ErrorNoConsistManager")); // NOI18N
        }
    }

    static public JsonNode getEngine(Locale locale, String id) {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, ENGINE);
        root.put(DATA, JsonUtil.getEngine(EngineManager.instance().getById(id)));
        return root;
    }

    static public JsonNode getEngines(Locale locale) {
        ArrayNode root = mapper.createArrayNode();
        for (RollingStock rs : EngineManager.instance().getByIdList()) {
            root.add(getEngine(locale, rs.getId()));
        }
        return root;
    }

    static public JsonNode getLight(Locale locale, String name) throws JsonException {
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
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", LIGHT, name));
        }
    }

    static public JsonNode getLights(Locale locale) throws JsonException {
        ArrayNode root = mapper.createArrayNode();
        for (String name : InstanceManager.lightManagerInstance().getSystemNameList()) {
            root.add(getLight(locale, name));
        }
        return root;
    }

    static public void putLight(Locale locale, String name, JsonNode data) throws JsonException {
        try {
            InstanceManager.lightManagerInstance().provideLight(name);
        } catch (Exception ex) {
            throw new JsonException(500, Bundle.getMessage(locale, "ErrorCreatingObject", LIGHT, name));
        }
        setLight(locale, name, data);
    }

    static public void setLight(Locale locale, String name, JsonNode data) throws JsonException {
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
                    throw new JsonException(400, Bundle.getMessage(locale, "ErrorUnknownState", LIGHT, state));
            }
        } catch (NullPointerException e) {
            log.error("Unable to get light [{}].", name);
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", LIGHT, name));
        }
    }

    static public JsonNode getLocation(Locale locale, String id) throws JsonException {
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
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", LOCATION, id));
        }
        return root;
    }

    static public JsonNode getLocations(Locale locale) throws JsonException {
        ArrayNode root = mapper.createArrayNode();
        for (Location location : LocationManager.instance().getLocationsByIdList()) {
            root.add(getLocation(locale, location.getId()));
        }
        return root;
    }

    static public JsonNode getMemories(Locale locale) throws JsonException {
        ArrayNode root = mapper.createArrayNode();
        for (String name : InstanceManager.memoryManagerInstance().getSystemNameList()) {
            root.add(getMemory(locale, name));
        }
        return root;
    }

    static public JsonNode getMemory(Locale locale, String name) throws JsonException {
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
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", MEMORY, name));
        }
        return root;
    }

    static public void putMemory(Locale locale, String name, JsonNode data) throws JsonException {
        try {
            InstanceManager.memoryManagerInstance().provideMemory(name);
        } catch (Exception ex) {
            throw new JsonException(500, Bundle.getMessage(locale, "ErrorCreatingObject", MEMORY, name));
        }
        setMemory(locale, name, data);
    }

    static public void setMemory(Locale locale, String name, JsonNode data) throws JsonException {
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
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", MEMORY, name));
        }
    }

    static public JsonNode getMetadata(Locale locale, String name) throws JsonException {
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
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", METADATA, name));
        }
        return root;
    }

    static public JsonNode getMetadata(Locale locale) throws JsonException {
        ArrayNode root = mapper.createArrayNode();
        List<String> names = Metadata.getSystemNameList();
        for (String name : names) {
            root.add(getMetadata(locale, name));
        }
        return root;
    }

    static public ObjectNode getPanel(Locale locale, Editor editor, String format) {
        if (editor.getAllowInFrameServlet()) {
            String title = ((JmriJFrame) editor.getTargetPanel().getTopLevelAncestor()).getTitle();
            if (!title.isEmpty() && !WebServerManager.getWebServerPreferences().getDisallowedFrames().contains(title)) {
                String type = PANEL;
                String name = "Panel";
                if (editor instanceof ControlPanelEditor) {
                    type = CONTROL_PANEL;
                    name = "ControlPanel";
                } else if (editor instanceof LayoutEditor) {
                    type = LAYOUT_PANEL;
                    name = "Layout";
                }
                ObjectNode root = mapper.createObjectNode();
                root.put(TYPE, PANEL);
                ObjectNode data = root.putObject(DATA);
                data.put(NAME, name + "/" + title.replaceAll(" ", "%20").replaceAll("#", "%23")); // NOI18N
                data.put(URL, "/panel/" + data.path(NAME).asText() + "?format=" + format); // NOI18N
                data.put(USERNAME, title);
                data.put(TYPE, type);
                return root;
            }
        }
        return null;
    }

    static public JsonNode getPanels(Locale locale, String format) {
        ArrayNode root = mapper.createArrayNode();
        // list loaded Panels (ControlPanelEditor, PanelEditor, LayoutEditor)
        for (Editor editor : Editor.getEditors(ControlPanelEditor.class)) {
            ObjectNode panel = JsonUtil.getPanel(locale, editor, format);
            if (panel != null) {
                root.add(panel);
            }
        }
        for (Editor editor : Editor.getEditors(PanelEditor.class)) {
            if (!(LayoutEditor.class.isInstance(editor))) {  //skip LayoutEditor panels, as they will be added later
                ObjectNode panel = JsonUtil.getPanel(locale, editor, format);
                if (panel != null) {
                    root.add(panel);
                }
            }
        }
        for (Editor editor : Editor.getEditors(LayoutEditor.class)) {
            ObjectNode panel = JsonUtil.getPanel(locale, editor, format);
            if (panel != null) {
                root.add(panel);
            }
        }
        return root;
    }

    @Deprecated
    static public JsonNode getPower(Locale locale) throws JsonException {
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
            throw new JsonException(500, Bundle.getMessage(locale, "ErrorPower"));
        } catch (NullPointerException e) {
            // No PowerManager is defined; just report it as UNKNOWN
            data.put(STATE, UNKNOWN);
        }
        return root;
    }

    @Deprecated
    static public void setPower(Locale locale, JsonNode data) throws JsonException {
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
                    throw new JsonException(400, Bundle.getMessage(locale, "ErrorUnknownState", POWER, state));
            }
        } catch (JmriException ex) {
            throw new JsonException(500, ex);
        }
    }

    static public JsonNode getRailroad(Locale locale) {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, RAILROAD);
        ObjectNode data = root.putObject(DATA);
        data.put(NAME, WebServerManager.getWebServerPreferences().getRailRoadName());
        return root;
    }

    static public JsonNode getReporter(Locale locale, String name) {
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

    static public JsonNode getReporters(Locale locale) {
        ArrayNode root = mapper.createArrayNode();
        for (String name : InstanceManager.reporterManagerInstance().getSystemNameList()) {
            root.add(getReporter(locale, name));
        }
        return root;
    }

    static public void putReporter(Locale locale, String name, JsonNode data) throws JsonException {
        try {
            InstanceManager.reporterManagerInstance().provideReporter(name);
        } catch (Exception ex) {
            throw new JsonException(500, Bundle.getMessage(locale, "ErrorCreatingObject", REPORTER, name));
        }
        setReporter(locale, name, data);
    }

    static public void setReporter(Locale locale, String name, JsonNode data) throws JsonException {
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
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", REPORTER, name));
        }
    }

    /**
     * Returns the JSON representation of a roster entry.
     *
     * Note that this returns, for images and icons, a URL relative to the root
     * folder of the JMRI server. It is expected that clients will fill in the
     * server IP address and port as they know it to be.
     *
     * @param locale
     * @param id     The id of an entry in the roster.
     * @return a roster entry in JSON notation
     * @deprcated since 4.3.5
     */
    @Deprecated
    static public JsonNode getRosterEntry(Locale locale, String id) {
        return JsonUtil.getRosterEntry(locale, Roster.instance().getEntryForId(id));
    }

    /**
     * Returns the JSON representation of a roster entry.
     *
     * Note that this returns, for images and icons, a URL relative to the root
     * folder of the JMRI server. It is expected that clients will fill in the
     * server IP address and port as they know it to be.
     *
     * @param locale
     * @param re     A RosterEntry that may or may not be in the roster.
     * @return a roster entry in JSON notation
     * @deprecated since 4.3.5
     */
    @Deprecated
    static public JsonNode getRosterEntry(Locale locale, RosterEntry re) {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, ROSTER_ENTRY);
        ObjectNode entry = root.putObject(DATA);
        entry.put(NAME, re.getId());
        entry.put(ADDRESS, re.getDccAddress());
        entry.put(IS_LONG_ADDRESS, re.isLongAddress());
        entry.put(ROAD, re.getRoadName());
        entry.put(NUMBER, re.getRoadNumber());
        entry.put(MFG, re.getMfg());
        entry.put(DECODER_MODEL, re.getDecoderModel());
        entry.put(DECODER_FAMILY, re.getDecoderFamily());
        entry.put(MODEL, re.getModel());
        entry.put(COMMENT, re.getComment());
        entry.put(MAX_SPD_PCT, Integer.toString(re.getMaxSpeedPCT()));
        entry.put(IMAGE, (re.getImagePath() != null) ? "/" + ROSTER + "/" + re.getId() + "/" + IMAGE : (String) null);
        entry.put(ICON, (re.getIconPath() != null) ? "/" + ROSTER + "/" + re.getId() + "/" + ICON : (String) null);
        entry.put(SHUNTING_FUNCTION, re.getShuntingFunction());
        ArrayNode labels = entry.putArray(FUNCTION_KEYS);
        for (int i = 0; i <= re.getMAXFNNUM(); i++) {
            ObjectNode label = mapper.createObjectNode();
            label.put(NAME, F + i);
            label.put(LABEL, re.getFunctionLabel(i));
            label.put(LOCKABLE, re.getFunctionLockable(i));
            label.put(ICON, (re.getFunctionImage(i) != null) ? "/" + ROSTER + "/" + re.getId() + "/" + F + i + "/" + ICON : (String) null);
            label.put(SELECTED_ICON, (re.getFunctionSelectedImage(i) != null) ? "/" + ROSTER + "/" + re.getId() + "/" + F + i + "/" + SELECTED_ICON : (String) null);
            labels.add(label);
        }
        ArrayNode rga = entry.putArray(ROSTER_GROUPS);
        for (RosterGroup group : re.getGroups()) {
            rga.add(group.getName());
        }
        return root;
    }

    /**
     *
     * @param locale The locale of the requesting client
     * @param data   A JsonNode optionally containing a group name in the
     *               "group" node
     * @return the Roster as a Json Array
     * @deprecated since 4.3.5
     */
    @Deprecated
    static public JsonNode getRoster(Locale locale, JsonNode data) {
        String group = (!data.path(GROUP).isMissingNode()) ? data.path(GROUP).asText() : null;
        if (Roster.ALLENTRIES.equals(group)) {
            group = null;
        }
        String roadName = (!data.path(ROAD).isMissingNode()) ? data.path(ROAD).asText() : null;
        String roadNumber = (!data.path(NUMBER).isMissingNode()) ? data.path(NUMBER).asText() : null;
        String dccAddress = (!data.path(ADDRESS).isMissingNode()) ? data.path(ADDRESS).asText() : null;
        String mfg = (!data.path(MFG).isMissingNode()) ? data.path(MFG).asText() : null;
        String decoderModel = (!data.path(DECODER_MODEL).isMissingNode()) ? data.path(DECODER_MODEL).asText() : null;
        String decoderFamily = (!data.path(DECODER_FAMILY).isMissingNode()) ? data.path(DECODER_FAMILY).asText() : null;
        String id = (!data.path(NAME).isMissingNode()) ? data.path(NAME).asText() : null;
        ArrayNode root = mapper.createArrayNode();
        for (RosterEntry re : Roster.instance().getEntriesMatchingCriteria(roadName, roadNumber, dccAddress, mfg, decoderModel, decoderFamily, id, group)) {
            root.add(getRosterEntry(locale, re.getId()));
        }
        return root;
    }

    /**
     * 
     * @param locale The locale of the requesting client
     * @return the list of Roster groups
     * @deprecated since 4.3.5
     */
    @Deprecated
    static public JsonNode getRosterGroups(Locale locale) {
        ArrayNode root = mapper.createArrayNode();
        root.add(getRosterGroup(locale, Roster.ALLENTRIES));
        for (String name : Roster.instance().getRosterGroupList()) {
            root.add(getRosterGroup(locale, name));
        }
        return root;
    }

    /**
     * 
     * @param locale The locale of the requesting client
     * @param name The name of the group
     * @return A description of the group including its name and size
     * @deprecated since 4.3.5
     */
    @Deprecated
    static public JsonNode getRosterGroup(Locale locale, String name) {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, ROSTER_GROUP);
        ObjectNode data = root.putObject(DATA);
        data.put(NAME, name);
        data.put(LENGTH, Roster.instance().getEntriesInGroup(name).size());
        return root;
    }

    static public JsonNode getRoute(Locale locale, String name) throws JsonException {
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
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", ROUTE, name));
        }
        return root;
    }

    static public JsonNode getRoutes(Locale locale) throws JsonException {
        ArrayNode root = mapper.createArrayNode();
        for (String name : InstanceManager.routeManagerInstance().getSystemNameList()) {
            root.add(getRoute(locale, name));
        }
        return root;
    }

    /**
     * Routes can be set by passing a JsonNode with the node <em>state</em>
     * equal to <em>8</em> (the aspect of {@link jmri.Route#TOGGLE}).
     *
     * @param locale The locale to throw exceptions in
     * @param name   The name of the route
     * @param data   A JsonNode containing route attributes to set
     * @throws JsonException
     * @see jmri.Route#TOGGLE
     */
    static public void setRoute(Locale locale, String name, JsonNode data) throws JsonException {
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
                    throw new JsonException(400, Bundle.getMessage(locale, "ErrorUnknownState", ROUTE, state));
            }
        } catch (NullPointerException ex) {
            log.error("Unable to get route [{}].", name);
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", ROUTE, name));
        }
    }

    static public JsonNode getSensor(Locale locale, String name) throws JsonException {
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
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", SENSOR, name));
        }
        return root;
    }

    static public JsonNode getSensors(Locale locale) throws JsonException {
        ArrayNode root = mapper.createArrayNode();
        for (String name : InstanceManager.sensorManagerInstance().getSystemNameList()) {
            root.add(getSensor(locale, name));
        }
        return root;
    }

    static public void putSensor(Locale locale, String name, JsonNode data) throws JsonException {
        try {
            InstanceManager.sensorManagerInstance().provideSensor(name);
        } catch (Exception ex) {
            throw new JsonException(500, Bundle.getMessage(locale, "ErrorCreatingObject", TURNOUT, name));
        }
        setSensor(locale, name, data);
    }

    static public void setSensor(Locale locale, String name, JsonNode data) throws JsonException {
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
                    throw new JsonException(400, Bundle.getMessage(locale, "ErrorUnknownState", SENSOR, state));
            }
        } catch (NullPointerException e) {
            log.error("Unable to get sensor [{}].", name);
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", SENSOR, name));
        } catch (JmriException ex) {
            throw new JsonException(500, ex);
        }
    }

    static public JsonNode getSignalHead(Locale locale, String name) throws JsonException {
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
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", SIGNAL_HEAD, name));
        }
        return root;
    }

    static public JsonNode getSignalHeads(Locale locale) throws JsonException {
        ArrayNode root = mapper.createArrayNode();
        for (String name : InstanceManager.signalHeadManagerInstance().getSystemNameList()) {
            root.add(getSignalHead(locale, name));
        }
        return root;
    }

    static public void setSignalHead(Locale locale, String name, JsonNode data) throws JsonException {
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
                throw new JsonException(400, Bundle.getMessage(locale, "ErrorUnknownState", SIGNAL_HEAD, state));
            }
        } catch (NullPointerException e) {
            log.error("Unable to get signal head [{}].", name);
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", SIGNAL_HEAD, name));
        }
    }

    static public JsonNode getSignalMast(Locale locale, String name) throws JsonException {
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
            } else if ((!signalMast.getLit()) && (signalMast.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.DARK) != null)) {
                data.put(STATE, ASPECT_DARK);
            } else {
                data.put(STATE, aspect);
            }
        } catch (NullPointerException e) {
            log.error("Unable to get signalMast [{}].", name);
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", SIGNAL_MAST, name));
        }
        return root;
    }

    static public JsonNode getSignalMasts(Locale locale) throws JsonException {
        ArrayNode root = mapper.createArrayNode();
        for (String name : InstanceManager.signalMastManagerInstance().getSystemNameList()) {
            root.add(getSignalMast(locale, name));
        }
        return root;
    }

    // TODO: test for HELD and DARK aspects
    static public void setSignalMast(Locale locale, String name, JsonNode data) throws JsonException {
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
                throw new JsonException(400, Bundle.getMessage(locale, "ErrorUnknownState", SIGNAL_MAST, aspect));
            }
        } catch (NullPointerException e) {
            log.error("Unable to get signal mast [{}].", name);
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", SIGNAL_MAST, name));
        }
    }

    static public JsonNode getSystemConnections(Locale locale) {
        ArrayNode root = mapper.createArrayNode();
        ArrayList<String> prefixes = new ArrayList<String>();
        for (ConnectionConfig config : InstanceManager.getDefault(ConnectionConfigManager.class)) {
            if (!config.getDisabled()) {
                ObjectNode connection = mapper.createObjectNode().put(TYPE, SYSTEM_CONNECTION);
                ObjectNode data = connection.putObject(DATA);
                data.put(NAME, config.getConnectionName());
                data.put(MFG, config.getManufacturer());
                data.put(PREFIX, config.getAdapter().getSystemConnectionMemo().getSystemPrefix());
                prefixes.add(config.getAdapter().getSystemConnectionMemo().getSystemPrefix());
                root.add(connection);
            }
        }
        for (Object instance : InstanceManager.getList(SystemConnectionMemo.class)) {
            SystemConnectionMemo memo = (SystemConnectionMemo) instance;
            if (!memo.getDisabled() && !prefixes.contains(memo.getSystemPrefix())) {
                ObjectNode connection = mapper.createObjectNode().put(TYPE, SYSTEM_CONNECTION);
                ObjectNode data = connection.putObject(DATA);
                data.put(NAME, memo.getUserName());
                data.put(PREFIX, memo.getSystemPrefix());
                data.putNull(MFG);
                prefixes.add(memo.getSystemPrefix());
                root.add(connection);
            }
        }
        // Following is required because despite there being a SystemConnectionMemo
        // for the default internal connection, it is not used for the default internal
        // connection. This allows a client to map the server's internal objects.
        String prefix = "I";
        if (!prefixes.contains(prefix)) {
            ObjectNode connection = mapper.createObjectNode().put(TYPE, SYSTEM_CONNECTION);
            ObjectNode data = connection.putObject(DATA);
            data.put(NAME, ConnectionNameFromSystemName.getConnectionName(prefix));
            data.put(PREFIX, prefix);
            data.putNull(MFG);
            root.add(connection);
        }
        return root;
    }

    @Deprecated
    static public JsonNode getTime(Locale locale) throws JsonException {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, TIME);
        ObjectNode data = root.putObject(DATA);
        data.put(TIME, new ISO8601DateFormat().format(InstanceManager.timebaseInstance().getTime()));
        data.put(RATE, InstanceManager.timebaseInstance().getRate());
        data.put(STATE, InstanceManager.timebaseInstance().getRun() ? ON : OFF);
        return root;
    }

    @Deprecated
    static public void setTime(Locale locale, JsonNode data) throws JsonException {
        try {
            if (data.path(TIME).isTextual()) {
                InstanceManager.timebaseInstance().setTime(new ISO8601DateFormat().parse(data.path(TIME).asText()));
            }
            if (data.path(RATE).isDouble()) {
                InstanceManager.clockControlInstance().setRate(data.path(RATE).asDouble());
            }
            if (data.path(STATE).isInt()) {
                InstanceManager.timebaseInstance().setRun(data.path(STATE).asInt() == ON);
            }
        } catch (ParseException ex) {
            log.error("Time \"{}\" not in ISO 8601 date format", data.path(TIME).asText());
            throw new JsonException(400, Bundle.getMessage(locale, "ErrorTimeFormat"));
        }
    }

    static public JsonNode getTrain(Locale locale, String id) throws JsonException {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, TRAIN);
        ObjectNode data = root.putObject(DATA);
        try {
            Train train = TrainManager.instance().getTrainById(id);
            data.put(NAME, train.getName());
            data.put(ICON_NAME, train.getIconName());
            data.put(ID, train.getId());
            data.put(DEPARTURE_TIME, train.getFormatedDepartureTime());
            data.put(DESCRIPTION, train.getDescription());
            data.put(COMMENT, train.getComment());
            data.put(ROUTE, train.getRoute().getName());
            data.put(ROUTE_ID, train.getRoute().getId());
            data.put(LOCATIONS, getRouteLocationsForTrain(locale, train));
            data.put(ENGINES, getEnginesForTrain(locale, train));
            data.put(CARS, getCarsForTrain(locale, train));
            if (train.getTrainDepartsName() != null) {
                data.put(DEPARTURE_LOCATION, train.getTrainDepartsName());
            }
            if (train.getTrainTerminatesName() != null) {
                data.put(TERMINATES_LOCATION, train.getTrainTerminatesName());
            }
            data.put(LOCATION, train.getCurrentLocationName());
            if (train.getCurrentLocation() != null) {
                data.put(LOCATION_ID, train.getCurrentLocation().getId());
            }
            data.put(STATUS, train.getStatus(locale));
            data.put(STATUS_CODE, train.getStatusCode());
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
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", TRAIN, id));
        }
        return root;
    }

    static public JsonNode getTrains(Locale locale) throws JsonException {
        ArrayNode root = mapper.createArrayNode();
        for (Train train : TrainManager.instance().getTrainsByNameList()) {
            root.add(getTrain(locale, train.getId()));
        }
        return root;
    }

    /**
     * Sets the properties in the data parameter for the train with the given
     * id.
     *
     * Currently only moves the train to the location given with the key
     * {@value jmri.jmris.json.JSON#LOCATION}. If the move cannot be completed,
     * throws error code 428.
     *
     * @param locale The locale to throw exceptions in.
     * @param id     The id of the train.
     * @param data   Train data to change.
     * @throws JsonException
     */
    static public void setTrain(Locale locale, String id, JsonNode data) throws JsonException {
        Train train = TrainManager.instance().getTrainById(id);
        if (!data.path(LOCATION).isMissingNode()) {
            String location = data.path(LOCATION).asText();
            if (location.equals(NULL)) {
                train.terminate();
            } else if (!train.move(location)) {
                throw new JsonException(428, Bundle.getMessage(locale, "ErrorTrainMovement", id, location));
            }
        }
    }

    @Deprecated
    static public JsonNode getTurnout(Locale locale, String name) throws JsonException {
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
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", TURNOUT, name));
        }
        return root;
    }

    @Deprecated
    static public JsonNode getTurnouts(Locale locale) throws JsonException {
        ArrayNode root = mapper.createArrayNode();
        for (String name : InstanceManager.turnoutManagerInstance().getSystemNameList()) {
            root.add(getTurnout(locale, name));
        }
        return root;
    }

    @Deprecated
    static public void putTurnout(Locale locale, String name, JsonNode data) throws JsonException {
        try {
            InstanceManager.turnoutManagerInstance().provideTurnout(name);
        } catch (Exception ex) {
            throw new JsonException(500, Bundle.getMessage(locale, "ErrorCreatingObject", TURNOUT, name));
        }
        setTurnout(locale, name, data);
    }

    @Deprecated
    static public void setTurnout(Locale locale, String name, JsonNode data) throws JsonException {
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
                    throw new JsonException(400, Bundle.getMessage(locale, "ErrorUnknownState", TURNOUT, state));
            }
        } catch (NullPointerException ex) {
            log.error("Unable to get turnout [{}].", name);
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", TURNOUT, name));
        }
    }

    static public JsonNode getUnknown(Locale locale, String type) {
        return handleError(404, Bundle.getMessage(locale, "ErrorUnknownType", type));
    }

    static private ArrayNode getCarsForTrain(Locale locale, Train train) {
        ArrayNode clan = mapper.createArrayNode();
        CarManager carManager = CarManager.instance();
        List<Car> carList = carManager.getByTrainDestinationList(train);
        for (Car car : carList) {
            clan.add(getCar(locale, car.getId()).get(DATA)); //add each car's data to the carList array
        }
        return clan;  //return array of car data
    }

    static private ArrayNode getEnginesForTrain(Locale locale, Train train) {
        ArrayNode elan = mapper.createArrayNode();
        EngineManager engineManager = EngineManager.instance();
        List<Engine> engineList = engineManager.getByTrainBlockingList(train);
        for (Engine engine : engineList) {
            elan.add(getEngine(locale, engine.getId()).get(DATA)); //add each engine's data to the engineList array
        }
        return elan;  //return array of engine data
    }

    static private ArrayNode getRouteLocationsForTrain(Locale locale, Train train) throws JsonException {
        ArrayNode rlan = mapper.createArrayNode();
        List<RouteLocation> routeList = train.getRoute().getLocationsBySequenceList();
        for (RouteLocation route : routeList) {
            ObjectNode rln = mapper.createObjectNode();
            RouteLocation rl = route;
            rln.put(ID, rl.getId());
            rln.put(NAME, rl.getName());
            rln.put(DIRECTION, rl.getTrainDirectionString());
            rln.put(COMMENT, rl.getComment());
            rln.put(SEQUENCE, rl.getSequenceId());
            rln.put(EXPECTED_ARRIVAL, train.getExpectedArrivalTime(rl));
            rln.put(EXPECTED_DEPARTURE, train.getExpectedDepartureTime(rl));
            rln.put(LOCATION, getLocation(locale, rl.getLocation().getId()).get(DATA));
            rlan.add(rln); //add this routeLocation to the routeLocation array
        }
        return rlan;  //return array of routeLocations
    }

    static public JsonNode getHello(Locale locale, int heartbeat) {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, HELLO);
        ObjectNode data = root.putObject(DATA);
        data.put(JMRI, jmri.Version.name());
        data.put(JSON, JSON_PROTOCOL_VERSION);
        data.put(HEARTBEAT, Math.round(heartbeat * 0.9f));
        data.put(RAILROAD, WebServerManager.getWebServerPreferences().getRailRoadName());
        data.put(NODE, NodeIdentity.identity());
        data.put(ACTIVE_PROFILE, ProfileManager.defaultManager().getActiveProfile().getName());
        return root;
    }

    static public JsonNode getNetworkServices(Locale locale) {
        ArrayNode root = mapper.createArrayNode();
        for (ZeroConfService service : ZeroConfService.allServices()) {
            ObjectNode ns = mapper.createObjectNode().put(TYPE, NETWORK_SERVICE);
            ObjectNode data = ns.putObject(DATA);
            data.put(NAME, service.name());
            data.put(PORT, service.serviceInfo().getPort());
            data.put(TYPE, service.type());
            Enumeration<String> pe = service.serviceInfo().getPropertyNames();
            while (pe.hasMoreElements()) {
                String pn = pe.nextElement();
                data.put(pn, service.serviceInfo().getPropertyString(pn));
            }
            root.add(ns);
        }
        return root;
    }

    public static JsonNode getNode(Locale locale) {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, NODE);
        ObjectNode data = root.putObject(DATA);
        data.put(NODE, NodeIdentity.identity());
        ArrayNode nodes = mapper.createArrayNode();
        for (String node : NodeIdentity.formerIdentities()) {
            nodes.add(node);
        }
        data.put(FORMER_NODES, nodes);
        return root;
    }

    /**
     * JSON errors should be handled by throwing a
     * {@link jmri.server.json.JsonException}.
     *
     * @param code
     * @param message
     * @return
     * @deprecated
     */
    @Deprecated
    static public ObjectNode handleError(int code, String message) {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, ERROR);
        ObjectNode data = root.putObject(DATA);
        data.put(CODE, code);
        data.put(MESSAGE, message);
        return root;
    }

    /**
     * Gets the {@link jmri.DccLocoAddress} for a String in the form
     * <code>number(type)</code> or <code>number</code>.
     *
     * Type may be <code>L</code> for long or <code>S</code> for short. If the
     * type is not specified, type is assumed to be short.
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

    static public ObjectNode getCar(Car car) {
        ObjectNode node = JsonUtil.getRollingStock(car);
        node.put(LOAD, car.getLoadName()); // NOI18N
        node.put(HAZARDOUS, car.isHazardous());
        node.put(REMOVE_COMMENT, car.getDropComment());
        node.put(ADD_COMMENT, car.getPickupComment());
        node.put(KERNEL, car.getKernelName());
        node.put(UTILITY, car.isUtility());
        if (car.getFinalDestinationTrack() != null) {
            node.put(FINAL_DESTINATION, JsonUtil.getLocationAndTrack(car.getFinalDestinationTrack(), null));
        } else if (car.getFinalDestination() != null) {
            node.put(FINAL_DESTINATION, JsonUtil.getLocation(car.getFinalDestination(), null));
        }
        if (car.getReturnWhenEmptyDestTrack() != null) {
            node.put(RETURN_WHEN_EMPTY, JsonUtil.getLocationAndTrack(car.getReturnWhenEmptyDestTrack(), null));
        } else if (car.getReturnWhenEmptyDestination() != null) {
            node.put(RETURN_WHEN_EMPTY, JsonUtil.getLocation(car.getReturnWhenEmptyDestination(), null));
        }
        return node;
    }

    static public ObjectNode getEngine(Engine engine) {
        ObjectNode node = JsonUtil.getRollingStock(engine);
        node.put(MODEL, engine.getModel());
        node.put(CONSIST, engine.getConsistName());
        return node;
    }

    static private ObjectNode getRollingStock(RollingStock rs) {
        ObjectNode node = mapper.createObjectNode();
        node.put(ID, rs.getId());
        node.put(NUMBER, splitString(rs.getNumber()));
        node.put(ROAD, rs.getRoadName());
        String[] type = rs.getTypeName().split("-"); // second half of string
        // can be anything
        node.put(TYPE, type[0]);
        node.put(LENGTH, rs.getLength());
        node.put(COLOR, rs.getColor());
        node.put(OWNER, rs.getOwner());
        node.put(COMMENT, rs.getComment());
        if (rs.getTrack() != null) {
            node.put(LOCATION, JsonUtil.getLocationAndTrack(rs.getTrack(), rs.getRouteLocation()));
        } else if (rs.getLocation() != null) {
            node.put(LOCATION, JsonUtil.getLocation(rs.getLocation(), rs.getRouteLocation()));
        }
        if (rs.getDestinationTrack() != null) {
            node.put(DESTINATION, JsonUtil.getLocationAndTrack(rs.getDestinationTrack(), rs.getRouteDestination()));
        } else if (rs.getDestination() != null) {
            node.put(DESTINATION, JsonUtil.getLocation(rs.getDestination(), rs.getRouteDestination()));
        }
        return node;
    }

    static private ObjectNode getLocation(Location location, RouteLocation routeLocation) {
        ObjectNode node = mapper.createObjectNode();
        node.put(NAME, location.getName());
        node.put(ID, location.getId());
        if (routeLocation != null) {
            node.put(ROUTE, routeLocation.getId());
        }
        return node;
    }

    static private ObjectNode getLocationAndTrack(Track track, RouteLocation routeLocation) {
        ObjectNode node = JsonUtil.getLocation(track.getLocation(), routeLocation);
        node.put(TRACK, JsonUtil.getTrack(track));
        return node;
    }

    static private ObjectNode getTrack(Track track) {
        ObjectNode node = mapper.createObjectNode();
        node.put(NAME, track.getName());
        node.put(ID, track.getId());
        return node;
    }

}
