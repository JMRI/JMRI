package jmri.jmrit.logixng;

import java.util.Locale;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
import jmri.jmrit.logixng.actions.DigitalMany;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        Assert.assertEquals("beanType is correct", "ConditionalNG", conditionalNG.getBeanType());
    }

    @Test
    public void testGetParent() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        ConditionalNG conditionalNG_1 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "A conditionalNG");  // NOI18N
        Assert.assertTrue("getParent() returns correct value", logixNG == conditionalNG_1.getParent());
    }

    @Test
    public void testGetLogixNG() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        ConditionalNG conditionalNG_1 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "A conditionalNG");  // NOI18N
        Assert.assertTrue("getLogixNG() returns correct value", logixNG == conditionalNG_1.getLogixNG());
    }

    @Test
    public void testGetRoot() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        ConditionalNG conditionalNG_1 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "A conditionalNG");  // NOI18N
        Assert.assertTrue("getRoot() returns correct value", logixNG == conditionalNG_1.getRoot());
    }

    @Test
    public void testState() throws JmriException {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class)
                .createLogixNG("A new logix for test");  // NOI18N
        Assert.assertTrue("getState() returns UNKNOWN", logixNG.getState() == LogixNG.UNKNOWN);
        JUnitAppender.assertWarnMessage("Unexpected call to getState in DefaultLogixNG.");
        logixNG.setState(LogixNG.INCONSISTENT);
        JUnitAppender.assertWarnMessage("Unexpected call to setState in DefaultLogixNG.");
        Assert.assertTrue("getState() returns UNKNOWN", logixNG.getState() == LogixNG.UNKNOWN);
        JUnitAppender.assertWarnMessage("Unexpected call to getState in DefaultLogixNG.");
    }

    public void setupInitialConditionalNGTree(ConditionalNG conditionalNG) {
        try {
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
        } catch (SocketAlreadyConnectedException e) {
            // This should never be able to happen.
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testConnectDisconnect() throws SocketAlreadyConnectedException {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class)
                .createLogixNG("A new logix for test");  // NOI18N
        DefaultConditionalNG conditionalNG =
                (DefaultConditionalNG) InstanceManager.getDefault(ConditionalNG_Manager.class)
                        .createConditionalNG(logixNG, "A conditionalNG");  // NOI18N
        Assert.assertEquals("socket name is correct", null, conditionalNG.getSocketSystemName());
        setupInitialConditionalNGTree(conditionalNG);
        MaleSocket many = conditionalNG.getChild(0).getConnectedSocket();
        Assert.assertTrue("description is correct", "Many".equals(many.getLongDescription()));
        Assert.assertEquals("socket name is correct", many.getSystemName(), conditionalNG.getSocketSystemName());
        conditionalNG.getChild(0).disconnect();
        Assert.assertEquals("socket name is correct", null, conditionalNG.getSocketSystemName());
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


        Assert.assertEquals("socket name is correct", null, conditionalNG.getSocketSystemName());
        conditionalNG.setSocketSystemName("Abc");
        Assert.assertEquals("socket name is correct", "Abc", conditionalNG.getSocketSystemName());
        conditionalNG.setSocketSystemName("Def");
        Assert.assertEquals("socket name is correct", "Def", conditionalNG.getSocketSystemName());
        conditionalNG.getFemaleSocket().connect(actionManySocket);
        Assert.assertEquals("socket name is correct",
                actionManySocket.getSystemName(),
                conditionalNG.getSocketSystemName());
        conditionalNG.setSocketSystemName(null);
        Assert.assertEquals("socket name is correct", null, conditionalNG.getSocketSystemName());
    }

    @Test
    public void testShortDescription() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class)
                .createLogixNG("A new logix for test");  // NOI18N
        ConditionalNG conditionalNG_1 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "A conditionalNG");  // NOI18N
        Assert.assertEquals("getLongDescription() returns correct value",
                "ConditionalNG: A conditionalNG", conditionalNG_1.getLongDescription(Locale.US));
    }

    @Test
    public void testLongDescription() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class)
                .createLogixNG("A new logix for test");  // NOI18N
        ConditionalNG conditionalNG_1 = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "A conditionalNG");  // NOI18N
        Assert.assertEquals("getLongDescription() returns correct value",
                "ConditionalNG: A conditionalNG", conditionalNG_1.getLongDescription(Locale.US));
    }

    @Test
    public void testGetChild() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class)
                .createLogixNG("A new logix for test");  // NOI18N
        ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "A new conditionalng for test");  // NOI18N

        Assert.assertNotNull("child(0) is not null", conditionalNG.getChild(0));

        boolean hasThrown = false;
        try {
            conditionalNG.getChild(1);
        } catch (IllegalArgumentException e) {
            hasThrown = true;
        }
        Assert.assertTrue("exception thrown", hasThrown);
    }

    @Test
    public void testGetChildCount() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class)
                .createLogixNG("A new logix for test");  // NOI18N
        ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "A new conditionalng for test");  // NOI18N
        Assert.assertEquals("conditionalNG has one child", 1, conditionalNG.getChildCount());
    }

    @Test
    public void testGetCategory() {
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class)
                .createLogixNG("A new logix for test");  // NOI18N
        ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(logixNG, "A new conditionalng for test");  // NOI18N
        boolean hasThrown = false;
        try {
            conditionalNG.getCategory();
        } catch (UnsupportedOperationException e) {
            hasThrown = true;
        }
        Assert.assertTrue("exception thrown", hasThrown);
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
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }

}
