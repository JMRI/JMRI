/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.web.servlet.index;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jmri.web.server.WebServerManager;
import jmri.web.servlet.EchoServlet;
import jmri.web.servlet.frameimage.JmriJFrameServlet;

/**
 *
 * @author rhwood
 */
public class IndexServlet extends HttpServlet {


    static ResourceBundle htmlStrings = ResourceBundle.getBundle("jmri.web.server.Html");
    static ResourceBundle indexResources = ResourceBundle.getBundle("jmri.web.servlet.index.IndexServlet");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println(String.format(htmlStrings.getString("HeadFormat"),
                htmlStrings.getString("HTML5DocType"),
                WebServerManager.getWebServerPreferences().getRailRoadName() + " Web Access",
                EchoServlet.class.getSimpleName()));
        response.getWriter().println("<h1>" + WebServerManager.getWebServerPreferences().getRailRoadName() + " Web Access</h1>");

        this.doPanels(request, response);

        response.getWriter().println(String.format(htmlStrings.getString("TailFormat"), "/en/html/web/index.shtml", htmlStrings.getString("GeneralMenuItems")));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }

    protected void doPanels(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.getWriter().println("<h2>Panels</h2>");
        response.getWriter().write(indexResources.getString("PanelIntro"));
        JmriJFrameServlet.doListMarkup(request, response);
        response.getWriter().write(indexResources.getString("PanelClosing"));
    }
}
