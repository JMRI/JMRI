package jmri.jmrit.logixng.swing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test SwingConfiguratorInterface
 *
 * @author Daniel Bergqvist (C) 2020
 */
public class SwingConfiguratorInterfaceTest {

    @Test
    @DisabledIfHeadless
    public void testSwingTools() {

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

        JLabel lbl0 = assertInstanceOf( JLabel.class, list.get(0));
        assertEquals("If turnout ", lbl0.getText());

        assertEquals(component2_Turnout, list.get(1));

        JLabel lbl2 = assertInstanceOf( JLabel.class, list.get(2));
        assertEquals(" ", lbl2.getText());

        assertEquals(component1_Is_IsNot, list.get(3));

        JLabel lbl4 = assertInstanceOf( JLabel.class, list.get(4));
        assertEquals(" ", lbl4.getText());

        assertEquals(component4_thrownClosed, list.get(5));

        JLabel lbl6 = assertInstanceOf( JLabel.class, list.get(6));
        assertEquals(" then check if sensor ", lbl6.getText());

        assertEquals(component0_sensor, list.get(7));

        JLabel lbl8 = assertInstanceOf( JLabel.class, list.get(8));
        assertEquals(" is ", lbl8.getText());

        assertEquals(component3_activeInactive, list.get(9));

        JLabel lbl10 = assertInstanceOf( JLabel.class, list.get(10));
        assertEquals(" now", lbl10.getText());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
    }

    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
