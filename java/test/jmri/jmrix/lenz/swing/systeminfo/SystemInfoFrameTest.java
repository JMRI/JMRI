package jmri.jmrix.lenz.swing.systeminfo;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * SystemInfoFrameTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.swing.systeminfo.SystemInfoFrame
 * class
 *
 * @author	Paul Bender
 */
public class SystemInfoFrameTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        jmri.jmrix.lenz.XNetInterfaceScaffold t = new jmri.jmrix.lenz.XNetInterfaceScaffold(new jmri.jmrix.lenz.LenzCommandStation());
        jmri.jmrix.lenz.XNetSystemConnectionMemo memo = new jmri.jmrix.lenz.XNetSystemConnectionMemo(t);

        SystemInfoFrame f = new SystemInfoFrame(memo);
        Assert.assertNotNull(f);
        f.dispose();
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
