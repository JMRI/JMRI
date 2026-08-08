package jmri.jmrix.loconet;

import java.util.Date;
import jmri.util.JUnitUtil;

import jmri.util.JUnitAppender;
import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LnClockControlTest {

    @Test
    public void testCtorOneArg() {
        LnTrafficController lnis = new LocoNetInterfaceScaffold();
        SlotManager slotmanager = new SlotManager(lnis);
        LocoNetSystemConnectionMemo c = new LocoNetSystemConnectionMemo(lnis, slotmanager);
        lnis.setSystemConnectionMemo(c);
        slotmanager.setCommandStationType(LnCommandStationType.COMMAND_STATION_DCS050);

        LnClockControl t = new LnClockControl(c);
        Assert.assertNotNull("exists",t);

        c.dispose();
        JUnitAppender.suppressWarnMessage("Cannot get throttleID from ThrottleManager. Using default 0x3ff1.");        
    }

    @Test
    public void testCtorTwoArg() {
        LnTrafficController lnis = new LocoNetInterfaceScaffold();
        SlotManager slotmanager = new SlotManager(lnis);
        LocoNetSystemConnectionMemo c = new LocoNetSystemConnectionMemo(lnis, slotmanager);
        lnis.setSystemConnectionMemo(c);
        slotmanager.setCommandStationType(LnCommandStationType.COMMAND_STATION_DCS050);

        LnClockControl t = new LnClockControl(slotmanager, lnis, null);

        Assert.assertNotNull("exists",t);
        slotmanager.dispose();
        JUnitAppender.suppressWarnMessage("Cannot get throttleID from ThrottleManager. Using default 0x3ff1.");        
    }

    @Test
    @SuppressWarnings("deprecation")        // Date(int,int,int)
    public void testConfigureHardware() throws jmri.JmriException {
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();
        SlotManager slotmanager = new SlotManager(lnis);
        LocoNetSystemConnectionMemo c = new LocoNetSystemConnectionMemo(lnis, slotmanager);
        lnis.setSystemConnectionMemo(c);
        slotmanager.setCommandStationType(LnCommandStationType.COMMAND_STATION_DCS050);

        // allow actual write
        jmri.InstanceManager.getDefault(jmri.Timebase.class).setSynchronize(true, false);

        LnClockControl t = new LnClockControl(c);

        // configure, hence write
        Date testDate = new Date(2018, 12, 1);  // deprecated, but OK for test
        t.initializeHardwareClock(1.0, testDate, false);

        // expect two messages
        Assert.assertEquals("sent", 2, lnis.outbound.size());
        // ignore first message caused by PowerManager
        // Assert.assertEquals("message 1", "EF 0E 7B 01 04 03 43 06 68 00 00 00 00 00", lnis.outbound.get(0).toString());
        Assert.assertEquals("message 2", "BB 7B 00 00", lnis.outbound.get(1).toString());

        c.dispose();
        JUnitAppender.suppressWarnMessage("Cannot get throttleID from ThrottleManager. Using default 0x3ff1.");        
    }

    @Test
    public void testLnClockStart() throws jmri.JmriException {
        // a brute-force approach to testing that the power bit follows
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();
        SlotManager slotmanager = new SlotManager(lnis);
        LocoNetSystemConnectionMemo c = new LocoNetSystemConnectionMemo(lnis, slotmanager);
        lnis.setSystemConnectionMemo(c);
        slotmanager.setCommandStationType(LnCommandStationType.COMMAND_STATION_DCS050);

        // allow actual write
        jmri.InstanceManager.getDefault(jmri.Timebase.class).setSynchronize(true, false);

        LnClockControl t = new LnClockControl(c);

        // configure, hence write
        Date testDate = new Date(2018, 12, 1);  // deprecated, but OK for test
        t.initializeHardwareClock(1.0, testDate, false);

        // expect two messages
        Assert.assertEquals("sent", 2, lnis.outbound.size());
        // ignore first message caused by PowerManager
        // Assert.assertEquals("message 1", "EF 0E 7B 01 04 03 43 07 68 00 00 00 00 00", lnis.outbound.get(0).toString());
        Assert.assertEquals("message 2", "BB 7B 00 00", lnis.outbound.get(1).toString());

        c.dispose();
        JUnitAppender.suppressWarnMessage("Cannot get throttleID from ThrottleManager. Using default 0x3ff1.");        
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.removeMatchingThreads("LnPowerManager LnTrackStatusUpdateThread");
        JUnitUtil.removeMatchingThreads("LnSensorUpdateThread");
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(LnClockControlTest.class);

}
