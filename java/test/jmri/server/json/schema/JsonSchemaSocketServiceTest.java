package jmri.server.json.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonMockConnection;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonSchemaSocketServiceTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    /**
     * Test that schema are gettable, but not modifiable.
     * Note: This test will be skipped if json-schema.org is unreachable.
     *
     * @throws IOException   on unexpected exception
     * @throws JmriException on unexpected exception
     * @throws JsonException on unexpected exception
     */
    @Test
    public void testOnMessage() throws IOException, JmriException, JsonException {
        // this test triggers a process that tries to contact json-schema.org.
        // we first verify that host is reachable.
        boolean reachable = false;
        try{
          java.net.InetAddress inet = java.net.InetAddress.getByName("json-schema.org");
          reachable = inet.isReachable(5000);
        } catch(java.net.UnknownHostException uhe) {
          reachable = false;
        }
        // if the host isn't reachable, we're going to skip the test.
        Assume.assumeTrue(reachable);
        String type = JSON.SCHEMA;
        ObjectNode data = mapper.createObjectNode();
        Locale locale = Locale.ENGLISH;
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonSchemaSocketService instance = new JsonSchemaSocketService(connection);
        // DELETE should fail
        try {
            instance.onMessage(type, data, JSON.DELETE, locale);
            Assert.fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            Assert.assertEquals("Error is HTTP 405", 405, ex.getCode());
        }
        // GET without NAME returns JSON schema
        instance.onMessage(type, data, JSON.GET, locale);
        Assert.assertTrue("Returned array", connection.getMessage().isArray());
        Assert.assertEquals("Returned array has 2 elements", 2, connection.getMessage().size());
        Assert.assertEquals("Returned schema is \"json\"", "json", connection.getMessage().get(0).path(JSON.DATA).path(JSON.NAME).asText());
        Assert.assertTrue("Returned schema is for server", connection.getMessage().get(0).path(JSON.DATA).path(JSON.SERVER).asBoolean());
        Assert.assertEquals("Returned schema is \"json\"", "json", connection.getMessage().get(1).path(JSON.DATA).path(JSON.NAME).asText());
        Assert.assertFalse("Returned schema is for client", connection.getMessage().get(1).path(JSON.DATA).path(JSON.SERVER).asBoolean());
        // Suppress a warning message (see networknt/json-schema-validator#79)
        JUnitAppender.checkForMessageStartingWith("Unknown keyword exclusiveMinimum - you should define your own Meta Schema.");
        // GET with NAME returns the desired schema
        instance.onMessage(type, mapper.readTree("{\"name\":\"schema\"}"), JSON.GET, locale);
        Assert.assertTrue("Returned array", connection.getMessage().isArray());
        Assert.assertEquals("Returned array has 2 elements", 2, connection.getMessage().size());
        Assert.assertEquals("Returned schema is \"json\"", "schema", connection.getMessage().get(0).path(JSON.DATA).path(JSON.NAME).asText());
        Assert.assertTrue("Returned schema is for server", connection.getMessage().get(0).path(JSON.DATA).path(JSON.SERVER).asBoolean());
        Assert.assertEquals("Returned schema is \"json\"", "schema", connection.getMessage().get(1).path(JSON.DATA).path(JSON.NAME).asText());
        Assert.assertFalse("Returned schema is for client", connection.getMessage().get(1).path(JSON.DATA).path(JSON.SERVER).asBoolean());
        // GET with NAME and SERVER==true returns a single schema
        instance.onMessage(type, mapper.readTree("{\"name\":\"schema\", \"server\":true}"), JSON.GET, locale);
        Assert.assertTrue("Returned single object", connection.getMessage().isObject());
        Assert.assertEquals("Returned schema is \"json\"", "schema", connection.getMessage().path(JSON.DATA).path(JSON.NAME).asText());
        Assert.assertTrue("Returned schema is for server", connection.getMessage().path(JSON.DATA).path(JSON.SERVER).asBoolean());
        instance.onMessage(type, mapper.readTree("{\"name\":\"schema\", \"server\":false}"), JSON.GET, locale);
        Assert.assertTrue("Returned single object", connection.getMessage().isObject());
        Assert.assertEquals("Returned schema is \"json\"", "schema", connection.getMessage().path(JSON.DATA).path(JSON.NAME).asText());
        Assert.assertFalse("Returned schema is for client", connection.getMessage().path(JSON.DATA).path(JSON.SERVER).asBoolean());
        // GET with unknown NAME should fail
        try {
            instance.onMessage("invalid-type", data, JSON.GET, locale);
            Assert.fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            Assert.assertEquals("Error is HTTP 400", 400, ex.getCode());
        }
        // POST should fail
        try {
            instance.onMessage(type, data, JSON.POST, locale);
            Assert.fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            Assert.assertEquals("Error is HTTP 405", 405, ex.getCode());
        }
        // PUT should fail
        try {
            instance.onMessage(type, data, JSON.PUT, locale);
            Assert.fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            Assert.assertEquals("Error is HTTP 405", 405, ex.getCode());
        }
    }

    /**
     * Test that schema are not listed.
     *
     * @throws IOException   on unexpected exception
     * @throws JmriException on unexpected exception
     * @throws JsonException on unexpected exception
     */
    @Test
    public void testOnList() throws IOException, JmriException, JsonException {
        ObjectNode data = mapper.createObjectNode();
        Locale locale = Locale.ENGLISH;
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonSchemaSocketService instance = new JsonSchemaSocketService(connection);
        try {
            instance.onList(JSON.SCHEMA, data, locale);
            Assert.fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            Assert.assertEquals(HttpServletResponse.SC_BAD_REQUEST, ex.getCode());
        }
        instance.onList(JSON.TYPE, data, locale);
        Assert.assertTrue("Result is array", connection.getMessage().isArray());
        Assert.assertEquals("Result contains all types", InstanceManager.getDefault(JsonSchemaServiceCache.class).getTypes().size(), connection.getMessage().size());
    }

}
