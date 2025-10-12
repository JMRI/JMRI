package jmri.server.json.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;

import javax.servlet.http.HttpServletResponse;

import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.Metadata;
import jmri.Version;
import jmri.server.json.JsonServerPreferences;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.switchboardEditor.SwitchboardEditor;
import jmri.profile.NullProfile;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpServiceTestBase;
import jmri.server.json.JsonRequest;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;
import jmri.util.node.NodeIdentity;
import jmri.util.zeroconf.ZeroConfService;
import jmri.web.server.WebServerPreferences;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonUtilHttpServiceTest extends JsonHttpServiceTestBase<JsonUtilHttpService> {

    @BeforeEach
    public void setUp(@TempDir File folder) throws Exception {
        super.setUp();
        service = new JsonUtilHttpService(mapper);
        JUnitUtil.resetWindows(true, false); // list open windows when running tests
        JUnitUtil.resetNodeIdentity();
        JUnitUtil.resetProfileManager(new NullProfile("JsonUtilHttpServiceTest", "12345678", folder));
        JUnitUtil.initConnectionConfigManager();
        JUnitUtil.initZeroConfServiceManager();
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        assertTrue(JUnitUtil.resetZeroConfServiceManager());
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
        assertEquals(service.getHello(10, new JsonRequest(locale, JSON.V5, JSON.GET, 42)), service.doGet(JSON.HELLO, null, NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 42)));
        assertEquals(service.getMetadata(locale, Metadata.JMRIVERCANON, 42), service.doGet(JSON.METADATA, Metadata.JMRIVERCANON, NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 42)));
        assertEquals(service.getMetadata(new JsonRequest(locale, JSON.V5, JSON.GET, 42)), service.doGet(JSON.METADATA, null, NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 42)));
        assertEquals(service.getNode(new JsonRequest(locale, JSON.V5, JSON.GET, 42)), service.doGet(JSON.NODE, null, NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 42)));
        assertEquals(service.getNetworkServices(new JsonRequest(locale, JSON.V5, JSON.GET, 42)), service.doGet(JSON.NETWORK_SERVICE, null, NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 42)));
        assertEquals(service.getNetworkServices(new JsonRequest(locale, JSON.V5, JSON.GET, 42)), service.doGet(JSON.NETWORK_SERVICES, null, NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 42)));

        JsonException ex = assertThrows(JsonException.class, () ->
            service.doGet(JSON.NETWORK_SERVICE, JSON.ZEROCONF_SERVICE_TYPE,
                NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown.");
        assertEquals(404, ex.getCode());

        ex = assertThrows(JsonException.class, () ->
            service.doGet("INVALID TYPE TOKEN", null, NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown.");
        assertEquals(500, ex.getCode());

        ZeroConfService zcs = ZeroConfService.create(JSON.ZEROCONF_SERVICE_TYPE, 9999);
        JUnitUtil.waitFor(() -> {
            return zcs.isPublished() == false;
        }, "zcs.isPublished did not go false");
        zcs.publish();
        JUnitUtil.waitFor(() -> {
            return zcs.isPublished() == true;
        }, "Published ZeroConf Service");
        assertEquals(service.getNetworkService(JSON.ZEROCONF_SERVICE_TYPE,
            new JsonRequest (locale, JSON.V5, JSON.GET, 42)),
                service.doGet(JSON.NETWORK_SERVICE, JSON.ZEROCONF_SERVICE_TYPE,
                NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 42)));
        zcs.stop();
        JUnitUtil.waitFor(() -> {
            return zcs.isPublished() == false;
        },"zcs.isPublished did not go false after stop");
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
        assertEquals(service.getMetadata(new JsonRequest(locale, JSON.V5, JSON.GET, 42)), service.doGetList(JSON.METADATA, NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 42)));
        assertEquals(service.getNetworkServices(new JsonRequest(locale, JSON.V5, JSON.GET, 42)), service.doGetList(JSON.NETWORK_SERVICES, NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 42)));
        assertEquals(service.getSystemConnections(new JsonRequest(locale, JSON.V5, JSON.GET, 42)), service.doGetList(JSON.SYSTEM_CONNECTIONS, NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 42)));
        assertEquals(service.getConfigProfiles(new JsonRequest(locale, JSON.V5, JSON.GET, 42)), service.doGetList(JSON.CONFIG_PROFILES, NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 42)));
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
        assertEquals(service.doGet(type, name, NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 42)), service.doPost(type, name, NullNode.getInstance(), new JsonRequest(locale, JSON.V5, JSON.GET, 42)));
    }

    /**
     * Test of getHello method, of class JsonUtilHttpService.
     *
     * @throws jmri.server.json.JsonException if messages are not schema valid
     */
    @Test
    public void testGetHello() throws JsonException {
        int heartbeat = 1000; // one second
        JsonNode result = service.getHello(heartbeat, new JsonRequest(locale, JSON.V5, JSON.GET, 0));
        validate(result);
        assertEquals( JSON.HELLO, result.path(JSON.TYPE).asText(), "Hello type");
        JsonNode data = result.path(JSON.DATA);
        assertEquals( Version.name(), data.path(JSON.JMRI).asText(), "JMRI Version");
        assertEquals( JSON.V5_PROTOCOL_VERSION, data.path(JSON.JSON).asText(), "JSON Complete Version");
        assertEquals( JSON.V5, data.path(JSON.VERSION).asText(), "JSON Version Identifier");
        assertEquals( Math.round(heartbeat * 0.9f), data.path(JSON.HEARTBEAT).asInt(), "Heartbeat");
        assertEquals( InstanceManager.getDefault(WebServerPreferences.class).getRailroadName(), data.path(JSON.RAILROAD).asText(),
            "RR Name");
        assertEquals( NodeIdentity.networkIdentity(), data.path(JSON.NODE).asText(), "Node Identity");
        Profile profile = ProfileManager.getDefault().getActiveProfile();
        assertNotNull(profile);
        assertEquals( profile.getName(), data.path(JSON.ACTIVE_PROFILE).asText(), "Profile");
        assertEquals( 2, result.size(), "Message has 2 elements");
        assertEquals( 7, data.size(), "Message data has 7 elements");
        result = service.getHello(heartbeat, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        validate(result);
        data = result.path(JSON.DATA);
        assertEquals( 3, result.size(), "Message has 2 elements");
        assertEquals( 7, data.size(), "Message data has 7 elements");
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
        JsonException ex = assertThrows(JsonException.class, () ->
            service.getMetadata(locale, "invalid_metadata_entry", 42),
            "Expected exception not thrown");
        assertEquals(404, ex.getCode());
    }

    /**
     * Test of getMetadata method, of class JsonUtilHttpService.
     *
     * @throws jmri.server.json.JsonException if messages are not schema valid
     */
    @Test
    public void testGetMetadata_Locale() throws JsonException {
        JsonNode result = service.getMetadata(new JsonRequest(locale, JSON.V5, JSON.GET, 42));
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
        JsonNode result = service.getNetworkServices(new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        validate(result);
        assertEquals(0, result.size());
        // publish a service
        ZeroConfService zcs = ZeroConfService.create(JSON.ZEROCONF_SERVICE_TYPE, 9999);
        JUnitUtil.waitFor(() -> {
            return zcs.isPublished() == false;
        },"zcs.isPublished did not go false");
        zcs.publish();
        JUnitUtil.waitFor(() -> {
            return zcs.isPublished() == true;
        }, "Published ZeroConf Service");
        result = service.getNetworkServices(new JsonRequest(locale, JSON.V5, JSON.GET, 42));
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
        },"zcs.isPublished did not go false after stop");
    }

    /**
     * Test of getNode method, of class JsonUtilHttpService.
     *
     * @throws jmri.server.json.JsonException if messages are not schema valid
     */
    @Test
    public void testGetNode() throws JsonException {
        JsonNode result = service.getNode(new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        validate(result);
        // We should have a single node with no history of nodes
        // There are 3 "former" IDs when there is no history
        assertEquals(JSON.NODE, result.path(JSON.TYPE).asText());
        assertEquals(NodeIdentity.networkIdentity(), result.path(JSON.DATA).path(JSON.NODE).asText());
        JsonNode nodes = result.path(JSON.DATA).path(JSON.FORMER_NODES);
        assertTrue(nodes.isArray());
        // Use whatever is returned by formerIdentities to avoid setup issues
        // when running within an IDE
        assertEquals(NodeIdentity.formerIdentities().size(), nodes.size());
    }

    /**
     * Test of getSystemConnection method, of class JsonUtilHttpService.
     *
     * @throws jmri.server.json.JsonException if connection name not found
     */
    @Test
    public void testGetSystemConnection() throws JsonException {
        JsonNode result = service.getSystemConnection("Internal", new JsonRequest(locale, JSON.V5, JSON.GET, 42));
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
        JsonNode result = service.getSystemConnections(new JsonRequest(locale, JSON.V5, JSON.GET, 42));
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
        JsonNode result;
        // non-existent service
        JsonException ex = assertThrows( JsonException.class, () ->
            service.getNetworkService("non-existant-service",
                new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
        "Expected exception not thrown ");
        assertEquals(HttpServletResponse.SC_NOT_FOUND, ex.getCode());

        // published service
        ZeroConfService zcs = ZeroConfService.create(JSON.ZEROCONF_SERVICE_TYPE, 9999);
        JUnitUtil.waitFor(() -> {
            return zcs.isPublished() == false;
        },"zcs.isPublished did not go false");
        zcs.publish();
        JUnitUtil.waitFor(() -> {
            return zcs.isPublished() == true;
        }, "Published ZeroConf Service");
        result = service.getNetworkService(JSON.ZEROCONF_SERVICE_TYPE, new JsonRequest(locale, JSON.V5, JSON.GET, 42));
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
        },"zcs.isPublished did not go false after stop");
    }

    /**
     * Test of getRailroad method, of class JsonUtilHttpService.
     *
     * @throws jmri.server.json.JsonException if messages are not schema valid
     */
    @Test
    public void testGetRailroad() throws JsonException {
        JsonNode result = service.getRailroad(new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        validate(result);
        assertEquals(JSON.RAILROAD, result.path(JSON.TYPE).asText());
        JsonNode data = result.path(JSON.DATA);
        assertEquals(InstanceManager.getDefault(WebServerPreferences.class).getRailroadName(),
            data.path(JSON.NAME).asText());
    }

    /**
     * Test of getPanel method, of class JsonUtilHttpService.
     *
     * @throws jmri.server.json.JsonException if the result cannot be validated
     */
    @Test
    @DisabledIfHeadless
    public void testGetPanel() throws JsonException {
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
    @DisabledIfHeadless
    public void testGetPanels_Locale_String() throws JsonException {
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
    @DisabledIfHeadless
    public void testGetPanels_Locale() throws JsonException {
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
        JsonException ex = assertThrows( JsonException.class, () ->
            service.getConfigProfile("non-existent-profile", new JsonRequest(locale, JSON.V5, JSON.GET, 42)),
            "Expected exception not thrown");
        assertEquals(HttpServletResponse.SC_NOT_FOUND, ex.getCode());

    }

    /**
     * Test of getConfigProfiles method, of class JsonUtilHttpService. Only
     * tests that result is schema valid and contains all profiles.
     *
     * @throws jmri.server.json.JsonException if unable to read profiles
     */
    @Test
    public void testGetConfigProfiles() throws JsonException {
        ArrayNode result = service.getConfigProfiles(new JsonRequest(locale, JSON.V5, JSON.GET, 42));
        validate(result);
        assertEquals( ProfileManager.getDefault().getProfiles().length, result.size(), "Result has every profile");
    }

    /**
     * Test of addressForString method, of class JsonUtilHttpService.
     */
    @Test
    public void testAddressForString() {
        DccLocoAddress result = JsonUtilHttpService.addressForString("123(l)");
        assertTrue( result.isLongAddress(), "Address is long");
        assertEquals( 123, result.getNumber(), "Address is 123");
        result = JsonUtilHttpService.addressForString("123(L)");
        assertTrue( result.isLongAddress(), "Address is long");
        assertEquals( 123, result.getNumber(), "Address is 123");
        result = JsonUtilHttpService.addressForString("123(s)");
        assertFalse( result.isLongAddress(), "Address is short");
        assertEquals( 123, result.getNumber(), "Address is 123");
        result = JsonUtilHttpService.addressForString("123");
        assertFalse( result.isLongAddress(), "Address is short");
        assertEquals( 123, result.getNumber(), "Address is 123");
        result = JsonUtilHttpService.addressForString("3");
        assertFalse( result.isLongAddress(), "Address is short");
        assertEquals( 3, result.getNumber(), "Address is 3");
        result = JsonUtilHttpService.addressForString("3(l)");
        assertTrue( result.isLongAddress(), "Address is long");
        assertEquals( 3, result.getNumber(), "Address is 3");
    }

    // private final static Logger log = LoggerFactory.getLogger(JsonUtilHttpServiceTest.class);

}
