package jmri.web.servlet;

import java.util.Date;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import jmri.web.server.WebServerPreferences;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 *
 * @author Randall Wood Copyright 2017
 */
public class ServletUtilTest {

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    @Test
    public void testGetRailroadName() {
        ServletUtil instance = new ServletUtil();
        String name = "TEST_RAILROAD";
        InstanceManager.getDefault(WebServerPreferences.class).setRailroadName(name);
        Assert.assertEquals("-->" + name + "<!--", instance.getRailroadName(true));
        Assert.assertEquals(name, instance.getRailroadName(false));
    }

    @Test
    public void testSetNonCachingHeaders() {
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        ServletUtil instance = new ServletUtil();
        Date now = instance.setNonCachingHeaders(response);
        Mockito.verify(response).setDateHeader("Date", now.getTime());
        Mockito.verify(response).setDateHeader("Last-Modified", now.getTime());
        Mockito.verify(response).setDateHeader("Expires", now.getTime());
        Mockito.verify(response).setHeader("Cache-control", "no-cache, no-store");
        Mockito.verify(response).setHeader("Pragma", "no-cache");
    }

    @Test
    public void testGetTitle() {
        ServletUtil instance = new ServletUtil();
        String title = "TITLE";
        String name = "TEST_RAILROAD";
        Locale locale = Locale.ENGLISH;
        InstanceManager.getDefault(WebServerPreferences.class).setRailroadName(name);
        Assert.assertEquals(Bundle.getMessage(locale, "HtmlTitle", name, title), instance.getTitle(locale, title));
    }

}
