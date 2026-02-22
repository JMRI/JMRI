package jmri.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import jmri.util.JUnitUtil;
import jmri.util.web.MockServletExchange;

import org.junit.jupiter.api.*;

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
        MockServletExchange ctx = new MockServletExchange("GET", "/foo")
                .withContextPath("foo");

        instance.doGet(ctx.getRequest(), ctx.getResponse());

        assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, ctx.getResponseStatus(), "Temporary redirection");
        assertEquals("bar", ctx.getRedirectedUrl(), "Redirection URL");

        // test with unexpected path
        // (note web server is seriously broken if this occurs in the wild)
        MockServletExchange errorCtx = new MockServletExchange("GET", "/baz")
                .withContextPath("baz");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
            () -> instance.doGet(errorCtx.getRequest(), errorCtx.getResponse()));

        assertEquals("Redirect URL must not be null", ex.getMessage(), "null URL");
    }

    @Test
    public void testPost() throws ServletException, IOException {
        MockServletExchange ctx = new MockServletExchange("GET", "/foo")
            .withContextPath("foo");

        RedirectionServlet instance = new RedirectionServlet("foo", "bar");
        instance.doGet(ctx.getRequest(), ctx.getResponse());

        assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, ctx.getResponseStatus(), "Temporary redirection");
        assertEquals("bar", ctx.getRedirectedUrl(), "Redirection URL");
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
