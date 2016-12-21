package jmri.jmrix.lenz.swing.liusb;

import java.awt.GraphicsEnvironment;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import javax.swing.JFrame;
import org.netbeans.jemmy.operators.JFrameOperator;
import java.util.ResourceBundle;

/**
 * Tests for the jmri.jmrix.lenz.packetgen.LIUSBConfigAction class
 *
 * @author	Bob Jacobsen Copyright (c) 2001, 2002
 */
public class LIUSBConfigActionTest {
       
    private jmri.jmrix.lenz.XNetInterfaceScaffold t = null;
    private jmri.jmrix.lenz.XNetSystemConnectionMemo memo = null;

    @Test
    public void testStringCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LIUSBConfigAction action = new LIUSBConfigAction("XNet Test Action",memo);
        Assert.assertNotNull(action);
    }

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LIUSBConfigAction action = new LIUSBConfigAction(memo);
        Assert.assertNotNull(action);
    }

    @Test
    public void testActionCreateAndFire() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LIUSBConfigAction action = new LIUSBConfigAction("LIUSB Configuration",memo);
        action.actionPerformed(null);
        // wait for frame with "LIUSB Configuration Utility" in title, case insensitive
        // first boolean is false for exact to allow substring to match
        // second boolean is false to all case insensitive match
        JFrame frame = JFrameOperator.waitJFrame("LIUSB Configuration Utility", false, false);
        Assert.assertNotNull(frame);
        // verify the action provided the expected frame class
        Assert.assertEquals(LIUSBConfigFrame.class.getName(), frame.getClass().getName());
        frame.dispose();
    }



    @Before
    public void setUp(){
       apps.tests.Log4JFixture.setUp();
       jmri.util.JUnitUtil.resetInstanceManager();
       t = new jmri.jmrix.lenz.XNetInterfaceScaffold(new jmri.jmrix.lenz.LenzCommandStation());
       memo = new jmri.jmrix.lenz.XNetSystemConnectionMemo(t);
    }

    @After
    public void tearDown(){
       t = null;
       memo = null;
       jmri.util.JUnitUtil.resetInstanceManager();
       apps.tests.Log4JFixture.tearDown();
    }

}
