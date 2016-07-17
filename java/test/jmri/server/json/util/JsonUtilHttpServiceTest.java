package jmri.server.json.util;

import apps.tests.Log4JFixture;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.Metadata;
import jmri.jmris.json.JsonServerPreferences;
import jmri.profile.NullProfile;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;
import jmri.util.node.NodeIdentity;
import jmri.util.zeroconf.ZeroConfService;
import jmri.web.server.WebServerPreferences;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author rhwood
 */
public class JsonUtilHttpServiceTest {

    public final static Logger log = LoggerFactory.getLogger(JsonUtilHttpServiceTest.class);

    public JsonUtilHttpServiceTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        Log4JFixture.setUp();
    }

    @AfterClass
    public static void tearDownClass() {
        Log4JFixture.tearDown();
    }

    @Before
    public void setUp() throws IOException {
        Profile profile = new NullProfile("TestProfile", null, FileUtil.getFile(FileUtil.SETTINGS));
        ProfileManager.getDefault().setActiveProfile(profile);
        JUnitUtil.initConfigureManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
    }

    /**
     * Test of doGet method, of class JsonUtilHttpService.
     */
    @Test
    public void testDoGet() throws Exception {
        Locale locale = Locale.ENGLISH;
        ObjectMapper mapper = new ObjectMapper();
        JsonUtilHttpService instance = new JsonUtilHttpService(mapper);
        JsonServerPreferences.getDefault().setHeartbeatInterval(10);
        Assert.assertEquals(instance.getHello(locale, 10), instance.doGet(JSON.HELLO, null, locale));
        Assert.assertEquals(instance.getMetadata(locale, Metadata.JMRIVERCANON), instance.doGet(JSON.METADATA, Metadata.JMRIVERCANON, locale));
        Assert.assertEquals(instance.getMetadata(locale), instance.doGet(JSON.METADATA, null, locale));
        Assert.assertEquals(instance.getNode(locale), instance.doGet(JSON.NODE, null, locale));
        Assert.assertEquals(instance.getNetworkService(locale, JSON.ZEROCONF_SERVICE_TYPE), instance.doGet(JSON.NETWORK_SERVICE, JSON.ZEROCONF_SERVICE_TYPE, locale));
        Assert.assertEquals(instance.getNetworkServices(locale), instance.doGet(JSON.NETWORK_SERVICE, null, locale));
        Assert.assertEquals(instance.getNetworkServices(locale), instance.doGet(JSON.NETWORK_SERVICES, null, locale));
        JsonException exception = null;
        try {
            instance.doGet("INVALID TYPE TOKEN", null, locale);
        } catch (JsonException ex) {
            exception = ex;
        }
        Assert.assertNotNull(exception);
        Assert.assertEquals(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getCode());
    }

    /**
     * Test of doGetList method, of class JsonUtilHttpService.
     */
    @Test
    public void testDoGetList() throws Exception {
        Locale locale = Locale.ENGLISH;
        ObjectMapper mapper = new ObjectMapper();
        JsonUtilHttpService instance = new JsonUtilHttpService(mapper);
        JsonServerPreferences.getDefault().setHeartbeatInterval(10);
        Assert.assertEquals(instance.getMetadata(locale), instance.doGetList(JSON.METADATA, locale));
        Assert.assertEquals(instance.getNetworkServices(locale), instance.doGetList(JSON.NETWORK_SERVICES, locale));
        Assert.assertEquals(instance.getSystemConnections(locale), instance.doGetList(JSON.SYSTEM_CONNECTIONS, locale));
    }

    /**
     * Test of doPost method, of class JsonUtilHttpService.
     */
    @Test
    public void testDoPost() {
        Locale locale = Locale.ENGLISH;
        ObjectMapper mapper = new ObjectMapper();
        JsonUtilHttpService instance = new JsonUtilHttpService(mapper);
        String type = JSON.HELLO;
        String name = JSON.HELLO;
        try {
            Assert.assertEquals(instance.doGet(type, name, locale), instance.doPost(type, name, null, locale));
        } catch (JsonException ex) {
            log.error("Unexpected exception.", ex);
            Assert.fail("Unexpected exception");
        }
    }

    /**
     * Test of getHello method, of class JsonUtilHttpService.
     */
    @Test
    public void testGetHello() {
        Locale locale = Locale.ENGLISH;
        int heartbeat = 10;
        ObjectMapper mapper = new ObjectMapper();
        JsonUtilHttpService instance = new JsonUtilHttpService(mapper);
        JsonNode result = instance.getHello(locale, heartbeat);
        Assert.assertEquals(JSON.HELLO, result.path(JSON.TYPE).asText());
        JsonNode data = result.path(JSON.DATA);
        Assert.assertEquals(jmri.Version.name(), data.path(JSON.JMRI).asText());
        Assert.assertEquals(JSON.JSON_PROTOCOL_VERSION, data.path(JSON.JSON).asText());
        Assert.assertEquals(Math.round(heartbeat * 0.9f), data.path(JSON.HEARTBEAT).asInt());
        Assert.assertEquals(WebServerPreferences.getDefault().getRailRoadName(), data.path(JSON.RAILROAD).asText());
        Assert.assertEquals(NodeIdentity.identity(), data.path(JSON.NODE).asText());
        Assert.assertEquals(ProfileManager.getDefault().getActiveProfile().getName(), data.path(JSON.ACTIVE_PROFILE).asText());
    }

    /**
     * Test of getMetadata method, of class JsonUtilHttpService.
     */
    @Test
    public void testGetMetadata_Locale_String() {
        Locale locale = Locale.ENGLISH;
        ObjectMapper mapper = new ObjectMapper();
        JsonUtilHttpService instance = new JsonUtilHttpService(mapper);
        JsonNode result;
        try {
            for (String metadata : Metadata.getSystemNameList()) {
                result = instance.getMetadata(locale, metadata);
                Assert.assertEquals(JSON.METADATA, result.path(JSON.TYPE).asText());
                Assert.assertEquals(metadata, result.path(JSON.DATA).path(JSON.NAME).asText());
                Assert.assertEquals(Metadata.getBySystemName(metadata), result.path(JSON.DATA).path(JSON.VALUE).asText());
            }
        } catch (JsonException ex) {
            log.error("Unexpected exception.", ex);
            Assert.fail("Unexpected exception");
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
     */
    @Test
    public void testGetMetadata_Locale() {
        Locale locale = Locale.ENGLISH;
        ObjectMapper mapper = new ObjectMapper();
        JsonUtilHttpService instance = new JsonUtilHttpService(mapper);
        JsonNode result = null;
        try {
            result = instance.getMetadata(locale);
        } catch (JsonException ex) {
            log.error("Unexpected exception.", ex);
            Assert.fail("Unexpected exception");
        }
        Assert.assertNotNull(result);
        Assert.assertEquals(Metadata.getSystemNameList().size(), result.size());
    }

    /**
     * Test of getNetworkServices method, of class JsonUtilHttpService.
     */
    @Test
    public void testGetNetworkServices() {
        Locale locale = Locale.ENGLISH;
        ObjectMapper mapper = new ObjectMapper();
        JsonUtilHttpService instance = new JsonUtilHttpService(mapper);
        // no services published
        JsonNode result = instance.getNetworkServices(locale);
        Assert.assertEquals(0, result.size());
        // publish a service
        ZeroConfService service = ZeroConfService.create(JSON.ZEROCONF_SERVICE_TYPE, 9999);
        service.publish();
        JUnitUtil.waitFor(() -> {
            return service.isPublished() == true;
        }, "Publishing ZeroConf Service");
        result = instance.getNetworkServices(locale);
        Assert.assertEquals(1, result.size());
        Assert.assertEquals(JSON.NETWORK_SERVICE, result.get(0).path(JSON.TYPE).asText());
        JsonNode data = result.get(0).path(JSON.DATA);
        Assert.assertFalse(data.isMissingNode());
        Assert.assertEquals(WebServerPreferences.getDefault().getRailRoadName(), data.path(JSON.NAME).asText());
        Assert.assertEquals(9999, data.path(JSON.PORT).asInt());
        Assert.assertEquals(JSON.ZEROCONF_SERVICE_TYPE, data.path(JSON.TYPE).asText());
        Assert.assertEquals(NodeIdentity.identity(), data.path(JSON.NODE).asText());
        Assert.assertEquals(Metadata.getBySystemName(Metadata.JMRIVERCANON), data.path("jmri").asText());
        Assert.assertEquals(Metadata.getBySystemName(Metadata.JMRIVERSION), data.path("version").asText());
    }

    /**
     * Test of getNode method, of class JsonUtilHttpService.
     */
    @Test
    public void testGetNode() {
        Locale locale = Locale.ENGLISH;
        ObjectMapper mapper = new ObjectMapper();
        JsonUtilHttpService instance = new JsonUtilHttpService(mapper);
        JsonNode result = instance.getNode(locale);
        // We should have a single node with no history of nodes
        Assert.assertEquals(JSON.NODE, result.path(JSON.TYPE).asText());
        Assert.assertEquals(NodeIdentity.identity(), result.path(JSON.DATA).path(JSON.NODE).asText());
        Assert.assertEquals(0, result.path(JSON.DATA).path(JSON.FORMER_NODES).size());
    }

    /**
     * Test of getSystemConnections method, of class JsonUtilHttpService.
     */
    @Test
    public void testGetSystemConnections() {
        Locale locale = Locale.ENGLISH;
        ObjectMapper mapper = new ObjectMapper();
        JsonUtilHttpService instance = new JsonUtilHttpService(mapper);
        JsonNode result = instance.getSystemConnections(locale);
        // We should only have one internal connection
        Assert.assertEquals(1, result.size());
        JsonNode connection = result.get(0);
        Assert.assertEquals(JSON.SYSTEM_CONNECTION, connection.path(JSON.TYPE).asText());
        Assert.assertEquals("I", connection.path(JSON.DATA).path(JSON.PREFIX).asText());
        Assert.assertTrue(connection.path(JSON.DATA).path(JSON.NAME).isNull());
        Assert.assertTrue(connection.path(JSON.DATA).path(JSON.MFG).isNull());
    }

}
