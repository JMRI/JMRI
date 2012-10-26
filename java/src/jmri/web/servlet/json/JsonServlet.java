// JsonServlet.java
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
import jmri.implementation.QuietShutDownTask;
import jmri.jmris.JmriConnection;
import jmri.jmris.json.JsonClientHandler;
import jmri.jmris.json.JsonLister;
import jmri.jmris.json.JsonServerManager;

import org.apache.log4j.Logger;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author rhwood
 */
public class JsonServlet extends WebSocketServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -671593634343578915L;
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

	/**
	 * handle HTTP get requests for json data
	 *   examples: /json/sensor/IS22 (return data for sensor with systemname "IS22"
	 *             /json/sensors (returns list of all sensors known to JMRI)
	 *   example responses: 
	 *     {"type":"sensor","data":{"name":"IS22","userName":"FarEast","comment":null,"inverted":false,"state":4}}
	 *     {"type":"list","list":[{"type":"sensor","data":{"name":"IS22","userName":"FarEast","comment":null,"inverted":false,"state":4}}]}
	 *   note that data will vary for each type
	 */
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
        String type = (rest.length > 1) ? rest[1] : null;
        if (type != null) {
            String name = (rest.length > 2) ? rest[2] : null;
        	JsonNode reply = null;
        	if (type.equals("cars")) {
        		reply = JsonLister.getCars();
        	} else if (type.equals("engines")) {
        		reply = JsonLister.getEngines();
        	} else if (type.equals("lights")) {
        		reply = JsonLister.getLights();
        	} else if (type.equals("locations")) {
        		reply = JsonLister.getLocations();
        	} else if (type.equals("memories")) {
        		reply = JsonLister.getMemories();
        	} else if (type.equals("metadata")) {
        		reply = JsonLister.getMetadata();
        	} else if (type.equals("panels")) {
        		reply = JsonLister.getPanels();
        	} else if (type.equals("power")) {
        		reply = JsonLister.getPower();
        	} else if (type.equals("railroad")) {
        		reply = JsonLister.getRailroad();
        	} else if (type.equals("roster")) {
        		reply = JsonLister.getRoster();
        	} else if (type.equals("routes")) {
        		reply = JsonLister.getRoutes();
        	} else if (type.equals("sensors")) {
        		reply = JsonLister.getSensors();
        	} else if (type.equals("signalHeads")) {
        		reply = JsonLister.getSignalHeads();
        	} else if (type.equals("trains")) {
        		reply = JsonLister.getTrains();
        	} else if (type.equals("turnouts")) {
        		reply = JsonLister.getTurnouts();
        	} else if (name != null) {
        		if (type.equals("car")) {
        			reply = JsonLister.getCar(name);
        		} else if (type.equals("engine")) {
        			reply = JsonLister.getEngine(name);
        		} else if (type.equals("light")) {
        			reply = JsonLister.getLight(name);
        		} else if (type.equals("location")) {
        			reply = JsonLister.getLocation(name);
        		} else if (type.equals("memory")) {
        			reply = JsonLister.getMemory(name);
        		} else if (type.equals("reporter")) {
        			reply = JsonLister.getReporter(name);
        		} else if (type.equals("rosterEntry")) {
        			reply = JsonLister.getRosterEntry(name);
        		} else if (type.equals("route")) {
        			reply = JsonLister.getRoute(name);
        		} else if (type.equals("sensor")) {
        			reply = JsonLister.getSensor(name);
        		} else if (type.equals("signalHead")) {
        			reply = JsonLister.getSignalHead(name);
        		} else if (type.equals("train")) {
        			reply = JsonLister.getTrain(name);
        		} else if (type.equals("turnout")) {
        			reply = JsonLister.getTurnout(name);
        		} else {
            		log.warn("Type \"" + type + "\" unknown.");
            		reply = JsonLister.getUnknown();
        		}
        	} else {
        		log.warn("Type \"" + type + "\" unknown.");
        		reply = JsonLister.getUnknown();
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
		protected JsonClientHandler handler;

		public void sendMessage(String message) throws IOException {
			this.wsConnection.sendMessage(message);
		}

		@Override
		public void onOpen(Connection cnctn) {
			this.wsConnection = cnctn;
			this.jmriConnection = new JmriConnection(this.wsConnection);
			this.wsConnection.setMaxIdleTime(JsonServerManager.getJsonServerPreferences().getHeartbeatInterval());
			this.mapper = new ObjectMapper();
			this.handler = new JsonClientHandler(this.jmriConnection);
			sockets.add(this);
			try {
				this.handler.sendHello(this.wsConnection.getMaxIdleTime());
			} catch (IOException e) {
				log.warn(e.getMessage(), e);
				this.wsConnection.close();
				sockets.remove(this);
			}
		}

		@Override
		public void onClose(int i, String string) {
			this.handler.onClose();
			sockets.remove(this);
		}

		@Override
		public void onMessage(String string) {
			try {
				this.handler.onMessage(string);
			} catch (IOException e) {
				log.warn(e.getMessage(), e);
				this.wsConnection.close();
				sockets.remove(this);
			}
		}

	}
}
