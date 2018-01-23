package jmri.server.json.logs;

import static jmri.server.json.logs.JsonLogs.LOGS;

import java.io.DataOutputStream;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonMockConnection;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Randall Wood Copyright 2017
 */
public class JsonLogsServiceFactoryTest {

    @Test
    public void testCTor() {
        JsonLogsServiceFactory t = new JsonLogsServiceFactory();
        Assert.assertNotNull("exists", t);
    }

    /**
     * Test of getTypes method, of class JsonLogsServiceFactory.
     */
    @Test
    public void testGetTypes() {
        JsonLogsServiceFactory instance = new JsonLogsServiceFactory();
        Assert.assertArrayEquals(new String[]{LOGS}, instance.getTypes());
    }

    /**
     * Test of getSocketService method, of class JsonLogsServiceFactory.
     */
    @Test
    public void testGetSocketService() {
        Assert.assertNotNull(new JsonLogsServiceFactory().getSocketService(new JsonMockConnection((DataOutputStream) null)));
    }

    /**
     * Test of getHttpService method, of class JsonLogsServiceFactory.
     */
    @Test
    public void testGetHttpService() {
        JsonConnection connection = new JsonMockConnection((DataOutputStream) null);
        Assert.assertNull(new JsonLogsServiceFactory().getHttpService(connection.getObjectMapper()));
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
