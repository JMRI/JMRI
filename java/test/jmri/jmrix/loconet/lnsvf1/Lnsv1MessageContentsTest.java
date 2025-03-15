package jmri.jmrix.loconet.lnsvf1;

import jmri.jmrix.loconet.LocoNetMessage;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static jmri.jmrix.loconet.lnsvf1.Lnsv1MessageContents.*;

/**
 *
 * @author Paul Bender Copyright (C) 2025
 */
public class Lnsv1MessageContentsTest {

    @Test
    public void testCTorIllegalArgument() {
        LocoNetMessage lm = new LocoNetMessage(3); // Lnsv1Message length should be 15
        Assert.assertThrows(IllegalArgumentException.class, () -> new Lnsv1MessageContents(lm));

        LocoNetMessage ln = new LocoNetMessage(new int[] {0xE5, 0x02, 0x05, 0x49, 0x4B, 0x1F, 0x11, 0x29, 0x13, 0x01, 0x00, 0x69, 0x03, 0x00, 0x4D});
        Assert.assertThrows(IllegalArgumentException.class, () -> new Lnsv1MessageContents(ln)); // invalid bytes

        LocoNetMessage l = new LocoNetMessage(new int[]{0xE5, 0x10, 0x01, 0x47, 0x02, 0x10, 0x3D, 0x01, 0x0D, 0x01, 0x10, 0x0B, 0x00, 0x00, 0x00, 0x75});
        Assertions.assertNull(Lnsv1MessageContents.extractMessageType(l), "check extract of cmd not lnsv1");
    }

    @Test
    public void testCTor() {
        LocoNetMessage l = new LocoNetMessage(new int[]{0xE5, 0x10, 0x50, 0x04, 0x01, 0x00, 0x02, 0x4A, 0x00, 0x00, 0x10, 0x02, 0x00, 0x00, 0x00, 0x05});
        Lnsv1MessageContents lnsv1 = new Lnsv1MessageContents(l);
        // static methods
        Assertions.assertTrue(isSupportedSv1Message(l), "check message validity");
        Assertions.assertTrue(isLnMessageASpecificSv1Command(l, Sv1Command.SV1_READ), "check isSupportedLnsv1Read");
        Assertions.assertEquals(extractMessageType(l), Sv1Command.SV1_READ,"check isSupportedLnsv1Read");
        Assertions.assertEquals(-1, extractMessageVersion(l), "check message version");
        // getters
        Assertions.assertEquals(74, lnsv1.getSvNum(), "check SV num");
        Assertions.assertEquals(80, lnsv1.getSrcL(), "check SRC_L");
        Assertions.assertEquals(4, lnsv1.getDstL(), "check DST_L");
        Assertions.assertEquals(2, lnsv1.getCmd(), "check CMD");
        Assertions.assertEquals(-1, lnsv1.getVersionNum(), "check Version num");
        Assertions.assertEquals(0, lnsv1.getSvValue(), "check svNum");

    }

    @Test
    public void testCreateMessage() {
        LocoNetMessage m1 = createSv1WriteRequest(0x13, 0x25, 0x20, 7);
        Assertions.assertEquals("(LNSV1) LocoBuffer => LocoIO@19/37 (0x13/0x25): Write SV32 (0x20) = 7.\n",
                m1.toMonitorString(), "Test Create Write");

        LocoNetMessage m2 = createSv1ReadRequest(0x100, 0x17, 0x2);
        Assertions.assertEquals("(LNSV1) LocoBuffer => broadcast: Probe All.\n",
                m2.toMonitorString(), "Test Create Read");

        LocoNetMessage[] mset = createBroadcastSetAddress(0x13, 0x25);
        Assertions.assertEquals("(LNSV1) LocoBuffer => LocoIO@broadcast: Set subaddress SV2 = 37 (0x25).\n",
                mset[1].toMonitorString(), "Test Create Set address");

        LocoNetMessage m4 = createBroadcastProbeAll();
        Assertions.assertEquals("(LNSV1) LocoBuffer => broadcast: Probe All.\n",
                m4.toMonitorString(), "Test Create Probe All");

        LocoNetMessage m5 = createSv1ReadReply(81, 50, 80, 106, 8, 66);
        Assertions.assertEquals("(LNSV1) LocoIO@81/80 (0x51/0x50) => LocoBuffer: Report SV8 = 66 (0x42) Firmware rev 1.0.6.\n",
                m5.toMonitorString(), "Test Create Sim Read Reply");

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(Lnsv1MessageContentsTest.class);

}
