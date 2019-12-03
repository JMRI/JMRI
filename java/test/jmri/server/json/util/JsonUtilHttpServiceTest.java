package jmri.server.json.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.GraphicsEnvironment;

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
import jmri.server.json.JsonHttpServiceTestBase;
import jmri.util.JUnitUtil;
import jmri.util.node.NodeIdentity;
import jmri.util.zeroconf.ZeroConfService;
import jmri.web.server.WebServerPreferences;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonUtilHttpServiceTest extends JsonHttpServiceTestBase<JsonUtilHttpService> {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        service = new JsonUtilHttpService(mapper);
        JUnitUtil.resetWindows(true, false); // list open windows when running tests
        JUnitUtil.resetNodeIdentity();
        JUnitUtil.resetProfileManager(new NullProfile("JsonUtilHttpServiceTest", "12345678", folder.newFolder(Profile.PROFILE)));
        JUnitUtil.initConnectionConfigManager();
        JUnitUtil.initZeroConfServiceManager();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        JUnitUtil.resetZeroConfServiceManager();
        JUnitUtil.clearShutDownManager();
        super.tearDown();
    }

    /**
     * Test of doGet method, of class JsonUtilHttpService.
     *
     * @throws jmri.server.json.JsonException if test fails in an unexpected
     *                                        manner
     */
    @Test
    public void testDoGet() throws JsonException {
        InstanceManager.getDefault(JsonServerPreferences.class).setHeartbeatInterval(10);
        assertEquals(service.getHello(locale, 10, 42), service.doGet(JSON.HELLO, null, NullNode.getInstance(), locale, 42));
        assertEquals(service.getMetadata(locale, Metadata.JMRIVERCANON, 42), service.doGet(JSON.METADATA, Metadata.JMRIVERCANON, NullNode.getInstance(), locale, 42));
        assertEquals(service.getMetadata(locale, 42), service.doGet(JSON.METADATA, null, NullNode.getInstance(), locale, 42));
        assertEquals(service.getNode(locale, 42), service.doGet(JSON.NODE, null, NullNode.getInstance(), locale, 42));
        assertEquals(service.getNetworkServices(locale, 42), service.doGet(JSON.NETWORK_SERVICE, null, NullNode.getInstance(), locale, 42));
        assertEquals(service.getNetworkServices(locale, 42), service.doGet(JSON.NETWORK_SERVICES, null, NullNode.getInstance(), locale, 42));
        try {
            service.doGet(JSON.NETWORK_SERVICE, JSON.ZEROCONF_SERVICE_TYPE,
                    NullNode.getInstance(), locale, 42);
            fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            assertEquals(404, ex.getCode());
        }
        try {
            service.doGet("INVALID TYPE TOKEN", null, NullNode.getInstance(), locale, 42);
            fail("Expected exception not thrown.");
        } catch (JsonException ex) {
            assertEquals(500, ex.getCode());
        }
        ZeroConfService zcs = ZeroConfService.create(JSON.ZEROCONF_SERVICE_TYPE, 9999);
        JUnitUtil.waitFor(() -> {
            return zcs.isPublished() == false;
        });
        zcs.publish();
        Assume.assumeTrue("Published ZeroConf Service", JUnitUtil.waitFor(() -> {
            return zcs.isPublished() == true;
        }));
        assertEquals(service.getNetworkService(locale, JSON.ZEROCONF_SERVICE_TYPE, 42), service.doGet(JSON.NETWORK_SERVICE, JSON.ZEROCONF_SERVICE_TYPE,
                        NullNode.getInstance(), locale, 42));
        zcs.stop();
        JUnitUtil.waitFor(() -> {
            return zcs.isPublished() == false;
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
        InstanceManager.getDefault(JsonServerPreferences.class).setHeartbeatInterval(10);
        assertEquals(service.getMetadata(locale, 42), service.doGetList(JSON.METADATA, NullNode.getInstance(), locale, 42));
        assertEquals(service.getNetworkServices(locale, 42), service.doGetList(JSON.NETWORK_SERVICES, NullNode.getInstance(), locale, 42));
        assertEquals(service.getSystemConnections(locale, 42), service.doGetList(JSON.SYSTEM_CONNECTIONS, NullNode.getInstance(), locale, 42));
        assertEquals(service.getConfigProfiles(locale, 42), service.doGetList(JSON.CONFIG_PROFILES, NullNode.getInstance(), locale, 42));
    }

    /**
     * Test of doPost method, of class JsonUtilHttpService. Verifies that POST
     * and GET requests for HELLO message are the same.
     *
     * @throws jmri.server.json.JsonException if fails unexpectedly
     */
    @Test
    public void testDoPost() throws JsonException {
        String type = JSON.HELLO;
        String name = JSON.HELLO;
        assertEquals(service.doGet(type, name, NullNode.getInstance(), locale, 42), service.doPost(type, name, NullNode.getInstance(), locale, 42));
    }

    /**
     * Test of getHello method, of class JsonUtilHttpService.
     *
     * @throws jmri.server.json.JsonException if messages are not schema valid
     */
    @Test
    public void testGetHello() throws JsonException {
        int heartbeat = 1000; // one second
        JsonNode result = service.getHello(locale, heartbeat, 0);
        validate(result);
        assertEquals("Hello type", JSON.HELLO, result.path(JSON.TYPE).asText());
        JsonNode data = result.path(JSON.DATA);
        assertEquals("JMRI Version", Version.name(), data.path(JSON.JMRI).asText());
        assertEquals("JSON Version", JSON.JSON_PROTOCOL_VERSION, data.path(JSON.JSON).asText());
        assertEquals("Heartbeat", Math.round(heartbeat * 0.9f), data.path(JSON.HEARTBEAT).asInt());
        assertEquals("RR Name", InstanceManager.getDefault(WebServerPreferences.class).getRailroadName(), data.path(JSON.RAILROAD).asText());
        assertEquals("Node Identity", NodeIdentity.networkIdentity(), data.path(JSON.NODE).asText());
        Profile profile = ProfileManager.getDefault().getActiveProfile();
        assertNotNull(profile);
        assertEquals("Profile", profile.getName(), data.path(JSON.ACTIVE_PROFILE).asText());
        assertEquals("Message has 2 elements", 2, result.size());
        assertEquals("Message data has 6 elements", 6, data.size());
        result = service.getHello(locale, heartbeat, 42);
        validate(result);
        data = result.path(JSON.DATA);
        assertEquals("Message has 2 elements", 3, result.size());
        assertEquals("Message data has 6 elements", 6, data.size());
    }

    /**
     * Test of getMetadata method, of class JsonUtilHttpService.
     *
     * @throws jmri.server.json.JsonException if messages are not schema valid
     */
    @Test
    public void testGetMetadata_Locale_String() throws JsonException {
        JsonNode result;
        for (String metadata : Metadata.getSystemNameList()) {
            result = service.getMetadata(locale, metadata, 42);
            validate(result);
            assertEquals(JSON.METADATA, result.path(JSON.TYPE).asText());
            assertEquals(metadata, result.path(JSON.DATA).path(JSON.NAME).asText());
            assertEquals(Metadata.getBySystemName(metadata), result.path(JSON.DATA).path(JSON.VALUE).asText());
        }
        try {
            service.getMetadata(locale, "invalid_metadata_entry", 42);
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertEquals(404, ex.getCode());
        }
    }

    /**
     * Test of getMetadata method, of class JsonUtilHttpService.
     *
     * @throws jmri.server.json.JsonException if messages are not schema valid
     */
    @Test
    public void testGetMetadata_Locale() throws JsonException {
        JsonNode result = service.getMetadata(locale, 42);
        validate(result);
        assertEquals(Metadata.getSystemNameList().size(), result.size());
    }

    /**
     * Test of getNetworkServices method, of class JsonUtilHttpService.
     *
     * @throws jmri.server.json.JsonException if messages are not schema valid
     */
    @Test
    public void testGetNetworkServices() throws JsonException {
        // no services published
        JsonNode result = service.getNetworkServices(locale, 42);
        validate(result);
        assertEquals(0, result.size());
        // publish a service
        ZeroConfService zcs = ZeroConfService.create(JSON.ZEROCONF_SERVICE_TYPE, 9999);
        JUnitUtil.waitFor(() -> {
            return zcs.isPublished() == false;
        });
        zcs.publish();
        Assume.assumeTrue("Published ZeroConf Service", JUnitUtil.waitFor(() -> {
            return zcs.isPublished() == true;
        }));
        result = service.getNetworkServices(locale, 42);
        validate(result);
        assertEquals(1, result.size());
        assertEquals(JSON.NETWORK_SERVICE, result.get(0).path(JSON.TYPE).asText());
        JsonNode data = result.get(0).path(JSON.DATA);
        assertFalse(data.isMissingNode());
        assertEquals(InstanceManager.getDefault(WebServerPreferences.class).getRailroadName(), data.path(JSON.USERNAME).asText());
        assertEquals(9999, data.path(JSON.PORT).asInt());
        assertEquals(JSON.ZEROCONF_SERVICE_TYPE, data.path(JSON.TYPE).asText());
        assertEquals(NodeIdentity.networkIdentity(), data.path(JSON.NODE).asText());
        assertEquals(Metadata.getBySystemName(Metadata.JMRIVERCANON), data.path("jmri").asText());
        assertEquals(Metadata.getBySystemName(Metadata.JMRIVERSION), data.path("version").asText());
        zcs.stop();
        JUnitUtil.waitFor(() -> {
            return zcs.isPublished() == false;
        });
    }

    /**
     * Test of getNode method, of class JsonUtilHttpService.
     *
     * @throws jmri.server.json.JsonException if messages are not schema valid
     */
    @Test
    public void testGetNode() throws JsonException {
        JsonNode result = service.getNode(locale, 42);
        validate(result);
        // We should have a single node with no history of nodes
        // There are 3 "former" IDs when there is no history
        assertEquals(JSON.NODE, result.path(JSON.TYPE).asText());
        assertEquals(NodeIdentity.networkIdentity(), result.path(JSON.DATA).path(JSON.NODE).asText());
        JsonNode nodes = result.path(JSON.DATA).path(JSON.FORMER_NODES);
        assertTrue(nodes.isArray());
        assertEquals(3, nodes.size());
    }

    /**
     * Test of getSystemConnection method, of class JsonUtilHttpService.
     *
     * @throws jmri.server.json.JsonException if connection name not found
     */
    @Test
    public void testGetSystemConnection() throws JsonException {
        JsonNode result = service.getSystemConnection(locale, "Internal", 42);
        validate(result);
        // We should get back type, data and id
        assertEquals(3, result.size());
        // Data should exist and have at least 4 elements
        assertTrue(result.path(JSON.DATA).size() >= 4);
        assertEquals(JSON.SYSTEM_CONNECTION, result.path(JSON.TYPE).asText());
        assertEquals("I", result.path(JSON.DATA).path(JSON.PREFIX).asText());
        assertEquals("Internal", result.path(JSON.DATA).path(JSON.NAME).asText());
        assertTrue(result.path(JSON.DATA).path(JSON.MFG).isNull());
    }

    /**
     * Test of getSystemConnections method, of class JsonUtilHttpService.
     *
     * @throws jmri.server.json.JsonException if messages are not schema valid
     */
    @Test
    public void testGetSystemConnections() throws JsonException {
        JsonNode result = service.getSystemConnections(locale, 42);
        validate(result);
        // We should only have one internal connection
        assertEquals(1, result.size());
        JsonNode connection = result.get(0);
        assertEquals(JSON.SYSTEM_CONNECTION, connection.path(JSON.TYPE).asText());
        assertEquals("I", connection.path(JSON.DATA).path(JSON.PREFIX).asText());
        assertEquals("Internal", connection.path(JSON.DATA).path(JSON.NAME).asText());
        assertTrue(connection.path(JSON.DATA).path(JSON.MFG).isNull());
    }

    /**
     * Test of getNetworkService method, of class JsonUtilHttpService.
     *
     * @throws jmri.server.json.JsonException if test fails in an unexpected
     *                                        manner
     */
    @Test
    public void testGetNetworkService() throws JsonException {
        JsonNode result = null;
        // non-existent service
        try {
            result = service.getNetworkService(locale, "non-existant-service", 42); // NOI18N
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertEquals(HttpServletResponse.SC_NOT_FOUND, ex.getCode());
        }
        // published service
        ZeroConfService zcs = ZeroConfService.create(JSON.ZEROCONF_SERVICE_TYPE, 9999);
        JUnitUtil.waitFor(() -> {
            return zcs.isPublished() == false;
        });
        zcs.publish();
        Assume.assumeTrue("Published ZeroConf Service", JUnitUtil.waitFor(() -> {
            return zcs.isPublished() == true;
        }));
        result = service.getNetworkService(locale, JSON.ZEROCONF_SERVICE_TYPE, 42);
        validate(result);
        assertEquals(JSON.NETWORK_SERVICE, result.path(JSON.TYPE).asText());
        JsonNode data = result.path(JSON.DATA);
        assertFalse(data.isMissingNode());
        assertEquals(InstanceManager.getDefault(WebServerPreferences.class).getRailroadName(), data.path(JSON.USERNAME).asText());
        assertEquals(9999, data.path(JSON.PORT).asInt());
        assertEquals(JSON.ZEROCONF_SERVICE_TYPE, data.path(JSON.TYPE).asText());
        assertEquals(NodeIdentity.networkIdentity(), data.path(JSON.NODE).asText());
        assertEquals(Metadata.getBySystemName(Metadata.JMRIVERCANON), data.path("jmri").asText());
        assertEquals(Metadata.getBySystemName(Metadata.JMRIVERSION), data.path("version").asText());
        zcs.stop();
        JUnitUtil.waitFor(() -> {
            return zcs.isPublished() == false;
        });
    }

    /**
     * Test of getRailroad method, of class JsonUtilHttpService.
     *
     * @throws jmri.server.json.JsonException if messages are not schema valid
     */
    @Test
    public void testGetRailroad() throws JsonException {
        JsonNode result = service.getRailroad(locale, 42);
        validate(result);
        assertEquals(JSON.RAILROAD, result.path(JSON.TYPE).asText());
        JsonNode data = result.path(JSON.DATA);
        assertEquals(InstanceManager.getDefault(WebServerPreferences.class).getRailroadName(), data.path(JSON.NAME).asText());
    }

    /**
     * Test of getPanel method, of class JsonUtilHttpService.
     *
     * @throws jmri.server.json.JsonException if the result cannot be validated
     */
    @Test
    public void testGetPanel() throws JsonException {
        Assume.assumeFalse("Needs GUI", GraphicsEnvironment.isHeadless());
        Editor editor = new SwitchboardEditor("test");
        ObjectNode result = service.getPanel(editor, JSON.XML, 42);
        validate(result);
        JUnitUtil.dispose(editor.getTargetFrame());
        JUnitUtil.dispose(editor);
    }

    /**
     * Test of getPanels method, of class JsonUtilHttpService.
     *
     * @throws jmri.server.json.JsonException if the result cannot be validated
     */
    @Test
    public void testGetPanels_Locale_String() throws JsonException {
        Assume.assumeFalse("Needs GUI", GraphicsEnvironment.isHeadless());
        Editor editor = new SwitchboardEditor("test");
        JsonNode result = service.getPanels(JSON.XML, 42);
        validate(result);
        JUnitUtil.dispose(editor.getTargetFrame());
        JUnitUtil.dispose(editor);
    }

    /**
     * Test of getPanels method, of class JsonUtilHttpService.
     *
     * @throws jmri.server.json.JsonException if the result cannot be validated
     */
    @Test
    public void testGetPanels_Locale() throws JsonException {
        Assume.assumeFalse("Needs GUI", GraphicsEnvironment.isHeadless());
        Editor editor = new SwitchboardEditor("test");
        JsonNode result = service.getPanels(42);
        validate(result);
        JUnitUtil.dispose(editor.getTargetFrame());
        JUnitUtil.dispose(editor);
    }

    /**
     * Test of getConfigProfile method, of class JsonUtilHttpService. 
     * only runs negative test that a profile is not found
     *
     */
    @Test
    public void testGetConfigProfile() {
        try {
            JsonNode result = service.getConfigProfile(locale, "non-existent-profile", 42);
            validate(result);
            fail("Expected exception not thrown");
        } catch (JsonException ex) {
            assertEquals(HttpServletResponse.SC_NOT_FOUND, ex.getCode());
        }
        
    }

    /**
     * Test of getConfigProfiles method, of class JsonUtilHttpService. Only
     * tests that result is schema valid and contains all profiles.
     *
     * @throws jmri.server.json.JsonException if unable to read profiles
     */
    @Test
    public void testGetConfigProfiles() throws JsonException {
        ArrayNode result = service.getConfigProfiles(locale, 42);
        validate(result);
        assertEquals("Result has every profile", ProfileManager.getDefault().getProfiles().length, result.size());
    }

    /**
     * Test of addressForString method, of class JsonUtilHttpService.
     */
    @Test
    public void testAddressForString() {
        DccLocoAddress result = JsonUtilHttpService.addressForString("123(l)");
        assertTrue("Address is long", result.isLongAddress());
        assertEquals("Address is 123", 123, result.getNumber());
        result = JsonUtilHttpService.addressForString("123(L)");
        assertTrue("Address is long", result.isLongAddress());
        assertEquals("Address is 123", 123, result.getNumber());
        result = JsonUtilHttpService.addressForString("123(s)");
        assertFalse("Address is short", result.isLongAddress());
        assertEquals("Address is 123", 123, result.getNumber());
        result = JsonUtilHttpService.addressForString("123");
        assertFalse("Address is short", result.isLongAddress());
        assertEquals("Address is 123", 123, result.getNumber());
        result = JsonUtilHttpService.addressForString("3");
        assertFalse("Address is short", result.isLongAddress());
        assertEquals("Address is 3", 3, result.getNumber());
        result = JsonUtilHttpService.addressForString("3(l)");
        assertTrue("Address is long", result.isLongAddress());
        assertEquals("Address is 3", 3, result.getNumber());
    }

    // private final static Logger log = LoggerFactory.getLogger(JsonUtilHttpServiceTest.class);

}
