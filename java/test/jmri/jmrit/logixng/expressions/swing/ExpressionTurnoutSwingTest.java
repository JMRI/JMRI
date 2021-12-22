package jmri.jmrit.logixng.expressions.swing;

import java.awt.GraphicsEnvironment;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JPanel;

import jmri.InstanceManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.IfThenElse;
import jmri.jmrit.logixng.expressions.ExpressionTurnout;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterfaceTestBase;
import jmri.jmrit.logixng.tools.swing.TreeEditor;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.*;

/**
 * Test ExpressionTurnoutSwing
 *
 * @author Daniel Bergqvist 2018
 */
public class ExpressionTurnoutSwingTest
        extends SwingConfiguratorInterfaceTestBase
        implements PropertyChangeListener {

    private final AtomicBoolean propertyChanged = new AtomicBoolean();

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        ExpressionTurnoutSwing t = new ExpressionTurnoutSwing();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testPanel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        ExpressionTurnoutSwing t = new ExpressionTurnoutSwing();
        JPanel panel = t.getConfigPanel(new JPanel());
        Assert.assertNotNull("exists",panel);
    }

    @Test
    public void testCreatePanel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Assert.assertTrue("panel is not null",
            null != new ExpressionTurnoutSwing().getConfigPanel(new JPanel()));
        Assert.assertTrue("panel is not null",
            null != new ExpressionTurnoutSwing().getConfigPanel(new ExpressionTurnout("IQDE1", null), new JPanel()));
    }

    ConditionalNG conditionalNG = null;
    ExpressionTurnout expression = null;

    @Test
    public void testDialogUseExistingTurnout() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Turnout t1 = InstanceManager.getDefault(TurnoutManager.class).provide("IT1");
        InstanceManager.getDefault(TurnoutManager.class).provide("IT2");

        ThreadingUtil.runOnGUI(() -> {

            try {
                jmri.jmrit.logixng.LogixNG logixNG = InstanceManager.getDefault(jmri.jmrit.logixng.LogixNG_Manager.class)
                        .createLogixNG("A logixNG with an empty conditionlNG");
                conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "IQC1", null);

                IfThenElse action = new IfThenElse("IQDA1", null);
                MaleSocket maleSocket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
                conditionalNG.getChild(0).connect(maleSocket);

                expression = new ExpressionTurnout("IQDE1", null);
                maleSocket = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
                action.getChild(0).connect(maleSocket);
            } catch (SocketAlreadyConnectedException e) {
                Assert.fail("SocketAlreadyConnectedException");
            }

        });

        JDialogOperator jdo = editItem(conditionalNG, "Edit ConditionalNG IQC1", "Edit ? ", 1);
        TreeEditor treeEditor = (TreeEditor)JFrameOperator.findJFrame("Edit ConditionalNG IQC1", true, true);
        treeEditor.addPropertyChangeListener(this);

        new JComboBoxOperator(jdo, 0).setSelectedItem(t1);
        new JComboBoxOperator(jdo, 1).setSelectedItem(Is_IsNot_Enum.IsNot);
        new JComboBoxOperator(jdo, 2).setSelectedItem(ExpressionTurnout.TurnoutState.Closed);
        propertyChanged.set(false);
        new JButtonOperator(jdo, "OK").push();  // NOI18N
        
        // Wait for the dialog to be closed
        Assert.assertTrue(JUnitUtil.waitFor(() -> {return propertyChanged.get();}));

        JUnitUtil.waitFor(() -> {return expression.getTurnout() != null;});

        Assert.assertEquals("IT1", expression.getTurnout().getBean().getSystemName());
        Assert.assertEquals(ExpressionTurnout.TurnoutState.Closed, expression.getBeanState());
        
        ThreadingUtil.runOnGUI(() -> { treeEditor.dispose(); });

        Assert.assertTrue(JUnitUtil.waitFor(() -> {return !JUnitUtil.hasOpenFrames();}));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        propertyChanged.set(true);
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
        InstanceManager.getDefault(LogixNGPreferences.class).setShowSystemUserNames(true);
        JUnitUtil.initLogixNGManager();
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown(true);
    }

}
