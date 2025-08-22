package jmri.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the jmri.web.servlet.RedirectionServlet class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class RedirectionServletTest {

    @Test
    public void testCtor() {
        RedirectionServlet a = new RedirectionServlet();
        assertNotNull(a);
    }

    @Test
    public void testGet() throws ServletException, IOException {
        RedirectionServlet instance = new RedirectionServlet("foo", "bar");
        // test with expected path
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setContextPath("foo");
        instance.doGet(request, response);
        assertEquals( HttpServletResponse.SC_MOVED_TEMPORARILY, response.getStatus(), "Temporary redirection");
        assertEquals( "bar", response.getRedirectedUrl(), "Redirection URL");

        // test with unexpected path
        // (note web server is seriously broken if this occurs in the wild)
        MockHttpServletRequest errorRequest = new MockHttpServletRequest();
        MockHttpServletResponse errorResponse = new MockHttpServletResponse();
        errorRequest.setContextPath("baz");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> instance.doGet(errorRequest, errorResponse));
        assertEquals( "Redirect URL must not be null", ex.getMessage(), "null URL");
    }

    @Test
    public void testPost() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContextPath("foo");
        MockHttpServletResponse response = new MockHttpServletResponse();
        RedirectionServlet instance = new RedirectionServlet("foo", "bar");
        instance.doGet(request, response);
        assertEquals( HttpServletResponse.SC_MOVED_TEMPORARILY, response.getStatus(), "Temporary redirection");
        assertEquals( "bar", response.getRedirectedUrl(), "Redirection URL");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
