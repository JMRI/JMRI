package jmri.jmrix.dcc4pc;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class Dcc4PcReporterTest extends jmri.implementation.AbstractRailComReporterTest {

    @BeforeEach
    @Override
    public void setUp() {
        r = new Dcc4PcReporter("DR1","test");
        JUnitUtil.setUp();
    }

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

    // 2026-10-13: while a write to CV 32 waited for its answer, replies to earlier POM packets
    // (still being repeated) were taken as the answer. A reply only answers the CV its DCC packet was for.
    @Test
    public void testPomReplyOnlyAnswersItsOwnCv() {
        Dcc4PcReporter rep = (Dcc4PcReporter) r;
        rep.locoEntered(9328, true);
        jmri.RailCom tag = (jmri.RailCom) jmri.InstanceManager.getDefault(jmri.RailComManager.class).provideIdTag("9328");
        tag.setExpectedCv(8);
        // channel 1 ADR_HIGH, then channel 2 POM value 151 and ACKs, as seen on the layout
        int[] reply = {0x9C, 0x47, 0xA9, 0x69, 0x0F, 0x0F, 0x0F, 0x0F};

        rep.setPacket(reply, Dcc4PcSensorManager.LONG_ADDRESS, 9328, 300, 0, 0); // answers a packet for CV 300
        Assertions.assertEquals(8, tag.getExpectedCv(), "a reply for CV 300 doesn't answer CV 8");

        rep.setPacket(reply, Dcc4PcSensorManager.LONG_ADDRESS, 9328, 8, 0, 0);
        Assertions.assertEquals(-1, tag.getExpectedCv(), "answered");
        Assertions.assertEquals(151, tag.getCV(8));
    }

    // 2026-10-13: the POM answer to a read of CV 1 came with garbage in channel 1 (95 04),
    // and the whole cutout was dropped. Channel 2 is a separate slot and still counts.
    @Test
    public void testBadChannel1KeepsChannel2() {
        Dcc4PcReporter rep = (Dcc4PcReporter) r;
        rep.locoEntered(9328, true);
        jmri.RailCom tag = (jmri.RailCom) jmri.InstanceManager.getDefault(jmri.RailComManager.class).provideIdTag("9328");
        tag.setExpectedCv(1);
        rep.setPacket(new int[]{0x95, 0x04, 0xAC, 0xA5, 0x0F, 0x0F, 0x0F, 0x0F}, Dcc4PcSensorManager.LONG_ADDRESS, 9328, 1, 0, 0);
        Assertions.assertEquals(-1, tag.getExpectedCv(), "answered");
        Assertions.assertEquals(3, tag.getCV(1));
    }

    // private static final Logger log = LoggerFactory.getLogger(Dcc4PcReporterTest.class);

}
