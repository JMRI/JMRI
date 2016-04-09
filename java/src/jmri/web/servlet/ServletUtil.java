package jmri.web.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.util.FileUtil;
import jmri.web.server.WebServerManager;

/**
 *
 * @author rhwood
 */
public class ServletUtil {

    private static ServletUtil instance = null;
    public static final String UTF8 = "UTF-8"; // NOI18N
    // media types
    public static final String APPLICATION_JSON = "application/json"; // NOI18N
    public static final String APPLICATION_XML = "application/xml"; // NOI18N
    public static final String IMAGE_PNG = "image/png"; // NOI18N
    public static final String TEXT_HTML = "text/html"; // NOI18N
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
            return "-->" + WebServerManager.getWebServerPreferences().getRailRoadName() + "<!--"; // NOI18N
        }
        return WebServerManager.getWebServerPreferences().getRailRoadName();
    }

    public String getFooter(Locale locale, String context) throws IOException {
        // Should return a built NavBar with li class for current context set to "active"
        String footer = String.format(locale,
                "-->" + FileUtil.readURL(FileUtil.findURL(Bundle.getMessage(locale, "Footer.html"))) + "<!--", // NOI18N
                this.getRailroadName(true));
        context = "context" + context.replace("/", "-"); // NOI18N
        // replace class "context-<this-context>-only" with class "show"
        footer = footer.replace(context + "-only", "show"); // NOI18N
        // replace class "context-<some-other-context>-only" with class "hidden"
        footer = footer.replaceAll("context-[\\w-]*-only", "hidden"); // NOI18N
        // replace class "context-<this-context>" with class "active"
        footer = footer.replace(context, "active"); // NOI18N
        return footer;
    }

    public String getNavBar(Locale locale, String context) throws IOException {
        // Should return a built NavBar with li class for current context set to "active"
        String navBar = String.format(locale,
                "-->" + FileUtil.readURL(FileUtil.findURL(Bundle.getMessage(locale, "NavBar.html"))) + "<!--", // NOI18N
                this.getRailroadName(true));
        context = "context" + context.replace("/", "-"); // NOI18N
        // replace class "context-<this-context>-only" with class "show"
        navBar = navBar.replace(context + "-only", "show"); // NOI18N
        // replace class "context-<some-other-context>-only" with class "hidden"
        navBar = navBar.replaceAll("context-[\\w-]*-only", "hidden"); // NOI18N
        // replace class "context-<this-context>" with class "active"
        navBar = navBar.replace(context, "active"); // NOI18N
        if (WebServerManager.getWebServerPreferences().allowRemoteConfig()) {
            navBar = navBar.replace("config-enabled-only", "show"); // NOI18N
            navBar = navBar.replace("config-disabled-only", "hidden"); // NOI18N
        } else {
            navBar = navBar.replace("config-enabled-only", "hidden"); // NOI18N
            navBar = navBar.replace("config-disabled-only", "show"); // NOI18N
        }
        if (!WebServerManager.getWebServerPreferences().isReadonlyPower()) {
            navBar = navBar.replace("data-power=\"readonly\"", "data-power=\"readwrite\""); // NOI18N
        }
        return navBar;
    }

    public static ServletUtil getInstance() {
        if (ServletUtil.instance == null) {
            ServletUtil.instance = new ServletUtil();
        }
        return ServletUtil.instance;
    }

    /**
     * Set HTTP headers in the response to prevent caching.
     *
     * @param response
     */
    public void setNonCachingHeaders(HttpServletResponse response) {
        Date now = new Date();
        response.setDateHeader("Date", now.getTime()); // NOI18N
        response.setDateHeader("Last-Modified", now.getTime()); // NOI18N
        response.setDateHeader("Expires", now.getTime()); // NOI18N
        response.setHeader("Cache-control", "no-cache, no-store"); // NOI18N
        response.setHeader("Pragma", "no-cache"); // NOI18N
    }

    /**
     * Write a file to the given response.
     *
     * @param response
     * @param file
     * @param contentType
     * @throws IOException
     */
    public void writeFile(HttpServletResponse response, File file, String contentType) throws IOException {
        if (file.exists()) {
            if (file.canRead()) {
                response.setContentType(contentType);
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentLength((int) file.length());
                FileInputStream fileInputStream = new FileInputStream(file);
                try {
                    int bytes;
                    while ((bytes = fileInputStream.read()) != -1) {
                        response.getOutputStream().write(bytes);
                    }
                } finally {
                    fileInputStream.close();
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
