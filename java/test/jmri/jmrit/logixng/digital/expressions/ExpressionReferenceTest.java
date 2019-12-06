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
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test ExpressionReference
 * 
 * @author Daniel Bergqvist 2019
 */
public class ExpressionReferenceTest {

    @Test
    public void testCtor() {
        ExpressionReference t = new ExpressionReference("IQDE321", null);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testDescription() {
        ExpressionReference expressionReference = new ExpressionReference("IQDE321", null);
        Assert.assertEquals("Reference", expressionReference.getShortDescription());
        Assert.assertEquals("Reference \"Not selected\" is Nothing", expressionReference.getLongDescription());
    }
/*    
    @Test
    public void testExpression() throws SocketAlreadyConnectedException, JmriException {
        Light light = InstanceManager.getDefault(LightManager.class).provide("IL1");
        light.setCommandedState(Light.OFF);
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        LogixNG logixNG =
                InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A logixNG");
        ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        conditionalNG.setRunOnGUIDelayed(false);
        logixNG.addConditionalNG(conditionalNG);
        logixNG.activateLogixNG();
        
        IfThenElse actionIfThen =
                new IfThenElse(InstanceManager.getDefault(
                        DigitalActionManager.class).getAutoSystemName(), null,
                        IfThenElse.Type.TRIGGER_ACTION);
        
        MaleSocket socketIfThen =
                InstanceManager.getDefault(DigitalActionManager.class)
                        .registerAction(actionIfThen);
        
        conditionalNG.getChild(0).connect(socketIfThen);
        
        ExpressionReference expressionReference =
                new ExpressionReference(InstanceManager.getDefault(
                        DigitalExpressionManager.class).getAutoSystemName(), null);
        
        expressionReference.setLight(light);
        expressionReference.set_Is_IsNot(Is_IsNot_Enum.IS);
        expressionReference.setLightState(ExpressionReference.LightState.ON);
        MaleSocket socketLight = InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionReference);
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
*/    
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
