package jmri.jmrit.logixng.digital.expressions.swing;

import java.awt.GraphicsEnvironment;

import javax.swing.JPanel;

import jmri.InstanceManager;
import jmri.LightManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.digital.actions.IfThenElse;
import jmri.jmrit.logixng.digital.expressions.ExpressionLight;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterfaceTestBase;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.*;

/**
 * Test ActionLight
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
    
    @Test
    public void testDialogUseExistingLight() throws SocketAlreadyConnectedException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        InstanceManager.getDefault(LightManager.class).provide("IL1");
        InstanceManager.getDefault(LightManager.class).provide("IL2");

        ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG("IQC1", null);
        
        IfThenElse action = new IfThenElse("IQDA1", null, IfThenElse.Type.TRIGGER_ACTION);
        MaleSocket maleSocket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
        conditionalNG.getChild(0).connect(maleSocket);
        
        ExpressionLight expression = new ExpressionLight("IQDE1", null);
        maleSocket = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
        action.getChild(0).connect(maleSocket);
        
        JDialogOperator jdo = editItem(conditionalNG, "Edit ConditionalNG IQC1", "Edit ? ", 1);
        
        new JComboBoxOperator(jdo, 0).setSelectedIndex(1);
        new JComboBoxOperator(jdo, 1).setSelectedItem(Is_IsNot_Enum.IS_NOT);
        new JComboBoxOperator(jdo, 2).setSelectedItem(ExpressionLight.LightState.OFF);
        
        if (1==0)
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
//                Exceptions.printStackTrace(ex);
            }
        
        new JButtonOperator(jdo, "OK").push();  // NOI18N
        
        Assert.assertEquals("IL1", expression.getLight().getBean().getSystemName());
        Assert.assertEquals(ExpressionLight.LightState.OFF, expression.getLightState());
    }

    @Test
    public void testDialogCreateNewLight() throws SocketAlreadyConnectedException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        InstanceManager.getDefault(LightManager.class).provide("IL1");
        InstanceManager.getDefault(LightManager.class).provide("IL2");

        ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG("IQC1", null);
        
        IfThenElse action = new IfThenElse("IQDA1", null, IfThenElse.Type.TRIGGER_ACTION);
        MaleSocket maleSocket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
        conditionalNG.getChild(0).connect(maleSocket);
        
        ExpressionLight expression = new ExpressionLight("IQDE1", null);
        maleSocket = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
        action.getChild(0).connect(maleSocket);
        
        JDialogOperator jdo = editItem(conditionalNG, "Edit ConditionalNG IQC1", "Edit ? ", 1);
        
        new JRadioButtonOperator(jdo, 1).clickMouse();
        new JTextFieldOperator(jdo, 2).enterText("IL99");
        new JComboBoxOperator(jdo, 1).setSelectedItem(Is_IsNot_Enum.IS);
        new JComboBoxOperator(jdo, 2).setSelectedItem(ExpressionLight.LightState.ON);
        
        if (1==0)
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
//                Exceptions.printStackTrace(ex);
            }
        
        new JButtonOperator(jdo, "OK").push();  // NOI18N
        
        Assert.assertEquals("IL99", expression.getLight().getBean().getSystemName());
        Assert.assertEquals(ExpressionLight.LightState.ON, expression.getLightState());
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
        JUnitUtil.initLogixNGManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
