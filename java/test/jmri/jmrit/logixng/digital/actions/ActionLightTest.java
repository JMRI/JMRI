package jmri.jmrit.logixng.digital.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Light;
import jmri.LightManager;
import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.util.JUnitAppender;
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
    
    
    @Test
    public void testLightState() {
        Assert.assertEquals("String matches", "Off", ActionLight.LightState.OFF.toString());
        Assert.assertEquals("String matches", "On", ActionLight.LightState.ON.toString());
        Assert.assertEquals("String matches", "Toggle", ActionLight.LightState.TOGGLE.toString());
        
        Assert.assertTrue("objects are equal", ActionLight.LightState.OFF == ActionLight.LightState.get(Light.OFF));
        Assert.assertTrue("objects are equal", ActionLight.LightState.ON == ActionLight.LightState.get(Light.ON));
        Assert.assertTrue("objects are equal", ActionLight.LightState.TOGGLE == ActionLight.LightState.get(-1));
        
        boolean hasThrown = false;
        try {
            ActionLight.LightState.get(Light.UNKNOWN);
        } catch (IllegalArgumentException ex) {
            hasThrown = true;
            Assert.assertTrue("Error message is correct", "invalid light state".equals(ex.getMessage()));
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }
    
    @Override
    public ConditionalNG getConditionalNG() {
        return conditionalNG;
    }
    
    @Override
    public LogixNG getLogixNG() {
        return logixNG;
    }
    
    @Override
    public MaleSocket getConnectableChild() {
        return null;
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
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new ActionLight(systemName, null);
    }
    
    @Override
    public boolean addNewSocket() {
        return false;
    }
    
    @Test
    public void testCtor() {
        Assert.assertTrue("object exists", _base != null);
        
        ActionLight action2;
        Assert.assertNotNull("light is not null", light);
        light.setState(Light.ON);
        
        action2 = new ActionLight("IQDA321", null);
        Assert.assertNotNull("object exists", action2);
        Assert.assertNull("Username matches", action2.getUserName());
        Assert.assertEquals("String matches", "Set light '' to On", action2.getLongDescription());
        
        action2 = new ActionLight("IQDA321", "My light");
        Assert.assertNotNull("object exists", action2);
        Assert.assertEquals("Username matches", "My light", action2.getUserName());
        Assert.assertEquals("String matches", "Set light '' to On", action2.getLongDescription());
        
        action2 = new ActionLight("IQDA321", null);
        action2.setLight(light);
        Assert.assertTrue("light is correct", light == action2.getLight().getBean());
        Assert.assertNotNull("object exists", action2);
        Assert.assertNull("Username matches", action2.getUserName());
        Assert.assertEquals("String matches", "Set light IL1 to On", action2.getLongDescription());
        
        Light l = InstanceManager.getDefault(LightManager.class).provide("IL1");
        action2 = new ActionLight("IQDA321", "My light");
        action2.setLight(l);
        Assert.assertTrue("light is correct", l == action2.getLight().getBean());
        Assert.assertNotNull("object exists", action2);
        Assert.assertEquals("Username matches", "My light", action2.getUserName());
        Assert.assertEquals("String matches", "Set light IL1 to On", action2.getLongDescription());
        
        boolean thrown = false;
        try {
            // Illegal system name
            new ActionLight("IQA55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        thrown = false;
        try {
            // Illegal system name
            new ActionLight("IQA55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        // Test setup(). This method doesn't do anything, but execute it for coverage.
        _base.setup();
    }
    
    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 0", 0 == actionLight.getChildCount());
        
        boolean hasThrown = false;
        try {
            actionLight.getChild(0);
        } catch (UnsupportedOperationException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "Not supported.", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }
    
    @Test
    public void testSetLight() {
        Light light11 = InstanceManager.getDefault(LightManager.class).provide("IL11");
        Light light12 = InstanceManager.getDefault(LightManager.class).provide("IL12");
        NamedBeanHandle<Light> lightHandle12 = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(light12.getDisplayName(), light12);
        Light light13 = InstanceManager.getDefault(LightManager.class).provide("IL13");
        Light light14 = InstanceManager.getDefault(LightManager.class).provide("IL14");
        light14.setUserName("Some user name");
        
        actionLight.setLight((Light)null);
        Assert.assertNull("light handle is null", actionLight.getLight());
        
        actionLight.setLight(light11);
        Assert.assertTrue("light is correct", light11 == actionLight.getLight().getBean());
        
        actionLight.setLight((Light)null);
        Assert.assertNull("light handle is null", actionLight.getLight());
        
        actionLight.setLight(lightHandle12);
        Assert.assertTrue("light handle is correct", lightHandle12 == actionLight.getLight());
        
        actionLight.setLight("A non existent light");
        Assert.assertNull("light handle is null", actionLight.getLight());
        JUnitAppender.assertErrorMessage("light \"A non existent light\" is not found");
        
        actionLight.setLight(light13.getSystemName());
        Assert.assertTrue("light is correct", light13 == actionLight.getLight().getBean());
        
        actionLight.setLight(light14.getUserName());
        Assert.assertTrue("light is correct", light14 == actionLight.getLight().getBean());
    }
    
    @Test
    public void testAction() throws SocketAlreadyConnectedException, JmriException {
        
        // Set the light
        light.setCommandedState(Light.OFF);
        // The light should be off
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
    
    @Test
    public void testCategory() {
        Assert.assertTrue("Category matches", Category.ITEM == _base.getCategory());
    }
    
    @Test
    public void testIsExternal() {
        Assert.assertTrue("is external", _base.isExternal());
    }
    
    @Test
    public void testShortDescription() {
        Assert.assertEquals("String matches", "Set light", _base.getShortDescription());
    }
    
    @Test
    public void testLongDescription() {
        Assert.assertEquals("String matches", "Set light IL1 to On", _base.getLongDescription());
    }
    
    @Test
    public void testChild() {
        Assert.assertTrue("Num children is zero", 0 == _base.getChildCount());
        boolean hasThrown = false;
        try {
            _base.getChild(0);
        } catch (UnsupportedOperationException ex) {
            hasThrown = true;
            Assert.assertTrue("Error message is correct", "Not supported.".equals(ex.getMessage()));
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalLightManager();
        
        _category = Category.ITEM;
        _isExternal = true;
        
        light = InstanceManager.getDefault(LightManager.class).provide("IL1");
        light.setCommandedState(Light.OFF);
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A logixNG");
        conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        logixNG.addConditionalNG(conditionalNG);
        conditionalNG.setRunOnGUIDelayed(false);
        conditionalNG.setEnabled(true);
        actionLight = new ActionLight(InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null);
        actionLight.setLight(light);
        actionLight.setLightState(ActionLight.LightState.ON);
        MaleSocket socket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionLight);
        conditionalNG.getChild(0).connect(socket);
        
        _base = actionLight;
        _baseMaleSocket = socket;
        
        logixNG.setParentForAllChildren();
        logixNG.setEnabled(true);
        logixNG.activateLogixNG();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
