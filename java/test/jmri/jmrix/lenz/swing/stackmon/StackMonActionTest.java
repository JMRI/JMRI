package jmri.jmrix.lenz.swing.stackmon;

import java.awt.GraphicsEnvironment;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import javax.swing.JFrame;
import org.netbeans.jemmy.operators.JFrameOperator;
import java.util.ResourceBundle;

/**
 * StackMonActionTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.swing.stackmon.StackMonAction class
 *
 * @author	Paul Bender
 */
public class StackMonActionTest {

    private jmri.jmrix.lenz.XNetSystemConnectionMemo memo = null;
    final private ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.lenz.swing.stackmon.StackMonBundle");

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        StackMonAction f = new StackMonAction(memo);
        Assert.assertNotNull(f);
    }

    @Test
    public void testActionCreateAndFire() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        StackMonAction action = new StackMonAction("Stack Monitor",memo);
        action.actionPerformed(null);
        // wait for frame with the value of "StackMonitortitle" (from the 
        // resource bundle ) in title, case insensitive
        // first boolean is false for exact to allow substring to match
        // second boolean is false to all case insensitive match
        JFrame frame = JFrameOperator.waitJFrame(rb.getString("StackMonitorTitle"), false, false);
        Assert.assertNotNull(frame);
        // verify the action provided the expected frame class
        Assert.assertEquals(StackMonFrame.class.getName(), frame.getClass().getName());
        frame.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.jmrix.lenz.XNetInterfaceScaffold t = new jmri.jmrix.lenz.XNetInterfaceScaffold(new jmri.jmrix.lenz.LenzCommandStation());
        memo = new jmri.jmrix.lenz.XNetSystemConnectionMemo(t);

    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
