package jmri.jmrix.openlcb.swing.send;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
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
    public void testInitComponents() {
        org.junit.Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));
        // for now, just makes ure there isn't an exception.
        ((OpenLcbCanSendPane)panel).initComponents(memo);
    }

    @Test
    public void testInitContext() {
        org.junit.Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));
        // for now, just makes ure there isn't an exception.
        panel.initContext(memo);
    }

    @Test
    public void testEventId() {
        org.junit.Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));
        OpenLcbCanSendPane p = (OpenLcbCanSendPane) panel;

        p.sendEventField.setText("05 01 01 01 14 FF 01 02");
        EventID expected = new EventID(new byte[]{0x05, 0x01, 0x01, 0x01, 0x14, (byte) 0xff, 0x01, 0x02});
        Assert.assertEquals(expected, p.eventID());
    }

    @Test
    public void testEventIdDotted() {
        org.junit.Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));
        OpenLcbCanSendPane p = (OpenLcbCanSendPane) panel;

        p.sendEventField.setText("05.01.01.01.14.FF.01.02");
        EventID expected = new EventID(new byte[]{0x05, 0x01, 0x01, 0x01, 0x14, (byte) 0xff, 0x01, 0x02});
        Assert.assertEquals(expected, p.eventID());
    }


    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        org.junit.Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));
        JUnitUtil.resetProfileManager();
        memo = new jmri.jmrix.can.CanSystemConnectionMemo();
        tc = new jmri.jmrix.can.TestTrafficController();
        memo.setTrafficController(tc);
        memo.setProtocol(jmri.jmrix.can.ConfigurationManager.OPENLCB);
        memo.configureManagers();
        panel = new OpenLcbCanSendPane();
        helpTarget="package.jmri.jmrix.openlcb.swing.send.OpenLcbCanSendFrame";
        title="Send CAN Frames and OpenLCB Messages";
    }

    @AfterEach
    @Override
    public void tearDown() {
        if (Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning") == false) {
            memo.dispose();
            memo = null;
            tc.terminateThreads();
            tc = null;
            panel = null;
            helpTarget = null;
            title = null;
        }
        JUnitUtil.tearDown();
    }
}
