package jmri.jmrit.logixng.digital.actions;

import java.util.concurrent.atomic.AtomicReference;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.ThrottleListener;
import jmri.ThrottleManager;
import jmri.jmrit.logixng.AnalogExpressionManager;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.analog.expressions.AnalogExpressionConstant;
import jmri.jmrit.logixng.analog.expressions.AnalogExpressionMemory;
import jmri.jmrit.logixng.digital.expressions.ExpressionSensor;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test ActionTimer
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
    public String getExpectedPrintedTree() {
        return String.format(
                "Throttle%n" +
                "   ?~ E1%n" +
                "      Socket not connected%n" +
                "   ?~ E2%n" +
                "      Socket not connected%n" +
                "   ? E3%n" +
                "      Socket not connected%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! %n" +
                "         Throttle%n" +
                "            ?~ E1%n" +
                "               Socket not connected%n" +
                "            ?~ E2%n" +
                "               Socket not connected%n" +
                "            ? E3%n" +
                "               Socket not connected%n");
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
        
        // Test template
        action2 = (ActionThrottle)_base.getNewObjectBasedOnTemplate();
        Assert.assertNotNull("object exists", action2);
        Assert.assertNull("Username is null", action2.getUserName());
//        Assert.assertTrue("Username matches", "My throttle".equals(expression2.getUserName()));
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
        Assert.assertTrue("Child LOCO_DIRECTION_SOCKET supports analog male socket",
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
    
    @Test
    public void testExecute() throws SocketAlreadyConnectedException {
//        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        ConditionalNG conditionalNG_2 = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A second conditionalNG");  // NOI18N
        logixNG.addConditionalNG(conditionalNG_2);
        ActionThrottle actionThrottle2 = new ActionThrottle("IQDA999", null);
        MaleSocket maleSocket2 =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionThrottle2);
        conditionalNG_2.getChild(0).connect(maleSocket2);
        
        // Test execute when no children are connected
        actionThrottle2.execute();
        
        MyThrottleListener myThrottleListener =  new MyThrottleListener();
        
        int locoAddress = 1234;
        AtomicReference<DccThrottle> myThrottleRef = myThrottleListener.myThrottleRef;
        
        boolean result = InstanceManager.getDefault(ThrottleManager.class)
                .requestThrottle(locoAddress, myThrottleListener);

        if (!result) {
            log.error("loco {} cannot be aquired", locoAddress);
//        } else {
//            _throttleIsRequested = true;
        }
        
        Assert.assertNotNull("has throttle", myThrottleRef.get());
        
        Memory locoAddressMemory = InstanceManager.getDefault(MemoryManager.class).provide("Loco address memory");
        AnalogExpressionMemory locoAddressExpression = new AnalogExpressionMemory("IQAE111", null);
        locoAddressExpression.setMemory(locoAddressMemory);
        
        Memory locoASpeedMemory = InstanceManager.getDefault(MemoryManager.class).provide("Loco speed memory");
        AnalogExpressionMemory locoSpeedExpression = new AnalogExpressionMemory("IQAE112", null);
        locoSpeedExpression.setMemory(locoASpeedMemory);
        
        Sensor locoDirectionSensor = InstanceManager.getDefault(SensorManager.class).provide("Loco direction sensor");
        ExpressionSensor locoDirectionExpression = new ExpressionSensor("IQDE113", null);
        locoDirectionExpression.setSensor(locoDirectionSensor);
        
        // Set loco address of actionThrottle2
        MaleSocket locoAddressSocket =
                InstanceManager.getDefault(AnalogExpressionManager.class)
                        .registerExpression(locoAddressExpression);
        actionThrottle2.getChild(ActionThrottle.LOCO_ADDRESS_SOCKET).connect(locoAddressSocket);
        
        // Set loco address of actionThrottle2
        MaleSocket locoSpeedSocket =
                InstanceManager.getDefault(AnalogExpressionManager.class)
                        .registerExpression(locoSpeedExpression);
        actionThrottle2.getChild(ActionThrottle.LOCO_SPEED_SOCKET).connect(locoSpeedSocket);
        
        // Set loco address of actionThrottle2
        MaleSocket locoDirectionSocket =
                InstanceManager.getDefault(DigitalExpressionManager.class)
                        .registerExpression(locoDirectionExpression);
        actionThrottle2.getChild(ActionThrottle.LOCO_DIRECTION_SOCKET).connect(locoDirectionSocket);
    }
    
    @Test
    public void testConnectedDisconnected() throws SocketAlreadyConnectedException {
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
        JUnitUtil.resetProfileManager();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initDebugThrottleManager();
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        logixNG.addConditionalNG(conditionalNG);
        actionThrottle = new ActionThrottle("IQDA321", null);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionThrottle);
        conditionalNG.getChild(0).connect(maleSocket);
        _base = actionThrottle;
        _baseMaleSocket = maleSocket;
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
    
    
    private class MyThrottleListener implements ThrottleListener {
        
        private final AtomicReference<DccThrottle> myThrottleRef = new AtomicReference<>();
        
        @Override
        @Deprecated
        public void notifyStealThrottleRequired(LocoAddress address) {
            log.error("Loco {} cannot be aquired. Decision required.", address.getNumber());
        }

        @Override
        public void notifyThrottleFound(DccThrottle t) {
            myThrottleRef.set(t);
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
    
    private final static Logger log = LoggerFactory.getLogger(ActionThrottleTest.class);
    
}
