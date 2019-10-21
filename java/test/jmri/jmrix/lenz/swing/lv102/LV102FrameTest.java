package jmri.jmrix.lenz.swing.lv102;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the jmri.jmrix.lenz.swing.lv102.LV102Frame class
 *
 * @author	Paul Bender
 */
public class LV102FrameTest extends jmri.util.JmriJFrameTestBase {

    @Test
    public void testCloseButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        frame.setVisible(true);
        LV102FrameScaffold operator = new LV102FrameScaffold();
        operator.pushCloseButton();
    }

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        if(!GraphicsEnvironment.isHeadless()){
           frame = new LV102Frame();
        }
    }

    @After
    @Override
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        super.tearDown();
    }

}
