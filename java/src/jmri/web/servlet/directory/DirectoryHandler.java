package jmri.web.servlet.directory;

import org.eclipse.jetty.server.handler.ResourceHandler;
import javax.annotation.Nonnull;

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
    }
}
