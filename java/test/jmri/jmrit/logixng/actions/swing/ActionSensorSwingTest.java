package jmri.jmrit.logixng.actions.swing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.swing.JDialog;
import javax.swing.JPanel;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionSensor;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterfaceTestBase;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.netbeans.jemmy.operators.*;

/**
 * Test ActionSensorSwing
 *
 * @author Daniel Bergqvist 2018
 */
public class ActionSensorSwingTest extends SwingConfiguratorInterfaceTestBase {

    @Test
    @DisabledIfHeadless
    public void testCtor() {

        ActionSensorSwing t = new ActionSensorSwing();
        assertNotNull( t, "exists");
    }

    @Test
    @DisabledIfHeadless
    public void testCreatePanel() {

        JDialog dialog = new JDialog();

        assertNotNull( new ActionSensorSwing(dialog).getConfigPanel(new JPanel()), "panel is not null");
        assertNotNull( new ActionSensorSwing(dialog).getConfigPanel(new ActionSensor("IQDA1", null), new JPanel()), "panel is not null");
    }

    @Disabled("Fails in Java 11 testing")
    @Test
    @DisabledIfHeadless
    public void testDialogUseExistingSensor() throws SocketAlreadyConnectedException {

        Sensor s1 = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        InstanceManager.getDefault(SensorManager.class).provide("IS2");

        jmri.jmrit.logixng.LogixNG logixNG = InstanceManager.getDefault(jmri.jmrit.logixng.LogixNG_Manager.class)
                .createLogixNG("A logixNG with an empty conditionlNG");
        ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "IQC1", null);

        ActionSensor action = new ActionSensor("IQDA1", null);
        MaleSocket maleSocket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
        conditionalNG.getChild(0).connect(maleSocket);

        JDialogOperator jdo = editItem(conditionalNG, "Edit ConditionalNG IQC1", "Edit ! ", 0);

        new JComboBoxOperator(jdo, 0).setSelectedItem(s1);
        new JComboBoxOperator(jdo, 1).setSelectedItem(ActionSensor.SensorState.Inactive);
        new JButtonOperator(jdo, "OK").push();  // NOI18N

        JUnitUtil.waitFor(() -> {return action.getSelectNamedBean().getNamedBean() != null;}, "nb not null");

        assertEquals("IS1", action.getSelectNamedBean().getNamedBean().getBean().getSystemName());
        assertEquals(ActionSensor.SensorState.Inactive, action.getSelectEnum().getEnum());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalSensorManager();
        InstanceManager.getDefault(LogixNGPreferences.class).setShowSystemUserNames(true);
        JUnitUtil.initLogixNGManager();
    }

    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
