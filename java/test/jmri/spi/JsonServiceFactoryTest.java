package jmri.spi;

import java.io.DataOutputStream;
import java.util.ServiceLoader;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonMockConnection;
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
            Assert.assertNotNull("Create Socket service", factory.getSocketService(connection));
            Assert.assertNotNull("Create HTTP service", factory.getHttpService(connection.getObjectMapper()));
            Assert.assertNotNull("Responds to message types", factory.getTypes());
            Assert.assertNotNull("Sends message types not responded to", factory.getSentTypes());
            Assert.assertNotNull("Receives messages types not sent", factory.getReceivedTypes());
        });
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
