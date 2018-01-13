package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LocoNetSlotTest {

    @Test
    public void testGetSlotSend() {
        SlotManager slotmanager = new SlotManager(lnis);
        SlotListener p2 = new SlotListener() {
            @Override
            public void notifyChangedSlot(LocoNetSlot l) {
            }
        };
        slotmanager.slotFromLocoAddress(21, p2);
        Assert.assertEquals("slot request message",
                "BF 00 15 00",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    @Test
    public void testCTor(){
        LocoNetSlot t = new LocoNetSlot(5);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testMessageCTor() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testGetSlot(){
        LocoNetSlot t = new LocoNetSlot(5);
        Assert.assertEquals("slot number",5,t.getSlot());
    }

    @Test
    public void testSetSlot() throws LocoNetException{
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(1);
        t.setSlot(lm); // we are checking to make sure this does not throw an
                       // exception.
    }

    @Test
    public void testDecoderType() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        Assert.assertEquals("decoder type",LnConstants.DEC_MODE_128,t.decoderType());
    }

    @Test
    public void testSlotStatus() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        Assert.assertEquals("Slot Status",LnConstants.LOCO_IN_USE,t.slotStatus());
    }

    @Test
    public void testss2() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        Assert.assertEquals("Slot Status",0x00,t.ss2());
    }

    @Test
    public void testConsistStatus() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        Assert.assertEquals("Consist Status",LnConstants.CONSIST_NO,t.consistStatus());
    }

    @Test
    public void testIsForward() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        Assert.assertTrue("is Forward",t.isForward());
    }

    @Test
    public void testIsF0() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        Assert.assertFalse("is F0",t.isF0());
    }
    @Test
    public void testIsF1() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        Assert.assertFalse("is F1",t.isF1());
    }
    @Test
    public void testIsF2() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        Assert.assertFalse("is F2",t.isF2());
    }
    @Test
    public void testIsF3() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        Assert.assertFalse("is F3",t.isF3());
    }
    @Test
    public void testIsF4() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        Assert.assertFalse("is F4",t.isF4());
    }
    @Test
    public void testIsF5() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        Assert.assertFalse("is F5",t.isF5());
    }
    @Test
    public void testIsF6() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        Assert.assertFalse("is F6",t.isF6());
    }
    @Test
    public void testIsF7() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        Assert.assertFalse("is F7",t.isF7());
    }
    @Test
    public void testIsF8() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        Assert.assertFalse("is F8",t.isF8());
    }
    @Test
    public void testIsF9() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        Assert.assertFalse("is F9",t.isF9());
    }
    @Test
    public void testIsF10() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        Assert.assertFalse("is F10",t.isF10());
    }
    @Test
    public void testIsF11() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        Assert.assertFalse("is F11",t.isF11());
    }
    @Test
    public void testIsF12() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        Assert.assertFalse("is F12",t.isF12());
    }
    @Test
    public void testIsF13() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        Assert.assertFalse("is F13",t.isF13());
    }
    @Test
    public void testIsF14() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        Assert.assertFalse("is F14",t.isF14());
    }
    @Test
    public void testIsF15() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        Assert.assertFalse("is F15",t.isF15());
    }
    @Test
    public void testIsF16() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        Assert.assertFalse("is F16",t.isF16());
    }
    @Test
    public void testIsF17() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        Assert.assertFalse("is F17",t.isF17());
    }
    @Test
    public void testIsF18() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        Assert.assertFalse("is F18",t.isF18());
    }
    @Test
    public void testIsF19() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        Assert.assertFalse("is F19",t.isF19());
    }
    @Test
    public void testIsF20() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        Assert.assertFalse("is F20",t.isF20());
    }
    @Test
    public void testIsF21() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        Assert.assertFalse("is F21",t.isF21());
    }
    @Test
    public void testIsF22() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        Assert.assertFalse("is F22",t.isF22());
    }
    @Test
    public void testIsF23() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        Assert.assertFalse("is F23",t.isF23());
    }
    @Test
    public void testIsF24() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        Assert.assertFalse("is F24",t.isF24());
    }
    @Test
    public void testIsF25() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        Assert.assertFalse("is F25",t.isF25());
    }
    @Test
    public void testIsF26() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        Assert.assertFalse("is F26",t.isF26());
    }
    @Test
    public void testIsF27() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        Assert.assertFalse("is F27",t.isF27());
    }
    @Test
    public void testIsF28() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        Assert.assertFalse("is F28",t.isF28());
    }

    @Test
    public void testLocoAddr() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        Assert.assertEquals("address",5544,t.locoAddr());
    }

    @Test
    public void testSpeed() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        Assert.assertEquals("speed",0,t.speed());
    }

    @Test
    public void testDirf() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        Assert.assertEquals("directions and Functions",0x00,t.dirf());
    }

    @Test
    public void testSnd() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        Assert.assertEquals("snd",0x00,t.snd());
    }

    @Test
    public void testID() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        Assert.assertEquals("ID",0x00,t.id());
    }

    @Test
    public void testWriteSlot() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        LocoNetMessage lm2 = t.writeSlot();
        Assert.assertEquals("Opcode",LnConstants.OPC_WR_SL_DATA,lm2.getOpCode());
        for(int i = 1;i<=12;i++){
            Assert.assertEquals("Element " + i,lm.getElement(i),lm2.getElement(i));
        }
    }

    @Test
    public void testWriteThrottleID() throws LocoNetException {
        int ia[]={0xE7, 0x0E, 0x01, 0x33, 0x28, 0x00, 0x00, 0x47,
			0x00, 0x2B, 0x00, 0x00, 0x00, 0x60 };
        LocoNetMessage lm =new LocoNetMessage(ia);
        LocoNetSlot t = new LocoNetSlot(new LocoNetMessage(lm));
        LocoNetMessage lm2 = t.writeThrottleID(0x0171);
        Assert.assertEquals("Opcode",LnConstants.OPC_WR_SL_DATA,lm2.getOpCode());
        for(int i = 1;i<=10;i++){
            Assert.assertEquals("Element " + i,lm.getElement(i),lm2.getElement(i));
        }
        Assert.assertEquals("Element 11",0x71,lm2.getElement(11));
        Assert.assertEquals("Element 12",0x02,lm2.getElement(12));
    }

    LocoNetInterfaceScaffold lnis;

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface
        lnis = new LocoNetInterfaceScaffold();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
