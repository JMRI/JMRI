// JsonUtil.java
package jmri.jmris.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.util.List;
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
import static jmri.jmris.json.JSON.*;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.panelEditor.PanelEditor;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.util.JmriJFrame;
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

    private static ObjectMapper mapper = new ObjectMapper();
    private static Logger log = LoggerFactory.getLogger(JsonUtil.class);

    static public JsonNode getCar(String id) {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, CAR);
        ObjectNode data = root.putObject(DATA);
        Car car = CarManager.instance().getById(id);
        data.put(ID, car.getId());
        data.put(ROAD, car.getRoad());
        data.put(NUMBER, car.getNumber());
        data.put(LOAD, car.getLoad());
        data.put(LOCATION, car.getRouteLocationId());
        data.put(LOCATION_TRACK, car.getTrackName());
        data.put(DESTINATION, car.getRouteDestinationId());
        data.put(DESTINATION_TRACK, car.getDestinationTrackName());
        data.put(TYPE, car.getType());
        data.put(LENGTH, car.getLength());
        data.put(COLOR, car.getColor());
        data.put(COMMENT, car.getComment());
        return root;
    }

    static public JsonNode getCars() {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, LIST);
        ArrayNode cars = root.putArray(LIST);
        for (String id : CarManager.instance().getByIdList()) {
            cars.add(getCar(id));
        }
        return root;
    }

    static public JsonNode getEngine(String id) {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, ENGINE);
        ObjectNode data = root.putObject(DATA);
        Engine engine = EngineManager.instance().getById(id);
        data.put(ID, engine.getId());
        data.put(ROAD, engine.getRoad());
        data.put(NUMBER, engine.getNumber());
        data.put(LOCATION, engine.getRouteLocationId());
        data.put(LOCATION_TRACK, engine.getTrackName());
        data.put(DESTINATION, engine.getRouteDestinationId());
        data.put(DESTINATION_TRACK, engine.getDestinationTrackName());
        data.put(MODEL, engine.getModel());
        data.put(COMMENT, engine.getComment());
        return root;
    }

    static public JsonNode getEngines() {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, LIST);
        ArrayNode engines = root.putArray(LIST);
        for (String id : EngineManager.instance().getByIdList()) {
            engines.add(getEngine(id));
        }
        return root;
    }

    static public JsonNode getLight(String name) {
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
    }

    static public JsonNode getLights() {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, LIST);
        ArrayNode lights = root.putArray(LIST);
        for (String name : InstanceManager.lightManagerInstance().getSystemNameList()) {
            lights.add(getLight(name));
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
            log.error("Unable to get light {}", name, e);
            throw new JsonException(404, Bundle.getMessage("ErrorObject", LIGHT, name));
        }
    }

    static public JsonNode getLocation(String id) {
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
            root = handleError(404, Bundle.getMessage("ErrorObject", LOCATION, id));
            log.error("Unable to get location id={}.", id, e);
        }
        return root;
    }

    static public JsonNode getLocations() {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, LIST);
        ArrayNode locations = root.putArray(LIST);
        for (String locationID : LocationManager.instance().getLocationsByIdList()) {
            locations.add(getLocation(locationID));
        }
        return root;
    }

    static public JsonNode getMemories() {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, LIST);
        ArrayNode memories = root.putArray(LIST);
        for (String name : InstanceManager.memoryManagerInstance().getSystemNameList()) {
            memories.add(getMemory(name));
        }
        return root;
    }

    static public JsonNode getMemory(String name) {
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
            root = handleError(404, Bundle.getMessage("ErrorObject", MEMORY, name));
            log.error("Unable to get memory {}", name);
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
            log.error("Unable to get memory {}", name);
            throw new JsonException(500, ex);
        }
    }

    static public JsonNode getMetadata(String name) {
        String metadata = Metadata.getBySystemName(name);
        ObjectNode root;
        if (metadata != null) {
            root = mapper.createObjectNode();
            root.put(TYPE, METADATA);
            ObjectNode data = root.putObject(DATA);
            data.put(NAME, name);
            data.put(VALUE, Metadata.getBySystemName(name));
        } else {
            root = handleError(404, Bundle.getMessage("ErrorObject", METADATA, name));
        }
        return root;
    }

    static public JsonNode getMetadata() {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, LIST);
        ArrayNode metadatas = root.putArray(LIST);
        List<String> names = Metadata.getSystemNameList();
        for (String name : names) {
            metadatas.add(getMetadata(name));
        }
        return root;
    }

    static public JsonNode getPanels(String format) {
        List<String> disallowedFrames = WebServerManager.getWebServerPreferences().getDisallowedFrames();
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, LIST);
        ArrayNode panels = root.putArray(LIST);
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
                    panels.add(data);
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
                    panels.add(data);
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
                    panels.add(data);
                }
            }
        }
        return root;
    }

    static public JsonNode getPower() {
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
            root = handleError(500, Bundle.getMessage("ErrorPower"));
            log.error("Unable to get Power state.", e);
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
        data.put(REPORT, reporter.getCurrentReport().toString());
        data.put(LAST_REPORT, reporter.getLastReport().toString());
        return root;
    }

    static public JsonNode getReporters() {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, LIST);
        ArrayNode reporters = root.putArray(LIST);
        for (String name : InstanceManager.reporterManagerInstance().getSystemNameList()) {
            reporters.add(getReporter(name));
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
            throw new JsonException(404, Bundle.getMessage("ErrorObject", ROUTE, name));
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
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, LIST);
        ArrayNode roster = root.putArray(LIST);
        for (RosterEntry re : Roster.instance().matchingList(null, null, null, null, null, null, null)) {
            roster.add(getRosterEntry(re.getId()));
        }
        return root;
    }

    static public JsonNode getRoute(String name) {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, ROUTE);
        ObjectNode data = root.putObject(DATA);
        try {
            Route route = InstanceManager.routeManagerInstance().getRoute(name);
            SensorManager s = InstanceManager.sensorManagerInstance();
            data.put(NAME, route.getSystemName());
            data.put(USERNAME, route.getUserName());
            data.put(COMMENT, route.getComment());
            // TODO: Completely insulate JSON state from Turnout state
            data.put(STATE, (s.getSensor(route.getTurnoutsAlignedSensor()) != null) ? (s.getSensor(route.getTurnoutsAlignedSensor())).getKnownState() : UNKNOWN);
        } catch (NullPointerException e) {
            root = handleError(404, Bundle.getMessage("ErrorObject", ROUTE, name));
            log.error("Unable to get route.", e);
        }
        return root;
    }

    static public JsonNode getRoutes() {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, LIST);
        ArrayNode routes = root.putArray(LIST);
        for (String name : InstanceManager.routeManagerInstance().getSystemNameList()) {
            routes.add(getRoute(name));
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
                case TOGGLE:
                    route.setRoute();
                    break;
                default:
                    throw new JsonException(400, Bundle.getMessage("ErrorUnknownState", ROUTE, state));
            }
        } catch (NullPointerException ex) {
            log.error("Unable to get route {}", name);
            throw new JsonException(404, Bundle.getMessage("ErrorObject", ROUTE, name));
        }
    }

    static public JsonNode getSensor(String name) {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, SENSOR);
        ObjectNode data = root.putObject(DATA);
        try {
            Sensor sensor = InstanceManager.sensorManagerInstance().getSensor(name);
            data.put(NAME, name);
            data.put(USERNAME, sensor.getUserName());
            data.put(COMMENT, sensor.getComment());
            data.put(INVERTED, sensor.getInverted());
            data.put(STATE, sensor.getKnownState());
        } catch (NullPointerException e) {
            root = handleError(404, Bundle.getMessage("ErrorObject", SENSOR, name));
            log.error("Unable to get sensor.", e);
        }
        return root;
    }

    static public JsonNode getSensors() {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, LIST);
        ArrayNode sensors = root.putArray(LIST);
        for (String name : InstanceManager.sensorManagerInstance().getSystemNameList()) {
            sensors.add(getSensor(name));
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
            log.error("Unable to get sensor [{}].", name, e);
            throw new JsonException(404, Bundle.getMessage("ErrorObject", SENSOR, name));
        } catch (JmriException ex) {
            throw new JsonException(500, ex);
        }
    }

    static public JsonNode getSignalHead(String name) {
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
            root = handleError(404, Bundle.getMessage("ErrorObject", SIGNAL_HEAD, name));
            log.error("Unable to get signalHead [{}].", name, e);
        }
        return root;
    }

    static public JsonNode getSignalHeads() {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, LIST);
        ArrayNode signalHeads = root.putArray(LIST);
        for (String name : InstanceManager.signalHeadManagerInstance().getSystemNameList()) {
            signalHeads.add(getSignalHead(name));
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
            log.error("Unable to get signal head {}", name, e);
            throw new JsonException(404, Bundle.getMessage("ErrorObject", SIGNAL_HEAD, name));
        }
    }

    static public JsonNode getSignalMast(String name) {
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
            root = handleError(404, Bundle.getMessage("ErrorObject", SIGNAL_MAST, name));
            log.error("Unable to get signalMast [{}].", name, e);
        }
        return root;
    }

    static public JsonNode getSignalMasts() {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, LIST);
        ArrayNode signalMasts = root.putArray(LIST);
        for (String name : InstanceManager.signalMastManagerInstance().getSystemNameList()) {
            signalMasts.add(getSignalMast(name));
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
            log.error("Unable to get signal mast [{}].", name, e);
            throw new JsonException(404, Bundle.getMessage("ErrorObject", SIGNAL_MAST, name));
        }
    }

    static public JsonNode getTrain(String id) {
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
            root = handleError(404, Bundle.getMessage("ErrorObject", TRAIN, id));
            log.error("Unable to get train id= {}.", id, e);
        }
        return root;
    }

    static public JsonNode getTrains() {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, LIST);
        ArrayNode trains = root.putArray(LIST);
        for (String trainID : TrainManager.instance().getTrainsByNameList()) {
            trains.add(getTrain(trainID));
        }
        return root;
    }

    static public void setTrain(String id, JsonNode data) {
        Train train = TrainManager.instance().getTrainById(id);
        train.move(data.path(id).asText());
    }

    static public JsonNode getTurnout(String name) {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, TURNOUT);
        ObjectNode data = root.putObject(DATA);
        try {
            Turnout turnout = InstanceManager.turnoutManagerInstance().getTurnout(name);
            data.put(NAME, turnout.getSystemName());
            data.put(USERNAME, turnout.getUserName());
            data.put(COMMENT, turnout.getComment());
            data.put(INVERTED, turnout.getInverted());
            data.put(STATE, turnout.getKnownState());
        } catch (NullPointerException e) {
            root = handleError(404, Bundle.getMessage("ErrorObject", TURNOUT, name));
            log.error("Unable to get turnout [{}].", name, e);
        }
        return root;
    }

    static public JsonNode getTurnouts() {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, LIST);
        ArrayNode turnouts = root.putArray(LIST);
        for (String name : InstanceManager.turnoutManagerInstance().getSystemNameList()) {
            turnouts.add(getTurnout(name));
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
            log.error("Unable to get turnout {}.", name, ex);
            throw new JsonException(404, Bundle.getMessage("ErrorObject", TURNOUT, name));
        }
    }

    static public JsonNode getUnknown(String type) {
        return handleError(404, Bundle.getMessage("ErrorUnknownType", type));
    }

    static private ArrayNode getCarsForTrain(Train train) {
        ArrayNode clan = mapper.createArrayNode();
        CarManager carManager = CarManager.instance();
        List<String> carList = carManager.getByTrainDestinationList(train);
        for (int k = 0; k < carList.size(); k++) {
            clan.add(getCar(carList.get(k)).get(DATA)); //add each car's data to the carList array
        }
        return clan;  //return array of car data
    }

    static private ArrayNode getEnginesForTrain(Train train) {
        ArrayNode elan = mapper.createArrayNode();
        EngineManager engineManager = EngineManager.instance();
        List<String> engineList = engineManager.getByTrainList(train);
        for (int k = 0; k < engineList.size(); k++) {
            elan.add(getEngine(engineList.get(k)).get(DATA)); //add each engine's data to the engineList array
        }
        return elan;  //return array of engine data
    }

    static private ArrayNode getRouteLocationsForTrain(Train train) {
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

    static protected ObjectNode handleError(int code, String message) {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, ERROR);
        ObjectNode data = root.putObject(DATA);
        data.put(CODE, code);
        data.put(MESSAGE, message);
        return root;
    }
}
