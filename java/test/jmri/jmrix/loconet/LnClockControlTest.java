package jmri.jmrix.loconet;

import java.util.Date;
import jmri.jmrix.loconet.LnClockControl.CommandStationFracType;
import jmri.jmrix.loconet.LnClockControl.TestState;
import jmri.util.JUnitUtil;

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

        LnClockControl t = new LnClockControl(c);
        Assert.assertNotNull("exists",t);

        c.dispose();
    }

    @Test
    public void testCtorTwoArg() {
        LnTrafficController lnis = new LocoNetInterfaceScaffold();
        SlotManager slotmanager = new SlotManager(lnis);
 
        LnClockControl t = new LnClockControl(slotmanager, lnis, null);
 
        Assert.assertNotNull("exists",t);
        slotmanager.dispose();
    }

    @Test
    public void testConfigureHardware() throws jmri.JmriException {
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();
        SlotManager slotmanager = new SlotManager(lnis);
        LocoNetSystemConnectionMemo c = new LocoNetSystemConnectionMemo(lnis, slotmanager);

        // allow actual write
        jmri.InstanceManager.getDefault(jmri.Timebase.class).setSynchronize(true, false);

        // set power manager to ON
        c.getPowerManager().setPower(jmri.PowerManager.ON);
        c.getPowerManager().message(lnis.outbound.get(0));
        lnis.outbound.removeAllElements();

        LnClockControl t = new LnClockControl(c);

        // configure, hence write
        Date testDate = new Date(2018, 12, 1);  // deprecated, but OK for test
        t.setTestState(TestState.TESTING_NO_SYNC);
        t.initializeHardwareClock(1.0, testDate, false);

        // expect one messages
        Assert.assertEquals("sent", 1, lnis.outbound.size());
        // set CS
        Assert.assertEquals("message 1", "EF 0E 7B 01 4F 7F 43 07 68 00 40 4C 03 00", lnis.outbound.get(0).toString());

        c.dispose();
    }

    @Test
    public void testSync() throws jmri.JmriException {
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold();
        SlotManager slotmanager = new SlotManager(lnis);
        LocoNetSystemConnectionMemo c = new LocoNetSystemConnectionMemo(lnis, slotmanager);

        // allow actual write
        jmri.InstanceManager.getDefault(jmri.Timebase.class).setSynchronize(true, false);
        // stop clock
        jmri.InstanceManager.getDefault(jmri.Timebase.class).setRun(false);

        // set power manager to ON
        c.getPowerManager().setPower(jmri.PowerManager.ON);
        c.getPowerManager().message(lnis.outbound.get(0));
        JUnitUtil.waitFor(()->{ return lnis.outbound.size()==2;}, "Wait for random power query");
        Assert.assertEquals("message 1", "BB 00 00 00", lnis.outbound.get(1).toString());
        lnis.outbound.removeAllElements();

        LnClockControl t = new LnClockControl(c);

        // configure, hence write
        Date testDate = new Date(2018, 12, 1);  // deprecated, but OK for test
        //lnis.outbound.removeAllElements();
        t.setTestState(TestState.TESTING_WITH_SYNC);

        t.initializeHardwareClock(1.0, testDate, false);

        Assert.assertEquals("message 1", "EF 0E 7B 01 4F 7F 43 07 68 00 40 4C 03 00", lnis.outbound.get(0).toString());
        JUnitUtil.waitFor(()->{ return lnis.outbound.size()>1;}, "Wait next set at min-1");
        Assert.assertEquals("message 2", "EF 0E 7B 01 4F 7F 42 07 68 00 40 4C 03 00", lnis.outbound.get(1).toString());
        JUnitUtil.waitFor(()->{ return lnis.outbound.size()>2;}, "Wait for read fcslot No1");
        Assert.assertEquals("message 3", "BB 7B 00 00", lnis.outbound.get(2).toString());
        int ia[]={0xE7, 0x0E, 0x7B, 0x01, 0x5F, 0x7F, 0x42, 0x07,
                0x68, 0x00, 0x40, 0x00, 0x00, 0x00 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        t.message(lm);
        JUnitUtil.waitFor(()->{ return lnis.outbound.size()>3;}, "Wait for read fcslot No2");
        Assert.assertEquals("message 4", "BB 7B 00 00", lnis.outbound.get(3).toString());
        ia[4]= 0x6F;
        lm =new LocoNetMessage(ia);
        t.message(lm);
        JUnitUtil.waitFor(()->{ return lnis.outbound.size()>4;}, "Wait for read fcslot No3");
        Assert.assertEquals("message 5", "BB 7B 00 00", lnis.outbound.get(4).toString());
        ia[4]=0x01;
        ia[5]=0x69;
        lm =new LocoNetMessage(ia);
        t.message(lm);
        JUnitUtil.waitFor(()->{ return lnis.outbound.size()>5;}, "Wait for read fcslot No4");
        Assert.assertEquals("message 6", "BB 7B 00 00", lnis.outbound.get(5).toString());
        ia[4]=0x20;
        ia[5]=0x69;
        lm =new LocoNetMessage(ia);
        t.message(lm);
        JUnitUtil.waitFor(()->{ return lnis.outbound.size()>6;}, "Wait for read fcslot No5");
        Assert.assertEquals("message 7", "BB 7B 00 00", lnis.outbound.get(6).toString());
        ia[4]=0x30;
        ia[5]=0x69;
        lm =new LocoNetMessage(ia);
        t.message(lm);
        JUnitUtil.waitFor(()->{ return t.commandStationSyncLimit<1 ;}, "Wait for sync done");
        Assert.assertEquals("CommandStationType", CommandStationFracType.TYPE1, t.getCommandStationFracType());
        // flip minute
        t.newMinute();
        // clock goes back to start of minute.
        JUnitUtil.waitFor(()->{ return lnis.outbound.size()>7;}, "Wait for read fcslot No6");
        Assert.assertEquals("time request", "E7 0E 7B 01 01 69 43 07 68 00 40 4C 03 00", lnis.outbound.get(7).toString());

        // reset and force re-calibrate
        // test for type 2
        lnis.outbound.removeAllElements();
        t.startCalibrate();

        JUnitUtil.waitFor(()->{ return lnis.outbound.size()>0;}, "Wait next set at min-1 for test 2");
        Assert.assertEquals("message 2A", "EF 0E 7B 01 4F 7F 42 07 68 00 40 4C 03 00", lnis.outbound.get(0).toString());
        JUnitUtil.waitFor(()->{ return lnis.outbound.size()>1;}, "Wait for read fcslot No21");
        Assert.assertEquals("message 3A", "BB 7B 00 00", lnis.outbound.get(1).toString());
        int ia2[]={0xE7, 0x0E, 0x7B, 0x01, 0x5F, 0x7F, 0x42, 0x07,
                0x68, 0x00, 0x40, 0x00, 0x00, 0x00 };
        lm =new LocoNetMessage(ia2);
        t.message(lm);
        JUnitUtil.waitFor(()->{ return lnis.outbound.size()>2;}, "Wait for read fcslot No22");
        Assert.assertEquals("message 4A", "BB 7B 00 00", lnis.outbound.get(2).toString());
        ia2[4]= 0x6F;
        lm =new LocoNetMessage(ia2);
        t.message(lm);
        JUnitUtil.waitFor(()->{ return lnis.outbound.size()>3;}, "Wait for read fcslot No23");
        Assert.assertEquals("message 5A", "BB 7B 00 00", lnis.outbound.get(3).toString());
        ia2[4]=0x10;
        ia2[5]=0x69;
        lm =new LocoNetMessage(ia2);
        t.message(lm);
        JUnitUtil.waitFor(()->{ return lnis.outbound.size()>4;}, "Wait for read fcslot No24");
        Assert.assertEquals("message 6A", "BB 7B 00 00", lnis.outbound.get(4).toString());
        ia2[4]=0x70;
        ia2[5]=0x69;
        lm =new LocoNetMessage(ia2);
        t.message(lm);
        JUnitUtil.waitFor(()->{ return lnis.outbound.size()>5;}, "Wait for read fcslot No25");
        Assert.assertEquals("message 7A", "BB 7B 00 00", lnis.outbound.get(5).toString());
        ia2[4]=0x20;
        ia2[5]=0x69;
        lm =new LocoNetMessage(ia2);
        t.message(lm);
        JUnitUtil.waitFor(()->{ return t.commandStationSyncLimit <1 ;}, "Wait for sync done2");
        // ask for time
        t.newMinute();
        Assert.assertEquals("CommandStationType2", CommandStationFracType.TYPE2, t.getCommandStationFracType());
        Assert.assertEquals("ZeroSecBase", 0x6910, t.getCommandStationZeroSecond());
        // clock goes back to start of minute.
        JUnitUtil.waitFor(()->{ return lnis.outbound.size()>6;}, "Wait for read fcslot No6");
        Assert.assertEquals("time request", "E7 0E 7B 01 10 69 43 07 68 00 40 4C 03 00", lnis.outbound.get(6).toString());

        c.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LnClockControlTest.class);

}
