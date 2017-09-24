package jmri.jmrix.openlcb.swing.send;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
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

    @Test
    public void testInitComponents() throws Exception{
        OpenLcbCanSendPane p = new OpenLcbCanSendPane();
        // for now, just makes ure there isn't an exception.
        p.initComponents(memo);
    }

    @Test
    public void testInitContext() throws Exception {
        OpenLcbCanSendPane p = new OpenLcbCanSendPane();
        // for now, just makes ure there isn't an exception.
        p.initContext(memo);
    }

    @Test
    public void testGetHelpTarget(){
        OpenLcbCanSendPane p = new OpenLcbCanSendPane();
        Assert.assertEquals("help target","package.jmri.jmrix.openlcb.swing.send.OpenLcbCanSendPane",p.getHelpTarget());
    }

    @Test
    public void testGetTitle(){
        OpenLcbCanSendPane p = new OpenLcbCanSendPane();
        Assert.assertEquals("title","Send CAN Frames and OpenLCB Messages",p.getTitle());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new jmri.jmrix.can.CanSystemConnectionMemo();
        tc = new jmri.jmrix.can.TestTrafficController();
        memo.setTrafficController(tc);
        memo.setProtocol(jmri.jmrix.can.ConfigurationManager.OPENLCB);
        memo.configureManagers();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
