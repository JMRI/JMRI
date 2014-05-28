package jmri.web.servlet.home;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jmri.util.FileUtil;
import jmri.web.servlet.ServletUtil;
import static jmri.web.servlet.ServletUtil.UTF8_TEXT_HTML;

/**
 *
 * @author Randall Wood (C) 2014
 */
public class HomeServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Connection", "Keep-Alive"); // NOI18N
        response.setContentType(UTF8_TEXT_HTML);
        response.getWriter().print(String.format(request.getLocale(),
                FileUtil.readURL(FileUtil.findURL(Bundle.getMessage(request.getLocale(), "Home.html"))),
                ServletUtil.getInstance().getRailroadName(false),
                ServletUtil.getInstance().getNavBar(request.getLocale(), "/home"),
                ServletUtil.getInstance().getRailroadName(false),
                ServletUtil.getInstance().getFooter(request.getLocale(), "/home")
        ));
    }

// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
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
