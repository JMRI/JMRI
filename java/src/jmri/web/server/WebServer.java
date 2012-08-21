package jmri.web.server;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.ResourceBundle;
import jmri.InstanceManager;
import jmri.ShutDownTask;
import jmri.implementation.QuietShutDownTask;
import jmri.util.FileUtil;
import jmri.util.zeroconf.ZeroConfService;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.LifeCycle;

/**
 * An HTTP server that handles requests for HTTPServlets.
 *
 * @author Bob Jacobsen Copyright 2005, 2006
 * @author Randall Wood Copyright 2012
 * @version $Revision$
 */
public final class WebServer implements LifeCycle.Listener {

    static ResourceBundle services = ResourceBundle.getBundle("jmri.web.server.Services");
    static ResourceBundle filePaths = ResourceBundle.getBundle("jmri.web.server.FilePaths");
    protected Server server;
    protected ZeroConfService zeroConfService = null;
    private WebServerPreferences preferences = null;
    protected ShutDownTask shutDownTask = null;
    static Logger log = Logger.getLogger(WebServer.class.getName());

    protected WebServer() {
        preferences = WebServerManager.getWebServerPreferences();
        shutDownTask = new QuietShutDownTask("Stop Web Server") {

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
            server = new Server(preferences.getPort());

            ContextHandlerCollection contexts = new ContextHandlerCollection();


            for (String path : services.keySet()) {
                ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SECURITY);
                context.setContextPath(path);
                if (services.getString(path).equals("fileHandler")) {
                    ServletHolder holder = context.addServlet(DefaultServlet.class, "/*");
                    holder.setInitParameter("resourceBase", FileUtil.getAbsoluteFilename(filePaths.getString(path)));
                    holder.setInitParameter("stylesheet", FileUtil.getAbsoluteFilename(filePaths.getString("/css")) + "/miniServer.css");
                } else {
                    context.addServlet(services.getString(path), "/*");
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
            return "(local host not found)";
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
            return path.replaceFirst(FileUtil.PREFERENCES, "/prefs/");
        } else if (path.startsWith(FileUtil.PROGRAM)) {
            return path.replaceFirst(FileUtil.PROGRAM, "/dist/");
        } else {
            return null;
        }
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
        zeroConfService = ZeroConfService.create("_http._tcp.local.", preferences.getPort(), new HashMap<String, String>() {

            {
                put("path", "/index.html");
            }
        });
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
