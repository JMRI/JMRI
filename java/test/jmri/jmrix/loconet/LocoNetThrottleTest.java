package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


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
        int expResult = 2;
        int result = instance.getSpeedStepMode();
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
    @Ignore("Speed steps on LocoNet are off. 1.0F reports back as speed step 124, not 127 as expected.  Speed step for 0.007874016f reports as speed step 12, not 2 as expected.")
    public void testGetSpeed_float() {
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
        boolean f0 = false;
        instance.setF0(f0);
    }

    /**
     * Test of setF1 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF1() {
        boolean f1 = false;
        instance.setF1(f1);
    }

    /**
     * Test of setF2 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF2() {
        boolean f2 = false;
        instance.setF2(f2);
    }

    /**
     * Test of setF3 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF3() {
        boolean f3 = false;
        instance.setF3(f3);
    }

    /**
     * Test of setF4 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF4() {
        boolean f4 = false;
        instance.setF4(f4);
    }

    /**
     * Test of setF5 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF5() {
        boolean f5 = false;
        instance.setF5(f5);
    }

    /**
     * Test of setF6 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF6() {
        boolean f6 = false;
        instance.setF6(f6);
    }

    /**
     * Test of setF7 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF7() {
        boolean f7 = false;
        instance.setF7(f7);
    }

    /**
     * Test of setF8 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF8() {
        boolean f8 = false;
        instance.setF8(f8);
    }

    /**
     * Test of setF9 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF9() {
        boolean f9 = false;
        instance.setF9(f9);
    }

    /**
     * Test of setF10 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF10() {
        boolean f10 = false;
        instance.setF10(f10);
    }

    /**
     * Test of setF11 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF11() {
        boolean f11 = false;
        instance.setF11(f11);
    }

    /**
     * Test of setF12 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF12() {
        boolean f12 = false;
        instance.setF12(f12);
    }

    /**
     * Test of setF13 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF13() {
        boolean f13 = false;
        instance.setF13(f13);
    }

    /**
     * Test of setF14 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF14() {
        boolean f14 = false;
        instance.setF14(f14);
    }

    /**
     * Test of setF15 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF15() {
        boolean f15 = false;
        instance.setF15(f15);
    }

    /**
     * Test of setF16 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF16() {
        boolean f16 = false;
        instance.setF16(f16);
    }

    /**
     * Test of setF17 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF17() {
        boolean f17 = false;
        instance.setF17(f17);
    }

    /**
     * Test of setF18 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF18() {
        boolean f18 = false;
        instance.setF18(f18);
    }

    /**
     * Test of setF19 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF19() {
        boolean f19 = false;
        instance.setF19(f19);
    }

    /**
     * Test of setF20 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF20() {
        boolean f20 = false;
        instance.setF20(f20);
    }

    /**
     * Test of setF21 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF21() {
        boolean f21 = false;
        instance.setF21(f21);
    }

    /**
     * Test of setF22 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF22() {
        boolean f22 = false;
        instance.setF22(f22);
    }

    /**
     * Test of setF23 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF23() {
        boolean f23 = false;
        instance.setF23(f23);
    }

    /**
     * Test of setF24 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF24() {
        boolean f24 = false;
        instance.setF24(f24);
    }

    /**
     * Test of setF25 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF25() {
        boolean f25 = false;
        instance.setF25(f25);
    }

    /**
     * Test of setF26 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF26() {
        boolean f26 = false;
        instance.setF26(f26);
    }

    /**
     * Test of setF27 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF27() {
        boolean f27 = false;
        instance.setF27(f27);
    }

    /**
     * Test of setF28 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF28() {
        boolean f28 = false;
        instance.setF28(f28);
    }

    /**
     * Test of sendFunctionGroup1 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSendFunctionGroup1() {
    }

    /**
     * Test of sendFunctionGroup2 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSendFunctionGroup2() {
    }

    /**
     * Test of sendFunctionGroup3 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSendFunctionGroup3() {
    }

    /**
     * Test of sendFunctionGroup4 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSendFunctionGroup4() {
    }

    /**
     * Test of sendFunctionGroup5 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSendFunctionGroup5() {
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

        memo = new LocoNetSystemConnectionMemo(lnis,slotmanager);
        memo.setThrottleManager(new LnThrottleManager(memo));
        jmri.InstanceManager.setDefault(jmri.ThrottleManager.class,memo.getThrottleManager());

        instance = new LocoNetThrottle(memo, new LocoNetSlot(0));
    }

    @After
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
