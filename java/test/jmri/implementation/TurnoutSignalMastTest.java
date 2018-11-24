package jmri.implementation;

import jmri.*;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class TurnoutSignalMastTest {

    @Test
    public void testCTor() {
        TurnoutSignalMast t = new TurnoutSignalMast("IF$tsm:basic:one-searchlight($1)");
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testSetAspect() {
        Turnout it1 = InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        Turnout it2 = InstanceManager.turnoutManagerInstance().provideTurnout("IT2");
        TurnoutSignalMast t = new TurnoutSignalMast("IF$tsm:basic:one-searchlight($1)");
        t.setTurnout("Stop", "IT1", Turnout.THROWN);
        t.setTurnout("Clear", "IT2", Turnout.CLOSED);
        
        t.setAspect("Clear");
        Assert.assertEquals("Clear", t.getAspect());
        Assert.assertEquals(Turnout.UNKNOWN, it1.getCommandedState()); // not thrown
        Assert.assertEquals(Turnout.CLOSED, it2.getCommandedState());
        
        t.setAspect("Stop");
        Assert.assertEquals("Stop", t.getAspect());
        Assert.assertEquals(Turnout.THROWN, it1.getCommandedState());
        Assert.assertEquals(Turnout.CLOSED, it2.getCommandedState()); // unchanged
        
        try {
            t.setAspect("Marblesnarb");
        } catch (IllegalArgumentException ex) {
            jmri.util.JUnitAppender.assertWarnMessage("attempting to set invalid aspect: Marblesnarb on mast: IF$tsm:basic:one-searchlight($1)");
            return;
        }
        
        Assert.fail("should have thrown");
    }

    @Test
    public void testUnLit() {
        Turnout it1 = InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        Turnout it2 = InstanceManager.turnoutManagerInstance().provideTurnout("IT2");
        TurnoutSignalMast t = new TurnoutSignalMast("IF$tsm:basic:one-searchlight($1)");
        t.setTurnout("Stop", "IT1", Turnout.THROWN);
        t.setTurnout("Clear", "IT2", Turnout.CLOSED);
        
        t.setAspect("Clear");
        Assert.assertEquals(true, t.getLit());

        t.setLit(false);
        Assert.assertEquals(false, t.getLit());
        Assert.assertEquals("Clear", t.getAspect());
        Assert.assertEquals(Turnout.CLOSED, it1.getCommandedState());
        Assert.assertEquals(Turnout.THROWN, it2.getCommandedState());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    //private final static Logger log = LoggerFactory.getLogger(TurnoutSignalMastTest.class);

}
