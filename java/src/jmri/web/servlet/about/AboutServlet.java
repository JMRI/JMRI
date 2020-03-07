package jmri.web.servlet.about;

import static jmri.web.servlet.ServletUtil.UTF8_TEXT_HTML;

import java.io.IOException;
import java.util.Locale;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jmri.Application;
import jmri.InstanceManager;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.ConnectionConfigManager;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.util.FileUtil;
import jmri.web.servlet.ServletUtil;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Randall Wood (C) 2014, 2016
 * @author mstevetodd (C) 2017
 */
@WebServlet(name = "AboutServlet",
        urlPatterns = {"/about"})
@ServiceProvider(service = HttpServlet.class)
public class AboutServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        //retrieve the list of JMRI connections as a string
        StringBuilder connList = new StringBuilder("");
        String comma = "";
        for (ConnectionConfig conn : InstanceManager.getDefault(ConnectionConfigManager.class)) {
            if (!conn.getDisabled()) {
                connList.append(comma).append(Bundle.getMessage(request.getLocale(), "ConnectionSucceeded", conn.getConnectionName(), conn.name(), conn.getInfo()));
                comma = ", ";
            }
        }

        //print the html, using the replacement values listed to fill in the calculated stuff
        response.setHeader("Connection", "Keep-Alive"); // NOI18N
        response.setContentType(UTF8_TEXT_HTML);
        Profile profile = ProfileManager.getDefault().getActiveProfile();
        String profileName = profile != null ? profile.getName() : "";
        response.getWriter().print(String.format(request.getLocale(),
                FileUtil.readURL(FileUtil.findURL(Bundle.getMessage(request.getLocale(), "About.html"))),
                Bundle.getMessage(request.getLocale(), "AboutTitle"),                                   // page title is parm 1
                InstanceManager.getDefault(ServletUtil.class).getNavBar(request.getLocale(), "/about"), // navbar is parm 2
                InstanceManager.getDefault(ServletUtil.class).getRailroadName(false),                   // railroad name is parm 3
                InstanceManager.getDefault(ServletUtil.class).getFooter(request.getLocale(), "/about"), // footer is parm 4
                connList,                                                                               // system connection list is parm 5
                Application.getApplicationName() + " " + jmri.Version.name(),                           // JMRI version is parm 6                                         //JMRI version is parm 6
                jmri.Version.getCopyright(),                                                            // Copyright is parm 7
                System.getProperty("java.version", "<unknown>"),                                        // Java version is parm 8
                Locale.getDefault().toString(),                                                         // locale is parm 9
                profileName                                                                             // active profile name is 10
        ));
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
        return "About Servlet";
    }// </editor-fold>

}
