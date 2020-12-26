package jmri.jmrix.lenz.swing.lv102;

import java.awt.GraphicsEnvironment;

import javax.swing.JFrame;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Tests for the jmri.jmrix.lenz.lv102.LV102Action class
 *
 * @author Bob Jacobsen Copyright (c) 2001, 2002
 */
public class LV102ActionTest {

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
        // wait for frame with the value of MenuItemLV102ConfigurationManager (from the
        // resource bundle ) in title, case insensitive
        // first boolean is false for exact to allow substring to match
        // second boolean is false to all case insensitive match
        JFrame frame = JFrameOperator.waitJFrame(Bundle.getMessage("MenuItemLV102ConfigurationManager"), false, false);
        Assert.assertNotNull(frame);
        // verify the action provided the expected frame class
        Assert.assertEquals(LV102Frame.class.getName(), frame.getClass().getName());
        frame.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
