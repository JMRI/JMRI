package jmri.jmrit.logixng.digital.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.concurrent.atomic.AtomicBoolean;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBeanHandle;
import jmri.Sensor;
import jmri.SensorManager;
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
 * Test ExpressionSensor
 * 
 * @author Daniel Bergqvist 2018
 */
public class ExpressionSensorTest {

    @Test
    public void testCtor() {
        ExpressionSensor t = new ExpressionSensor("IQDE321", null);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testDescription() {
        ExpressionSensor expressionSensor = new ExpressionSensor("IQDE321", null);
        Assert.assertTrue("Get sensor".equals(expressionSensor.getShortDescription()));
        Assert.assertTrue("Sensor Not selected is Active".equals(expressionSensor.getLongDescription()));
        Sensor sensor = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        expressionSensor.setSensor(sensor);
        expressionSensor.set_Is_IsNot(Is_IsNot_Enum.IS);
        expressionSensor.setSensorState(ExpressionSensor.SensorState.INACTIVE);
        Assert.assertTrue("Sensor IS1 is Inactive".equals(expressionSensor.getLongDescription()));
        expressionSensor.set_Is_IsNot(Is_IsNot_Enum.IS_NOT);
        Assert.assertTrue("Sensor IS1 is not Inactive".equals(expressionSensor.getLongDescription()));
        expressionSensor.setSensorState(ExpressionSensor.SensorState.OTHER);
        Assert.assertTrue("Sensor IS1 is not Other".equals(expressionSensor.getLongDescription()));
    }
    
    @Test
    public void testExpression() throws SocketAlreadyConnectedException, JmriException {
        Sensor sensor = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        sensor.setCommandedState(Sensor.INACTIVE);
        AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A logixNG");
        ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        conditionalNG.setRunOnGUIDelayed(false);
        logixNG.addConditionalNG(conditionalNG);
        logixNG.activateLogixNG();
        
        IfThenElse actionIfThen =
                new IfThenElse(
                        InstanceManager.getDefault(
                                DigitalActionManager.class).getAutoSystemName(), null,
                                IfThenElse.Type.TRIGGER_ACTION);
        MaleSocket socketIfThen =
                InstanceManager.getDefault(DigitalActionManager.class)
                        .registerAction(actionIfThen);
        conditionalNG.getChild(0).connect(socketIfThen);
        
        ExpressionSensor expressionSensor =
                new ExpressionSensor(
                        InstanceManager.getDefault(DigitalExpressionManager.class)
                                .getAutoSystemName(), null);
        expressionSensor.setSensor(sensor);
        expressionSensor.set_Is_IsNot(Is_IsNot_Enum.IS);
        expressionSensor.setSensorState(ExpressionSensor.SensorState.ACTIVE);
        MaleSocket socketSensor =
                InstanceManager.getDefault(DigitalExpressionManager.class)
                        .registerExpression(expressionSensor);
        socketIfThen.getChild(0).connect(socketSensor);
        
        ActionAtomicBoolean actionAtomicBoolean = new ActionAtomicBoolean(atomicBoolean, true);
        MaleSocket socketAtomicBoolean =
                InstanceManager.getDefault(DigitalActionManager.class)
                        .registerAction(actionAtomicBoolean);
        socketIfThen.getChild(1).connect(socketAtomicBoolean);
        
        // The action is not yet executed so the atomic boolean should be false
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // Throw the switch. This should not execute the conditional.
        sensor.setCommandedState(Sensor.ACTIVE);
        // The conditionalNG is not yet enabled so it shouldn't be executed.
        // So the atomic boolean should be false
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // Close the switch. This should not execute the conditional.
        sensor.setCommandedState(Sensor.INACTIVE);
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // The action is not yet executed so the atomic boolean should be false
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // Throw the switch. This should execute the conditional.
        sensor.setCommandedState(Sensor.ACTIVE);
        // The action should now be executed so the atomic boolean should be true
        Assert.assertTrue("atomicBoolean is true",atomicBoolean.get());
    }
    
    @Test
    public void testSetSensor() {
        // Test setSensor() when listeners are registered
        Sensor sensor = InstanceManager.getDefault(SensorManager.class).provide("IT1");
        Assert.assertNotNull("Sensor is not null", sensor);
        ExpressionSensor expression =
                new ExpressionSensor(
                        InstanceManager.getDefault(DigitalExpressionManager.class)
                                .getAutoSystemName(), null);
        expression.setSensor(sensor);
        
        Assert.assertNotNull("Sensor is not null", expression.getSensor());
        expression.registerListeners();
        boolean thrown = false;
        try {
            expression.setSensor((String)null);
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setSensor must not be called when listeners are registered");
        
        thrown = false;
        try {
            expression.setSensor((NamedBeanHandle<Sensor>)null);
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setSensor must not be called when listeners are registered");
        
        thrown = false;
        try {
            expression.setSensor((Sensor)null);
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setSensor must not be called when listeners are registered");
    }
    
    @Test
    public void testVetoableChange() throws PropertyVetoException {
        // Get the expression and set the sensor
        Sensor sensor = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        Assert.assertNotNull("Sensor is not null", sensor);
        ExpressionSensor expression =
                new ExpressionSensor(
                        InstanceManager.getDefault(DigitalExpressionManager.class)
                                .getAutoSystemName(), null);
        expression.setSensor(sensor);
        
        // Get some other sensor for later use
        Sensor otherSensor = InstanceManager.getDefault(SensorManager.class).provide("IM99");
        Assert.assertNotNull("Sensor is not null", otherSensor);
        Assert.assertNotEquals("Sensor is not equal", sensor, otherSensor);
        
        // Test vetoableChange() for some other propery
        expression.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        Assert.assertEquals("Sensor matches", sensor, expression.getSensor().getBean());
        
        // Test vetoableChange() for a string
        expression.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        Assert.assertEquals("Sensor matches", sensor, expression.getSensor().getBean());
        expression.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        Assert.assertEquals("Sensor matches", sensor, expression.getSensor().getBean());
        
        // Test vetoableChange() for another sensor
        expression.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherSensor, null));
        Assert.assertEquals("Sensor matches", sensor, expression.getSensor().getBean());
        expression.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherSensor, null));
        Assert.assertEquals("Sensor matches", sensor, expression.getSensor().getBean());
        
        // Test vetoableChange() for its own sensor
        boolean thrown = false;
        try {
            expression.vetoableChange(new PropertyChangeEvent(this, "CanDelete", sensor, null));
        } catch (PropertyVetoException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        Assert.assertEquals("Sensor matches", sensor, expression.getSensor().getBean());
        expression.vetoableChange(new PropertyChangeEvent(this, "DoDelete", sensor, null));
        Assert.assertNull("Sensor is null", expression.getSensor());
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
