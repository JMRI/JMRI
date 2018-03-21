package jmri.server.json.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import apps.tests.Log4JFixture;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.jmris.json.JsonServerPreferences;
import jmri.server.json.JSON;
import jmri.server.json.JsonHttpServiceTest;
import jmri.server.json.JsonMockConnection;
import jmri.util.JUnitUtil;
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
public class JsonUtilSocketServiceTest {

    private static final Logger log = LoggerFactory.getLogger(JsonUtilSocketServiceTest.class);

    public JsonUtilSocketServiceTest() {
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
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
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
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode empty = connection.getObjectMapper().createObjectNode();
        JsonUtilSocketService instance = new JsonUtilSocketService(connection);
        // JSON.LOCALE
        instance.onMessage(JSON.LOCALE, empty, JSON.POST, locale);
        assertNull(connection.getMessage());
        // JSON.PING
        instance.onMessage(JSON.PING, empty, JSON.POST, locale);
        JsonHttpServiceTest.testValidJmriJsonMessage(connection.getMessage());
        JsonNode result = connection.getMessage().path(JSON.TYPE);
        assertNotNull(result);
        assertTrue(JsonNode.class.isInstance(result));
        assertEquals(JSON.PONG, result.asText());
        assertTrue(connection.getMessage().path(JSON.DATA).isMissingNode());
        // JSON.GOODBYE
        instance.onMessage(JSON.GOODBYE, empty, JSON.POST, locale);
        JsonHttpServiceTest.testValidJmriJsonMessage(connection.getMessage());
        result = connection.getMessage().path(JSON.TYPE);
        assertNotNull(result);
        assertTrue(JsonNode.class.isInstance(result));
        assertEquals(JSON.GOODBYE, result.asText());
        assertTrue(connection.getMessage().path(JSON.DATA).isMissingNode());
    }

    /**
     * Test of onList method, of class JsonUtilSocketService.
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

}
