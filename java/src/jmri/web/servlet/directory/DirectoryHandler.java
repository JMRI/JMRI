package jmri.web.servlet.directory;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jmri.web.servlet.ServletUtil;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;

/**
 *
 * @author rhwood
 */
public class DirectoryHandler extends ResourceHandler {

    @Override
    protected void doDirectory(HttpServletRequest request, HttpServletResponse response, Resource resource) throws IOException {
        if (this.isDirectoriesListed()) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setContentType(ServletUtil.UTF8_TEXT_HTML);
            response.getWriter().print((new DirectoryResource(request.getLocale(), resource)).getListHTML(request.getRequestURI(), request.getPathInfo().lastIndexOf("/") > 0)); // NOI18N
        } else {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }
}
