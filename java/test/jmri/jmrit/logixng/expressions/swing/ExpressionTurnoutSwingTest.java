package jmri.jmrit.logixng.expressions.swing;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.JDialog;
import javax.swing.JPanel;

import jmri.InstanceManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.IfThenElse;
import jmri.jmrit.logixng.expressions.ExpressionTurnout;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterfaceTestBase;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.netbeans.jemmy.operators.*;

/**
 * Test ExpressionTurnoutSwing
 *
 * @author Daniel Bergqvist 2018
 */
public class ExpressionTurnoutSwingTest extends SwingConfiguratorInterfaceTestBase {

    @Test
    @DisabledIfHeadless
    public void testCtor() {

        ExpressionTurnoutSwing t = new ExpressionTurnoutSwing();
        assertNotNull( t, "exists");
    }

    @Test
    @DisabledIfHeadless
    public void testPanel() {

        JDialog dialog = new JDialog();

        ExpressionTurnoutSwing t = new ExpressionTurnoutSwing(dialog);
        JPanel panel = t.getConfigPanel(new JPanel());
        assertNotNull( panel, "exists");
    }

    @Test
    @DisabledIfHeadless
    public void testCreatePanel() {

        JDialog dialog = new JDialog();

        assertNotNull( new ExpressionTurnoutSwing(dialog).getConfigPanel(new JPanel()));
        assertNotNull( new ExpressionTurnoutSwing(dialog).getConfigPanel(new ExpressionTurnout("IQDE1", null), new JPanel()),
            "panel is not null");
    }

    private ConditionalNG conditionalNG = null;
    private ExpressionTurnout expression = null;

    @Disabled("Fails in Java 11 testing")
    @Test
    @DisabledIfHeadless
    public void testDialogUseExistingTurnout() {

        Turnout t1 = InstanceManager.getDefault(TurnoutManager.class).provide("IT1");
        InstanceManager.getDefault(TurnoutManager.class).provide("IT2");

        boolean complete = ThreadingUtil.runOnGUIwithReturn(() -> {

            assertDoesNotThrow( () ->  {
                jmri.jmrit.logixng.LogixNG logixNG = InstanceManager.getDefault(jmri.jmrit.logixng.LogixNG_Manager.class)
                        .createLogixNG("A logixNG with an empty conditionlNG");
                conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "IQC1", null);

                IfThenElse action = new IfThenElse("IQDA1", null);
                MaleSocket maleSocket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
                conditionalNG.getChild(0).connect(maleSocket);

                expression = new ExpressionTurnout("IQDE1", null);
                maleSocket = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
                action.getChild(0).connect(maleSocket);
            //} catch (SocketAlreadyConnectedException e) {
            //    Assert.fail("SocketAlreadyConnectedException");
            });
            return true;
        });
        assertTrue(complete);

        JDialogOperator jdo = editItem(conditionalNG, "Edit ConditionalNG IQC1", "Edit ? ", 1);

        new JComboBoxOperator(jdo, 0).setSelectedItem(t1);
        new JComboBoxOperator(jdo, 1).setSelectedItem(Is_IsNot_Enum.IsNot);
        new JComboBoxOperator(jdo, 2).setSelectedItem(ExpressionTurnout.TurnoutState.Closed);
        new JButtonOperator(jdo, "OK").push();  // NOI18N

        JUnitUtil.waitFor(() -> {return expression.getSelectNamedBean().getNamedBean() != null;}, "nb not null");

        assertEquals("IT1", expression.getSelectNamedBean().getNamedBean().getBean().getSystemName());
        assertEquals(ExpressionTurnout.TurnoutState.Closed, expression.getBeanState());
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
