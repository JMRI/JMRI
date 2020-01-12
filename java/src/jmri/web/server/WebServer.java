package jmri.web.server;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ServiceLoader;
import javax.annotation.Nonnull;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import jmri.InstanceManager;
import jmri.ShutDownManager;
import jmri.ShutDownTask;
import jmri.implementation.QuietShutDownTask;
import jmri.server.json.JSON;
import jmri.server.web.spi.WebServerConfiguration;
import jmri.util.FileUtil;
import jmri.util.zeroconf.ZeroConfService;
import jmri.web.servlet.DenialServlet;
import jmri.web.servlet.RedirectionServlet;
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
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An HTTP server that handles requests for HTTPServlets.
 * <p>
 * This server loads HttpServlets registered as
 * {@link javax.servlet.http.HttpServlet} service providers and annotated with
 * the {@link javax.servlet.annotation.WebServlet} annotation. It also loads the
 * registered {@link jmri.server.web.spi.WebServerConfiguration} objects to get
 * configuration for file handling, redirection, and denial of access to
 * resources.
 * <p>
 * When there is a conflict over how a path should be handled, denials take
 * precedence, followed by servlets, redirections, and lastly direct access to
 * files.
 *
 * @author Bob Jacobsen Copyright 2005, 2006
 * @author Randall Wood Copyright 2012, 2016
 */
public final class WebServer implements LifeCycle, LifeCycle.Listener {

    private static enum Registration {
        DENIAL, REDIRECTION, RESOURCE, SERVLET
    }
    private final Server server;
    private ZeroConfService zeroConfService = null;
    private WebServerPreferences preferences = null;
    private ShutDownTask shutDownTask = null;
    private final HashMap<String, Registration> registeredUrls = new HashMap<>();
    private final static Logger log = LoggerFactory.getLogger(WebServer.class);

    /**
     * Create a WebServer instance with the default preferences.
     */
    public WebServer() {
        this(InstanceManager.getDefault(WebServerPreferences.class));
    }

    /**
     * Create a WebServer instance with the specified preferences.
     *
     * @param preferences the preferences
     */
    public WebServer(WebServerPreferences preferences) {
        QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setName("WebServer");
        threadPool.setMaxThreads(1000);
        server = new Server(threadPool);
        this.preferences = preferences;
    }

    /**
     * Get the default web server instance.
     *
     * @return a WebServer instance, either the existing instance or a new
     *         instance created with the default constructor.
     */
    @Nonnull
    public static WebServer getDefault() {
        return InstanceManager.getOptionalDefault(WebServer.class).orElseGet(() -> {
            return InstanceManager.setDefault(WebServer.class, new WebServer());
        });
    }

    /**
     * Start the web server. Calls {@link #start(boolean)} with {@code true}.
     */
    @Override
    public void start() {
        this.start(true);
    }

    /**
     * Start the web server.
     *
     * @param autoLoad true to load all registered
     *                 {@link WebServerConfiguration}s and {@link HttpServlet}s;
     *                 false otherwise
     */
    public void start(boolean autoLoad) {
        if (!server.isRunning()) {
            ServerConnector connector = new ServerConnector(server);
            connector.setIdleTimeout(5 * 60 * 1000); // 5 minutes
            connector.setSoLingerTime(-1);
            connector.setPort(preferences.getPort());
            server.setConnectors(new Connector[]{connector});
            server.setHandler(new ContextHandlerCollection());

            // Load all path handlers
            ServiceLoader.load(WebServerConfiguration.class).forEach((configuration) -> {
                configuration.getFilePaths().entrySet().forEach((resource) -> {
                    this.registerResource(resource.getKey(), resource.getValue());
                });
                configuration.getRedirectedPaths().entrySet().forEach((redirection) -> {
                    this.registerRedirection(redirection.getKey(), redirection.getValue());
                });
                configuration.getForbiddenPaths().forEach((denial) -> {
                    this.registerDenial(denial);
                });
            });
            // Load all classes that provide the HttpServlet service.
            ServiceLoader.load(HttpServlet.class).forEach((servlet) -> {
                this.registerServlet(servlet.getClass(), servlet);
            });
            server.addLifeCycleListener(this);

            Thread serverThread = new ServerThread(server);
            serverThread.setName("WebServer"); // NOI18N
            serverThread.start();
        }

    }

    /**
     * Stop the server.
     *
     * @throws Exception if there is an error stopping the server; defined by
     *                   Jetty superclass
     */
    @Override
    public void stop() throws Exception {
        server.stop();
    }

    /**
     * Get the public URI for a portable path. This method returns public URIs
     * for only some portable paths, and does not check that the portable path
     * is actually sane. Note that this refuses to return portable paths that
     * are outside of {@link jmri.util.FileUtil#PREFERENCES},
     * {@link jmri.util.FileUtil#PROFILE},
     * {@link jmri.util.FileUtil#SETTINGS}, or
     * {@link jmri.util.FileUtil#PROGRAM}.
     *
     * @param path the JMRI portable path
     * @return The servable URI or null
     * @see jmri.util.FileUtil#getPortableFilename(java.io.File)
     */
    public static String URIforPortablePath(String path) {
        if (path.startsWith(FileUtil.PREFERENCES)) {
            return path.replaceFirst(FileUtil.PREFERENCES, "/prefs/"); // NOI18N
        } else if (path.startsWith(FileUtil.PROFILE)) {
            return path.replaceFirst(FileUtil.PROFILE, "/project/"); // NOI18N
        } else if (path.startsWith(FileUtil.SETTINGS)) {
            return path.replaceFirst(FileUtil.SETTINGS, "/settings/"); // NOI18N
        } else if (path.startsWith(FileUtil.PROGRAM)) {
            return path.replaceFirst(FileUtil.PROGRAM, "/dist/"); // NOI18N
        } else {
            return null;
        }
    }

    public int getPort() {
        return preferences.getPort();
    }

    public WebServerPreferences getPreferences() {
        return preferences;
    }

    /**
     * Register a URL pattern to be denied access.
     *
     * @param urlPattern the pattern to deny access to
     */
    public void registerDenial(String urlPattern) {
        this.registeredUrls.put(urlPattern, Registration.DENIAL);
        ServletContextHandler servletContext = new ServletContextHandler(ServletContextHandler.NO_SECURITY);
        servletContext.setContextPath(urlPattern);
        DenialServlet servlet = new DenialServlet();
        servletContext.addServlet(new ServletHolder(servlet), "/*"); // NOI18N
        ((ContextHandlerCollection) this.server.getHandler()).addHandler(servletContext);
    }

    /**
     * Register a URL pattern to return resources from the file system. The
     * filePath may start with any of the following:
     * <ol>
     * <li>{@link jmri.util.FileUtil#PREFERENCES}
     * <li>{@link jmri.util.FileUtil#PROFILE}
     * <li>{@link jmri.util.FileUtil#SETTINGS}
     * <li>{@link jmri.util.FileUtil#PROGRAM}
     * </ol>
     * Note that the filePath can be overridden by an otherwise identical
     * filePath starting with any of the portable paths above it in the
     * preceding list.
     *
     * @param urlPattern the pattern to get resources for
     * @param filePath   the portable path for the resources
     * @throws IllegalArgumentException if urlPattern is already registered to
     *                                  deny access or for a servlet or if
     *                                  filePath is not allowed
     */
    public void registerResource(String urlPattern, String filePath) throws IllegalArgumentException {
        if (this.registeredUrls.get(urlPattern) != null) {
            throw new IllegalArgumentException("urlPattern \"" + urlPattern + "\" is already registered.");
        }
        this.registeredUrls.put(urlPattern, Registration.RESOURCE);
        ServletContextHandler servletContext = new ServletContextHandler(ServletContextHandler.NO_SECURITY);
        servletContext.setContextPath(urlPattern);
        HandlerList handlers = new HandlerList();
        if (filePath.startsWith(FileUtil.PROGRAM) && !filePath.equals(FileUtil.PROGRAM)) {
            // make it possible to override anything under program: with an identical path under preference:, profile:, or settings:
            log.debug("Setting up handler chain for {}", urlPattern);
            ResourceHandler preferenceHandler = new DirectoryHandler(FileUtil.getAbsoluteFilename(filePath.replace(FileUtil.PROGRAM, FileUtil.PREFERENCES)));
            ResourceHandler projectHandler = new DirectoryHandler(FileUtil.getAbsoluteFilename(filePath.replace(FileUtil.PROGRAM, FileUtil.PROFILE)));
            ResourceHandler settingsHandler = new DirectoryHandler(FileUtil.getAbsoluteFilename(filePath.replace(FileUtil.PROGRAM, FileUtil.SETTINGS)));
            ResourceHandler programHandler = new DirectoryHandler(FileUtil.getAbsoluteFilename(filePath));
            handlers.setHandlers(new Handler[]{preferenceHandler, projectHandler, settingsHandler, programHandler, new DefaultHandler()});
        } else if (filePath.startsWith(FileUtil.SETTINGS) && !filePath.equals(FileUtil.SETTINGS)) {
            // make it possible to override anything under settings: with an identical path under preference: or profile:
            log.debug("Setting up handler chain for {}", urlPattern);
            ResourceHandler preferenceHandler = new DirectoryHandler(FileUtil.getAbsoluteFilename(filePath.replace(FileUtil.SETTINGS, FileUtil.PREFERENCES)));
            ResourceHandler projectHandler = new DirectoryHandler(FileUtil.getAbsoluteFilename(filePath.replace(FileUtil.PROGRAM, FileUtil.PROFILE)));
            ResourceHandler settingsHandler = new DirectoryHandler(FileUtil.getAbsoluteFilename(filePath));
            handlers.setHandlers(new Handler[]{preferenceHandler, projectHandler, settingsHandler, new DefaultHandler()});
        } else if (filePath.startsWith(FileUtil.PROFILE) && !filePath.equals(FileUtil.PROFILE)) {
            // make it possible to override anything under profile: with an identical path under preference:
            log.debug("Setting up handler chain for {}", urlPattern);
            ResourceHandler preferenceHandler = new DirectoryHandler(FileUtil.getAbsoluteFilename(filePath.replace(FileUtil.SETTINGS, FileUtil.PREFERENCES)));
            ResourceHandler projectHandler = new DirectoryHandler(FileUtil.getAbsoluteFilename(filePath.replace(FileUtil.PROGRAM, FileUtil.PROFILE)));
            handlers.setHandlers(new Handler[]{preferenceHandler, projectHandler, new DefaultHandler()});
        } else if (FileUtil.isPortableFilename(filePath)) {
            log.debug("Setting up handler chain for {}", urlPattern);
            ResourceHandler handler = new DirectoryHandler(FileUtil.getAbsoluteFilename(filePath));
            handlers.setHandlers(new Handler[]{handler, new DefaultHandler()});
        } else if (URIforPortablePath(filePath) == null) {
            throw new IllegalArgumentException("\"" + filePath + "\" is not allowed.");
        }
        ContextHandler handlerContext = new ContextHandler();
        handlerContext.setContextPath(urlPattern);
        handlerContext.setHandler(handlers);
        ((ContextHandlerCollection) this.server.getHandler()).addHandler(handlerContext);
    }

    /**
     * Register a URL pattern to be redirected to another resource.
     *
     * @param urlPattern  the pattern to be redirected
     * @param redirection the path to which the pattern is redirected
     * @throws IllegalArgumentException if urlPattern is already registered for
     *                                  any other purpose
     */
    public void registerRedirection(String urlPattern, String redirection) throws IllegalArgumentException {
        Registration registered = this.registeredUrls.get(urlPattern);
        if (registered != null && registered != Registration.REDIRECTION) {
            throw new IllegalArgumentException("\"" + urlPattern + "\" registered to " + registered);
        }
        this.registeredUrls.put(urlPattern, Registration.REDIRECTION);
        ServletContextHandler servletContext = new ServletContextHandler(ServletContextHandler.NO_SECURITY);
        servletContext.setContextPath(urlPattern);
        RedirectionServlet servlet = new RedirectionServlet(urlPattern, redirection);
        servletContext.addServlet(new ServletHolder(servlet), ""); // NOI18N
        ((ContextHandlerCollection) this.server.getHandler()).addHandler(servletContext);
    }

    /**
     * Register a {@link javax.servlet.http.HttpServlet } that is annotated with
     * the {@link javax.servlet.annotation.WebServlet } annotation.
     * <p>
     * This method calls
     * {@link #registerServlet(java.lang.Class, javax.servlet.http.HttpServlet)}
     * with a null HttpServlet.
     *
     * @param type The actual class of the servlet.
     */
    public void registerServlet(Class<? extends HttpServlet> type) {
        this.registerServlet(type, null);
    }

    /**
     * Register a {@link javax.servlet.http.HttpServlet } that is annotated with
     * the {@link javax.servlet.annotation.WebServlet } annotation.
     * <p>
     * Registration reads the WebServlet annotation to get the list of paths the
     * servlet should handle and creates instances of the Servlet to handle each
     * path.
     * <p>
     * Note that all HttpServlets registered using this mechanism must have a
     * default constructor.
     *
     * @param type     The actual class of the servlet.
     * @param instance An un-initialized, un-registered instance of the servlet.
     */
    public void registerServlet(Class<? extends HttpServlet> type, HttpServlet instance) {
        try {
            for (ServletContextHandler handler : this.registerServlet(
                    ServletContextHandler.NO_SECURITY,
                    type,
                    instance
            )) {
                ((ContextHandlerCollection) this.server.getHandler()).addHandler(handler);
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
            log.error("Unable to register servlet", ex);
        }
    }

    private List<ServletContextHandler> registerServlet(int options, Class<? extends HttpServlet> type, HttpServlet instance)
            throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        WebServlet info = type.getAnnotation(WebServlet.class);
        List<ServletContextHandler> handlers = new ArrayList<>(info.urlPatterns().length);
        for (String pattern : info.urlPatterns()) {
            if (this.registeredUrls.get(pattern) != Registration.DENIAL) {
                // DenialServlet gets special handling
                if (info.name().equals("DenialServlet")) { // NOI18N
                    this.registeredUrls.put(pattern, Registration.DENIAL);
                } else {
                    this.registeredUrls.put(pattern, Registration.SERVLET);
                }
                ServletContextHandler context = new ServletContextHandler(options);
                context.setContextPath(pattern);
                log.debug("Creating new {} for URL pattern {}", type.getName(), pattern);
                context.addServlet(type, "/*"); // NOI18N
                handlers.add(context);
            } else {
                log.error("Unable to register servlet \"{}\" to provide denied URL {}", info.name(), pattern);
            }
        }
        return handlers;
    }

    @Override
    public void lifeCycleStarting(LifeCycle lc) {
        shutDownTask = new ServerShutDownTask(this);
        InstanceManager.getDefault(ShutDownManager.class).register(shutDownTask);
        log.info("Starting Web Server on port {}", preferences.getPort());
    }

    @Override
    public void lifeCycleStarted(LifeCycle lc) {
        if (this.preferences.isUseZeroConf()) {
            HashMap<String, String> properties = new HashMap<>();
            properties.put("path", "/"); // NOI18N
            properties.put(JSON.JSON, JSON.JSON_PROTOCOL_VERSION);
            log.info("Starting ZeroConfService _http._tcp.local for Web Server with properties {}", properties);
            zeroConfService = ZeroConfService.create("_http._tcp.local.", preferences.getPort(), properties); // NOI18N
            zeroConfService.publish();
        }
        log.debug("Web Server finished starting");
    }

    @Override
    public void lifeCycleFailure(LifeCycle lc, Throwable thrwbl) {
        if (zeroConfService != null) {
            zeroConfService.stop();
        }
        log.error("Web Server failed", thrwbl);
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
        if (zeroConfService != null) {
            zeroConfService.stop();
        }
        InstanceManager.getDefault(ShutDownManager.class).deregister(shutDownTask);
        log.debug("Web Server stopped");
    }

    @Override
    public boolean isRunning() {
        return this.server.isRunning();
    }

    @Override
    public boolean isStarted() {
        return this.server.isStarted();
    }

    @Override
    public boolean isStarting() {
        return this.server.isStarting();
    }

    @Override
    public boolean isStopping() {
        return this.server.isStopping();
    }

    @Override
    public boolean isStopped() {
        return this.server.isStopped();
    }

    @Override
    public boolean isFailed() {
        return this.server.isFailed();
    }

    @Override
    public void addLifeCycleListener(Listener ll) {
        this.server.addLifeCycleListener(ll);
    }

    @Override
    public void removeLifeCycleListener(Listener ll) {
        this.server.removeLifeCycleListener(ll);
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
                log.error("Exception starting Web Server", ex);
            }
        }
    }

    static private class ServerShutDownTask extends QuietShutDownTask {

        private final WebServer server;
        private boolean isComplete = false;

        public ServerShutDownTask(WebServer server) {
            super("Stop Web Server"); // NOI18N
            this.server = server;
        }

        @Override
        public boolean execute() {
            Thread t = new Thread(() -> {
                try {
                    server.stop();
                } catch (Exception ex) {
                    // Error without stack trace
                    log.warn("Error shutting down WebServer: {}", ex);
                    // Full stack trace
                    log.debug("Details follow: ", ex);
                }
                this.isComplete = true;
            });
            t.setName("ServerShutDownTask");
            t.start();
            return true;
        }

        @Override
        public boolean isParallel() {
            return true;
        }

        @Override
        public boolean isComplete() {
            return this.isComplete;
        }
    }
}
