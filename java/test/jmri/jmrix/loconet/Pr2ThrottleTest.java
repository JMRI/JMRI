package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class Pr2ThrottleTest extends jmri.jmrix.AbstractThrottleTest {

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",instance);
    }

    /**
     * {@inheritDoc}
     *
     * The default mode is 28 speed steps
     */
    @Test
    @Override
    public void testGetSpeedStepMode() {
        int expResult = jmri.DccThrottle.SpeedStepMode28;
        int result = instance.getSpeedStepMode();
        Assert.assertEquals(expResult, result);
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void testGetSpeedIncrement() {
        float expResult = 1.0F;
        float result = instance.getSpeedIncrement();
        Assert.assertEquals(expResult, result, 0.0);
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    @Test
    @Override
    public void testSetF0() {
        boolean f0 = false;
        instance.setF0(f0);
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void testSetF1() {
        boolean f1 = false;
        instance.setF1(f1);
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void testSetF2() {
        boolean f2 = false;
        instance.setF2(f2);
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void testSetF3() {
        boolean f3 = false;
        instance.setF3(f3);
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void testSetF4() {
        boolean f4 = false;
        instance.setF4(f4);
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void testSetF5() {
        boolean f5 = false;
        instance.setF5(f5);
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void testSetF6() {
        boolean f6 = false;
        instance.setF6(f6);
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void testSetF7() {
        boolean f7 = false;
        instance.setF7(f7);
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void testSetF8() {
        boolean f8 = false;
        instance.setF8(f8);
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void testSetF9() {
        boolean f9 = false;
        instance.setF9(f9);
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void testSetF10() {
        boolean f10 = false;
        instance.setF10(f10);
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void testSetF11() {
        boolean f11 = false;
        instance.setF11(f11);
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void testSetF12() {
        boolean f12 = false;
        instance.setF12(f12);
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void testSetF13() {
        boolean f13 = false;
        instance.setF13(f13);
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void testSetF14() {
        boolean f14 = false;
        instance.setF14(f14);
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void testSetF15() {
        boolean f15 = false;
        instance.setF15(f15);
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void testSetF16() {
        boolean f16 = false;
        instance.setF16(f16);
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void testSetF17() {
        boolean f17 = false;
        instance.setF17(f17);
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void testSetF18() {
        boolean f18 = false;
        instance.setF18(f18);
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void testSetF19() {
        boolean f19 = false;
        instance.setF19(f19);
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void testSetF20() {
        boolean f20 = false;
        instance.setF20(f20);
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void testSetF21() {
        boolean f21 = false;
        instance.setF21(f21);
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void testSetF22() {
        boolean f22 = false;
        instance.setF22(f22);
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void testSetF23() {
        boolean f23 = false;
        instance.setF23(f23);
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void testSetF24() {
        boolean f24 = false;
        instance.setF24(f24);
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void testSetF25() {
        boolean f25 = false;
        instance.setF25(f25);
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void testSetF26() {
        boolean f26 = false;
        instance.setF26(f26);
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void testSetF27() {
        boolean f27 = false;
        instance.setF27(f27);
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void testSetF28() {
        boolean f28 = false;
        instance.setF28(f28);
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void testSendFunctionGroup1() {
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void testSendFunctionGroup2() {
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void testSendFunctionGroup3() {
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void testSendFunctionGroup4() {
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void testSendFunctionGroup5() {
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void testRelease_0args() {
        instance.release();
        jmri.util.JUnitAppender.assertWarnMessage("Dispose called without knowing the original throttle listener");
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void testDispatch_0args() {
        instance.dispatch();
        jmri.util.JUnitAppender.assertWarnMessage("Dispose called without knowing the original throttle listener");
    }


    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        LnTrafficController lnis = new LocoNetInterfaceScaffold();
        SlotManager slotmanager = new SlotManager(lnis);
        LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo(lnis,slotmanager);
        jmri.InstanceManager.setDefault(jmri.ThrottleManager.class,new LnPr2ThrottleManager(memo));
        instance = new Pr2Throttle(memo,new jmri.DccLocoAddress(5,false));
    }

    @After
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(Pr2ThrottleTest.class);

}
