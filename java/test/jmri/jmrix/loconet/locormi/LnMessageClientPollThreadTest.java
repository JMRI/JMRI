package jmri.jmrix.loconet.locormi;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LnMessageClientPollThreadTest.class);

}
