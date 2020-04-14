package jmri.jmrix.lenz.swing.li101;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * LI101FrameTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.swing.li101.LI101Frame class
 *
 * @author	Paul Bender
 */
public class LI101FrameTest extends jmri.util.JmriJFrameTestBase {

    private jmri.jmrix.lenz.XNetInterfaceScaffold t = null;
    private jmri.jmrix.lenz.XNetSystemConnectionMemo memo = null;

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        t = new jmri.jmrix.lenz.XNetInterfaceScaffold(new jmri.jmrix.lenz.LenzCommandStation());
        memo = new jmri.jmrix.lenz.XNetSystemConnectionMemo(t);
        if(!GraphicsEnvironment.isHeadless()){
           frame = new LI101Frame(memo);
        }
    }

    @After
    @Override
    public void tearDown() {
        memo = null;
        t = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        super.tearDown();
    }

}
