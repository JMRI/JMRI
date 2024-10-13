package jmri.jmrix.ecos;

import java.awt.GraphicsEnvironment;
import java.io.File;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.Assert;

import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.SpeedStepMode;
import jmri.ThrottleManager;
import jmri.jmrix.AbstractThrottleTest;
import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.ToDo;

import org.junit.jupiter.api.io.TempDir;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class EcosDccThrottleTest extends AbstractThrottleTest {
        
    private EcosSystemConnectionMemo memo = null;
    private static final int MAX_FUNCTIONS = 32;

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists", instance);
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
        SpeedStepMode expResult = SpeedStepMode.NMRA_DCC_128;
        SpeedStepMode result = instance.getSpeedStepMode();
        Assert.assertEquals(expResult, result);
    }
    
    /**
     * Test of getSpeedIncrement method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testGetSpeedIncrement() {
        float expResult = SpeedStepMode.NMRA_DCC_128.increment;
        float result = instance.getSpeedIncrement();
        Assert.assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of setSpeedSetting method, of class AbstractThrottle.
     */
    @Test
    @Disabled("needs response to see change?")
    @ToDo("investigate what response needs to be sent to throttle after setSpeedSetting is called before the assert")
    @Override
    public void testSetSpeedSetting() {
        float speed = 1.0F;
        instance.setSpeedSetting(speed);
        Assert.assertEquals(speed, instance.getSpeedSetting(), 0.0);
    }
    
    @Test
    @Disabled("_haveControl boolean not true?")
    @ToDo("investigate what response needs to be sent to throttle after setSpeedSetting is called before the assert")
    @Override
    public void testLogsSpeedToBasicRosterEntry (@TempDir File folder) throws java.io.IOException {
        super.testLogsSpeedToBasicRosterEntry(folder);
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
    
    @Test
    public void testSetGetF29(){
        instance.setFunction(29, true);
        Assert.assertTrue(instance.getFunction(29));
        instance.setFunction(29, false);
        Assert.assertFalse(instance.getFunction(29));
    }
    
    @Test
    public void testSetGetF30(){
        instance.setFunction(30, true);
        Assert.assertTrue(instance.getFunction(30));
        instance.setFunction(30, false);
        Assert.assertFalse(instance.getFunction(30));
    }
    
    @Test
    public void testSetGetF31(){
        instance.setFunction(31, true);
        Assert.assertTrue(instance.getFunction(31));
        instance.setFunction(31, false);
        Assert.assertFalse(instance.getFunction(31));
    }
    
    @Test
    public void testUpdateFunction0(){
        instance.updateFunction(0, true);
        Assert.assertTrue(instance.getFunction(0));
        instance.updateFunction(0, false);
        Assert.assertFalse(instance.getFunction(0));
    }
    
    @Test
    public void testUpdateFunction1(){
        instance.updateFunction(1, true);
        Assert.assertTrue(instance.getFunction(1));
        instance.updateFunction(1, false);
        Assert.assertFalse(instance.getFunction(1));
    }
    
    @Test
    public void testUpdateFunction29(){
        instance.updateFunction(29, true);
        Assert.assertTrue(instance.getFunction(29));
        instance.updateFunction(29, false);
        Assert.assertFalse(instance.getFunction(29));
    }
    
    @Test
    public void testUpdateFunction30(){
        instance.updateFunction(30, true);
        Assert.assertTrue(instance.getFunction(30));
        instance.updateFunction(30, false);
        Assert.assertFalse(instance.getFunction(30));
    }
    
    @Test
    public void testUpdateFunction31(){
        instance.updateFunction(31, true);
        Assert.assertTrue(instance.getFunction(31));
        instance.updateFunction(31, false);
        Assert.assertFalse(instance.getFunction(31));
    }
    
    /*
     * Ecos setFunction() calls updateFunction() so we need to keep this override to
     * assert the unhandled update rather than unhandled set message
    */
    @Test
    @Override
    public void testOutOfRangeSetFunction(){
        
        instance.setFunction(-1, true);
        jmri.util.JUnitAppender.assertWarnMessageStartingWith("Unhandled update function number: -1");
        
        instance.setFunction(32, true);
        jmri.util.JUnitAppender.assertWarnMessageStartingWith("Unhandled update function number: 32");
        
    }
    
    
    /**
     * Test of sendFunctionGroup1 method, of class AbstractThrottle.
     */
    @Test
    @Override
    @Disabled("Test requires further development")
    public void testSendFunctionGroup1() {
    }

    /**
     * Test of sendFunctionGroup2 method, of class AbstractThrottle.
     */
    @Test
    @Override
    @Disabled("Test requires further development")
    public void testSendFunctionGroup2() {
    }

    /**
     * Test of sendFunctionGroup3 method, of class AbstractThrottle.
     */
    @Test
    @Override
    @Disabled("Test requires further development")
    public void testSendFunctionGroup3() {
    }

    /**
     * Test of sendFunctionGroup4 method, of class AbstractThrottle.
     */
    @Test
    @Override
    @Disabled("Test requires further development")
    public void testSendFunctionGroup4() {
    }

    /**
     * Test of sendFunctionGroup5 method, of class AbstractThrottle.
     */
    @Test
    @Override
    @Disabled("Test requires further development")
    public void testSendFunctionGroup5() {
    }

    private static EcosTrafficController tc = null;

    @BeforeAll
    public static void earlySetup() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDefaultUserMessagePreferences();
        EcosSystemConnectionMemo memo = new EcosSystemConnectionMemo();
        tc = new EcosInterfaceScaffold();
        memo.setEcosTrafficController(tc);
        tc.setAdapterMemo(memo);
    }

    @BeforeEach
    @Override
    public void setUp() {
        if (!GraphicsEnvironment.isHeadless()) {
            memo = new EcosSystemConnectionMemo(tc) {
                @Override
                public EcosLocoAddressManager getLocoAddressManager() {
                    return new EcosLocoAddressManager(this);
                }

                @Override
                public EcosPreferences getPreferenceManager() {
                    return new EcosPreferences(this) {
                        @Override
                        public boolean getPreferencesLoaded() {
                            return true;
                        }
                    };
                }
            };
        } else {
            // GraphicsEnvironment.isHeadless()
            memo = new EcosSystemConnectionMemo(tc) {
                @Override
                public EcosLocoAddressManager getLocoAddressManager() {
                    store(new EcosLocoAddressManager(this),EcosLocoAddressManager.class);
                    return get(EcosLocoAddressManager.class);
                }

                @Override
                public EcosPreferences getPreferenceManager() {
                    return new EcosPreferences(this) {
                        @Override
                        public boolean getPreferencesLoaded() {
                            return true;
                        }
                        // don't ask any questions related to locos.

                        @Override
                        public int getAddLocoToEcos() {
                            return EcosPreferences.NO;
                        }

                        @Override
                        public int getAddLocoToJMRI() {
                            return EcosPreferences.NO;
                        }

                        @Override
                        public int getAdhocLocoFromEcos() {
                            return EcosPreferences.NO;
                        }

                        @Override
                        public int getForceControlFromEcos() {
                            return EcosPreferences.NO;
                        }

                        @Override
                        public int getRemoveLocoFromEcos() {
                            return EcosPreferences.NO;
                        }

                        @Override
                        public int getRemoveLocoFromJMRI() {
                            return EcosPreferences.NO;
                        }
                    };
                }
            };
        }
        InstanceManager.setDefault(ThrottleManager.class, new EcosDccThrottleManager(new EcosSystemConnectionMemo(tc)));
        instance = new EcosDccThrottle(new DccLocoAddress(100, true), memo, true);
        setMaxFns(MAX_FUNCTIONS);
    }

    @AfterEach
    @Override
    public void tearDown() {
        memo.getTrafficController().terminateThreads();
        memo.dispose();
        memo = null;
        JUnitUtil.clearShutDownManager(); // shutdown task left running
    }

    @AfterAll
    public static void finalTearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(EcosDccThrottleTest.class);
}
