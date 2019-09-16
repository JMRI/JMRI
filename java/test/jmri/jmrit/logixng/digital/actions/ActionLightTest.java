package jmri.jmrit.logixng.digital.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Light;
import jmri.LightManager;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test ActionLight
 * 
 * @author Daniel Bergqvist 2018
 */
public class ActionLightTest extends AbstractDigitalActionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private ActionLight actionLight;
    private Light light;
    
    
    @Override
    public ConditionalNG getConditionalNG() {
        return conditionalNG;
    }
    
    @Override
    public LogixNG getLogixNG() {
        return logixNG;
    }
    
    @Override
    public String getExpectedPrintedTree() {
        return String.format("Set light IL1 to On%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A logixNG%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! %n" +
                "         Set light IL1 to On%n");
    }
    
    @Test
    public void testCtor() {
        ActionLight t = new ActionLight("IQDA321", null);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testAction() throws SocketAlreadyConnectedException, JmriException {
        
        // The action is not yet executed so the light should be off
        Assert.assertTrue("light is off",light.getCommandedState() == Light.OFF);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the light should be on
        Assert.assertTrue("light is on",light.getCommandedState() == Light.ON);
        
        // Test to set light to off
        actionLight.setLightState(ActionLight.LightState.OFF);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the light should be on
        Assert.assertTrue("light is on",light.getCommandedState() == Light.OFF);
        
        // Test to set light to toggle
        actionLight.setLightState(ActionLight.LightState.TOGGLE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the light should be on
        Assert.assertTrue("light is on",light.getCommandedState() == Light.ON);
        
        // Test to set light to toggle
        actionLight.setLightState(ActionLight.LightState.TOGGLE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the light should be on
        Assert.assertTrue("light is on",light.getCommandedState() == Light.OFF);
    }
    
    @Test
    public void testVetoableChange() throws PropertyVetoException {
        // Get the action and set the light
//        Light light = InstanceManager.getDefault(LightManager.class).provide("IL1");
        Assert.assertNotNull("Light is not null", light);
//        ActionLight action = new ActionLight();
//        action.setLight(light);
        
        // Get some other light for later use
        Light otherLight = InstanceManager.getDefault(LightManager.class).provide("IM99");
        Assert.assertNotNull("Light is not null", otherLight);
        Assert.assertNotEquals("Light is not equal", light, otherLight);
        
        // Test vetoableChange() for some other propery
        actionLight.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        Assert.assertEquals("Light matches", light, actionLight.getLight().getBean());
        
        // Test vetoableChange() for a string
        actionLight.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        Assert.assertEquals("Light matches", light, actionLight.getLight().getBean());
        actionLight.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        Assert.assertEquals("Light matches", light, actionLight.getLight().getBean());
        
        // Test vetoableChange() for another light
        actionLight.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherLight, null));
        Assert.assertEquals("Light matches", light, actionLight.getLight().getBean());
        actionLight.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherLight, null));
        Assert.assertEquals("Light matches", light, actionLight.getLight().getBean());
        
        // Test vetoableChange() for its own light
        boolean thrown = false;
        try {
            actionLight.vetoableChange(new PropertyChangeEvent(this, "CanDelete", light, null));
        } catch (PropertyVetoException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        Assert.assertEquals("Light matches", light, actionLight.getLight().getBean());
        actionLight.vetoableChange(new PropertyChangeEvent(this, "DoDelete", light, null));
        Assert.assertNull("Light is null", actionLight.getLight());
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initLogixNG();
        
        light = InstanceManager.getDefault(LightManager.class).provide("IL1");
        light.setCommandedState(Light.OFF);
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A logixNG");
        conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        logixNG.addConditionalNG(conditionalNG);
        conditionalNG.setEnabled(true);
        actionLight = new ActionLight(InstanceManager.getDefault(DigitalActionManager.class).getNewSystemName(), null);
        actionLight.setLight(light);
        actionLight.setLightState(ActionLight.LightState.ON);
        MaleSocket socket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionLight);
        conditionalNG.getChild(0).connect(socket);
        
        _base = actionLight;
        _baseMaleSocket = socket;
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
