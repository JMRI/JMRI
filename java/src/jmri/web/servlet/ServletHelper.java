package jmri.web.servlet;

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
public class ServletHelper {

    private static ServletHelper instance = null;

    public String getRailroadName(boolean inComments) {
        if (inComments) {
            return "-->" + WebServerManager.getWebServerPreferences().getRailRoadName() + "<!--";
        }
        return WebServerManager.getWebServerPreferences().getRailRoadName();
    }

    public String getNavBar(Locale locale, String context) throws IOException {
        // Should return a built NavBar with li class for current context set to "active"
        return String.format(locale,
                "-->" + FileUtil.readURL(FileUtil.findURL(Bundle.getMessage(locale, "NavBar.html"))) + "<!--",
                this.getRailroadName(true));
    }

    public static ServletHelper getHelper() {
        if (ServletHelper.instance == null) {
            ServletHelper.instance = new ServletHelper();
        }
        return ServletHelper.instance;
    }

    public void setNonCachingHeaders(HttpServletResponse response) {
        Date now = new Date();
        response.setDateHeader("Date", now.getTime()); // NOI18N
        response.setDateHeader("Last-Modified", now.getTime()); // NOI18N
        response.setDateHeader("Expires", now.getTime()); // NOI18N
        response.setHeader("Cache-control", "no-cache, no-store"); // NOI18N
        response.setHeader("Pragma", "no-cache"); // NOI18N
    }
}
