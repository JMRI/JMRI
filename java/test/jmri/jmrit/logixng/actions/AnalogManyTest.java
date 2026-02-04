package jmri.jmrit.logixng.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test Many
 *
 * @author Daniel Bergqvist 2018
 */
public class AnalogManyTest extends AbstractAnalogActionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;

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
                "Many ::: Use default%n" +
                "   !~ A1%n" +
                "      Socket not connected%n");
    }

    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         Read analog E and set analog A ::: Use default%n" +
                "            ?~ E%n" +
                "               Socket not connected%n" +
                "            !~ A%n" +
                "               Many ::: Use default%n" +
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
        assertNotNull( action, "exists");
        assertEquals( 1, action.getChildCount(), "action has one female socket");
        assertEquals( "A1", action.getChild(0).getName(), "action female socket name is A1");
        assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogActionSocket",
                action.getChild(0).getClass().getName(),
                "action female socket is of correct class");
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
        assertNotNull( action, "exists");
        assertEquals( 5, action.getChildCount(), "action has 5 female sockets");

        for (int i=0; i < 5; i++) {
            Map.Entry<String,String> entry = actionSystemNames.get(i);
            assertEquals( entry.getKey(), action.getChild(i).getName(),
                    "action female socket name is "+entry.getKey());
            assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogActionSocket",
                    action.getChild(i).getClass().getName(),
                    "action female socket is of correct class");
            assertFalse( action.getChild(i).isConnected(),
                    "action female socket is not connected");
        }

        // Setup action. This connects the child actions to this action
        action.setup();

        jmri.util.JUnitAppender.assertMessage("cannot load analog action IQAA61232");

        for (int i=0; i < 5; i++) {
            Map.Entry<String,String> entry = actionSystemNames.get(i);
            assertEquals( entry.getKey(), action.getChild(i).getName(),
                    "action female socket name is "+entry.getKey());

            if (maleSockets.get(i) != null) {
                assertTrue( action.getChild(i).isConnected(),
                        "action female socket is connected");
//                assertEquals( maleSockets.get(i),
//                        action.getChild(i).getConnectedSocket(), "child is correct bean");
            } else {
                assertFalse( action.getChild(i).isConnected(),
                        "action female socket is not connected");
            }
        }

        assertEquals( 5, action.getChildCount(), "action has 5 female sockets");
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
        assertNotNull( action, "exists");
        assertEquals( 5, action.getChildCount(), "action has 5 female sockets");

        for (int i=0; i < 5; i++) {
            Map.Entry<String,String> entry = actionSystemNames.get(i);
            assertEquals( entry.getKey(), action.getChild(i).getName(),
                    "action female socket name is "+entry.getKey());
            assertEquals( "jmri.jmrit.logixng.implementation.DefaultFemaleAnalogActionSocket",
                    action.getChild(i).getClass().getName(),
                    "action female socket is of correct class");
            assertFalse( action.getChild(i).isConnected(),
                    "action female socket is not connected");
        }

        // Setup action. This connects the child actions to this action
        action.setup();

        for (int i=0; i < 5; i++) {
            Map.Entry<String,String> entry = actionSystemNames.get(i);
            assertEquals( entry.getKey(), action.getChild(i).getName(),
                    "action female socket name is "+entry.getKey());

            if (maleSockets.get(i) != null) {
                assertTrue( action.getChild(i).isConnected(),
                        "action female socket is connected");
//                assertEquals( maleSockets.get(i),
//                        action.getChild(i).getConnectedSocket(), "child is correct bean");
            } else {
                assertFalse( action.getChild(i).isConnected(),
                        "action female socket is not connected");
            }
        }

        // Since all the sockets are connected, a new socket must have been created.
        assertEquals( 6, action.getChildCount(), "action has 6 female sockets");

        // Try run setup() again. That should not cause any problems.
        action.setup();

        assertEquals( 6, action.getChildCount(), "action has 6 female sockets");
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

        InvocationTargetException e = assertThrows( InvocationTargetException.class, () ->
            method.invoke(action, new Object[]{null}), "Exception thrown");
        RuntimeException ex = assertInstanceOf( RuntimeException.class, e.getCause());
        assertEquals( "action system names cannot be set more than once",
                ex.getMessage(), "Exception message is correct");
    }

    @Test
    public void testGetChild() throws SocketAlreadyConnectedException {
        AnalogMany action2 = new AnalogMany("IQAA321", null);

        for (int i=0; i < 3; i++) {
            assertEquals( i+1, action2.getChildCount(), "getChildCount() returns "+i);

            assertNotNull( action2.getChild(0),
                    "getChild(0) returns a non null value");

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
        assertSame( LogixNG_Category.COMMON, _base.getCategory(), "Category matches");
    }

    // Test the methods connected(FemaleSocket) and getActionSystemName(int)
    @Test
    public void testConnected_getActionSystemName() throws SocketAlreadyConnectedException {
        AnalogMany action = new AnalogMany("IQAA121", null);

        AnalogActionMemory analogActionMemory = new AnalogActionMemory("IQAA122", null);
        MaleSocket maleSAMSocket =
                InstanceManager.getDefault(AnalogActionManager.class).registerAction(analogActionMemory);

        assertEquals( 1, action.getChildCount(), "Num children is correct");

        // Test connect and disconnect
        action.getChild(0).connect(maleSAMSocket);
        assertEquals( 2, action.getChildCount(), "Num children is correct");
        assertEquals( "IQAA122", action.getActionSystemName(0), "getActionSystemName(0) is correct");
        assertNull( action.getActionSystemName(1), "getActionSystemName(1) is null");
        action.getChild(0).disconnect();
        assertEquals( 2, action.getChildCount(), "Num children is correct");
        assertNull( action.getActionSystemName(0), "getActionSystemName(0) is null");
        assertNull( action.getActionSystemName(1), "getActionSystemName(1) is null");

        action.getChild(1).connect(maleSAMSocket);
        assertEquals( 2, action.getChildCount(), "Num children is correct");
        assertNull( action.getActionSystemName(0), "getActionSystemName(0) is null");
        assertEquals( "IQAA122", action.getActionSystemName(1), "getActionSystemName(1) is correct");
        action.getChild(0).disconnect();    // Test removing child with the wrong index.
        assertEquals( 2, action.getChildCount(), "Num children is correct");
        assertNull( action.getActionSystemName(0), "getActionSystemName(0) is null");
        assertEquals( "IQAA122", action.getActionSystemName(1), "getActionSystemName(1) is correct");
        action.getChild(1).disconnect();
        assertEquals( 2, action.getChildCount(), "Num children is correct");
        assertNull( action.getActionSystemName(0), "getActionSystemName(0) is null");
        assertNull( action.getActionSystemName(1), "getActionSystemName(1) is null");
    }

    @Test
    public void testDescription() {
        AnalogMany action = new AnalogMany("IQAA121", null);
        assertEquals( "Many", action.getShortDescription(), "Short description");
        assertEquals( "Many", action.getLongDescription(), "Long description");
    }

    @BeforeEach
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();

        _category = LogixNG_Category.COMMON;
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
