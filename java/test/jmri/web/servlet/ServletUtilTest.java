package jmri.web.servlet;

import java.util.Date;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import jmri.util.web.MockServletExchange;
import jmri.web.server.WebServerPreferences;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
        assertEquals("-->" + name + "<!--", instance.getRailroadName(true));
        assertEquals(name, instance.getRailroadName(false));
    }

    @Test
    public void testSetNonCachingHeaders() {
        // create a ServletUtil instance
        ServletUtil instance = new ServletUtil();
        MockServletExchange exchange = new MockServletExchange("GET", "/test");
        HttpServletResponse response = exchange.getResponse();

        Date now = instance.setNonCachingHeaders(response);

        // get a date string matching the date in the response
        // Java has no standard method to get an RFC 7232 formatted date
        String date = MockServletExchange.getRfc7232formatHttpDate(now.getTime());

        // verify instance has expected header values
        assertEquals(date, response.getHeader("Date"));
        assertEquals(date, response.getHeader("Last-Modified"));
        assertEquals(date, response.getHeader("Expires"));
        assertEquals("no-cache, no-store", response.getHeader("Cache-control"));
        assertEquals("no-cache", response.getHeader("Pragma"));
    }

    @Test
    public void testGetTitle() {
        ServletUtil instance = new ServletUtil();
        String title = "TITLE";
        String name = "TEST_RAILROAD";
        Locale locale = Locale.ENGLISH;
        InstanceManager.getDefault(WebServerPreferences.class).setRailroadName(name);
        assertEquals("TITLE | TEST_RAILROAD", instance.getTitle(locale, title));
    }

}
