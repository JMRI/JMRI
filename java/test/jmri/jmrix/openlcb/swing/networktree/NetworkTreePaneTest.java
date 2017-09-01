package jmri.jmrix.openlcb.swing.networktree;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Bob Jacobsen Copyright 2013
 * @author Paul Bender Copyright (C) 2016
 */
public class NetworkTreePaneTest {

    jmri.jmrix.can.CanSystemConnectionMemo memo;
    jmri.jmrix.can.TrafficController tc;

    @Test
    public void testCtor() {
        NetworkTreePane p = new NetworkTreePane();
        Assert.assertNotNull("Pane object non-null", p);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();

        memo = new jmri.jmrix.can.CanSystemConnectionMemo();
        tc = new jmri.jmrix.can.adapters.loopback.LoopbackTrafficController();
        memo.setTrafficController(tc);
        memo.setProtocol(jmri.jmrix.can.ConfigurationManager.OPENLCB);

    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetWindows(false, false);
        JUnitUtil.tearDown();
    }
}
