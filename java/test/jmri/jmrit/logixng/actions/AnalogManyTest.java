package jmri.jmrit.logixng.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
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
public class AnalogManyTest extends AbstractAnalogActionTestBase {

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
        AnalogMany action = new AnalogMany("IQAA999", null);
        MaleSocket maleSocket =
                InstanceManager.getDefault(AnalogActionManager.class).registerAction(action);
        return maleSocket;
    }
    
    @Override
    public String getExpectedPrintedTree() {
        return String.format(
                "Many ::: Log error%n" +
                "   !~ A1%n" +
                "      Socket not connected%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         Read analog E and set analog A ::: Log error%n" +
                "            ?~ E%n" +
                "               Socket not connected%n" +
                "            !~ A%n" +
                "               Many ::: Log error%n" +
                "                  !~ A1%n" +
                "                     Socket not connected%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new AnalogMany(systemName, null);
    }
    
    @Override
    public boolean addNewSocket() throws SocketAlreadyConnectedException {
        int count = _base.getChildCount();
        for (int i=0; i < count; i++) {
            if (!_base.getChild(i).isConnected()) {
                _base.getChild(i).connect(getConnectableChild());
            }
        }
        return true;
    }
    
    @Test
    public void testCtor() {
        AnalogMany action = new AnalogMany("IQAA321", null);
        Assert.assertNotNull("exists", action);
        Assert.assertEquals("action has one female socket", 1, action.getChildCount());
        Assert.assertEquals("action female socket name is A1", "A1", action.getChild(0).getName());
        Assert.assertEquals("action female socket is of correct class",
                "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogActionSocket",
                action.getChild(0).getClass().getName());
    }
    
    // Test action when at least one child socket is not connected
    @Test
    public void testCtorAndSetup1() {
        AnalogActionManager m = InstanceManager.getDefault(AnalogActionManager.class);
        
        List<MaleSocket> maleSockets = new ArrayList<>();
        maleSockets.add(m.registerAction(new AnalogActionMemory("IQAA52", null)));
        maleSockets.add(null);  // This is null by purpose
        maleSockets.add(m.registerAction(new AnalogActionMemory("IQAA554", null)));
        maleSockets.add(null);  // This is null by purpose
        maleSockets.add(m.registerAction(new AnalogActionMemory("IQAA3", null)));
        
        List<Map.Entry<String, String>> actionSystemNames = new ArrayList<>();
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("XYZ123", "IQAA52"));
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("ZH12", null));   // This is null by purpose
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("Hello", "IQAA554"));
        // IQAA61232 doesn't exist by purpose
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("SomethingElse", "IQAA61232"));
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("Yes123", "IQAA3"));
        
        AnalogMany action = new AnalogMany("IQAA321", null, actionSystemNames);
        Assert.assertNotNull("exists", action);
        Assert.assertEquals("action has 5 female sockets", 5, action.getChildCount());
        
        for (int i=0; i < 5; i++) {
            Map.Entry<String,String> entry = actionSystemNames.get(i);
            Assert.assertEquals("action female socket name is "+entry.getKey(),
                    entry.getKey(), action.getChild(i).getName());
            Assert.assertEquals("action female socket is of correct class",
                    "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogActionSocket",
                    action.getChild(i).getClass().getName());
            Assert.assertFalse("action female socket is not connected",
                    action.getChild(i).isConnected());
        }
        
        // Setup action. This connects the child actions to this action
        action.setup();
        
        jmri.util.JUnitAppender.assertMessage("cannot load analog action IQAA61232");
        
        for (int i=0; i < 5; i++) {
            Map.Entry<String,String> entry = actionSystemNames.get(i);
            Assert.assertEquals("action female socket name is "+entry.getKey(),
                    entry.getKey(), action.getChild(i).getName());
            
            if (maleSockets.get(i) != null) {
                Assert.assertTrue("action female socket is connected",
                        action.getChild(i).isConnected());
//                Assert.assertEquals("child is correct bean",
//                        maleSockets.get(i),
//                        action.getChild(i).getConnectedSocket());
            } else {
                Assert.assertFalse("action female socket is not connected",
                        action.getChild(i).isConnected());
            }
        }
        
        Assert.assertEquals("action has 5 female sockets", 5, action.getChildCount());
    }
    
    // Test action when at least one child socket is not connected.
    // This should never happen, but test it anyway.
    @Test
    public void testCtorAndSetup2() {
        AnalogActionManager m = InstanceManager.getDefault(AnalogActionManager.class);
        
        List<MaleSocket> maleSockets = new ArrayList<>();
        maleSockets.add(m.registerAction(new AnalogActionMemory("IQAA52", null)));
        maleSockets.add(m.registerAction(new AnalogActionMemory("IQAA99", null)));
        maleSockets.add(m.registerAction(new AnalogActionMemory("IQAA554", null)));
        maleSockets.add(m.registerAction(new AnalogActionMemory("IQAA61232", null)));
        maleSockets.add(m.registerAction(new AnalogActionMemory("IQAA3", null)));
        
        List<Map.Entry<String, String>> actionSystemNames = new ArrayList<>();
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("XYZ123", "IQAA52"));
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("ZH12", "IQAA99"));
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("Hello", "IQAA554"));
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("SomethingElse", "IQAA61232"));
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("Yes123", "IQAA3"));
        
        AnalogMany action = new AnalogMany("IQAA321", null, actionSystemNames);
        Assert.assertNotNull("exists", action);
        Assert.assertEquals("action has 5 female sockets", 5, action.getChildCount());
        
        for (int i=0; i < 5; i++) {
            Map.Entry<String,String> entry = actionSystemNames.get(i);
            Assert.assertEquals("action female socket name is "+entry.getKey(),
                    entry.getKey(), action.getChild(i).getName());
            Assert.assertEquals("action female socket is of correct class",
                    "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogActionSocket",
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
//                Assert.assertEquals("child is correct bean",
//                        maleSockets.get(i),
//                        action.getChild(i).getConnectedSocket());
            } else {
                Assert.assertFalse("action female socket is not connected",
                        action.getChild(i).isConnected());
            }
        }
        
        // Since all the sockets are connected, a new socket must have been created.
        Assert.assertEquals("action has 6 female sockets", 6, action.getChildCount());
        
        // Try run setup() again. That should not cause any problems.
        action.setup();
        
        Assert.assertEquals("action has 6 female sockets", 6, action.getChildCount());
    }
    
    // Test calling setActionSystemNames() twice
    @Test
    public void testCtorAndSetup3() throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException {
        List<Map.Entry<String, String>> actionSystemNames = new ArrayList<>();
        actionSystemNames.add(new java.util.HashMap.SimpleEntry<>("XYZ123", "IQAA52"));
        
        AnalogMany action = new AnalogMany("IQAA321", null, actionSystemNames);
        
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
        AnalogMany action2 = new AnalogMany("IQAA321", null);
        
        for (int i=0; i < 3; i++) {
            Assert.assertTrue("getChildCount() returns "+i, i+1 == action2.getChildCount());
            
            Assert.assertNotNull("getChild(0) returns a non null value",
                    action2.getChild(0));
            
            assertIndexOutOfBoundsException(action2::getChild, i+1, i+1);
            
            // Connect a new child expression
            AnalogActionMemory expr = new AnalogActionMemory("IQAA"+i, null);
            MaleSocket maleSocket =
                    InstanceManager.getDefault(AnalogActionManager.class).registerAction(expr);
            action2.getChild(i).connect(maleSocket);
        }
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
        AnalogMany action = new AnalogMany("IQAA121", null);
        
        AnalogActionMemory analogActionMemory = new AnalogActionMemory("IQAA122", null);
        MaleSocket maleSAMSocket =
                InstanceManager.getDefault(AnalogActionManager.class).registerAction(analogActionMemory);
        
        Assert.assertEquals("Num children is correct", 1, action.getChildCount());
        
        // Test connect and disconnect
        action.getChild(0).connect(maleSAMSocket);
        Assert.assertEquals("Num children is correct", 2, action.getChildCount());
        Assert.assertEquals("getActionSystemName(0) is correct", "IQAA122", action.getActionSystemName(0));
        Assert.assertNull("getActionSystemName(1) is null", action.getActionSystemName(1));
        action.getChild(0).disconnect();
        Assert.assertEquals("Num children is correct", 2, action.getChildCount());
        Assert.assertNull("getActionSystemName(0) is null", action.getActionSystemName(0));
        Assert.assertNull("getActionSystemName(1) is null", action.getActionSystemName(1));
        
        action.getChild(1).connect(maleSAMSocket);
        Assert.assertEquals("Num children is correct", 2, action.getChildCount());
        Assert.assertNull("getActionSystemName(0) is null", action.getActionSystemName(0));
        Assert.assertEquals("getActionSystemName(1) is correct", "IQAA122", action.getActionSystemName(1));
        action.getChild(0).disconnect();    // Test removing child with the wrong index.
        Assert.assertEquals("Num children is correct", 2, action.getChildCount());
        Assert.assertNull("getActionSystemName(0) is null", action.getActionSystemName(0));
        Assert.assertEquals("getActionSystemName(1) is correct", "IQAA122", action.getActionSystemName(1));
        action.getChild(1).disconnect();
        Assert.assertEquals("Num children is correct", 2, action.getChildCount());
        Assert.assertNull("getActionSystemName(0) is null", action.getActionSystemName(0));
        Assert.assertNull("getActionSystemName(1) is null", action.getActionSystemName(1));
    }
    
    @Test
    public void testDescription() {
        AnalogMany action = new AnalogMany("IQAA121", null);
        Assert.assertEquals("Short description", "Many", action.getShortDescription());
        Assert.assertEquals("Long description", "Many", action.getLongDescription());
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
        JUnitUtil.initLogixNGManager();
        
        _category = Category.COMMON;
        _isExternal = false;
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        conditionalNG.setEnabled(true);
        conditionalNG.setRunDelayed(false);
        logixNG.addConditionalNG(conditionalNG);
        
        DoAnalogAction doAnalogAction = new DoAnalogAction("IQDA321", null);
        MaleSocket maleSocketDoAnalogAction =
                InstanceManager.getDefault(DigitalActionManager.class)
                        .registerAction(doAnalogAction);
        conditionalNG.getChild(0).connect(maleSocketDoAnalogAction);
        
        AnalogMany action = new AnalogMany("IQAA321", null);
        MaleSocket maleSocket =
                InstanceManager.getDefault(AnalogActionManager.class).registerAction(action);
        doAnalogAction.getChild(1).connect(maleSocket);
        _base = action;
        _baseMaleSocket = maleSocket;
        
        logixNG.setParentForAllChildren();
        logixNG.setEnabled(true);
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }
    
}
