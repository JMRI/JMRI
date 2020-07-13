package jmri.jmrit.logixng.digital.expressions.swing;

import java.awt.GraphicsEnvironment;

import javax.swing.JPanel;

import jmri.InstanceManager;
import jmri.SensorManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.digital.actions.IfThenElse;
import jmri.jmrit.logixng.digital.expressions.ExpressionSensor;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterfaceTestBase;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.*;

/**
 * Test ActionSensor
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
    
    @Test
    public void testDialogUseExistingSensor() throws SocketAlreadyConnectedException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        InstanceManager.getDefault(SensorManager.class).provide("IS1");
        InstanceManager.getDefault(SensorManager.class).provide("IS2");

        ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG("IQC1", null);
        
        IfThenElse action = new IfThenElse("IQDA1", null, IfThenElse.Type.TRIGGER_ACTION);
        MaleSocket maleSocket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
        conditionalNG.getChild(0).connect(maleSocket);
        
        ExpressionSensor expression = new ExpressionSensor("IQDE1", null);
        maleSocket = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
        action.getChild(0).connect(maleSocket);
        
        JDialogOperator jdo = editItem(conditionalNG, "Edit ConditionalNG IQC1", "Edit ? ", 1);
        
        new JComboBoxOperator(jdo, 0).setSelectedIndex(1);
        new JComboBoxOperator(jdo, 1).setSelectedItem(Is_IsNot_Enum.IS_NOT);
        new JComboBoxOperator(jdo, 2).setSelectedItem(ExpressionSensor.SensorState.INACTIVE);
        new JButtonOperator(jdo, "OK").push();  // NOI18N
        
        Assert.assertEquals("IS1", expression.getSensor().getBean().getSystemName());
        Assert.assertEquals(ExpressionSensor.SensorState.INACTIVE, expression.getSensorState());
    }
    
    @Test
    public void testDialogCreateNewSensor() throws SocketAlreadyConnectedException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        InstanceManager.getDefault(SensorManager.class).provide("IL1");
        InstanceManager.getDefault(SensorManager.class).provide("IL2");

        ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG("IQC1", null);
        
        IfThenElse action = new IfThenElse("IQDA1", null, IfThenElse.Type.TRIGGER_ACTION);
        MaleSocket maleSocket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
        conditionalNG.getChild(0).connect(maleSocket);
        
        ExpressionSensor expression = new ExpressionSensor("IQDE1", null);
        maleSocket = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
        action.getChild(0).connect(maleSocket);
        
        JDialogOperator jdo = editItem(conditionalNG, "Edit ConditionalNG IQC1", "Edit ? ", 1);
        
        new JRadioButtonOperator(jdo, 1).clickMouse();
        new JTextFieldOperator(jdo, 2).enterText("IS99");
        
        new JComboBoxOperator(jdo, 1).setSelectedItem(Is_IsNot_Enum.IS);
        new JComboBoxOperator(jdo, 2).setSelectedItem(ExpressionSensor.SensorState.ACTIVE);
        new JButtonOperator(jdo, "OK").push();  // NOI18N
        
        Assert.assertEquals("IS99", expression.getSensor().getBean().getSystemName());
        Assert.assertEquals(ExpressionSensor.SensorState.ACTIVE, expression.getSensorState());
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initLogixNGManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
