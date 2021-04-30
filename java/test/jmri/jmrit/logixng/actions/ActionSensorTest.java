package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test ActionSensor
 * 
 * @author Daniel Bergqvist 2018
 */
public class ActionSensorTest extends AbstractDigitalActionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private ActionSensor actionSensor;
    private Sensor sensor;
    
    
    @Test
    public void testSensorState() {
        Assert.assertEquals("String matches", "Inactive", ActionSensor.SensorState.Inactive.toString());
        Assert.assertEquals("String matches", "Active", ActionSensor.SensorState.Active.toString());
        Assert.assertEquals("String matches", "Toggle", ActionSensor.SensorState.Toggle.toString());
        
        Assert.assertTrue("objects are equal", ActionSensor.SensorState.Inactive == ActionSensor.SensorState.get(Sensor.INACTIVE));
        Assert.assertTrue("objects are equal", ActionSensor.SensorState.Active == ActionSensor.SensorState.get(Sensor.ACTIVE));
        Assert.assertTrue("objects are equal", ActionSensor.SensorState.Toggle == ActionSensor.SensorState.get(-1));
        
        boolean hasThrown = false;
        try {
            ActionSensor.SensorState.get(Sensor.UNKNOWN);
        } catch (IllegalArgumentException ex) {
            hasThrown = true;
            Assert.assertTrue("Error message is correct", "invalid sensor state".equals(ex.getMessage()));
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
        return String.format("Set sensor IS1 to state Active ::: Use default%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A logixNG%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         Set sensor IS1 to state Active ::: Use default%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new ActionSensor(systemName, null);
    }
    
    @Override
    public boolean addNewSocket() {
        return false;
    }
    
    @Test
    public void testCtor() throws JmriException {
        Assert.assertTrue("object exists", _base != null);
        
        ActionSensor action2;
        Assert.assertNotNull("sensor is not null", sensor);
        sensor.setState(Sensor.ON);
        
        action2 = new ActionSensor("IQDA321", null);
        Assert.assertNotNull("object exists", action2);
        Assert.assertNull("Username matches", action2.getUserName());
        Assert.assertEquals("String matches", "Set sensor '' to state Active", action2.getLongDescription());
        
        action2 = new ActionSensor("IQDA321", "My sensor");
        Assert.assertNotNull("object exists", action2);
        Assert.assertEquals("Username matches", "My sensor", action2.getUserName());
        Assert.assertEquals("String matches", "Set sensor '' to state Active", action2.getLongDescription());
        
        action2 = new ActionSensor("IQDA321", null);
        action2.setSensor(sensor);
        Assert.assertTrue("sensor is correct", sensor == action2.getSensor().getBean());
        Assert.assertNotNull("object exists", action2);
        Assert.assertNull("Username matches", action2.getUserName());
        Assert.assertEquals("String matches", "Set sensor IS1 to state Active", action2.getLongDescription());
        
        Sensor l = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        action2 = new ActionSensor("IQDA321", "My sensor");
        action2.setSensor(l);
        Assert.assertTrue("sensor is correct", l == action2.getSensor().getBean());
        Assert.assertNotNull("object exists", action2);
        Assert.assertEquals("Username matches", "My sensor", action2.getUserName());
        Assert.assertEquals("String matches", "Set sensor IS1 to state Active", action2.getLongDescription());
        
        boolean thrown = false;
        try {
            // Illegal system name
            new ActionSensor("IQA55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        thrown = false;
        try {
            // Illegal system name
            new ActionSensor("IQA55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        // Test setup(). This method doesn't do anything, but execute it for coverage.
        _base.setup();
    }
    
    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 0", 0 == actionSensor.getChildCount());
        
        boolean hasThrown = false;
        try {
            actionSensor.getChild(0);
        } catch (UnsupportedOperationException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "Not supported.", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }
    
    @Test
    public void testSetSensor() {
        Sensor sensor11 = InstanceManager.getDefault(SensorManager.class).provide("IS11");
        Sensor sensor12 = InstanceManager.getDefault(SensorManager.class).provide("IS12");
        NamedBeanHandle<Sensor> sensorHandle12 = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(sensor12.getDisplayName(), sensor12);
        Sensor sensor13 = InstanceManager.getDefault(SensorManager.class).provide("IS13");
        Sensor sensor14 = InstanceManager.getDefault(SensorManager.class).provide("IS14");
        sensor14.setUserName("Some user name");
        
        actionSensor.removeSensor();
        Assert.assertNull("sensor handle is null", actionSensor.getSensor());
        
        actionSensor.setSensor(sensor11);
        Assert.assertTrue("sensor is correct", sensor11 == actionSensor.getSensor().getBean());
        
        actionSensor.removeSensor();
        Assert.assertNull("sensor handle is null", actionSensor.getSensor());
        
        actionSensor.setSensor(sensorHandle12);
        Assert.assertTrue("sensor handle is correct", sensorHandle12 == actionSensor.getSensor());
        
        actionSensor.setSensor("A non existent sensor");
        Assert.assertNull("sensor handle is null", actionSensor.getSensor());
        JUnitAppender.assertWarnMessage("sensor \"A non existent sensor\" is not found");
        
        actionSensor.setSensor(sensor13.getSystemName());
        Assert.assertTrue("sensor is correct", sensor13 == actionSensor.getSensor().getBean());
        
        actionSensor.setSensor(sensor14.getUserName());
        Assert.assertTrue("sensor is correct", sensor14 == actionSensor.getSensor().getBean());
    }
    
    @Test
    public void testAction() throws SocketAlreadyConnectedException, JmriException {
        // Set the light
        sensor.setCommandedState(Sensor.INACTIVE);
        // The sensor should be inactive
        Assert.assertTrue("sensor is inactive",sensor.getCommandedState() == Sensor.INACTIVE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the sensor should be active
        Assert.assertTrue("sensor is active",sensor.getCommandedState() == Sensor.ACTIVE);
        
        // Test to set sensor to inactive
        actionSensor.setBeanState(ActionSensor.SensorState.Inactive);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the sensor should be active
        Assert.assertTrue("sensor is active",sensor.getCommandedState() == Sensor.INACTIVE);
        
        // Test to set sensor to toggle
        actionSensor.setBeanState(ActionSensor.SensorState.Toggle);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the sensor should be active
        Assert.assertTrue("sensor is active",sensor.getCommandedState() == Sensor.ACTIVE);
        
        // Test to set sensor to toggle
        actionSensor.setBeanState(ActionSensor.SensorState.Toggle);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the sensor should be active
        Assert.assertTrue("sensor is active",sensor.getCommandedState() == Sensor.INACTIVE);
    }
    
    @Test
    public void testIndirectAddressing() throws JmriException {
        
        Memory m1 = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        m1.setValue("IS102");
        
        Assert.assertTrue(conditionalNG.isActive());
        Sensor t1 = InstanceManager.getDefault(SensorManager.class).provide("IS101");
        Sensor t2 = InstanceManager.getDefault(SensorManager.class).provide("IS102");
        Sensor t3 = InstanceManager.getDefault(SensorManager.class).provide("IS103");
        Sensor t4 = InstanceManager.getDefault(SensorManager.class).provide("IS104");
        Sensor t5 = InstanceManager.getDefault(SensorManager.class).provide("IS105");
        
        actionSensor.setBeanState(ActionSensor.SensorState.Active);
        actionSensor.setSensor(t1.getSystemName());
        actionSensor.setReference("{IM1}");    // Points to "IS102"
        actionSensor.setLocalVariable("mySensor");
        actionSensor.setFormula("\"IS10\" + str(index)");
        _baseMaleSocket.addLocalVariable("refSensor", SymbolTable.InitialValueType.String, "IS103");
        _baseMaleSocket.addLocalVariable("mySensor", SymbolTable.InitialValueType.String, "IS104");
        _baseMaleSocket.addLocalVariable("index", SymbolTable.InitialValueType.Integer, "5");
        
        // Test direct addressing
        actionSensor.setAddressing(NamedBeanAddressing.Direct);
        t1.setState(Sensor.INACTIVE);
        t2.setState(Sensor.INACTIVE);
        t3.setState(Sensor.INACTIVE);
        t4.setState(Sensor.INACTIVE);
        t5.setState(Sensor.INACTIVE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct sensor should be thrown
        Assert.assertEquals(Sensor.ACTIVE, t1.getCommandedState());
        Assert.assertEquals(Sensor.INACTIVE, t2.getCommandedState());
        Assert.assertEquals(Sensor.INACTIVE, t3.getCommandedState());
        Assert.assertEquals(Sensor.INACTIVE, t4.getCommandedState());
        Assert.assertEquals(Sensor.INACTIVE, t5.getCommandedState());
        
        // Test reference by memory addressing
        actionSensor.setAddressing(NamedBeanAddressing.Reference);
        t1.setState(Sensor.INACTIVE);
        t2.setState(Sensor.INACTIVE);
        t3.setState(Sensor.INACTIVE);
        t4.setState(Sensor.INACTIVE);
        t5.setState(Sensor.INACTIVE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct sensor should be thrown
        Assert.assertEquals(Sensor.INACTIVE, t1.getCommandedState());
        Assert.assertEquals(Sensor.ACTIVE, t2.getCommandedState());
        Assert.assertEquals(Sensor.INACTIVE, t3.getCommandedState());
        Assert.assertEquals(Sensor.INACTIVE, t4.getCommandedState());
        Assert.assertEquals(Sensor.INACTIVE, t5.getCommandedState());
        
        // Test reference by local variable addressing
        actionSensor.setReference("{refSensor}");    // Points to "IS103"
        actionSensor.setAddressing(NamedBeanAddressing.Reference);
        t1.setState(Sensor.INACTIVE);
        t2.setState(Sensor.INACTIVE);
        t3.setState(Sensor.INACTIVE);
        t4.setState(Sensor.INACTIVE);
        t5.setState(Sensor.INACTIVE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct sensor should be thrown
        Assert.assertEquals(Sensor.INACTIVE, t1.getCommandedState());
        Assert.assertEquals(Sensor.INACTIVE, t2.getCommandedState());
        Assert.assertEquals(Sensor.ACTIVE, t3.getCommandedState());
        Assert.assertEquals(Sensor.INACTIVE, t4.getCommandedState());
        Assert.assertEquals(Sensor.INACTIVE, t5.getCommandedState());
        
        // Test local variable addressing
        actionSensor.setAddressing(NamedBeanAddressing.LocalVariable);
        t1.setState(Sensor.INACTIVE);
        t2.setState(Sensor.INACTIVE);
        t3.setState(Sensor.INACTIVE);
        t4.setState(Sensor.INACTIVE);
        t5.setState(Sensor.INACTIVE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct sensor should be thrown
        Assert.assertEquals(Sensor.INACTIVE, t1.getCommandedState());
        Assert.assertEquals(Sensor.INACTIVE, t2.getCommandedState());
        Assert.assertEquals(Sensor.INACTIVE, t3.getCommandedState());
        Assert.assertEquals(Sensor.ACTIVE, t4.getCommandedState());
        Assert.assertEquals(Sensor.INACTIVE, t5.getCommandedState());
        
        // Test formula addressing
        actionSensor.setAddressing(NamedBeanAddressing.Formula);
        t1.setState(Sensor.INACTIVE);
        t2.setState(Sensor.INACTIVE);
        t3.setState(Sensor.INACTIVE);
        t4.setState(Sensor.INACTIVE);
        t5.setState(Sensor.INACTIVE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct sensor should be thrown
        Assert.assertEquals(Sensor.INACTIVE, t1.getCommandedState());
        Assert.assertEquals(Sensor.INACTIVE, t2.getCommandedState());
        Assert.assertEquals(Sensor.INACTIVE, t3.getCommandedState());
        Assert.assertEquals(Sensor.INACTIVE, t4.getCommandedState());
        Assert.assertEquals(Sensor.ACTIVE, t5.getCommandedState());
    }
    
    @Test
    public void testIndirectStateAddressing() throws JmriException {
        
        Memory m1 = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        m1.setValue("IS102");
        
        Assert.assertTrue(conditionalNG.isActive());
        
        
        // Test direct addressing
        actionSensor.setStateAddressing(NamedBeanAddressing.Direct);
        // Test Inactive
        sensor.setState(Sensor.ACTIVE);
        actionSensor.setBeanState(ActionSensor.SensorState.Inactive);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct sensor should be thrown
        Assert.assertEquals(Sensor.INACTIVE, sensor.getCommandedState());
        // Test Inactive
        sensor.setState(Sensor.INACTIVE);
        actionSensor.setBeanState(ActionSensor.SensorState.Active);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct sensor should be thrown
        Assert.assertEquals(Sensor.ACTIVE, sensor.getCommandedState());
        
        
        // Test reference by memory addressing
        actionSensor.setStateAddressing(NamedBeanAddressing.Reference);
        actionSensor.setStateReference("{IM1}");
        // Test Inactive
        m1.setValue("Inactive");
        sensor.setState(Sensor.ACTIVE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct sensor should be thrown
        Assert.assertEquals(Sensor.INACTIVE, sensor.getCommandedState());
        // Test Active
        m1.setValue("Active");
        sensor.setState(Sensor.INACTIVE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct sensor should be thrown
        Assert.assertEquals(Sensor.ACTIVE, sensor.getCommandedState());
        
        
        // Test reference by local variable addressing
        actionSensor.setStateAddressing(NamedBeanAddressing.Reference);
        actionSensor.setStateReference("{refVariable}");
        // Test Inactive
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "Inactive");
        sensor.setState(Sensor.ACTIVE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct sensor should be thrown
        Assert.assertEquals(Sensor.INACTIVE, sensor.getCommandedState());
        // Test Active
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "Active");
        sensor.setState(Sensor.INACTIVE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct sensor should be thrown
        Assert.assertEquals(Sensor.ACTIVE, sensor.getCommandedState());
        
        
        // Test local variable addressing
        actionSensor.setStateAddressing(NamedBeanAddressing.Reference);
        actionSensor.setStateLocalVariable("myVariable");
        // Test Inactive
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "Inactive");
        sensor.setState(Sensor.ACTIVE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct sensor should be thrown
        Assert.assertEquals(Sensor.INACTIVE, sensor.getCommandedState());
        // Test Active
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "Active");
        sensor.setState(Sensor.INACTIVE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct sensor should be thrown
        Assert.assertEquals(Sensor.ACTIVE, sensor.getCommandedState());
        
        
        // Test formula addressing
        actionSensor.setStateAddressing(NamedBeanAddressing.Formula);
        actionSensor.setStateFormula("refVariable + myVariable");
        // Test Inactive
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "Ina");
        _baseMaleSocket.addLocalVariable("myVariable", SymbolTable.InitialValueType.String, "ctive");
        sensor.setState(Sensor.ACTIVE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct sensor should be thrown
        Assert.assertEquals(Sensor.INACTIVE, sensor.getCommandedState());
        // Test Active
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "Act");
        _baseMaleSocket.addLocalVariable("myVariable", SymbolTable.InitialValueType.String, "ive");
        sensor.setState(Sensor.INACTIVE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct sensor should be thrown
        Assert.assertEquals(Sensor.ACTIVE, sensor.getCommandedState());
    }
    
    @Test
    public void testVetoableChange() throws PropertyVetoException {
        // Get the action and set the sensor
        Sensor sensor = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        Assert.assertNotNull("Sensor is not null", sensor);
        ActionSensor action = new ActionSensor(InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null);
        action.setSensor(sensor);
        
        // Get some other sensor for later use
        Sensor otherSensor = InstanceManager.getDefault(SensorManager.class).provide("IM99");
        Assert.assertNotNull("Sensor is not null", otherSensor);
        Assert.assertNotEquals("Sensor is not equal", sensor, otherSensor);
        
        // Test vetoableChange() for some other propery
        action.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        Assert.assertEquals("Sensor matches", sensor, action.getSensor().getBean());
        
        // Test vetoableChange() for a string
        action.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        Assert.assertEquals("Sensor matches", sensor, action.getSensor().getBean());
        action.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        Assert.assertEquals("Sensor matches", sensor, action.getSensor().getBean());
        
        // Test vetoableChange() for another sensor
        action.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherSensor, null));
        Assert.assertEquals("Sensor matches", sensor, action.getSensor().getBean());
        action.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherSensor, null));
        Assert.assertEquals("Sensor matches", sensor, action.getSensor().getBean());
        
        // Test vetoableChange() for its own sensor
        boolean thrown = false;
        try {
            action.vetoableChange(new PropertyChangeEvent(this, "CanDelete", sensor, null));
        } catch (PropertyVetoException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        Assert.assertEquals("Sensor matches", sensor, action.getSensor().getBean());
        action.vetoableChange(new PropertyChangeEvent(this, "DoDelete", sensor, null));
        Assert.assertNull("Sensor is null", action.getSensor());
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
        Assert.assertEquals("String matches", "Set sensor", _base.getShortDescription());
    }
    
    @Test
    public void testLongDescription() {
        Assert.assertEquals("String matches", "Set sensor IS1 to state Active", _base.getLongDescription());
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
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initLogixNGManager();
        
        _category = Category.ITEM;
        _isExternal = true;
        
        sensor = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        sensor.setCommandedState(Sensor.INACTIVE);
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A logixNG");
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        logixNG.addConditionalNG(conditionalNG);
        conditionalNG.setEnabled(true);
        conditionalNG.setRunDelayed(false);
        actionSensor = new ActionSensor(InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null);
        actionSensor.setSensor(sensor);
        actionSensor.setBeanState(ActionSensor.SensorState.Active);
        MaleSocket socket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionSensor);
        conditionalNG.getChild(0).connect(socket);
        
        _base = actionSensor;
        _baseMaleSocket = socket;
        
        logixNG.setParentForAllChildren();
        logixNG.setEnabled(true);
    }

    @After
    public void tearDown() {
        // JUnitAppender.clearBacklog();    REMOVE THIS!!!
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }
    
}
