package jmri.jmrix.lenz.swing.systeminfo;

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
 * Tests for the jmri.jmrix.lenz.packetgen.SystemInfoAction class
 *
 * @author	Bob Jacobsen Copyright (c) 2001, 2002
 */
public class SystemInfoActionTest {
        
    private jmri.jmrix.lenz.XNetSystemConnectionMemo memo = null;

    @Test
    public void testStringCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SystemInfoAction action = new SystemInfoAction("XNet Test Action",memo);
        Assert.assertNotNull(action);
    }

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SystemInfoAction action = new SystemInfoAction(memo);
        Assert.assertNotNull(action);
    }

    @Test
    public void testActionCreateAndFire() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SystemInfoAction action = new SystemInfoAction("Xpressnet System Information",memo);
        action.actionPerformed(null);
        // wait for frame with the value of "XPressNet System Information 
        // in title, case insensitive
        // first boolean is false for exact to allow substring to match
        // second boolean is false to all case insensitive match
        JFrame frame = JFrameOperator.waitJFrame("XPressNet System Information", false, false);
        Assert.assertNotNull(frame);
        // verify the action provided the expected frame class
        Assert.assertEquals(SystemInfoFrame.class.getName(), frame.getClass().getName());
    }

    @Before
    public void setUp(){
       apps.tests.Log4JFixture.setUp();
       jmri.util.JUnitUtil.resetInstanceManager();
        jmri.jmrix.lenz.XNetInterfaceScaffold t = new jmri.jmrix.lenz.XNetInterfaceScaffold(new jmri.jmrix.lenz.LenzCommandStation());
        memo = new jmri.jmrix.lenz.XNetSystemConnectionMemo(t);
    }

    @After
    public void tearDown(){
       jmri.util.JUnitUtil.resetInstanceManager();
       apps.tests.Log4JFixture.tearDown();
    }

}
