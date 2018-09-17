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
        TurnoutSignalMast t = new TurnoutSignalMast("IF$tsm:basic:one-searchlight($1)");
        t.setTurnout("Stop", "IT1", Turnout.THROWN);
        t.setTurnout("Clear", "IT2", Turnout.THROWN);
        
        t.setAspect("Clear");
        Assert.assertEquals("Clear", t.getAspect());
        
        t.setAspect("Stop");
        Assert.assertEquals("Stop", t.getAspect());
        
        try {
            t.setAspect("Marblesnarb");
        } catch (IllegalArgumentException ex) {
            jmri.util.JUnitAppender.assertWarnMessage("attempting to set invalid aspect: Marblesnarb on mast: IF$tsm:basic:one-searchlight($1)");
            return;
        }
        
        Assert.fail("should have thrown");
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    //private final static Logger log = LoggerFactory.getLogger(TurnoutSignalMastTest.class);

}
