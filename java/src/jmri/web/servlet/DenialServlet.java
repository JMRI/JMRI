package jmri.web.servlet;

import static jmri.web.servlet.ServletUtil.UTF8_TEXT_HTML;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.openide.util.lookup.ServiceProvider;

/**
 * Servlet that simply sends an HTTP 403 FORBIDDEN error.
 *
 * Passing requests for certain resources protects those resources from network
 * access.
 *
 * @author rhwood
 */
@WebServlet(name = "DenialServlet",
        urlPatterns = {"/prefs/networkServices"})
@ServiceProvider(service = HttpServlet.class)
public class DenialServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(UTF8_TEXT_HTML);
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }

}
