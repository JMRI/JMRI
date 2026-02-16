package jmri.jmrit.logixng.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.AnalogExpressionConstant;
import jmri.jmrit.logixng.expressions.AnalogExpressionMemory;
import jmri.jmrit.logixng.expressions.ExpressionMemory;
import jmri.jmrit.logixng.expressions.ExpressionSensor;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.ToDo;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Test ActionThrottle
 *
 * @author Daniel Bergqvist 2019
 */
public class ActionThrottleTest extends AbstractDigitalActionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private ActionThrottle actionThrottle;

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
        AnalogExpressionBean childExpression = new AnalogExpressionConstant("IQAE999", null);
        MaleSocket maleSocketChild =
                InstanceManager.getDefault(AnalogExpressionManager.class).registerExpression(childExpression);
        return maleSocketChild;
    }

    @Override
    public String getExpectedPrintedTree() {
        return String.format(
                "Throttle. Don't wait for throttle. Stop loco when switching loco ::: Use default%n" +
                "   ?~ Address%n" +
                "      Socket not connected%n" +
                "   ?~ Speed%n" +
                "      Socket not connected%n" +
                "   ? Direction%n" +
                "      Socket not connected%n" +
                "   ?~ Function%n" +
                "      Socket not connected%n" +
                "   ? FunctionOnOff%n" +
                "      Socket not connected%n");
    }

    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         Throttle. Don't wait for throttle. Stop loco when switching loco ::: Use default%n" +
                "            ?~ Address%n" +
                "               Socket not connected%n" +
                "            ?~ Speed%n" +
                "               Socket not connected%n" +
                "            ? Direction%n" +
                "               Socket not connected%n" +
                "            ?~ Function%n" +
                "               Socket not connected%n" +
                "            ? FunctionOnOff%n" +
                "               Socket not connected%n");
    }

    @Override
    public NamedBean createNewBean(String systemName) {
        return new ActionThrottle(systemName, null);
    }

    @Override
    public boolean addNewSocket() {
        return false;
    }

    @Test
    public void testCtor() {
        ActionThrottle action2;

        action2 = new ActionThrottle("IQDA321", null);
        assertNotNull( action2, "object exists");
        assertNull( action2.getUserName(), "Username matches");
        assertEquals( "Throttle. Don't wait for throttle. Stop loco when switching loco", action2.getLongDescription(), "String matches");

        action2 = new ActionThrottle("IQDA321", "My throttle");
        assertNotNull( action2, "object exists");
        assertEquals( "My throttle", action2.getUserName(), "Username matches");
        assertEquals( "Throttle. Don't wait for throttle. Stop loco when switching loco", action2.getLongDescription(), "String matches");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class,
            () -> {
                ActionThrottle at = new ActionThrottle("IQA55:12:XY11", null);
                fail( "Action Thtottle created: " + at.toString());
            }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);

        ex = assertThrows( IllegalArgumentException.class,
            () -> {
                ActionThrottle at = new ActionThrottle("IQA55:12:XY11", "A name");
                fail( "Action Thtottle created: " + at.toString());
            }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);
    }

    @Test
    public void testCtorAndSetup1() {
        ActionThrottle expression = new ActionThrottle("IQDA321", null);
        assertNotNull( expression, "exists");
        assertEquals( 5, expression.getChildCount(), "expression has 5 female sockets");
        expression.getChild(0).setName("XYZ123");
        expression.setLocoAddressSocketSystemName("IQAE52");
        expression.getChild(1).setName("ZH12");
        expression.setLocoSpeedSocketSystemName("IQAE554");
        expression.getChild(2).setName("Bj23");
        expression.setLocoDirectionSocketSystemName("IQDE594");

        assertEquals( "XYZ123", expression.getChild(0).getName(),
            "expression female socket name is XYZ123");
        assertEquals(
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$AnalogSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogExpressionSocket",
                expression.getChild(0).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(0).isConnected(),
            "expression female socket is not connected");

        assertEquals( "ZH12", expression.getChild(1).getName(),
            "expression female socket name is ZH12");
        assertEquals(
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$AnalogSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogExpressionSocket",
                expression.getChild(1).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(1).isConnected(),
            "expression female socket is not connected");

        assertEquals( "Bj23", expression.getChild(2).getName(),
            "expression female socket name is Bj23");
        assertEquals(
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                expression.getChild(2).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(2).isConnected(),
                "expression female socket is not connected");

        // Setup action. This connects the child actions to this action
        expression.setup();

        JUnitAppender.assertMessage("cannot load analog expression IQAE52");
        JUnitAppender.assertMessage("cannot load analog expression IQAE554");
        JUnitAppender.assertMessage("cannot load digital expression IQDE594");

        assertEquals( "XYZ123", expression.getChild(0).getName(),
            "expression female socket name is XYZ123");
        assertEquals(
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$AnalogSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogExpressionSocket",
                expression.getChild(0).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(0).isConnected(),
            "expression female socket is not connected");

        assertEquals( "ZH12", expression.getChild(1).getName(),
            "expression female socket name is ZH12");
        assertEquals(
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$AnalogSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogExpressionSocket",
                expression.getChild(1).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(1).isConnected(),
                "expression female socket is not connected");

        assertEquals( "Bj23", expression.getChild(2).getName(),
            "expression female socket name is Bj23");
        assertEquals(
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                expression.getChild(2).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(2).isConnected(),
                "expression female socket is not connected");

        assertEquals( 5, expression.getChildCount(), "expression has 5 female sockets");
    }

    @Test
    public void testCtorAndSetup2() {
        ActionThrottle expression = new ActionThrottle("IQDA321", null);
        assertNotNull( expression, "exists");
        assertEquals( 5, expression.getChildCount(), "expression has 5 female sockets");
        expression.getChild(0).setName("XYZ123");
        expression.setLocoAddressSocketSystemName(null);
        expression.getChild(1).setName("ZH12");
        expression.setLocoSpeedSocketSystemName(null);
        expression.getChild(2).setName("Bj23");
        expression.setLocoDirectionSocketSystemName(null);

        assertEquals( "XYZ123", expression.getChild(0).getName(),
            "expression female socket name is XYZ123");
        assertEquals(
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$AnalogSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogExpressionSocket",
                expression.getChild(0).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(0).isConnected(),
                "expression female socket is not connected");

        assertEquals( "ZH12", expression.getChild(1).getName(),
                "expression female socket name is ZH12");
        assertEquals(
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$AnalogSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogExpressionSocket",
                expression.getChild(1).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(1).isConnected(),
                "expression female socket is not connected");

        assertEquals( "Bj23", expression.getChild(2).getName(),
                "expression female socket name is Bj23");
        assertEquals(
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                expression.getChild(2).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(2).isConnected(),
                "expression female socket is not connected");

        // Setup action. This connects the child actions to this action
        expression.setup();

        assertEquals( "XYZ123", expression.getChild(0).getName(),
                "expression female socket name is XYZ123");
        assertEquals(
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$AnalogSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogExpressionSocket",
                expression.getChild(0).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(0).isConnected(),
                "expression female socket is not connected");

        assertEquals( "ZH12", expression.getChild(1).getName(),
            "expression female socket name is ZH12");
        assertEquals(
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$AnalogSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogExpressionSocket",
                expression.getChild(1).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(1).isConnected(),
                "expression female socket is not connected");

        assertEquals( "Bj23", expression.getChild(2).getName(),
                "expression female socket name is Bj23");
        assertEquals(
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                expression.getChild(2).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(2).isConnected(),
                "expression female socket is not connected");

        assertEquals( 5, expression.getChildCount(), "expression has 5 female sockets");
    }

    @Test
    public void testCtorAndSetup3() {
        AnalogExpressionManager m0 = InstanceManager.getDefault(AnalogExpressionManager.class);
        DigitalExpressionManager m2 = InstanceManager.getDefault(DigitalExpressionManager.class);

        m0.registerExpression(new AnalogExpressionMemory("IQAE52", null));
        m0.registerExpression(new AnalogExpressionMemory("IQAE554", null));
        m2.registerExpression(new ExpressionMemory("IQDE594", null));

        ActionThrottle expression = new ActionThrottle("IQDA321", null);
        assertNotNull( expression, "exists");
        assertEquals( 5, expression.getChildCount(), "expression has 5 female sockets");
        expression.getChild(0).setName("XYZ123");
        expression.setLocoAddressSocketSystemName("IQAE52");
        expression.getChild(1).setName("ZH12");
        expression.setLocoSpeedSocketSystemName("IQAE554");
        expression.getChild(2).setName("Bj23");
        expression.setLocoDirectionSocketSystemName("IQDE594");

        assertEquals( "XYZ123", expression.getChild(0).getName(),
                "expression female socket name is XYZ123");
        assertEquals(
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$AnalogSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogExpressionSocket",
                expression.getChild(0).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(0).isConnected(),
                "expression female socket is not connected");

        assertEquals( "ZH12", expression.getChild(1).getName(),
                "expression female socket name is ZH12");
        assertEquals(
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$AnalogSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogExpressionSocket",
                expression.getChild(1).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(1).isConnected(),
                "expression female socket is not connected");

        assertEquals( "Bj23", expression.getChild(2).getName(),
                "expression female socket name is Bj23");
        assertEquals(
 //               "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                expression.getChild(2).getClass().getName(),
                "expression female socket is of correct class");
        assertFalse( expression.getChild(2).isConnected(),
                "expression female socket is not connected");

        // Setup action. This connects the child actions to this action
        expression.setup();

        assertTrue( expression.getChild(0).isConnected(),
                "expression female socket is connected");
//        assertEquals(
//                childSocket0,
//                expression.getChild(0).getConnectedSocket(), "child is correct bean");
        assertEquals( 5, expression.getChildCount(), "expression has 5 female sockets");

        assertTrue( expression.getChild(1).isConnected(),
                "expression female socket is connected");
//        Assert.assertEquals("child is correct bean",
//                childSocket1,
//                expression.getChild(1).getConnectedSocket());
        assertEquals( 5, expression.getChildCount(), "expression has 5 female sockets");

        assertTrue( expression.getChild(2).isConnected(),
                "expression female socket is connected");
//        Assert.assertEquals("child is correct bean",
//                childSocket2,
//                expression.getChild(2).getConnectedSocket());

        assertEquals( 5, expression.getChildCount(), "expression has 5 female sockets");

        // Try run setup() again. That should not cause any problems.
        expression.setup();

        assertEquals( 5, expression.getChildCount(), "expression has 5 female sockets");
    }

    @Test
    public void testGetChild() {
        assertEquals( 5, actionThrottle.getChildCount(), "getChildCount() returns 5");

        assertNotNull( actionThrottle.getChild(0),
                "getChild(0) returns a non null value");
        assertNotNull( actionThrottle.getChild(1),
                "getChild(1) returns a non null value");
        assertNotNull( actionThrottle.getChild(2),
                "getChild(2) returns a non null value");
        assertNotNull( actionThrottle.getChild(3),
                "getChild(3) returns a non null value");
        assertNotNull( actionThrottle.getChild(4),
                "getChild(4) returns a non null value");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class,
            () -> actionThrottle.getChild(5),
            "Exception is thrown");
        assertEquals( "index has invalid value: 5", ex.getMessage(),
            "Error message is correct");
    }

    @Test
    public void testCategory() {
        assertEquals( LogixNG_Category.ITEM, _base.getCategory(), "Category matches");
    }

    @Test
    public void testShortDescription() {
        assertEquals( "Throttle", _base.getShortDescription(), "String matches");
    }

    @Test
    public void testLongDescription() {
        assertEquals( "Throttle. Don't wait for throttle. Stop loco when switching loco", _base.getLongDescription(), "String matches");
    }

    @Test
    public void testChild() {
        MaleSocket analogExpressionMaleSocket =
                InstanceManager.getDefault(AnalogExpressionManager.class)
                        .registerExpression(new AnalogExpressionConstant("IQAE1", null));
        MaleSocket digitalExpressionMaleSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class)
                        .registerExpression(new ExpressionSensor("IQDE1", null));

        assertEquals( 5, _base.getChildCount(), "Num children is correct");

        // Socket LOCO_ADDRESS_SOCKET is loco address
        assertTrue( _base.getChild(ActionThrottle.LOCO_ADDRESS_SOCKET).isCompatible(analogExpressionMaleSocket),
                "Child LOCO_ADDRESS_SOCKET supports analog male socket");

        // Socket LOCO_SPEED_SOCKET is loco speed
        assertTrue( _base.getChild(ActionThrottle.LOCO_SPEED_SOCKET).isCompatible(analogExpressionMaleSocket),
                "Child LOCO_SPEED_SOCKET supports analog male socket");

        // Socket LOCO_DIRECTION_SOCKET is loco direction
        assertTrue( _base.getChild(ActionThrottle.LOCO_DIRECTION_SOCKET).isCompatible(digitalExpressionMaleSocket),
                "Child LOCO_DIRECTION_SOCKET supports digital male socket");

        // Socket LOCO_DIRECTION_SOCKET is loco direction
        assertTrue( _base.getChild(ActionThrottle.LOCO_FUNCTION_SOCKET).isCompatible(analogExpressionMaleSocket),
                "Child LOCO_FUNCTION_SOCKET supports analog male socket");

        // Socket LOCO_DIRECTION_SOCKET is loco direction
        assertTrue( _base.getChild(ActionThrottle.LOCO_FUNCTION_ONOFF_SOCKET).isCompatible(digitalExpressionMaleSocket),
                "Child LOCO_FUNCTION_STATE_SOCKET supports digital male socket");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class,
            () -> _base.getChild(5),
            "Exception is thrown");
        assertEquals( "index has invalid value: 5", ex.getMessage(), "Error message is correct");
    }

    @Disabled("This method fails too often")
    @ToDo("This method fails too often")
    @Test
    public void testExecute() throws SocketAlreadyConnectedException, JmriException {
        ThrottleManager tm = InstanceManager.getDefault(ThrottleManager.class);

        int locoAddress = 1234;
        int locoAddress2 = 1235;
        assertEquals( 0, tm.getThrottleUsageCount(locoAddress), "Throttle is used 0 times");
        assertEquals( 0, tm.getThrottleUsageCount(locoAddress2), "Throttle is used 0 times");

        logixNG.setEnabled(false);
        ConditionalNG conditionalNG_2 = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG(logixNG, "A second conditionalNG");  // NOI18N
        logixNG.addConditionalNG(conditionalNG_2);
        ActionThrottle actionThrottle2 = new ActionThrottle("IQDA999", null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionThrottle2);
        conditionalNG_2.getChild(0).connect(maleSocket2);

        logixNG.setEnabled(true);
        assertTrue(logixNG.setParentForAllChildren(new ArrayList<>()));

        // Test execute when no children are connected
        actionThrottle2.execute();

        assertNotNull( actionThrottle2.getConditionalNG(), "getConditionalNG() returns not null");

        conditionalNG.unregisterListeners();

        AtomicReference<DccThrottle> myThrottleRef = new AtomicReference<>();

        MyThrottleListener myThrottleListener = new MyThrottleListener(myThrottleRef);

        boolean result = tm.requestThrottle(locoAddress, myThrottleListener);
        assertTrue( result, () -> "loco " + locoAddress + " cannot be aquired");

        assertNotNull( myThrottleRef.get(), "has throttle");

        Memory locoAddressMemory = InstanceManager.getDefault(MemoryManager.class).provide("Loco address memory");
        locoAddressMemory.setValue(locoAddress);
        AnalogExpressionMemory locoAddressExpression = new AnalogExpressionMemory("IQAE111", null);
        locoAddressExpression.getSelectNamedBean().setNamedBean(locoAddressMemory);

        Memory locoSpeedMemory = InstanceManager.getDefault(MemoryManager.class).provide("Loco speed memory");
        locoSpeedMemory.setValue(0);
        AnalogExpressionMemory locoSpeedExpression = new AnalogExpressionMemory("IQAE112", null);
        locoSpeedExpression.getSelectNamedBean().setNamedBean(locoSpeedMemory);

        Sensor locoDirectionSensor = InstanceManager.getDefault(SensorManager.class).provide("Loco direction sensor");
        locoDirectionSensor.setState(Sensor.ACTIVE);
        ExpressionSensor locoDirectionExpression = new ExpressionSensor("IQDE113", null);
        locoDirectionExpression.getSelectNamedBean().setNamedBean(locoDirectionSensor);

        // Set loco address of actionThrottle2
        conditionalNG_2.unregisterListeners();
        MaleSocket locoAddressSocket =
                InstanceManager.getDefault(AnalogExpressionManager.class)
                        .registerExpression(locoAddressExpression);
        actionThrottle2.getChild(ActionThrottle.LOCO_ADDRESS_SOCKET).connect(locoAddressSocket);
        conditionalNG_2.registerListeners();

        // Test execute when loco address socket is connected
        actionThrottle2.execute();
        assertEquals( 0.0, myThrottleRef.get().getSpeedSetting(), 0.0001,
                "loco speed is correct");

        // Set loco address of actionThrottle2
        conditionalNG_2.unregisterListeners();
        MaleSocket locoSpeedSocket =
                InstanceManager.getDefault(AnalogExpressionManager.class)
                        .registerExpression(locoSpeedExpression);
        actionThrottle2.getChild(ActionThrottle.LOCO_SPEED_SOCKET).connect(locoSpeedSocket);
        assertTrue( myThrottleRef.get().getIsForward(), "loco direction is correct");
        conditionalNG_2.registerListeners();

        // Test execute when loco speed socket is connected
        actionThrottle2.execute();

        // Set loco address of actionThrottle2
        conditionalNG_2.unregisterListeners();
        MaleSocket locoDirectionSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class)
                        .registerExpression(locoDirectionExpression);
        actionThrottle2.getChild(ActionThrottle.LOCO_DIRECTION_SOCKET).connect(locoDirectionSocket);
        conditionalNG_2.registerListeners();

        // Test execute when loco direction socket is connected
        actionThrottle2.execute();

        // Set a different speed
        locoSpeedMemory.setValue(0.5);
        // Test execute when loco speed is changed
        actionThrottle2.execute();
        assertEquals( 0.5, myThrottleRef.get().getSpeedSetting(), 0.0001, "loco speed is correct");
        assertTrue( myThrottleRef.get().getIsForward(), "loco direction is correct");

        // Set a different direction
        locoDirectionSensor.setState(Sensor.INACTIVE);
        // Test execute when loco direction is changed
        actionThrottle2.execute();
        assertEquals( 0.5, myThrottleRef.get().getSpeedSetting(), 0.0001, "loco speed is correct");
        assertFalse( myThrottleRef.get().getIsForward(), "loco direction is correct");

        // Test execute when loco address is changed
        actionThrottle2.execute();

        // Test execute when loco address socket is connected
        actionThrottle2.execute();

        // Test execute when loco address is changed
        AtomicReference<DccThrottle> myThrottleRef2 = new AtomicReference<>();
        MyThrottleListener myThrottleListener2 = new MyThrottleListener(myThrottleRef2);
        result = tm.requestThrottle(locoAddress2, myThrottleListener2);
        assertTrue( result, () -> "loco " + locoAddress + " cannot be aquired");
        assertNotNull( myThrottleRef2.get(), "has throttle");
        myThrottleRef2.get().setSpeedSetting(1);
        assertEquals( 0.5, myThrottleRef.get().getSpeedSetting(), 0.0001, "loco speed is correct");
        assertEquals( 1.0, myThrottleRef2.get().getSpeedSetting(), 0.0001, "loco speed is correct");

        // Change loco address
        locoAddressMemory.setValue(locoAddress2);

        // Execute the action
        actionThrottle2.execute();

        assertEquals( 0.0, myThrottleRef.get().getSpeedSetting(), 0.0001, "loco speed is correct");
        assertEquals( 0.5, myThrottleRef2.get().getSpeedSetting(), 0.0001, "loco speed is correct");

        // Test execute when loco address socket is disconnected
        conditionalNG_2.unregisterListeners();
        actionThrottle2.getChild(ActionThrottle.LOCO_ADDRESS_SOCKET).disconnect();
        conditionalNG_2.registerListeners();
        actionThrottle2.execute();

        // This test is not reliable. Why?
//        Assert.assertEquals("Throttle is used 1 times", 1, tm.getThrottleUsageCount(locoAddress2));

        // Test disposeMe(). ActionThrottle does not have a throttle now.
        actionThrottle2.disposeMe();

        // This test is not reliable. Why?
//        Assert.assertEquals("Throttle is used 1 times", 1, tm.getThrottleUsageCount(locoAddress2));
    }

    @Test
    public void testDisposeMe() throws SocketAlreadyConnectedException, JmriException {
        ThrottleManager tm = InstanceManager.getDefault(ThrottleManager.class);

        logixNG.setEnabled(false);
        ConditionalNG conditionalNG_2 = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG(logixNG, "A second conditionalNG");  // NOI18N
        conditionalNG_2.setRunDelayed(false);
        ActionThrottle actionThrottle2 = new ActionThrottle("IQDA999", null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionThrottle2);
        conditionalNG_2.getChild(0).connect(maleSocket2);

        assertTrue( logixNG.setParentForAllChildren(new ArrayList<>()));

        int locoAddress = 1234;
        AtomicReference<DccThrottle> myThrottleRef = new AtomicReference<>();

        MyThrottleListener myThrottleListener = new MyThrottleListener(myThrottleRef);

        boolean result = tm.requestThrottle(locoAddress, myThrottleListener);
        assertTrue( result, () -> "loco " + locoAddress + " cannot be aquired");

        assertNotNull( myThrottleRef.get(), "has throttle");

        Memory locoAddressMemory = InstanceManager.getDefault(MemoryManager.class).provide("Loco address memory");
        locoAddressMemory.setValue(locoAddress);
        AnalogExpressionMemory locoAddressExpression = new AnalogExpressionMemory("IQAE111", null);
        locoAddressExpression.getSelectNamedBean().setNamedBean(locoAddressMemory);

        Memory locoSpeedMemory = InstanceManager.getDefault(MemoryManager.class).provide("Loco speed memory");
        locoSpeedMemory.setValue(0);
        AnalogExpressionMemory locoSpeedExpression = new AnalogExpressionMemory("IQAE112", null);
        locoSpeedExpression.getSelectNamedBean().setNamedBean(locoSpeedMemory);

        Sensor locoDirectionSensor = InstanceManager.getDefault(SensorManager.class).provide("Loco direction sensor");
        locoDirectionSensor.setState(Sensor.ACTIVE);
        ExpressionSensor locoDirectionExpression = new ExpressionSensor("IQDE113", null);
        locoDirectionExpression.getSelectNamedBean().setNamedBean(locoDirectionSensor);

        // Set loco address of actionThrottle2
        MaleSocket locoAddressSocket =
                InstanceManager.getDefault(AnalogExpressionManager.class)
                        .registerExpression(locoAddressExpression);
        actionThrottle2.getChild(ActionThrottle.LOCO_ADDRESS_SOCKET).connect(locoAddressSocket);

        assertEquals( 1, tm.getThrottleUsageCount(locoAddress), "Throttle is used 1 times");

        // Test execute when loco address socket is connected
        conditionalNG_2.execute();
        assertEquals( 0.0, myThrottleRef.get().getSpeedSetting(), 0.0001, "loco speed is correct");

        assertEquals( 2, tm.getThrottleUsageCount(locoAddress), "Throttle is used 2 times");

        // Test disposeMe(). ActionThrottle has a throttle now which must be released.
        actionThrottle2.disposeMe();

        assertEquals( 1, tm.getThrottleUsageCount(locoAddress), "Throttle is used 1 times");
    }

    @Test
    public void testConnectedDisconnected() throws SocketAlreadyConnectedException {
        _baseMaleSocket.setEnabled(false);

        assertEquals( 5, _base.getChildCount(), "Num children is correct");

        MaleSocket analogExpressionMaleSocket =
                InstanceManager.getDefault(AnalogExpressionManager.class)
                        .registerExpression(new AnalogExpressionConstant("IQAE1", null));
        MaleSocket digitalExpressionMaleSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class)
                        .registerExpression(new ExpressionSensor("IQDE1", null));

        actionThrottle.getChild(ActionThrottle.LOCO_ADDRESS_SOCKET).disconnect();
        assertNull( actionThrottle.getLocoAddressSocketSystemName(), "socket name is null");
        actionThrottle.getChild(ActionThrottle.LOCO_ADDRESS_SOCKET).connect(analogExpressionMaleSocket);
        assertEquals( "IQAE1", actionThrottle.getLocoAddressSocketSystemName(), "socket name is correct");
        actionThrottle.getChild(ActionThrottle.LOCO_ADDRESS_SOCKET).disconnect();
        assertNull( actionThrottle.getLocoAddressSocketSystemName(), "socket name is null");

        actionThrottle.getChild(ActionThrottle.LOCO_SPEED_SOCKET).disconnect();
        assertNull( actionThrottle.getLocoAddressSocketSystemName(), "socket name is null");
        actionThrottle.getChild(ActionThrottle.LOCO_SPEED_SOCKET).connect(analogExpressionMaleSocket);
        assertEquals( "IQAE1", actionThrottle.getLocoSpeedSocketSystemName(), "socket name is correct");
        actionThrottle.getChild(ActionThrottle.LOCO_SPEED_SOCKET).disconnect();
        assertNull( actionThrottle.getLocoAddressSocketSystemName(), "socket name is null");

        actionThrottle.getChild(ActionThrottle.LOCO_DIRECTION_SOCKET).disconnect();
        assertNull( actionThrottle.getLocoAddressSocketSystemName(), "socket name is null");
        actionThrottle.getChild(ActionThrottle.LOCO_DIRECTION_SOCKET).connect(digitalExpressionMaleSocket);
        assertEquals( "IQDE1", actionThrottle.getLocoDirectionSocketSystemName(), "socket name is correct");
        actionThrottle.getChild(ActionThrottle.LOCO_DIRECTION_SOCKET).disconnect();
        assertNull( actionThrottle.getLocoAddressSocketSystemName(), "socket name is null");

        FemaleSocket badFemaleSocket = InstanceManager.getDefault(AnalogExpressionManager.class)
                .createFemaleSocket(actionThrottle, actionThrottle, "E1");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class,
            () -> actionThrottle.connected(badFemaleSocket),
            "Exception is thrown");
        assertEquals( "unkown socket", ex.getMessage(), "Error message is correct");

        ex = assertThrows( IllegalArgumentException.class,
            () -> actionThrottle.disconnected(badFemaleSocket),
            "Exception is thrown");
        assertEquals( "unkown socket", ex.getMessage(), "Error message is correct");

    }

    @Test
    public void testToString() {
        ActionThrottle a1 = new ActionThrottle("IQDA321", null);
        assertEquals( "Throttle", a1.getShortDescription(), "strings are equal");
        ActionThrottle a2 = new ActionThrottle("IQDA321", null);
        assertEquals( "Throttle. Don't wait for throttle. Stop loco when switching loco", a2.getLongDescription(), "strings are equal");
    }

    @BeforeEach
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initLogixNGManager();

        _category = LogixNG_Category.ITEM;
        _isExternal = true;

        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        conditionalNG.setEnabled(true);
        conditionalNG.setRunDelayed(false);
        logixNG.addConditionalNG(conditionalNG);
        actionThrottle = new ActionThrottle("IQDA321", null);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionThrottle);
        conditionalNG.getChild(0).connect(maleSocket);
        _base = actionThrottle;
        _baseMaleSocket = maleSocket;

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



    private static class MyThrottleListener implements ThrottleListener {

        private final AtomicReference<DccThrottle> _myThrottleRef;

        MyThrottleListener(AtomicReference<DccThrottle> myThrottleRef) {
            _myThrottleRef = myThrottleRef;
        }

        @Override
        public void notifyThrottleFound(DccThrottle t) {
            _myThrottleRef.set(t);
        }

        @Override
        public void notifyFailedThrottleRequest(LocoAddress address, String reason) {
            fail("loco " + address.getNumber() + " cannot be aquired " + reason);
        }

        @Override
        public void notifyDecisionRequired(LocoAddress address, ThrottleListener.DecisionType question) {
            fail("loco " + address.getNumber() + " cannot be aquired. Decision required. " + question);
        }
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionThrottleTest.class);

}
