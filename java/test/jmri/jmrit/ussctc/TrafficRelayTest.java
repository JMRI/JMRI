package jmri.jmrit.ussctc;

import org.junit.*;

import jmri.*;

import java.util.*;

/**
 * Tests for TrafficRelay class in the jmri.jmrit.ussctc package
 *
 * @author	Bob Jacobsen Copyright 2007
  */
public class TrafficRelayTest {

    @Test
    public void testLocked() {
        SignalHeadSection s = new SignalHeadSection() {
            public String getName() { return "";}
        };
        s.lastIndication = SignalHeadSection.CODE_LEFT;
        
        TrafficLock lock = new TrafficLock(s, SignalHeadSection.CODE_LEFT);
        
        Assert.assertTrue(! lock.isLockClear());
    }

    @Test
    public void testUnlocked() {
        SignalHeadSection s = new SignalHeadSection() {
            public String getName() { return "";}
        };
        s.lastIndication = SignalHeadSection.CODE_RIGHT;
        
        TrafficLock lock = new TrafficLock(s, SignalHeadSection.CODE_LEFT);
        
        Assert.assertTrue(lock.isLockClear());
    }

      
    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initConfigureManager();
        jmri.util.JUnitUtil.initMemoryManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
