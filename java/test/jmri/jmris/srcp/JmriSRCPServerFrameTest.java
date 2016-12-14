package jmri.jmris.srcp;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

/**
 * Tests for the jmri.jmris.srcp.JmriSRCPServerFrame class 
 *
 * @author Paul Bender
 */
public class JmriSRCPServerFrameTest {

    @Test
    public void testCtorDefault() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JmriSRCPServerFrame a = new JmriSRCPServerFrame();
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
