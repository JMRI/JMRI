package jmri.web.servlet;

import apps.tests.Log4JFixture;
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
import org.powermock.api.mockito.PowerMockito;
import org.powermock.api.mockito.mockpolicies.Slf4jMockPolicy;
import org.powermock.core.classloader.annotations.MockPolicy;

/**
 *
 * @author Randall Wood Copyright 2017
 */
@MockPolicy(Slf4jMockPolicy.class)
public class ServletUtilTest {

    @Before
    public void setUp() {
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
    }

    @Test
    public void testGetRailroadName() {
        ServletUtil instance = new ServletUtil();
        String name = "TEST_RAILROAD";
        InstanceManager.getDefault(WebServerPreferences.class).setRailRoadName(name);
        Assert.assertEquals("-->" + name + "<!--", instance.getRailroadName(true));
        Assert.assertEquals(name, instance.getRailroadName(false));
    }

    @Test
    public void testSetNonCachingHeaders() {
        HttpServletResponse response = PowerMockito.mock(HttpServletResponse.class);
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
        InstanceManager.getDefault(WebServerPreferences.class).setRailRoadName(name);
        Assert.assertEquals(Bundle.getMessage(locale, "HtmlTitle", name, title), instance.getTitle(locale, title));
    }

}
