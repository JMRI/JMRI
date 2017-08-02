package jmri.web.servlet.home;

import static jmri.web.servlet.ServletUtil.UTF8_TEXT_HTML;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.util.FileUtil;
import jmri.web.servlet.ServletUtil;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Randall Wood (C) 2014, 2016
 */
@WebServlet(name = "HomeServlet",
        urlPatterns = {
            "/", // default
            "/index.html", // redirect to default since ~ 1 FEB 2014
            "/prefs/index.html" // some WiThrottle clients require this URL to show web services
        })
@ServiceProvider(service = HttpServlet.class)
public class HomeServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/index.html")
                || request.getRequestURI().startsWith("/prefs/index.html")) {
            response.sendRedirect("/");
            return;
        }
        if (!request.getRequestURI().equals("/")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.setHeader("Connection", "Keep-Alive"); // NOI18N
        response.setContentType(UTF8_TEXT_HTML);
        response.getWriter().print(String.format(request.getLocale(),
                FileUtil.readURL(FileUtil.findURL(Bundle.getMessage(request.getLocale(), "Home.html"))),
                InstanceManager.getDefault(ServletUtil.class).getRailroadName(false),
                InstanceManager.getDefault(ServletUtil.class).getNavBar(request.getLocale(), "/home"),
                InstanceManager.getDefault(ServletUtil.class).getRailroadName(false),
                InstanceManager.getDefault(ServletUtil.class).getFooter(request.getLocale(), "/home")
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
        return "Home Servlet";
    }// </editor-fold>

}
