package jmri.server.json.util;

import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.DataOutputStream;
import java.util.Locale;
import jmri.server.json.JSON;
import jmri.server.json.JsonMockConnection;
import org.junit.After;
import org.junit.AfterClass;
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
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
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
        JsonMockConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonNode empty = connection.getObjectMapper().createObjectNode();
        JsonUtilSocketService instance = new JsonUtilSocketService(connection);
        // JSON.LOCALE
        instance.onMessage(JSON.LOCALE, empty, locale);
        assertNull(connection.getMessage());
        // JSON.PING
        instance.onMessage(JSON.PING, empty, locale);
        JsonNode result = connection.getMessage().path(JSON.TYPE);
        assertNotNull(result);
        assertTrue(JsonNode.class.isInstance(result));
        assertEquals(JSON.PONG, result.asText());
        // JSON.GOODBYE
        instance.onMessage(JSON.GOODBYE, empty, locale);
        result = connection.getMessage().path(JSON.TYPE);
        assertNotNull(result);
        assertTrue(JsonNode.class.isInstance(result));
        assertEquals(JSON.GOODBYE, result.asText());
    }

    /**
     * Test of onList method, of class JsonUtilSocketService.
     *
     * @throws java.lang.Exception if an exception unexpected in the context of
     *                             these tests occurs
     */
    @Test
    public void testOnList() throws Exception {
        System.out.println("onList");
        String type = "";
        JsonNode data = null;
        Locale locale = null;
        JsonUtilSocketService instance = null;
        instance.onList(type, data, locale);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
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
