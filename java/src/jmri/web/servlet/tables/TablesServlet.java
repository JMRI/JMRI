package jmri.web.servlet.tables;

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
 * Provide web UI for data, such as turnouts, sensors, etc.
 *
 * Each method of this Servlet responds to a unique URL pattern.
 *
 * @author mstevetodd
 */

/*
 *
 */
@WebServlet(name = "TablesServlet",
        urlPatterns = {
            "/tables", // default
        })
@ServiceProvider(service = HttpServlet.class)
public class TablesServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String[] path = request.getRequestURI().split("/"); // NOI18N
        String tableType = java.net.URLDecoder.decode(path[path.length - 1], "UTF-8");

        //print the html, using the replacement values listed to fill in the calculated stuff
        response.setHeader("Connection", "Keep-Alive"); // NOI18N
        response.setContentType(UTF8_TEXT_HTML);
        response.getWriter().print(String.format(request.getLocale(),
                FileUtil.readURL(FileUtil.findURL(Bundle.getMessage(request.getLocale(), "Tables.html"))),
                Bundle.getMessage(request.getLocale(), "TablesTitle"),               //page title is parm 1
                InstanceManager.getDefault(ServletUtil.class).getNavBar(request.getLocale(), "/tables"), //navbar is parm 2
                InstanceManager.getDefault(ServletUtil.class).getRailroadName(false),                   //railroad name is parm 3
                InstanceManager.getDefault(ServletUtil.class).getFooter(request.getLocale(), "/tables"), //footer is parm 4
                tableType //tableType is parm 5
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
        return "Tables Servlet";
    }// </editor-fold>

}
