package jmri.web.servlet.directory;

import java.io.IOException;
import java.util.Objects;

import org.eclipse.jetty.server.handler.ResourceHandler;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;

/**
 * @author Randall Wood Copyright 2016, 2020
 */
public class DirectoryHandler extends ResourceHandler {

    /**
     * Construct a DirectoryHandler for the named directory with common default
     * settings. This constructor sets directory listings to true and returns
     * the contents of index.html within the directory instead of listing the
     * contents of the directory if index.html exists.
     *
     * @param resourceBase the directory to serve, should be non-null, but
     *                     allowing null until deprecated constructor is removed
     */
    public DirectoryHandler(@Nonnull String resourceBase) {
        super(new DirectoryService());
        super.setDirectoriesListed(true);
        super.setWelcomeFiles(new String[]{"index.html"}); // NOI18N
        super.setResourceBase(resourceBase);

//        this.handle(STARTED, baseRequest, request, response);
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
//        log.error("DirectoryHandler: target: {}, baseRequest: {}, request: {}, response: {}", target, Objects.hashCode(baseRequest), Objects.hashCode(request), Objects.hashCode(response));
        log.error("DirectoryHandler: target: {}, baseRequest: {}, request: {}, response: {}", target, baseRequest, request, response);

//        if (target.equals("/help/en/html/web/js/side.js")) {
//            target = "Hello";
//        }

        super.handle(target, baseRequest, request, response);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DirectoryHandler.class);
}
