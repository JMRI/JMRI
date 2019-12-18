package jmri.jmrix.openlcb.swing.send;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openlcb.EventID;

/**
 * @author Bob Jacobsen Copyright 2013
 * @author Paul Bender Copyright (C) 2016
 */
public class OpenLcbCanSendPaneTest extends jmri.util.swing.JmriPanelTest {

    jmri.jmrix.can.CanSystemConnectionMemo memo;
    jmri.jmrix.can.TrafficController tc;

    @Test
    @Override
    public void testInitComponents() throws Exception{
        // for now, just makes ure there isn't an exception.
        ((OpenLcbCanSendPane)panel).initComponents(memo);
    }

    @Test
    public void testInitContext() throws Exception {
        // for now, just makes ure there isn't an exception.
        ((OpenLcbCanSendPane)panel).initContext(memo);
    }

    @Test
    public void testEventId() throws Exception {
        OpenLcbCanSendPane p = (OpenLcbCanSendPane) panel;

        p.sendEventField.setText("05 01 01 01 14 FF 01 02");
        EventID expected = new EventID(new byte[]{05, 01, 01, 01, 0x14, (byte) 0xff, 01, 02});
        Assert.assertEquals(expected, p.eventID());
    }

    @Test
    public void testEventIdDotted() throws Exception {
        OpenLcbCanSendPane p = (OpenLcbCanSendPane) panel;

        p.sendEventField.setText("05.01.01.01.14.FF.01.02");
        EventID expected = new EventID(new byte[]{05, 01, 01, 01, 0x14, (byte) 0xff, 01, 02});
        Assert.assertEquals(expected, p.eventID());
    }


    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        memo = new jmri.jmrix.can.CanSystemConnectionMemo();
        tc = new jmri.jmrix.can.TestTrafficController();
        memo.setTrafficController(tc);
        memo.setProtocol(jmri.jmrix.can.ConfigurationManager.OPENLCB);
        memo.configureManagers();
        panel = new OpenLcbCanSendPane();
        helpTarget="package.jmri.jmrix.openlcb.swing.send.OpenLcbCanSendPane";
        title="Send CAN Frames and OpenLCB Messages";
    }

    @After
    @Override
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }
}
