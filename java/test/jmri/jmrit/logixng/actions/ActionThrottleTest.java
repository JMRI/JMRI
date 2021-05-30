package jmri.jmrit.logixng.actions;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicReference;

import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.NamedBean;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.ThrottleListener;
import jmri.ThrottleManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.AnalogExpressionConstant;
import jmri.jmrit.logixng.expressions.AnalogExpressionMemory;
import jmri.jmrit.logixng.expressions.ExpressionMemory;
import jmri.jmrit.logixng.expressions.ExpressionSensor;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.ToDo;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test ActionThrottle
 *
 * @author Daniel Bergqvist 2019
 */
public class ActionThrottleTest extends AbstractDigitalActionTestBase {

    LogixNG logixNG;
    ConditionalNG conditionalNG;
    ActionThrottle actionThrottle;

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
                "Throttle ::: Use default%n" +
                "   ?~ Address%n" +
                "      Socket not connected%n" +
                "   ?~ Speed%n" +
                "      Socket not connected%n" +
                "   ? Direction%n" +
                "      Socket not connected%n");
    }

    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         Throttle ::: Use default%n" +
                "            ?~ Address%n" +
                "               Socket not connected%n" +
                "            ?~ Speed%n" +
                "               Socket not connected%n" +
                "            ? Direction%n" +
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
        Assert.assertNotNull("object exists", action2);
        Assert.assertNull("Username matches", action2.getUserName());
        Assert.assertEquals("String matches", "Throttle", action2.getLongDescription());

        action2 = new ActionThrottle("IQDA321", "My throttle");
        Assert.assertNotNull("object exists", action2);
        Assert.assertEquals("Username matches", "My throttle", action2.getUserName());
        Assert.assertEquals("String matches", "Throttle", action2.getLongDescription());

        boolean thrown = false;
        try {
            // Illegal system name
            new ActionThrottle("IQA55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);

        thrown = false;
        try {
            // Illegal system name
            new ActionThrottle("IQA55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
    }

    @Test
    public void testCtorAndSetup1() {
        ActionThrottle expression = new ActionThrottle("IQDA321", null);
        Assert.assertNotNull("exists", expression);
        Assert.assertEquals("expression has 3 female sockets", 3, expression.getChildCount());
        expression.getChild(0).setName("XYZ123");
        expression.setLocoAddressSocketSystemName("IQAE52");
        expression.getChild(1).setName("ZH12");
        expression.setLocoSpeedSocketSystemName("IQAE554");
        expression.getChild(2).setName("Bj23");
        expression.setLocoDirectionSocketSystemName("IQDE594");

        Assert.assertEquals("expression female socket name is XYZ123",
                "XYZ123", expression.getChild(0).getName());
        Assert.assertEquals("expression female socket is of correct class",
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$AnalogSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogExpressionSocket",
                expression.getChild(0).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(0).isConnected());

        Assert.assertEquals("expression female socket name is ZH12",
                "ZH12", expression.getChild(1).getName());
        Assert.assertEquals("expression female socket is of correct class",
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$AnalogSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogExpressionSocket",
                expression.getChild(1).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(1).isConnected());

        Assert.assertEquals("expression female socket name is Bj23",
                "Bj23", expression.getChild(2).getName());
        Assert.assertEquals("expression female socket is of correct class",
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                expression.getChild(2).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(2).isConnected());

        // Setup action. This connects the child actions to this action
        expression.setup();

        jmri.util.JUnitAppender.assertMessage("cannot load analog expression IQAE52");
        jmri.util.JUnitAppender.assertMessage("cannot load analog expression IQAE554");
        jmri.util.JUnitAppender.assertMessage("cannot load digital expression IQDE594");

        Assert.assertEquals("expression female socket name is XYZ123",
                "XYZ123", expression.getChild(0).getName());
        Assert.assertEquals("expression female socket is of correct class",
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$AnalogSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogExpressionSocket",
                expression.getChild(0).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(0).isConnected());

        Assert.assertEquals("expression female socket name is ZH12",
                "ZH12", expression.getChild(1).getName());
        Assert.assertEquals("expression female socket is of correct class",
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$AnalogSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogExpressionSocket",
                expression.getChild(1).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(1).isConnected());

        Assert.assertEquals("expression female socket name is Bj23",
                "Bj23", expression.getChild(2).getName());
        Assert.assertEquals("expression female socket is of correct class",
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                expression.getChild(2).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(2).isConnected());

        Assert.assertEquals("expression has 3 female sockets", 3, expression.getChildCount());
    }

    @Test
    public void testCtorAndSetup2() {
        ActionThrottle expression = new ActionThrottle("IQDA321", null);
        Assert.assertNotNull("exists", expression);
        Assert.assertEquals("expression has 3 female sockets", 3, expression.getChildCount());
        expression.getChild(0).setName("XYZ123");
        expression.setLocoAddressSocketSystemName(null);
        expression.getChild(1).setName("ZH12");
        expression.setLocoSpeedSocketSystemName(null);
        expression.getChild(2).setName("Bj23");
        expression.setLocoDirectionSocketSystemName(null);

        Assert.assertEquals("expression female socket name is XYZ123",
                "XYZ123", expression.getChild(0).getName());
        Assert.assertEquals("expression female socket is of correct class",
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$AnalogSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogExpressionSocket",
                expression.getChild(0).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(0).isConnected());

        Assert.assertEquals("expression female socket name is ZH12",
                "ZH12", expression.getChild(1).getName());
        Assert.assertEquals("expression female socket is of correct class",
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$AnalogSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogExpressionSocket",
                expression.getChild(1).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(1).isConnected());

        Assert.assertEquals("expression female socket name is Bj23",
                "Bj23", expression.getChild(2).getName());
        Assert.assertEquals("expression female socket is of correct class",
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                expression.getChild(2).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(2).isConnected());

        // Setup action. This connects the child actions to this action
        expression.setup();

        Assert.assertEquals("expression female socket name is XYZ123",
                "XYZ123", expression.getChild(0).getName());
        Assert.assertEquals("expression female socket is of correct class",
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$AnalogSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogExpressionSocket",
                expression.getChild(0).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(0).isConnected());

        Assert.assertEquals("expression female socket name is ZH12",
                "ZH12", expression.getChild(1).getName());
        Assert.assertEquals("expression female socket is of correct class",
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$AnalogSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogExpressionSocket",
                expression.getChild(1).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(1).isConnected());

        Assert.assertEquals("expression female socket name is Bj23",
                "Bj23", expression.getChild(2).getName());
        Assert.assertEquals("expression female socket is of correct class",
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                expression.getChild(2).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(2).isConnected());

        Assert.assertEquals("expression has 3 female sockets", 3, expression.getChildCount());
    }

    @Test
    public void testCtorAndSetup3() {
        AnalogExpressionManager m0 = InstanceManager.getDefault(AnalogExpressionManager.class);
        DigitalExpressionManager m2 = InstanceManager.getDefault(DigitalExpressionManager.class);

        m0.registerExpression(new AnalogExpressionMemory("IQAE52", null));
        m0.registerExpression(new AnalogExpressionMemory("IQAE554", null));
        m2.registerExpression(new ExpressionMemory("IQDE594", null));

        ActionThrottle expression = new ActionThrottle("IQDA321", null);
        Assert.assertNotNull("exists", expression);
        Assert.assertEquals("expression has 3 female sockets", 3, expression.getChildCount());
        expression.getChild(0).setName("XYZ123");
        expression.setLocoAddressSocketSystemName("IQAE52");
        expression.getChild(1).setName("ZH12");
        expression.setLocoSpeedSocketSystemName("IQAE554");
        expression.getChild(2).setName("Bj23");
        expression.setLocoDirectionSocketSystemName("IQDE594");

        Assert.assertEquals("expression female socket name is XYZ123",
                "XYZ123", expression.getChild(0).getName());
        Assert.assertEquals("expression female socket is of correct class",
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$AnalogSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogExpressionSocket",
                expression.getChild(0).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(0).isConnected());

        Assert.assertEquals("expression female socket name is ZH12",
                "ZH12", expression.getChild(1).getName());
        Assert.assertEquals("expression female socket is of correct class",
//                "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$AnalogSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogExpressionSocket",
                expression.getChild(1).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(1).isConnected());

        Assert.assertEquals("expression female socket name is Bj23",
                "Bj23", expression.getChild(2).getName());
        Assert.assertEquals("expression female socket is of correct class",
 //               "jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket$DigitalSocket",
                "jmri.jmrit.logixng.implementation.DefaultFemaleDigitalExpressionSocket",
                expression.getChild(2).getClass().getName());
        Assert.assertFalse("expression female socket is not connected",
                expression.getChild(2).isConnected());

        // Setup action. This connects the child actions to this action
        expression.setup();

        Assert.assertTrue("expression female socket is connected",
                expression.getChild(0).isConnected());
//        Assert.assertEquals("child is correct bean",
//                childSocket0,
//                expression.getChild(0).getConnectedSocket());
        Assert.assertEquals("expression has 3 female sockets", 3, expression.getChildCount());

        Assert.assertTrue("expression female socket is connected",
                expression.getChild(1).isConnected());
//        Assert.assertEquals("child is correct bean",
//                childSocket1,
//                expression.getChild(1).getConnectedSocket());
        Assert.assertEquals("expression has 3 female sockets", 3, expression.getChildCount());

        Assert.assertTrue("expression female socket is connected",
                expression.getChild(2).isConnected());
//        Assert.assertEquals("child is correct bean",
//                childSocket2,
//                expression.getChild(2).getConnectedSocket());

        Assert.assertEquals("expression has 3 female sockets", 3, expression.getChildCount());

        // Try run setup() again. That should not cause any problems.
        expression.setup();

        Assert.assertEquals("expression has 3 female sockets", 3, expression.getChildCount());
    }

    @Test
    public void testGetChild() {
        Assert.assertTrue("getChildCount() returns 3", 3 == actionThrottle.getChildCount());

        Assert.assertNotNull("getChild(0) returns a non null value",
                actionThrottle.getChild(0));
        Assert.assertNotNull("getChild(1) returns a non null value",
                actionThrottle.getChild(1));
        Assert.assertNotNull("getChild(2) returns a non null value",
                actionThrottle.getChild(2));

        boolean hasThrown = false;
        try {
            actionThrottle.getChild(3);
        } catch (IllegalArgumentException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "index has invalid value: 3", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
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
        Assert.assertEquals("String matches", "Throttle", _base.getShortDescription());
    }

    @Test
    public void testLongDescription() {
        Assert.assertEquals("String matches", "Throttle", _base.getLongDescription());
    }

    @Test
    public void testChild() {
        MaleSocket analogExpressionMaleSocket =
                InstanceManager.getDefault(AnalogExpressionManager.class)
                        .registerExpression(new AnalogExpressionConstant("IQAE1", null));
        MaleSocket digitalExpressionMaleSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class)
                        .registerExpression(new ExpressionSensor("IQDE1", null));

        Assert.assertEquals("Num children is correct", 3, _base.getChildCount());

        // Socket LOCO_ADDRESS_SOCKET is loco address
        Assert.assertTrue("Child LOCO_ADDRESS_SOCKET supports analog male socket",
                _base.getChild(ActionThrottle.LOCO_ADDRESS_SOCKET).isCompatible(analogExpressionMaleSocket));

        // Socket LOCO_SPEED_SOCKET is loco speed
        Assert.assertTrue("Child LOCO_SPEED_SOCKET supports analog male socket",
                _base.getChild(ActionThrottle.LOCO_SPEED_SOCKET).isCompatible(analogExpressionMaleSocket));

        // Socket LOCO_DIRECTION_SOCKET is loco direction
        Assert.assertTrue("Child LOCO_DIRECTION_SOCKET supports digital male socket",
                _base.getChild(ActionThrottle.LOCO_DIRECTION_SOCKET).isCompatible(digitalExpressionMaleSocket));

        boolean hasThrown = false;
        try {
            _base.getChild(3);
        } catch (IllegalArgumentException ex) {
            hasThrown = true;
            Assert.assertTrue("Error message is correct", "index has invalid value: 3".equals(ex.getMessage()));
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }

    @Ignore
    @ToDo("This method fails too often")
    @Test
    public void testExecute() throws Exception {
        ThrottleManager tm = InstanceManager.getDefault(ThrottleManager.class);

        int locoAddress = 1234;
        int locoAddress2 = 1235;
        Assert.assertEquals("Throttle is used 0 times", 0, tm.getThrottleUsageCount(locoAddress));
        Assert.assertEquals("Throttle is used 0 times", 0, tm.getThrottleUsageCount(locoAddress2));

        logixNG.setEnabled(false);
        ConditionalNG conditionalNG_2 = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG(logixNG, "A second conditionalNG");  // NOI18N
        logixNG.addConditionalNG(conditionalNG_2);
        ActionThrottle actionThrottle2 = new ActionThrottle("IQDA999", null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionThrottle2);
        conditionalNG_2.getChild(0).connect(maleSocket2);

        logixNG.setEnabled(true);
        if (! logixNG.setParentForAllChildren(new ArrayList<>())) throw new RuntimeException();

        // Test execute when no children are connected
        actionThrottle2.execute();

        Assert.assertNotNull("getConditionalNG() returns not null", actionThrottle2.getConditionalNG());

        conditionalNG.unregisterListeners();

        AtomicReference<DccThrottle> myThrottleRef = new AtomicReference<>();

        MyThrottleListener myThrottleListener = new MyThrottleListener(myThrottleRef);

        boolean result = tm.requestThrottle(locoAddress, myThrottleListener);

        if (!result) {
            log.error("loco {} cannot be aquired", locoAddress);
        }

        Assert.assertNotNull("has throttle", myThrottleRef.get());

        Memory locoAddressMemory = InstanceManager.getDefault(MemoryManager.class).provide("Loco address memory");
        locoAddressMemory.setValue(locoAddress);
        AnalogExpressionMemory locoAddressExpression = new AnalogExpressionMemory("IQAE111", null);
        locoAddressExpression.setMemory(locoAddressMemory);

        Memory locoSpeedMemory = InstanceManager.getDefault(MemoryManager.class).provide("Loco speed memory");
        locoSpeedMemory.setValue(0);
        AnalogExpressionMemory locoSpeedExpression = new AnalogExpressionMemory("IQAE112", null);
        locoSpeedExpression.setMemory(locoSpeedMemory);

        Sensor locoDirectionSensor = InstanceManager.getDefault(SensorManager.class).provide("Loco direction sensor");
        locoDirectionSensor.setState(Sensor.ACTIVE);
        ExpressionSensor locoDirectionExpression = new ExpressionSensor("IQDE113", null);
        locoDirectionExpression.setSensor(locoDirectionSensor);

        // Set loco address of actionThrottle2
        conditionalNG_2.unregisterListeners();
        MaleSocket locoAddressSocket =
                InstanceManager.getDefault(AnalogExpressionManager.class)
                        .registerExpression(locoAddressExpression);
        actionThrottle2.getChild(ActionThrottle.LOCO_ADDRESS_SOCKET).connect(locoAddressSocket);
        conditionalNG_2.registerListeners();

        // Test execute when loco address socket is connected
        actionThrottle2.execute();
        Assert.assertEquals("loco speed is correct", 0.0, myThrottleRef.get().getSpeedSetting(), 0.0001);

        // Set loco address of actionThrottle2
        conditionalNG_2.unregisterListeners();
        MaleSocket locoSpeedSocket =
                InstanceManager.getDefault(AnalogExpressionManager.class)
                        .registerExpression(locoSpeedExpression);
        actionThrottle2.getChild(ActionThrottle.LOCO_SPEED_SOCKET).connect(locoSpeedSocket);
        Assert.assertTrue("loco direction is correct", myThrottleRef.get().getIsForward());
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
        Assert.assertEquals("loco speed is correct", 0.5, myThrottleRef.get().getSpeedSetting(), 0.0001);
        Assert.assertTrue("loco direction is correct", myThrottleRef.get().getIsForward());

        // Set a different direction
        locoDirectionSensor.setState(Sensor.INACTIVE);
        // Test execute when loco direction is changed
        actionThrottle2.execute();
        Assert.assertEquals("loco speed is correct", 0.5, myThrottleRef.get().getSpeedSetting(), 0.0001);
        Assert.assertFalse("loco direction is correct", myThrottleRef.get().getIsForward());

        // Test execute when loco address is changed
        actionThrottle2.execute();

        // Test execute when loco address socket is connected
        actionThrottle2.execute();

        // Test execute when loco address is changed
        AtomicReference<DccThrottle> myThrottleRef2 = new AtomicReference<>();
        MyThrottleListener myThrottleListener2 = new MyThrottleListener(myThrottleRef2);
        result = tm.requestThrottle(locoAddress2, myThrottleListener2);
        if (!result) {
            log.error("loco {} cannot be aquired", locoAddress);
        }
        Assert.assertNotNull("has throttle", myThrottleRef2.get());
        myThrottleRef2.get().setSpeedSetting(1);
        Assert.assertEquals("loco speed is correct", 0.5, myThrottleRef.get().getSpeedSetting(), 0.0001);
        Assert.assertEquals("loco speed is correct", 1.0, myThrottleRef2.get().getSpeedSetting(), 0.0001);

        // Change loco address
        locoAddressMemory.setValue(locoAddress2);

        // Execute the action
        actionThrottle2.execute();

        Assert.assertEquals("loco speed is correct", 0.0, myThrottleRef.get().getSpeedSetting(), 0.0001);
        Assert.assertEquals("loco speed is correct", 0.5, myThrottleRef2.get().getSpeedSetting(), 0.0001);

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
    public void testDisposeMe() throws Exception {
        ThrottleManager tm = InstanceManager.getDefault(ThrottleManager.class);

        logixNG.setEnabled(false);
        ConditionalNG conditionalNG_2 = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG(logixNG, "A second conditionalNG");  // NOI18N
        conditionalNG_2.setRunDelayed(false);
        ActionThrottle actionThrottle2 = new ActionThrottle("IQDA999", null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionThrottle2);
        conditionalNG_2.getChild(0).connect(maleSocket2);

        if (! logixNG.setParentForAllChildren(new ArrayList<>())) throw new RuntimeException();

        int locoAddress = 1234;
        AtomicReference<DccThrottle> myThrottleRef = new AtomicReference<>();

        MyThrottleListener myThrottleListener = new MyThrottleListener(myThrottleRef);

        boolean result = tm.requestThrottle(locoAddress, myThrottleListener);

        if (!result) {
            log.error("loco {} cannot be aquired", locoAddress);
        }

        Assert.assertNotNull("has throttle", myThrottleRef.get());

        Memory locoAddressMemory = InstanceManager.getDefault(MemoryManager.class).provide("Loco address memory");
        locoAddressMemory.setValue(locoAddress);
        AnalogExpressionMemory locoAddressExpression = new AnalogExpressionMemory("IQAE111", null);
        locoAddressExpression.setMemory(locoAddressMemory);

        Memory locoSpeedMemory = InstanceManager.getDefault(MemoryManager.class).provide("Loco speed memory");
        locoSpeedMemory.setValue(0);
        AnalogExpressionMemory locoSpeedExpression = new AnalogExpressionMemory("IQAE112", null);
        locoSpeedExpression.setMemory(locoSpeedMemory);

        Sensor locoDirectionSensor = InstanceManager.getDefault(SensorManager.class).provide("Loco direction sensor");
        locoDirectionSensor.setState(Sensor.ACTIVE);
        ExpressionSensor locoDirectionExpression = new ExpressionSensor("IQDE113", null);
        locoDirectionExpression.setSensor(locoDirectionSensor);

        // Set loco address of actionThrottle2
        MaleSocket locoAddressSocket =
                InstanceManager.getDefault(AnalogExpressionManager.class)
                        .registerExpression(locoAddressExpression);
        actionThrottle2.getChild(ActionThrottle.LOCO_ADDRESS_SOCKET).connect(locoAddressSocket);

        Assert.assertEquals("Throttle is used 1 times", 1, tm.getThrottleUsageCount(locoAddress));

        // Test execute when loco address socket is connected
        conditionalNG_2.execute();
        Assert.assertEquals("loco speed is correct", 0.0, myThrottleRef.get().getSpeedSetting(), 0.0001);

        Assert.assertEquals("Throttle is used 2 times", 2, tm.getThrottleUsageCount(locoAddress));

        // Test disposeMe(). ActionThrottle has a throttle now which must be released.
        actionThrottle2.disposeMe();

        Assert.assertEquals("Throttle is used 1 times", 1, tm.getThrottleUsageCount(locoAddress));
    }

    @Test
    public void testConnectedDisconnected() throws SocketAlreadyConnectedException {
        _baseMaleSocket.setEnabled(false);

        Assert.assertEquals("Num children is correct", 3, _base.getChildCount());

        MaleSocket analogExpressionMaleSocket =
                InstanceManager.getDefault(AnalogExpressionManager.class)
                        .registerExpression(new AnalogExpressionConstant("IQAE1", null));
        MaleSocket digitalExpressionMaleSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class)
                        .registerExpression(new ExpressionSensor("IQDE1", null));

        actionThrottle.getChild(ActionThrottle.LOCO_ADDRESS_SOCKET).disconnect();
        Assert.assertNull("socket name is null", actionThrottle.getLocoAddressSocketSystemName());
        actionThrottle.getChild(ActionThrottle.LOCO_ADDRESS_SOCKET).connect(analogExpressionMaleSocket);
        Assert.assertEquals("socket name is correct", "IQAE1", actionThrottle.getLocoAddressSocketSystemName());
        actionThrottle.getChild(ActionThrottle.LOCO_ADDRESS_SOCKET).disconnect();
        Assert.assertNull("socket name is null", actionThrottle.getLocoAddressSocketSystemName());

        actionThrottle.getChild(ActionThrottle.LOCO_SPEED_SOCKET).disconnect();
        Assert.assertNull("socket name is null", actionThrottle.getLocoAddressSocketSystemName());
        actionThrottle.getChild(ActionThrottle.LOCO_SPEED_SOCKET).connect(analogExpressionMaleSocket);
        Assert.assertEquals("socket name is correct", "IQAE1", actionThrottle.getLocoSpeedSocketSystemName());
        actionThrottle.getChild(ActionThrottle.LOCO_SPEED_SOCKET).disconnect();
        Assert.assertNull("socket name is null", actionThrottle.getLocoAddressSocketSystemName());

        actionThrottle.getChild(ActionThrottle.LOCO_DIRECTION_SOCKET).disconnect();
        Assert.assertNull("socket name is null", actionThrottle.getLocoAddressSocketSystemName());
        actionThrottle.getChild(ActionThrottle.LOCO_DIRECTION_SOCKET).connect(digitalExpressionMaleSocket);
        Assert.assertEquals("socket name is correct", "IQDE1", actionThrottle.getLocoDirectionSocketSystemName());
        actionThrottle.getChild(ActionThrottle.LOCO_DIRECTION_SOCKET).disconnect();
        Assert.assertNull("socket name is null", actionThrottle.getLocoAddressSocketSystemName());

        FemaleSocket badFemaleSocket = InstanceManager.getDefault(AnalogExpressionManager.class)
                .createFemaleSocket(actionThrottle, actionThrottle, "E1");

        boolean hasThrown = false;
        try {
            actionThrottle.connected(badFemaleSocket);
        } catch (IllegalArgumentException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "unkown socket", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);

        hasThrown = false;
        try {
            actionThrottle.disconnected(badFemaleSocket);
        } catch (IllegalArgumentException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "unkown socket", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }

    @Test
    public void testToString() {
        ActionThrottle a1 = new ActionThrottle("IQDA321", null);
        Assert.assertEquals("strings are equal", "Throttle", a1.getShortDescription());
        ActionThrottle a2 = new ActionThrottle("IQDA321", null);
        Assert.assertEquals("strings are equal", "Throttle", a2.getLongDescription());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initLogixNGManager();

        _category = Category.ITEM;
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

        if (! logixNG.setParentForAllChildren(new ArrayList<>())) throw new RuntimeException();
        logixNG.setEnabled(true);
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }



    private class MyThrottleListener implements ThrottleListener {

        private final AtomicReference<DccThrottle> _myThrottleRef;

        public MyThrottleListener(AtomicReference<DccThrottle> myThrottleRef) {
            _myThrottleRef = myThrottleRef;
        }

        @Override
        public void notifyThrottleFound(DccThrottle t) {
            _myThrottleRef.set(t);
        }

        @Override
        public void notifyFailedThrottleRequest(LocoAddress address, String reason) {
            log.error("loco {} cannot be aquired", address.getNumber());
        }

        @Override
        public void notifyDecisionRequired(LocoAddress address, ThrottleListener.DecisionType question) {
            log.error("Loco {} cannot be aquired. Decision required.", address.getNumber());
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionThrottleTest.class);

}
