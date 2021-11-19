package jmri.jmrit.logixng.expressions.swing;

import java.awt.GraphicsEnvironment;

import javax.swing.JPanel;

import jmri.InstanceManager;
import jmri.Light;
import jmri.LightManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.IfThenElse;
import jmri.jmrit.logixng.expressions.ExpressionLight;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterfaceTestBase;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.*;

/**
 * Test ExpressionLightSwing
 *
 * @author Daniel Bergqvist 2018
 */
public class ExpressionLightSwingTest extends SwingConfiguratorInterfaceTestBase {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        ExpressionLightSwing t = new ExpressionLightSwing();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testPanel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        ExpressionLightSwing t = new ExpressionLightSwing();
        JPanel panel = t.getConfigPanel(new JPanel());
        Assert.assertNotNull("exists",panel);
    }

    @org.junit.Ignore("Fails in Java 11 testing")
    @Test
    public void testDialogUseExistingLight() throws SocketAlreadyConnectedException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Light l1 = InstanceManager.getDefault(LightManager.class).provide("IL1");
        InstanceManager.getDefault(LightManager.class).provide("IL2");

        jmri.jmrit.logixng.LogixNG logixNG = InstanceManager.getDefault(jmri.jmrit.logixng.LogixNG_Manager.class)
                .createLogixNG("A logixNG with an empty conditionlNG");
        ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "IQC1", null);

        IfThenElse action = new IfThenElse("IQDA1", null);
        MaleSocket maleSocket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
        conditionalNG.getChild(0).connect(maleSocket);

        ExpressionLight expression = new ExpressionLight("IQDE1", null);
        maleSocket = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
        action.getChild(0).connect(maleSocket);

        JDialogOperator jdo = editItem(conditionalNG, "Edit ConditionalNG IQC1", "Edit ? ", 1);

        new JComboBoxOperator(jdo, 0).setSelectedItem(l1);
        new JComboBoxOperator(jdo, 1).setSelectedItem(Is_IsNot_Enum.IsNot);
        new JComboBoxOperator(jdo, 2).setSelectedItem(ExpressionLight.LightState.Off);

        new JButtonOperator(jdo, "OK").push();  // NOI18N

        JUnitUtil.waitFor(() -> {return expression.getLight() != null;});

        Assert.assertEquals("IL1", expression.getLight().getBean().getSystemName());
        Assert.assertEquals(ExpressionLight.LightState.Off, expression.getBeanState());
    }

    @Test
    public void testCreatePanel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        Assert.assertTrue("panel is not null",
            null != new ExpressionLightSwing().getConfigPanel(new JPanel()));
        Assert.assertTrue("panel is not null",
            null != new ExpressionLightSwing().getConfigPanel(new ExpressionLight("IQDE1", null), new JPanel()));
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalLightManager();
        InstanceManager.getDefault(LogixNGPreferences.class).setShowSystemUserNames(true);
        JUnitUtil.initLogixNGManager();
    }

    @After
    public void tearDown() {
        // Java 11 integration temporary - clear messages to get JUnit 5 traceback
        jmri.util.JUnitAppender.clearBacklog(org.apache.log4j.Level.ERROR);  // REMOVE THIS!!!

        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }

}
