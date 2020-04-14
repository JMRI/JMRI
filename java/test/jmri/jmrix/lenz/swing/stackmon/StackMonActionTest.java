package jmri.jmrix.lenz.swing.stackmon;

import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Tests for the jmri.jmrix.lenz.swing.stackmon.StackMonAction class
 *
 * @author	Paul Bender
 */
public class StackMonActionTest {

    private jmri.jmrix.lenz.XNetSystemConnectionMemo memo = null;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        StackMonAction f = new StackMonAction(memo);
        Assert.assertNotNull(f);
    }

    @Test
    public void testActionCreateAndFire() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        StackMonAction action = new StackMonAction(Bundle.getMessage("MenuItemCSDatabaseManager"),memo);
        action.actionPerformed(null);
        // wait for frame with the value of "StackMonitortitle" (from the 
        // resource bundle ) in title, case insensitive
        // first boolean is false for exact to allow substring to match
        // second boolean is false to all case insensitive match
        JFrame frame = JFrameOperator.waitJFrame(Bundle.getMessage("MenuItemCSDatabaseManager"), false, false);
        Assert.assertNotNull(frame);
        // verify the action provided the expected frame class
        Assert.assertEquals(StackMonFrame.class.getName(), frame.getClass().getName());
        frame.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();

        jmri.jmrix.lenz.XNetInterfaceScaffold t = new jmri.jmrix.lenz.XNetInterfaceScaffold(new jmri.jmrix.lenz.LenzCommandStation());
        memo = new jmri.jmrix.lenz.XNetSystemConnectionMemo(t);
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
