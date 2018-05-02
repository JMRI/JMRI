package jmri;

import jmri.implementation.AbstractTurnout;
import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Tests for the TurnoutOperation class
 *
 * @author	Bob Jacobsen Copyright (C) 2016
 */
public class TurnoutOperationTest extends TestCase {

    public void testEquals() {
        TurnoutOperation to1 = new TurnoutOperation("to1"){
            @Override
            public TurnoutOperation makeCopy(String n) { return null; }
            @Override
            public boolean equivalentTo(TurnoutOperation other) { return true; }
            @Override
            public TurnoutOperator getOperator(AbstractTurnout t) { return null; }
        };
        TurnoutOperation to2 = new TurnoutOperation("to2"){
            @Override
            public TurnoutOperation makeCopy(String n) { return null; }
            @Override
            public boolean equivalentTo(TurnoutOperation other) { return true; }
            @Override
            public TurnoutOperator getOperator(AbstractTurnout t) { return null; }
        };
        TurnoutOperation to2a = new TurnoutOperation("to2"){
            @Override
            public TurnoutOperation makeCopy(String n) { return null; }
            @Override
            public boolean equivalentTo(TurnoutOperation other) { return true; }
            @Override
            public TurnoutOperator getOperator(AbstractTurnout t) { return null; }
        };
        
        Assert.assertTrue("Identity", to1.equals(to1));
        
        Assert.assertTrue("Equal name", to2.equals(to2a));
        Assert.assertFalse("Unequal name", to1.equals(to2));
        
        Assert.assertFalse("Wrong type", to1.equals("foo"));
        Assert.assertFalse("on null", to1.equals(null));
        
    }
    
    // from here down is testing infrastructure
    public TurnoutOperationTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {TurnoutOperationTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(TurnoutOperationTest.class);
        return suite;
    }

    // The minimal setup for log4J

    @Override
    protected void setUp() throws Exception { 
        apps.tests.Log4JFixture.setUp(); 
        super.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
    }

    @Override
    protected void tearDown() throws Exception { 
        super.tearDown();
        apps.tests.Log4JFixture.tearDown(); 
        JUnitUtil.resetTurnoutOperationManager();
        JUnitUtil.resetInstanceManager();
    }

}
