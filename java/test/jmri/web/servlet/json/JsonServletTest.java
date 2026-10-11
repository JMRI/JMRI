package jmri.web.servlet.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

import javax.servlet.ServletException;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.managers.DefaultPowerManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonServerPreferences;
import jmri.server.json.power.JsonPowerServiceFactory;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.web.MockServletExchange;

import org.junit.jupiter.api.*;

import javax.servlet.http.HttpServletResponse;

import jmri.web.servlet.ServletUtil;

import static jmri.util.web.MockServletExchange.DELETE;
import static jmri.util.web.MockServletExchange.GET;
import static jmri.util.web.MockServletExchange.POST;
import static jmri.util.web.MockServletExchange.PUT;

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

        JsonServlet instance = new MockJsonServlet();
        ObjectNode result = new ObjectMapper().createObjectNode();

        // test a schema valid message with validation on
        result.put("type", "pong");

        MockServletExchange validCtx = getJsonMockServletExchange(GET, "/json/test")
            .withAttribute("result", result);

        instance.init(validCtx.getConfig());

        instance.doGet(validCtx.getRequest(), validCtx.getResponse());
        assertEquals( HttpServletResponse.SC_OK, validCtx.getResponseStatus(), "HTTP OK");
        assertEquals( result.toString(), validCtx.getResponseContentAsString(), "Contains result");

        // test a schema invalid message with validation on
        result.put("type", "invalid-type");
        result.put("type", "invalid-type");
        MockServletExchange invalidCtx = getJsonMockServletExchange("GET", "/json/test")
            .withAttribute("result", result);
        instance.doGet(invalidCtx.getRequest(), invalidCtx.getResponse());
        assertEquals( HttpServletResponse.SC_INTERNAL_SERVER_ERROR, invalidCtx.getResponseStatus(), "HTTP Internal Error");
        assertNotEquals( result.toString(), invalidCtx.getResponseContentAsString(), "Does not contain result");
        JUnitAppender.assertWarnMessage("Errors validating {\"type\":\"invalid-type\"}");
        JUnitAppender.assertWarnMessageStartingWith("JSON Validation Error: 1028");
        JUnitAppender.assertWarnMessageStartingWith("JSON Validation Error: 1008");
        // As of 1.0.* this was code 1003 but by 1.3.3 it is 1029 - 
        JUnitAppender.assertWarnMessageStartingWith("JSON Validation Error: 1029");

        // test a schema invalid message with validation off
        InstanceManager.getDefault(JsonServerPreferences.class).setValidateServerMessages(false);
        result.put("type", "invalid-type");
        MockServletExchange valOffCtx = getJsonMockServletExchange(GET, "/json/test")
            .withAttribute("result", result);
        instance.doGet(valOffCtx.getRequest(), valOffCtx.getResponse());
        assertEquals( HttpServletResponse.SC_OK, valOffCtx.getResponseStatus(), "HTTP OK");
        assertEquals( result.toString(), valOffCtx.getResponseContentAsString(), "Contains result");
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
        MockServletExchange ctx = getJsonMockServletExchange(GET, "/json/power")
            .withParameter("id", "42");
        JsonServlet instance = new MockJsonServlet();
        instance.init(ctx.getConfig());

        // test a schema valid message with validation on
        instance.doGet(ctx.getRequest(), ctx.getResponse());
        assertEquals( HttpServletResponse.SC_OK, ctx.getResponseStatus(), "HTTP OK");
        JsonNode node = new ObjectMapper().readTree(ctx.getResponseContentAsString());
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

        // test a schema valid message with validation on
        MockServletExchange ctx = getJsonMockServletExchange(GET, "/json/power")
            .withParameter("id", "bad-parameter");
        JsonServlet instance = new MockJsonServlet();

        instance.init(ctx.getConfig());

        instance.doGet(ctx.getRequest(), ctx.getResponse());
        assertEquals( HttpServletResponse.SC_OK, ctx.getResponseStatus(), "HTTP OK");
        JsonNode node = new ObjectMapper().readTree(ctx.getResponseContentAsString());
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
        MockServletExchange ctx = getJsonMockServletExchange(GET, "/json/power");
        JsonServlet instance = new MockJsonServlet();
        instance.init(ctx.getConfig());
        // test a schema valid message with validation on
        instance.doGet(ctx.getRequest(), ctx.getResponse());
        assertEquals( HttpServletResponse.SC_OK, ctx.getResponseStatus(), "HTTP OK");
        JsonNode node = new ObjectMapper().readTree(ctx.getResponseContentAsString());
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
        MockServletExchange ctx = getJsonMockServletExchange(GET, "/json/v5/power");
        JsonServlet instance = new MockJsonServlet();
        instance.init(ctx.getConfig());
        // test a schema valid message with validation on
        instance.doGet(ctx.getRequest(), ctx.getResponse());
        assertEquals( HttpServletResponse.SC_OK, ctx.getResponseStatus(), "HTTP OK");
        JsonNode node = new ObjectMapper().readTree(ctx.getResponseContentAsString());
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

        MockServletExchange ctx = getJsonMockServletExchange(GET, "/json/v4/power");

        JsonServlet instance = new MockJsonServlet();
        instance.init(ctx.getConfig());
        // test a schema valid message with validation on
        instance.doGet(ctx.getRequest(), ctx.getResponse());
        assertEquals(HttpServletResponse.SC_NOT_FOUND, ctx.getResponseStatus(), "HTTP Not Found");

        JsonNode node = new ObjectMapper().readTree(ctx.getResponseContentAsString());
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

        MockServletExchange ctx = getJsonMockServletExchange(POST, "/json/v5/power")
                .withBody("{\"state\":4}");

        JsonServlet instance = new MockJsonServlet();
        instance.init(ctx.getConfig());

        // test a schema valid message with validation on
        instance.doPost(ctx.getRequest(), ctx.getResponse());

        assertEquals(HttpServletResponse.SC_OK, ctx.getResponseStatus(), "HTTP OK");
        JsonNode node = new ObjectMapper().readTree(ctx.getResponseContentAsString());

        assertTrue(node.isObject(), "Node is object");
        assertEquals(JsonPowerServiceFactory.POWER, node.path(JSON.TYPE).asText(), "Object is power");
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

        MockServletExchange ctx = getJsonMockServletExchange(POST, "/json/v5/power")
        // content type must not be JSON to use parameters
                .withRequestContentType("")
                .withParameter(JSON.STATE, "4");

        JsonServlet instance = new MockJsonServlet();
        instance.init(ctx.getConfig());
        // test a schema valid message with validation on
        instance.doPost(ctx.getRequest(), ctx.getResponse());

        assertEquals(HttpServletResponse.SC_OK, ctx.getResponseStatus(), "HTTP OK");
        JsonNode node = new ObjectMapper().readTree(ctx.getResponseContentAsString());
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

        MockServletExchange ctx = getJsonMockServletExchange(GET, "/json/v5");
        JsonServlet instance = new MockJsonServlet();
        instance.init(ctx.getConfig());

        // test a schema valid message with validation on
        instance.doGet(ctx.getRequest(), ctx.getResponse());

        assertEquals(HttpServletResponse.SC_OK, ctx.getResponseStatus(), "HTTP OK");
        assertEquals("text/html; charset=utf-8", ctx.getResponseContentType(), "Content type is HTML");
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

        JsonServlet instance = new MockJsonServlet();
        ObjectMapper mapper = new ObjectMapper();

        // request list of sensors
        MockServletExchange listCtx = getJsonMockServletExchange(GET, "/json/v5/sensor");
        instance.init(listCtx.getConfig());
        instance.doGet(listCtx.getRequest(), listCtx.getResponse());

        assertEquals(HttpServletResponse.SC_OK, listCtx.getResponseStatus(), "HTTP OK");
        JsonNode node = mapper.readTree(listCtx.getResponseContentAsString());
        assertTrue( node.isArray(), "Node is array");
        assertEquals( 2, node.size(), "Array has 2 entries");
        assertEquals( "IS1", node.path(0).path(JSON.DATA).path(JSON.NAME).asText(), "Sensor 1");
        assertEquals( "IS2", node.path(1).path(JSON.DATA).path(JSON.NAME).asText(), "Sensor 2");

        // request sensor IS1
        MockServletExchange getOneCtx = getJsonMockServletExchange("GET", "/json/v5/sensor/IS1");
        instance.doGet(getOneCtx.getRequest(), getOneCtx.getResponse());

        assertEquals(HttpServletResponse.SC_OK, getOneCtx.getResponseStatus(), "HTTP OK");
        node = mapper.readTree(getOneCtx.getResponseContentAsString());
        assertTrue( node.isObject(), "Node is object");
        assertEquals( 2, node.size(), "Node has 2 entries");
        assertEquals( "IS1", node.path(JSON.DATA).path(JSON.NAME).asText(), "Sensor 1");

        // request sensor IS3 (does not exist)
        MockServletExchange getMissingCtx = getJsonMockServletExchange("GET", "/json/v5/sensor/IS3");
        instance.doGet(getMissingCtx.getRequest(), getMissingCtx.getResponse());

        assertEquals(HttpServletResponse.SC_NOT_FOUND, getMissingCtx.getResponseStatus(), "HTTP Not Found");
        node = mapper.readTree(getMissingCtx.getResponseContentAsString());
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

        JsonServlet instance = new MockJsonServlet();
        ObjectMapper mapper = new ObjectMapper();

        // create sensor IS3
        assertNull(manager.getBySystemName("IS3"), "Sensor does not exist");
        MockServletExchange putCtx = getJsonMockServletExchange(PUT, "/json/v5/sensor")
            .withBody("{\"name\":\"IS3\"}");
        instance.init(putCtx.getConfig());
        instance.doPut(putCtx.getRequest(), putCtx.getResponse());

        assertEquals(HttpServletResponse.SC_OK, putCtx.getResponseStatus(), "HTTP OK");

        JsonNode node = mapper.readTree(putCtx.getResponseContentAsString());
        assertTrue( node.isObject(), "Node is object");
        assertEquals( 2, node.size(), "Object has 2 parameters");
        assertEquals( "IS3", node.path(JSON.DATA).path(JSON.NAME).asText(), "Sensor 3");
        assertEquals( 0, node.path(JSON.DATA).path(JSON.STATE).asInt(), "Unknown state");
        Sensor is3 = manager.getBySystemName("IS3");
        assertNotNull(is3, "Sensor exists");
        assertEquals(Sensor.UNKNOWN, is3.getCommandedState(), "Sensor state unknown");


        // modify sensor IS3
        MockServletExchange postCtx = getJsonMockServletExchange(POST, "/json/v5/sensor/IS3")
            .withBody("{\"state\":4}");
        instance.doPost(postCtx.getRequest(), postCtx.getResponse());

        assertEquals(HttpServletResponse.SC_OK, postCtx.getResponseStatus(), "HTTP OK");
        node = mapper.readTree(postCtx.getResponseContentAsString());
        assertTrue( node.isObject(), "Node is object");
        assertEquals( 2, node.size(), "Object has 2 parameters");
        assertEquals( "IS3", node.path(JSON.DATA).path(JSON.NAME).asText(), "Sensor 3");
        assertEquals( 4, node.path(JSON.DATA).path(JSON.STATE).asInt(), "Thrown state");
        assertEquals(Sensor.INACTIVE, is3.getCommandedState(), "Sensor state changed");


        // delete sensor IS3
        MockServletExchange deleteCtx = getJsonMockServletExchange(DELETE, "/json/v5/sensor/IS3");
        instance.doDelete(deleteCtx.getRequest(), deleteCtx.getResponse());

        assertEquals(HttpServletResponse.SC_OK, deleteCtx.getResponseStatus(), "HTTP OK");
        assertEquals(0, deleteCtx.getResponseContentAsString().length(), "Response body should be empty");
        assertNull( manager.getBySystemName("IS3"), "No Sensor");
    }

    /**
     * Test getting, changing, and deleting an unknown type with version 5 specified.
     *
     * @throws java.io.IOException unexpected failure in test context
     * @throws javax.servlet.ServletException unexpected failure in test context
     */
    @Test
    public void testV5ManipulateUnknownType() throws ServletException, IOException {

        ObjectMapper mapper = new ObjectMapper();
        String uri = "/json/v5/invalid-type/invalid-name";

        // GET unknown type
        MockServletExchange getCtx = getJsonMockServletExchange(GET, uri)
            .withRequestContentType("");
        JsonServlet instance = new MockJsonServlet();
        instance.init(getCtx.getConfig());
        instance.doGet(getCtx.getRequest(), getCtx.getResponse());

        assertEquals(HttpServletResponse.SC_NOT_FOUND, getCtx.getResponseStatus(), "HTTP Not Found");
        JsonNode node = mapper.readTree(getCtx.getResponseContentAsString());
        assertTrue( node.isObject(), "Node is object");
        assertEquals( JsonException.ERROR, node.path(JSON.TYPE).asText(), "Node is error");
        JUnitAppender.assertWarnMessage("Requested type 'invalid-type' unknown.");

        // POST unknown type
        MockServletExchange postCtx = getJsonMockServletExchange(POST, uri)
            .withRequestContentType("");
        instance.doPost(postCtx.getRequest(), postCtx.getResponse());

        assertEquals(HttpServletResponse.SC_NOT_FOUND, postCtx.getResponseStatus(), "HTTP Not Found");
        node = mapper.readTree(postCtx.getResponseContentAsString());
        assertTrue( node.isObject(), "Node is object");
        assertEquals( JsonException.ERROR, node.path(JSON.TYPE).asText(), "Node is error");
        JUnitAppender.assertWarnMessage("Requested type 'invalid-type' unknown.");

        // DELETE unknown type
        MockServletExchange deleteCtx = getJsonMockServletExchange(DELETE, uri)
            .withRequestContentType("");
        instance.doDelete(deleteCtx.getRequest(), deleteCtx.getResponse());

        assertEquals(HttpServletResponse.SC_NOT_FOUND, deleteCtx.getResponseStatus(), "HTTP Not Found");
        node = mapper.readTree(deleteCtx.getResponseContentAsString());
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

    private MockServletExchange getJsonMockServletExchange(String method, String uri) {
        return new MockServletExchange(method, uri)
            .withRequestContentType(ServletUtil.APPLICATION_JSON)
            .withContextPath("/json");
    }

}
