package jmri.jmrix.openlcb.swing.send;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assumptions.assumeFalse;

import jmri.InstanceManager;
import jmri.IdTagManager;
import jmri.jmrix.openlcb.OlcbEventNameStore;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import org.openlcb.EventID;

/**
 * @author Bob Jacobsen Copyright 2013
 * @author Paul Bender Copyright (C) 2016
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class OpenLcbCanSendPaneTest extends jmri.util.swing.JmriPanelTest {

    private jmri.jmrix.can.CanSystemConnectionMemo memo;
    private jmri.jmrix.can.TrafficController tc;

    @Test
    @Override
    public void testInitComponents() {
        assumeFalse( Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"), "Ignoring intermittent test");
        // for now, just makes ure there isn't an exception.
        ((OpenLcbCanSendPane)panel).initComponents(memo);
    }

    @Test
    public void testInitContext() {
        assumeFalse( Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"), "Ignoring intermittent test");
        // for now, just makes ure there isn't an exception.
        panel.initContext(memo);
    }

    @Test
    public void testEventId() {
        assumeFalse( Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"), "Ignoring intermittent test");
        OpenLcbCanSendPane p = (OpenLcbCanSendPane) panel;

        p.sendEventField.setText("05.01.01.01.14.FF.01.02");
        EventID expected = new EventID(new byte[]{0x05, 0x01, 0x01, 0x01, 0x14, (byte) 0xff, 0x01, 0x02});
        assertEquals(expected, p.eventID());
    }

    @Test
    public void testEventIdDotted() {
        assumeFalse( Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"), "Ignoring intermittent test");
        OpenLcbCanSendPane p = (OpenLcbCanSendPane) panel;

        p.sendEventField.setText("05.01.01.01.14.FF.01.02");
        EventID expected = new EventID(new byte[]{0x05, 0x01, 0x01, 0x01, 0x14, (byte) 0xff, 0x01, 0x02});
        assertEquals(expected, p.eventID());
    }


    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
       // this test is run separately because it leaves a lot of threads behind
        assumeFalse( Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"), "Ignoring intermittent test");
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
            memo.get(OlcbEventNameStore.class).deregisterShutdownTask();
            memo.dispose();
            memo = null;
            tc.terminateThreads();
            tc = null;
            panel = null;
            helpTarget = null;
            title = null;
            InstanceManager.getDefault(IdTagManager.class).dispose();
        }
        JUnitAppender.suppressWarnMessage("Can't get IP address to make NodeID. You should set a NodeID in the Connection preferences.");
        JUnitUtil.tearDown();
    }
}
