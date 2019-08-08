package jmri.web.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.util.FileUtil;
import jmri.web.server.WebServerPreferences;

/**
 * Utility methods to reduce code duplication in servlets.
 *
 * @author Randall Wood
 */
public class ServletUtil implements InstanceManagerAutoDefault {

    public static final String UTF8 = StandardCharsets.UTF_8.toString(); // NOI18N
    // media types
    public static final String APPLICATION_JAVASCRIPT = "application/javascript"; // NOI18N
    public static final String APPLICATION_JSON = "application/json"; // NOI18N
    public static final String APPLICATION_XML = "application/xml"; // NOI18N
    public static final String IMAGE_PNG = "image/png"; // NOI18N
    public static final String TEXT_HTML = "text/html"; // NOI18N
    public static final String UTF8_APPLICATION_JAVASCRIPT = APPLICATION_JAVASCRIPT + "; charset=utf-8"; // NOI18N
    public static final String UTF8_APPLICATION_JSON = APPLICATION_JSON + "; charset=utf-8"; // NOI18N
    public static final String UTF8_APPLICATION_XML = APPLICATION_XML + "; charset=utf-8"; // NOI18N
    public static final String UTF8_TEXT_HTML = TEXT_HTML + "; charset=utf-8"; // NOI18N

    /**
     * Get the railroad name for HTML documents.
     *
     * @param inComments Return the railroad name prepended and appended by
     *                   closing and opening comment markers
     * @return the Railroad name, possibly with formatting
     */
    public String getRailroadName(boolean inComments) {
        if (inComments) {
            return "-->" + InstanceManager.getDefault(WebServerPreferences.class).getRailroadName() + "<!--"; // NOI18N
        }
        return InstanceManager.getDefault(WebServerPreferences.class).getRailroadName();
    }

    /**
     * Create a common footer.
     *
     * @param locale  If a template is not available in locale, will return US
     *                English.
     * @param context divs included in footer template with class
     *                {@code context-<context>-only} will be shown.
     * @return an HTML footer
     * @throws IOException if template cannot be located
     */
    public String getFooter(Locale locale, String context) throws IOException {
        // Should return a built NavBar with li class for current context set to "active"
        String footer = String.format(locale,
                "-->" + FileUtil.readURL(FileUtil.findURL(Bundle.getMessage(locale, "Footer.html"))) + "<!--", // NOI18N
                this.getRailroadName(true));
        String clazz = "context" + context.replace("/", "-"); // NOI18N
        // replace class "context-<this-context>-only" with class "show"
        footer = footer.replace(clazz + "-only", "show"); // NOI18N
        // replace class "context-<some-other-context>-only" with class "hidden"
        footer = footer.replaceAll("context-[\\w-]*-only", "hidden"); // NOI18N
        // replace class "context-<this-context>" with class "active"
        footer = footer.replace(clazz, "active"); // NOI18N
        return footer;
    }

    /**
     * Create a common navigation header.
     *
     * @param locale  If a template is not available in locale, will return US
     *                English.
     * @param context divs included in navigation bar template with class
     *                {@code context-<context>-only} will be shown.
     * @return an HTML navigation bar
     * @throws IOException if template cannot be located
     */
    public String getNavBar(Locale locale, String context) throws IOException {
        // Should return a built NavBar with li class for current context set to "active"
        String navBar = String.format(locale,
                "-->" + FileUtil.readURL(FileUtil.findURL(Bundle.getMessage(locale, "NavBar.html"))) + "<!--", // NOI18N
                this.getRailroadName(true));
        String clazz = "context" + context.replace("/", "-"); // NOI18N
        // replace class "context-<this-context>-only" with class "show"
        navBar = navBar.replace(clazz + "-only", "show"); // NOI18N
        // replace class "context-<some-other-context>-only" with class "hidden"
        navBar = navBar.replaceAll("context-[\\w-]*-only", "hidden"); // NOI18N
        // replace class "context-<this-context>" with class "active"
        navBar = navBar.replace(clazz, "active"); // NOI18N
        if (InstanceManager.getDefault(WebServerPreferences.class).allowRemoteConfig()) {
            navBar = navBar.replace("config-enabled-only", "show"); // NOI18N
            navBar = navBar.replace("config-disabled-only", "hidden"); // NOI18N
        } else {
            navBar = navBar.replace("config-enabled-only", "hidden"); // NOI18N
            navBar = navBar.replace("config-disabled-only", "show"); // NOI18N
        }
        if (!InstanceManager.getDefault(WebServerPreferences.class).isReadonlyPower()) {
            navBar = navBar.replace("data-power=\"readonly\"", "data-power=\"readwrite\""); // NOI18N
        }
        return navBar;
    }

    /**
     * Set HTTP headers to prevent caching.
     *
     * @param response the response to set headers in
     * @return the date used for headers setting expiration and modification times
     */
    public Date setNonCachingHeaders(HttpServletResponse response) {
        Date now = new Date();
        response.setDateHeader("Date", now.getTime()); // NOI18N
        response.setDateHeader("Last-Modified", now.getTime()); // NOI18N
        response.setDateHeader("Expires", now.getTime()); // NOI18N
        response.setHeader("Cache-control", "no-cache, no-store"); // NOI18N
        response.setHeader("Pragma", "no-cache"); // NOI18N
        return now;
    }

    /**
     * Write a file to the given response.
     *
     * @param response    the response to write the file into
     * @param file        file to write
     * @param contentType file mime content type
     * @throws java.io.IOException if communications lost with client
     */
    public void writeFile(HttpServletResponse response, File file, String contentType) throws IOException {
        if (file.exists()) {
            if (file.canRead()) {
                response.setContentType(contentType);
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentLength((int) file.length());
                try (FileInputStream fileInputStream = new FileInputStream(file)) {
                    int bytes = fileInputStream.read();
                    while (bytes != -1) {
                        response.getOutputStream().write(bytes);
                        bytes = fileInputStream.read();
                    }
                }
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Return the complete title for an HTML document given the portion of the
     * title specific to the document.
     *
     * @param locale The requested Locale
     * @param title  Portion of title specific to page
     * @return The complete title
     */
    public String getTitle(Locale locale, String title) {
        return Bundle.getMessage(locale, "HtmlTitle", this.getRailroadName(false), title);
    }
}
