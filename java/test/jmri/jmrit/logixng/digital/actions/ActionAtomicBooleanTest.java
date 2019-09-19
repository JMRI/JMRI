package jmri.jmrit.logixng.digital.actions;

import java.util.concurrent.atomic.AtomicBoolean;
import jmri.InstanceManager;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrit.logixng.DigitalActionBean;

/**
 * Test ActionTurnout
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
    public String getExpectedPrintedTree() {
        return String.format("Set the atomic boolean to true%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A logixNG%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! %n" +
                "         Set the atomic boolean to true%n");
    }
    
    @Test
    public void testCtor() {
        Assert.assertTrue("object exists", _base != null);
        
        ActionAtomicBoolean action2;
        Assert.assertNotNull("memory is not null", atomicBoolean);
        atomicBoolean.set(true);
        
        action2 = new ActionAtomicBoolean("IQDA321", null);
        Assert.assertNotNull("object exists", action2);
        Assert.assertNull("Username matches", action2.getUserName());
        Assert.assertEquals("String matches", "Set the atomic boolean to false", action2.getLongDescription());
        
        action2 = new ActionAtomicBoolean("IQDA321", "My memory");
        Assert.assertNotNull("object exists", action2);
        Assert.assertEquals("Username matches", "My memory", action2.getUserName());
        Assert.assertEquals("String matches", "Set the atomic boolean to false", action2.getLongDescription());
        
        action2 = new ActionAtomicBoolean("IQDA321", null);
        action2.setAtomicBoolean(atomicBoolean);
        Assert.assertNotNull("object exists", action2);
        Assert.assertNull("Username matches", action2.getUserName());
        Assert.assertEquals("String matches", "Set the atomic boolean to false", action2.getLongDescription());
        
        action2 = new ActionAtomicBoolean("IQDA321", "My memory");
        action2.setAtomicBoolean(atomicBoolean);
        Assert.assertNotNull("object exists", action2);
        Assert.assertEquals("Username matches", "My memory", action2.getUserName());
        Assert.assertEquals("String matches", "Set the atomic boolean to false", action2.getLongDescription());
        
        // Test template
        action2 = (ActionAtomicBoolean)_base.getNewObjectBasedOnTemplate();
        Assert.assertNotNull("object exists", action2);
        Assert.assertNull("Username is null", action2.getUserName());
//        Assert.assertTrue("Username matches", "My memory".equals(expression2.getUserName()));
        Assert.assertEquals("String matches", "Set the atomic boolean to false", action2.getLongDescription());
        
        boolean thrown = false;
        try {
            // Illegal system name
            new ActionAtomicBoolean("IQA55:12:XY11", null);
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
        
        thrown = false;
        try {
            // Illegal system name
            new ActionAtomicBoolean("IQA55:12:XY11", "A name");
        } catch (IllegalArgumentException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
    }
    
    @Test
    public void testAction() throws SocketAlreadyConnectedException {
        // Set new value to true
        actionAtomicBoolean.setNewValue(true);
        Assert.assertTrue("new value is true", actionAtomicBoolean.getNewValue());
        // The action is not yet executed so the atomic boolean should be false
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the atomic boolean should be true
        Assert.assertTrue("atomicBoolean is true",atomicBoolean.get());
        
        // Set new value to false
        actionAtomicBoolean.setNewValue(false);
        Assert.assertFalse("new value is false", actionAtomicBoolean.getNewValue());
        // Execute the conditional
        conditionalNG.execute();
        // The action should now be executed so the atomic boolean should be true
        Assert.assertFalse("atomicBoolean is false",atomicBoolean.get());
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNG();
        
        atomicBoolean = new AtomicBoolean(false);
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A logixNG");
        conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        logixNG.addConditionalNG(conditionalNG);
        conditionalNG.setEnabled(true);
        actionAtomicBoolean = new ActionAtomicBoolean("IQDA321", null, atomicBoolean, true);
        MaleSocket socket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionAtomicBoolean);
        conditionalNG.getChild(0).connect(socket);
        
        _base = actionAtomicBoolean;
        _baseMaleSocket = socket;
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
