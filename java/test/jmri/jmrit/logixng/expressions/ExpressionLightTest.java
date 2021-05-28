package jmri.jmrit.logixng.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

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
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.Is_IsNot_Enum;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.actions.ActionAtomicBoolean;
import jmri.jmrit.logixng.actions.IfThenElse;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test ExpressionLight
 * 
 * @author Daniel Bergqvist 2018
 */
public class ExpressionLightTest extends AbstractDigitalExpressionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private ExpressionLight expressionLight;
    private ActionAtomicBoolean actionAtomicBoolean;
    private AtomicBoolean atomicBoolean;
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
    public MaleSocket getConnectableChild() {
        return null;
    }
    
    @Override
    public String getExpectedPrintedTree() {
        return String.format("Light IL1 is On ::: Use default%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         If Then Else. Execute on change ::: Use default%n" +
                "            ? If%n" +
                "               Light IL1 is On ::: Use default%n" +
                "            ! Then%n" +
                "               Set the atomic boolean to true ::: Use default%n" +
                "            ! Else%n" +
                "               Socket not connected%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new ExpressionLight(systemName, null);
    }
    
    @Override
    public boolean addNewSocket() {
        return false;
    }
    
    @Test
    public void testCtor() {
        ExpressionLight expression2;
        Assert.assertNotNull("light is not null", light);
        light.setState(Light.ON);
        
        expression2 = new ExpressionLight("IQDE321", null);
        Assert.assertNotNull("object exists", expression2);
        Assert.assertNull("Username matches", expression2.getUserName());
        Assert.assertEquals("String matches", "Light '' is On", expression2.getLongDescription());
        
        expression2 = new ExpressionLight("IQDE321", "My light");
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My light", expression2.getUserName());
        Assert.assertEquals("String matches", "Light '' is On", expression2.getLongDescription());
        
        expression2 = new ExpressionLight("IQDE321", null);
        expression2.setLight(light);
        Assert.assertTrue("light is correct", light == expression2.getLight().getBean());
        Assert.assertNotNull("object exists", expression2);
        Assert.assertNull("Username matches", expression2.getUserName());
        Assert.assertEquals("String matches", "Light IL1 is On", expression2.getLongDescription());
        
        Light l = InstanceManager.getDefault(LightManager.class).provide("IL2");
        expression2 = new ExpressionLight("IQDE321", "My light");
        expression2.setLight(l);
        Assert.assertTrue("light is correct", l == expression2.getLight().getBean());
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My light", expression2.getUserName());
        Assert.assertEquals("String matches", "Light IL2 is On", expression2.getLongDescription());
        
        boolean thrown = false;
        try {
            // Illegal system name
            new ExpressionLight("IQE55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        thrown = false;
        try {
            // Illegal system name
            new ExpressionLight("IQE55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
    }
    
    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 0", 0 == expressionLight.getChildCount());
        
        boolean hasThrown = false;
        try {
            expressionLight.getChild(0);
        } catch (UnsupportedOperationException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "Not supported.", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }
    
    @Test
    public void testLightState() {
        Assert.assertEquals("String matches", "Off", ExpressionLight.LightState.Off.toString());
        Assert.assertEquals("String matches", "On", ExpressionLight.LightState.On.toString());
        Assert.assertEquals("String matches", "Other", ExpressionLight.LightState.Other.toString());
        
        Assert.assertTrue("objects are equal", ExpressionLight.LightState.Off == ExpressionLight.LightState.get(Light.OFF));
        Assert.assertTrue("objects are equal", ExpressionLight.LightState.On == ExpressionLight.LightState.get(Light.ON));
        Assert.assertTrue("objects are equal", ExpressionLight.LightState.Other == ExpressionLight.LightState.get(Light.UNKNOWN));
        Assert.assertTrue("objects are equal", ExpressionLight.LightState.Other == ExpressionLight.LightState.get(Light.INCONSISTENT));
        Assert.assertTrue("objects are equal", ExpressionLight.LightState.Other == ExpressionLight.LightState.get(-1));
        
        Assert.assertEquals("ID matches", Light.ON, ExpressionLight.LightState.On.getID());
        Assert.assertEquals("ID matches", Light.OFF, ExpressionLight.LightState.Off.getID());
        Assert.assertEquals("ID matches", -1, ExpressionLight.LightState.Other.getID());
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
    public void testDescription() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);
        
        expressionLight.removeLight();
        Assert.assertTrue("Light".equals(expressionLight.getShortDescription()));
        Assert.assertTrue("Light '' is On".equals(expressionLight.getLongDescription()));
        expressionLight.setLight(light);
        expressionLight.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionLight.setBeanState(ExpressionLight.LightState.Off);
        Assert.assertTrue("Light IL1 is Off".equals(expressionLight.getLongDescription()));
        expressionLight.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        Assert.assertTrue("Light IL1 is not Off".equals(expressionLight.getLongDescription()));
        expressionLight.setBeanState(ExpressionLight.LightState.Other);
        Assert.assertTrue("Light IL1 is not Other".equals(expressionLight.getLongDescription()));
    }
    
    @Test
    public void testExpression() throws SocketAlreadyConnectedException, JmriException {
        // Clear flag
        atomicBoolean.set(false);
        // Turn light off
        light.setCommandedState(Light.OFF);
        // Disable the conditionalNG
        conditionalNG.setEnabled(false);
        // The action is not yet executed so the atomic boolean should be false
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // Turn the light on. This should not execute the conditional.
        light.setCommandedState(Light.ON);
        // The conditionalNG is not yet enabled so it shouldn't be executed.
        // So the atomic boolean should be false
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // Close the switch. This should not execute the conditional.
        light.setCommandedState(Light.OFF);
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // The action is not yet executed so the atomic boolean should be false
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // Turn the light on. This should execute the conditional.
        light.setCommandedState(Light.ON);
        // The action should now be executed so the atomic boolean should be true
        Assert.assertTrue("atomicBoolean is true",atomicBoolean.get());
        // Clear the atomic boolean.
        atomicBoolean.set(false);
        // Turn the light off. This should not execute the conditional.
        light.setCommandedState(Light.OFF);
        // The action should now be executed so the atomic boolean should be true
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        
        // Test IS_NOT
        expressionLight.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        // Turn the light on. This should not execute the conditional.
        light.setCommandedState(Light.ON);
        // The action should now be executed so the atomic boolean should be true
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // Turn the light off. This should not execute the conditional.
        light.setCommandedState(Light.OFF);
        // The action should now be executed so the atomic boolean should be true
        Assert.assertTrue("atomicBoolean is true",atomicBoolean.get());
    }
    
    @Test
    public void testSetLight() {
        expressionLight.unregisterListeners();
        
        Light otherLight = InstanceManager.getDefault(LightManager.class).provide("IL99");
        Assert.assertNotEquals("Lights are different", otherLight, expressionLight.getLight().getBean());
        expressionLight.setLight(otherLight);
        Assert.assertEquals("Lights are equal", otherLight, expressionLight.getLight().getBean());
        
        NamedBeanHandle<Light> otherLightHandle =
                InstanceManager.getDefault(NamedBeanHandleManager.class)
                        .getNamedBeanHandle(otherLight.getDisplayName(), otherLight);
        expressionLight.removeLight();
        Assert.assertNull("Light is null", expressionLight.getLight());
        expressionLight.setLight(otherLightHandle);
        Assert.assertEquals("Lights are equal", otherLight, expressionLight.getLight().getBean());
        Assert.assertEquals("LightHandles are equal", otherLightHandle, expressionLight.getLight());
    }
    
    @Test
    public void testSetLight2() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);
        
        Light light11 = InstanceManager.getDefault(LightManager.class).provide("IL11");
        Light light12 = InstanceManager.getDefault(LightManager.class).provide("IL12");
        NamedBeanHandle<Light> lightHandle12 = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(light12.getDisplayName(), light12);
        Light light13 = InstanceManager.getDefault(LightManager.class).provide("IL13");
        Light light14 = InstanceManager.getDefault(LightManager.class).provide("IL14");
        light14.setUserName("Some user name");
        
        expressionLight.removeLight();
        Assert.assertNull("light handle is null", expressionLight.getLight());
        
        expressionLight.setLight(light11);
        Assert.assertTrue("light is correct", light11 == expressionLight.getLight().getBean());
        
        expressionLight.removeLight();
        Assert.assertNull("light handle is null", expressionLight.getLight());
        
        expressionLight.setLight(lightHandle12);
        Assert.assertTrue("light handle is correct", lightHandle12 == expressionLight.getLight());
        
        expressionLight.setLight("A non existent light");
        Assert.assertNull("light handle is null", expressionLight.getLight());
        JUnitAppender.assertErrorMessage("light \"A non existent light\" is not found");
        
        expressionLight.setLight(light13.getSystemName());
        Assert.assertTrue("light is correct", light13 == expressionLight.getLight().getBean());
        
        String userName = light14.getUserName();
        Assert.assertNotNull("light is not null", userName);
        expressionLight.setLight(userName);
        Assert.assertTrue("light is correct", light14 == expressionLight.getLight().getBean());
    }
    
    @Test
    public void testSetLightException() {
        // Test setLight() when listeners are registered
        Assert.assertNotNull("Light is not null", light);
        Assert.assertNotNull("Light is not null", expressionLight.getLight());
        expressionLight.registerListeners();
        boolean thrown = false;
        try {
            expressionLight.setLight("A light");
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setLight must not be called when listeners are registered");
        
        thrown = false;
        try {
            Light light99 = InstanceManager.getDefault(LightManager.class).provide("IL99");
            NamedBeanHandle<Light> lightHandle99 =
                    InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(light99.getDisplayName(), light99);
            expressionLight.setLight(lightHandle99);
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setLight must not be called when listeners are registered");
        
        thrown = false;
        try {
            expressionLight.setLight((Light)null);
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setLight must not be called when listeners are registered");
    }
    
    @Test
    public void testRegisterListeners() {
        // Test registerListeners() when the ExpressionLight has no light
        conditionalNG.setEnabled(false);
        expressionLight.removeLight();
        conditionalNG.setEnabled(true);
    }
    
    @Test
    public void testVetoableChange() throws PropertyVetoException {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);
        
        // Get the expression and set the light
        Assert.assertNotNull("Light is not null", light);
        
        // Get some other light for later use
        Light otherLight = InstanceManager.getDefault(LightManager.class).provide("IM99");
        Assert.assertNotNull("Light is not null", otherLight);
        Assert.assertNotEquals("Light is not equal", light, otherLight);
        
        // Test vetoableChange() for some other propery
        expressionLight.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        Assert.assertEquals("Light matches", light, expressionLight.getLight().getBean());
        
        // Test vetoableChange() for a string
        expressionLight.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        Assert.assertEquals("Light matches", light, expressionLight.getLight().getBean());
        expressionLight.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        Assert.assertEquals("Light matches", light, expressionLight.getLight().getBean());
        
        // Test vetoableChange() for another light
        expressionLight.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherLight, null));
        Assert.assertEquals("Light matches", light, expressionLight.getLight().getBean());
        expressionLight.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherLight, null));
        Assert.assertEquals("Light matches", light, expressionLight.getLight().getBean());
        
        // Test vetoableChange() for its own light
        boolean thrown = false;
        try {
            expressionLight.vetoableChange(new PropertyChangeEvent(this, "CanDelete", light, null));
        } catch (PropertyVetoException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        Assert.assertEquals("Light matches", light, expressionLight.getLight().getBean());
        expressionLight.vetoableChange(new PropertyChangeEvent(this, "DoDelete", light, null));
        Assert.assertNull("Light is null", expressionLight.getLight());
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initLogixNGManager();
        
        _category = Category.ITEM;
        _isExternal = true;
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);
        
        logixNG.addConditionalNG(conditionalNG);
        
        IfThenElse ifThenElse = new IfThenElse("IQDA321", null);
        MaleSocket socketIfThenElse =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThenElse);
        conditionalNG.getChild(0).connect(socketIfThenElse);
        
        expressionLight = new ExpressionLight("IQDE321", null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionLight);
        ifThenElse.getChild(0).connect(maleSocket2);
        
        _base = expressionLight;
        _baseMaleSocket = maleSocket2;
        
        atomicBoolean = new AtomicBoolean(false);
        actionAtomicBoolean = new ActionAtomicBoolean(atomicBoolean, true);
        MaleSocket socketAtomicBoolean = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionAtomicBoolean);
        ifThenElse.getChild(1).connect(socketAtomicBoolean);
        
        light = InstanceManager.getDefault(LightManager.class).provide("IL1");
        expressionLight.setLight(light);
        light.setCommandedState(Light.ON);
        
        if (! logixNG.setParentForAllChildren(new ArrayList<>())) throw new RuntimeException();
        logixNG.setEnabled(true);
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }
    
}
