package jmri.jmrix.lenz.swing.lz100;

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
 * Tests for the jmri.jmrix.lenz.packetgen.LZ100Action class
 *
 * @author	Bob Jacobsen Copyright (c) 2001, 2002
 */
public class LZ100ActionTest {

    private jmri.jmrix.lenz.XNetSystemConnectionMemo memo = null;
    private final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.lenz.swing.lz100.LZ100Bundle");


    @Test
    public void testStringCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LZ100Action action = new LZ100Action("XNet Test Action",memo);
        Assert.assertNotNull(action);
    }

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LZ100Action action = new LZ100Action(memo);
        Assert.assertNotNull(action);
    }

    @Test
    public void testActionCreateAndFire() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LZ100Action action = new LZ100Action("LZ100 Configuration Utility",memo);
        action.actionPerformed(null);
        // wait for frame with the value of "Z100Config" (from the 
        // resource bundle ) in title, case insensitive
        // first boolean is false for exact to allow substring to match
        // second boolean is false to all case insensitive match
        JFrame frame = JFrameOperator.waitJFrame(rb.getString("LZ100Config"), false, false);
        Assert.assertNotNull(frame);
        // verify the action provided the expected frame class
        Assert.assertEquals(LZ100Frame.class.getName(), frame.getClass().getName());
        frame.dispose();
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
