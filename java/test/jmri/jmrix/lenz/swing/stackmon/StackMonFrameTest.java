package jmri.jmrix.lenz.swing.stackmon;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.lenz.swing.stackmon.StackMonFrame class
 *
 * @author Paul Bender
 */
public class StackMonFrameTest extends jmri.util.JmriJFrameTestBase {

    private jmri.jmrix.lenz.XNetInterfaceScaffold t = null;
    private jmri.jmrix.lenz.XNetSystemConnectionMemo memo = null;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        t = new jmri.jmrix.lenz.XNetInterfaceScaffold(new jmri.jmrix.lenz.LenzCommandStation());
        memo = new jmri.jmrix.lenz.XNetSystemConnectionMemo(t);
        if(!GraphicsEnvironment.isHeadless()){
           frame = new StackMonFrame(memo);
        }
    }

    @AfterEach
    @Override
    public void tearDown() {
        memo = null;
        t = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        super.tearDown();
    }

}
