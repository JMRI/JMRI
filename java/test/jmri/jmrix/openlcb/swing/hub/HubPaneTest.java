package jmri.jmrix.openlcb.swing.hub;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Bob Jacobsen Copyright 2013
 */
public class HubPaneTest {

    HubPane hub;
    jmri.jmrix.can.CanSystemConnectionMemo memo;
    jmri.jmrix.can.TrafficController tc;

    @Test
    public void testCtor() {
        hub = new HubPane();
        Assert.assertNotNull("Connection memo object non-null", memo);
        // this next step takes 30 seconds of clock time, so has been commented out
        //hub.initContext(memo);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();

        memo = new jmri.jmrix.can.CanSystemConnectionMemo();
        Assert.assertNotNull("Connection memo object non-null", memo);
        tc = new jmri.jmrix.can.adapters.loopback.LoopbackTrafficController();
        memo.setTrafficController(tc);
        memo.setProtocol(jmri.jmrix.can.ConfigurationManager.OPENLCB);

    }

    @After
    public void tearDown() {
        hub.stopHubThread();
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }
}
