package jmri.server.web.app;

import static jmri.web.servlet.ServletUtil.UTF8_APPLICATION_JSON;
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
        log.error("App contextPath: {}, pathInfo: {}, pathTranslated: {}", request.getContextPath(), request.getPathInfo(), request.getPathTranslated());
        if (request.getContextPath().equals("/app") && request.getPathTranslated() == null) {
            response.setContentType("text/html;charset=UTF-8");
            Profile profile = ProfileManager.getDefault().getActiveProfile();
            File cache = ProfileUtils.getCacheDirectory(profile, this.getClass());
            File index = new File(cache, "index.html");
            if (!index.exists()) {
                WebAppManager manager = InstanceManager.getNullableDefault(WebAppManager.class);
                if (manager == null) {
                    log.error("No WebAppManager available.");
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    return;
                }
                // Format elements for index.html
                // 1 = railroad name
                // 2 = scripts
                // 3 = stylesheets
                // 4 = body content (divs)
                // 5 = help menu title
                // 6 = help menu contents (in comments)
                // 7 = personal menu title
                // 8 = personal menu contents (in comments)
                FileUtil.appendTextToFile(index, String.format(request.getLocale(),
                        FileUtil.readURL(FileUtil.findURL("web/app/index.html")),
                        ServletUtil.getInstance().getRailroadName(false), // railroad name
                        manager.getScriptTags(profile), // scripts
                        "", // stylesheets
                        "<!-- -->", // body content (divs)
                        Bundle.getMessage(request.getLocale(), "help"), // help menu title
                        "--> <!--", // help menu contents (in comments)
                        Bundle.getMessage(request.getLocale(), "user"), // personal menu title
                        "--> <!--" // personal menu contents (in comments)
                ));
            }
            response.setHeader("Connection", "Keep-Alive"); // NOI18N
            response.setContentType(UTF8_TEXT_HTML);
            response.getWriter().print(FileUtil.readFile(index));
        } else {
            response.setContentType(UTF8_APPLICATION_JSON);
            // TODO: build dynamic JSON bits that get fed into the app
        }
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
