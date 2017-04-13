package jmri.server.web.app;

import static jmri.web.servlet.ServletUtil.UTF8_APPLICATION_JAVASCRIPT;
import static jmri.web.servlet.ServletUtil.UTF8_TEXT_HTML;

import java.io.File;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.profile.ProfileUtils;
import jmri.util.FileUtil;
import jmri.web.servlet.ServletUtil;
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
    "/app/dynamic"
})
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
        log.debug("App contextPath: {}, pathInfo: {}, pathTranslated: {}", request.getContextPath(), request.getPathInfo(), request.getPathTranslated());
        response.setHeader("Connection", "Keep-Alive"); // NOI18N
        switch (request.getContextPath()) {
            case "/app": // NOI18N
                this.processApp(request, response);
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
            String inComments = "-->%s\n<!--"; // NOI18N
            WebAppManager manager = getWebAppManager();
            // Format elements for index.html
            // 1 = railroad name
            // 2 = scripts (in comments)
            // 3 = stylesheets (in comments)
            // 4 = body content (divs)
            // 5 = help menu title
            // 6 = help menu contents (in comments)
            // 7 = personal menu title
            // 8 = personal menu contents (in comments)
            FileUtil.appendTextToFile(index, String.format(request.getLocale(),
                    FileUtil.readURL(FileUtil.findURL("web/app/index.html")),
                    ServletUtil.getInstance().getRailroadName(false), // railroad name
                    String.format(inComments, manager.getScriptTags(profile)), // scripts (in comments)
                    String.format(inComments, manager.getStyleTags(profile)), // stylesheets (in comments)
                    "<!-- -->", // body content (divs)
                    Bundle.getMessage(request.getLocale(), "help"), // help menu title
                    String.format(inComments, manager.getHelpMenuItems(profile, request.getLocale())), // help menu contents (in comments)
                    Bundle.getMessage(request.getLocale(), "user"), // personal menu title
                    String.format(inComments, manager.getUserMenuItems(profile, request.getLocale())) // personal menu contents (in comments)
            ));
        }
        response.getWriter().print(FileUtil.readFile(index));
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
                    FileUtil.readURL(FileUtil.findURL("web/app/script.js")), // NOI18N
                    manager.getAngularDependencies(profile, request.getLocale()),
                    manager.getAngularRoutes(profile, request.getLocale()),
                    String.format("\n$scope.navigationItems = %s;\n", manager.getNavigation(profile, request.getLocale())) // NOI18N
            ));
        }
        response.getWriter().print(FileUtil.readFile(script));
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
        return "Short description";
    }// </editor-fold>

}
