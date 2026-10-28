package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import jmri.Block;
import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

/**
 * Tests that asking an ID tag Reporter, or its Block, for the loco address
 * of its report gives null rather than an exception when the report is not a
 * RailCom report, or there is no report. Virtual Sound Decoder location
 * following asks this when a Block with a Reporter becomes occupied.
 */
public class ReporterLocoAddressTest {

    /** A RailCom Reporter, like a CBUS Reporter, which can also read RFID tags. */
    private static class TestRailComReporter extends AbstractRailComReporter {
        TestRailComReporter() {
            super("IRrailcom");
        }

        @Override
        public boolean hasExitReports() {
            return false;
        }
    }

    @Test
    public void testRailComReporterWithRfidTag() {
        TestRailComReporter r = new TestRailComReporter();
        ThreadingUtil.runOnLayout(() -> r.notify(new DefaultIdTag("ID30393130AB")));
        assertNull(r.getLocoAddress("ID30393130AB"), "no loco address from an RFID tag");
    }

    @Test
    public void testRailComReporterWithRailComTag() {
        TestRailComReporter r = new TestRailComReporter();
        DefaultRailCom tag = new DefaultRailCom("ID1234");
        tag.setDccAddress(new DccLocoAddress(1234, LocoAddress.Protocol.DCC_LONG));
        ThreadingUtil.runOnLayout(() -> r.notify(tag));
        assertEquals(new DccLocoAddress(1234, LocoAddress.Protocol.DCC_LONG), r.getLocoAddress("x"), "address");
    }

    @Test
    public void testRailComReporterWithNoReport() {
        assertNull(new TestRailComReporter().getLocoAddress(null), "no report");
    }

    @Test
    public void testIdTagReporterWithNoReport() {
        assertNull(new AbstractIdTagReporter("IRrfid").getLocoAddress(null), "no report");
    }

    @Test
    public void testBlockWithReporterLastReport() {
        // a Block following the last report, with the reporter's current report cleared
        TestRailComReporter r = new TestRailComReporter();
        Block b = new Block("IB1");
        b.setReporter(r);
        ThreadingUtil.runOnLayout(() -> {
            r.notify(new DefaultIdTag("ID30393130AB"));
            r.notify(null);
        });
        assertNull(b.getLocoAddress("ID30393130AB"), "no loco address");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initIdTagManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
