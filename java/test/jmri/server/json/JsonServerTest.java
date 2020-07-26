package jmri.server.json;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.server.json package
 *
 * @author Paul Bender
 */
public class JsonServerTest {

    @Test
    public void testCtor() {
        JsonServer a = new JsonServer();
        Assert.assertNotNull(a);
    }

    @Test
    public void testCtorwithParameter() {
        JsonServer a = new JsonServer(12345, 10000);
        //jmri.util.JUnitAppender.assertErrorMessage("Failed to connect to port 12345");
        Assert.assertNotNull(a);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
