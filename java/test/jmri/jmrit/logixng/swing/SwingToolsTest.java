package jmri.jmrit.logixng.swing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import jmri.jmrit.logixng.DigitalActionBean;
import jmri.jmrit.logixng.actions.ActionTurnout;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test SwingToolsTest
 *
 * @author Daniel Bergqvist 2018
 */
public class SwingToolsTest {

    @Test
    @DisabledIfHeadless
    public void testSwingTools() {

        DigitalActionBean action = new ActionTurnout("IQDA1", null);
        Class<ActionTurnout> actionClass = ActionTurnout.class;

        assertEquals(  "jmri.jmrit.logixng.actions.swing.ActionTurnoutSwing",
            SwingTools.adapterNameForObject(action), "Class name is correct");

        assertEquals( "jmri.jmrit.logixng.actions.swing.ActionTurnoutSwing",
            SwingTools.adapterNameForClass(actionClass), "Class name is correct");

        assertEquals( "jmri.jmrit.logixng.actions.swing.ActionTurnoutSwing",
            SwingTools.getSwingConfiguratorForObject(action).getClass().getName(), "Class name is correct");

        assertEquals( "jmri.jmrit.logixng.actions.swing.ActionTurnoutSwing",
            SwingTools.getSwingConfiguratorForClass(actionClass).getClass().getName(), "Class name is correct");

        // The class SwingToolsTest does not have a swing configurator
        SwingConfiguratorInterface iface = SwingTools.getSwingConfiguratorForObject(this);
        assertNull( iface, "interface is null");
        JUnitAppender.assertErrorMessage("Cannot load SwingConfiguratorInterface adapter for jmri.jmrit.logixng.swing.SwingToolsTest");
        JUnitAppender.assertErrorMessage("Cannot load SwingConfiguratorInterface for jmri.jmrit.logixng.swing.SwingToolsTest");

        // The class SwingToolsTest does not have a swing configurator
        iface = SwingTools.getSwingConfiguratorForClass(this.getClass());
        assertNull( iface, "interface is null");
        JUnitAppender.assertErrorMessage("Cannot load SwingConfiguratorInterface adapter for jmri.jmrit.logixng.swing.SwingToolsTest");
        JUnitAppender.assertErrorMessage("Cannot load SwingConfiguratorInterface for jmri.jmrit.logixng.swing.SwingToolsTest");
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
