package jmri.jmrit.logixng.digital.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.concurrent.atomic.AtomicBoolean;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Light;
import jmri.LightManager;
import jmri.NamedBeanHandle;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.Is_IsNot_Enum;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.digital.actions.ActionAtomicBoolean;
import jmri.jmrit.logixng.digital.actions.IfThenElse;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
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
public class ExpressionLightTest {

    @Test
    public void testCtor() {
        ExpressionLight t = new ExpressionLight("IQDE321", null);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testDescription() {
        ExpressionLight expressionLight = new ExpressionLight("IQDE321", null);
        Assert.assertTrue("Get light".equals(expressionLight.getShortDescription()));
        Assert.assertTrue("Light Not selected is On".equals(expressionLight.getLongDescription()));
        Light light = InstanceManager.getDefault(LightManager.class).provide("IL1");
        expressionLight.setLight(light);
        expressionLight.set_Is_IsNot(Is_IsNot_Enum.IS);
        expressionLight.setLightState(ExpressionLight.LightState.OFF);
        Assert.assertTrue("Light IL1 is Off".equals(expressionLight.getLongDescription()));
        expressionLight.set_Is_IsNot(Is_IsNot_Enum.IS_NOT);
        Assert.assertTrue("Light IL1 is not Off".equals(expressionLight.getLongDescription()));
        expressionLight.setLightState(ExpressionLight.LightState.OTHER);
        Assert.assertTrue("Light IL1 is not Other".equals(expressionLight.getLongDescription()));
    }
    
    @Test
    public void testExpression() throws SocketAlreadyConnectedException, JmriException {
        Light light = InstanceManager.getDefault(LightManager.class).provide("IL1");
        light.setCommandedState(Light.OFF);
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        LogixNG logixNG =
                InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A logixNG");
        ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        logixNG.addConditionalNG(conditionalNG);
        logixNG.activateLogixNG();
        
        IfThenElse actionIfThen =
                new IfThenElse(InstanceManager.getDefault(
                        DigitalActionManager.class).getNewSystemName(), null,
                        IfThenElse.Type.TRIGGER_ACTION);
        
        MaleSocket socketIfThen =
                InstanceManager.getDefault(DigitalActionManager.class)
                        .registerAction(actionIfThen);
        
        conditionalNG.getChild(0).connect(socketIfThen);
        
        ExpressionLight expressionLight =
                new ExpressionLight(InstanceManager.getDefault(
                        DigitalExpressionManager.class).getNewSystemName(), null);
        
        expressionLight.setLight(light);
        expressionLight.set_Is_IsNot(Is_IsNot_Enum.IS);
        expressionLight.setLightState(ExpressionLight.LightState.ON);
        MaleSocket socketLight = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionLight);
        socketIfThen.getChild(0).connect(socketLight);
        
        ActionAtomicBoolean actionAtomicBoolean = new ActionAtomicBoolean(atomicBoolean, true);
        MaleSocket socketAtomicBoolean = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionAtomicBoolean);
        socketIfThen.getChild(1).connect(socketAtomicBoolean);
        
        // The action is not yet executed so the atomic boolean should be false
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // Throw the switch. This should not execute the conditional.
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
        // Throw the switch. This should execute the conditional.
        light.setCommandedState(Light.ON);
        // The action should now be executed so the atomic boolean should be true
        Assert.assertTrue("atomicBoolean is true",atomicBoolean.get());
    }
    
    @Test
    public void testSetLight() {
        // Test setLight() when listeners are registered
        Light turnout = InstanceManager.getDefault(LightManager.class).provide("IT1");
        Assert.assertNotNull("Light is not null", turnout);
        ExpressionLight expression =
                new ExpressionLight(
                        InstanceManager.getDefault(
                                DigitalExpressionManager.class).getNewSystemName(), null);
        expression.setLight(turnout);
        
        Assert.assertNotNull("Light is not null", expression.getLight());
        expression.registerListeners();
        boolean thrown = false;
        try {
            expression.setLight((String)null);
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setLight must not be called when listeners are registered");
        
        thrown = false;
        try {
            expression.setLight((NamedBeanHandle<Light>)null);
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setLight must not be called when listeners are registered");
        
        thrown = false;
        try {
            expression.setLight((Light)null);
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setLight must not be called when listeners are registered");
    }
    
    @Test
    public void testVetoableChange() throws PropertyVetoException {
        // Get the expression and set the light
        Light light = InstanceManager.getDefault(LightManager.class).provide("IL1");
        Assert.assertNotNull("Light is not null", light);
        ExpressionLight expression =
                new ExpressionLight(
                        InstanceManager.getDefault(
                                DigitalExpressionManager.class).getNewSystemName(), null);
        expression.setLight(light);
        
        // Get some other light for later use
        Light otherLight = InstanceManager.getDefault(LightManager.class).provide("IM99");
        Assert.assertNotNull("Light is not null", otherLight);
        Assert.assertNotEquals("Light is not equal", light, otherLight);
        
        // Test vetoableChange() for some other propery
        expression.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        Assert.assertEquals("Light matches", light, expression.getLight().getBean());
        
        // Test vetoableChange() for a string
        expression.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        Assert.assertEquals("Light matches", light, expression.getLight().getBean());
        expression.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        Assert.assertEquals("Light matches", light, expression.getLight().getBean());
        
        // Test vetoableChange() for another light
        expression.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherLight, null));
        Assert.assertEquals("Light matches", light, expression.getLight().getBean());
        expression.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherLight, null));
        Assert.assertEquals("Light matches", light, expression.getLight().getBean());
        
        // Test vetoableChange() for its own light
        boolean thrown = false;
        try {
            expression.vetoableChange(new PropertyChangeEvent(this, "CanDelete", light, null));
        } catch (PropertyVetoException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        Assert.assertEquals("Light matches", light, expression.getLight().getBean());
        expression.vetoableChange(new PropertyChangeEvent(this, "DoDelete", light, null));
        Assert.assertNull("Light is null", expression.getLight());
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalLightManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
