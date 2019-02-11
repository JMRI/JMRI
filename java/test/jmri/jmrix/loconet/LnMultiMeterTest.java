package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
public class LnMultiMeterTest {

    LocoNetInterfaceScaffold lnis;
    SlotManager slotmanager;
    LocoNetSystemConnectionMemo memo;

    @Test
    public void testLnMeter() {
        LnMultiMeter lm = new LnMultiMeter(memo);
        Assert.assertNotNull("exists",lm);
        Assert.assertTrue(lm.hasCurrent());
        Assert.assertTrue(lm.hasVoltage());
        lm.requestUpdateFromLayout();
        // expect one messages
        Assert.assertEquals("sent", 1, lnis.outbound.size());
        // set CS
        Assert.assertEquals("message 1", "BB 79 41 00", lnis.outbound.get(0).toString());
        int ia[]={0xE6, 0x15, 0x01, 0x79, 0x4B, 0x4D, 0x05, 0x32, 0x70, 0x00, 0x36, 0x00,
                0x33, 0x00, 0x00, 0x00, 0x1C, 0x7F, 0x68, 0x03, 0x38 };
        LocoNetMessage msg = new LocoNetMessage(ia);
        lm.message(msg);
        Assert.assertEquals(0.1f,lm.getCurrent(),0);
    }

    @Before
    public void setUp() {
        lnis = new LocoNetInterfaceScaffold();
        slotmanager = new SlotManager(lnis);
        memo = new LocoNetSystemConnectionMemo(lnis,slotmanager);

        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        memo.dispose();
        slotmanager.dispose();
        lnis.dispose();
        JUnitUtil.tearDown();
    }
}
