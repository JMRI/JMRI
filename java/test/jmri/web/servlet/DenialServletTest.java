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

import static jmri.web.servlet.ServletUtil.UTF8_TEXT_HTML;

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
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        new DenialServlet().doGet(request, response);
        assertEquals( UTF8_TEXT_HTML, response.getContentType(), "Response is HTML");
        assertEquals( HttpServletResponse.SC_FORBIDDEN, response.getStatus(), "Status is 403");
    }
    
    @Test
    public void testPost() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        new DenialServlet().doPost(request, response);
        assertEquals( UTF8_TEXT_HTML, response.getContentType(), "Response is HTML");
        assertEquals( HttpServletResponse.SC_FORBIDDEN, response.getStatus(), "Status is 403");
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
