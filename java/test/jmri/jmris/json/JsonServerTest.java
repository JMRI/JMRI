//JsonServerTest.java
package jmri.jmris.json;

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
    public void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
    }

    @After
    public void tearDown() throws Exception {
        apps.tests.Log4JFixture.tearDown();
    }

}
