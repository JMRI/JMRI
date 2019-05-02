package jmri.jmris;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmris.JmriServer class 
 *
 * @author Paul Bender
 */
public class JmriServerTest {

    @Test
    public void testCtorDefault() {
        JmriServer a = new JmriServer();
        Assert.assertNotNull(a);
    }

    @Test
    public void testCtorPort() {
        JmriServer a = new JmriServer(25520);
        Assert.assertNotNull(a);
        jmri.util.JUnitAppender.suppressErrorMessage("Failed to connect to port 25520");
    }

    @Test
    public void testCtorPortAndTimeout() {
        JmriServer a = new JmriServer(25520,100);
        Assert.assertNotNull(a);
        jmri.util.JUnitAppender.suppressErrorMessage("Failed to connect to port 25520");
    }

    @Before
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();

    }

}
