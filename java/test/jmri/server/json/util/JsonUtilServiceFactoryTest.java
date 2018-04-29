package jmri.server.json.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.DataOutputStream;
import jmri.server.json.JSON;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonMockConnection;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author rhwood
 */
public class JsonUtilServiceFactoryTest {
    
    public JsonUtilServiceFactoryTest() {
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
     * Test of getTypes method, of class JsonUtilServiceFactory.
     */
    @Test
    public void testGetTypes() {
        JsonUtilServiceFactory instance = new JsonUtilServiceFactory();
        String[] expResult = new String[]{JSON.GOODBYE,
            JSON.HELLO,
            JSON.LOCALE,
            JSON.METADATA,
            JSON.NETWORK_SERVICES,
            JSON.NODE,
            JSON.PANELS,
            JSON.PING,
            JSON.RAILROAD,
            JSON.SYSTEM_CONNECTIONS,
            JSON.CONFIG_PROFILES};
        String[] result = instance.getTypes();
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of getSocketService method, of class JsonUtilServiceFactory.
     */
    @Test
    public void testGetSocketService() {
        JsonConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonUtilServiceFactory instance = new JsonUtilServiceFactory();
        JsonUtilSocketService result = instance.getSocketService(connection);
        assertNotNull(result);
    }

    /**
     * Test of getHttpService method, of class JsonUtilServiceFactory.
     */
    @Test
    public void testGetHttpService() {
        ObjectMapper mapper = null;
        JsonUtilServiceFactory instance = new JsonUtilServiceFactory();
        JsonUtilHttpService result = instance.getHttpService(mapper);
        assertNotNull(result);
    }
    
}
