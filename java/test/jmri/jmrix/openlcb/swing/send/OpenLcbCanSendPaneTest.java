package jmri.jmrix.openlcb.swing.send;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
/**
 * @author Bob Jacobsen Copyright 2013
 * @author Paul Bender Copyright (C) 2016
 */
public class OpenLcbCanSendPaneTest {

    jmri.jmrix.can.CanSystemConnectionMemo memo;
    jmri.jmrix.can.TrafficController tc;

    @Test
    public void testCtor() {
        OpenLcbCanSendPane p = new OpenLcbCanSendPane();
        Assert.assertNotNull("Pane object non-null", p);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();

        memo = new jmri.jmrix.can.CanSystemConnectionMemo();
        tc = new jmri.jmrix.can.adapters.loopback.LoopbackTrafficController();
        memo.setTrafficController(tc);
        memo.setProtocol(jmri.jmrix.can.ConfigurationManager.OPENLCB);

    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }
}
