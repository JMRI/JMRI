package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;
import jmri.SpeedStepMode;
import org.junit.*;

public class LocoNetThrottleTest extends jmri.jmrix.AbstractThrottleTest {

    @Test
    public void testCTor() {
        Assert.assertNotNull(instance);
    }

    // test the speed setting code.
    @Test
    public void testSpeedSetting() {
        // we have 4 cases to check
        // Case 1: The locomotive is not consisted.
        LocoNetSlot s1 = new LocoNetSlot(0) {
            @Override
            public int consistStatus() {
                return LnConstants.CONSIST_NO;
            }

            @Override
            public int speed() {
                return 0;
            }
        };
        LocoNetThrottle t1 = new LocoNetThrottle(memo, s1);
        Assert.assertEquals(0.0f, t1.getSpeedSetting(), 0.0);
        t1.setSpeedSetting(0.5f);
        // the speed change SHOULD be changed.
        Assert.assertEquals(0.5f, t1.getSpeedSetting(), 0.0);

        // Case 2: The locomotive is a consist top.
        LocoNetSlot s2 = new LocoNetSlot(1) {
            @Override
            public int consistStatus() {
                return LnConstants.CONSIST_TOP;
            }

            @Override
            public int speed() {
                return 0;
            }
        };
        LocoNetThrottle t2 = new LocoNetThrottle(memo, s2);
        Assert.assertEquals(0.0f, t2.getSpeedSetting(), 0.0);
        t2.setSpeedSetting(0.5f);
        // the speed change SHOULD be changed.
        Assert.assertEquals(0.5f, t2.getSpeedSetting(), 0.0);

        // Case 3: The locomotive is a consist mid.
        LocoNetSlot s3 = new LocoNetSlot(2) {
            @Override
            public int consistStatus() {
                return LnConstants.CONSIST_MID;
            }

            @Override
            public int speed() {
                return 0;
            }
        };
        LocoNetThrottle t3 = new LocoNetThrottle(memo, s3);
        Assert.assertEquals(0.0f, t3.getSpeedSetting(), 0.0);
        t3.setSpeedSetting(0.5f);
        // the speed change SHOULD NOT be changed.
        Assert.assertEquals(0.0f, t3.getSpeedSetting(), 0.0);

        // Case 3: The locomotive is a consist mid.
        // make sure the speed does NOT change for a consist sub
        LocoNetSlot s4 = new LocoNetSlot(3) {
            @Override
            public int consistStatus() {
                return LnConstants.CONSIST_SUB;
            }

            @Override
            public int speed() {
                return 0;
            }
        };
        LocoNetThrottle t4 = new LocoNetThrottle(memo, s4);
        Assert.assertEquals(0.0f, t4.getSpeedSetting(), 0.0);
        t4.setSpeedSetting(0.5f);
        // the speed change SHOULD be ignored.
        Assert.assertEquals(0.0f, t4.getSpeedSetting(), 0.0);
    }

    /**
     * Test of getIsForward method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testGetIsForward() {
        boolean expResult = true;
        boolean result = instance.getIsForward();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getSpeedStepMode method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testGetSpeedStepMode() {
        SpeedStepMode expResult = SpeedStepMode.NMRA_DCC_28;
        SpeedStepMode result = instance.getSpeedStepMode();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getSpeedIncrement method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testGetSpeedIncrement() {
        float expResult = 1.0F/28.0F;
        float result = instance.getSpeedIncrement();
        Assert.assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of intSpeed method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testGetSpeed_float() {
        // set speed step mode to 128.
        instance.setSpeedStepMode(jmri.SpeedStepMode.NMRA_DCC_128);
        Assert.assertEquals("Full Speed", 127, ((LocoNetThrottle)instance).intSpeed(1.0F));
        float incre = 0.007874016f;
        float speed = incre;
        // Cannot get speeedStep 1. range is 2 to 127
        int i = 2;
        while (speed < 0.999f) {
            int result = ((LocoNetThrottle)instance).intSpeed(speed);
            Assert.assertEquals("speed step ", i++, result);
            speed += incre;
        }
    }




    /**
     * Test of setF0 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF0() {
        lnis.outbound.clear();
        lnis.resetStatistics();
        boolean f0 = false;
        instance.setF0(f0);
        Assert.assertEquals(f0, instance.getF0());
        Assert.assertEquals("number of messages is 1", 1, lnis.outbound.size());
        Assert.assertEquals("opcode is OPC_LOCO_DIRF", LnConstants.OPC_LOCO_DIRF, lnis.outbound.get(0).getOpCode());
        Assert.assertEquals("sent f0 in correct state", 0, lnis.outbound.get(0).getElement(2) & 0x10);
        f0 = true;
        instance.setF0(f0);
        Assert.assertEquals(f0, instance.getF0());
        Assert.assertEquals("number of messages is 2", 2, lnis.outbound.size());
        Assert.assertEquals("opcode is OPC_LOCO_DIRF", LnConstants.OPC_LOCO_DIRF, lnis.outbound.get(1).getOpCode());
        Assert.assertEquals("sent f0 in correct state", 0x10, lnis.outbound.get(1).getElement(2) & 0x10);
    }

    /**
     * Test of setF1 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF1() {
        lnis.outbound.clear();
        lnis.resetStatistics();
        boolean f1 = false;
        instance.setF1(f1);
        Assert.assertEquals(f1, instance.getF1());
        Assert.assertEquals("number of messages is 1", 1, lnis.outbound.size());
        Assert.assertEquals("opcode is OPC_LOCO_DIRF", LnConstants.OPC_LOCO_DIRF, lnis.outbound.get(0).getOpCode());
        Assert.assertEquals("sent f1 in correct state", 0, lnis.outbound.get(0).getElement(2) & 1);
        f1 = true;
        instance.setF1(f1);
        Assert.assertEquals(f1, instance.getF1());
        Assert.assertEquals("number of messages is 2", 2, lnis.outbound.size());
        Assert.assertEquals("opcode is OPC_LOCO_DIRF", LnConstants.OPC_LOCO_DIRF, lnis.outbound.get(1).getOpCode());
        Assert.assertEquals("sent f1 in correct state", 1, lnis.outbound.get(1).getElement(2) & 1);
    }

    /**
     * Test of setF2 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF2() {
        lnis.outbound.clear();
        lnis.resetStatistics();
        boolean f2 = false;
        instance.setF2(f2);
        Assert.assertEquals(f2, instance.getF2());
        Assert.assertEquals("number of messages is 1", 1, lnis.outbound.size());
        Assert.assertEquals("opcode is OPC_LOCO_DIRF", LnConstants.OPC_LOCO_DIRF, lnis.outbound.get(0).getOpCode());
        Assert.assertEquals("sent f2 in correct state", 0, lnis.outbound.get(0).getElement(2) & 2);
        f2 = true;
        instance.setF2(f2);
        Assert.assertEquals(f2, instance.getF2());
        Assert.assertEquals("number of messages is 2", 2, lnis.outbound.size());
        Assert.assertEquals("opcode is OPC_LOCO_DIRF", LnConstants.OPC_LOCO_DIRF, lnis.outbound.get(1).getOpCode());
        Assert.assertEquals("sent f2 in correct state", 2, lnis.outbound.get(1).getElement(2) & 2);
    }

    /**
     * Test of setF3 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF3() {
        lnis.outbound.clear();
        lnis.resetStatistics();
        boolean f3 = false;
        instance.setF3(f3);
        Assert.assertEquals(f3, instance.getF3());
        Assert.assertEquals("number of messages is 1", 1, lnis.outbound.size());
        Assert.assertEquals("opcode is OPC_LOCO_DIRF", LnConstants.OPC_LOCO_DIRF, lnis.outbound.get(0).getOpCode());
        Assert.assertEquals("sent f2 in correct state", 0, lnis.outbound.get(0).getElement(2) & 4);
        f3 = true;
        instance.setF3(f3);
        Assert.assertEquals(f3, instance.getF3());
        Assert.assertEquals("number of messages is 2", 2, lnis.outbound.size());
        Assert.assertEquals("opcode is OPC_LOCO_DIRF", LnConstants.OPC_LOCO_DIRF, lnis.outbound.get(1).getOpCode());
        Assert.assertEquals("sent f3 in correct state", 4, lnis.outbound.get(1).getElement(2) & 4);
    }

    /**
     * Test of setF4 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF4() {
        lnis.outbound.clear();
        lnis.resetStatistics();
        boolean f4 = false;
        instance.setF4(f4);
        Assert.assertEquals(f4, instance.getF4());
        Assert.assertEquals("number of messages is 1", 1, lnis.outbound.size());
        Assert.assertEquals("opcode is OPC_LOCO_DIRF", LnConstants.OPC_LOCO_DIRF, lnis.outbound.get(0).getOpCode());
        Assert.assertEquals("sent f4 in correct state", 0, lnis.outbound.get(0).getElement(2) & 8);
        f4 = true;
        instance.setF4(f4);
        Assert.assertEquals(f4, instance.getF4());
        Assert.assertEquals("number of messages is 2", 2, lnis.outbound.size());
        Assert.assertEquals("opcode is OPC_LOCO_DIRF", LnConstants.OPC_LOCO_DIRF, lnis.outbound.get(1).getOpCode());
        Assert.assertEquals("sent f4 in correct state", 8, lnis.outbound.get(1).getElement(2) & 8);
    }

    /**
     * Test of setF5 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF5() {
        lnis.outbound.clear();
        lnis.resetStatistics();
        boolean f5 = false;
        instance.setF5(f5);
        Assert.assertEquals(f5, instance.getF5());
        Assert.assertEquals("number of messages is 1", 1, lnis.outbound.size());
        Assert.assertEquals("opcode is OPC_LOCO_SND", LnConstants.OPC_LOCO_SND, lnis.outbound.get(0).getOpCode());
        Assert.assertEquals("sent f5 in correct state", 0, lnis.outbound.get(0).getElement(2) & 1);
        f5 = true;
        instance.setF5(f5);
        Assert.assertEquals(f5, instance.getF5());
        Assert.assertEquals("number of messages is 2", 2, lnis.outbound.size());
        Assert.assertEquals("opcode is OPC_LOCO_SND", LnConstants.OPC_LOCO_SND, lnis.outbound.get(1).getOpCode());
        Assert.assertEquals("sent f5 in correct state", 1, lnis.outbound.get(1).getElement(2) & 1);
    }

    /**
     * Test of setF6 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF6() {
        lnis.outbound.clear();
        lnis.resetStatistics();
        boolean f6 = false;
        instance.setF6(f6);
        Assert.assertEquals(f6, instance.getF6());
        Assert.assertEquals("number of messages is 1", 1, lnis.outbound.size());
        Assert.assertEquals("opcode is OPC_LOCO_SND", LnConstants.OPC_LOCO_SND, lnis.outbound.get(0).getOpCode());
        Assert.assertEquals("sent f6 in correct state", 0, lnis.outbound.get(0).getElement(2) & 2);
        f6 = true;
        instance.setF6(f6);
        Assert.assertEquals(f6, instance.getF6());
        Assert.assertEquals("number of messages is 2", 2, lnis.outbound.size());
        Assert.assertEquals("opcode is OPC_LOCO_SND", LnConstants.OPC_LOCO_SND, lnis.outbound.get(1).getOpCode());
        Assert.assertEquals("sent f6 in correct state", 2, lnis.outbound.get(1).getElement(2) & 2);
    }

    /**
     * Test of setF7 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF7() {
        lnis.outbound.clear();
        lnis.resetStatistics();
        boolean f7 = false;
        instance.setF7(f7);
        Assert.assertEquals(f7, instance.getF7());
        Assert.assertEquals("number of messages is 1", 1, lnis.outbound.size());
        Assert.assertEquals("opcode is OPC_LOCO_SND", LnConstants.OPC_LOCO_SND, lnis.outbound.get(0).getOpCode());
        Assert.assertEquals("sent f7 in correct state", 0, lnis.outbound.get(0).getElement(2) & 4);
        f7 = true;
        instance.setF7(f7);
        Assert.assertEquals(f7, instance.getF7());
        Assert.assertEquals("number of messages is 2", 2, lnis.outbound.size());
        Assert.assertEquals("opcode is OPC_LOCO_SND", LnConstants.OPC_LOCO_SND, lnis.outbound.get(1).getOpCode());
        Assert.assertEquals("sent f7 in correct state", 4, lnis.outbound.get(1).getElement(2) & 4);
    }

    /**
     * Test of setF8 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF8() {
        lnis.outbound.clear();
        lnis.resetStatistics();
        boolean f8 = false;
        instance.setF8(f8);
        Assert.assertEquals(f8, instance.getF8());
        Assert.assertEquals("number of messages is 1", 1, lnis.outbound.size());
        Assert.assertEquals("opcode is OPC_LOCO_SND", LnConstants.OPC_LOCO_SND, lnis.outbound.get(0).getOpCode());
        Assert.assertEquals("sent f8 in correct state", 0, lnis.outbound.get(0).getElement(2) & 8);
        f8 = true;
        instance.setF8(f8);
        Assert.assertEquals(f8, instance.getF8());
        Assert.assertEquals("number of messages is 2", 2, lnis.outbound.size());
        Assert.assertEquals("opcode is OPC_LOCO_SND", LnConstants.OPC_LOCO_SND, lnis.outbound.get(1).getOpCode());
        Assert.assertEquals("sent f8 in correct state", 8, lnis.outbound.get(1).getElement(2) & 8);
    }

    /**
     * Test of setF9 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF9() {
        boolean f9 = false;
        instance.setF9(f9);
        Assert.assertEquals(f9, instance.getF9());
        f9 = true;
        instance.setF9(f9);
        Assert.assertEquals(f9, instance.getF9());
    }

    /**
     * Test of setF10 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF10() {
        boolean f10 = false;
        instance.setF10(f10);
        Assert.assertEquals(f10, instance.getF10());
        f10 = true;
        instance.setF10(f10);
        Assert.assertEquals(f10, instance.getF10());
    }

    /**
     * Test of setF11 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF11() {
        boolean f11 = false;
        instance.setF11(f11);
        Assert.assertEquals(f11, instance.getF11());
        f11 = true;
        instance.setF11(f11);
        Assert.assertEquals(f11, instance.getF11());
    }

    /**
     * Test of setF12 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF12() {
        boolean f12 = false;
        instance.setF12(f12);
        Assert.assertEquals(f12, instance.getF12());
        f12 = true;
        instance.setF12(f12);
        Assert.assertEquals(f12, instance.getF12());
    }

    /**
     * Test of setF13 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF13() {
        boolean f13 = false;
        instance.setF13(f13);
        Assert.assertEquals(f13, instance.getF13());
        f13 = true;
        instance.setF13(f13);
        Assert.assertEquals(f13, instance.getF13());
    }

    /**
     * Test of setF14 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF14() {
        boolean f14 = false;
        instance.setF14(f14);
        Assert.assertEquals(f14, instance.getF14());
        f14 = true;
        instance.setF14(f14);
        Assert.assertEquals(f14, instance.getF14());
    }

    /**
     * Test of setF15 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF15() {
        boolean f15 = false;
        instance.setF15(f15);
        Assert.assertEquals(f15, instance.getF15());
        f15 = true;
        instance.setF15(f15);
        Assert.assertEquals(f15, instance.getF15());
    }

    /**
     * Test of setF16 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF16() {
        boolean f16 = false;
        instance.setF16(f16);
        Assert.assertEquals(f16, instance.getF16());
        f16 = true;
        instance.setF16(f16);
        Assert.assertEquals(f16, instance.getF16());
    }

    /**
     * Test of setF17 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF17() {
        boolean f17 = false;
        instance.setF17(f17);
        Assert.assertEquals(f17, instance.getF17());
        f17 = true;
        instance.setF17(f17);
        Assert.assertEquals(f17, instance.getF17());
    }

    /**
     * Test of setF18 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF18() {
        boolean f18 = false;
        instance.setF18(f18);
        Assert.assertEquals(f18, instance.getF18());
        f18 = true;
        instance.setF18(f18);
        Assert.assertEquals(f18, instance.getF18());
    }

    /**
     * Test of setF19 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF19() {
        boolean f19 = false;
        instance.setF19(f19);
        Assert.assertEquals(f19, instance.getF19());
        f19 = true;
        instance.setF19(f19);
        Assert.assertEquals(f19, instance.getF19());
    }

    /**
     * Test of setF20 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF20() {
        boolean f20 = false;
        instance.setF20(f20);
        Assert.assertEquals(f20, instance.getF20());
        f20 = true;
        instance.setF20(f20);
        Assert.assertEquals(f20, instance.getF20());
    }

    /**
     * Test of setF21 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF21() {
        boolean f21 = false;
        instance.setF21(f21);
        Assert.assertEquals(f21, instance.getF21());
        f21 = true;
        instance.setF21(f21);
        Assert.assertEquals(f21, instance.getF21());
    }

    /**
     * Test of setF22 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF22() {
        boolean f22 = false;
        instance.setF22(f22);
        Assert.assertEquals(f22, instance.getF22());
        f22 = true;
        instance.setF22(f22);
        Assert.assertEquals(f22, instance.getF22());
    }

    /**
     * Test of setF23 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF23() {
        boolean f23 = false;
        instance.setF23(f23);
        Assert.assertEquals(f23, instance.getF23());
        f23 = true;
        instance.setF23(f23);
        Assert.assertEquals(f23, instance.getF23());
    }

    /**
     * Test of setF24 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF24() {
        boolean f24 = false;
        instance.setF24(f24);
        Assert.assertEquals(f24, instance.getF24());
        f24 = true;
        instance.setF24(f24);
        Assert.assertEquals(f24, instance.getF24());
    }

    /**
     * Test of setF25 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF25() {
        boolean f25 = false;
        instance.setF25(f25);
        Assert.assertEquals(f25, instance.getF25());
        f25 = true;
        instance.setF25(f25);
        Assert.assertEquals(f25, instance.getF25());
    }

    /**
     * Test of setF26 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF26() {
        boolean f26 = false;
        instance.setF26(f26);
        Assert.assertEquals(f26, instance.getF26());
        f26 = true;
        instance.setF26(f26);
        Assert.assertEquals(f26, instance.getF26());
    }

    /**
     * Test of setF27 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF27() {
        boolean f27 = false;
        instance.setF27(f27);
        Assert.assertEquals(f27, instance.getF27());
        f27 = true;
        instance.setF27(f27);
        Assert.assertEquals(f27, instance.getF27());
    }

    /**
     * Test of setF28 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF28() {
        boolean f28 = false;
        instance.setF28(f28);
        Assert.assertEquals(f28, instance.getF28());
        f28 = true;
        instance.setF28(f28);
        Assert.assertEquals(f28, instance.getF28());
    }

    /**
     * Test of sendFunctionGroup1 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSendFunctionGroup1() {
        lnis.outbound.clear();
        lnis.resetStatistics();
        instance.setF0(false);
        instance.setF1(true);
        instance.setF2(true);
        instance.setF3(false);
        instance.setIsForward(true);
        lnis.outbound.clear();
        lnis.resetStatistics();
        Assert.assertEquals("check send of function group 1 (0)", 0, lnis.outbound.size());
        ((LocoNetThrottle)instance).sendFunctionGroup1();
        Assert.assertEquals("check send of function group 1 (1)", 1, lnis.outbound.size());
        Assert.assertEquals("check opcode",LnConstants.OPC_LOCO_DIRF, lnis.outbound.get(0).getOpCode());
        Assert.assertEquals("check dirf byte", 0x03, lnis.outbound.get(0).getElement(2));

        lnis.outbound.clear();
        lnis.resetStatistics();
        instance.setIsForward(false);
        Assert.assertEquals("check send of function group 1 (2)", 1, lnis.outbound.size());
        ((LocoNetThrottle)instance).sendFunctionGroup1();
        Assert.assertEquals("check send of function group 1 (3)", 2, lnis.outbound.size());
        Assert.assertEquals("check opcode",LnConstants.OPC_LOCO_DIRF, lnis.outbound.get(1).getOpCode());
        Assert.assertEquals("check dirf byte {4}", 0x023, lnis.outbound.get(1).getElement(2));

    }

    /**
     * Test of sendFunctionGroup2 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSendFunctionGroup2() {

        for (int i = 5; i <9; ++i ) {
            instance.setF5(i==5);
            instance.setF6(i==6);
            instance.setF7(i==7);
            instance.setF8(i==8);
            lnis.outbound.clear();
            lnis.resetStatistics();
            ((LocoNetThrottle)instance).sendFunctionGroup2();

            Assert.assertEquals("check send of function group 2 for F"+i+" (0)", 1, lnis.outbound.size());
            Assert.assertEquals("check opcode is OPC_LOCO_SND for F"+i+"",LnConstants.OPC_LOCO_SND, lnis.outbound.get(0).getOpCode());
            Assert.assertEquals("check byte 2 for F"+i+"{0}", 1<<(i-5), lnis.outbound.get(0).getElement(2));

            instance.setF5(!(i==5));
            instance.setF6(!(i==6));
            instance.setF7(!(i==7));
            instance.setF8(!(i==8));
            lnis.outbound.clear();
            lnis.resetStatistics();
            ((LocoNetThrottle)instance).sendFunctionGroup2();

            Assert.assertEquals("check send of function group 2 for !F"+i+"(1)", 1, lnis.outbound.size());
            Assert.assertEquals("check opcode is OPC_LOCO_SND for F"+i+"",LnConstants.OPC_LOCO_SND, lnis.outbound.get(0).getOpCode());
            Assert.assertEquals("check byte 2 for !F"+i+"{1}", 0x0f - (1<<(i-5)), lnis.outbound.get(0).getElement(2));
        }
    }

    /**
     * Test of sendFunctionGroup3 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSendFunctionGroup3() {

        for (int i = 9; i <13; ++i ) {
            instance.setF9(i==9);
            instance.setF10(i==10);
            instance.setF11(i==11);
            instance.setF12(i==12);
            lnis.outbound.clear();
            lnis.resetStatistics();
            ((LocoNetThrottle)instance).sendFunctionGroup3();

            Assert.assertEquals("check send of function group 3 for F"+i+" (0)", 1, lnis.outbound.size());
            Assert.assertEquals("check opcode is OPC_IMM_PACKET for F"+i+"",LnConstants.OPC_IMM_PACKET, lnis.outbound.get(0).getOpCode());
            Assert.assertEquals("check byte 1 for F"+i+"{0}", 0x0b, lnis.outbound.get(0).getElement(1));
            Assert.assertEquals("check byte 2 for F"+i+"{0}", 0x7f, lnis.outbound.get(0).getElement(2));
            Assert.assertEquals("check byte 3 for F"+i+"{0}", 0x23, lnis.outbound.get(0).getElement(3));
            Assert.assertEquals("check byte 4 for F"+i+"{0}", 0x02, lnis.outbound.get(0).getElement(4));
            Assert.assertEquals("check byte 5 for F"+i+"{0}", 0x00, lnis.outbound.get(0).getElement(5));
            Assert.assertEquals("check byte 6 for F"+i+"{0}", 0x20+(1<<(i-9)), lnis.outbound.get(0).getElement(6));
            Assert.assertEquals("check byte 7 for F"+i+"{0}", 0x00, lnis.outbound.get(0).getElement(7));
            Assert.assertEquals("check byte 8 for F"+i+"{0}", 0x00, lnis.outbound.get(0).getElement(8));
            Assert.assertEquals("check byte 9 for F"+i+"{0}", 0x00, lnis.outbound.get(0).getElement(9));

            instance.setF9(!(i==9));
            instance.setF10(!(i==10));
            instance.setF11(!(i==11));
            instance.setF12(!(i==12));
            lnis.outbound.clear();
            lnis.resetStatistics();
            ((LocoNetThrottle)instance).sendFunctionGroup3();

            Assert.assertEquals("check send of function group 3 for !F"+i+"(1)", 1, lnis.outbound.size());
            Assert.assertEquals("check opcode is OPC_IMM_PACKET for !F"+i+"{1}",LnConstants.OPC_IMM_PACKET, lnis.outbound.get(0).getOpCode());
            Assert.assertEquals("check byte 1 for !F"+i+"{1}", 0x0b, lnis.outbound.get(0).getElement(1));
            Assert.assertEquals("check byte 2 for !F"+i+"{1}", 0x7f, lnis.outbound.get(0).getElement(2));
            Assert.assertEquals("check byte 3 for !F"+i+"{1}", 0x23, lnis.outbound.get(0).getElement(3));
            Assert.assertEquals("check byte 4 for !F"+i+"{1}", 0x02, lnis.outbound.get(0).getElement(4));
            Assert.assertEquals("check byte 5 for !F"+i+"{1}", 0x00, lnis.outbound.get(0).getElement(5));
            Assert.assertEquals("check byte 6 for !F"+i+"{1}", 0x2F-(1<<(i-9)), lnis.outbound.get(0).getElement(6));
            Assert.assertEquals("check byte 7 for !F"+i+"{1}", 0x00, lnis.outbound.get(0).getElement(7));
            Assert.assertEquals("check byte 8 for !F"+i+"{1}", 0x00, lnis.outbound.get(0).getElement(8));
            Assert.assertEquals("check byte 9 for 1F"+i+"{1}", 0x00, lnis.outbound.get(0).getElement(9));
        }
    }

    /**
     * Test of sendFunctionGroup4 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSendFunctionGroup4() {
        for (int i = 13; i <21; ++i ) {
            instance.setF13(i==13);
            instance.setF14(i==14);
            instance.setF15(i==15);
            instance.setF16(i==16);
            instance.setF17(i==17);
            instance.setF18(i==18);
            instance.setF19(i==19);
            instance.setF20(i==20);
            lnis.outbound.clear();
            lnis.resetStatistics();
            ((LocoNetThrottle)instance).sendFunctionGroup4();

            Assert.assertEquals("check send of function group 4 for F"+i+" (0)", 1, lnis.outbound.size());
            Assert.assertEquals("check opcode is OPC_IMM_PACKET for F"+i+"",LnConstants.OPC_IMM_PACKET, lnis.outbound.get(0).getOpCode());
            Assert.assertEquals("check byte 1 for F"+i+"{0}", 0x0b, lnis.outbound.get(0).getElement(1));
            Assert.assertEquals("check byte 2 for F"+i+"{0}", 0x7f, lnis.outbound.get(0).getElement(2));
            Assert.assertEquals("check byte 3 for F"+i+"{0}", 0x33, lnis.outbound.get(0).getElement(3));
            Assert.assertEquals("check byte 4 for F"+i+"{0}", (i==20)?0x06:0x02, lnis.outbound.get(0).getElement(4));
            Assert.assertEquals("check byte 5 for F"+i+"{0}", 0x00, lnis.outbound.get(0).getElement(5));
            Assert.assertEquals("check byte 6 for F"+i+"{0}", 0x5e, lnis.outbound.get(0).getElement(6));
            Assert.assertEquals("check byte 7 for F"+i+"{0}", (i < 20)?(1<<(i-13)):0, lnis.outbound.get(0).getElement(7));
            Assert.assertEquals("check byte 8 for F"+i+"{0}", 0x00, lnis.outbound.get(0).getElement(8));
            Assert.assertEquals("check byte 9 for F"+i+"{0}", 0x00, lnis.outbound.get(0).getElement(9));

            instance.setF13(!(i==13));
            instance.setF14(!(i==14));
            instance.setF15(!(i==15));
            instance.setF16(!(i==16));
            instance.setF17(!(i==17));
            instance.setF18(!(i==18));
            instance.setF19(!(i==19));
            instance.setF20(!(i==20));
            lnis.outbound.clear();
            lnis.resetStatistics();
            ((LocoNetThrottle)instance).sendFunctionGroup4();

            Assert.assertEquals("check send of function group 4 for !F"+i+"(1)", 1, lnis.outbound.size());
            Assert.assertEquals("check opcode is OPC_IMM_PACKET for !F"+i+"{1}",LnConstants.OPC_IMM_PACKET, lnis.outbound.get(0).getOpCode());
            Assert.assertEquals("check byte 1 for !F"+i+"{1}", 0x0b, lnis.outbound.get(0).getElement(1));
            Assert.assertEquals("check byte 2 for !F"+i+"{1}", 0x7f, lnis.outbound.get(0).getElement(2));
            Assert.assertEquals("check byte 3 for !F"+i+"{1}", 0x33, lnis.outbound.get(0).getElement(3));
            Assert.assertEquals("check byte 4 for !F"+i+"{1}", (i==20)?0x02:0x06, lnis.outbound.get(0).getElement(4));
            Assert.assertEquals("check byte 5 for !F"+i+"{1}", 0x00, lnis.outbound.get(0).getElement(5));
            Assert.assertEquals("check byte 6 for !F"+i+"{1}", 0x5e, lnis.outbound.get(0).getElement(6));
            Assert.assertEquals("check byte 7 for !F"+i+"{1}", (i < 20)?(127-(1<<(i-13))):0x7f, lnis.outbound.get(0).getElement(7));
            Assert.assertEquals("check byte 8 for !F"+i+"{1}", 0x00, lnis.outbound.get(0).getElement(8));
            Assert.assertEquals("check byte 9 for 1F"+i+"{1}", 0x00, lnis.outbound.get(0).getElement(9));
        }
    }

    /**
     * Test of sendFunctionGroup5 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSendFunctionGroup5() {
        for (int i = 21; i <29; ++i ) {
            instance.setF21(i==21);
            instance.setF22(i==22);
            instance.setF23(i==23);
            instance.setF24(i==24);
            instance.setF25(i==25);
            instance.setF26(i==26);
            instance.setF27(i==27);
            instance.setF28(i==28);
            lnis.outbound.clear();
            lnis.resetStatistics();
            ((LocoNetThrottle)instance).sendFunctionGroup5();

            Assert.assertEquals("check send of function group 5 for F"+i+" (0)", 1, lnis.outbound.size());
            Assert.assertEquals("check opcode is OPC_IMM_PACKET for F"+i+"",LnConstants.OPC_IMM_PACKET, lnis.outbound.get(0).getOpCode());
            Assert.assertEquals("check byte 1 for F"+i+"{0}", 0x0b, lnis.outbound.get(0).getElement(1));
            Assert.assertEquals("check byte 2 for F"+i+"{0}", 0x7f, lnis.outbound.get(0).getElement(2));
            Assert.assertEquals("check byte 3 for F"+i+"{0}", 0x33, lnis.outbound.get(0).getElement(3));
            Assert.assertEquals("check byte 4 for F"+i+"{0}", (i==28)?0x06:0x02, lnis.outbound.get(0).getElement(4));
            Assert.assertEquals("check byte 5 for F"+i+"{0}", 0x00, lnis.outbound.get(0).getElement(5));
            Assert.assertEquals("check byte 6 for F"+i+"{0}", 0x5f, lnis.outbound.get(0).getElement(6));
            Assert.assertEquals("check byte 7 for F"+i+"{0}", (i < 28)?(1<<(i-21)):0, lnis.outbound.get(0).getElement(7));
            Assert.assertEquals("check byte 8 for F"+i+"{0}", 0x00, lnis.outbound.get(0).getElement(8));
            Assert.assertEquals("check byte 9 for F"+i+"{0}", 0x00, lnis.outbound.get(0).getElement(9));

            instance.setF21(!(i==21));
            instance.setF22(!(i==22));
            instance.setF23(!(i==23));
            instance.setF24(!(i==24));
            instance.setF25(!(i==25));
            instance.setF26(!(i==26));
            instance.setF27(!(i==27));
            instance.setF28(!(i==28));
            lnis.outbound.clear();
            lnis.resetStatistics();
            ((LocoNetThrottle)instance).sendFunctionGroup5();

            Assert.assertEquals("check send of function group 5 for !F"+i+"(1)", 1, lnis.outbound.size());
            Assert.assertEquals("check opcode is OPC_IMM_PACKET for !F"+i+"{1}",LnConstants.OPC_IMM_PACKET, lnis.outbound.get(0).getOpCode());
            Assert.assertEquals("check byte 1 for !F"+i+"{1}", 0x0b, lnis.outbound.get(0).getElement(1));
            Assert.assertEquals("check byte 2 for !F"+i+"{1}", 0x7f, lnis.outbound.get(0).getElement(2));
            Assert.assertEquals("check byte 3 for !F"+i+"{1}", 0x33, lnis.outbound.get(0).getElement(3));
            Assert.assertEquals("check byte 4 for !F"+i+"{1}", (i==28)?0x02:0x06, lnis.outbound.get(0).getElement(4));
            Assert.assertEquals("check byte 5 for !F"+i+"{1}", 0x00, lnis.outbound.get(0).getElement(5));
            Assert.assertEquals("check byte 6 for !F"+i+"{1}", 0x5f, lnis.outbound.get(0).getElement(6));
            Assert.assertEquals("check byte 7 for !F"+i+"{1}", (i < 28)?(127-(1<<(i-21))):0x7f, lnis.outbound.get(0).getElement(7));
            Assert.assertEquals("check byte 8 for !F"+i+"{1}", 0x00, lnis.outbound.get(0).getElement(8));
            Assert.assertEquals("check byte 9 for 1F"+i+"{1}", 0x00, lnis.outbound.get(0).getElement(9));

        }
    }

    /**
     * Test of getF2Momentary method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testGetF2Momentary() {
        boolean expResult = true;
        boolean result = instance.getF2Momentary();
        Assert.assertEquals("Check F2 Momentary true", expResult, result);

        expResult = false;
        instance.setF2Momentary(false);
        result = instance.getF2Momentary();
        Assert.assertEquals("Check F2 Momentary false", expResult, result);

    }

    private LocoNetInterfaceScaffold lnis;
    private SlotManager slotmanager;
    private LocoNetSystemConnectionMemo memo = null;

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        // prepare an interface
        lnis = new LocoNetInterfaceScaffold();
        slotmanager = new SlotManager(lnis);

        // set slot 3 to address 3
        LocoNetMessage m = new LocoNetMessage(13);
        m.setOpCode(LnConstants.OPC_WR_SL_DATA);
        m.setElement(1, 0x0E);
        m.setElement(2, 0x03);
        m.setElement(4, 0x03);
        slotmanager.slot(3).setSlot(m);

        // set slot 4 to address 255
        m.setElement(2, 0x04);
        m.setElement(4, 0x7F);
        m.setElement(9, 0x01);
        slotmanager.slot(4).setSlot(m);

        memo = new LocoNetSystemConnectionMemo(lnis, slotmanager);
        memo.setThrottleManager(new LnThrottleManager(memo));
        jmri.InstanceManager.setDefault(jmri.ThrottleManager.class, memo.getThrottleManager());

        instance = new LocoNetThrottle(memo, new LocoNetSlot(0));
    }

    @After
    @Override
    public void tearDown() {
        ((LnThrottleManager)memo.getThrottleManager()).dispose();
        memo.dispose();
        lnis = null;
        JUnitUtil.tearDown();
    }

}
