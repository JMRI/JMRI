package jmri.jmrit.logixng.digital.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.concurrent.atomic.AtomicBoolean;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Sensor;
import jmri.SensorManager;
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
public class ExpressionSensorTest extends AbstractDigitalExpressionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private ExpressionSensor expressionSensor;
    private ActionAtomicBoolean actionAtomicBoolean;
    private AtomicBoolean atomicBoolean;
    private Sensor sensor;
    
    
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
        return String.format("Sensor IS1 is Active%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! %n" +
                "         If E then A1 else A2%n" +
                "            ? E%n" +
                "               Sensor IS1 is Active%n" +
                "            ! A1%n" +
                "               Set the atomic boolean to true%n" +
                "            ! A2%n" +
                "               Socket not connected%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new ExpressionSensor(systemName, null);
    }
    
    @Test
    public void testCtor() throws JmriException {
        ExpressionSensor expression2;
        Assert.assertNotNull("sensor is not null", sensor);
        sensor.setState(Sensor.ON);
        
        expression2 = new ExpressionSensor("IQDE321", null);
        Assert.assertNotNull("object exists", expression2);
        Assert.assertNull("Username matches", expression2.getUserName());
        Assert.assertEquals("String matches", "Sensor '' is Active", expression2.getLongDescription());
        
        expression2 = new ExpressionSensor("IQDE321", "My sensor");
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My sensor", expression2.getUserName());
        Assert.assertEquals("String matches", "Sensor '' is Active", expression2.getLongDescription());
        
        expression2 = new ExpressionSensor("IQDE321", null);
        expression2.setSensor(sensor);
        Assert.assertTrue("sensor is correct", sensor == expression2.getSensor().getBean());
        Assert.assertNotNull("object exists", expression2);
        Assert.assertNull("Username matches", expression2.getUserName());
        Assert.assertEquals("String matches", "Sensor IS1 is Active", expression2.getLongDescription());
        
        Sensor s = InstanceManager.getDefault(SensorManager.class).provide("IS2");
        expression2 = new ExpressionSensor("IQDE321", "My sensor");
        expression2.setSensor(s);
        Assert.assertTrue("sensor is correct", s == expression2.getSensor().getBean());
        Assert.assertNotNull("object exists", expression2);
        Assert.assertEquals("Username matches", "My sensor", expression2.getUserName());
        Assert.assertEquals("String matches", "Sensor IS2 is Active", expression2.getLongDescription());
        
        boolean thrown = false;
        try {
            // Illegal system name
            new ExpressionSensor("IQE55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        thrown = false;
        try {
            // Illegal system name
            new ExpressionSensor("IQE55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
    }
    
    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 0", 0 == expressionSensor.getChildCount());
        
        boolean hasThrown = false;
        try {
            expressionSensor.getChild(0);
        } catch (UnsupportedOperationException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "Not supported.", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }
    
    @Test
    public void testSensorState() {
        Assert.assertEquals("String matches", "Inactive", ExpressionSensor.SensorState.INACTIVE.toString());
        Assert.assertEquals("String matches", "Active", ExpressionSensor.SensorState.ACTIVE.toString());
        Assert.assertEquals("String matches", "Other", ExpressionSensor.SensorState.OTHER.toString());
        
        Assert.assertTrue("objects are equal", ExpressionSensor.SensorState.INACTIVE == ExpressionSensor.SensorState.get(Sensor.INACTIVE));
        Assert.assertTrue("objects are equal", ExpressionSensor.SensorState.ACTIVE == ExpressionSensor.SensorState.get(Sensor.ACTIVE));
        Assert.assertTrue("objects are equal", ExpressionSensor.SensorState.OTHER == ExpressionSensor.SensorState.get(Sensor.UNKNOWN));
        Assert.assertTrue("objects are equal", ExpressionSensor.SensorState.OTHER == ExpressionSensor.SensorState.get(Sensor.INCONSISTENT));
        Assert.assertTrue("objects are equal", ExpressionSensor.SensorState.OTHER == ExpressionSensor.SensorState.get(-1));
        
        Assert.assertEquals("ID matches", Sensor.INACTIVE, ExpressionSensor.SensorState.INACTIVE.getID());
        Assert.assertEquals("ID matches", Sensor.ACTIVE, ExpressionSensor.SensorState.ACTIVE.getID());
        Assert.assertEquals("ID matches", -1, ExpressionSensor.SensorState.OTHER.getID());
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
        
        expressionSensor.setSensor((Sensor)null);
        Assert.assertEquals("Get sensor", expressionSensor.getShortDescription());
        Assert.assertEquals("Sensor '' is Active", expressionSensor.getLongDescription());
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
        // Clear flag
        atomicBoolean.set(false);
        // Turn light off
        sensor.setCommandedState(Sensor.INACTIVE);
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);
        
        expressionSensor.setSensor(sensor);
        expressionSensor.set_Is_IsNot(Is_IsNot_Enum.IS);
        expressionSensor.setSensorState(ExpressionSensor.SensorState.ACTIVE);
        
        // The action is not yet executed so the atomic boolean should be false
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // Activate the sensor. This should not execute the conditional.
        sensor.setCommandedState(Sensor.ACTIVE);
        // The conditionalNG is not yet enabled so it shouldn't be executed.
        // So the atomic boolean should be false
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // Inactivate the sensor. This should not execute the conditional.
        sensor.setCommandedState(Sensor.INACTIVE);
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // The action is not yet executed so the atomic boolean should be false
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // Activate the sensor. This should execute the conditional.
        sensor.setCommandedState(Sensor.ACTIVE);
        // The action should now be executed so the atomic boolean should be true
        Assert.assertTrue("atomicBoolean is true",atomicBoolean.get());
        // Clear the atomic boolean.
        atomicBoolean.set(false);
        // Inactivate the sensor. This should not execute the conditional.
        sensor.setCommandedState(Sensor.INACTIVE);
        // The action should now be executed so the atomic boolean should be true
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        
        // Test IS_NOT
        expressionSensor.set_Is_IsNot(Is_IsNot_Enum.IS_NOT);
        // Activate the sensor. This should not execute the conditional.
        sensor.setCommandedState(Sensor.ACTIVE);
        // The action should now be executed so the atomic boolean should be true
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // Inactivate the sensor. This should not execute the conditional.
        sensor.setCommandedState(Sensor.INACTIVE);
        // The action should now be executed so the atomic boolean should be true
        Assert.assertTrue("atomicBoolean is true",atomicBoolean.get());
        
        // Test reset(). The method ExpressionSensor.reset() doesn't do
        // anything so we only call it for coverage.
        expressionSensor.reset();
    }
    
    @Test
    public void testSetSensor() {
        expressionSensor.unregisterListeners();
        
        Sensor otherSensor = InstanceManager.getDefault(SensorManager.class).provide("IM99");
        Assert.assertNotEquals("Sensors are different", otherSensor, expressionSensor.getSensor().getBean());
        expressionSensor.setSensor(otherSensor);
        Assert.assertEquals("Sensors are equal", otherSensor, expressionSensor.getSensor().getBean());
        
        NamedBeanHandle<Sensor> otherSensorHandle =
                InstanceManager.getDefault(NamedBeanHandleManager.class)
                        .getNamedBeanHandle(otherSensor.getDisplayName(), otherSensor);
        expressionSensor.setSensor((Sensor)null);
        Assert.assertNull("Sensor is null", expressionSensor.getSensor());
        expressionSensor.setSensor(otherSensorHandle);
        Assert.assertEquals("Sensors are equal", otherSensor, expressionSensor.getSensor().getBean());
        Assert.assertEquals("SensorHandles are equal", otherSensorHandle, expressionSensor.getSensor());
    }
    
    @Test
    public void testSetSensor2() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);
        
        Sensor sensor11 = InstanceManager.getDefault(SensorManager.class).provide("IL11");
        Sensor sensor12 = InstanceManager.getDefault(SensorManager.class).provide("IL12");
        NamedBeanHandle<Sensor> sensorHandle12 = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(sensor12.getDisplayName(), sensor12);
        Sensor sensor13 = InstanceManager.getDefault(SensorManager.class).provide("IL13");
        Sensor sensor14 = InstanceManager.getDefault(SensorManager.class).provide("IL14");
        sensor14.setUserName("Some user name");
        
        expressionSensor.setSensor((Sensor)null);
        Assert.assertNull("sensor handle is null", expressionSensor.getSensor());
        
        expressionSensor.setSensor(sensor11);
        Assert.assertTrue("sensor is correct", sensor11 == expressionSensor.getSensor().getBean());
        
        expressionSensor.setSensor((Sensor)null);
        Assert.assertNull("sensor handle is null", expressionSensor.getSensor());
        
        expressionSensor.setSensor(sensorHandle12);
        Assert.assertTrue("sensor handle is correct", sensorHandle12 == expressionSensor.getSensor());
        
        expressionSensor.setSensor("A non existent sensor");
        Assert.assertNull("sensor handle is null", expressionSensor.getSensor());
        JUnitAppender.assertErrorMessage("sensor \"A non existent sensor\" is not found");
        
        expressionSensor.setSensor(sensor13.getSystemName());
        Assert.assertTrue("sensor is correct", sensor13 == expressionSensor.getSensor().getBean());
        
        String userName = sensor14.getUserName();
        Assert.assertNotNull("sensor is not null", userName);
        expressionSensor.setSensor(userName);
        Assert.assertTrue("sensor is correct", sensor14 == expressionSensor.getSensor().getBean());
    }
    
    @Test
    public void testSetSensorException() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);
        
        // Test setSensor() when listeners are not registered
        Assert.assertNotNull("Sensor is not null", sensor);
        expressionSensor.setSensor(sensor);
        
        Assert.assertNotNull("Sensor is not null", expressionSensor.getSensor());
        // Enable the conditionalNG. This will register the listeners
        conditionalNG.setEnabled(true);
        boolean thrown = false;
        try {
            expressionSensor.setSensor("A sensor");
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setSensor must not be called when listeners are registered");
        
        thrown = false;
        try {
            Sensor sensor99 = InstanceManager.getDefault(SensorManager.class).provide("IS99");
            NamedBeanHandle<Sensor> sensorHandle99 =
                    InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(sensor99.getDisplayName(), sensor99);
            expressionSensor.setSensor(sensorHandle99);
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setSensor must not be called when listeners are registered");
        
        thrown = false;
        try {
            expressionSensor.setSensor((Sensor)null);
        } catch (RuntimeException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        JUnitAppender.assertErrorMessage("setSensor must not be called when listeners are registered");
    }
    
    @Test
    public void testRegisterListeners() {
        // Test registerListeners() when the ExpressionSensor has no sensor
        conditionalNG.setEnabled(false);
        expressionSensor.setSensor((Sensor)null);
        conditionalNG.setEnabled(true);
    }
    
    @Test
    public void testVetoableChange() throws PropertyVetoException {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);
        
        // Get the expressionSensor and set the sensor
        Assert.assertNotNull("Sensor is not null", sensor);
        expressionSensor.setSensor(sensor);
        
        // Get some other sensor for later use
        Sensor otherSensor = InstanceManager.getDefault(SensorManager.class).provide("IM99");
        Assert.assertNotNull("Sensor is not null", otherSensor);
        Assert.assertNotEquals("Sensor is not equal", sensor, otherSensor);
        
        // Test vetoableChange() for some other propery
        expressionSensor.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        Assert.assertEquals("Sensor matches", sensor, expressionSensor.getSensor().getBean());
        
        // Test vetoableChange() for a string
        expressionSensor.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        Assert.assertEquals("Sensor matches", sensor, expressionSensor.getSensor().getBean());
        expressionSensor.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        Assert.assertEquals("Sensor matches", sensor, expressionSensor.getSensor().getBean());
        
        // Test vetoableChange() for another sensor
        expressionSensor.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherSensor, null));
        Assert.assertEquals("Sensor matches", sensor, expressionSensor.getSensor().getBean());
        expressionSensor.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherSensor, null));
        Assert.assertEquals("Sensor matches", sensor, expressionSensor.getSensor().getBean());
        
        // Test vetoableChange() for its own sensor
        boolean thrown = false;
        try {
            expressionSensor.vetoableChange(new PropertyChangeEvent(this, "CanDelete", sensor, null));
        } catch (PropertyVetoException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        Assert.assertEquals("Sensor matches", sensor, expressionSensor.getSensor().getBean());
        expressionSensor.vetoableChange(new PropertyChangeEvent(this, "DoDelete", sensor, null));
        Assert.assertNull("Sensor is null", expressionSensor.getSensor());
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        
        _category = Category.ITEM;
        _isExternal = true;
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        conditionalNG.setRunOnGUIDelayed(false);
        conditionalNG.setEnabled(true);
        
        logixNG.addConditionalNG(conditionalNG);
        
        IfThenElse ifThenElse = new IfThenElse("IQDA321", null, IfThenElse.Type.TRIGGER_ACTION);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(ifThenElse);
        conditionalNG.getChild(0).connect(maleSocket);
        
        expressionSensor = new ExpressionSensor("IQDE321", null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expressionSensor);
        ifThenElse.getChild(0).connect(maleSocket2);
        
        _base = expressionSensor;
        _baseMaleSocket = maleSocket2;
        
        atomicBoolean = new AtomicBoolean(false);
        actionAtomicBoolean = new ActionAtomicBoolean(atomicBoolean, true);
        MaleSocket socketAtomicBoolean = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionAtomicBoolean);
        ifThenElse.getChild(1).connect(socketAtomicBoolean);
        
        sensor = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        expressionSensor.setSensor(sensor);
        sensor.setCommandedState(Sensor.ACTIVE);
        
        logixNG.setParentForAllChildren();
        logixNG.setEnabled(true);
        logixNG.activateLogixNG();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
