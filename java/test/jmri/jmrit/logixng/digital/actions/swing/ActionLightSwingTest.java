package jmri.jmrit.logixng.digital.actions.swing;

import java.awt.GraphicsEnvironment;

import javax.swing.JPanel;

import jmri.InstanceManager;
import jmri.LightManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.digital.actions.ActionLight;
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
public class ActionLightSwingTest extends SwingConfiguratorInterfaceTestBase {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        ActionLightSwing t = new ActionLightSwing();
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testCreatePanel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        Assert.assertTrue("panel is not null",
            null != new ActionLightSwing().getConfigPanel(new JPanel()));
        Assert.assertTrue("panel is not null",
            null != new ActionLightSwing().getConfigPanel(new ActionLight("IQDA1", null), new JPanel()));
    }
    
    @Test
    public void testDialogUseExistingLight() throws SocketAlreadyConnectedException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        InstanceManager.getDefault(LightManager.class).provide("IL1");
        InstanceManager.getDefault(LightManager.class).provide("IL2");

        ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG("IQC1", null);
        
        ActionLight action = new ActionLight("IQDA1", null);
        MaleSocket maleSocket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
        conditionalNG.getChild(0).connect(maleSocket);
        
        JDialogOperator jdo = editItem(conditionalNG, "Edit ConditionalNG IQC1", "Edit ! ", 0);
        
        new JComboBoxOperator(jdo, 0).setSelectedIndex(1);
        new JComboBoxOperator(jdo, 1).setSelectedItem(ActionLight.LightState.OFF);
        new JButtonOperator(jdo, "OK").push();  // NOI18N
        
        Assert.assertEquals("IL1", action.getLight().getBean().getSystemName());
        Assert.assertEquals(ActionLight.LightState.OFF, action.getLightState());
    }

    @Test
    public void testDialogCreateNewLight() throws SocketAlreadyConnectedException {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        InstanceManager.getDefault(LightManager.class).provide("IL1");
        InstanceManager.getDefault(LightManager.class).provide("IL2");

        ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG("IQC1", null);
        
        ActionLight action = new ActionLight("IQDA1", null);
        MaleSocket maleSocket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
        conditionalNG.getChild(0).connect(maleSocket);
        
        JDialogOperator jdo = editItem(conditionalNG, "Edit ConditionalNG IQC1", "Edit ! ", 0);
        
        new JRadioButtonOperator(jdo, 1).clickMouse();
        new JTextFieldOperator(jdo, 2).enterText("IL99");
        new JComboBoxOperator(jdo, 1).setSelectedItem(ActionLight.LightState.ON);
        new JButtonOperator(jdo, "OK").push();  // NOI18N
        
        Assert.assertEquals("IL99", action.getLight().getBean().getSystemName());
        Assert.assertEquals(ActionLight.LightState.ON, action.getLightState());
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initLogixNGManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
