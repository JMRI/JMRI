package jmri.web.servlet.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import jmri.InstanceManager;
import jmri.jmris.json.JsonServerPreferences;
import jmri.util.JUnitUtil;
import jmri.util.JUnitAppender;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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
        assertNotNull(a);
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
        JsonServlet instance = new MockJsonServlet();
        instance.init(config);
        ObjectNode result = new ObjectMapper().createObjectNode();
        // test a schema valid message with validation on
        result.put("type", "pong");
        request.setAttribute("result", result);
        instance.doGet(request, response);
        assertEquals("HTTP OK", HttpServletResponse.SC_OK, response.getStatus());
        assertEquals("Contains result", result.toString(), response.getContentAsString());
        // test a schema invalid message with validation on
        response = new MockHttpServletResponse();
        result.put("type", "invalid-type");
        request.setAttribute("result", result);
        instance.doGet(request, response);
        assertEquals("HTTP Internal Error", HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response.getStatus());
        assertNotEquals("Does not contain result", result.toString(), response.getContentAsString());
        JUnitAppender.assertWarnMessage("Errors validating {\"type\":\"invalid-type\"}");
        JUnitAppender.assertWarnMessageStartingWith("JSON Validation Error: 1028");
        JUnitAppender.assertWarnMessageStartingWith("JSON Validation Error: 1008");
        JUnitAppender.assertWarnMessageStartingWith("JSON Validation Error: 1003");
        // test a schema invalid message with validation off
        InstanceManager.getDefault(JsonServerPreferences.class).setValidateServerMessages(false);
        response = new MockHttpServletResponse();
        result.put("type", "invalid-type");
        request.setAttribute("result", result);
        instance.doGet(request, response);
        assertEquals("HTTP OK", HttpServletResponse.SC_OK, response.getStatus());
        assertEquals("Contains result", result.toString(), response.getContentAsString());
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        InstanceManager.getDefault(JsonServerPreferences.class).setValidateServerMessages(true);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
    private static class MockJsonServlet extends JsonServlet {
        
        void superInit() {
            // do nothing
        }
        
    }
}
