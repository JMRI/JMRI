//SimpleServerFrameTest.java
package jmri.jmris.simpleserver;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmris.simpleserver.SimpleServerFrame class
 *
 * @author Paul Bender
 */
public class SimpleServerFrameTest {

    @Test
    public void testCtorDefault() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SimpleServerFrame a = new SimpleServerFrame();
        Assert.assertNotNull(a);
        JUnitUtil.dispose(a);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
