package jmri.jmrit.logixng.actions.swing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.swing.JDialog;
import javax.swing.JPanel;

import jmri.InstanceManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionTurnout;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterfaceTestBase;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.netbeans.jemmy.operators.*;

/**
 * Test ActionTurnoutSwing
 *
 * @author Daniel Bergqvist 2018
 */
public class ActionTurnoutSwingTest extends SwingConfiguratorInterfaceTestBase {

    @Test
    @DisabledIfHeadless
    public void testCtor() {

        ActionTurnoutSwing t = new ActionTurnoutSwing(new JDialog());
        assertNotNull( t, "exists");
    }

    @Test
    @DisabledIfHeadless
    public void testCreatePanel() {

        JDialog dialog = new JDialog();

        assertNotNull( new ActionTurnoutSwing(dialog).getConfigPanel(new JPanel()), "panel is not null");
        assertNotNull( new ActionTurnoutSwing(dialog).getConfigPanel(new ActionTurnout("IQDA1", null), new JPanel()),
                "panel is not null");
    }

    @Disabled("Fails in Java 11 testing")
    @Test
    @DisabledIfHeadless
    public void testDialogUseExistingTurnout() throws SocketAlreadyConnectedException {

        Turnout t1 = InstanceManager.getDefault(TurnoutManager.class).provide("IT1");
        InstanceManager.getDefault(TurnoutManager.class).provide("IT2");

        jmri.jmrit.logixng.LogixNG logixNG = InstanceManager.getDefault(jmri.jmrit.logixng.LogixNG_Manager.class)
                .createLogixNG("A logixNG with an empty conditionlNG");
        ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "IQC1", null);

        ActionTurnout action = new ActionTurnout("IQDA1", null);
        MaleSocket maleSocket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
        conditionalNG.getChild(0).connect(maleSocket);

        JDialogOperator jdo = editItem(conditionalNG, "Edit ConditionalNG IQC1", "Edit ! ", 0);

        new JComboBoxOperator(jdo, 0).setSelectedItem(t1);
        new JComboBoxOperator(jdo, 1).setSelectedItem(ActionTurnout.TurnoutState.Closed);
        new JButtonOperator(jdo, "OK").push();  // NOI18N

        JUnitUtil.waitFor(() -> {return action.getSelectNamedBean().getNamedBean() != null;}, "nb not null");
        JUnitUtil.waitFor(() -> {return ActionTurnout.TurnoutState.Closed == action.getSelectEnum().getEnum();}, "turnout closed");

        assertEquals("IT1", action.getSelectNamedBean().getNamedBean().getBean().getSystemName());
        assertEquals(ActionTurnout.TurnoutState.Closed, action.getSelectEnum().getEnum());
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
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
