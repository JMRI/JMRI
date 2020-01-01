package jmri.web.servlet.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import jmri.InstanceManager;
import jmri.PowerManager;
import jmri.jmris.json.JsonServerPreferences;
import jmri.managers.DefaultPowerManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
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

    private MockServletContext context;
    private MockServletConfig config;
    private MockHttpServletRequest request;


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

    /**
     * Test doGet() with a passed in expected result and ID parameter.
     *
     * @throws java.io.IOException unexpected failure in test context
     * @throws javax.servlet.ServletException unexpected failure in test context
     */
    @Test
    public void testDoGetResultWithId() throws IOException, ServletException {
        MockHttpServletResponse response = new MockHttpServletResponse();
        JsonServlet instance = new MockJsonServlet();
        instance.init(config);
        ObjectNode result = new ObjectMapper().createObjectNode();
        // test a schema valid message with validation on
        result.put("type", "pong");
        request.setAttribute("result", result);
        request.addParameter("id", "42");
        instance.doGet(request, response);
        assertEquals("HTTP OK", HttpServletResponse.SC_OK, response.getStatus());
        assertEquals("Contains result", result.toString(), response.getContentAsString());
    }

    /**
     * Test doGet() with a request for power status with no version specified.
     *
     * @throws java.io.IOException unexpected failure in test context
     * @throws javax.servlet.ServletException unexpected failure in test context
     */
    @Test
    public void testDoGetPowerNoVersion() throws IOException, ServletException {
        InstanceManager.setDefault(PowerManager.class, new DefaultPowerManager());
        request.setRequestURI("/json/power");
        MockHttpServletResponse response = new MockHttpServletResponse();
        JsonServlet instance = new MockJsonServlet();
        instance.init(config);
        // test a schema valid message with validation on
        instance.doGet(request, response);
        assertEquals("HTTP OK", HttpServletResponse.SC_OK, response.getStatus());
        JsonNode node = new ObjectMapper().readTree(response.getContentAsString());
        assertTrue("Node is array", node.isArray());
        assertEquals("Array has 1 entry", 1, node.size());
    }

    /**
     * Test doGet() with a request for power status with version 5 specified.
     *
     * @throws java.io.IOException unexpected failure in test context
     * @throws javax.servlet.ServletException unexpected failure in test context
     */
    @Test
    public void testDoGetPowerVersion5() throws IOException, ServletException {
        InstanceManager.setDefault(PowerManager.class, new DefaultPowerManager());
        request.setRequestURI("/json/v5/power");
        MockHttpServletResponse response = new MockHttpServletResponse();
        JsonServlet instance = new MockJsonServlet();
        instance.init(config);
        // test a schema valid message with validation on
        instance.doGet(request, response);
        assertEquals("HTTP OK", HttpServletResponse.SC_OK, response.getStatus());
        JsonNode node = new ObjectMapper().readTree(response.getContentAsString());
        assertTrue("Node is array", node.isArray());
        assertEquals("Array has 1 entry", 1, node.size());
    }

    /**
     * Test doGet() with a request for power status with version 4 (not allowed) specified.
     *
     * @throws java.io.IOException unexpected failure in test context
     * @throws javax.servlet.ServletException unexpected failure in test context
     */
    @Test
    public void testDoGetPowerVersion4() throws IOException, ServletException {
        InstanceManager.setDefault(PowerManager.class, new DefaultPowerManager());
        request.setRequestURI("/json/v4/power");
        MockHttpServletResponse response = new MockHttpServletResponse();
        JsonServlet instance = new MockJsonServlet();
        instance.init(config);
        // test a schema valid message with validation on
        instance.doGet(request, response);
        assertEquals("HTTP Not Found", HttpServletResponse.SC_NOT_FOUND, response.getStatus());
        JsonNode node = new ObjectMapper().readTree(response.getContentAsString());
        assertTrue("Node is object", node.isObject());
        assertEquals("Node is error", JsonException.ERROR, node.path(JSON.TYPE).asText());
        JUnitAppender.assertWarnMessage("Requested type 'v4' unknown.");
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        InstanceManager.getDefault(JsonServerPreferences.class).setValidateServerMessages(true);
        context = new MockServletContext();
        config = new MockServletConfig(context);
        request = new MockHttpServletRequest(context);
        // set default request URI to expected context path
        request.setContextPath("/json");
        request.setRequestURI(request.getContextPath());
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
