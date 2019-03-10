package jmri.server.json.util;

import java.awt.GraphicsEnvironment;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jmri.InstanceManager;
import jmri.jmris.json.JsonServerPreferences;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.switchboardEditor.SwitchboardEditor;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonMockConnection;
import jmri.util.JUnitUtil;
import jmri.web.server.WebServerPreferences;

/**
 *
 * @author rhwood
 */
public class JsonUtilSocketServiceTest {

    @Before
    public void setUp() throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    /**
     * Test of onMessage method, of class JsonUtilSocketService. Tests only
     * responses that are expected to be consistent between a 
     *
     * @throws java.lang.Exception if an exception unexpected in the context of
     *                             these tests occurs
     */
    @Test
    public void testOnMessage() throws Exception {
        Locale locale = Locale.ENGLISH;
        JsonNode message;
        InstanceManager.getDefault(JsonServerPreferences.class).setValidateServerMessages(true);
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode empty = connection.getObjectMapper().createObjectNode();
        JsonUtilSocketService instance = new JsonUtilSocketService(connection);
        // JSON.LOCALE
        instance.onMessage(JSON.LOCALE, empty, JSON.POST, locale);
        Assert.assertNull(connection.getMessage()); // assert no reply
        // JSON.PING
        instance.onMessage(JSON.PING, empty, JSON.POST, locale);
        message = connection.getMessage();
        Assert.assertNotNull("message is not null", message);
        JsonNode result = message.path(JSON.TYPE);
        Assert.assertNotNull(result);
        Assert.assertTrue(JsonNode.class.isInstance(result));
        Assert.assertEquals(JSON.PONG, result.asText());
        Assert.assertTrue(message.path(JSON.DATA).isMissingNode());
        // JSON.RAILROAD
        WebServerPreferences wsp = InstanceManager.getDefault(WebServerPreferences.class);
        instance.onMessage(JSON.RAILROAD, empty, JSON.GET, locale);
        message = connection.getMessage();
        Assert.assertNotNull("message is not null", message);
        result = message.path(JSON.DATA);
        Assert.assertNotNull(result);
        Assert.assertEquals(JSON.RAILROAD, message.path(JSON.TYPE).asText());
        Assert.assertEquals("Railroad name matches", wsp.getRailroadName(), result.path(JSON.NAME).asText());
        wsp.setRailroadName("test railroad");
        message = connection.getMessage();
        Assert.assertNotNull("message is not null", message);
        result = message.path(JSON.DATA);
        Assert.assertNotNull(result);
        Assert.assertEquals(JSON.RAILROAD, message.path(JSON.TYPE).asText());
        Assert.assertEquals("Railroad name matches", wsp.getRailroadName(), result.path(JSON.NAME).asText());
        // JSON.NETWORK_SERVICE (should return 404 because not running the requested service)
        message = connection.getObjectMapper().createObjectNode().put(JSON.NAME, JSON.ZEROCONF_SERVICE_TYPE);
        try {
            instance.onMessage(JSON.NETWORK_SERVICE, message, JSON.GET, locale);
            Assert.fail("Expected exception not thrown");
        } catch (JsonException ex) {
            Assert.assertEquals("HTTP Not Found", 404, ex.getCode());
            Assert.assertEquals("Error Message", "Unable to access networkService _jmri-json._tcp.local..", ex.getMessage());
        }
        // JSON.GOODBYE
        instance.onMessage(JSON.GOODBYE, empty, JSON.POST, locale);
        message = connection.getMessage();
        Assert.assertNotNull("message is not null", message);
        result = message.path(JSON.TYPE);
        Assert.assertNotNull(result);
        Assert.assertTrue(JsonNode.class.isInstance(result));
        Assert.assertEquals(JSON.GOODBYE, result.asText());
        Assert.assertTrue(message.path(JSON.DATA).isMissingNode());
    }

    /**
     * Test of onMessage method, of class JsonUtilSocketService. Tests PANEL JSON type
     * if not running headless.
     *
     * @throws java.lang.Exception if an exception unexpected in the context of
     *                             these tests occurs
     */
    @Test
    public void testOnMessagePanels() throws Exception {
        Assume.assumeFalse("Needs GUI", GraphicsEnvironment.isHeadless());
        Editor editor = new SwitchboardEditor("test");
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode empty = connection.getObjectMapper().createObjectNode();
        JsonUtilSocketService instance = new JsonUtilSocketService(connection);
        instance.onMessage(JSON.PANELS, empty, JSON.GET, Locale.ENGLISH);
        JsonNode message = connection.getMessage();
        Assert.assertNotNull("Message is not null", message);
        Assert.assertTrue("Message is array", message.isArray());
        Assert.assertEquals("Array has one element", 1, message.size());
        editor.getTargetFrame().dispose();
        editor.dispose();
    }
    
    /**
     * Test of onList method, of class JsonUtilSocketService. Does not test CONFIG_PROFILE
     * JSON type, see {@link #testOnListConfigProfile()} for that. Does not test PANEL
     * JSON type, see {@link #testOnListPanels()} for that.
     *
     * @throws java.lang.Exception if an exception unexpected in the context of
     *                             these tests occurs
     */
    @Test
    public void testOnList() throws Exception {
        Locale locale = Locale.ENGLISH;
        ObjectMapper mapper = new ObjectMapper();
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode empty = connection.getObjectMapper().createObjectNode();
        JsonUtilSocketService instance = new JsonUtilSocketService(connection);
        JsonUtilHttpService helper = new JsonUtilHttpService(mapper);
        InstanceManager.getDefault(JsonServerPreferences.class).setHeartbeatInterval(10);
        instance.onList(JSON.METADATA, empty, locale);
        Assert.assertEquals(helper.getMetadata(locale), connection.getMessage());
        instance.onList(JSON.NETWORK_SERVICES, empty, locale);
        Assert.assertEquals(helper.getNetworkServices(locale), connection.getMessage());
        instance.onList(JSON.SYSTEM_CONNECTIONS, empty, locale);
        Assert.assertEquals(helper.getSystemConnections(locale), connection.getMessage());
    }

    /**
     * Test of onList method for CONFIG_PROFILE JSON type, of class JsonUtilSocketService.
     *
     * @throws java.lang.Exception if an exception unexpected in the context of
     *                             these tests occurs
     */
    @Test
    @Ignore("See Issue #5642")
    public void testOnListConfigProfile() throws Exception {
        Locale locale = Locale.ENGLISH;
        ObjectMapper mapper = new ObjectMapper();
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode empty = connection.getObjectMapper().createObjectNode();
        JsonUtilSocketService instance = new JsonUtilSocketService(connection);
        JsonUtilHttpService helper = new JsonUtilHttpService(mapper);
        InstanceManager.getDefault(JsonServerPreferences.class).setHeartbeatInterval(10);
        instance.onList(JSON.CONFIG_PROFILES, empty, locale);
        Assert.assertEquals(helper.getConfigProfiles(locale), connection.getMessage());
    }

    /**
     * Test of onList method, of class JsonUtilSocketService. Tests PANEL JSON type
     * if not running headless.
     *
     * @throws java.lang.Exception if an exception unexpected in the context of
     *                             these tests occurs
     */
    @Test
    public void testOnListPanels() throws Exception {
        Assume.assumeFalse("Needs GUI", GraphicsEnvironment.isHeadless());
        Editor editor = new SwitchboardEditor("test");
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode empty = connection.getObjectMapper().createObjectNode();
        JsonUtilSocketService instance = new JsonUtilSocketService(connection);
        instance.onList(JSON.PANELS, empty, Locale.ENGLISH);
        JsonNode message = connection.getMessage();
        Assert.assertNotNull("Message is not null", message);
        Assert.assertTrue("Message is array", message.isArray());
        Assert.assertEquals("Array has one element", 1, message.size());
        editor.getTargetFrame().dispose();
        editor.dispose();
    }

    
    /**
     * Test of onClose method, of class JsonUtilSocketService.
     */
    @Test
    public void testOnClose() {
        JsonUtilSocketService instance = new JsonUtilSocketService(new JsonMockConnection((DataOutputStream) null));
        try {
            instance.onClose();
        } catch (RuntimeException ex) {
            log.error("Unexpected exception", ex);
        }
    }

    private static final Logger log = LoggerFactory.getLogger(JsonUtilSocketServiceTest.class);
}
