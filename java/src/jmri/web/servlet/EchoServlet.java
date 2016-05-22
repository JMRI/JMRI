/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.web.servlet;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author rhwood
 */
public class EchoServlet extends HttpServlet {

    static ResourceBundle htmlStrings = ResourceBundle.getBundle("jmri.web.server.Html");

    @Override
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="XSS_REQUEST_PARAMETER_TO_SERVLET_WRITER",
                                    justification="header being returned in formatted part of page")
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println(String.format(htmlStrings.getString("HeadFormat"),
                htmlStrings.getString("HTML5DocType"),
                "JMRI Echo: Request Returned",
                EchoServlet.class.getSimpleName(),
                ""));
        response.getWriter().println("<h1>Request Returned</h1>");
        response.getWriter().println("<table class=\"data\">\n<tr><th>Header</th><th>Value</th></tr>\n");
        List<String> headers = Collections.list(request.getHeaderNames());
        Collections.sort(headers);
        for (String header : headers) {
            response.getWriter().println("<tr><td>" + header + "</td><td>" + request.getHeader(header) + "</td></tr>");
        }
        if (request.getParameterNames().hasMoreElements()) {
            response.getWriter().println("<tr><th>Parameter</th><th>Value</th></tr>\n");
            List<String> parameters = Collections.list(request.getParameterNames());
            Collections.sort(parameters);
            for (String parameter : parameters) {
                String[] values = request.getParameterValues(parameter);
                for (int i = 0; i < values.length; i++) {
                    response.getWriter().println("<tr><td>" + parameter + "</td><td>" + values[i] + "</td></tr>");
                }
            }
        }
        response.getWriter().println("</table>");
        response.getWriter().println(String.format(htmlStrings.getString("TailFormat"), "", ""));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }

}
