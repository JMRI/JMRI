// SimpleWebSocketServlet.java
package jmri.web.servlet.simple;

import java.io.IOException;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.implementation.QuietShutDownTask;
import jmri.jmris.simpleserver.SimpleLightServer;
import jmri.jmris.simpleserver.SimpleOperationsServer;
import jmri.jmris.simpleserver.SimplePowerServer;
import jmri.jmris.simpleserver.SimpleReporterServer;
import jmri.jmris.simpleserver.SimpleSensorServer;
import jmri.jmris.simpleserver.SimpleSignalHeadServer;
import jmri.jmris.simpleserver.SimpleTurnoutServer;
import jmri.util.node.NodeIdentity;
import jmri.web.server.WebServerManager;
import org.eclipse.jetty.websocket.WebSocket;
import org.eclipse.jetty.websocket.WebSocketServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rhwood
 */
public class SimpleServlet extends WebSocketServlet {

    private final Set<SimpleWebSocket> sockets = new CopyOnWriteArraySet<SimpleWebSocket>();
    private static final ResourceBundle html = ResourceBundle.getBundle("jmri.web.server.Html");
    private static final ResourceBundle wsHtml = ResourceBundle.getBundle("jmri.web.servlet.simple.Simple");
    private static final Logger log = LoggerFactory.getLogger(SimpleServlet.class);

    public SimpleServlet() {
        super();
        InstanceManager.shutDownManagerInstance().register(new QuietShutDownTask("Close simple web sockets") {
            @Override
            public boolean execute() {
                for (SimpleWebSocket socket : sockets) {
                    socket.connection.close();
                }
                return true;
            }

            @Override
            public String name() {
                return "CloseSimpleWebSockets";
            }
        });
    }

    @Override
    public WebSocket doWebSocketConnect(HttpServletRequest hsr, String string) {
        return new SimpleWebSocket();
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

        response.getWriter().println(String.format(html.getString("HeadFormat"),
                html.getString("HTML5DocType"),
                "JMRI Simple Network Console",
                SimpleServlet.class.getSimpleName(),
                wsHtml.getString("HeadAdditions")));
        response.getWriter().println(wsHtml.getString("BodyContent"));
        response.getWriter().println(String.format(html.getString("TailFormat"), "", ""));
    }

    public class SimpleWebSocket implements WebSocket.OnTextMessage {

        private Connection connection;
        private SimpleLightServer lightServer;
        private SimpleOperationsServer operationsServer;
        private SimplePowerServer powerServer;
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
            sockets.add(this);
            this.lightServer = new SimpleLightServer(this.connection);
            this.operationsServer = new SimpleOperationsServer(this.connection);
            this.powerServer = new SimplePowerServer(this.connection);
            this.reporterServer = new SimpleReporterServer(this.connection);
            this.sensorServer = new SimpleSensorServer(this.connection);
            this.signalHeadServer = new SimpleSignalHeadServer(this.connection);
            this.turnoutServer = new SimpleTurnoutServer(this.connection);
            try {
                this.connection.sendMessage("JMRI " + jmri.Version.name() + " \n");
                this.connection.sendMessage("RAILROAD " + WebServerManager.getWebServerPreferences().getRailRoadName() + " \n");
                this.connection.sendMessage("NODE " + NodeIdentity.identity() + " \n");
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
                log.debug("Received from client: {}", string);
            }
            try {
                if (string.startsWith("POWER")) {
                    this.powerServer.parseStatus(string);
                    this.powerServer.sendStatus(InstanceManager.powerManagerInstance().getPower());
                } else if (string.startsWith("TURNOUT")) {
                    this.turnoutServer.parseStatus(string);
                } else if (string.startsWith("LIGHT")) {
                    this.lightServer.parseStatus(string);
                } else if (string.startsWith("SENSOR")) {
                    this.sensorServer.parseStatus(string);
                } else if (string.startsWith("SIGNALHEAD")) {
                    this.signalHeadServer.parseStatus(string);
                } else if (string.startsWith("REPORTER")) {
                    this.reporterServer.parseStatus(string);
                } else if (string.startsWith(SimpleOperationsServer.OPERATIONS)) {
                    this.operationsServer.parseStatus(string);
                } else {
                    this.connection.sendMessage("Unknown Command " + string + "\n");
                }
            } catch (JmriException je) {
                try {
                    this.connection.sendMessage("not supported\n");
                } catch (IOException ie) {
                    log.warn(ie.getMessage(), ie);
                    this.connection.close();
                    sockets.remove(this);
                }
            } catch (IOException ie) {
                log.warn(ie.getMessage(), ie);
                this.connection.close();
                sockets.remove(this);
            }
        }
    }
}
