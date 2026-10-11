package jmri.jmrit.logixng;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Locale;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
import jmri.jmrit.logixng.actions.DigitalMany;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test ConditionalNG
 *
 * @author Daniel Bergqvist 2018
 */
public class ConditionalNGTest {

    @Test
    public void testGetBeanType() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "A conditionalNG");  // NOI18N
        assertEquals( "ConditionalNG", conditionalNG.getBeanType(), "beanType is correct");
    }

    @Test
    public void testGetParent() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        ConditionalNG conditionalNG_1 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "A conditionalNG");  // NOI18N
        assertSame( logixNG, conditionalNG_1.getParent(), "getParent() returns correct value");
    }

    @Test
    public void testGetLogixNG() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        ConditionalNG conditionalNG_1 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "A conditionalNG");  // NOI18N
        assertSame( logixNG, conditionalNG_1.getLogixNG(), "getLogixNG() returns correct value");
    }

    @Test
    public void testGetRoot() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        ConditionalNG conditionalNG_1 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "A conditionalNG");  // NOI18N
        assertSame( logixNG, conditionalNG_1.getRoot(), "getRoot() returns correct value");
    }

    @Test
    public void testState() throws JmriException {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class)
                .createLogixNG("A new logix for test");  // NOI18N
        assertEquals( LogixNG.UNKNOWN, logixNG.getState(), "getState() returns UNKNOWN");
        JUnitAppender.assertWarnMessage("Unexpected call to getState in DefaultLogixNG.");
        logixNG.setState(LogixNG.INCONSISTENT);
        JUnitAppender.assertWarnMessage("Unexpected call to setState in DefaultLogixNG.");
        assertEquals( LogixNG.UNKNOWN, logixNG.getState(), "getState() returns UNKNOWN");
        JUnitAppender.assertWarnMessage("Unexpected call to getState in DefaultLogixNG.");
    }

    public void setupInitialConditionalNGTree(ConditionalNG conditionalNG) {
        assertDoesNotThrow( () -> {
            DigitalActionManager digitalActionManager =
                    InstanceManager.getDefault(DigitalActionManager.class);

            FemaleSocket femaleSocket = conditionalNG.getFemaleSocket();
            MaleDigitalActionSocket actionManySocket =
                    InstanceManager.getDefault(DigitalActionManager.class)
                            .registerAction(new DigitalMany(digitalActionManager.getAutoSystemName(), null));
            femaleSocket.connect(actionManySocket);
//            femaleSocket.setLock(Base.Lock.HARD_LOCK);

//            femaleSocket = actionManySocket.getChild(0);
//            MaleDigitalActionSocket actionIfThenSocket =
//                    InstanceManager.getDefault(DigitalActionManager.class)
//                            .registerAction(new IfThenElse(digitalActionManager.getAutoSystemName(), null, IfThenElse.Type.TRIGGER_ACTION));
//            femaleSocket.connect(actionIfThenSocket);
        });
    }

    @Test
    public void testConnectDisconnect() throws SocketAlreadyConnectedException {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class)
                .createLogixNG("A new logix for test");  // NOI18N
        DefaultConditionalNG conditionalNG =
                (DefaultConditionalNG) InstanceManager.getDefault(ConditionalNG_Manager.class)
                        .createConditionalNG(logixNG, "A conditionalNG");  // NOI18N
        assertNull( conditionalNG.getSocketSystemName(), "socket name is correct");
        setupInitialConditionalNGTree(conditionalNG);
        MaleSocket many = conditionalNG.getChild(0).getConnectedSocket();
        assertEquals( "Many", many.getLongDescription(), "description is correct");
        assertEquals( many.getSystemName(), conditionalNG.getSocketSystemName(), "socket name is correct");
        conditionalNG.getChild(0).disconnect();
        assertNull( conditionalNG.getSocketSystemName(), "socket name is correct");
    }

    @Test
    public void testSetSocketName() throws SocketAlreadyConnectedException {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class)
                .createLogixNG("A new logix for test");  // NOI18N
        DefaultConditionalNG conditionalNG =
                (DefaultConditionalNG) InstanceManager.getDefault(ConditionalNG_Manager.class)
                        .createConditionalNG(logixNG, "A conditionalNG");  // NOI18N

        DigitalActionManager digitalActionManager =
                InstanceManager.getDefault(DigitalActionManager.class);

        MaleDigitalActionSocket actionManySocket =
                InstanceManager.getDefault(DigitalActionManager.class)
                        .registerAction(new DigitalMany(digitalActionManager.getAutoSystemName(), null));


        assertNull( conditionalNG.getSocketSystemName(), "socket name is correct");
        conditionalNG.setSocketSystemName("Abc");
        assertEquals( "Abc", conditionalNG.getSocketSystemName(), "socket name is correct");
        conditionalNG.setSocketSystemName("Def");
        assertEquals( "Def", conditionalNG.getSocketSystemName(), "socket name is correct");
        conditionalNG.getFemaleSocket().connect(actionManySocket);
        assertEquals( actionManySocket.getSystemName(),
                conditionalNG.getSocketSystemName(),
                "socket name is correct");
        conditionalNG.setSocketSystemName(null);
        assertNull( conditionalNG.getSocketSystemName(), "socket name is correct");
    }

    @Test
    public void testShortDescription() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class)
                .createLogixNG("A new logix for test");  // NOI18N
        ConditionalNG conditionalNG_1 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "A conditionalNG");  // NOI18N
        assertEquals( "ConditionalNG: A conditionalNG",
                conditionalNG_1.getLongDescription(Locale.US),
                "getLongDescription() returns correct value");
    }

    @Test
    public void testLongDescription() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class)
                .createLogixNG("A new logix for test");  // NOI18N
        ConditionalNG conditionalNG_1 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "A conditionalNG");  // NOI18N
        assertEquals( "ConditionalNG: A conditionalNG",
                conditionalNG_1.getLongDescription(Locale.US),
                "getLongDescription() returns correct value");
    }

    @Test
    public void testGetChild() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class)
                .createLogixNG("A new logix for test");  // NOI18N
        ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "A new conditionalng for test");  // NOI18N

        assertNotNull( conditionalNG.getChild(0), "child(0) is not null");

        IllegalArgumentException e = assertThrows( IllegalArgumentException.class, () ->
            conditionalNG.getChild(1), "exception thrown");
        assertNotNull(e);
    }

    @Test
    public void testGetChildCount() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class)
                .createLogixNG("A new logix for test");  // NOI18N
        ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "A new conditionalng for test");  // NOI18N
        assertEquals( 1, conditionalNG.getChildCount(), "conditionalNG has one child");
    }

    @Test
    public void testGetCategory() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class)
                .createLogixNG("A new logix for test");  // NOI18N
        ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "A new conditionalng for test");  // NOI18N

        UnsupportedOperationException e = assertThrows( UnsupportedOperationException.class, () ->
            conditionalNG.getCategory(), "exception thrown");
        assertNotNull(e);
    }
/*
    @Test
    public void testSetLock() {
        ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG("A new conditionalng for test");  // NOI18N
        boolean hasThrown = false;
        try {
            conditionalNG.setLock(Lock.NONE);
        } catch (UnsupportedOperationException e) {
            hasThrown = true;
        }
        Assert.assertTrue("exception thrown", hasThrown);
    }
*/

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
    }

    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
