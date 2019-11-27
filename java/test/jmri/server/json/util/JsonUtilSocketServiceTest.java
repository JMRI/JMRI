package jmri.server.json.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.awt.GraphicsEnvironment;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmris.json.JsonServerPreferences;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.panelEditor.PanelEditor;
import jmri.jmrit.display.switchboardEditor.SwitchboardEditor;
import jmri.profile.NullProfile;
import jmri.profile.Profile;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonMockConnection;
import jmri.util.JUnitUtil;
import jmri.web.server.WebServerPreferences;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rhwood
 */
public class JsonUtilSocketServiceTest {

    private Locale locale = Locale.ENGLISH;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetWindows(true, false); // list open windows when running tests
        JUnitUtil.resetNodeIdentity();
        JUnitUtil.resetProfileManager(new NullProfile("JsonUtilHttpServiceTest", "12345678", folder.newFolder(Profile.PROFILE)));
        JUnitUtil.initConfigureManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }

    /**
     * Test of onMessage method, of class JsonUtilSocketService. Tests only
     * responses that are expected to be consistent between a
     *
     * @throws java.lang.Exception if an exception unexpected in the context of
     *                                 these tests occurs
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
        instance.onMessage(JSON.LOCALE, empty, JSON.POST, locale, 42);
        Assert.assertNull(connection.getMessage()); // assert no reply
        // JSON.PING
        instance.onMessage(JSON.PING, empty, JSON.POST, locale, 42);
        message = connection.getMessage();
        Assert.assertNotNull("message is not null", message);
        JsonNode result = message.path(JSON.TYPE);
        Assert.assertNotNull(result);
        Assert.assertTrue(JsonNode.class.isInstance(result));
        Assert.assertEquals(JSON.PONG, result.asText());
        Assert.assertTrue(message.path(JSON.DATA).isMissingNode());
        // JSON.RAILROAD
        WebServerPreferences wsp = InstanceManager.getDefault(WebServerPreferences.class);
        instance.onMessage(JSON.RAILROAD, empty, JSON.GET, locale, 42);
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
            instance.onMessage(JSON.NETWORK_SERVICE, message, JSON.GET, locale, 42);
            Assert.fail("Expected exception not thrown");
        } catch (JsonException ex) {
            Assert.assertEquals("HTTP Not Found", 404, ex.getCode());
            Assert.assertEquals("Error Message", "Unable to access networkService _jmri-json._tcp.local..",
                    ex.getMessage());
        }
        // JSON.GOODBYE
        instance.onMessage(JSON.GOODBYE, empty, JSON.POST, locale, 42);
        message = connection.getMessage();
        Assert.assertNotNull("message is not null", message);
        result = message.path(JSON.TYPE);
        Assert.assertNotNull(result);
        Assert.assertTrue(JsonNode.class.isInstance(result));
        Assert.assertEquals(JSON.GOODBYE, result.asText());
        Assert.assertTrue(message.path(JSON.DATA).isMissingNode());
    }

    /**
     * Test of onMessage method, of class JsonUtilSocketService. Tests PANEL
     * JSON type if not running headless.
     *
     * @throws java.lang.Exception if an exception unexpected in the context of
     *                                 these tests occurs
     */
    @Test
    public void testOnMessagePanels() throws Exception {
        Assume.assumeFalse("Needs GUI", GraphicsEnvironment.isHeadless());
        Editor editor = new SwitchboardEditor("json test switchboard");
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode empty = connection.getObjectMapper().createObjectNode();
        JsonUtilSocketService instance = new JsonUtilSocketService(connection);
        try {
            instance.onMessage(JSON.PANELS, empty, JSON.GET, locale, 42);
            Assert.fail("Expected exception not thrown");
        } catch (JsonException ex) {
            Assert.assertEquals("HTTP Not Found", 404, ex.getCode());
            Assert.assertEquals("Error Message", "Unable to access panel .",
                    ex.getMessage());
        }
        JsonNode data = connection.getObjectMapper().createObjectNode().put(JSON.NAME, "Switchboard/json%20test%20switchboard");
        instance.onMessage(JSON.PANEL, data, JSON.GET, locale, 42);
        
        JUnitUtil.dispose(editor.getTargetFrame());
        JUnitUtil.dispose(editor);
    }

    /**
     * Test of onList method, of class JsonUtilSocketService. Does not test
     * CONFIG_PROFILE JSON type, see {@link #testOnListConfigProfile()} for
     * that. Does not test PANEL JSON type, see {@link #testOnListPanels()} for
     * that.
     *
     * @throws java.lang.Exception if an exception unexpected in the context of
     *                                 these tests occurs
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
        instance.onList(JSON.METADATA, empty, locale, 42);
        Assert.assertEquals(helper.getMetadata(locale, 42), connection.getMessage());
        instance.onList(JSON.NETWORK_SERVICES, empty, locale, 42);
        Assert.assertEquals(helper.getNetworkServices(locale, 42), connection.getMessage());
        instance.onList(JSON.SYSTEM_CONNECTIONS, empty, locale, 42);
        Assert.assertEquals(helper.getSystemConnections(locale, 42), connection.getMessage());
    }

    /**
     * Test of onList method for CONFIG_PROFILE JSON type, of class
     * JsonUtilSocketService.
     *
     * @throws java.lang.Exception if an exception unexpected in the context of
     *                                 these tests occurs
     */
    @Test
    public void testOnListConfigProfile() throws Exception {
        Locale locale = Locale.ENGLISH;
        ObjectMapper mapper = new ObjectMapper();
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode empty = connection.getObjectMapper().createObjectNode();
        JsonUtilSocketService instance = new JsonUtilSocketService(connection);
        JsonUtilHttpService helper = new JsonUtilHttpService(mapper);
        InstanceManager.getDefault(JsonServerPreferences.class).setHeartbeatInterval(10);
        instance.onList(JSON.CONFIG_PROFILES, empty, locale, 42);
        Assert.assertEquals(helper.getConfigProfiles(locale, 42), connection.getMessage());
    }

    /**
     * Test of onList method, of class JsonUtilSocketService. Tests PANEL JSON
     * type if not running headless.
     *
     * @throws java.lang.Exception if an exception unexpected in the context of
     *                                 these tests occurs
     */
    @Test
    public void testOnListPanels() throws Exception {
        Assume.assumeFalse("Needs GUI", GraphicsEnvironment.isHeadless());
        Editor switchboard = new SwitchboardEditor("json test switchboard");
        Editor controlPanel = new ControlPanelEditor("json test control panel");
        Editor layoutPanel = new LayoutEditor("json test layout panel");
        Editor panel = new PanelEditor("json test panel");
        Editor disabled = new PanelEditor("disabled json test panel");
        disabled.setAllowInFrameServlet(false);
        // 5 editors should return array of 4 since one is barred
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode empty = connection.getObjectMapper().createObjectNode();
        JsonUtilSocketService instance = new JsonUtilSocketService(connection);
        instance.onList(JSON.PANELS, empty, locale, 42);
        JsonNode message = connection.getMessage();
        Assert.assertNotNull("Message is not null", message);
        Assert.assertTrue("Message is array", message.isArray());
        if (message.size() != 4) {
            log.error(message.toString()); // what panel was left in place that triggered this?
        }
        Assert.assertEquals("Array has four elements", 4, message.size());
        JUnitUtil.dispose(switchboard.getTargetFrame());
        JUnitUtil.dispose(switchboard);
        JUnitUtil.dispose(controlPanel.getTargetFrame());
        JUnitUtil.dispose(controlPanel);
        JUnitUtil.dispose(layoutPanel.getTargetFrame());
        JUnitUtil.dispose(layoutPanel);
        JUnitUtil.dispose(panel.getTargetFrame());
        JUnitUtil.dispose(panel);
    }

    @Test
    public void testRRNameListener() throws IOException, JmriException, JsonException {
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode empty = connection.getObjectMapper().createObjectNode();
        TestJsonUtilHttpService httpService = new TestJsonUtilHttpService(connection.getObjectMapper());
        JsonUtilSocketService instance = new JsonUtilSocketService(connection, httpService);
        WebServerPreferences preferences = InstanceManager.getDefault(WebServerPreferences.class);
        Assert.assertEquals("No preferences listener", 0, preferences.getPropertyChangeListeners().length);
        instance.onMessage(JSON.RAILROAD, empty, JSON.GET, locale, 42);
        JsonNode message = connection.getMessage();
        Assert.assertNotNull("Message is not null", message);
        Assert.assertEquals("Message has RR Name", preferences.getRailroadName(),
                message.path(JSON.DATA).path(JSON.NAME).asText());
        Assert.assertEquals("There is a preferences listener", 1, preferences.getPropertyChangeListeners().length);
        preferences.setRailroadName("New Name");
        message = connection.getMessage();
        Assert.assertNotNull("Message is not null", message);
        Assert.assertEquals("Message has RR Name", preferences.getRailroadName(),
                message.path(JSON.DATA).path(JSON.NAME).asText());
        // force JsonException
        httpService.setThrowException(true);
        preferences.setRailroadName("Another New Name");
        message = connection.getMessage();
        Assert.assertNotNull("Message is not null", message);
        Assert.assertEquals("Message is error", JsonException.ERROR, message.path(JSON.TYPE).asText());
        Assert.assertEquals("Error code is 499", 499, message.path(JSON.DATA).path(JsonException.CODE).asInt());
        // force IOException
        Assert.assertEquals("There is a preferences listener", 1, preferences.getPropertyChangeListeners().length);
        connection.setThrowIOException(true);
        preferences.setRailroadName("Yet Another New Name");
        Assert.assertEquals("There is no longer a preferences listener", 0,
                preferences.getPropertyChangeListeners().length);
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

    private static class TestJsonUtilHttpService extends JsonUtilHttpService {

        private boolean throwException = false;

        public TestJsonUtilHttpService(ObjectMapper mapper) {
            super(mapper);
        }

        @Override
        public JsonNode doGet(String type, String name, JsonNode data, Locale locale, int id) throws JsonException {
            if (throwException) {
                throwException = false;
                throw new JsonException(499, "Mock Exception", id);
            }
            return super.doGet(type, name, data, locale, id);
        }

        public void setThrowException(boolean throwException) {
            this.throwException = throwException;
        }

    }

    private static final Logger log = LoggerFactory.getLogger(JsonUtilSocketServiceTest.class);
}
