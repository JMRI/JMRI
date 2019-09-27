package jmri.jmrit.ussctc;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for TrafficRelay class in the jmri.jmrit.ussctc package
 *
 * @author	Bob Jacobsen Copyright 2007
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

        Assert.assertTrue(!lock.isLockClear());
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
