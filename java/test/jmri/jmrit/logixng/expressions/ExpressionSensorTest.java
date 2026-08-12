package jmri.jmrit.logixng.expressions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBean;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.logixng.LogixNG_Category;
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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        return String.format("Sensor IS1 is Active ::: Use default%n");
    }

    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         If Then Else. Execute on change ::: Use default%n" +
                "            ? If%n" +
                "               Sensor IS1 is Active ::: Use default%n" +
                "            ! Then%n" +
                "               Set the atomic boolean to true ::: Use default%n" +
                "            ! Else%n" +
                "               Socket not connected%n");
    }

    @Override
    public NamedBean createNewBean(String systemName) {
        return new ExpressionSensor(systemName, null);
    }

    @Override
    public boolean addNewSocket() {
        return false;
    }

    @Test
    public void testCtor() throws JmriException {
        ExpressionSensor expression2;
        assertNotNull( sensor, "sensor is not null");
        sensor.setState(Sensor.ON);

        expression2 = new ExpressionSensor("IQDE321", null);
        assertNotNull( expression2, "object exists");
        assertNull( expression2.getUserName(), "Username matches");
        assertEquals( "Sensor '' is Active", expression2.getLongDescription(), "String matches");

        expression2 = new ExpressionSensor("IQDE321", "My sensor");
        assertNotNull( expression2, "object exists");
        assertEquals( "My sensor", expression2.getUserName(), "Username matches");
        assertEquals( "Sensor '' is Active", expression2.getLongDescription(), "String matches");

        expression2 = new ExpressionSensor("IQDE321", null);
        expression2.getSelectNamedBean().setNamedBean(sensor);
        assertSame( sensor, expression2.getSelectNamedBean().getNamedBean().getBean(), "sensor is correct");
        assertNotNull( expression2, "object exists");
        assertNull( expression2.getUserName(), "Username matches");
        assertEquals( "Sensor IS1 is Active", expression2.getLongDescription(), "String matches");

        Sensor s = InstanceManager.getDefault(SensorManager.class).provide("IS2");
        expression2 = new ExpressionSensor("IQDE321", "My sensor");
        expression2.getSelectNamedBean().setNamedBean(s);
        assertSame( s, expression2.getSelectNamedBean().getNamedBean().getBean(), "sensor is correct");
        assertNotNull( expression2, "object exists");
        assertEquals( "My sensor", expression2.getUserName(), "Username matches");
        assertEquals( "Sensor IS2 is Active", expression2.getLongDescription(), "String matches");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new ExpressionSensor("IQE55:12:XY11", null);
            fail("Should have thrown, not created " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);

        ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new ExpressionSensor("IQE55:12:XY11", "A name");
            fail("Should have thrown, not created " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);
    }

    @Test
    public void testGetChild() {
        assertEquals( 0, expressionSensor.getChildCount(), "getChildCount() returns 0");

        UnsupportedOperationException ex = assertThrows( UnsupportedOperationException.class, () ->
            expressionSensor.getChild(0), "Exception is thrown");
        assertEquals( "Not supported.", ex.getMessage(), "Error message is correct");
    }

    @Test
    public void testSensorState() {
        assertEquals( "Inactive", ExpressionSensor.SensorState.Inactive.toString(), "String matches");
        assertEquals( "Active", ExpressionSensor.SensorState.Active.toString(), "String matches");
        assertEquals( "Other", ExpressionSensor.SensorState.Other.toString(), "String matches");

        assertSame( ExpressionSensor.SensorState.Inactive, ExpressionSensor.SensorState.get(Sensor.INACTIVE), "objects are equal");
        assertSame( ExpressionSensor.SensorState.Active, ExpressionSensor.SensorState.get(Sensor.ACTIVE), "objects are equal");
        assertSame( ExpressionSensor.SensorState.Other, ExpressionSensor.SensorState.get(Sensor.UNKNOWN), "objects are equal");
        assertSame( ExpressionSensor.SensorState.Other, ExpressionSensor.SensorState.get(Sensor.INCONSISTENT), "objects are equal");
        assertSame( ExpressionSensor.SensorState.Other, ExpressionSensor.SensorState.get(-1), "objects are equal");

        assertEquals( Sensor.INACTIVE, ExpressionSensor.SensorState.Inactive.getID(), "ID matches");
        assertEquals( Sensor.ACTIVE, ExpressionSensor.SensorState.Active.getID(), "ID matches");
        assertEquals( -1, ExpressionSensor.SensorState.Other.getID(), "ID matches");
    }

    @Test
    public void testCategory() {
        assertSame( LogixNG_Category.ITEM, _base.getCategory(), "Category matches");
    }

    @Test
    public void testDescription() {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        expressionSensor.getSelectNamedBean().removeNamedBean();
        assertEquals("Sensor", expressionSensor.getShortDescription());
        assertEquals("Sensor '' is Active", expressionSensor.getLongDescription());
        expressionSensor.getSelectNamedBean().setNamedBean(sensor);
        expressionSensor.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionSensor.getSelectEnum().setEnum(ExpressionSensor.SensorState.Inactive);
        assertEquals( "Sensor IS1 is Inactive", expressionSensor.getLongDescription());
        expressionSensor.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        assertEquals( "Sensor IS1 is not Inactive", expressionSensor.getLongDescription());
        expressionSensor.getSelectEnum().setEnum(ExpressionSensor.SensorState.Other);
        assertEquals( "Sensor IS1 is not Other", expressionSensor.getLongDescription());
    }

    @Test
    public void testExpression() throws SocketAlreadyConnectedException, JmriException {
        // Clear flag
        atomicBoolean.set(false);
        // Turn light off
        sensor.setCommandedState(Sensor.INACTIVE);
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        expressionSensor.getSelectNamedBean().setNamedBean(sensor);
        expressionSensor.set_Is_IsNot(Is_IsNot_Enum.Is);
        expressionSensor.getSelectEnum().setEnum(ExpressionSensor.SensorState.Active);

        // The action is not yet executed so the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        // Activate the sensor. This should not execute the conditional.
        sensor.setCommandedState(Sensor.ACTIVE);
        // The conditionalNG is not yet enabled so it shouldn't be executed.
        // So the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        // Inactivate the sensor. This should not execute the conditional.
        sensor.setCommandedState(Sensor.INACTIVE);
        // Enable the conditionalNG and all its children.
        conditionalNG.setEnabled(true);
        // The action is not yet executed so the atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        // Activate the sensor. This should execute the conditional.
        sensor.setCommandedState(Sensor.ACTIVE);
        // The action should now be executed so the atomic boolean should be true
        assertTrue( atomicBoolean.get(), "atomicBoolean is true");
        // Clear the atomic boolean.
        atomicBoolean.set(false);
        // Inactivate the sensor. This should not execute the conditional.
        sensor.setCommandedState(Sensor.INACTIVE);
        // The action should now be executed so the atomic boolean should be true
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");

        // Test IS_NOT
        expressionSensor.set_Is_IsNot(Is_IsNot_Enum.IsNot);
        // Activate the sensor. This should not execute the conditional.
        sensor.setCommandedState(Sensor.ACTIVE);
        // The action should now be executed so the atomic boolean should be true
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        // Inactivate the sensor. This should not execute the conditional.
        sensor.setCommandedState(Sensor.INACTIVE);
        // The action should now be executed so the atomic boolean should be true
        assertTrue( atomicBoolean.get(), "atomicBoolean is true");
    }

    @Test
    public void testSetSensor() {
        expressionSensor.unregisterListeners();

        Sensor otherSensor = InstanceManager.getDefault(SensorManager.class).provide("IM99");
        assertNotEquals( otherSensor, expressionSensor.getSelectNamedBean().getNamedBean().getBean(), "Sensors are different");
        expressionSensor.getSelectNamedBean().setNamedBean(otherSensor);
        assertEquals( otherSensor, expressionSensor.getSelectNamedBean().getNamedBean().getBean(), "Sensors are equal");

        NamedBeanHandle<Sensor> otherSensorHandle =
                InstanceManager.getDefault(NamedBeanHandleManager.class)
                        .getNamedBeanHandle(otherSensor.getDisplayName(), otherSensor);
        expressionSensor.getSelectNamedBean().removeNamedBean();
        assertNull( expressionSensor.getSelectNamedBean().getNamedBean(), "Sensor is null");
        expressionSensor.getSelectNamedBean().setNamedBean(otherSensorHandle);
        assertEquals( otherSensor, expressionSensor.getSelectNamedBean().getNamedBean().getBean(), "Sensors are equal");
        assertEquals( otherSensorHandle, expressionSensor.getSelectNamedBean().getNamedBean(), "SensorHandles are equal");
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

        expressionSensor.getSelectNamedBean().removeNamedBean();
        assertNull( expressionSensor.getSelectNamedBean().getNamedBean(), "sensor handle is null");

        expressionSensor.getSelectNamedBean().setNamedBean(sensor11);
        assertSame( sensor11, expressionSensor.getSelectNamedBean().getNamedBean().getBean(), "sensor is correct");

        expressionSensor.getSelectNamedBean().removeNamedBean();
        assertNull( expressionSensor.getSelectNamedBean().getNamedBean(), "sensor handle is null");

        expressionSensor.getSelectNamedBean().setNamedBean(sensorHandle12);
        assertSame( sensorHandle12, expressionSensor.getSelectNamedBean().getNamedBean(), "sensor handle is correct");

        expressionSensor.getSelectNamedBean().setNamedBean("A non existent sensor");
        assertNull( expressionSensor.getSelectNamedBean().getNamedBean(), "sensor handle is null");
        JUnitAppender.assertErrorMessage("Sensor \"A non existent sensor\" is not found");

        expressionSensor.getSelectNamedBean().setNamedBean(sensor13.getSystemName());
        assertSame( sensor13, expressionSensor.getSelectNamedBean().getNamedBean().getBean(), "sensor is correct");

        String userName = sensor14.getUserName();
        assertNotNull( userName, "sensor is not null");
        expressionSensor.getSelectNamedBean().setNamedBean(userName);
        assertSame( sensor14, expressionSensor.getSelectNamedBean().getNamedBean().getBean(), "sensor is correct");
    }

    @Test
    public void testSetSensorException() {
        assertNotNull( sensor, "Sensor is not null");
        assertNotNull( expressionSensor.getSelectNamedBean().getNamedBean(), "Sensor is not null");
        expressionSensor.registerListeners();
        RuntimeException ex = assertThrows( RuntimeException.class, () ->
            expressionSensor.getSelectNamedBean().setNamedBean("A sensor"),"Expected exception thrown");
        assertNotNull(ex);
        JUnitAppender.assertErrorMessage("setNamedBean must not be called when listeners are registered");

        ex = assertThrows( RuntimeException.class, () -> {
            Sensor sensor99 = InstanceManager.getDefault(SensorManager.class).provide("IS99");
            NamedBeanHandle<Sensor> sensorHandle99 =
                    InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(sensor99.getDisplayName(), sensor99);
            expressionSensor.getSelectNamedBean().setNamedBean(sensorHandle99);
        }, "Expected exception thrown");
        assertNotNull(ex);
        JUnitAppender.assertErrorMessage("setNamedBean must not be called when listeners are registered");

        ex = assertThrows( RuntimeException.class, () ->
            expressionSensor.getSelectNamedBean().removeNamedBean(),"Expected exception thrown");
        assertNotNull(ex);
        JUnitAppender.assertErrorMessage("setNamedBean must not be called when listeners are registered");
    }

    @Test
    public void testRegisterListeners() {
        // Test registerListeners() when the ExpressionSensor has no sensor
        conditionalNG.setEnabled(false);
        expressionSensor.getSelectNamedBean().removeNamedBean();
        conditionalNG.setEnabled(true);
    }

    @Test
    public void testVetoableChange() throws PropertyVetoException {
        // Disable the conditionalNG. This will unregister the listeners
        conditionalNG.setEnabled(false);

        // Get the expressionSensor and set the sensor
        assertNotNull( sensor, "Sensor is not null");
        expressionSensor.getSelectNamedBean().setNamedBean(sensor);

        // Get some other sensor for later use
        Sensor otherSensor = InstanceManager.getDefault(SensorManager.class).provide("IM99");
        assertNotNull( otherSensor, "Sensor is not null");
        assertNotEquals( sensor, otherSensor, "Sensor is not equal");

        // Test vetoableChange() for some other propery
        expressionSensor.vetoableChange(new PropertyChangeEvent(this, "CanSomething", "test", null));
        assertEquals( sensor, expressionSensor.getSelectNamedBean().getNamedBean().getBean(), "Sensor matches");

        // Test vetoableChange() for a string
        expressionSensor.vetoableChange(new PropertyChangeEvent(this, "CanDelete", "test", null));
        assertEquals( sensor, expressionSensor.getSelectNamedBean().getNamedBean().getBean(), "Sensor matches");
        expressionSensor.vetoableChange(new PropertyChangeEvent(this, "DoDelete", "test", null));
        assertEquals( sensor, expressionSensor.getSelectNamedBean().getNamedBean().getBean(), "Sensor matches");

        // Test vetoableChange() for another sensor
        expressionSensor.vetoableChange(new PropertyChangeEvent(this, "CanDelete", otherSensor, null));
        assertEquals( sensor, expressionSensor.getSelectNamedBean().getNamedBean().getBean(), "Sensor matches");
        expressionSensor.vetoableChange(new PropertyChangeEvent(this, "DoDelete", otherSensor, null));
        assertEquals( sensor, expressionSensor.getSelectNamedBean().getNamedBean().getBean(), "Sensor matches");

        // Test vetoableChange() for its own sensor
        PropertyVetoException ex = assertThrows( PropertyVetoException.class, () ->
            expressionSensor.getSelectNamedBean().vetoableChange(new PropertyChangeEvent(this, "CanDelete", sensor, null)),
                "Expected exception thrown");
        assertNotNull(ex);

        assertEquals( sensor, expressionSensor.getSelectNamedBean().getNamedBean().getBean(), "Sensor matches");
        expressionSensor.getSelectNamedBean().vetoableChange(new PropertyChangeEvent(this, "DoDelete", sensor, null));
        assertNull( expressionSensor.getSelectNamedBean().getNamedBean(), "Sensor is null");
    }

    @BeforeEach
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initLogixNGManager();

        _category = LogixNG_Category.ITEM;
        _isExternal = true;

        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);

        logixNG.addConditionalNG(conditionalNG);

        IfThenElse ifThenElse = new IfThenElse("IQDA321", null);
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
        expressionSensor.getSelectNamedBean().setNamedBean(sensor);
        sensor.setCommandedState(Sensor.ACTIVE);

        assertTrue( logixNG.setParentForAllChildren(new ArrayList<>()));
        logixNG.activate();
        logixNG.setEnabled(true);
    }

    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
