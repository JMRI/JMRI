package jmri;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.util.JUnitUtil;
import jmri.implementation.AbstractTurnout;

/**
 * Tests for the TurnoutOperation class
 *
 * @author	Bob Jacobsen Copyright (C) 2016
 */
public class TurnoutOperationTest extends TestCase {

    public void testEquals() {
        TurnoutOperation to1 = new TurnoutOperation("to1"){
            public TurnoutOperation makeCopy(String n) { return null; }
            public boolean equivalentTo(TurnoutOperation other) { return true; }
            public TurnoutOperator getOperator(AbstractTurnout t) { return null; }
        };
        TurnoutOperation to2 = new TurnoutOperation("to2"){
            public TurnoutOperation makeCopy(String n) { return null; }
            public boolean equivalentTo(TurnoutOperation other) { return true; }
            public TurnoutOperator getOperator(AbstractTurnout t) { return null; }
        };
        TurnoutOperation to2a = new TurnoutOperation("to2"){
            public TurnoutOperation makeCopy(String n) { return null; }
            public boolean equivalentTo(TurnoutOperation other) { return true; }
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
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(TurnoutOperationTest.class);
        return suite;
    }

    // The minimal setup for log4J

    protected void setUp() throws Exception { 
        apps.tests.Log4JFixture.setUp(); 
        super.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
    }

    protected void tearDown() throws Exception { 
        super.tearDown();
        apps.tests.Log4JFixture.tearDown(); 
        JUnitUtil.resetTurnoutOperationManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.resetInstanceManager();
    }

}
