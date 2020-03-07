package jmri.jmrit.ussctc;

import jmri.util.JUnitUtil;
import org.junit.*;


/**
 * Tests for TrafficLock class in the jmri.jmrit.ussctc package
 *
 * @author	Bob Jacobsen Copyright 2007
 */
public class TrafficLockTest {

    @Test
    public void testLocked() {
        SignalHeadSection s = new SignalHeadSection() {
            @Override
            public String getName() { return "";}
        };
        s.lastIndication = SignalHeadSection.CODE_LEFT;

        TrafficLock lock = new TrafficLock(s, SignalHeadSection.CODE_LEFT);

        Assert.assertTrue(! lock.isLockClear());
    }

    @Test
    public void testUnlocked() {
        SignalHeadSection s = new SignalHeadSection() {
            @Override
            public String getName() { return "";}
        };
        s.lastIndication = SignalHeadSection.CODE_RIGHT;

        TrafficLock lock = new TrafficLock(s, SignalHeadSection.CODE_LEFT);

        Assert.assertTrue(lock.isLockClear());
    }


    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initMemoryManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
