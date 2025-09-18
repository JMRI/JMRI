package jmri.web.servlet.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.SensorManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.server.json.JsonServerPreferences;
import jmri.managers.DefaultPowerManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.power.JsonPowerServiceFactory;
import jmri.util.JUnitUtil;
import jmri.web.servlet.ServletUtil;
import jmri.util.JUnitAppender;

import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        assertEquals( HttpServletResponse.SC_OK, response.getStatus(), "HTTP OK");
        assertEquals( result.toString(), response.getContentAsString(), "Contains result");
        // test a schema invalid message with validation on
        response = new MockHttpServletResponse();
        result.put("type", "invalid-type");
        request.setAttribute("result", result);
        instance.doGet(request, response);
        assertEquals( HttpServletResponse.SC_INTERNAL_SERVER_ERROR, response.getStatus(), "HTTP Internal Error");
        assertNotEquals( result.toString(), response.getContentAsString(), "Does not contain result");
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
        assertEquals( HttpServletResponse.SC_OK, response.getStatus(), "HTTP OK");
        assertEquals( result.toString(), response.getContentAsString(), "Contains result");
    }

    /**
     * Test doGet() with a passed in expected result and a good ID parameter.
     *
     * @throws java.io.IOException unexpected failure in test context
     * @throws javax.servlet.ServletException unexpected failure in test context
     */
    @Test
    public void testDoGetPowerWithGoodId() throws IOException, ServletException {
        InstanceManager.setDefault(PowerManager.class, new DefaultPowerManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class)));
        request.setRequestURI("/json/power");
        MockHttpServletResponse response = new MockHttpServletResponse();
        JsonServlet instance = new MockJsonServlet();
        instance.init(config);
        // test a schema valid message with validation on
        request.addParameter("id", "42");
        instance.doGet(request, response);
        assertEquals( HttpServletResponse.SC_OK, response.getStatus(), "HTTP OK");
        JsonNode node = new ObjectMapper().readTree(response.getContentAsString());
        assertTrue( node.isObject(), "Node is object");
        assertEquals( JSON.LIST, node.path(JSON.TYPE).asText(), "Node type is list");
        assertTrue( node.path(JSON.DATA).isArray(), "Node data is array");
        assertEquals( 42, node.path(JSON.ID).asInt(), "Node ID is 42");
        assertEquals( 1, node.path(JSON.DATA).size(), "Node data has 1 entry");
    }

    /**
     * Test doGet() with a passed in expected result and a bad ID parameter.
     *
     * @throws java.io.IOException unexpected failure in test context
     * @throws javax.servlet.ServletException unexpected failure in test context
     */
    @Test
    public void testDoGetPowerWithBadId() throws IOException, ServletException {
        InstanceManager.setDefault(PowerManager.class, new DefaultPowerManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class)));
        request.setRequestURI("/json/power");
        MockHttpServletResponse response = new MockHttpServletResponse();
        JsonServlet instance = new MockJsonServlet();
        instance.init(config);
        // test a schema valid message with validation on
        request.addParameter("id", "bad-parameter");
        instance.doGet(request, response);
        assertEquals( HttpServletResponse.SC_OK, response.getStatus(), "HTTP OK");
        JsonNode node = new ObjectMapper().readTree(response.getContentAsString());
        assertTrue( node.isArray(), "Node is array");
        assertEquals( 1, node.size(), "Array has 1 entry");
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
        InstanceManager.setDefault(PowerManager.class, new DefaultPowerManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class)));
        request.setRequestURI("/json/power");
        MockHttpServletResponse response = new MockHttpServletResponse();
        JsonServlet instance = new MockJsonServlet();
        instance.init(config);
        // test a schema valid message with validation on
        instance.doGet(request, response);
        assertEquals( HttpServletResponse.SC_OK, response.getStatus(), "HTTP OK");
        JsonNode node = new ObjectMapper().readTree(response.getContentAsString());
        assertTrue( node.isArray(), "Node is array");
        assertEquals( 1, node.size(), "Array has 1 entry");
    }

    /**
     * Test doGet() with a request for power status with version 5 specified.
     *
     * @throws java.io.IOException unexpected failure in test context
     * @throws javax.servlet.ServletException unexpected failure in test context
     */
    @Test
    public void testDoGetPowerV5() throws IOException, ServletException {
        InstanceManager.setDefault(PowerManager.class, new DefaultPowerManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class)));
        request.setRequestURI("/json/v5/power");
        MockHttpServletResponse response = new MockHttpServletResponse();
        JsonServlet instance = new MockJsonServlet();
        instance.init(config);
        // test a schema valid message with validation on
        instance.doGet(request, response);
        assertEquals( HttpServletResponse.SC_OK, response.getStatus(), "HTTP OK");
        JsonNode node = new ObjectMapper().readTree(response.getContentAsString());
        assertTrue( node.isArray(), "Node is array");
        assertEquals( 1, node.size(), "Array has 1 entry");
    }

    /**
     * Test doGet() with a request for power status with version 4 (not allowed) specified.
     *
     * @throws java.io.IOException unexpected failure in test context
     * @throws javax.servlet.ServletException unexpected failure in test context
     */
    @Test
    public void testDoGetPowerV4() throws IOException, ServletException {
        InstanceManager.setDefault(PowerManager.class, new DefaultPowerManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class)));
        request.setRequestURI("/json/v4/power");
        MockHttpServletResponse response = new MockHttpServletResponse();
        JsonServlet instance = new MockJsonServlet();
        instance.init(config);
        // test a schema valid message with validation on
        instance.doGet(request, response);
        assertEquals( HttpServletResponse.SC_NOT_FOUND, response.getStatus(), "HTTP Not Found");
        JsonNode node = new ObjectMapper().readTree(response.getContentAsString());
        assertTrue( node.isObject(), "Node is object");
        assertEquals( JsonException.ERROR, node.path(JSON.TYPE).asText(), "Node is error");
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
        InstanceManager.setDefault(PowerManager.class, new DefaultPowerManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class)));
        assertEquals(PowerManager.UNKNOWN, InstanceManager.getDefault(PowerManager.class).getPower());
        request.setRequestURI("/json/v5/power");
        MockHttpServletResponse response = new MockHttpServletResponse();
        JsonServlet instance = new MockJsonServlet();
        instance.init(config);
        request.setContent("{\"state\":4}".getBytes(ServletUtil.UTF8));
        // test a schema valid message with validation on
        instance.doPost(request, response);
        assertEquals( HttpServletResponse.SC_OK, response.getStatus(), "HTTP OK");
        JsonNode node = new ObjectMapper().readTree(response.getContentAsString());
        assertTrue( node.isObject(), "Node is object");
        assertEquals( JsonPowerServiceFactory.POWER, node.path(JSON.TYPE).asText(), "Object is power");
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
        InstanceManager.setDefault(PowerManager.class, new DefaultPowerManager(InstanceManager.getDefault(InternalSystemConnectionMemo.class)));
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
        assertEquals( HttpServletResponse.SC_OK, response.getStatus(), "HTTP OK");
        JsonNode node = new ObjectMapper().readTree(response.getContentAsString());
        assertTrue( node.isObject(), "Node is object");
        assertEquals( JsonPowerServiceFactory.POWER, node.path(JSON.TYPE).asText(), "Object is power");
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
        assertEquals( HttpServletResponse.SC_OK, response.getStatus(), "HTTP OK");
        assertEquals( "text/html; charset=utf-8", response.getContentType(), "Content type is HTML");
    }

    /**
     * Test doGet() with a request for two sensors with version 5 specified.
     *
     * @throws java.io.IOException unexpected failure in test context
     * @throws javax.servlet.ServletException unexpected failure in test context
     */
    @Test
    public void testGetTwoSensorsV5() throws ServletException, IOException {
        SensorManager manager = InstanceManager.getDefault(SensorManager.class);
        manager.provide("IS1");
        manager.provide("IS2");
        // request list of sensors
        request.setRequestURI("/json/v5/sensor");
        MockHttpServletResponse response = new MockHttpServletResponse();
        JsonServlet instance = new MockJsonServlet();
        instance.init(config);
        instance.doGet(request, response);
        assertEquals( HttpServletResponse.SC_OK, response.getStatus(), "HTTP OK");
        JsonNode node = new ObjectMapper().readTree(response.getContentAsString());
        assertTrue( node.isArray(), "Node is array");
        assertEquals( 2, node.size(), "Array has 2 entries");
        assertEquals( "IS1", node.path(0).path(JSON.DATA).path(JSON.NAME).asText(), "Sensor 1");
        assertEquals( "IS2", node.path(1).path(JSON.DATA).path(JSON.NAME).asText(), "Sensor 2");
        // request sensor IS1
        request.setRequestURI("/json/v5/sensor/IS1");
        response = new MockHttpServletResponse();
        instance.doGet(request, response);
        assertEquals( HttpServletResponse.SC_OK, response.getStatus(), "HTTP OK");
        node = new ObjectMapper().readTree(response.getContentAsString());
        assertTrue( node.isObject(), "Node is object");
        assertEquals( 2, node.size(), "Node has 2 entries");
        assertEquals( "IS1", node.path(JSON.DATA).path(JSON.NAME).asText(), "Sensor 1");
        // request sensor IS3 (does not exist)
        request.setRequestURI("/json/v5/sensor/IS3");
        response = new MockHttpServletResponse();
        instance.doGet(request, response);
        assertEquals( HttpServletResponse.SC_NOT_FOUND, response.getStatus(), "HTTP Not Found");
        node = new ObjectMapper().readTree(response.getContentAsString());
        assertTrue( node.isObject(), "Node is object");
        assertEquals( JsonException.ERROR, node.path(JSON.TYPE).asText(), "Node is error");
    }

    /**
     * Test creating, changing, and deleting a sensor with version 5 specified.
     *
     * @throws java.io.IOException unexpected failure in test context
     * @throws javax.servlet.ServletException unexpected failure in test context
     */
    @Test
    public void testGetCreateAndDeleteSensorV5() throws ServletException, IOException {
        SensorManager manager = InstanceManager.getDefault(SensorManager.class);
        // create sensor IS3
        request.setRequestURI("/json/v5/sensor");
        request.setContentType(ServletUtil.APPLICATION_JSON);
        request.setCharacterEncoding(ServletUtil.UTF8);
        request.setContent("{\"name\":\"IS3\"}".getBytes(ServletUtil.UTF8));
        MockHttpServletResponse response = new MockHttpServletResponse();
        JsonServlet instance = new MockJsonServlet();
        instance.init(config);
        instance.doPut(request, response);
        assertEquals( HttpServletResponse.SC_OK, response.getStatus(), "HTTP OK");
        JsonNode node = new ObjectMapper().readTree(response.getContentAsString());
        assertTrue( node.isObject(), "Node is object");
        assertEquals( 2, node.size(), "Object has 2 parameters");
        assertEquals( "IS3", node.path(JSON.DATA).path(JSON.NAME).asText(), "Sensor 3");
        assertEquals( 0, node.path(JSON.DATA).path(JSON.STATE).asInt(), "Unknown state");
        // modify sensor IS3
        request.setRequestURI("/json/v5/sensor/IS3");
        request.setContent("{\"state\":4}".getBytes(ServletUtil.UTF8));
        response = new MockHttpServletResponse();
        instance.doPost(request, response);
        assertEquals( HttpServletResponse.SC_OK, response.getStatus(), "HTTP OK");
        node = new ObjectMapper().readTree(response.getContentAsString());
        assertTrue( node.isObject(), "Node is object");
        assertEquals( 2, node.size(), "Object has 2 parameters");
        assertEquals( "IS3", node.path(JSON.DATA).path(JSON.NAME).asText(), "Sensor 3");
        assertEquals( 4, node.path(JSON.DATA).path(JSON.STATE).asInt(), "Thrown state");
        // delete sensor IS3
        request.setRequestURI("/json/v5/sensor/IS3");
        request.setContentType("");
        response = new MockHttpServletResponse();
        instance.doDelete(request, response);
        assertEquals( HttpServletResponse.SC_OK, response.getStatus(), "HTTP Not Found");
        assertEquals( 0, response.getContentLength(), "HTTP content is empty");
        assertNull( manager.getBySystemName("IS3"), "No sensor");
    }

    /**
     * Test getting, changing, and deleting an unknown type with version 5 specified.
     *
     * @throws java.io.IOException unexpected failure in test context
     * @throws javax.servlet.ServletException unexpected failure in test context
     */
    @Test
    public void testV5ManipulateUnknownType() throws ServletException, IOException {
        request.setRequestURI("/json/v5/invalid-type/invalid-name");
        request.setContentType("");
        MockHttpServletResponse response = new MockHttpServletResponse();
        JsonServlet instance = new MockJsonServlet();
        instance.init(config);
        instance.doGet(request, response);
        assertEquals( HttpServletResponse.SC_NOT_FOUND, response.getStatus(), "HTTP Not Found");
        JsonNode node = new ObjectMapper().readTree(response.getContentAsString());
        assertTrue( node.isObject(), "Node is object");
        assertEquals( JsonException.ERROR, node.path(JSON.TYPE).asText(), "Node is error");
        JUnitAppender.assertWarnMessage("Requested type 'invalid-type' unknown.");
        instance.doPost(request, response);
        assertEquals( HttpServletResponse.SC_NOT_FOUND, response.getStatus(), "HTTP Not Found");
        node = new ObjectMapper().readTree(response.getContentAsString());
        assertTrue( node.isObject(), "Node is object");
        assertEquals( JsonException.ERROR, node.path(JSON.TYPE).asText(), "Node is error");
        JUnitAppender.assertWarnMessage("Requested type 'invalid-type' unknown.");
        instance.doDelete(request, response);
        assertEquals( HttpServletResponse.SC_NOT_FOUND, response.getStatus(), "HTTP Not Found");
        node = new ObjectMapper().readTree(response.getContentAsString());
        assertTrue( node.isObject(), "Node is object");
        assertEquals( JsonException.ERROR, node.path(JSON.TYPE).asText(), "Node is error");
        JUnitAppender.assertWarnMessage("Requested type 'invalid-type' unknown.");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initInternalSensorManager();
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

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
    private static class MockJsonServlet extends JsonServlet {
        
        @Override
        void superInit() {
            // do nothing
        }
        
    }
}
