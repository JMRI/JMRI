// JsonLister.java
package jmri.jmris.json;

import java.io.File;
import java.util.List;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Light;
import jmri.Memory;
import jmri.Metadata;
import jmri.Reporter;
import jmri.Route;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.SignalHead;
import jmri.Turnout;
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

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 *
 * @author rhwood
 */
public class JsonLister {

	private static ObjectMapper mapper = new ObjectMapper();
	private static Logger log = Logger.getLogger(JsonLister.class);

	static public JsonNode getCar(String id) {
		ObjectNode root = mapper.createObjectNode();
		root.put("type", "car");
		ObjectNode data = root.putObject("data");
		Car car = CarManager.instance().getById(id);
		data.put("id", car.getId());
		data.put("road", car.getRoad());
		data.put("number", car.getNumber());
		data.put("load", car.getLoad());
		data.put("locationId", car.getRouteLocationId());
		data.put("trackName", car.getTrackName());
		data.put("destinationId", car.getRouteDestinationId());
		data.put("destinationTrackName", car.getDestinationTrackName());
		data.put("type", car.getType());
		data.put("length", car.getLength());
		data.put("color", car.getColor());
		data.put("comment", car.getComment());
	    return root;
	}

	static public JsonNode getCars() {
		ObjectNode root = mapper.createObjectNode();
		root.put("type", "list");
		ArrayNode cars = root.putArray("list");
	    for (String id : CarManager.instance().getByIdList()) {
	        cars.add(getCar(id));
	    }
	    return root;
	}

	static public JsonNode getEngine(String id) {
		ObjectNode root = mapper.createObjectNode();
		root.put("type", "engine");
		ObjectNode data = root.putObject("data");
		Engine engine = EngineManager.instance().getById(id);
		data.put("id", engine.getId());
		data.put("road", engine.getRoad());
		data.put("number", engine.getNumber());
		data.put("locationId", engine.getRouteLocationId());
		data.put("trackName", engine.getTrackName());
		data.put("destinationId", engine.getRouteDestinationId());
		data.put("destinationTrackName", engine.getDestinationTrackName());
		data.put("model", engine.getModel());
		data.put("comment", engine.getComment());
	    return root;
	}

	static public JsonNode getEngines() {
		ObjectNode root = mapper.createObjectNode();
		root.put("type", "list");
		ArrayNode engines = root.putArray("list");
	    for (String id : EngineManager.instance().getByIdList()) {
	        engines.add(getEngine(id));
	    }
	    return root;
	}

	static public JsonNode getLight(String name) {
		ObjectNode root = mapper.createObjectNode();
		root.put("type", "light");
		ObjectNode data = root.putObject("data");
		Light light = InstanceManager.lightManagerInstance().getLight(name);
		data.put("name", light.getSystemName());
		data.put("userName", light.getUserName());
		data.put("comment", light.getComment());
		data.put("state", light.getState());
		return root;
	}

	static public JsonNode getLights() {
		ObjectNode root = mapper.createObjectNode();
		root.put("type", "list");
		ArrayNode lights = root.putArray("list");
		for (String name : InstanceManager.lightManagerInstance().getSystemNameList()) {
			lights.add(getLight(name));
		}
		return root;
	}

	static public JsonNode getLocation(String id) {
		ObjectNode root = mapper.createObjectNode();
		root.put("type", "location");
		ObjectNode data = root.putObject("data");
		try {
			Location location = LocationManager.instance().getLocationByName(id);
			data.put("name", location.getName());
			data.put("id", location.getId());
			data.put("length", location.getLength());
			data.put("comment", location.getComment());
		} catch (NullPointerException e) {
			root.put("type", "error");
			data.put("code", -1);
			data.put("message", "Unable to get location [" + id + "]");
			log.error("Unable to get location [" + id + "].", e);
		}
		return root;
	}

	static public JsonNode getLocations() {
		ObjectNode root = mapper.createObjectNode();
		root.put("type", "list");
		ArrayNode locations = root.putArray("list");
		for (String locationID : LocationManager.instance().getLocationsByNameList()) {
			locations.add(getLocation(locationID));
		}
		return root;
	}

	static public JsonNode getMemories() {
		ObjectNode root = mapper.createObjectNode();
		root.put("type", "list");
		ArrayNode memories = root.putArray("list");
        for (String name : InstanceManager.memoryManagerInstance().getSystemNameList()) {
            memories.add(getMemory(name));
        }
        return root;
	}

	static public JsonNode getMemory(String name) {
		ObjectNode root = mapper.createObjectNode();
		root.put("type", "memory");
		ObjectNode data = root.putObject("data");
		Memory memory = InstanceManager.memoryManagerInstance().getMemory(name);
        data.put("name", memory.getSystemName());
        data.put("userName", memory.getUserName());
        data.put("comment", memory.getComment());
        data.put("value", memory.getValue().toString());
		return root;
	}

	static public JsonNode getMetadata() {
		ObjectNode root = mapper.createObjectNode();
		root.put("type", "list");
		ArrayNode metadatas = root.putArray("list");
        List<String> names = Metadata.getSystemNameList();
        for (String name : names) {
            ObjectNode metadata = mapper.createObjectNode();
            metadata.put("type", "metadata");
            ObjectNode data = metadata.putObject("data");
            data.put("name", name);
            data.put("value", Metadata.getBySystemName(name));
            metadatas.add(metadata);
        }
        return root;
	}

	static public JsonNode getPanels() {
		List<String> disallowedFrames = WebServerManager.getWebServerPreferences().getDisallowedFrames();
		ObjectNode root = mapper.createObjectNode();
		root.put("type", "list");
		ArrayNode panels = root.putArray("list");
       	// list loaded Panels (ControlPanelEditor, PanelEditor, LayoutEditor)
        List<JmriJFrame> frames = JmriJFrame.getFrameList(ControlPanelEditor.class);
        for (JmriJFrame frame : frames) {
            if (frame.getAllowInFrameServlet()) {
                String title = ((JmriJFrame) ((Editor)frame).getTargetPanel().getTopLevelAncestor()).getTitle();
                if (!title.equals("") && !disallowedFrames.contains(title)) {
                	ObjectNode panel = mapper.createObjectNode();
                	panel.put("type", "panel");
                	ObjectNode data = panel.putObject("data");
                	data.put("name", "ControlPanel/" + title.replaceAll(" ", "%20").replaceAll("#", "%23"));
                	data.put("URL", "/panel/" + data.path("name").asText() + "?format=json");
                	data.put("userName", title);
                	data.put("type", "Control Panel");
                    panels.add(data);
                }
            }
        }
        frames = JmriJFrame.getFrameList(PanelEditor.class);
        for (JmriJFrame frame : frames) {
            if (frame.getAllowInFrameServlet() && !(LayoutEditor.class.isInstance(frame))) {  //skip LayoutEditor panels, as they will be added next
                String title = ((JmriJFrame) ((Editor)frame).getTargetPanel().getTopLevelAncestor()).getTitle();
                if (!title.equals("") && !disallowedFrames.contains(title)) {
                	ObjectNode panel = mapper.createObjectNode();
                	panel.put("type", "panel");
                	ObjectNode data = panel.putObject("data");
                	data.put("name", "Panel/" + title.replaceAll(" ", "%20").replaceAll("#", "%23"));
                	data.put("URL", "/panel/" + data.path("name").asText() + "?format=json");
                	data.put("userName", title);
                	data.put("type", "Panel");
                	panels.add(data);
                }
            }
        }
        frames = JmriJFrame.getFrameList(LayoutEditor.class);
        for (JmriJFrame frame : frames) {
            if (frame.getAllowInFrameServlet()) {
                String title = ((JmriJFrame) ((Editor)frame).getTargetPanel().getTopLevelAncestor()).getTitle();
                if (!title.equals("") && !disallowedFrames.contains(title)) {
                	ObjectNode panel = mapper.createObjectNode();
                	panel.put("type", "panel");
                	ObjectNode data = panel.putObject("data");
                	data.put("name", "Layout/" + title.replaceAll(" ", "%20").replaceAll("#", "%23"));
                	data.put("URL", "/panel/" + data.path("name").asText() + "?format=json");
                	data.put("userName", title);
                	data.put("type", "Layout");
                	panels.add(data);
                }
            }
        }
        return root;
	}
	
	static public JsonNode getPower() {
		ObjectNode root = mapper.createObjectNode();
		root.put("type", "power");
		ObjectNode data = root.putObject("data");
		try {
			data.put("state", InstanceManager.powerManagerInstance().getPower());
		} catch (JmriException e) {
			data.put("state", -1);
			log.error("Unable to get Power state." + e);
		}
		return root;
	}
	
	static public JsonNode getRailroad() {
		ObjectNode root = mapper.createObjectNode();
		root.put("type", "railroad");
		ObjectNode data = root.putObject("data");
		data.put("name", WebServerManager.getWebServerPreferences().getRailRoadName());
		return root;
	}

	static public JsonNode getReporter(String name) {
		ObjectNode root = mapper.createObjectNode();
		root.put("type", "reporter");
		ObjectNode data = root.putObject("data");
		Reporter reporter = InstanceManager.reporterManagerInstance().getReporter(name);
		data.put("name", reporter.getSystemName());
		data.put("userName", reporter.getUserName());
		data.put("state", reporter.getState());
		data.put("comment", reporter.getComment());
		data.put("currentReport", reporter.getCurrentReport().toString());
		data.put("lastReport", reporter.getLastReport().toString());
		return root;
	}

	static public JsonNode getReporters() {
		ObjectNode root = mapper.createObjectNode();
		root.put("type", "list");
		ArrayNode reporters = root.putArray("list");
		for (String name : InstanceManager.reporterManagerInstance().getSystemNameList()) {
			reporters.add(getReporter(name));
		}
		return root;
	}

	static public JsonNode getRosterEntry(String id) {
		ObjectNode root = mapper.createObjectNode();
		root.put("type", "rosterEntry");
		ObjectNode entry = root.putObject("data");
		RosterEntry re = Roster.instance().getEntryForId(id);
		entry.put("name", re.getId());
		entry.put("dccAddress", re.getDccAddress());
		entry.put("isLongAddress", re.isLongAddress());
		entry.put("roadName", re.getRoadName());
		entry.put("roadNumber", re.getRoadNumber());
		entry.put("mfg", re.getMfg());
		entry.put("model", re.getModel());
		entry.put("comment", re.getComment());
		entry.put("maxSpeedPct", Integer.valueOf(re.getMaxSpeedPCT()).toString());
		File file = new File(re.getImagePath());
		entry.put("imageFileName", file.getName());
		file = new File(re.getIconPath());
		entry.put("imageIconName", file.getName());
		ArrayNode labels = entry.putArray("functionKeys");
		for (int i = 0; i < re.getMAXFNNUM(); i++) {
			ObjectNode label = mapper.createObjectNode();
			label.put("name", "F" + i);
			label.put("label", (re.getFunctionLabel(i) != null) ? re.getFunctionLabel(i) : "F" + i);
			label.put("lockable", re.getFunctionLockable(i));
			labels.add(label);
		}
		return root;
	}
	
	static public JsonNode getRoster() {
		ObjectNode root = mapper.createObjectNode();
		root.put("type", "list");
		ArrayNode roster = root.putArray("list");
    	for (RosterEntry re : Roster.instance().matchingList(null, null, null, null, null, null, null)) {
    		roster.add(getRosterEntry(re.getId()));
    	}
		return root;
	}

	static public JsonNode getRoute(String name) {
		ObjectNode root = mapper.createObjectNode();
		root.put("type", "route");
		ObjectNode data = root.putObject("data");
		try {
			Route route = InstanceManager.routeManagerInstance().getRoute(name);
			SensorManager s = InstanceManager.sensorManagerInstance();
			data.put("name", route.getSystemName());
            data.put("userName", route.getUserName());
            data.put("comment", route.getComment());
            data.put("state", (s.provideSensor(route.getTurnoutsAlignedSensor()) != null) ? (s.provideSensor(route.getTurnoutsAlignedSensor())).getKnownState() : Route.UNKNOWN);
		} catch (NullPointerException e) {
			root.put("type", "error");
			data.put("code", -1);
			data.put("message", "Unable to get route");
			log.error("Unable to get route." + e);
		}
		return root;
	}

	static public JsonNode getRoutes() {
		ObjectNode root = mapper.createObjectNode();
		root.put("type", "list");
		ArrayNode routes = root.putArray("list");
        for (String name : InstanceManager.routeManagerInstance().getSystemNameList()) {
            routes.add(getRoute(name));
        }
        return root;
	}

	static public JsonNode getSensor(String name) {
		ObjectNode root = mapper.createObjectNode();
		root.put("type", "sensor");
		ObjectNode data = root.putObject("data");
        Sensor sensor = InstanceManager.sensorManagerInstance().getSensor(name);
        data.put("name", name);
        data.put("userName", sensor.getUserName());
        data.put("comment", sensor.getComment());
        data.put("inverted", sensor.getInverted());
        data.put("state", sensor.getKnownState());		
		return root;
	}
	
	static public JsonNode getSensors() {
		ObjectNode root = mapper.createObjectNode();
		root.put("type", "list");
		ArrayNode sensors = root.putArray("list");
        for (String name : InstanceManager.sensorManagerInstance().getSystemNameList()) {
            sensors.add(getSensor(name));
        }
        return root;
	}

	static public JsonNode getSignalHead(String name) {
		ObjectNode root = mapper.createObjectNode();
		root.put("type", "signalHead");
		ObjectNode data = root.putObject("data");
		SignalHead signalHead = InstanceManager.signalHeadManagerInstance().getSignalHead(name);
		data.put("name", name);
		data.put("userName", signalHead.getUserName());
		data.put("comment", signalHead.getComment());
		data.put("state", signalHead.getAppearance());
		data.put("lit", signalHead.getLit());
        return root;
	}
	
	static public JsonNode getSignalHeads() {
		ObjectNode root = mapper.createObjectNode();
		root.put("type", "list");
		ArrayNode signalHeads = root.putArray("list");
        for (String name : InstanceManager.signalHeadManagerInstance().getSystemNameList()) {
            signalHeads.add(getSignalHead(name));
        }
        return root;
	}

	static public JsonNode getTrain(String id) {
		ObjectNode root = mapper.createObjectNode();
		root.put("type", "train");
		ObjectNode data = root.putObject("data");
		try {
			Train train = TrainManager.instance().getTrainById(id);
			data.put("name", train.getName());
			data.put("id", train.getId());
			data.put("departureTime", train.getFormatedDepartureTime());
			data.put("description", train.getDescription());
			data.put("comment", train.getComment());
			data.put("route", train.getRoute().getName());
			data.put("routeId", train.getRoute().getId());
			data.put("routeLocations", getRouteLocationsForTrain(train));
			data.put("engines", getEnginesForTrain(train));
			data.put("cars", getCarsForTrain(train));
			if (train.getTrainDepartsName() != null) {
				data.put("trainDepartsName", train.getTrainDepartsName());
			}
			if (train.getTrainTerminatesName() != null) {
				data.put("trainTerminatesName", train.getTrainTerminatesName());
			}
			data.put("currentLocationName", train.getCurrentLocationName());
			data.put("status", train.getStatus());
			data.put("trainLength", train.getTrainLength());
			data.put("trainWeight", train.getTrainWeight());
			data.put("numberCarsInTrain", train.getNumberCarsInTrain());
	        if (train.getLeadEngine() != null) {
	        	data.put("leadEngine", train.getLeadEngine().toString());
	        }
	        if (train.getCabooseRoadAndNumber() != null) {
	        	data.put("cabooseRoadAndNumber", train.getCabooseRoadAndNumber());
	        }
			
		} catch (NullPointerException e) {
			root.put("type", "error");
			data.put("code", -1);
			data.put("message", "Unable to get train [" + id + "]");
			log.error("Unable to get train [" + id + "].", e);
		}
		return root;
	}

	static public JsonNode getTrains() {
		ObjectNode root = mapper.createObjectNode();
		root.put("type", "list");
		ArrayNode trains = root.putArray("list");
		for (String trainID : TrainManager.instance().getTrainsByNameList()) {
			trains.add(getTrain(trainID));
		}
		return root;
	}

	static public JsonNode getTurnout(String name) {
		ObjectNode root = mapper.createObjectNode();
		root.put("type", "turnout");
		ObjectNode data = root.putObject("data");
		try {
			Turnout turnout = InstanceManager.turnoutManagerInstance().getTurnout(name);
			data.put("name", turnout.getSystemName());
            data.put("userName", turnout.getUserName());
            data.put("comment", turnout.getComment());
            data.put("inverted", turnout.getInverted());
			data.put("state", turnout.getKnownState());
		} catch (NullPointerException e) {
			root.put("type", "error");
			data.put("code", -1);
			data.put("message", "Unable to get turnout [" + name + "]");
			log.error("Unable to get turnout [" + name + "]." + e);
		}
		return root;
	}

	static public JsonNode getTurnouts() {
		ObjectNode root = mapper.createObjectNode();
		root.put("type", "list");
		ArrayNode turnouts = root.putArray("list");
        for (String name : InstanceManager.turnoutManagerInstance().getSystemNameList()) {
            turnouts.add(getTurnout(name));
        }
        return root;
	}

	static public JsonNode getUnknown() {
		ObjectNode root = mapper.createObjectNode();
		root.put("type", "error");
		ObjectNode data = root.putObject("data");
		data.put("code", "-1");
		data.put("message", "unknown type");
		return root;
	}

	static private ArrayNode getCarsForTrain(Train train) {
		ArrayNode clan = mapper.createArrayNode();
		CarManager carManager = CarManager.instance();
		List<String> carList = carManager.getByTrainDestinationList(train);
		for (int k = 0; k < carList.size(); k++) {
			clan.add(getCar(carList.get(k)).get("data")); //add each car's data to the carList array
		}
		return clan;  //return array of car data
	}

	static private ArrayNode getEnginesForTrain(Train train) {
		ArrayNode elan = mapper.createArrayNode();
		EngineManager engineManager = EngineManager.instance();
		List<String> engineList = engineManager.getByTrainList(train);
		for (int k = 0; k < engineList.size(); k++) {
			elan.add(getEngine(engineList.get(k)).get("data")); //add each engine's data to the engineList array
		}
		return elan;  //return array of engine data
	}

	static private ArrayNode getRouteLocationsForTrain(Train train) {
		ArrayNode rlan = mapper.createArrayNode();
		List<String> routeList = train.getRoute().getLocationsBySequenceList();
		for (int r = 0; r < routeList.size(); r++) {
			ObjectNode rln = mapper.createObjectNode();
			RouteLocation rl = train.getRoute().getLocationById(routeList.get(r));			
			rln.put("id", 				rl.getId());
			rln.put("name", 			rl.getName());
			rln.put("trainDirection", 	rl.getTrainDirectionString());
			rln.put("comment", 			rl.getComment());
			rln.put("sequenceId", 		rl.getSequenceId());
			rln.put("expectedArrivalTime", train.getExpectedArrivalTime(rl));
			rln.put("expectedDepartureTime", train.getExpectedDepartureTime(rl));
			rln.put("location", 		rl.getLocation().getName());
			rln.put("locationId", 		rl.getLocation().getId());
			rln.put("locationLength", 	rl.getLocation().getLength());
			rln.put("locationComment", 	rl.getLocation().getComment());
			rlan.add(rln); //add this routeLocation to the routeLocation array
		}
		return rlan;  //return array of routeLocations
	}
	
}