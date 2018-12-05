package jmri.server.web.app;

import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.VALUE;
import static jmri.web.servlet.ServletUtil.UTF8_APPLICATION_JAVASCRIPT;
import static jmri.web.servlet.ServletUtil.UTF8_APPLICATION_JSON;
import static jmri.web.servlet.ServletUtil.UTF8_TEXT_HTML;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jmri.Application;
import jmri.InstanceManager;
import jmri.Version;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.ConnectionConfigManager;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.profile.ProfileUtils;
import jmri.util.FileUtil;
import jmri.web.server.WebServerPreferences;
import jmri.web.servlet.ServletUtil;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dynamic content for the Angular JMRI web application.
 *
 * @author Randall Wood (C) 2016
 */
@WebServlet(name = "AppDynamicServlet", urlPatterns = {
    "/app",
    "/app/script",
    "/app/about"
})
@ServiceProvider(service = HttpServlet.class)
public class WebAppServlet extends HttpServlet {

    private final static Logger log = LoggerFactory.getLogger(WebAppServlet.class);

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        log.debug("App contextPath: {}, pathInfo: {}", request.getContextPath(), request.getPathInfo());
        response.setHeader("Connection", "Keep-Alive"); // NOI18N
        switch (request.getContextPath()) {
            case "/app": // NOI18N
                if (request.getPathInfo().startsWith("/locale-")) { // NOI18N
                    this.processLocale(request, response);
                } else {
                    this.processApp(request, response);
                }
                break;
            case "/app/about": // NOI18N
                this.processAbout(request, response);
                break;
            case "/app/script": // NOI18N
                this.processScript(request, response);
                break;
            default:
        }
    }

    private void processApp(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(UTF8_TEXT_HTML);
        Profile profile = ProfileManager.getDefault().getActiveProfile();
        File cache = new File(ProfileUtils.getCacheDirectory(profile, this.getClass()), request.getLocale().toString());
        FileUtil.createDirectory(cache);
        File index = new File(cache, "index.html"); // NOI18N
        if (!index.exists()) {
            String inComments = "-->%n%s<!--"; // NOI18N
            WebAppManager manager = getWebAppManager();
            // Format elements for index.html
            // 1 = railroad name
            // 2 = scripts (in comments)
            // 3 = stylesheets (in comments)
            // 4 = body content (divs)
            // 5 = help menu contents (in comments)
            // 6 = personal menu contents (in comments)
            FileUtil.appendTextToFile(index, String.format(request.getLocale(),
                    FileUtil.readURL(FileUtil.findURL("web/app/app/index.html")),
                    InstanceManager.getDefault(ServletUtil.class).getRailroadName(false), // railroad name
                    String.format(inComments, manager.getScriptTags(profile)), // scripts (in comments)
                    String.format(inComments, manager.getStyleTags(profile)), // stylesheets (in comments)
                    "<!-- -->", // body content (divs)
                    String.format(inComments, manager.getHelpMenuItems(profile, request.getLocale())), // help menu contents (in comments)
                    String.format(inComments, manager.getUserMenuItems(profile, request.getLocale())) // personal menu contents (in comments)
            ));
        }
        response.getWriter().print(FileUtil.readFile(index));
    }

    private void processAbout(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(UTF8_APPLICATION_JSON);
        Profile profile = ProfileManager.getDefault().getActiveProfile();
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode about = mapper.createObjectNode();
        about.put("additionalInfo", Bundle.getMessage(request.getLocale(), "AdditionalInfo", Application.getApplicationName())); // NOI18N
        about.put("copyright", Version.getCopyright()); // NOI18N
        about.put("title", InstanceManager.getDefault(WebServerPreferences.class).getRailroadName()); // NOI18N
        about.put("imgAlt", Application.getApplicationName()); // NOI18N
        // assuming Application.getLogo() is relative to program:
        about.put("imgSrc", "/" + Application.getLogo()); // NOI18N
        ArrayNode productInfo = about.putArray("productInfo"); // NOI18N
        productInfo.add(mapper.createObjectNode().put(NAME, Application.getApplicationName()).put(VALUE, Version.name()));
        if (profile != null) {
            productInfo.add(mapper.createObjectNode().put(NAME, Bundle.getMessage(request.getLocale(), "ActiveProfile")).put(VALUE, profile.getName())); // NOI18N
        }
        productInfo.add(mapper.createObjectNode()
                .put(NAME, "Java") // NOI18N
                .put(VALUE, Bundle.getMessage(request.getLocale(), "JavaVersion",
                        System.getProperty("java.version", Bundle.getMessage(request.getLocale(), "Unknown")), // NOI18N
                        System.getProperty("java.vm.name", Bundle.getMessage(request.getLocale(), "Unknown")), // NOI18N
                        System.getProperty("java.vm.version", ""), // NOI18N
                        System.getProperty("java.vendor", Bundle.getMessage(request.getLocale(), "Unknown")) // NOI18N
                )));
        productInfo.add(mapper.createObjectNode()
                .put(NAME, Bundle.getMessage(request.getLocale(), "Runtime"))
                .put(VALUE, Bundle.getMessage(request.getLocale(), "RuntimeVersion",
                        System.getProperty("java.runtime.name", Bundle.getMessage(request.getLocale(), "Unknown")), // NOI18N
                        System.getProperty("java.runtime.version", "") // NOI18N
                )));
        for (ConnectionConfig conn : InstanceManager.getDefault(ConnectionConfigManager.class)) {
            if (!conn.getDisabled()) {
                productInfo.add(mapper.createObjectNode()
                        .put(NAME, Bundle.getMessage(request.getLocale(), "ConnectionName", conn.getConnectionName()))
                        .put(VALUE, Bundle.getMessage(request.getLocale(), "ConnectionValue", conn.name(), conn.getInfo())));
            }
        }
        response.getWriter().print(mapper.writeValueAsString(about));
    }

    private void processScript(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(UTF8_APPLICATION_JAVASCRIPT);
        Profile profile = ProfileManager.getDefault().getActiveProfile();
        File cache = new File(ProfileUtils.getCacheDirectory(profile, this.getClass()), request.getLocale().toString());
        FileUtil.createDirectory(cache);
        File script = new File(cache, "script.js"); // NOI18N
        if (!script.exists()) {
            WebAppManager manager = getWebAppManager();
            FileUtil.appendTextToFile(script, String.format(request.getLocale(),
                    FileUtil.readURL(FileUtil.findURL("web/app/app/script.js")), // NOI18N
                    manager.getAngularDependencies(profile, request.getLocale()),
                    manager.getAngularRoutes(profile, request.getLocale()),
                    String.format("%n    $scope.navigationItems = %s;%n", manager.getNavigation(profile, request.getLocale())), // NOI18N
                    manager.getAngularSources(profile, request.getLocale()),
                    InstanceManager.getDefault(WebServerPreferences.class).getRailroadName()
            ));
        }
        response.getWriter().print(FileUtil.readFile(script));
    }

    private void processLocale(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(UTF8_APPLICATION_JSON);
        Profile profile = ProfileManager.getDefault().getActiveProfile();
        // the locale is the file name portion between "locale-" and ".json"
        Locale locale = new Locale(request.getPathInfo().substring(8, request.getPathInfo().length() - 5));
        File cache = new File(ProfileUtils.getCacheDirectory(profile, this.getClass()), locale.toString());
        FileUtil.createDirectory(cache);
        File file = new File(cache, "locale.json"); // NOI18N
        if (!file.exists()) {
            WebAppManager manager = getWebAppManager();
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode translation = mapper.createObjectNode();
            for (URI url : manager.getPreloadedTranslations(profile, locale)) {
                log.debug("Reading {}", url);
                JsonNode translations = mapper.readTree(url.toURL());
                log.debug("Read {}", translations);
                if (translations.isObject()) {
                    log.debug("Adding {}", translations);
                    Iterator<Entry<String, JsonNode>> fields = translations.fields();
                    fields.forEachRemaining((field) -> {
                        translation.set(field.getKey(), field.getValue());
                    });
                }
            }
            log.debug("Writing {}", translation);
            mapper.writeValue(file, translation);
        }
        response.getWriter().print(FileUtil.readFile(file));
    }

    private WebAppManager getWebAppManager() throws ServletException {
        return InstanceManager.getOptionalDefault(WebAppManager.class).orElseThrow(ServletException::new);
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "JMRI Web App support";
    }// </editor-fold>

}
