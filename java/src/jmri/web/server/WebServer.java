package jmri.web.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import jmri.InstanceManager;
import jmri.ShutDownTask;
import jmri.implementation.QuietShutDownTask;
import static jmri.jmris.json.JSON.JSON;
import static jmri.jmris.json.JSON.JSON_PROTOCOL_VERSION;
import jmri.util.FileUtil;
import jmri.util.zeroconf.ZeroConfService;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An HTTP server that handles requests for HTTPServlets.
 *
 * @author Bob Jacobsen Copyright 2005, 2006
 * @author Randall Wood Copyright 2012
 * @version $Revision$
 */
public final class WebServer implements LifeCycle.Listener {

    protected Server server;
    protected ZeroConfService zeroConfService = null;
    private WebServerPreferences preferences = null;
    protected ShutDownTask shutDownTask = null;
    static Logger log = LoggerFactory.getLogger(WebServer.class.getName());

    protected WebServer() {
        preferences = WebServerManager.getWebServerPreferences();
        shutDownTask = new QuietShutDownTask("Stop Web Server") { // NOI18N
            @Override
            public boolean execute() {
                try {
                    WebServerManager.getWebServer().stop();
                } catch (Exception ex) {
                    log.warn("Error shutting down WebServer: " + ex);
                    if (log.isDebugEnabled()) {
                        log.debug("Details follow: ", ex);
                    }
                }
                return true;
            }
        };
    }

    public void start() {
        if (server == null) {
            server = new Server();
            SelectChannelConnector connector = new SelectChannelConnector();
            connector.setMaxIdleTime(5 * 60 * 1000); // 5 minutes
            connector.setSoLingerTime(-1);
            connector.setPort(preferences.getPort());
            server.setThreadPool(new ExecutorThreadPool(10, 1000, 10, TimeUnit.SECONDS));
            server.setConnectors(new Connector[]{connector});

            ContextHandlerCollection contexts = new ContextHandlerCollection();
            Properties services = new Properties();
            Properties filePaths = new Properties();
            try {
                InputStream in;
                in = this.getClass().getResourceAsStream("Services.properties"); // NOI18N
                services.load(in);
                in.close();
                in = this.getClass().getResourceAsStream("FilePaths.properties"); // NOI18N
                filePaths.load(in);
                in.close();
            } catch (IOException e) {
                log.error(e.getMessage());
            }
            for (String path : services.stringPropertyNames()) {
                ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SECURITY);
                context.setContextPath(path);
                if (services.getProperty(path).equals("fileHandler")) { // NOI18N
                    ServletHolder holder = context.addServlet(DefaultServlet.class, "/*"); // NOI18N
                    holder.setInitParameter("resourceBase", FileUtil.getAbsoluteFilename(filePaths.getProperty(path))); // NOI18N
                    holder.setInitParameter("stylesheet", FileUtil.getAbsoluteFilename(filePaths.getProperty("/css")) + "/miniServer.css"); // NOI18N
                } else {
                    context.addServlet(services.getProperty(path), "/*"); // NOI18N
                }
                contexts.addHandler(context);
            }
            server.setHandler(contexts);

            server.addLifeCycleListener(this);

            Thread serverThread = new ServerThread(server);
            serverThread.start();

        }

    }

    public void stop() throws Exception {
        server.stop();
    }

    public static String getLocalAddress() {
        InetAddress hostAddress = null;
        try {
            hostAddress = Inet4Address.getLocalHost();
        } catch (java.net.UnknownHostException e) {
        }
        if (hostAddress == null || hostAddress.isLoopbackAddress()) {
            hostAddress = ZeroConfService.hostAddress();  //lookup from interfaces
        }
        if (hostAddress == null) {
            return WebServer.getString("MessageAddressNotFound");
        } else {
            return hostAddress.getHostAddress().toString();
        }
    }

    /**
     * Get the public URI for a portable path. This method returns public URIs
     * for only some portable paths, and does not check that the portable path
     * is actually sane. Note that this refuses to return portable paths that
     * are outside of program: and preference:
     *
     * @param path
     * @return The servable URI or null
     * @see jmri.util.FileUtil#getPortableFilename(java.io.File)
     */
    public static String URIforPortablePath(String path) {
        if (path.startsWith(FileUtil.PREFERENCES)) {
            return path.replaceFirst(FileUtil.PREFERENCES, "/prefs/"); // NOI18N
        } else if (path.startsWith(FileUtil.PROGRAM)) {
            return path.replaceFirst(FileUtil.PROGRAM, "/dist/"); // NOI18N
        } else {
            return null;
        }
    }

    @SuppressWarnings("FinalStaticMethod")
    public static final String getString(String message) {
        return ResourceBundle.getBundle("jmri.web.server.Bundle").getString(message);
    }

    public int getPort() {
        return preferences.getPort();
    }

    @Override
    public void lifeCycleStarting(LifeCycle lc) {
        if (InstanceManager.shutDownManagerInstance() != null) {
            InstanceManager.shutDownManagerInstance().register(shutDownTask);
        }
        log.info("Starting Web Server on port " + preferences.getPort());
    }

    @Override
    public void lifeCycleStarted(LifeCycle lc) {
        HashMap<String, String> properties = new HashMap<String, String>();
        properties.put("path", "/index.html"); // NOI18N
        properties.put(JSON, JSON_PROTOCOL_VERSION);
        zeroConfService = ZeroConfService.create("_http._tcp.local.", preferences.getPort(), properties); // NOI18N
        zeroConfService.publish();
        log.info("Starting ZeroConfService _http._tcp.local for Web Server");
        log.debug("Web Server finished starting");
    }

    @Override
    public void lifeCycleFailure(LifeCycle lc, Throwable thrwbl) {
        log.warn("Web Server failed", thrwbl);
    }

    @Override
    public void lifeCycleStopping(LifeCycle lc) {
        if (zeroConfService != null) {
            zeroConfService.stop();
        }
        log.info("Stopping Web Server");
    }

    @Override
    public void lifeCycleStopped(LifeCycle lc) {
        if (InstanceManager.shutDownManagerInstance() != null) {
            InstanceManager.shutDownManagerInstance().deregister(shutDownTask);
        }
        log.debug("Web Server stopped");
    }

    static private class ServerThread extends Thread {

        private Server server;

        public ServerThread(Server server) {
            this.server = server;
        }

        @Override
        public void run() {
            try {
                server.start();
                server.join();
            } catch (Exception ex) {
                log.error("Exception starting Web Server: " + ex);
            }
        }
    }
}
