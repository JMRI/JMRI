package jmri.jmrit.logix;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        Engineer t = new Engineer(warrant,throttle);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(EngineerTest.class.getName());

}
