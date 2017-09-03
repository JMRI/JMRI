package jmri.jmrix.loconet.locormi;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class LnMessageClientPollThreadTest {

    @Test
    public void testCTor() throws InterruptedException {
        LnMessageClient c = new LnMessageClient();
        LnMessageClientPollThread t = new LnMessageClientPollThread(c);
        Assert.assertNotNull("exists",t);
        t.interrupt();
        t.join();
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

    // private final static Logger log = LoggerFactory.getLogger(LnMessageClientPollThreadTest.class);

}
