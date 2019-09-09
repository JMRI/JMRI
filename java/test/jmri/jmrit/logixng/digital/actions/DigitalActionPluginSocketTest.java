package jmri.jmrit.logixng.digital.actions;

import java.util.Map;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.DigitalActionPlugin;
import jmri.jmrit.logixng.LogixNG;
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

    @Override
    public ConditionalNG getConditionalNG() {
        return null;
    }
    
    @Override
    public LogixNG getLogixNG() {
        return null;
    }
    
    @Override
    public String getExpectedPrintedTree() {
        return String.format("Set turnout '' to Thrown%n");
    }
    
    @Override
    public String getExpectedPrintedTreeFromRoot() {
        return String.format("Set turnout '' to Thrown%n");
    }
    
    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", new DigitalActionPluginSocket("IQDA1", new MyDigitalActionPlugin("IQDA2")));
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        _base = new DigitalActionPluginSocket("IQDA1", new MyDigitalActionPlugin("IQDA2"));
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
    
    
    private class MyDigitalActionPlugin extends ActionTurnout implements DigitalActionPlugin {

        public MyDigitalActionPlugin(String sys) throws BadUserNameException {
            super(sys);
        }

        @Override
        public void init(Map<String, String> config) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Map<String, String> getConfig() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
    
}
