// JsonWebSocketServlet.java
package jmri.web.servlet.json;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.Route;
import jmri.RouteManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.implementation.QuietShutDownTask;
import jmri.jmris.json.JsonPowerServer;
import jmri.jmris.simpleserver.SimpleLightServer;
import jmri.jmris.simpleserver.SimpleOperationsServer;
import jmri.jmris.simpleserver.SimpleReporterServer;
import jmri.jmris.simpleserver.SimpleSensorServer;
import jmri.jmris.simpleserver.SimpleSignalHeadServer;
import jmri.jmris.simpleserver.SimpleTurnoutServer;

import org.apache.log4j.Logger;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;
import org.jdom.Element;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 *
 * @author rhwood
 */
public class JsonServlet extends WebSocketServlet {

	protected ObjectMapper mapper;
	
	private final Set<JsonWebSocket> sockets = new CopyOnWriteArraySet<JsonWebSocket>();
    private static ResourceBundle html = ResourceBundle.getBundle("jmri.web.server.Html");
    private static ResourceBundle wsHtml = ResourceBundle.getBundle("jmri.web.servlet.json.JsonHtml");
	private static Logger log = Logger.getLogger(JsonServlet.class);

	public JsonServlet() {
		super();
		this.mapper = new ObjectMapper();
		InstanceManager.shutDownManagerInstance().register(new QuietShutDownTask("Close JSON web sockets") {
			@Override
			public boolean execute() {
				for (JsonWebSocket socket : sockets) {
					try {
						socket.connection.sendMessage(socket.mapper.writeValueAsString(socket.mapper.createObjectNode().put("type", "goodbye")));
					} catch (Exception e) {
						log.warn("Unable to send goodbye while closing socket.\n" + e.getMessage());
					}
					socket.connection.close();
				}
				return true;
			}

			@Override
			public String name() {
				return "CloseJsonWebSockets";
			}
		});
	}

	@Override
	public WebSocket doWebSocketConnect(HttpServletRequest hsr, String string) {
		return new JsonWebSocket();
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Date now = new Date();
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/html");
        response.setHeader("Connection", "Keep-Alive");
        response.setDateHeader("Date", now.getTime());
        response.setDateHeader("Last-Modified", now.getTime());
        response.setDateHeader("Expires", now.getTime());

        String rest = request.getPathInfo().substring(request.getPathInfo().indexOf("/") + 1);
        if (rest.length() != 0) {
        	if (rest.equals("memories")) {
        		this.doMemories(request, response);
        	} else if (rest.equals("routes")) {
        		this.doRoutes(request, response);
        	} else if (rest.equals("sensors")) {
        		this.doSensors(request, response);
        	} else if (rest.equals("turnouts")) {
        		this.doTurnouts(request, response);
        	}
        } else {
            response.getWriter().println(String.format(html.getString("HeadFormat"),
                    html.getString("HTML5DocType"),
                    "JSON Console",
                    JsonServlet.class.getSimpleName(),
                    wsHtml.getString("HeadAdditions")));
            response.getWriter().println(wsHtml.getString("BodyContent"));
            response.getWriter().println(String.format(html.getString("TailFormat"), "", ""));
        }
	}

	protected void doMemories(HttpServletRequest request, HttpServletResponse response) throws IOException {
		ObjectNode root = this.mapper.createObjectNode();
		root.put("type", "list");
		root.put("list", "memory");
		ArrayNode memories = root.putArray("memory");
        MemoryManager m = InstanceManager.memoryManagerInstance();
        List<String> names = m.getSystemNameList();
        for (String name : names) {
            Memory t = m.getMemory(name);
            ObjectNode memory = this.mapper.createObjectNode();
            memory.put("name", name);
            memory.put("userName", t.getUserName());
            memory.put("comment", t.getComment());
            memory.put("value", t.getValue().toString());
            memories.add(memory);
        }
        response.getWriter().write(this.mapper.writeValueAsString(root));
	}

	protected void doRoutes(HttpServletRequest request, HttpServletResponse response) throws IOException {
		ObjectNode root = this.mapper.createObjectNode();
		root.put("type", "list");
		root.put("list", "route");
		ArrayNode routes = root.putArray("route");
        RouteManager m = InstanceManager.routeManagerInstance();
        SensorManager s = InstanceManager.sensorManagerInstance();
        List<String> names = m.getSystemNameList();
        for (String name : names) {
            Route t = m.getRoute(name);
            ObjectNode route = this.mapper.createObjectNode();
            route.put("name", name);
            route.put("userName", t.getUserName());
            route.put("comment", t.getComment());
            route.put("state", (s.provideSensor(t.getTurnoutsAlignedSensor()) != null) ? (s.provideSensor(t.getTurnoutsAlignedSensor())).getKnownState() : 0);
            routes.add(route);
        }
        response.getWriter().write(this.mapper.writeValueAsString(root));
	}

	protected void doSensors(HttpServletRequest request, HttpServletResponse response) throws IOException {
		ObjectNode root = this.mapper.createObjectNode();
		root.put("type", "list");
		root.put("list", "sensor");
		ArrayNode sensors = root.putArray("sensor");
        SensorManager m = InstanceManager.sensorManagerInstance();
        List<String> names = m.getSystemNameList();
        for (String name : names) {
            Sensor t = m.getSensor(name);
            ObjectNode sensor = this.mapper.createObjectNode();
            sensor.put("name", name);
            sensor.put("userName", t.getUserName());
            sensor.put("comment", t.getComment());
            sensor.put("inverted", t.getInverted());
            sensor.put("state", t.getKnownState());
            sensors.add(sensor);
        }            
        response.getWriter().write(this.mapper.writeValueAsString(root));
	}

	protected void doTurnouts(HttpServletRequest request, HttpServletResponse response) throws IOException {
		ObjectNode root = this.mapper.createObjectNode();
		root.put("type", "list");
		root.put("list", "turnout");
		ArrayNode turnouts = root.putArray("turnout");
        TurnoutManager m = InstanceManager.turnoutManagerInstance();
        List<String> names = m.getSystemNameList();
        for (String name : names) {
            Turnout t = m.getTurnout(name);
            ObjectNode turnout = this.mapper.createObjectNode();
            turnout.put("name", name);
            turnout.put("userName", t.getUserName());
            turnout.put("comment", t.getComment());
            turnout.put("inverted", t.getInverted());
            turnout.put("state", t.getKnownState());
            turnouts.add(turnout);
        }
        response.getWriter().write(this.mapper.writeValueAsString(root));
	}

	public class JsonWebSocket implements WebSocket.OnTextMessage {

		protected Connection connection;
		protected ObjectMapper mapper;
		private SimpleLightServer lightServer;
		private SimpleOperationsServer operationsServer;
		private JsonPowerServer powerServer;
		private SimpleReporterServer reporterServer;
		private SimpleSensorServer sensorServer;
		private SimpleSignalHeadServer signalHeadServer;
		private SimpleTurnoutServer turnoutServer;

		public void sendMessage(String message) throws IOException {
			this.connection.sendMessage(message);
		}

		@Override
		public void onOpen(Connection cnctn) {
			this.connection = cnctn;
			this.connection.setMaxIdleTime(10000); // default is 10 seconds (10000 milliseconds) set to 0 to disable timeouts
			this.mapper = new ObjectMapper();
			sockets.add(this);
			this.lightServer = new SimpleLightServer(this.connection);
			this.operationsServer = new SimpleOperationsServer(this.connection);
			this.powerServer = new JsonPowerServer(this.connection);
			this.reporterServer = new SimpleReporterServer(this.connection);
			this.sensorServer = new SimpleSensorServer(this.connection);
			this.signalHeadServer = new SimpleSignalHeadServer(this.connection);
			this.turnoutServer = new SimpleTurnoutServer(this.connection);
			try {
				ObjectNode root = this.mapper.createObjectNode();
				root.put("type", "hello");
				ObjectNode data = root.putObject("data");
				data.put("JMRI", jmri.Version.name());
				data.put("heartbeat", this.connection.getMaxIdleTime() * 0.8);
				this.connection.sendMessage(this.mapper.writeValueAsString(root));
			} catch (IOException e) {
				log.warn(e.getMessage(), e);
				this.connection.close();
				sockets.remove(this);
			}
		}

		@Override
		public void onClose(int i, String string) {
			sockets.remove(this);
		}

		@Override
		public void onMessage(String string) {
			if (log.isDebugEnabled()) {
				log.debug("Received from client: " + string);
			}
			try {
				JsonNode root = this.mapper.readTree(string);
				String type = root.path("type").asText().toLowerCase().trim(); // be forgiving?
				if (type.equals("ping")) {
					this.connection.sendMessage(this.mapper.writeValueAsString(this.mapper.createObjectNode().put("type", "pong")));
				} else if (type.equals("goodbye")) {
					this.connection.sendMessage(this.mapper.writeValueAsString(this.mapper.createObjectNode().put("type", "goodbye")));
					this.connection.close();
				} else if (type.equals("power")) {
					this.powerServer.parseStatus(string);
				} else if (type.equals("TURNOUT")) {
					this.turnoutServer.parseStatus(string);
				} else if (type.equals("LIGHT")) {
					this.lightServer.parseStatus(string);
				} else if (type.equals("SENSOR")) {
					this.sensorServer.parseStatus(string);
				} else if (type.equals("SIGNALHEAD")) {
					this.signalHeadServer.parseStatus(string);
				} else if (type.equals("REPORTER")) {
					this.reporterServer.parseStatus(string);
				} else if (type.equals(SimpleOperationsServer.OPERATIONS)) {
					this.operationsServer.parseStatus(string);
				} else {
					ObjectNode error = this.mapper.createObjectNode();
					error.put("type", "error");
					ObjectNode data = error.putObject("data");
					data.put("code", 0);
					data.put("message", "unknown type");
					this.connection.sendMessage(this.mapper.writeValueAsString(error));
				}
			} catch (JsonProcessingException pe) {
				log.warn("Exception processing \"" + string + "\"\n" + pe.getMessage());
				ObjectNode root = this.mapper.createObjectNode();
				root.put("type", "error");
				ObjectNode data = root.putObject("data");
				data.put("code", 0);
				data.put("message", "unable to process");
				try {
					this.connection.sendMessage(this.mapper.writeValueAsString(root));
				} catch (IOException ie) {
					log.warn(ie.getMessage(), ie);
					this.connection.close();
					sockets.remove(this);
				} catch (Exception e) {
					log.warn("Exception processing exception\n" + e.getMessage());
				}
			} catch (JmriException je) {
				ObjectNode root = this.mapper.createObjectNode();
				root.put("type", "error");
				ObjectNode data = root.putObject("data");
				data.put("code", 0);
				data.put("message", "unsupported operation");
				try {
					this.connection.sendMessage(this.mapper.writeValueAsString(root));
				} catch (IOException ie) {
					log.warn(ie.getMessage(), ie);
					this.connection.close();
					sockets.remove(this);
				} catch (Exception e) {
					log.warn("Exception processing exception\n" + e.getMessage());
				}
			} catch (IOException ie) {
				log.warn(ie.getMessage(), ie);
				this.connection.close();
				sockets.remove(this);
			}
		}

	}
}
