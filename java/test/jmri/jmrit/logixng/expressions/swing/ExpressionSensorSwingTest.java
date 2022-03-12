package jmri.jmrit.logixng.expressions.swing;

import java.awt.GraphicsEnvironment;

import javax.swing.JPanel;

import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.IfThenElse;
import jmri.jmrit.logixng.expressions.ExpressionSensor;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterfaceTestBase;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.*;

/**
 * Test ExpressionSensorSwing
 *
 * @author Daniel Bergqvist 2018
 */
public class ExpressionSensorSwingTest extends SwingConfiguratorInterfaceTestBase {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        ExpressionSensorSwing t = new ExpressionSensorSwing();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testPanel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        ExpressionSensorSwing t = new ExpressionSensorSwing();
        JPanel panel = t.getConfigPanel(new JPanel());
        Assert.assertNotNull("exists",panel);
    }

    @Test
    public void testCreatePanel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Assert.assertTrue("panel is not null",
            null != new ExpressionSensorSwing().getConfigPanel(new JPanel()));
        Assert.assertTrue("panel is not null",
            null != new ExpressionSensorSwing().getConfigPanel(new ExpressionSensor("IQDE1", null), new JPanel()));
    }

    @org.junit.Ignore("Fails in Java 11 testing")
    @Test
    public void testDialogUseExistingSensor() throws SocketAlreadyConnectedException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Sensor s1 = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        InstanceManager.getDefault(SensorManager.class).provide("IS2");

        jmri.jmrit.logixng.LogixNG logixNG = InstanceManager.getDefault(jmri.jmrit.logixng.LogixNG_Manager.class)
                .createLogixNG("A logixNG with an empty conditionlNG");
        ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "IQC1", null);

        IfThenElse action = new IfThenElse("IQDA1", null);
        MaleSocket maleSocket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
        conditionalNG.getChild(0).connect(maleSocket);

        ExpressionSensor expression = new ExpressionSensor("IQDE1", null);
        maleSocket = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
        action.getChild(0).connect(maleSocket);

        JDialogOperator jdo = editItem(conditionalNG, "Edit ConditionalNG IQC1", "Edit ? ", 1);

        new JComboBoxOperator(jdo, 0).setSelectedItem(s1);
        new JComboBoxOperator(jdo, 1).setSelectedItem(Is_IsNot_Enum.IsNot);
        new JComboBoxOperator(jdo, 2).setSelectedItem(ExpressionSensor.SensorState.Inactive);
        new JButtonOperator(jdo, "OK").push();  // NOI18N

        JUnitUtil.waitFor(() -> {return expression.getSensor() != null;});

        Assert.assertEquals("IS1", expression.getSensor().getBean().getSystemName());
        Assert.assertEquals(ExpressionSensor.SensorState.Inactive, expression.getBeanState());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        InstanceManager.getDefault(LogixNGPreferences.class).setShowSystemUserNames(true);
        JUnitUtil.initLogixNGManager();
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }

}
