package jmri.jmrix.openlcb.swing.hub;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrix.can.TestTrafficController;

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
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();

        memo  = new jmri.jmrix.openlcb.OlcbSystemConnectionMemo();
        TestTrafficController tc = new TestTrafficController();
        memo.setTrafficController(tc);

    }

    @After
    public void tearDown() {
        hub.stopHubThread();
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }
}
