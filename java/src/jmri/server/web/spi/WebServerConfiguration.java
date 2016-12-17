package jmri.server.web.spi;

import java.util.List;
import java.util.Map;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Provide additional configurations not encapsulated by a servlet for the
 * {@link jmri.web.server.WebServer}.
 *
 * @author Randall Wood
 */
public interface WebServerConfiguration {

    /**
     * Get paths that are to be returned by the web server as individual files
     * or directory listings. Note that all files or directories listed must be
     * in the {@link jmri.util.FileUtil#PREFERENCES},
     * {@link jmri.util.FileUtil#PROFILE}, {@link jmri.util.FileUtil#SETTINGS},
     * or {@link jmri.util.FileUtil#PROGRAM} JMRI portable path.
     *
     * @return a map containing the web path as the key, and the path on disk as
     *         the value; return an empty map if none
     */
    @CheckReturnValue
    @Nonnull
    public Map<String, String> getFilePaths();

    /**
     * Get paths that are to redirected by the web server.
     *
     * @return a map containing the request path as the key and the path to
     *         redirect to as the value; return an empty map if none
     */
    @CheckReturnValue
    @Nonnull
    public Map<String, String> getRedirectedPaths();

    /**
     * Get paths that are not to be returned. Requests for paths listed here
     * will be denied a
     * {@link javax.servlet.http.HttpServletResponse#SC_FORBIDDEN} response.
     *
     * @return a list containing the request path to be denied access to; return
     *         an empty list if none
     */
    @CheckReturnValue
    @Nonnull
    public List<String> getForbiddenPaths();

}
