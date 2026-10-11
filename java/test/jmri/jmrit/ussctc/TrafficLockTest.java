package jmri.jmrit.ussctc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;


/**
 * Tests for TrafficLock class in the jmri.jmrit.ussctc package
 *
 * @author Bob Jacobsen Copyright 2007
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

        assertFalse(lock.isLockClear(Lock.signalLockLogger));
    }

    @Test
    public void testUnlocked() {
        SignalHeadSection s = new SignalHeadSection() {
            @Override
            public String getName() { return "";}
        };
        s.lastIndication = SignalHeadSection.CODE_RIGHT;

        TrafficLock lock = new TrafficLock(s, SignalHeadSection.CODE_LEFT);

        assertTrue(lock.isLockClear(Lock.signalLockLogger));
    }


    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initMemoryManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
