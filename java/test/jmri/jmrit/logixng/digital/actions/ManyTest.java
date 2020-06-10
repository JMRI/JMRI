package jmri.jmrit.logixng.digital.actions;

import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalAction;
import jmri.jmrit.logixng.DigitalActionBean;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.DigitalExpressionBean;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleDigitalActionSocket;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.jmrit.logixng.digital.expressions.True;
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
        Assert.assertNotNull("exists", new Many("IQDA321", null));
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
