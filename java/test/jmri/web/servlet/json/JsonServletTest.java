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
import jmri.JmriException;
import jmri.PowerManager;
import jmri.jmris.json.JsonServerPreferences;
import jmri.managers.DefaultPowerManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.power.JsonPowerServiceFactory;
import jmri.util.JUnitUtil;
import jmri.web.servlet.ServletUtil;
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
     * Test doGet() with a passed in expected result and a good ID parameter.
     *
     * @throws java.io.IOException unexpected failure in test context
     * @throws javax.servlet.ServletException unexpected failure in test context
     */
    @Test
    public void testDoGetPowerWithGoodId() throws IOException, ServletException {
        InstanceManager.setDefault(PowerManager.class, new DefaultPowerManager());
        request.setRequestURI("/json/power");
        MockHttpServletResponse response = new MockHttpServletResponse();
        JsonServlet instance = new MockJsonServlet();
        instance.init(config);
        // test a schema valid message with validation on
        request.addParameter("id", "42");
        instance.doGet(request, response);
        assertEquals("HTTP OK", HttpServletResponse.SC_OK, response.getStatus());
        JsonNode node = new ObjectMapper().readTree(response.getContentAsString());
        assertTrue("Node is object", node.isObject());
        assertEquals("Node type is list", JSON.LIST, node.path(JSON.TYPE).asText());
        assertTrue("Node data is array", node.path(JSON.DATA).isArray());
        assertEquals("Node ID is 42", 42, node.path(JSON.ID).asInt());
        assertEquals("Node data has 1 entry", 1, node.path(JSON.DATA).size());
    }

    /**
     * Test doGet() with a passed in expected result and a bad ID parameter.
     *
     * @throws java.io.IOException unexpected failure in test context
     * @throws javax.servlet.ServletException unexpected failure in test context
     */
    @Test
    public void testDoGetPowerWithBadId() throws IOException, ServletException {
        InstanceManager.setDefault(PowerManager.class, new DefaultPowerManager());
        request.setRequestURI("/json/power");
        MockHttpServletResponse response = new MockHttpServletResponse();
        JsonServlet instance = new MockJsonServlet();
        instance.init(config);
        // test a schema valid message with validation on
        request.addParameter("id", "bad-parameter");
        instance.doGet(request, response);
        assertEquals("HTTP OK", HttpServletResponse.SC_OK, response.getStatus());
        JsonNode node = new ObjectMapper().readTree(response.getContentAsString());
        assertTrue("Node is array", node.isArray());
        assertEquals("Array has 1 entry", 1, node.size());
        JUnitAppender.assertErrorMessage("Unable to parse JSON {\"id\":bad-parameter}");
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
    public void testDoGetPowerV5() throws IOException, ServletException {
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
    public void testDoGetPowerV4() throws IOException, ServletException {
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

    /**
     * Test doPost() with a request for power status with version 5 specified.
     *
     * @throws java.io.IOException unexpected failure in test context
     * @throws javax.servlet.ServletException unexpected failure in test context
     * @throws JmriException unexpected failure in test context
     */
    @Test
    public void testDoPostPowerV5Content() throws IOException, ServletException, JmriException {
        InstanceManager.setDefault(PowerManager.class, new DefaultPowerManager());
        assertEquals(PowerManager.UNKNOWN, InstanceManager.getDefault(PowerManager.class).getPower());
        request.setRequestURI("/json/v5/power");
        MockHttpServletResponse response = new MockHttpServletResponse();
        JsonServlet instance = new MockJsonServlet();
        instance.init(config);
        request.setContent("{\"state\":4}".getBytes(ServletUtil.UTF8));
        // test a schema valid message with validation on
        instance.doPost(request, response);
        assertEquals("HTTP OK", HttpServletResponse.SC_OK, response.getStatus());
        JsonNode node = new ObjectMapper().readTree(response.getContentAsString());
        assertTrue("Node is object", node.isObject());
        assertEquals("Object is power", JsonPowerServiceFactory.POWER, node.path(JSON.TYPE).asText());
        assertEquals(PowerManager.OFF, InstanceManager.getDefault(PowerManager.class).getPower());
    }

    /**
     * Test doPost() with a request for power status with version 5 specified.
     *
     * @throws java.io.IOException unexpected failure in test context
     * @throws javax.servlet.ServletException unexpected failure in test context
     * @throws JmriException unexpected failure in test context
     */
    @Test
    public void testDoPostPowerV5Parameters() throws IOException, ServletException, JmriException {
        InstanceManager.setDefault(PowerManager.class, new DefaultPowerManager());
        assertEquals(PowerManager.UNKNOWN, InstanceManager.getDefault(PowerManager.class).getPower());
        // content type must not be JSON to use parameters
        request.setContentType("");
        request.setRequestURI("/json/v5/power");
        request.setParameter(JSON.STATE, "4");
        MockHttpServletResponse response = new MockHttpServletResponse();
        JsonServlet instance = new MockJsonServlet();
        instance.init(config);
        // test a schema valid message with validation on
        instance.doPost(request, response);
        assertEquals("HTTP OK", HttpServletResponse.SC_OK, response.getStatus());
        JsonNode node = new ObjectMapper().readTree(response.getContentAsString());
        assertTrue("Node is object", node.isObject());
        assertEquals("Object is power", JsonPowerServiceFactory.POWER, node.path(JSON.TYPE).asText());
        assertEquals(PowerManager.OFF, InstanceManager.getDefault(PowerManager.class).getPower());
    }

    /**
     * Test doGet() with a request for nothing with version 5 specified.
     *
     * @throws java.io.IOException unexpected failure in test context
     * @throws javax.servlet.ServletException unexpected failure in test context
     */
    @Test
    public void testGetV5noParameters() throws ServletException, IOException {
        request.setRequestURI("/json/v5");
        MockHttpServletResponse response = new MockHttpServletResponse();
        JsonServlet instance = new MockJsonServlet();
        instance.init(config);
        // test a schema valid message with validation on
        instance.doGet(request, response);
        assertEquals("HTTP OK", HttpServletResponse.SC_OK, response.getStatus());
        assertEquals("Content type is HTML", "text/html; charset=utf-8", response.getContentType());
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
        request.setContentType(ServletUtil.APPLICATION_JSON);
        request.setCharacterEncoding(ServletUtil.UTF8);
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
