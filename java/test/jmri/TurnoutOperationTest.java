package jmri;

import jmri.implementation.AbstractTurnout;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the TurnoutOperation class
 *
 * @author Bob Jacobsen Copyright (C) 2016
 */
public class TurnoutOperationTest {

    @Test
    @SuppressWarnings({"unlikely-arg-type", "ObjectEqualsNull", "IncompatibleEquals"}) // String unrelated when testing Wrong type
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
        
        assertTrue( to1.equals(to1), "Identity");
        
        assertTrue( to2.equals(to2a), "Equal name");
        assertFalse( to1.equals(to2), "Unequal name");
        
        assertFalse( to1.equals("foo"), "Wrong type");
        assertFalse( to1.equals(null), "on null");
        
    }

    @BeforeEach
    public void setUp() { 
        JUnitUtil.setUp(); 
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
    }

    @AfterEach
    public void tearDown() { 
        JUnitUtil.tearDown(); 
    }

}
