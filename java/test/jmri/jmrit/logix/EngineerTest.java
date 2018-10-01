package jmri.jmrit.logix;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class EngineerTest {

    @Test
    public void testCTor() {
        Warrant warrant = new Warrant("IW0", "AllTestWarrant");
        jmri.DccLocoAddress addr = new jmri.DccLocoAddress(5,false);
        jmri.jmrix.SystemConnectionMemo memo = new jmri.jmrix.internal.InternalSystemConnectionMemo();
        jmri.DccThrottle throttle = new jmri.jmrix.debugthrottle.DebugThrottle(addr,memo);
        Engineer t = new Engineer(warrant, throttle);
        Assert.assertNotNull("exists",t);
        // Throttle Manager unavailable or cannot provide throttle. 5(S) [main] jmri.jmrit.logix.Warrant.releaseThrottle()
        //t.stopRun(false, true);
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

    // private final static Logger log = LoggerFactory.getLogger(EngineerTest.class);

}
