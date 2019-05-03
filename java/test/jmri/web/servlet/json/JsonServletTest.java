package jmri.web.servlet.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;

/**
 * Tests for the jmri.web.servlet.json.JsonServlet class
 *
 * @author Paul Bender Copyright (C) 2012, 2016
 * @author Randall Wood 2018
 */
public class JsonServletTest {

    @Test
    public void testCtor() {
        JsonServlet a = new JsonServlet();
        Assert.assertNotNull(a);
    }

    /**
     * Test doGet() with a passed in expected result.
     *
     * @throws java.io.IOException unexpected failure in test context
     * @throws javax.servlet.ServletException unexpected failure in test context
     */
    @Test
    public void testDoGetResult() throws IOException, ServletException {
        MockServletContext context = new MockServletContext();
        MockServletConfig config = new MockServletConfig(context);
        MockHttpServletRequest request = new MockHttpServletRequest(context);
        MockHttpServletResponse response = new MockHttpServletResponse();
        JsonServlet instance = Mockito.spy(new JsonServlet());
        Mockito.doNothing().when(instance).superInit();
        instance.init(config);
        ObjectNode result = new ObjectMapper().createObjectNode();
        result.put("foo", "bar");
        request.setAttribute("result", result);
        instance.doGet(request, response);
        Assert.assertEquals("HTTP OK", HttpServletResponse.SC_OK, response.getStatus());
        Assert.assertEquals("Contains result", result.toString(), response.getContentAsString());
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
