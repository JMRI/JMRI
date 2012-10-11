// JsonWebSocketServlet.java
package jmri.web.servlet.json;

import java.io.IOException;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.implementation.QuietShutDownTask;
import jmri.jmris.JmriConnection;
import jmri.jmris.json.JsonLightServer;
import jmri.jmris.json.JsonOperationsServer;
import jmri.jmris.json.JsonPowerServer;
import jmri.jmris.json.JsonProgrammerServer;
import jmri.jmris.json.JsonReporterServer;
import jmri.jmris.json.JsonSensorServer;
import jmri.jmris.json.JsonTurnoutServer;
import jmri.jmris.json.JsonSignalHeadServer;
import jmri.web.server.WebServerManager;

import org.apache.log4j.Logger;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 *
 * @author rhwood
 */
public class JsonServlet extends WebSocketServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -671593634343578915L;
	private JsonServer server;
	private ObjectMapper mapper;
	
	private final Set<JsonWebSocket> sockets = new CopyOnWriteArraySet<JsonWebSocket>();
    private static ResourceBundle html = ResourceBundle.getBundle("jmri.web.server.Html");
    private static ResourceBundle wsHtml = ResourceBundle.getBundle("jmri.web.servlet.json.JsonHtml");
	private static Logger log = Logger.getLogger(JsonServlet.class);

	public JsonServlet() {
		super();
	}
	
	public void init() throws ServletException {
		super.init();
		this.server = new JsonServer();
		this.mapper = new ObjectMapper();
		InstanceManager.shutDownManagerInstance().register(new QuietShutDownTask("Close JSON web sockets") {
			@Override
			public boolean execute() {
				for (JsonWebSocket socket : sockets) {
					try {
						socket.wsConnection.sendMessage(socket.mapper.writeValueAsString(socket.mapper.createObjectNode().put("type", "goodbye")));
					} catch (Exception e) {
						log.warn("Unable to send goodbye while closing socket.\n" + e.getMessage());
					}
					socket.wsConnection.close();
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

        String[] rest = request.getPathInfo().split("/");
        String type = rest[1];
        if (type.length() != 0) {
            String name = (rest.length > 2) ? rest[2] : null;
        	JsonNode reply = null;
        	if (type.equals("memories")) {
        		reply = this.server.getMemories();
        	} else if (type.equals("metadata")) {
        		reply = this.server.getMetadata();
        	} else if (type.equals("panels")) {
        		reply = this.server.getPanels();
        	} else if (type.equals("power")) {
        		reply = this.server.getPower();
        	} else if (type.equals("railroad")) {
        		reply = this.server.getRailroad();
        	} else if (type.equals("roster")) {
        		reply = this.server.getRoster();
        	} else if (type.equals("routes")) {
        		reply = this.server.getRoutes();
        	} else if (type.equals("sensors")) {
        		reply = this.server.getSensors();
        	} else if (type.equals("signalHeads")) {
        		reply = this.server.getSignalHeads();
        	} else if (type.equals("turnouts")) {
        		reply = this.server.getTurnouts();
        	} else if (name != null) {
        		if (type.equals("memory")) {
        			reply = this.server.getMemory(name);
        		} else if (type.equals("reporter")) {
        			reply = this.server.getReporter(name);
        		} else if (type.equals("rosterEntry")) {
        			reply = this.server.getRosterEntry(name);
        		} else if (type.equals("route")) {
        			reply = this.server.getRoute(name);
        		} else if (type.equals("sensor")) {
        			reply = this.server.getSensor(name);
        		} else if (type.equals("signalHead")) {
        			reply = this.server.getSignalHead(name);
        		} else if (type.equals("turnout")) {
        			reply = this.server.getTurnout(name);
        		} else {
            		log.warn("Type \"" + type + "\" unknown.");
            		reply = this.server.getUnknown();
        		}
        	} else {
        		log.warn("Type \"" + type + "\" unknown.");
        		reply = this.server.getUnknown();
        	}
        	response.getWriter().write(this.mapper.writeValueAsString(reply));
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

	public class JsonWebSocket implements WebSocket.OnTextMessage {

		protected Connection wsConnection;
		protected JmriConnection jmriConnection;
		protected ObjectMapper mapper;
		private JsonLightServer lightServer;
		private JsonOperationsServer operationsServer;
		private JsonPowerServer powerServer;
		private JsonProgrammerServer programmerServer;
		private JsonReporterServer reporterServer;
		private JsonSensorServer sensorServer;
		private JsonSignalHeadServer signalHeadServer;
		private JsonTurnoutServer turnoutServer;

		public void sendMessage(String message) throws IOException {
			this.wsConnection.sendMessage(message);
		}

		@Override
		public void onOpen(Connection cnctn) {
			this.wsConnection = cnctn;
			this.jmriConnection = new JmriConnection(this.wsConnection);
			this.wsConnection.setMaxIdleTime(0); // default is 10 seconds (10000 milliseconds) set to 0 to disable timeouts
			this.mapper = new ObjectMapper();
			sockets.add(this);
			this.lightServer = new JsonLightServer(this.jmriConnection);
			this.operationsServer = new JsonOperationsServer(this.jmriConnection);
			this.powerServer = new JsonPowerServer(this.jmriConnection);
			this.programmerServer = new JsonProgrammerServer(this.jmriConnection);
			this.reporterServer = new JsonReporterServer(this.jmriConnection);
			this.sensorServer = new JsonSensorServer(this.jmriConnection);
			this.signalHeadServer = new JsonSignalHeadServer(this.jmriConnection);
			this.turnoutServer = new JsonTurnoutServer(this.jmriConnection);
			try {
				ObjectNode root = this.mapper.createObjectNode();
				root.put("type", "hello");
				ObjectNode data = root.putObject("data");
				data.put("JMRI", jmri.Version.name());
				data.put("heartbeat", this.wsConnection.getMaxIdleTime() * 0.8);
				data.put("railroad", WebServerManager.getWebServerPreferences().getRailRoadName());
				this.wsConnection.sendMessage(this.mapper.writeValueAsString(root));
			} catch (IOException e) {
				log.warn(e.getMessage(), e);
				this.wsConnection.close();
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
				String type = root.path("type").asText();
				JsonNode data = root.path("data");
				if (type.equals("ping")) {
					this.jmriConnection.sendMessage(this.mapper.writeValueAsString(this.mapper.createObjectNode().put("type", "pong")));
				} else if (type.equals("goodbye")) {
					this.jmriConnection.sendMessage(this.mapper.writeValueAsString(this.mapper.createObjectNode().put("type", "goodbye")));
					this.wsConnection.close();
				} else if (type.equals("list")) {
					JsonNode reply = null;
					String list = root.path("list").asText();
					if (list.equals("memories")) {
						reply = server.getMemories();
					} else if (list.equals("metadata")) {
						reply = server.getMetadata();
					} else if (list.equals("panels")) {
						reply = server.getPanels();
					} else if (list.equals("roster")) {
						reply = server.getRoster();
					} else if (list.equals("routes")) {
						reply = server.getRoutes();
					} else if (list.equals("sensors")) {
						reply = server.getSensors();
					} else if (list.equals("signalHeads")) {
						reply = server.getSignalHeads();
					} else if (list.equals("turnouts")) {
						reply = server.getTurnouts();
					} else {
						this.sendErrorMessage(0, "unknown type");
						return;
					}
					this.jmriConnection.sendMessage(this.mapper.writeValueAsString(reply));
				} else if (!data.isMissingNode()) {
					if (type.equals("light")) {
						this.lightServer.parseRequest(data);
					} else if (type.equals(JsonOperationsServer.OPERATIONS)) {
						this.operationsServer.parseRequest(data);
					} else if (type.equals("power")) {
						this.powerServer.parseRequest(data);
					} else if (type.equals("programmer")) {
						this.programmerServer.parseRequest(data);
					} else if (type.equals("sensor")) {
						this.sensorServer.parseRequest(data);
					} else if (type.equals("signalHead")) {
						this.signalHeadServer.parseRequest(data);
					} else if (type.equals("reporter")) {
						this.reporterServer.parseRequest(data);
					} else if (type.equals("rosterEntry")) {
						this.wsConnection.sendMessage(this.mapper.writeValueAsString(server.getRosterEntry(data.path("name").asText())));
					} else if (type.equals("turnout")) {
						this.turnoutServer.parseRequest(data);
					} else {
						this.sendErrorMessage(0, "unknown type");
					}
				} else {
					this.sendErrorMessage(0, "expected message data");
				}
			} catch (JsonProcessingException pe) {
				log.warn("Exception processing \"" + string + "\"\n" + pe.getMessage());
				this.sendErrorMessage(0, "unable to process");
			} catch (JmriException je) {
				this.sendErrorMessage(0, "unsupported operation");
			} catch (IOException ie) {
				log.warn(ie.getMessage(), ie);
				this.wsConnection.close();
				sockets.remove(this);
			}
		}

		private void sendErrorMessage(int code, String message) {
			ObjectNode root = this.mapper.createObjectNode();
			root.put("type", "error");
			ObjectNode data = root.putObject("error");
			data.put("code", code);
			data.put("message", message);
			try {
				this.wsConnection.sendMessage(this.mapper.writeValueAsString(root));
			} catch (IOException ie) {
				log.warn(ie.getMessage(), ie);
				this.wsConnection.close();
				sockets.remove(this);
			} catch (Exception e) {
				log.warn("Exception processing exception\n" + e.getMessage());
			}
		}
	}
}
