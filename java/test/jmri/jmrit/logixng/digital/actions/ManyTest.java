package jmri.jmrit.logixng.digital.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.jmrit.logixng.*;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test Many
 * 
 * @author Daniel Bergqvist 2018
 */
public class ManyTest extends AbstractDigitalActionTestBase {

    LogixNG logixNG;
    ConditionalNG conditionalNG;
    
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
        Many action = new Many("IQDA999", null);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
        return maleSocket;
    }
    
    @Override
    public String getExpectedPrintedTree() {
        return String.format(
                "Many%n" +
                "   ! A1%n" +
                "      Socket not connected%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! %n" +
                "         Many%n" +
                "            ! A1%n" +
                "               Socket not connected%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new Many(systemName, null);
    }
    
    @Test
    public void testCtor() {
        Many action = new Many("IQDA321", null);
        Assert.assertNotNull("exists", action);
        Assert.assertEquals("action has one female socket", 1, action.getChildCount());
        Assert.assertEquals("action female socket name is A1", "A1", action.getChild(0).getName());
        Assert.assertEquals("action female socket is of correct class",
                "jmri.jmrit.logixng.digital.implementation.DefaultFemaleDigitalActionSocket",
                action.getChild(0).getClass().getName());
    }
    
    @Test
    public void testCtorAndSetup1() {
        DigitalActionManager m = InstanceManager.getDefault(DigitalActionManager.class);
        
        List<MaleSocket> maleSockets = new ArrayList<>();
        maleSockets.add(m.registerAction(new ActionMemory("IQDA52", null)));
        maleSockets.add(null);  // This is null by purpose
        maleSockets.add(m.registerAction(new ActionMemory("IQDA554", null)));
        maleSockets.add(m.registerAction(new ActionMemory("IQDA61232", null)));
        maleSockets.add(m.registerAction(new ActionMemory("IQDA3", null)));
        
        List<Map.Entry<String, String>> actionSystemNames = new ArrayList<>();
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("XYZ123", "IQDA52"));
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("ZH12", null));   // This is null by purpose
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("Hello", "IQDA554"));
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("SomethingElse", "IQDA61232"));
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("Yes123", "IQDA3"));
        
        Many action = new Many("IQDA321", null, actionSystemNames);
        Assert.assertNotNull("exists", action);
        Assert.assertEquals("action has 5 female sockets", 5, action.getChildCount());
        
        for (int i=0; i < 5; i++) {
            Map.Entry<String,String> entry = actionSystemNames.get(i);
            Assert.assertEquals("action female socket name is "+entry.getKey(),
                    entry.getKey(), action.getChild(i).getName());
            Assert.assertEquals("action female socket is of correct class",
                    "jmri.jmrit.logixng.digital.implementation.DefaultFemaleDigitalActionSocket",
                    action.getChild(i).getClass().getName());
            Assert.assertFalse("action female socket is not connected",
                    action.getChild(i).isConnected());
        }
        
        // Setup action. This connects the child actions to this action
        action.setup();
        
        for (int i=0; i < 5; i++) {
            Map.Entry<String,String> entry = actionSystemNames.get(i);
            Assert.assertEquals("action female socket name is "+entry.getKey(),
                    entry.getKey(), action.getChild(i).getName());
            
            if (maleSockets.get(i) != null) {
                Assert.assertTrue("action female socket is connected",
                        action.getChild(i).isConnected());
                Assert.assertEquals("child is correct bean",
                        maleSockets.get(i),
                        action.getChild(i).getConnectedSocket());
            } else {
                Assert.assertFalse("action female socket is not connected",
                        action.getChild(i).isConnected());
            }
        }
        
        Assert.assertEquals("action has 5 female sockets", 5, action.getChildCount());
    }
    
    @Test
    public void testCtorAndSetup2() {
        DigitalActionManager m = InstanceManager.getDefault(DigitalActionManager.class);
        
        List<MaleSocket> maleSockets = new ArrayList<>();
        maleSockets.add(m.registerAction(new ActionMemory("IQDA52", null)));
        maleSockets.add(m.registerAction(new ActionMemory("IQDA99", null)));
        maleSockets.add(m.registerAction(new ActionMemory("IQDA554", null)));
        maleSockets.add(m.registerAction(new ActionMemory("IQDA61232", null)));
        maleSockets.add(m.registerAction(new ActionMemory("IQDA3", null)));
        
        List<Map.Entry<String, String>> actionSystemNames = new ArrayList<>();
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("XYZ123", "IQDA52"));
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("ZH12", "IQDA99"));
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("Hello", "IQDA554"));
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("SomethingElse", "IQDA61232"));
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("Yes123", "IQDA3"));
        
        Many action = new Many("IQDA321", null, actionSystemNames);
        Assert.assertNotNull("exists", action);
        Assert.assertEquals("action has 5 female sockets", 5, action.getChildCount());
        
        for (int i=0; i < 5; i++) {
            Map.Entry<String,String> entry = actionSystemNames.get(i);
            Assert.assertEquals("action female socket name is "+entry.getKey(),
                    entry.getKey(), action.getChild(i).getName());
            Assert.assertEquals("action female socket is of correct class",
                    "jmri.jmrit.logixng.digital.implementation.DefaultFemaleDigitalActionSocket",
                    action.getChild(i).getClass().getName());
            Assert.assertFalse("action female socket is not connected",
                    action.getChild(i).isConnected());
        }
        
        // Setup action. This connects the child actions to this action
        action.setup();
        
        for (int i=0; i < 5; i++) {
            Map.Entry<String,String> entry = actionSystemNames.get(i);
            Assert.assertEquals("action female socket name is "+entry.getKey(),
                    entry.getKey(), action.getChild(i).getName());
            
            if (maleSockets.get(i) != null) {
                Assert.assertTrue("action female socket is connected",
                        action.getChild(i).isConnected());
                Assert.assertEquals("child is correct bean",
                        maleSockets.get(i),
                        action.getChild(i).getConnectedSocket());
            } else {
                Assert.assertFalse("action female socket is not connected",
                        action.getChild(i).isConnected());
            }
        }
        
        Assert.assertEquals("action has 6 female sockets", 6, action.getChildCount());
    }
    
    // Test calling setActionSystemNames() twice
    @Test
    public void testCtorAndSetup3() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException {
        List<Map.Entry<String, String>> actionSystemNames = new ArrayList<>();
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("XYZ123", "IQDA52"));
        
        Many action = new Many("IQDA321", null, actionSystemNames);
        
        java.lang.reflect.Method method =
                action.getClass().getDeclaredMethod("setActionSystemNames", new Class<?>[]{List.class});
        method.setAccessible(true);
        
        boolean hasThrown = false;
        try {
            method.invoke(action, new Object[]{null});
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException) {
                hasThrown = true;
                Assert.assertEquals("Exception message is correct",
                        "action system names cannot be set more than once",
                        e.getCause().getMessage());
            }
        }
        Assert.assertTrue("Exception thrown", hasThrown);
    }
    
    @Test
    public void testGetChild() throws SocketAlreadyConnectedException {
        Many action2 = new Many("IQDA321", null);
        
        for (int i=0; i < 3; i++) {
            Assert.assertTrue("getChildCount() returns "+i, i+1 == action2.getChildCount());
            
            Assert.assertNotNull("getChild(0) returns a non null value",
                    action2.getChild(0));
            
            assertIndexOutOfBoundsException(action2::getChild, i+1, i+1);
            
            // Connect a new child expression
            ActionLight expr = new ActionLight("IQDA"+i, null);
            MaleSocket maleSocket =
                    InstanceManager.getDefault(DigitalActionManager.class).registerAction(expr);
            action2.getChild(i).connect(maleSocket);
        }
    }
    
    @Test
    @Override
    public void testSupportsEnableExecution() throws SocketAlreadyConnectedException {
        DigitalAction da = new Many("IQDA321", null);
        
//        // By default, doesn't support enable execution
        Assert.assertFalse("supportsEnableExecution() returns correct value",
                da.supportsEnableExecution());
/*        
        // Support enable execution if hard lock
        da.setLock(Base.Lock.HARD_WITH_CHILDREN_LOCK);
        Assert.assertTrue("supportsEnableExecution() returns correct value",
                da.supportsEnableExecution());
        
        // Support enable execution if hard lock
        da.setLock(Base.Lock.HARD_LOCK);
        Assert.assertTrue("supportsEnableExecution() returns correct value",
                da.supportsEnableExecution());
        
        // Doesn't support enable execution if any of the children doesn't
        // support it.
        DigitalActionBean da2 = new Many("IQDA322", null);
        MaleSocket socket = InstanceManager.getDefault(DigitalActionManager.class)
                .registerAction(da2);
        da.getChild(0).connect(socket);
        Assert.assertFalse("supportsEnableExecution() returns correct value",
                ((MaleDigitalActionSocket)socket).supportsEnableExecution());
        Assert.assertFalse("supportsEnableExecution() returns correct value",
                da.supportsEnableExecution());
        
        // But support enable execution if all children supports enabled execution
        socket.setLock(Base.Lock.HARD_LOCK);
        Assert.assertTrue("supportsEnableExecution() returns correct value",
                ((MaleDigitalActionSocket)socket).supportsEnableExecution());
        Assert.assertTrue("supportsEnableExecution() returns correct value",
                da.supportsEnableExecution());
*/
    }
    
    @Test
    public void testCategory() {
        Assert.assertTrue("Category matches", Category.COMMON == _base.getCategory());
    }
    
    @Test
    public void testIsExternal() {
        Assert.assertFalse("is external", _base.isExternal());
    }
    
    // Test the methods connected(FemaleSocket) and getActionSystemName(int)
    @Test
    public void testConnected_getActionSystemName() throws SocketAlreadyConnectedException {
        Many action = new Many("IQDA121", null);
        
        ActionMemory actionMemory = new ActionMemory("IQDA122", null);
        MaleSocket maleSAMSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionMemory);
        
        Assert.assertEquals("Num children is correct", 1, action.getChildCount());
        
        // Test connect and disconnect
        action.getChild(0).connect(maleSAMSocket);
        Assert.assertEquals("Num children is correct", 2, action.getChildCount());
        Assert.assertEquals("getActionSystemName(0) is correct", "IQDA122", action.getActionSystemName(0));
        Assert.assertNull("getActionSystemName(1) is null", action.getActionSystemName(1));
        action.getChild(0).disconnect();
        Assert.assertEquals("Num children is correct", 2, action.getChildCount());
        Assert.assertNull("getActionSystemName(0) is null", action.getActionSystemName(0));
        Assert.assertNull("getActionSystemName(1) is null", action.getActionSystemName(1));
        
        action.getChild(1).connect(maleSAMSocket);
        Assert.assertEquals("Num children is correct", 2, action.getChildCount());
        Assert.assertNull("getActionSystemName(0) is null", action.getActionSystemName(0));
        Assert.assertEquals("getActionSystemName(1) is correct", "IQDA122", action.getActionSystemName(1));
        action.getChild(0).disconnect();    // Test removing child with the wrong index.
        Assert.assertEquals("Num children is correct", 2, action.getChildCount());
        Assert.assertNull("getActionSystemName(0) is null", action.getActionSystemName(0));
        Assert.assertEquals("getActionSystemName(1) is correct", "IQDA122", action.getActionSystemName(1));
        action.getChild(1).disconnect();
        Assert.assertEquals("Num children is correct", 2, action.getChildCount());
        Assert.assertNull("getActionSystemName(0) is null", action.getActionSystemName(0));
        Assert.assertNull("getActionSystemName(1) is null", action.getActionSystemName(1));
    }
    
    @Test
    public void testDescription() {
        Many action = new Many("IQDA121", null);
        Assert.assertEquals("Short description", "Many", action.getShortDescription());
        Assert.assertEquals("Long description", "Many", action.getLongDescription());
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        
        _category = Category.COMMON;
        _isExternal = false;
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        conditionalNG.setEnabled(true);
        conditionalNG.setRunOnGUIDelayed(false);
        logixNG.addConditionalNG(conditionalNG);
        Many action = new Many("IQDA321", null);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
        conditionalNG.getChild(0).connect(maleSocket);
        _base = action;
        _baseMaleSocket = maleSocket;
        
        logixNG.setParentForAllChildren();
        logixNG.setEnabled(true);
        logixNG.activateLogixNG();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
