package jmri.spi;

import java.io.DataOutputStream;
import java.util.ServiceLoader;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonHttpService;
import jmri.server.json.JsonMockConnection;
import jmri.server.json.JsonSocketService;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test that JsonServiceFactory classes adhere to contract.
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonServiceFactoryTest {

    /**
     * Test that every published service factory creates valid objects.
     */
    @Test
    public void testJsonServiceFactories() {
        JsonConnection connection = new JsonMockConnection((DataOutputStream) null);
        ServiceLoader<JsonServiceFactory> loader = ServiceLoader.load(JsonServiceFactory.class);
        Assert.assertTrue("Json Services exist", loader.iterator().hasNext());
        loader.forEach((factory) -> {
            // verify factory is well behaved
            JsonSocketService<?> socket = factory.getSocketService(connection);
            JsonHttpService http = factory.getHttpService(connection.getObjectMapper());
            Assert.assertNotNull("Create Socket service", socket);
            Assert.assertNotNull("Create HTTP service", http);
            Assert.assertNotNull("Responds to message types", factory.getTypes());
            Assert.assertNotNull("Sends message types not responded to", factory.getSentTypes());
            Assert.assertNotNull("Receives messages types not sent", factory.getReceivedTypes());
            // verify socket service constructors are populating finals correctly
            Assert.assertEquals("Socket has connection", connection, socket.getConnection());
            Assert.assertTrue("Socket creates same HTTP service class as factory", http.getClass().equals(socket.getHttpService().getClass()));
            // verify HTTP service constructors are populating finals correctly
            Assert.assertEquals("HTTP object mapper matches connection", connection.getObjectMapper(), http.getObjectMapper());
        });
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
