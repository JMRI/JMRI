package jmri.jmrit.logixng.swing;

import java.awt.GraphicsEnvironment;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test SwingConfiguratorInterface
 * 
 * @author Daniel Bergqvist (C) 2020
 */
public class SwingConfiguratorInterfaceTest {
    
    @Test
    public void testSwingTools() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        // This test tests that the components can be in a different order than
        // expected, since different languages may order words in a different way.
        
        // If turnout 'IT2' 'is' 'thrown' then check if sensor 'IS34' is 'active' now
        String message = "If turnout {2} {1} {4} then check if sensor {0} is {3} now";
        JTextField component2_Turnout = new JTextField();
        JTextField component1_Is_IsNot = new JTextField();
        JTextField component4_thrownClosed = new JTextField();
        JTextField component0_sensor = new JTextField();
        JTextField component3_activeInactive = new JTextField();
        
        JComponent[] components = new JComponent[]{
            component0_sensor,
            component1_Is_IsNot,
            component2_Turnout,
            component3_activeInactive,
            component4_thrownClosed};
        
        List<JComponent> list = SwingConfiguratorInterface.parseMessage(message, components);
        
        Assert.assertTrue(list.get(0) instanceof JLabel);
        Assert.assertEquals("If turnout ", ((JLabel)list.get(0)).getText());
        
        Assert.assertEquals(component2_Turnout, list.get(1));
        
        Assert.assertTrue(list.get(2) instanceof JLabel);
        Assert.assertEquals(" ", ((JLabel)list.get(2)).getText());
        
        Assert.assertEquals(component1_Is_IsNot, list.get(3));
        
        Assert.assertTrue(list.get(4) instanceof JLabel);
        Assert.assertEquals(" ", ((JLabel)list.get(4)).getText());
        
        Assert.assertEquals(component4_thrownClosed, list.get(5));
        
        Assert.assertTrue(list.get(6) instanceof JLabel);
        Assert.assertEquals(" then check if sensor ", ((JLabel)list.get(6)).getText());
        
        Assert.assertEquals(component0_sensor, list.get(7));
        
        Assert.assertTrue(list.get(8) instanceof JLabel);
        Assert.assertEquals(" is ", ((JLabel)list.get(8)).getText());
        
        Assert.assertEquals(component3_activeInactive, list.get(9));
        
        Assert.assertTrue(list.get(10) instanceof JLabel);
        Assert.assertEquals(" now", ((JLabel)list.get(10)).getText());
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }
    
}
