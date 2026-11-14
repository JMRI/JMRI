package jmri.jmrix.can.cbus.swing.bootloader;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.cbus.CbusConstants;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the BootloaderPane class
 *
 * @author Bob Andrew Crosland (C) 2020
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class CbusBootloaderPaneTest extends jmri.util.swing.JmriPanelTest {

    private jmri.jmrix.can.CanSystemConnectionMemo memo = null;
    private jmri.jmrix.can.TrafficControllerScaffold tcis = null;

    @Override 
    @Test
    public void testInitComponents() {
        // for now, just makes ure there isn't an exception.
        Assertions.assertDoesNotThrow( () -> {
            ((CbusBootloaderPane) panel).initComponents(memo);
        });
    }

    @Test
    public void testInitComponentsNoArgs() {
        // for now, just makes ure there isn't an exception.
        Assertions.assertDoesNotThrow( () -> {
            panel.initComponents();
        });
    }

    @Test
    public void testInitContext() {
        // for now, just makes ure there isn't an exception.
        Assertions.assertDoesNotThrow( () -> {
            ((CbusBootloaderPane) panel).initContext(memo);
        });
    }


    // PARAN reply from the node being programmed, standard frame
    private static CanReply paran(int nn, int index, int value) {
        CanReply r = new CanReply(new int[]{CbusConstants.CBUS_PARAN, nn >> 8, nn & 0xFF, index, value});
        r.setHeader(0x11);
        return r;
    }

    // Set up the pane as if "Read Node Parameters" had been pressed for node 256,
    // without starting a parameter request timer.
    private CbusBootloaderPane readingParams() {
        CbusBootloaderPane p = (CbusBootloaderPane) panel;
        p.initComponents(memo);
        tcis.outbound.clear();
        p.nodeNumber = 256;
        p.hardwareParams = new CbusParameters();
        p.nextParam = 0;
        p.bootState = CbusBootloaderPane.BootState.GET_PARAMS;
        return p;
    }

    @Test
    public void testParanStoredAndNextRequested() {
        CbusBootloaderPane p = readingParams();
        p.reply(paran(256, 0, 7)); // 7 parameters

        Assertions.assertEquals(7, p.hardwareParams.getParam(0), "parameter 0 stored");
        Assertions.assertEquals(1, p.nextParam, "next parameter");
        Assertions.assertEquals(1, tcis.outbound.size(), "request for next parameter sent");
        CanMessage m = tcis.outbound.get(0);
        Assertions.assertEquals(CbusConstants.CBUS_RQNPN, m.getElement(0), "RQNPN");
        Assertions.assertEquals(256, (m.getElement(1) << 8) + m.getElement(2), "to node 256");
        Assertions.assertEquals(1, m.getElement(3), "for parameter 1");

        // let the request timeout expire, nothing else is waiting on it
        JUnitUtil.waitFor(() -> !p.hasActiveTimers(), "parameter timer stopped");
    }

    @Test
    public void testParanFromOtherNodeIgnored() {
        CbusBootloaderPane p = readingParams();
        p.reply(paran(257, 0, 7));

        Assertions.assertEquals(-1, p.hardwareParams.getParam(0), "parameter 0 not stored");
        Assertions.assertEquals(0, p.nextParam, "next parameter unchanged");
        Assertions.assertTrue(tcis.outbound.isEmpty(), "no request sent");
    }

    @Test
    public void testParanUnexpectedIndexIgnored() {
        CbusBootloaderPane p = readingParams();
        p.reply(paran(256, 3, 7)); // expecting parameter 0

        Assertions.assertEquals(-1, p.hardwareParams.getParam(3), "parameter 3 not stored");
        Assertions.assertEquals(0, p.nextParam, "next parameter unchanged");
        Assertions.assertTrue(tcis.outbound.isEmpty(), "no request sent");
    }

    @Test
    public void testParanOutOfRangeIndexIgnored() {
        CbusBootloaderPane p = readingParams();
        p.nextParam = CbusParameters.MAX_PARAMS + 1;
        Assertions.assertDoesNotThrow(() -> p.reply(paran(256, CbusParameters.MAX_PARAMS + 1, 7)));
        JUnitAppender.assertWarnMessageStartingWith("Ignoring PARAN reply from node 256 with out of range parameter index");

        Assertions.assertEquals(CbusParameters.MAX_PARAMS + 1, p.nextParam, "next parameter unchanged");
        Assertions.assertTrue(tcis.outbound.isEmpty(), "no request sent");
    }

    @Test
    public void testParanWhenNotReadingIgnored() {
        CbusBootloaderPane p = readingParams();
        p.hardwareParams = null;
        Assertions.assertDoesNotThrow(() -> p.reply(paran(256, 0, 7)));

        Assertions.assertEquals(0, p.nextParam, "next parameter unchanged");
        Assertions.assertTrue(tcis.outbound.isEmpty(), "no request sent");
    }

    @Test
    public void testParanLastParameterFinishesReading() {
        CbusBootloaderPane p = readingParams();
        p.busyDialog = new jmri.util.swing.BusyDialog(null, "test", false);
        p.reply(paran(256, 0, 0)); // node reports no further parameters

        Assertions.assertTrue(p.hardwareParams.areValid(), "parameters valid");
        Assertions.assertEquals(CbusBootloaderPane.BootState.IDLE, p.bootState, "back to idle");
        Assertions.assertNull(p.busyDialog, "busy dialog closed");
        Assertions.assertTrue(tcis.outbound.isEmpty(), "no further request sent");
    }

    @Test
    public void testGetDevId() {
        // CBUS_EXT_DEVID, manufacturer, device, then the 4-byte ID, most significant first
        CanReply r = new CanReply(new int[]{0, 165, 1, 0x12, 0x34, 0x56, 0x78});
        Assertions.assertEquals(0x12345678L, CbusBootloaderPane.getDevId(r), "device ID from bytes 3 to 6");

        r = new CanReply(new int[]{0, 165, 1, 0xFF, 0xFE, 0xFD, 0xFC});
        Assertions.assertEquals(0xFFFEFDFCL, CbusBootloaderPane.getDevId(r), "device ID is unsigned");
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        memo = new jmri.jmrix.can.CanSystemConnectionMemo();
        tcis = new jmri.jmrix.can.TrafficControllerScaffold();
        memo.setTrafficController(tcis);
        
        memo.setProtocol(jmri.jmrix.can.CanConfigurationManager.MERGCBUS);
        // memo.configureManagers();
        
        panel = new CbusBootloaderPane();
        helpTarget="package.jmri.jmrix.can.cbus.swing.bootloader.CbusBootloaderPane";
        title="CBUS Firmware Update";
    }

    @AfterEach
    @Override
    public void tearDown() {
        Assertions.assertNotNull(tcis);
        tcis.terminateThreads();
        Assertions.assertNotNull(memo);
        memo.dispose();
        tcis = null;
        memo = null;
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }


    
}
