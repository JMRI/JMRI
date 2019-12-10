package jmri.jmrit.logixng.digital.actions;

import java.util.Map;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.DigitalActionManager;
import jmri.jmrit.logixng.DigitalActionPlugin;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * DigitalActionPluginSocket
 * 
 * @author Daniel Bergqvist 2018
 */
public class DigitalActionPluginSocketTest extends AbstractDigitalActionTestBase {

    private LogixNG logixNG;
    private ConditionalNG conditionalNG;
    private DigitalActionPluginSocket actionDigitalActionPluginSocket;
    
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
        return String.format("Set turnout '' to Thrown%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format(
                "LogixNG: A new logix for test%n" +
                "   ConditionalNG: A conditionalNG%n" +
                "      ! %n" +
                "         Set turnout '' to Thrown%n");
    }
    
    @Override
    public NamedBean createNewBean(String systemName) {
        return new DigitalActionPluginSocket(systemName, null, new MyDigitalActionPlugin("IQDA3"));
    }
    
    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", new DigitalActionPluginSocket("IQDA1", null, new MyDigitalActionPlugin("IQDA2")));
    }
    
    @Test
    public void testGetChild() {
        Assert.assertTrue("getNumChilds() returns 0", 0 == actionDigitalActionPluginSocket.getChildCount());
        
        boolean hasThrown = false;
        try {
            actionDigitalActionPluginSocket.getChild(0);
        } catch (UnsupportedOperationException ex) {
            hasThrown = true;
            Assert.assertEquals("Error message is correct", "Not supported.", ex.getMessage());
        }
        Assert.assertTrue("Exception is thrown", hasThrown);
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() throws SocketAlreadyConnectedException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        
        _category = Category.ITEM;
        _isExternal = true;
        
        logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logix for test");  // NOI18N
        conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class)
                .createConditionalNG("A conditionalNG");  // NOI18N
        logixNG.addConditionalNG(conditionalNG);
        actionDigitalActionPluginSocket = new DigitalActionPluginSocket("IQDA1", null, new MyDigitalActionPlugin("IQDA2"));
        MaleSocket maleSocket =
                InstanceManager.getDefault(DigitalActionManager.class).registerAction(actionDigitalActionPluginSocket);
        conditionalNG.getChild(0).connect(maleSocket);
        _base = actionDigitalActionPluginSocket;
        _baseMaleSocket = maleSocket;
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
    
    
    private class MyDigitalActionPlugin extends ActionTurnout implements DigitalActionPlugin {

        public MyDigitalActionPlugin(String sys) throws BadUserNameException {
            super(sys, null);
        }

        @Override
        public void init(Map<String, String> config) {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public Map<String, String> getConfig() {
            throw new UnsupportedOperationException("Not supported.");
        }
        
    }
    
}
