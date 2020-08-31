package jmri.jmrix.loconet;

import jmri.Meter;
import jmri.MeterGroup;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

public class LnMeterGroupTest {

    LocoNetInterfaceScaffold lnis;
    SlotManager slotmanager;
    LocoNetSystemConnectionMemo memo;

    @Test
    public void testLnMeter() {
        LnMeterGroup lm = new LnMeterGroup(memo);
        Assert.assertNotNull("exists",lm);
        Assert.assertNotNull(lm.getMeterByName(MeterGroup.CurrentMeter));
        Assert.assertNotNull(lm.getMeterByName(MeterGroup.VoltageMeter));
        Assert.assertEquals("Reports in Amps", lm.getMeterByName(MeterGroup.CurrentMeter).getMeter().getUnit(), Meter.Unit.NoPrefix);
        lm.requestUpdateFromLayout();
        // expect one messages
        Assert.assertEquals("sent", 1, lnis.outbound.size());
        // set CS
        Assert.assertEquals("message 1", "BB 79 41 00", lnis.outbound.get(0).toString());
        int ia[]={0xE6, 0x15, 0x01, 0x79, 0x4B, 0x4D, 0x05, 0x32, 0x70, 0x00, 0x36, 0x00,
                0x33, 0x00, 0x00, 0x00, 0x1C, 0x7F, 0x68, 0x03, 0x38 };
        LocoNetMessage msg = new LocoNetMessage(ia);
        lm.message(msg);
        Assert.assertEquals(0.5f,getCurrent(lm),0); // 0.5AMps
    }

    public double getCurrent(LnMeterGroup lm) {
        return lm.getMeterByName(MeterGroup.CurrentMeter).getMeter().getKnownAnalogValue();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();

        lnis = new LocoNetInterfaceScaffold();
        slotmanager = new SlotManager(lnis);
        memo = new LocoNetSystemConnectionMemo(lnis,slotmanager);
    }

    @AfterEach
    public void tearDown() {
        memo.dispose();
        lnis.dispose();
        JUnitUtil.tearDown();
    }
}
