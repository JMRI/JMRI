package jmri.jmrit.logixng.actions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultConditionalNGScaffold;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test ActionAtomicBoolean
 *
 * @author Daniel Bergqvist 2018
 */
public class ActionAtomicBooleanTest extends AbstractDigitalActionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private AtomicBoolean atomicBoolean;
    private ActionAtomicBoolean actionAtomicBoolean;


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
        return String.format("Set the atomic boolean to true ::: Use default%n");
    }

    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A logixNG%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! A%n" +
                "         Set the atomic boolean to true ::: Use default%n");
    }

    @Override
    public NamedBean createNewBean(String systemName) {
        return new ActionAtomicBoolean(systemName, null);
    }

    @Override
    public boolean addNewSocket() {
        return false;
    }

    @Test
    public void testCtor() {
        assertNotNull( _base, "object exists");

        ActionAtomicBoolean action2;
        assertNotNull( atomicBoolean, "atomicBoolean is not null");
        atomicBoolean.set(true);

        action2 = new ActionAtomicBoolean("IQDA321", null);
        assertNotNull( action2, "object exists");
        assertNull( action2.getUserName(), "Username matches");
        assertEquals( "Set the atomic boolean to false",
                action2.getLongDescription(), "String matches");

        action2 = new ActionAtomicBoolean("IQDA321", "My atomicBoolean");
        assertNotNull( action2, "object exists");
        assertEquals( "My atomicBoolean", action2.getUserName(), "Username matches");
        assertEquals( "Set the atomic boolean to false",
                action2.getLongDescription(), "String matches");

        action2 = new ActionAtomicBoolean("IQDA321", null);
        action2.setAtomicBoolean(atomicBoolean);
        assertSame( atomicBoolean, action2.getAtomicBoolean(), "atomic boolean is correct");
        assertNotNull( action2, "object exists");
        assertNull( action2.getUserName(), "Username matches");
        assertEquals( "Set the atomic boolean to false",
                action2.getLongDescription(), "String matches");

        AtomicBoolean ab = new AtomicBoolean();
        action2 = new ActionAtomicBoolean("IQDA321", "My atomicBoolean");
        action2.setAtomicBoolean(ab);
        assertSame( ab, action2.getAtomicBoolean(), "atomic boolean is correct");
        assertNotNull( action2, "object exists");
        assertEquals( "My atomicBoolean", action2.getUserName(), "Username matches");
        assertEquals( "Set the atomic boolean to false", action2.getLongDescription(), "String matches");

        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new ActionAtomicBoolean("IQA55:12:XY11", null);
            fail("should have thrown, created " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);

        ex = assertThrows( IllegalArgumentException.class, () -> {
            var t = new ActionAtomicBoolean("IQA55:12:XY11", "A name");
            fail("should have thrown, created " + t);
        }, "Illegal system name Expected exception thrown");
        assertNotNull(ex);

        // Test setup(). This method doesn't do anything, but execute it for coverage.
        _base.setup();
    }

    @Test
    public void testGetChild() {
        assertEquals( 0, actionAtomicBoolean.getChildCount(), "getChildCount() returns 0");

        UnsupportedOperationException ex = assertThrows( UnsupportedOperationException.class, () ->
            actionAtomicBoolean.getChild(0), "Exception is thrown");
        assertEquals( "Not supported.", ex.getMessage(), "Error message is correct");
    }

    @Test
    public void testAction() throws SocketAlreadyConnectedException {
        // Set new value to true
        actionAtomicBoolean.setNewValue(true);
        assertTrue( actionAtomicBoolean.getNewValue(), "new value is true");
        // Set the atomic boolean
        atomicBoolean.set(false);
        // The atomic boolean should be false
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the atomic boolean should be true
        assertTrue( atomicBoolean.get(), "atomicBoolean is true");

        // Set new value to false
        actionAtomicBoolean.setNewValue(false);
        assertFalse( actionAtomicBoolean.getNewValue(), "new value is false");
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the atomic boolean should be true
        assertFalse( atomicBoolean.get(), "atomicBoolean is false");
    }

    @Test
    public void testCategory() {
        assertSame( LogixNG_Category.OTHER, _base.getCategory(), "Category matches");
    }

    @Test
    public void testShortDescription() {
        assertEquals( "Atomic boolean", _base.getShortDescription(), "String matches");
    }

    @Test
    public void testLongDescription() {
        assertEquals( "Set the atomic boolean to true", _base.getLongDescription(), "String matches");
    }

    @Test
    public void testChild() {
        assertEquals( 0, _base.getChildCount(), "Num children is zero");
        UnsupportedOperationException ex = assertThrows( UnsupportedOperationException.class, () ->
            _base.getChild(0), "Exception is thrown");
        assertEquals( "Not supported.", ex.getMessage(), "Error message is correct");
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

        _category = LogixNG_Category.ITEM;
        _isExternal = true;

        atomicBoolean = new AtomicBoolean(false);
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A logixNG");
        conditionalNG = new DefaultConditionalNGScaffold("IQC1", "A conditionalNG");  // NOI18N;
        InstanceManager.getDefault(ConditionalNG_Manager.class).register(conditionalNG);
        logixNG.addConditionalNG(conditionalNG);
        conditionalNG.setRunDelayed(false);
        conditionalNG.setEnabled(true);
        actionAtomicBoolean = new ActionAtomicBoolean("IQDA321", null, atomicBoolean, true);
        MaleSocket socket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionAtomicBoolean);
        conditionalNG.getChild(0).connect(socket);

        _base = actionAtomicBoolean;
        _baseMaleSocket = socket;

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
