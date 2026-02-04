package jmri.jmrit.ussctc;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for TrafficRelay class in the jmri.jmrit.ussctc package
 *
 * @author Bob Jacobsen Copyright 2007
 */
public class TrafficRelayTest {

    @Test
    public void testLocked() {
        SignalHeadSection s = new SignalHeadSection() {
            @Override
            public String getName() {
                return "";
            }
        };
        s.lastIndication = SignalHeadSection.CODE_LEFT;

        TrafficRelay lock = new TrafficRelay(s, SignalHeadSection.CODE_LEFT);

        assertFalse(lock.isLockClear(Lock.turnoutLockLogger));
    }

    @Test
    public void testUnlocked() {
        SignalHeadSection s = new SignalHeadSection() {
            @Override
            public String getName() {
                return "";
            }
        };
        s.lastIndication = SignalHeadSection.CODE_RIGHT;

        TrafficRelay lock = new TrafficRelay(s, SignalHeadSection.CODE_LEFT);

        assertTrue(lock.isLockClear(Lock.turnoutLockLogger));
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
