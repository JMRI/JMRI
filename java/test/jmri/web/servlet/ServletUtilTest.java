package jmri.web.servlet;

import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import jmri.web.server.WebServerPreferences;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 *
 * @author Randall Wood Copyright 2017, 2020
 */
public class ServletUtilTest {

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
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
        // create a ServletUtil instance
        HttpServletResponse response = new MockHttpServletResponse();
        ServletUtil instance = new ServletUtil();
        // set the headers for a response
        Date now = instance.setNonCachingHeaders(response);
        // get a date string matching the date in the response
        // Java has no standard method to get an RFC 7232 formatted date
        HttpServletResponse template = new MockHttpServletResponse();
        template.setDateHeader("Date", now.getTime());
        String date = template.getHeader("Date");
        // verify instance has expected header values
        Assert.assertEquals(date, response.getHeader("Date"));
        Assert.assertEquals(date, response.getHeader("Last-Modified"));
        Assert.assertEquals(date, response.getHeader("Expires"));
        Assert.assertEquals("no-cache, no-store", response.getHeader("Cache-control"));
        Assert.assertEquals("no-cache", response.getHeader("Pragma"));
    }

    @Test
    public void testGetTitle() {
        ServletUtil instance = new ServletUtil();
        String title = "TITLE";
        String name = "TEST_RAILROAD";
        Locale locale = Locale.ENGLISH;
        InstanceManager.getDefault(WebServerPreferences.class).setRailroadName(name);
        Assert.assertEquals("TITLE | TEST_RAILROAD", instance.getTitle(locale, title));
    }

}
