package jmri.jmrit.logixng.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.util.ArrayList;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Before;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        assertEquals( "Inactive", ActionSensor.SensorState.Inactive.toString(), "String matches");
        assertEquals( "Active", ActionSensor.SensorState.Active.toString(), "String matches");
        assertEquals( "Toggle", ActionSensor.SensorState.Toggle.toString(), "String matches");

        assertSame( ActionSensor.SensorState.Inactive, ActionSensor.SensorState.get(Sensor.INACTIVE), "objects are equal");
        assertSame( ActionSensor.SensorState.Active, ActionSensor.SensorState.get(Sensor.ACTIVE), "objects are equal");
        assertSame( ActionSensor.SensorState.Toggle, ActionSensor.SensorState.get(-1), "objects are equal");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () ->
            ActionSensor.SensorState.get(100),      // The number 100 is a state that ActionSensor.SensorState isn't aware of
                "Exception is thrown");
        assertEquals( "invalid sensor state", ex.getMessage(), "Error message is correct");
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
        assertNotNull( _base, "object exists");

        ActionSensor action2;
        assertNotNull( sensor, "sensor is not null");
        sensor.setState(Sensor.ON);

        action2 = new ActionSensor("IQDA321", null);
        assertNotNull( action2, "object exists");
        assertNull( action2.getUserName(), "Username matches");
        assertEquals( "Set sensor '' to state Active", action2.getLongDescription(), "String matches");

        action2 = new ActionSensor("IQDA321", "My sensor");
        assertNotNull( action2, "object exists");
        assertEquals( "My sensor", action2.getUserName(), "Username matches");
        assertEquals( "Set sensor '' to state Active", action2.getLongDescription(), "String matches");

        action2 = new ActionSensor("IQDA321", null);
        action2.getSelectNamedBean().setNamedBean(sensor);
        assertSame( sensor, action2.getSelectNamedBean().getNamedBean().getBean(), "sensor is correct");
        assertNotNull( action2, "object exists");
        assertNull( action2.getUserName(), "Username matches");
        assertEquals( "Set sensor IS1 to state Active", action2.getLongDescription(), "String matches");

        Sensor l = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        action2 = new ActionSensor("IQDA321", "My sensor");
        action2.getSelectNamedBean().setNamedBean(l);
        assertSame( l, action2.getSelectNamedBean().getNamedBean().getBean(), "sensor is correct");
        assertNotNull( action2, "object exists");
        assertEquals( "My sensor", action2.getUserName(), "Username matches");
        assertEquals( "Set sensor IS1 to state Active", action2.getLongDescription(), "String matches");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new ActionSensor("IQA55:12:XY11", null);
            fail("Did not throw, created " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);

        ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new ActionSensor("IQA55:12:XY11", "A name");
            fail("Did not throw, created " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);

        // Test setup(). This method doesn't do anything, but execute it for coverage.
        _base.setup();
    }

    @Test
    public void testGetChild() {
        assertEquals( 0, actionSensor.getChildCount(), "getChildCount() returns 0");

        UnsupportedOperationException ex = assertThrows( UnsupportedOperationException.class, () ->
            actionSensor.getChild(0), "Exception is thrown");
        assertEquals( "Not supported.", ex.getMessage(), "Error message is correct");
    }

    @Test
    public void testSetSensor() {
        Sensor sensor11 = InstanceManager.getDefault(SensorManager.class).provide("IS11");
        Sensor sensor12 = InstanceManager.getDefault(SensorManager.class).provide("IS12");
        NamedBeanHandle<Sensor> sensorHandle12 = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(sensor12.getDisplayName(), sensor12);
        Sensor sensor13 = InstanceManager.getDefault(SensorManager.class).provide("IS13");
        Sensor sensor14 = InstanceManager.getDefault(SensorManager.class).provide("IS14");
        sensor14.setUserName("Some user name");

        actionSensor.getSelectNamedBean().removeNamedBean();
        assertNull( actionSensor.getSelectNamedBean().getNamedBean(), "sensor handle is null");

        actionSensor.getSelectNamedBean().setNamedBean(sensor11);
        assertSame( sensor11, actionSensor.getSelectNamedBean().getNamedBean().getBean(), "sensor is correct");

        actionSensor.getSelectNamedBean().removeNamedBean();
        assertNull( actionSensor.getSelectNamedBean().getNamedBean(), "sensor handle is null");

        actionSensor.getSelectNamedBean().setNamedBean(sensorHandle12);
        assertSame( sensorHandle12, actionSensor.getSelectNamedBean().getNamedBean(), "sensor handle is correct");

        actionSensor.getSelectNamedBean().setNamedBean("A non existent sensor");
        assertNull( actionSensor.getSelectNamedBean().getNamedBean(), "sensor handle is null");
        JUnitAppender.assertErrorMessage("Sensor \"A non existent sensor\" is not found");

        actionSensor.getSelectNamedBean().setNamedBean(sensor13.getSystemName());
        assertSame( sensor13, actionSensor.getSelectNamedBean().getNamedBean().getBean(), "sensor is correct");

        String sensor14UserName = sensor14.getUserName();
        assertNotNull(sensor14UserName);
        actionSensor.getSelectNamedBean().setNamedBean(sensor14UserName);
        assertSame( sensor14, actionSensor.getSelectNamedBean().getNamedBean().getBean(), "sensor is correct");
    }

    @Test
    public void testAction() throws SocketAlreadyConnectedException, JmriException {
        // Set the light
        sensor.setCommandedState(Sensor.INACTIVE);
        // The sensor should be inactive
        assertEquals( Sensor.INACTIVE, sensor.getCommandedState(), "sensor is inactive");
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the sensor should be active
        assertEquals( Sensor.ACTIVE, sensor.getCommandedState(), "sensor is active");

        // Test to set sensor to inactive
        actionSensor.getSelectEnum().setEnum(ActionSensor.SensorState.Inactive);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the sensor should be active
        assertEquals( sensor.getCommandedState(), Sensor.INACTIVE, "sensor is active");

        // Test to set sensor to toggle
        actionSensor.getSelectEnum().setEnum(ActionSensor.SensorState.Toggle);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the sensor should be active
        assertEquals( sensor.getCommandedState(), Sensor.ACTIVE, "sensor is active");

        // Test to set sensor to toggle
        actionSensor.getSelectEnum().setEnum(ActionSensor.SensorState.Toggle);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the sensor should be active
        assertEquals( sensor.getCommandedState(), Sensor.INACTIVE, "sensor is active");
    }

    @Test
    public void testIndirectAddressing() throws JmriException {

        Memory m1 = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        m1.setValue("IS102");

        assertTrue(conditionalNG.isActive());
        Sensor t1 = InstanceManager.getDefault(SensorManager.class).provide("IS101");
        Sensor t2 = InstanceManager.getDefault(SensorManager.class).provide("IS102");
        Sensor t3 = InstanceManager.getDefault(SensorManager.class).provide("IS103");
        Sensor t4 = InstanceManager.getDefault(SensorManager.class).provide("IS104");
        Sensor t5 = InstanceManager.getDefault(SensorManager.class).provide("IS105");

        actionSensor.getSelectEnum().setEnum(ActionSensor.SensorState.Active);
        actionSensor.getSelectNamedBean().setNamedBean(t1.getSystemName());
        actionSensor.getSelectNamedBean().setReference("{IM1}");    // Points to "IS102"
        actionSensor.getSelectNamedBean().setLocalVariable("mySensor");
        actionSensor.getSelectNamedBean().setFormula("\"IS10\" + str(index)");
        _baseMaleSocket.addLocalVariable("refSensor", SymbolTable.InitialValueType.String, "IS103");
        _baseMaleSocket.addLocalVariable("mySensor", SymbolTable.InitialValueType.String, "IS104");
        _baseMaleSocket.addLocalVariable("index", SymbolTable.InitialValueType.Integer, "5");

        // Test direct addressing
        actionSensor.getSelectNamedBean().setAddressing(NamedBeanAddressing.Direct);
        t1.setState(Sensor.INACTIVE);
        t2.setState(Sensor.INACTIVE);
        t3.setState(Sensor.INACTIVE);
        t4.setState(Sensor.INACTIVE);
        t5.setState(Sensor.INACTIVE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct sensor should be thrown
        assertEquals(Sensor.ACTIVE, t1.getCommandedState());
        assertEquals(Sensor.INACTIVE, t2.getCommandedState());
        assertEquals(Sensor.INACTIVE, t3.getCommandedState());
        assertEquals(Sensor.INACTIVE, t4.getCommandedState());
        assertEquals(Sensor.INACTIVE, t5.getCommandedState());

        // Test reference by memory addressing
        actionSensor.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
        t1.setState(Sensor.INACTIVE);
        t2.setState(Sensor.INACTIVE);
        t3.setState(Sensor.INACTIVE);
        t4.setState(Sensor.INACTIVE);
        t5.setState(Sensor.INACTIVE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct sensor should be thrown
        assertEquals(Sensor.INACTIVE, t1.getCommandedState());
        assertEquals(Sensor.ACTIVE, t2.getCommandedState());
        assertEquals(Sensor.INACTIVE, t3.getCommandedState());
        assertEquals(Sensor.INACTIVE, t4.getCommandedState());
        assertEquals(Sensor.INACTIVE, t5.getCommandedState());

        // Test reference by local variable addressing
        actionSensor.getSelectNamedBean().setReference("{refSensor}");    // Points to "IS103"
        actionSensor.getSelectNamedBean().setAddressing(NamedBeanAddressing.Reference);
        t1.setState(Sensor.INACTIVE);
        t2.setState(Sensor.INACTIVE);
        t3.setState(Sensor.INACTIVE);
        t4.setState(Sensor.INACTIVE);
        t5.setState(Sensor.INACTIVE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct sensor should be thrown
        assertEquals(Sensor.INACTIVE, t1.getCommandedState());
        assertEquals(Sensor.INACTIVE, t2.getCommandedState());
        assertEquals(Sensor.ACTIVE, t3.getCommandedState());
        assertEquals(Sensor.INACTIVE, t4.getCommandedState());
        assertEquals(Sensor.INACTIVE, t5.getCommandedState());

        // Test local variable addressing
        actionSensor.getSelectNamedBean().setAddressing(NamedBeanAddressing.LocalVariable);
        t1.setState(Sensor.INACTIVE);
        t2.setState(Sensor.INACTIVE);
        t3.setState(Sensor.INACTIVE);
        t4.setState(Sensor.INACTIVE);
        t5.setState(Sensor.INACTIVE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct sensor should be thrown
        assertEquals(Sensor.INACTIVE, t1.getCommandedState());
        assertEquals(Sensor.INACTIVE, t2.getCommandedState());
        assertEquals(Sensor.INACTIVE, t3.getCommandedState());
        assertEquals(Sensor.ACTIVE, t4.getCommandedState());
        assertEquals(Sensor.INACTIVE, t5.getCommandedState());

        // Test formula addressing
        actionSensor.getSelectNamedBean().setAddressing(NamedBeanAddressing.Formula);
        t1.setState(Sensor.INACTIVE);
        t2.setState(Sensor.INACTIVE);
        t3.setState(Sensor.INACTIVE);
        t4.setState(Sensor.INACTIVE);
        t5.setState(Sensor.INACTIVE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct sensor should be thrown
        assertEquals(Sensor.INACTIVE, t1.getCommandedState());
        assertEquals(Sensor.INACTIVE, t2.getCommandedState());
        assertEquals(Sensor.INACTIVE, t3.getCommandedState());
        assertEquals(Sensor.INACTIVE, t4.getCommandedState());
        assertEquals(Sensor.ACTIVE, t5.getCommandedState());
    }

    @Test
    public void testIndirectStateAddressing() throws JmriException {

        Memory m1 = InstanceManager.getDefault(MemoryManager.class).provide("IM1");
        m1.setValue("IS102");

        assertTrue(conditionalNG.isActive());


        // Test direct addressing
        actionSensor.getSelectEnum().setAddressing(NamedBeanAddressing.Direct);
        // Test Inactive
        sensor.setState(Sensor.ACTIVE);
        actionSensor.getSelectEnum().setEnum(ActionSensor.SensorState.Inactive);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct sensor should be thrown
        assertEquals(Sensor.INACTIVE, sensor.getCommandedState());
        // Test Inactive
        sensor.setState(Sensor.INACTIVE);
        actionSensor.getSelectEnum().setEnum(ActionSensor.SensorState.Active);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct sensor should be thrown
        assertEquals(Sensor.ACTIVE, sensor.getCommandedState());


        // Test reference by memory addressing
        actionSensor.getSelectEnum().setAddressing(NamedBeanAddressing.Reference);
        actionSensor.getSelectEnum().setReference("{IM1}");
        // Test Inactive
        m1.setValue("Inactive");
        sensor.setState(Sensor.ACTIVE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct sensor should be thrown
        assertEquals(Sensor.INACTIVE, sensor.getCommandedState());
        // Test Active
        m1.setValue("Active");
        sensor.setState(Sensor.INACTIVE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct sensor should be thrown
        assertEquals(Sensor.ACTIVE, sensor.getCommandedState());


        // Test reference by local variable addressing
        actionSensor.getSelectEnum().setAddressing(NamedBeanAddressing.Reference);
        actionSensor.getSelectEnum().setReference("{refVariable}");
        // Test Inactive
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "Inactive");
        sensor.setState(Sensor.ACTIVE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct sensor should be thrown
        assertEquals(Sensor.INACTIVE, sensor.getCommandedState());
        // Test Active
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "Active");
        sensor.setState(Sensor.INACTIVE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct sensor should be thrown
        assertEquals(Sensor.ACTIVE, sensor.getCommandedState());


        // Test local variable addressing
        actionSensor.getSelectEnum().setAddressing(NamedBeanAddressing.Reference);
        actionSensor.getSelectEnum().setLocalVariable("myVariable");
        // Test Inactive
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "Inactive");
        sensor.setState(Sensor.ACTIVE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct sensor should be thrown
        assertEquals(Sensor.INACTIVE, sensor.getCommandedState());
        // Test Active
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "Active");
        sensor.setState(Sensor.INACTIVE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct sensor should be thrown
        assertEquals(Sensor.ACTIVE, sensor.getCommandedState());


        // Test formula addressing
        actionSensor.getSelectEnum().setAddressing(NamedBeanAddressing.Formula);
        actionSensor.getSelectEnum().setFormula("refVariable + myVariable");
        // Test Inactive
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "Ina");
        _baseMaleSocket.addLocalVariable("myVariable", SymbolTable.InitialValueType.String, "ctive");
        sensor.setState(Sensor.ACTIVE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct sensor should be thrown
        assertEquals(Sensor.INACTIVE, sensor.getCommandedState());
        // Test Active
        _baseMaleSocket.clearLocalVariables();
        _baseMaleSocket.addLocalVariable("refVariable", SymbolTable.InitialValueType.String, "Act");
        _baseMaleSocket.addLocalVariable("myVariable", SymbolTable.InitialValueType.String, "ive");
        sensor.setState(Sensor.INACTIVE);
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the correct sensor should be thrown
        assertEquals(Sensor.ACTIVE, sensor.getCommandedState());
    }

    @Test
    public void testVetoableChange() throws PropertyVetoException {
        // Get the action and set the sensor
        sensor = InstanceManager.getDefault(SensorManager.class).provide("IS1");
        assertNotNull( sensor, "Sensor is not null");
        ActionSensor action = new ActionSensor(InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null);
        action.getSelectNamedBean().setNamedBean(sensor);

        // Get some other sensor for later use
        Sensor otherSensor = InstanceManager.getDefault(SensorManager.class).provide("IM99");
        assertNotNull( otherSensor, "Sensor is not null");
        assertNotEquals( sensor, otherSensor, "Sensor is not equal");

        // Test vetoableChange() for some other propery
        action.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        assertEquals( sensor, action.getSelectNamedBean().getNamedBean().getBean(), "Sensor matches");

        // Test vetoableChange() for a string
        action.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        assertEquals( sensor, action.getSelectNamedBean().getNamedBean().getBean(), "Sensor matches");
        action.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        assertEquals( sensor, action.getSelectNamedBean().getNamedBean().getBean(), "Sensor matches");

        // Test vetoableChange() for another sensor
        action.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherSensor, null));
        assertEquals( sensor, action.getSelectNamedBean().getNamedBean().getBean(), "Sensor matches");
        action.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherSensor, null));
        assertEquals( sensor, action.getSelectNamedBean().getNamedBean().getBean(), "Sensor matches");

        // Test vetoableChange() for its own sensor
        PropertyVetoException ex = assertThrows( PropertyVetoException.class, () ->
            action.getSelectNamedBean().vetoableChange(
                    new PropertyChangeEvent(this, "CanDelete", sensor, null)),
                "Expected exception thrown");
        assertNotNull(ex);

        assertEquals( sensor, action.getSelectNamedBean().getNamedBean().getBean(), "Sensor matches");
        action.getSelectNamedBean().vetoableChange(new PropertyChangeEvent(this, "DoDelete", sensor, null));
        assertNull( action.getSelectNamedBean().getNamedBean(), "Sensor is null");
    }

    @Test
    public void testCategory() {
        assertSame( LogixNG_Category.ITEM, _base.getCategory(), "Category matches");
    }

    @Test
    public void testShortDescription() {
        assertEquals( "Sensor", _base.getShortDescription(), "String matches");
    }

    @Test
    public void testLongDescription() {
        assertEquals( "Set sensor IS1 to state Active", _base.getLongDescription(), "String matches");
    }

    @Test
    public void testChild() {
        assertEquals( 0, _base.getChildCount(), "Num children is zero");
        UnsupportedOperationException ex = assertThrows( UnsupportedOperationException.class, () ->
            _base.getChild(0), "Exception is thrown");
        assertEquals( "Not supported.", ex.getMessage(), "Error message is correct");
    }

    @Before
    @BeforeEach
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initLogixNGManager();

        _category = LogixNG_Category.ITEM;
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
        actionSensor.getSelectNamedBean().setNamedBean(sensor);
        actionSensor.getSelectEnum().setEnum(ActionSensor.SensorState.Active);
        MaleSocket socket = InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionSensor);
        conditionalNG.getChild(0).connect(socket);

        _base = actionSensor;
        _baseMaleSocket = socket;

        assertTrue( logixNG.setParentForAllChildren(new ArrayList<>()));
        logixNG.activate();
        logixNG.setEnabled(true);
    }

    @After
    @AfterEach
    public void tearDown() {
        // JUnitAppender.clearBacklog();    REMOVE THIS!!!
        JUnitUtil.deregisterBlockManagerShutdownTask();
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }

}
