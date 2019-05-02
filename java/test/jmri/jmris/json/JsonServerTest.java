package jmri.jmris.json;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmris.json package
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

    // The minimal setup for log4J
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
