package jmri.web.servlet.simple;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.implementation.QuietShutDownTask;
import jmri.jmris.JmriConnection;
import jmri.jmris.simpleserver.SimpleLightServer;
import jmri.jmris.simpleserver.SimpleOperationsServer;
import jmri.jmris.simpleserver.SimplePowerServer;
import jmri.jmris.simpleserver.SimpleReporterServer;
import jmri.jmris.simpleserver.SimpleSensorServer;
import jmri.jmris.simpleserver.SimpleSignalHeadServer;
import jmri.jmris.simpleserver.SimpleTurnoutServer;
import jmri.util.FileUtil;
import jmri.util.node.NodeIdentity;
import jmri.web.server.WebServerPreferences;
import jmri.web.servlet.ServletUtil;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebSocket servlet for JMRI Simple service protocol.
 *
 * @author Randall Wood (c) 2016
 */
@WebServlet(name = "SimpleServlet",
        urlPatterns = {"/simple"})
@ServiceProvider(service = HttpServlet.class)
public class SimpleServlet extends WebSocketServlet {

    private static final Logger log = LoggerFactory.getLogger(SimpleServlet.class);

    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.register(SimpleWebSocket.class);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(ServletUtil.UTF8_TEXT_HTML); // NOI18N
        response.getWriter().print(String.format(request.getLocale(),
                FileUtil.readURL(FileUtil.findURL(Bundle.getMessage(request.getLocale(), "Simple.html"))),
                String.format(request.getLocale(),
                        Bundle.getMessage(request.getLocale(), "HtmlTitle"),
                        InstanceManager.getDefault(ServletUtil.class).getRailroadName(false),
                        Bundle.getMessage(request.getLocale(), "SimpleTitle")
                ),
                InstanceManager.getDefault(ServletUtil.class).getNavBar(request.getLocale(), request.getContextPath()),
                InstanceManager.getDefault(ServletUtil.class).getRailroadName(false),
                InstanceManager.getDefault(ServletUtil.class).getFooter(request.getLocale(), request.getContextPath())
        ));
    }

    @WebSocket
    public static class SimpleWebSocket {

        private JmriConnection connection;
        private QuietShutDownTask shutDownTask;
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

        @OnWebSocketConnect
        public void onOpen(Session cnctn) {
            this.connection = new JmriConnection(cnctn);
            this.shutDownTask = new QuietShutDownTask("Close simple web sockets") { // NOI18N
                @Override
                public boolean execute() {
                    SimpleWebSocket.this.connection.getSession().close();
                    return true;
                }
            };
            this.lightServer = new SimpleLightServer(this.connection);
            this.operationsServer = new SimpleOperationsServer(this.connection);
            this.powerServer = new SimplePowerServer(this.connection);
            this.reporterServer = new SimpleReporterServer(this.connection);
            this.sensorServer = new SimpleSensorServer(this.connection);
            this.signalHeadServer = new SimpleSignalHeadServer(this.connection);
            this.turnoutServer = new SimpleTurnoutServer(this.connection);
            try {
                this.connection.sendMessage("JMRI " + jmri.Version.name() + " \n");
                this.connection.sendMessage("RAILROAD " + InstanceManager.getDefault(WebServerPreferences.class).getRailroadName() + " \n");
                this.connection.sendMessage("NODE " + NodeIdentity.networkIdentity() + " \n");
            } catch (IOException e) {
                log.warn(e.getMessage(), e);
                this.connection.getSession().close();
            }
            InstanceManager.getDefault(jmri.ShutDownManager.class).register(this.shutDownTask);
        }

        @OnWebSocketError
        public void onError(Throwable thrwbl) {
            log.error(thrwbl.getMessage(), thrwbl);
        }

        @OnWebSocketMessage
        public void onMessage(String string) {
            log.debug("Received from client: {}", string);
            try {
                if (string.startsWith("POWER")) {
                    this.powerServer.parseStatus(string);
                    this.powerServer.sendStatus(InstanceManager.getDefault(jmri.PowerManager.class).getPower());
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
                    this.connection.getSession().close();
                    InstanceManager.getDefault(jmri.ShutDownManager.class).deregister(this.shutDownTask);
                }
            } catch (IOException ie) {
                log.warn(ie.getMessage(), ie);
                this.connection.getSession().close();
                InstanceManager.getDefault(jmri.ShutDownManager.class).deregister(this.shutDownTask);
            }
        }
    }
}
