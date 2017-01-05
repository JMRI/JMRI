package jmri.jmrix.lenz.swing.lv102;

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
 * Tests for the jmri.jmrix.lenz.lv102.LV102Action class
 *
 * @author	Bob Jacobsen Copyright (c) 2001, 2002
 */
public class LV102ActionTest {

    private ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrix.lenz.swing.lv102.LV102Bundle");

    @Test
    public void testStringCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LV102Action action = new LV102Action("XNet Test Action");
        Assert.assertNotNull(action);
    }

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LV102Action action = new LV102Action();
        Assert.assertNotNull(action);
    }

    @Test
    public void testActionCreateAndFire() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LV102Action action = new LV102Action("LV102 Configuration Manager");
        action.actionPerformed(null);
        // wait for frame with the value of LV102config (from the 
        // resource bundle ) in title, case insensitive
        // first boolean is false for exact to allow substring to match
        // second boolean is false to all case insensitive match
        JFrame frame = JFrameOperator.waitJFrame(rb.getString("LV102Config"), false, false);
        Assert.assertNotNull(frame);
        // verify the action provided the expected frame class
        Assert.assertEquals(LV102Frame.class.getName(), frame.getClass().getName());
        frame.dispose();
    }

    @Before
    public void setUp(){
       apps.tests.Log4JFixture.setUp();
       jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown(){
       jmri.util.JUnitUtil.resetInstanceManager();
       apps.tests.Log4JFixture.tearDown();
    }

}
