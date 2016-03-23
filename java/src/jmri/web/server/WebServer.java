package jmri.web.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;
import jmri.InstanceManager;
import jmri.ShutDownTask;
import jmri.implementation.QuietShutDownTask;
import jmri.jmris.json.JSON;
import jmri.util.FileUtil;
import jmri.util.zeroconf.ZeroConfService;
import jmri.web.servlet.directory.DirectoryHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
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

    private Server server;
    private ZeroConfService zeroConfService = null;
    private WebServerPreferences preferences = null;
    private ShutDownTask shutDownTask = null;
    private final static Logger log = LoggerFactory.getLogger(WebServer.class.getName());

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
            QueuedThreadPool threadPool = new QueuedThreadPool();
            threadPool.setName("WebServer");
            threadPool.setMaxThreads(1000);
            server = new Server(threadPool);
            ServerConnector connector = new ServerConnector(server);
            connector.setIdleTimeout(5 * 60 * 1000); // 5 minutes
            connector.setSoLingerTime(-1);
            connector.setPort(preferences.getPort());
            server.setConnectors(new Connector[]{connector});

            ContextHandlerCollection contexts = new ContextHandlerCollection();
            Properties services = new Properties();
            Properties filePaths = new Properties();
            try (InputStream in = this.getClass().getResourceAsStream("Services.properties")) { // NOI18N
                services.load(in);
                in.close();
            } catch (IOException ex) {
                log.error(ex.getMessage());
            }
            try (InputStream in = this.getClass().getResourceAsStream("FilePaths.properties")) { // NOI18N
                filePaths.load(in);
                in.close();
            } catch (IOException ex) {
                log.error(ex.getMessage());
            }
            for (String path : services.stringPropertyNames()) {
                ServletContextHandler servletContext = new ServletContextHandler(ServletContextHandler.NO_SECURITY);
                servletContext.setContextPath(path);
                if (services.getProperty(path).equals("fileHandler")) { // NOI18N
                    if (filePaths.getProperty(path).startsWith("program:web")) { // NOI18N
                        log.debug("Setting up handler chain for {}", path);
                        // make it possible to override anything under program:web/ with an identical path under preference:web/
                        ResourceHandler preferenceHandler = new DirectoryHandler();
                        preferenceHandler.setDirectoriesListed(true);
                        preferenceHandler.setWelcomeFiles(new String[]{"index.html"}); // NOI18N
                        preferenceHandler.setResourceBase(FileUtil.getAbsoluteFilename(filePaths.getProperty(path).replace("program:", "preference:"))); // NOI18N
                        ResourceHandler programHandler = new DirectoryHandler();
                        programHandler.setDirectoriesListed(true);
                        programHandler.setWelcomeFiles(new String[]{"index.html"}); // NOI18N
                        programHandler.setResourceBase(FileUtil.getAbsoluteFilename(filePaths.getProperty(path)));
                        HandlerList handlers = new HandlerList();
                        handlers.setHandlers(new Handler[]{preferenceHandler, programHandler, new DefaultHandler()});
                        ContextHandler handlerContext = new ContextHandler();
                        handlerContext.setContextPath(path);
                        handlerContext.setHandler(handlers);
                        contexts.addHandler(handlerContext);
                        continue;
                    }
                    ResourceHandler handler = new DirectoryHandler();
                    handler.setDirectoriesListed(true);
                    handler.setWelcomeFiles(new String[]{"index.html"});
                    handler.setResourceBase(FileUtil.getAbsoluteFilename(filePaths.getProperty(path)));
                    HandlerList handlers = new HandlerList();
                    handlers.setHandlers(new Handler[]{handler, new DefaultHandler()});
                    ContextHandler handlerContext = new ContextHandler();
                    handlerContext.setContextPath(path);
                    handlerContext.setHandler(handlers);
                    contexts.addHandler(handlerContext);
                    continue;
                } else if (services.getProperty(path).equals("redirectHandler")) { // NOI18N
                    servletContext.addServlet("jmri.web.servlet.RedirectionServlet", ""); // NOI18N
                } else if (services.getProperty(path).startsWith("jmri.web.servlet.config.ConfigServlet") && !this.preferences.allowRemoteConfig()) { // NOI18N
                    // if not allowRemoteConfig, use DenialServlet for any path configured to use ConfigServlet
                    servletContext.addServlet("jmri.web.servlet.DenialServlet", "/*"); // NOI18N
                } else {
                    servletContext.addServlet(services.getProperty(path), "/*"); // NOI18N
                }
                contexts.addHandler(servletContext);
            }
            server.setHandler(contexts);

            server.addLifeCycleListener(this);

            Thread serverThread = new ServerThread(server);
            serverThread.setName("WebServer"); // NOI18N
            serverThread.start();

        }

    }

    public void stop() throws Exception {
        server.stop();
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
        HashMap<String, String> properties = new HashMap<>();
        properties.put("path", "/"); // NOI18N
        properties.put(JSON.JSON, JSON.JSON_PROTOCOL_VERSION);
        log.info("Starting ZeroConfService _http._tcp.local for Web Server with properties {}", properties);
        zeroConfService = ZeroConfService.create("_http._tcp.local.", preferences.getPort(), properties); // NOI18N
        zeroConfService.publish();
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

        private final Server server;

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
