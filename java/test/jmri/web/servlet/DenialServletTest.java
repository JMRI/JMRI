package jmri.web.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import jmri.util.JUnitUtil;
import jmri.util.web.MockServletExchange;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import static jmri.web.servlet.ServletUtil.UTF8_TEXT_HTML;

import static org.mockito.Mockito.verify;

/**
 * Tests for the jmri.web.servlet.DenialServlet class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class DenialServletTest {

    @Test
    public void testCtor() {
        DenialServlet a = new DenialServlet();
        assertNotNull(a);
    }

    @Test
    public void testGet() throws ServletException, IOException {
        MockServletExchange ms = new MockServletExchange("GET", "");
        new DenialServlet().doGet(ms.getRequest(), ms.getResponse());
        verify(ms.getResponse()).setContentType(UTF8_TEXT_HTML); // Response is HTML
        verify(ms.getResponse()).sendError(HttpServletResponse.SC_FORBIDDEN); // Status is 403
    }

    @Test
    public void testPost() throws ServletException, IOException {
        MockServletExchange ms = new MockServletExchange("POST","");
        new DenialServlet().doPost(ms.getRequest(), ms.getResponse());
        verify(ms.getResponse()).setContentType(UTF8_TEXT_HTML); // Response is HTML
        verify(ms.getResponse()).sendError(HttpServletResponse.SC_FORBIDDEN); // Status is 403
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
