package jmri.web.servlet;

import java.io.IOException;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Redirect traffic to another location.
 *
 * @author Randall Wood (C) 2016
 */
@WebServlet(name = "RedirectionServlet")
public class RedirectionServlet extends HttpServlet {

    private final Properties redirections = new Properties();
    // private static final Logger log = LoggerFactory.getLogger(RedirectionServlet.class);

    public RedirectionServlet() {
        // do nothing
    }

    public RedirectionServlet(String urlPattern, String redirection) {
        this.redirections.setProperty(urlPattern, redirection);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.sendRedirect(redirections.getProperty(request.getContextPath()));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        this.doGet(request, response);
    }

}
