package jmri.web.servlet.directory;

import org.eclipse.jetty.server.handler.ResourceHandler;

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
        super.setResourceBase(resourceBase);
    }

    /**
     * Default handler constructor.
     */
    public DirectoryHandler() {
        super(new DirectoryService());
        super.setDirectoriesListed(true);
        super.setWelcomeFiles(new String[]{"index.html"}); // NOI18N
    }

}
