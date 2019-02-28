package jmri.web.servlet;

import static jmri.web.servlet.ServletUtil.UTF8_TEXT_HTML;

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
 * Tests for the jmri.web.servlet.DenialServlet class
 *
 * @author Paul Bender Copyright (C) 2012,2016
 */
public class DenialServletTest {

    @Test
    public void testCtor() {
        DenialServlet a = new DenialServlet();
        Assert.assertNotNull(a);
    }

    @Test
    public void testGet() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        new DenialServlet().doGet(request, response);
        Assert.assertEquals("Response is HTML", UTF8_TEXT_HTML, response.getContentType());
        Assert.assertEquals("Status is 403", HttpServletResponse.SC_FORBIDDEN, response.getStatus());
    }
    
    @Test
    public void testPost() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        new DenialServlet().doPost(request, response);
        Assert.assertEquals("Response is HTML", UTF8_TEXT_HTML, response.getContentType());
        Assert.assertEquals("Status is 403", HttpServletResponse.SC_FORBIDDEN, response.getStatus());
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
