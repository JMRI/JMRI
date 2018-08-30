package jmri.server.json.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.Metadata;
import jmri.Version;
import jmri.jmris.json.JsonServerPreferences;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.switchboardEditor.SwitchboardEditor;
import jmri.profile.NullProfile;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.schema.JsonSchemaServiceCache;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;
import jmri.util.node.NodeIdentity;
import jmri.util.zeroconf.ZeroConfService;
import jmri.web.server.WebServerPreferences;

import org.junit.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rhwood
 */
public class JsonUtilHttpServiceTest {

    public JsonUtilHttpServiceTest() {
    }

    @Before
    public void setUp() throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager(new NullProfile("JsonUtilHttpServiceTest", "12345678", FileUtil.getFile("program:test")));
        JUnitUtil.initConnectionConfigManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
        ZeroConfService.stopAll();
    }

    /**
     * Test of doGet method, of class JsonUtilHttpService.
     *
     * @throws jmri.server.json.JsonException if test fails in an unexpected
     *                                        manner
     */
    @Test
    public void testDoGet() throws JsonException {
        Locale locale = Locale.ENGLISH;
        ObjectMapper mapper = new ObjectMapper();
        JsonUtilHttpService instance = new JsonUtilHttpService(mapper);
        InstanceManager.getDefault(JsonServerPreferences.class).setHeartbeatInterval(10);
        Assert.assertEquals(instance.getHello(locale, 10), instance.doGet(JSON.HELLO, null, locale));
        Assert.assertEquals(instance.getMetadata(locale, Metadata.JMRIVERCANON), instance.doGet(JSON.METADATA, Metadata.JMRIVERCANON, locale));
        Assert.assertEquals(instance.getMetadata(locale), instance.doGet(JSON.METADATA, null, locale));
        Assert.assertEquals(instance.getNode(locale), instance.doGet(JSON.NODE, null, locale));
        Assert.assertEquals(instance.getNetworkServices(locale), instance.doGet(JSON.NETWORK_SERVICE, null, locale));
        Assert.assertEquals(instance.getNetworkServices(locale), instance.doGet(JSON.NETWORK_SERVICES, null, locale));
        JsonException exception = null;
        try {
            instance.doGet(JSON.NETWORK_SERVICE, JSON.ZEROCONF_SERVICE_TYPE, locale);
        } catch (JsonException ex) {
            exception = ex;
        }
        Assert.assertNotNull(exception);
        Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, exception.getCode());
        exception = null;
        try {
            instance.doGet("INVALID TYPE TOKEN", null, locale);
        } catch (JsonException ex) {
            exception = ex;
        }
        Assert.assertNotNull(exception);
        Assert.assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getCode());
        ZeroConfService service = ZeroConfService.create(JSON.ZEROCONF_SERVICE_TYPE, 9999);
        JUnitUtil.waitFor(() -> {
            return service.isPublished() == false;
        });
        service.publish();
        Assume.assumeTrue("Published ZeroConf Service", JUnitUtil.waitFor(() -> {
            return service.isPublished() == true;
        }));
        Assert.assertEquals(instance.getNetworkService(locale, JSON.ZEROCONF_SERVICE_TYPE), instance.doGet(JSON.NETWORK_SERVICE, JSON.ZEROCONF_SERVICE_TYPE, locale));
        service.stop();
        JUnitUtil.waitFor(() -> {
            return service.isPublished() == false;
        });
    }

    /**
     * Test of doGetList method, of class JsonUtilHttpService. Verifies that
     * JSON types that are lists are reported the same if requested using GET or
     * LIST methods.
     *
     * @throws jmri.server.json.JsonException if test fails in an unexpected
     *                                        manner
     */
    @Test
    public void testDoGetList() throws JsonException {
        Locale locale = Locale.ENGLISH;
        ObjectMapper mapper = new ObjectMapper();
        JsonUtilHttpService instance = new JsonUtilHttpService(mapper);
        InstanceManager.getDefault(JsonServerPreferences.class).setHeartbeatInterval(10);
        Assert.assertEquals(instance.getMetadata(locale), instance.doGetList(JSON.METADATA, locale));
        Assert.assertEquals(instance.getNetworkServices(locale), instance.doGetList(JSON.NETWORK_SERVICES, locale));
        Assert.assertEquals(instance.getSystemConnections(locale), instance.doGetList(JSON.SYSTEM_CONNECTIONS, locale));
        Assert.assertEquals(instance.getConfigProfiles(locale), instance.doGetList(JSON.CONFIG_PROFILES, locale));
    }

    /**
     * Test of doPost method, of class JsonUtilHttpService. Verifies that POST
     * and GET requests for HELLO message are the same.
     *
     * @throws jmri.server.json.JsonException if fails unexpectedly
     */
    @Test
    public void testDoPost() throws JsonException {
        Locale locale = Locale.ENGLISH;
        ObjectMapper mapper = new ObjectMapper();
        JsonUtilHttpService instance = new JsonUtilHttpService(mapper);
        String type = JSON.HELLO;
        String name = JSON.HELLO;
        Assert.assertEquals(instance.doGet(type, name, locale), instance.doPost(type, name, null, locale));
    }

    /**
     * Test of getHello method, of class JsonUtilHttpService.
     *
     * @throws jmri.server.json.JsonException if messages are not schema valid
     */
    @Test
    public void testGetHello() throws JsonException {
        Locale locale = Locale.ENGLISH;
        int heartbeat = 1000; // one second
        ObjectMapper mapper = new ObjectMapper();
        JsonUtilHttpService instance = new JsonUtilHttpService(mapper);
        JsonNode result = instance.getHello(locale, heartbeat);
        JsonSchemaServiceCache cache = InstanceManager.getDefault(JsonSchemaServiceCache.class);
        cache.validateMessage(result, true, locale);
        Assert.assertEquals("Hello type", JSON.HELLO, result.path(JSON.TYPE).asText());
        JsonNode data = result.path(JSON.DATA);
        Assert.assertEquals("JMRI Version", Version.name(), data.path(JSON.JMRI).asText());
        Assert.assertEquals("JSON Version", JSON.JSON_PROTOCOL_VERSION, data.path(JSON.JSON).asText());
        Assert.assertEquals("Heartbeat", Math.round(heartbeat * 0.9f), data.path(JSON.HEARTBEAT).asInt());
        Assert.assertEquals("RR Name", InstanceManager.getDefault(WebServerPreferences.class).getRailroadName(), data.path(JSON.RAILROAD).asText());
        Assert.assertEquals("Node Identity", NodeIdentity.identity(), data.path(JSON.NODE).asText());
        Profile profile = ProfileManager.getDefault().getActiveProfile();
        Assert.assertNotNull(profile);
        Assert.assertEquals("Profile", profile.getName(), data.path(JSON.ACTIVE_PROFILE).asText());
        Assert.assertEquals("Message has 2 elements", 2, result.size());
        Assert.assertEquals("Message data has 6 elements", 6, data.size());
    }

    /**
     * Test of getMetadata method, of class JsonUtilHttpService.
     *
     * @throws jmri.server.json.JsonException if messages are not schema valid
     */
    @Test
    public void testGetMetadata_Locale_String() throws JsonException {
        Locale locale = Locale.ENGLISH;
        ObjectMapper mapper = new ObjectMapper();
        JsonUtilHttpService instance = new JsonUtilHttpService(mapper);
        JsonNode result;
        JsonSchemaServiceCache cache = InstanceManager.getDefault(JsonSchemaServiceCache.class);
        for (String metadata : Metadata.getSystemNameList()) {
            result = instance.getMetadata(locale, metadata);
            cache.validateMessage(result, true, locale);
            Assert.assertEquals(JSON.METADATA, result.path(JSON.TYPE).asText());
            Assert.assertEquals(metadata, result.path(JSON.DATA).path(JSON.NAME).asText());
            Assert.assertEquals(Metadata.getBySystemName(metadata), result.path(JSON.DATA).path(JSON.VALUE).asText());
        }
        JsonException exception = null;
        try {
            instance.getMetadata(locale, "invalid_metadata_entry");
        } catch (JsonException ex) {
            exception = ex;
        }
        Assert.assertNotNull(exception);
    }

    /**
     * Test of getMetadata method, of class JsonUtilHttpService.
     *
     * @throws jmri.server.json.JsonException if messages are not schema valid
     */
    @Test
    public void testGetMetadata_Locale() throws JsonException {
        Locale locale = Locale.ENGLISH;
        ObjectMapper mapper = new ObjectMapper();
        JsonUtilHttpService instance = new JsonUtilHttpService(mapper);
        JsonNode result = instance.getMetadata(locale);
        JsonSchemaServiceCache cache = InstanceManager.getDefault(JsonSchemaServiceCache.class);
        cache.validateMessage(result, true, locale);
        Assert.assertNotNull(result);
        Assert.assertEquals(Metadata.getSystemNameList().size(), result.size());
    }

    /**
     * Test of getNetworkServices method, of class JsonUtilHttpService.
     *
     * @throws jmri.server.json.JsonException if messages are not schema valid
     */
    @Test
    public void testGetNetworkServices() throws JsonException {
        Locale locale = Locale.ENGLISH;
        ObjectMapper mapper = new ObjectMapper();
        JsonUtilHttpService instance = new JsonUtilHttpService(mapper);
        // no services published
        JsonNode result = instance.getNetworkServices(locale);
        JsonSchemaServiceCache cache = InstanceManager.getDefault(JsonSchemaServiceCache.class);
        cache.validateMessage(result, true, locale);
        Assert.assertEquals(0, result.size());
        // publish a service
        ZeroConfService service = ZeroConfService.create(JSON.ZEROCONF_SERVICE_TYPE, 9999);
        JUnitUtil.waitFor(() -> {
            return service.isPublished() == false;
        });
        service.publish();
        Assume.assumeTrue("Published ZeroConf Service", JUnitUtil.waitFor(() -> {
            return service.isPublished() == true;
        }));
        result = instance.getNetworkServices(locale);
        cache = InstanceManager.getDefault(JsonSchemaServiceCache.class);
        cache.validateMessage(result, true, locale);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(JSON.NETWORK_SERVICE, result.get(0).path(JSON.TYPE).asText());
        JsonNode data = result.get(0).path(JSON.DATA);
        Assert.assertFalse(data.isMissingNode());
        Assert.assertEquals(InstanceManager.getDefault(WebServerPreferences.class).getRailroadName(), data.path(JSON.NAME).asText());
        Assert.assertEquals(9999, data.path(JSON.PORT).asInt());
        Assert.assertEquals(JSON.ZEROCONF_SERVICE_TYPE, data.path(JSON.TYPE).asText());
        Assert.assertEquals(NodeIdentity.identity(), data.path(JSON.NODE).asText());
        Assert.assertEquals(Metadata.getBySystemName(Metadata.JMRIVERCANON), data.path("jmri").asText());
        Assert.assertEquals(Metadata.getBySystemName(Metadata.JMRIVERSION), data.path("version").asText());
        service.stop();
        JUnitUtil.waitFor(() -> {
            return service.isPublished() == false;
        });
    }

    /**
     * Test of getNode method, of class JsonUtilHttpService.
     *
     * @throws jmri.server.json.JsonException if messages are not schema valid
     */
    @Test
    public void testGetNode() throws JsonException {
        Locale locale = Locale.ENGLISH;
        ObjectMapper mapper = new ObjectMapper();
        JsonUtilHttpService instance = new JsonUtilHttpService(mapper);
        JsonNode result = instance.getNode(locale);
        JsonSchemaServiceCache cache = InstanceManager.getDefault(JsonSchemaServiceCache.class);
        cache.validateMessage(result, true, locale);
        // We should have a single node with no history of nodes
        Assert.assertEquals(JSON.NODE, result.path(JSON.TYPE).asText());
        Assert.assertEquals(NodeIdentity.identity(), result.path(JSON.DATA).path(JSON.NODE).asText());
        Assert.assertEquals(0, result.path(JSON.DATA).path(JSON.FORMER_NODES).size());
    }

    /**
     * Test of getSystemConnections method, of class JsonUtilHttpService.
     *
     * @throws jmri.server.json.JsonException if messages are not schema valid
     */
    @Test
    public void testGetSystemConnections() throws JsonException {
        Locale locale = Locale.ENGLISH;
        ObjectMapper mapper = new ObjectMapper();
        JsonUtilHttpService instance = new JsonUtilHttpService(mapper);
        JsonNode result = instance.getSystemConnections(locale);
        JsonSchemaServiceCache cache = InstanceManager.getDefault(JsonSchemaServiceCache.class);
        cache.validateMessage(result, true, locale);
        // We should only have one internal connection
        Assert.assertEquals(1, result.size());
        JsonNode connection = result.get(0);
        Assert.assertEquals(JSON.SYSTEM_CONNECTION, connection.path(JSON.TYPE).asText());
        Assert.assertEquals("I", connection.path(JSON.DATA).path(JSON.PREFIX).asText());
        Assert.assertTrue(connection.path(JSON.DATA).path(JSON.NAME).isNull());
        Assert.assertTrue(connection.path(JSON.DATA).path(JSON.MFG).isNull());
    }

    /**
     * Test of getNetworkService method, of class JsonUtilHttpService.
     *
     * @throws jmri.server.json.JsonException if test fails in an unexpected
     *                                        manner
     */
    @Test
    public void testGetNetworkService() throws JsonException {
        Locale locale = Locale.ENGLISH;
        ObjectMapper mapper = new ObjectMapper();
        JsonUtilHttpService instance = new JsonUtilHttpService(mapper);
        JsonNode result = null;
        // non-existant service
        JsonException exception = null;
        try {
            result = instance.getNetworkService(locale, "non-existant-service"); // NOI18N
        } catch (JsonException ex) {
            exception = ex;
        }
        Assert.assertNull(result);
        Assert.assertNotNull(exception);
        Assert.assertEquals(HttpServletResponse.SC_NOT_FOUND, exception.getCode());
        // published service
        ZeroConfService service = ZeroConfService.create(JSON.ZEROCONF_SERVICE_TYPE, 9999);
        JUnitUtil.waitFor(() -> {
            return service.isPublished() == false;
        });
        service.publish();
        Assume.assumeTrue("Published ZeroConf Service", JUnitUtil.waitFor(() -> {
            return service.isPublished() == true;
        }));
        result = instance.getNetworkService(locale, JSON.ZEROCONF_SERVICE_TYPE);
        JsonSchemaServiceCache cache = InstanceManager.getDefault(JsonSchemaServiceCache.class);
        cache.validateMessage(result, true, locale);
        Assert.assertEquals(JSON.NETWORK_SERVICE, result.path(JSON.TYPE).asText());
        JsonNode data = result.path(JSON.DATA);
        Assert.assertFalse(data.isMissingNode());
        Assert.assertEquals(InstanceManager.getDefault(WebServerPreferences.class).getRailroadName(), data.path(JSON.NAME).asText());
        Assert.assertEquals(9999, data.path(JSON.PORT).asInt());
        Assert.assertEquals(JSON.ZEROCONF_SERVICE_TYPE, data.path(JSON.TYPE).asText());
        Assert.assertEquals(NodeIdentity.identity(), data.path(JSON.NODE).asText());
        Assert.assertEquals(Metadata.getBySystemName(Metadata.JMRIVERCANON), data.path("jmri").asText());
        Assert.assertEquals(Metadata.getBySystemName(Metadata.JMRIVERSION), data.path("version").asText());
        service.stop();
        JUnitUtil.waitFor(() -> {
            return service.isPublished() == false;
        });
    }

    /**
     * Test of getRailroad method, of class JsonUtilHttpService.
     *
     * @throws jmri.server.json.JsonException if messages are not schema valid
     */
    @Test
    public void testGetRailroad() throws JsonException {
        Locale locale = Locale.ENGLISH;
        ObjectMapper mapper = new ObjectMapper();
        JsonUtilHttpService instance = new JsonUtilHttpService(mapper);
        JsonNode result = instance.getRailroad(locale);
        JsonSchemaServiceCache cache = InstanceManager.getDefault(JsonSchemaServiceCache.class);
        cache.validateMessage(result, true, locale);
        Assert.assertEquals(JSON.RAILROAD, result.path(JSON.TYPE).asText());
        JsonNode data = result.path(JSON.DATA);
        Assert.assertEquals(InstanceManager.getDefault(WebServerPreferences.class).getRailroadName(), data.path(JSON.NAME).asText());
    }

    /**
     * Test of getPanel method, of class JsonUtilHttpService.
     *
     * @throws jmri.server.json.JsonException if the result cannot be validated
     */
    @Test
    public void testGetPanel() throws JsonException {
        Assume.assumeFalse("Needs GUI", GraphicsEnvironment.isHeadless());
        Locale locale = Locale.ENGLISH;
        Editor editor = new SwitchboardEditor("test");
        JsonUtilHttpService instance = new JsonUtilHttpService(new ObjectMapper());
        ObjectNode result = instance.getPanel(locale, editor, JSON.XML);
        JsonSchemaServiceCache cache = InstanceManager.getDefault(JsonSchemaServiceCache.class);
        cache.validateMessage(result, true, locale);
        editor.getTargetFrame().dispose();
        editor.dispose();
    }

    /**
     * Test of getPanels method, of class JsonUtilHttpService.
     *
     * @throws jmri.server.json.JsonException if the result cannot be validated
     */
    @Test
    public void testGetPanels_Locale_String() throws JsonException {
        Assume.assumeFalse("Needs GUI", GraphicsEnvironment.isHeadless());
        Locale locale = Locale.ENGLISH;
        Editor editor = new SwitchboardEditor("test");
        JsonUtilHttpService instance = new JsonUtilHttpService(new ObjectMapper());
        JsonNode result = instance.getPanels(locale, JSON.XML);
        JsonSchemaServiceCache cache = InstanceManager.getDefault(JsonSchemaServiceCache.class);
        cache.validateMessage(result, true, locale);
        editor.getTargetFrame().dispose();
        editor.dispose();
    }

    /**
     * Test of getPanels method, of class JsonUtilHttpService.
     *
     * @throws jmri.server.json.JsonException if the result cannot be validated
     */
    @Test
    public void testGetPanels_Locale() throws JsonException {
        Assume.assumeFalse("Needs GUI", GraphicsEnvironment.isHeadless());
        Locale locale = Locale.ENGLISH;
        Editor editor = new SwitchboardEditor("test");
        JsonUtilHttpService instance = new JsonUtilHttpService(new ObjectMapper());
        JsonNode result = instance.getPanels(locale);
        JsonSchemaServiceCache cache = InstanceManager.getDefault(JsonSchemaServiceCache.class);
        cache.validateMessage(result, true, locale);
        editor.getTargetFrame().dispose();
        editor.dispose();
    }

    /**
     * Test of getConfigProfiles method, of class JsonUtilHttpService. Only
     * tests that result is schema valid and contains all profiles.
     *
     * @throws jmri.server.json.JsonException if unable to read profiles
     */
    @Ignore // See Issue #5642
    @Test
    public void testGetConfigProfiles() throws JsonException {
        Locale locale = Locale.ENGLISH;
        ObjectMapper mapper = new ObjectMapper();
        JsonUtilHttpService instance = new JsonUtilHttpService(mapper);
        ArrayNode result = instance.getConfigProfiles(locale);
        JsonSchemaServiceCache cache = InstanceManager.getDefault(JsonSchemaServiceCache.class);
        cache.validateMessage(result, true, locale);
        Assert.assertEquals("Result has every profile", ProfileManager.getDefault().getProfiles().length, result.size());
    }

    /**
     * Test of addressForString method, of class JsonUtilHttpService.
     */
    @Test
    public void testAddressForString() {
        DccLocoAddress result = JsonUtilHttpService.addressForString("123(l)");
        Assert.assertTrue("Address is long", result.isLongAddress());
        Assert.assertEquals("Address is 123", 123, result.getNumber());
        result = JsonUtilHttpService.addressForString("123(L)");
        Assert.assertTrue("Address is long", result.isLongAddress());
        Assert.assertEquals("Address is 123", 123, result.getNumber());
        result = JsonUtilHttpService.addressForString("123(s)");
        Assert.assertFalse("Address is short", result.isLongAddress());
        Assert.assertEquals("Address is 123", 123, result.getNumber());
        result = JsonUtilHttpService.addressForString("123");
        Assert.assertFalse("Address is short", result.isLongAddress());
        Assert.assertEquals("Address is 123", 123, result.getNumber());
        result = JsonUtilHttpService.addressForString("3");
        Assert.assertFalse("Address is short", result.isLongAddress());
        Assert.assertEquals("Address is 3", 3, result.getNumber());
        result = JsonUtilHttpService.addressForString("3(l)");
        Assert.assertTrue("Address is long", result.isLongAddress());
        Assert.assertEquals("Address is 3", 3, result.getNumber());
    }

    // private final static Logger log = LoggerFactory.getLogger(JsonUtilHttpServiceTest.class);

}
