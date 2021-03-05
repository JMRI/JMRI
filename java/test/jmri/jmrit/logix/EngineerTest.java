package jmri.jmrit.logix;

import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class EngineerTest {

    @Test
    public void testCTor() {
        Warrant warrant = new Warrant("IW0", "AllTestWarrant");
        jmri.DccLocoAddress addr = new jmri.DccLocoAddress(5,false);
        jmri.SystemConnectionMemo memo = new jmri.jmrix.internal.InternalSystemConnectionMemo();
        jmri.DccThrottle throttle = new jmri.jmrix.debugthrottle.DebugThrottle(addr,memo);
        Engineer t = new Engineer(warrant, throttle);
        assertThat(t).withFailMessage("exists").isNotNull();
        t.stopRun(true, true);
        JUnitAppender.assertErrorMessageStartsWith("Throttle Manager unavailable or cannot provide throttle. 5(S)");
        warrant.stopWarrant(true, true);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(EngineerTest.class);

}
