package jmri.web.servlet.directory;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jmri.web.servlet.ServletUtil;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;

/**
 *
 * @author Randall Wood (C) 2016
 */
public class DirectoryHandler extends ResourceHandler {

    /**
     * Construct a DirectoryHandler for the named directory with common
     * default settings.
     *
     * This constructor sets directory listings to true and returns the contents
     * of index.html within the directory instead of listing the contents of the
     * directory if index.html exists.
     *
     * @param resourceBase the directory to serve
     */
    public DirectoryHandler(String resourceBase) {
        this();
        this.setResourceBase(resourceBase);
    }

    /**
     * Default handler constructor.
     */
    public DirectoryHandler() {
        super();
        this.setDirectoriesListed(true);
        this.setWelcomeFiles(new String[]{"index.html"}); // NOI18N
    }

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
