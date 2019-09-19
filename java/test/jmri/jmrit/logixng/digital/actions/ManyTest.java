package jmri.jmrit.logixng.digital.actions;

import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalAction;
import jmri.jmrit.logixng.DigitalActionBean;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleDigitalActionSocket;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
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
    
    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", new Many("IQDA321", null));
    }
    
    @Test
    @Override
    public void testSupportsEnableExecution() throws SocketAlreadyConnectedException {
        DigitalAction da = new Many("IQDA321", null);
        
        // By default, doesn't support enable execution
        Assert.assertFalse("supportsEnableExecution() returns correct value",
                da.supportsEnableExecution());
        
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
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        logixNG.addConditionalNG(conditionalNG);
        Many action = new Many("IQDA321", null);
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
        conditionalNG.getChild(0).connect(maleSocket);
        _base = action;
        _baseMaleSocket = maleSocket;
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
