package jmri.server.json.throttle;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.DataOutputStream;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonHttpService;
import jmri.server.json.JsonMockConnection;
import jmri.server.json.JsonSocketService;
import org.junit.Assert;
import org.junit.Test;

/**
 * JsonThrottleServiceFactory tests.
 *
 * @author Randall Wood
 */
public class JsonThrottleServiceFactoryTest {

    /**
     * Test of getTypes method, of class JsonThrottleServiceFactory.
     */
    @Test
    public void testGetTypes() {
        JsonThrottleServiceFactory instance = new JsonThrottleServiceFactory();
        String[] expResult = new String[]{JsonThrottle.THROTTLE};
        String[] result = instance.getTypes();
        Assert.assertArrayEquals(expResult, result);
    }

    /**
     * Test of getSocketService method, of class JsonThrottleServiceFactory.
     */
    @Test
    public void testGetSocketService() {
        JsonConnection connection = new JsonMockConnection((DataOutputStream) null);
        JsonThrottleServiceFactory instance = new JsonThrottleServiceFactory();
        JsonSocketService result = instance.getSocketService(connection);
        Assert.assertNotNull(result);
        Assert.assertTrue(JsonSocketService.class.isInstance(result));
    }

    /**
     * Test of getHttpService method, of class JsonThrottleServiceFactory.
     */
    @Test
    public void testGetHttpService() {
        JsonThrottleServiceFactory instance = new JsonThrottleServiceFactory();
        JsonHttpService result = instance.getHttpService(new ObjectMapper());
        Assert.assertNull(result);
    }

}
