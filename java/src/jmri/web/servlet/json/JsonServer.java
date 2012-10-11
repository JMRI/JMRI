// JsonServer.java
package jmri.web.servlet.json;

import java.io.File;
import java.util.List;

import jmri.InstanceManager;
import jmri.JmriException;
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
public class JsonServer {

	protected ObjectMapper mapper;
	
	private static Logger log = Logger.getLogger(JsonServer.class);

	public JsonServer() {
		this.mapper = new ObjectMapper();
	}

	protected JsonNode getMemories() {
		ObjectNode root = this.mapper.createObjectNode();
		root.put("type", "list");
		ArrayNode memories = root.putArray("list");
        for (String name : InstanceManager.memoryManagerInstance().getSystemNameList()) {
            memories.add(this.getMemory(name));
        }
        return root;
	}

	protected JsonNode getMemory(String name) {
		ObjectNode root = this.mapper.createObjectNode();
		root.put("type", "memory");
		ObjectNode data = root.putObject("data");
		Memory memory = InstanceManager.memoryManagerInstance().getMemory(name);
        data.put("name", memory.getSystemName());
        data.put("userName", memory.getUserName());
        data.put("comment", memory.getComment());
        data.put("value", memory.getValue().toString());
		return root;
	}

	protected JsonNode getMetadata() {
		ObjectNode root = this.mapper.createObjectNode();
		root.put("type", "list");
		ArrayNode metadatas = root.putArray("list");
        List<String> names = Metadata.getSystemNameList();
        for (String name : names) {
            ObjectNode metadata = this.mapper.createObjectNode();
            metadata.put("type", "metadata");
            ObjectNode data = metadata.putObject("data");
            data.put("name", name);
            data.put("value", Metadata.getBySystemName(name));
            metadatas.add(metadata);
        }
        return root;
	}

	protected JsonNode getPanels() {
		List<String> disallowedFrames = WebServerManager.getWebServerPreferences().getDisallowedFrames();
		ObjectNode root = this.mapper.createObjectNode();
		root.put("type", "list");
		ArrayNode panels = root.putArray("list");
       	// list loaded Panels (ControlPanelEditor, PanelEditor, LayoutEditor)
        List<JmriJFrame> frames = JmriJFrame.getFrameList(ControlPanelEditor.class);
        for (JmriJFrame frame : frames) {
            if (frame.getAllowInFrameServlet()) {
                String title = ((JmriJFrame) ((Editor)frame).getTargetPanel().getTopLevelAncestor()).getTitle();
                if (!title.equals("") && !disallowedFrames.contains(title)) {
                	ObjectNode panel = this.mapper.createObjectNode();
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
                	ObjectNode panel = this.mapper.createObjectNode();
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
                	ObjectNode panel = this.mapper.createObjectNode();
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
	
	protected JsonNode getPower() {
		ObjectNode root = this.mapper.createObjectNode();
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
	
	protected JsonNode getRailroad() {
		ObjectNode root = this.mapper.createObjectNode();
		root.put("type", "railroad");
		ObjectNode data = root.putObject("data");
		data.put("name", WebServerManager.getWebServerPreferences().getRailRoadName());
		return root;
	}

	protected JsonNode getReporter(String name) {
		ObjectNode root = this.mapper.createObjectNode();
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

	protected JsonNode getReporters() {
		ObjectNode root = this.mapper.createObjectNode();
		root.put("type", "list");
		ArrayNode reporters = root.putArray("list");
		for (String name : InstanceManager.reporterManagerInstance().getSystemNameList()) {
			reporters.add(this.getReporter(name));
		}
		return root;
	}

	protected JsonNode getRosterEntry(String id) {
		ObjectNode root = this.mapper.createObjectNode();
		root.put("type", "rosterEntry");
		ObjectNode entry = root.putObject("data");
		RosterEntry re = Roster.instance().getEntryForId(id);
		entry.put("name", re.getId());
		entry.put("dccAddress", re.getDccAddress());
		entry.put("addressLength", re.isLongAddress() ? "L" : "S");
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
			ObjectNode label = this.mapper.createObjectNode();
			label.put("name", "F" + i);
			label.put("label", (re.getFunctionLabel(i) != null) ? re.getFunctionLabel(i) : "F" + i);
			label.put("lockable", re.getFunctionLockable(i));
			labels.add(label);
		}
		return root;
	}
	
	protected JsonNode getRoster() {
		ObjectNode root = this.mapper.createObjectNode();
		root.put("type", "list");
		ArrayNode roster = root.putArray("list");
    	for (RosterEntry re : Roster.instance().matchingList(null, null, null, null, null, null, null)) {
    		roster.add(this.getRosterEntry(re.getId()));
    	}
		return root;
	}

	protected JsonNode getRoute(String name) {
		ObjectNode root = this.mapper.createObjectNode();
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

	protected JsonNode getRoutes() {
		ObjectNode root = this.mapper.createObjectNode();
		root.put("type", "list");
		ArrayNode routes = root.putArray("list");
        for (String name : InstanceManager.routeManagerInstance().getSystemNameList()) {
            routes.add(this.getRoute(name));
        }
        return root;
	}

	protected JsonNode getSensor(String name) {
		ObjectNode root = this.mapper.createObjectNode();
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
	
	protected JsonNode getSensors() {
		ObjectNode root = this.mapper.createObjectNode();
		root.put("type", "list");
		ArrayNode sensors = root.putArray("list");
        for (String name : InstanceManager.sensorManagerInstance().getSystemNameList()) {
            sensors.add(this.getSensor(name));
        }
        return root;
	}

	protected JsonNode getSignalHead(String name) {
		ObjectNode root = this.mapper.createObjectNode();
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
	
	protected JsonNode getSignalHeads() {
		ObjectNode root = this.mapper.createObjectNode();
		root.put("type", "list");
		ArrayNode signalHeads = root.putArray("list");
        for (String name : InstanceManager.signalHeadManagerInstance().getSystemNameList()) {
            signalHeads.add(this.getSignalHead(name));
        }
        return root;
	}

	protected JsonNode getTurnout(String name) {
		ObjectNode root = this.mapper.createObjectNode();
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
			data.put("message", "Unable to get turnout");
			log.error("Unable to get turnout." + e);
		}
		return root;
	}

	protected JsonNode getTurnouts() {
		ObjectNode root = this.mapper.createObjectNode();
		root.put("type", "list");
		ArrayNode turnouts = root.putArray("list");
        for (String name : InstanceManager.turnoutManagerInstance().getSystemNameList()) {
            turnouts.add(this.getTurnout(name));
        }
        return root;
	}

	protected JsonNode getUnknown() {
		ObjectNode root = this.mapper.createObjectNode();
		root.put("type", "error");
		ObjectNode data = root.putObject("data");
		data.put("code", "-1");
		data.put("message", "unknown type");
		return root;
	}
	
}