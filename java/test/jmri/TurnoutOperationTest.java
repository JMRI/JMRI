package jmri;

import jmri.implementation.AbstractTurnout;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the TurnoutOperation class
 *
 * @author	Bob Jacobsen Copyright (C) 2016
 */
public class TurnoutOperationTest {

    @Test
    @SuppressWarnings("unlikely-arg-type") // String unrelated when testing Wrong type
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
    
    @Before
    public void setUp() throws Exception { 
        jmri.util.JUnitUtil.setUp(); 
        jmri.util.JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
    }

    @After
    public void tearDown() throws Exception { 
        jmri.util.JUnitUtil.tearDown(); 
    }

}
