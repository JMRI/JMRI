package jmri.jmrix.lenz.swing.lz100;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * LZ100InternalFrameTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.swing.lz100.LZ100InternalFrame class
 *
 * @author	Paul Bender
 */
public class LZ100InternalFrameTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // infrastructure objects
        XNetInterfaceScaffold tc = new XNetInterfaceScaffold(new LenzCommandStation());

        LZ100InternalFrame f = new LZ100InternalFrame(new XNetSystemConnectionMemo(tc));
        Assert.assertNotNull(f);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
	    JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
