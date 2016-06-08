package jmri.web.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Redirect traffic based on the values provided in a properties list.
 *
 * @author rhwood
 */
public class RedirectionServlet extends HttpServlet {

    private final Properties redirections = new Properties();
    private static final Logger log = LoggerFactory.getLogger(RedirectionServlet.class);

    public RedirectionServlet() {

        try {
            InputStream in;
            in = RedirectionServlet.class.getResourceAsStream("/jmri/web/server/FilePaths.properties"); // NOI18N
            try {
                redirections.load(in);
            } catch (IOException e) {
                log.error("Error in servlet creation IO", e);
            } finally {
                in.close();
            }
        } catch (IOException e) {
            log.error("Error in servlet creation IO", e);
        }
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
