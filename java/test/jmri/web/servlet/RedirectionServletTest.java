package jmri.web.servlet;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

/**
 * Tests for the jmri.web.servlet.RedirectionServlet class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class RedirectionServletTest {

    @Test
    public void testCtor() {
        RedirectionServlet a = new RedirectionServlet();
        Assert.assertNotNull(a);
    }

    @Test
    public void testGet() throws ServletException, IOException {
        RedirectionServlet instance = new RedirectionServlet("foo", "bar");
        // test with expected path
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setContextPath("foo");
        instance.doGet(request, response);
        Assert.assertEquals("Temporary redirection", HttpServletResponse.SC_MOVED_TEMPORARILY, response.getStatus());
        Assert.assertEquals("Redirection URL", "bar", response.getRedirectedUrl());
        // test with unexpected path
        // (note web server is seriously broken if this occurs in the wild)
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        request.setContextPath("baz");
        try {
            instance.doGet(request, response);
            Assert.fail("Expected exception not thrown");
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("null URL", "Redirect URL must not be null", ex.getMessage());
        }
    }

    @Test
    public void testPost() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("foo");
        MockHttpServletResponse response = new MockHttpServletResponse();
        RedirectionServlet instance = new RedirectionServlet("foo", "bar");
        instance.doGet(request, response);
        Assert.assertEquals("Temporary redirection", HttpServletResponse.SC_MOVED_TEMPORARILY, response.getStatus());
        Assert.assertEquals("Redirection URL", "bar", response.getRedirectedUrl());
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
