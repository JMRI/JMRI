package jmri.server.json.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.jmris.json.JsonServerPreferences;
import jmri.server.json.JSON;
import jmri.server.json.JsonMockConnection;
import jmri.util.JUnitUtil;
import jmri.web.server.WebServerPreferences;

import org.junit.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rhwood
 */
public class JsonUtilSocketServiceTest {

    public JsonUtilSocketServiceTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        jmri.util.JUnitUtil.setUp();

        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
    }

    @AfterClass
    public static void tearDownClass() {
        jmri.util.JUnitUtil.tearDown();

    }

    @Before
    public void setUp() throws IOException {
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of onMessage method, of class JsonUtilSocketService.
     *
     * @throws java.lang.Exception if an exception unexpected in the context of
     *                             these tests occurs
     */
    @Test
    public void testOnMessage() throws Exception {
        Locale locale = Locale.ENGLISH;
        InstanceManager.getDefault(JsonServerPreferences.class).setValidateServerMessages(true);
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode empty = connection.getObjectMapper().createObjectNode();
        JsonUtilSocketService instance = new JsonUtilSocketService(connection);
        // JSON.LOCALE
        instance.onMessage(JSON.LOCALE, empty, JSON.POST, locale);
        Assert.assertNull(connection.getMessage()); // assert no reply
        // JSON.PING
        instance.onMessage(JSON.PING, empty, JSON.POST, locale);
        JsonNode result = connection.getMessage().path(JSON.TYPE);
        Assert.assertNotNull(result);
        Assert.assertTrue(JsonNode.class.isInstance(result));
        Assert.assertEquals(JSON.PONG, result.asText());
        Assert.assertTrue(connection.getMessage().path(JSON.DATA).isMissingNode());
        // JSON.RAILROAD
        WebServerPreferences wsp = InstanceManager.getDefault(WebServerPreferences.class);
        instance.onMessage(JSON.RAILROAD, empty, JSON.GET, locale);
        result = connection.getMessage().path(JSON.DATA);
        Assert.assertNotNull(result);
        Assert.assertEquals(JSON.RAILROAD, connection.getMessage().path(JSON.TYPE).asText());
        Assert.assertEquals("Railroad name matches", wsp.getRailroadName(), result.path(JSON.NAME).asText());
        wsp.setRailroadName("test railroad");
        result = connection.getMessage().path(JSON.DATA);
        Assert.assertNotNull(result);
        Assert.assertEquals(JSON.RAILROAD, connection.getMessage().path(JSON.TYPE).asText());
        Assert.assertEquals("Railroad name matches", wsp.getRailroadName(), result.path(JSON.NAME).asText());
        // JSON.GOODBYE
        instance.onMessage(JSON.GOODBYE, empty, JSON.POST, locale);
        result = connection.getMessage().path(JSON.TYPE);
        Assert.assertNotNull(result);
        Assert.assertTrue(JsonNode.class.isInstance(result));
        Assert.assertEquals(JSON.GOODBYE, result.asText());
        Assert.assertTrue(connection.getMessage().path(JSON.DATA).isMissingNode());
    }

    /**
     * Test of onList method, of class JsonUtilSocketService.
     *
     * @throws java.lang.Exception if an exception unexpected in the context of
     *                             these tests occurs
     */
    @Test
    @Ignore // See Issue #5642
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
        instance.onList(JSON.CONFIG_PROFILES, empty, locale);
        Assert.assertEquals(helper.getConfigProfiles(locale), connection.getMessage());
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
