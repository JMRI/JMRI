package jmri.server.json;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonWebSocketTest {

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

    @Test
    public void testLifecycle() {
        JsonWebSocket instance = new JsonWebSocket();
        Assert.assertNull(instance.getConnection());
        // until I can figure out how to create a Session, leave rest commented out
        // since onOpen wants the Session to work and Session is abstract, and
        // Eclipse.org is down
        // instance.onOpen(new Session());
        // Assert.assertNotNull(instance.getConnection().getSession());
        // Assert.assertNull(instance.getConnection().getDataOutputStream());
    }
}
