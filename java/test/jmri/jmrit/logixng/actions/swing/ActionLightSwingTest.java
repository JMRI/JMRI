package jmri.jmrit.logixng.actions.swing;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.swing.JDialog;
import javax.swing.JPanel;

import jmri.InstanceManager;
import jmri.Light;
import jmri.LightManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionLight;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterfaceTestBase;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.*;

/**
 * Test ActionLightSwing
 *
 * @author Daniel Bergqvist 2018
 */
public class ActionLightSwingTest extends SwingConfiguratorInterfaceTestBase {

    @Test
    @DisabledIfHeadless
    public void testCtor() {
        ActionLightSwing t = new ActionLightSwing();
        assertNotNull( t, "exists");
    }

    @Test
    @DisabledIfHeadless
    public void testCreatePanel() {

        JDialog dialog = new JDialog();

        assertNotNull( new ActionLightSwing(dialog).getConfigPanel(new JPanel()), "panel is not null");
        assertNotNull( new ActionLightSwing(dialog).getConfigPanel(new ActionLight("IQDA1", null), new JPanel()), "panel is not null");
    }

    private ConditionalNG conditionalNG = null;
    private ActionLight action = null;

    @Disabled("Fails in Java 11 testing")
    @Test
    @DisabledIfHeadless
    public void testDialogUseExistingLight() {

        Light l1 = InstanceManager.getDefault(LightManager.class).provide("IL1");
        InstanceManager.getDefault(LightManager.class).provide("IL2");

        ThreadingUtil.runOnGUI(() -> {
            jmri.jmrit.logixng.LogixNG logixNG = InstanceManager.getDefault(jmri.jmrit.logixng.LogixNG_Manager.class)
                    .createLogixNG("A logixNG with an empty conditionlNG");
            conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "IQC1", null);

            action = new ActionLight("IQDA1", null);
            MaleSocket maleSocket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
            assertDoesNotThrow( () ->
                conditionalNG.getChild(0).connect(maleSocket));
        });

        JDialogOperator jdo = editItem(conditionalNG, "Edit ConditionalNG IQC1", "Edit ! ", 0);

        new JComboBoxOperator(jdo, 0).setSelectedItem(l1);
        new JComboBoxOperator(jdo, 1).setSelectedItem(ActionLight.LightState.Off);
        new JButtonOperator(jdo, "OK").push();  // NOI18N

        JUnitUtil.waitFor(() -> {return action.getSelectNamedBean().getNamedBean() != null;}, "nb not null");
        JUnitUtil.waitFor(() -> {return ActionLight.LightState.Off == action.getSelectEnum().getEnum();}, "Light off");

        assertEquals("IL1", action.getSelectNamedBean().getNamedBean().getBean().getSystemName());
        assertEquals(ActionLight.LightState.Off, action.getSelectEnum().getEnum());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalLightManager();
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
