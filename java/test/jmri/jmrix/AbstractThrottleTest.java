package jmri.jmrix;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import jmri.BasicRosterEntry;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.SpeedStepMode;
import jmri.ThrottleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Randall Wood 2015
 */
public class AbstractThrottleTest {
        
    protected AbstractThrottle instance = null;

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        InstanceManager.setThrottleManager(new AbstractThrottleManager() {

            @Override
            public void requestThrottleSetup(LocoAddress a, boolean control) {
            }

            @Override
            public boolean canBeLongAddress(int address) {
                return true;
            }

            @Override
            public boolean canBeShortAddress(int address) {
                return true;
            }

            @Override
            public boolean addressTypeUnique() {
                return true;
            }
        });
        instance = new AbstractThrottleImpl();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

    /**
     * Test of getSpeedSetting method, of class AbstractThrottle.
     */
    @Test
    public void testGetSpeedSetting() {
        float expResult = 0.0F;
        float result = instance.getSpeedSetting();
        Assert.assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of setSpeedSettingAgain method, of class AbstractThrottle.
     */
    @Test
    public void testSetSpeedSettingAgain() {
        float speed = 1.0F;
        instance.setSpeedSettingAgain(speed);
        Assert.assertEquals(speed, instance.getSpeedSetting(), 0.0);
    }

    /**
     * Test of setSpeedSetting method, of class AbstractThrottle.
     */
    @Test
    public void testSetSpeedSetting() {
        float speed = 1.0F;
        instance.setSpeedSetting(speed);
        Assert.assertEquals(speed, instance.getSpeedSetting(), 0.0);
    }

    /**
     * Test of getIsForward method, of class AbstractThrottle.
     */
    @Test
    public void testGetIsForward() {
        boolean expResult = false;
        boolean result = instance.getIsForward();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of setIsForward method, of class AbstractThrottle.
     */
    @Test
    public void testSetIsForward() {
        boolean forward = false;
        instance.setIsForward(forward);
    }

    /**
     * Test of getF0 method, of class AbstractThrottle.
     */
    @Test
    public void testGetF0() {
        boolean expResult = false;
        boolean result = instance.getF0();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF1 method, of class AbstractThrottle.
     */
    @Test
    public void testGetF1() {
        boolean expResult = false;
        boolean result = instance.getF1();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF2 method, of class AbstractThrottle.
     */
    @Test
    public void testGetF2() {
        boolean expResult = false;
        boolean result = instance.getF2();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF3 method, of class AbstractThrottle.
     */
    @Test
    public void testGetF3() {
        boolean expResult = false;
        boolean result = instance.getF3();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF4 method, of class AbstractThrottle.
     */
    @Test
    public void testGetF4() {
        boolean expResult = false;
        boolean result = instance.getF4();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF5 method, of class AbstractThrottle.
     */
    @Test
    public void testGetF5() {
        boolean expResult = false;
        boolean result = instance.getF5();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF6 method, of class AbstractThrottle.
     */
    @Test
    public void testGetF6() {
        boolean expResult = false;
        boolean result = instance.getF6();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF7 method, of class AbstractThrottle.
     */
    @Test
    public void testGetF7() {
        boolean expResult = false;
        boolean result = instance.getF7();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF8 method, of class AbstractThrottle.
     */
    @Test
    public void testGetF8() {
        boolean expResult = false;
        boolean result = instance.getF8();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF9 method, of class AbstractThrottle.
     */
    @Test
    public void testGetF9() {
        boolean expResult = false;
        boolean result = instance.getF9();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF10 method, of class AbstractThrottle.
     */
    @Test
    public void testGetF10() {
        boolean expResult = false;
        boolean result = instance.getF10();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF11 method, of class AbstractThrottle.
     */
    @Test
    public void testGetF11() {
        boolean expResult = false;
        boolean result = instance.getF11();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF12 method, of class AbstractThrottle.
     */
    @Test
    public void testGetF12() {
        boolean expResult = false;
        boolean result = instance.getF12();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF13 method, of class AbstractThrottle.
     */
    @Test
    public void testGetF13() {
        boolean expResult = false;
        boolean result = instance.getF13();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF14 method, of class AbstractThrottle.
     */
    @Test
    public void testGetF14() {
        boolean expResult = false;
        boolean result = instance.getF14();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF15 method, of class AbstractThrottle.
     */
    @Test
    public void testGetF15() {
        boolean expResult = false;
        boolean result = instance.getF15();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF16 method, of class AbstractThrottle.
     */
    @Test
    public void testGetF16() {
        boolean expResult = false;
        boolean result = instance.getF16();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF17 method, of class AbstractThrottle.
     */
    @Test
    public void testGetF17() {
        boolean expResult = false;
        boolean result = instance.getF17();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF18 method, of class AbstractThrottle.
     */
    @Test
    public void testGetF18() {
        boolean expResult = false;
        boolean result = instance.getF18();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF19 method, of class AbstractThrottle.
     */
    @Test
    public void testGetF19() {
        boolean expResult = false;
        boolean result = instance.getF19();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF20 method, of class AbstractThrottle.
     */
    @Test
    public void testGetF20() {
        boolean expResult = false;
        boolean result = instance.getF20();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF21 method, of class AbstractThrottle.
     */
    @Test
    public void testGetF21() {
        boolean expResult = false;
        boolean result = instance.getF21();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF22 method, of class AbstractThrottle.
     */
    @Test
    public void testGetF22() {
        boolean expResult = false;
        boolean result = instance.getF22();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF23 method, of class AbstractThrottle.
     */
    @Test
    public void testGetF23() {
        boolean expResult = false;
        boolean result = instance.getF23();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF24 method, of class AbstractThrottle.
     */
    @Test
    public void testGetF24() {
        boolean expResult = false;
        boolean result = instance.getF24();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF25 method, of class AbstractThrottle.
     */
    @Test
    public void testGetF25() {
        boolean expResult = false;
        boolean result = instance.getF25();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF26 method, of class AbstractThrottle.
     */
    @Test
    public void testGetF26() {
        boolean expResult = false;
        boolean result = instance.getF26();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF27 method, of class AbstractThrottle.
     */
    @Test
    public void testGetF27() {
        boolean expResult = false;
        boolean result = instance.getF27();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF28 method, of class AbstractThrottle.
     */
    @Test
    public void testGetF28() {
        boolean expResult = false;
        boolean result = instance.getF28();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF0Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testGetF0Momentary() {
        boolean expResult = false;
        boolean result = instance.getF0Momentary();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF1Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testGetF1Momentary() {
        boolean expResult = false;
        boolean result = instance.getF1Momentary();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF2Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testGetF2Momentary() {
        boolean expResult = false;
        boolean result = instance.getF2Momentary();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF3Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testGetF3Momentary() {
        boolean expResult = false;
        boolean result = instance.getF3Momentary();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF4Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testGetF4Momentary() {
        boolean expResult = false;
        boolean result = instance.getF4Momentary();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF5Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testGetF5Momentary() {
        boolean expResult = false;
        boolean result = instance.getF5Momentary();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF6Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testGetF6Momentary() {
        boolean expResult = false;
        boolean result = instance.getF6Momentary();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF7Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testGetF7Momentary() {
        boolean expResult = false;
        boolean result = instance.getF7Momentary();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF8Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testGetF8Momentary() {
        boolean expResult = false;
        boolean result = instance.getF8Momentary();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF9Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testGetF9Momentary() {
        boolean expResult = false;
        boolean result = instance.getF9Momentary();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF10Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testGetF10Momentary() {
        boolean expResult = false;
        boolean result = instance.getF10Momentary();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF11Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testGetF11Momentary() {
        boolean expResult = false;
        boolean result = instance.getF11Momentary();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF12Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testGetF12Momentary() {
        boolean expResult = false;
        boolean result = instance.getF12Momentary();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF13Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testGetF13Momentary() {
        boolean expResult = false;
        boolean result = instance.getF13Momentary();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF14Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testGetF14Momentary() {
        boolean expResult = false;
        boolean result = instance.getF14Momentary();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF15Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testGetF15Momentary() {
        boolean expResult = false;
        boolean result = instance.getF15Momentary();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF16Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testGetF16Momentary() {
        boolean expResult = false;
        boolean result = instance.getF16Momentary();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF17Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testGetF17Momentary() {
        boolean expResult = false;
        boolean result = instance.getF17Momentary();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF18Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testGetF18Momentary() {
        boolean expResult = false;
        boolean result = instance.getF18Momentary();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF19Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testGetF19Momentary() {
        boolean expResult = false;
        boolean result = instance.getF19Momentary();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF20Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testGetF20Momentary() {
        boolean expResult = false;
        boolean result = instance.getF20Momentary();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF21Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testGetF21Momentary() {
        boolean expResult = false;
        boolean result = instance.getF21Momentary();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF22Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testGetF22Momentary() {
        boolean expResult = false;
        boolean result = instance.getF22Momentary();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF23Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testGetF23Momentary() {
        boolean expResult = false;
        boolean result = instance.getF23Momentary();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF24Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testGetF24Momentary() {
        boolean expResult = false;
        boolean result = instance.getF24Momentary();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF25Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testGetF25Momentary() {
        boolean expResult = false;
        boolean result = instance.getF25Momentary();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF26Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testGetF26Momentary() {
        boolean expResult = false;
        boolean result = instance.getF26Momentary();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF27Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testGetF27Momentary() {
        boolean expResult = false;
        boolean result = instance.getF27Momentary();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of getF28Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testGetF28Momentary() {
        boolean expResult = false;
        boolean result = instance.getF28Momentary();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of removePropertyChangeListener method, of class AbstractThrottle.
     */
    @Test
    public void testRemovePropertyChangeListener() {
        PropertyChangeListener l = (PropertyChangeEvent evt) -> {
        };
        instance.removePropertyChangeListener(l);
    }

    /**
     * Test of addPropertyChangeListener method, of class AbstractThrottle.
     */
    @Test
    public void testAddPropertyChangeListener() {
        PropertyChangeListener l = null;
        instance.addPropertyChangeListener(l);
    }

    /**
     * Test of notifyPropertyChangeListener method, of class AbstractThrottle.
     */
    @Test
    public void testNotifyPropertyChangeListener() {
        String property = "";
        Object oldValue = null;
        Object newValue = null;
        instance.notifyPropertyChangeListener(property, oldValue, newValue);
        jmri.util.JUnitAppender.assertErrorMessage("notifyPropertyChangeListener without change");
    }

    /**
     * Test of getListeners method, of class AbstractThrottle.
     */
    @Test
    public void testGetListeners() {
        Vector<PropertyChangeListener> expResult = new Vector<>();
        Vector<PropertyChangeListener> result = instance.getListeners();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of dispose method, of class AbstractThrottle.
     */
    @Test
    public void testDispose_ThrottleListener() {
        ThrottleListener l = null;
        instance.dispose(l);
    }

    /**
     * Test of dispatch method, of class AbstractThrottle.
     */
    @Test
    public void testDispatch_ThrottleListener() {
        ThrottleListener l = null;
        instance.dispatch(l);
    }

    /**
     * Test of release method, of class AbstractThrottle.
     */
    @Test
    public void testRelease_ThrottleListener() {
        ThrottleListener l = null;
        instance.release(l);
    }

    /**
     * Test of throttleDispose method, of class AbstractThrottle.
     */
    @Test
    public void testThrottleDispose() {
        instance.throttleDispose();
    }

    /**
     * Test of getSpeedIncrement method, of class AbstractThrottle.
     */
    @Test
    public void testGetSpeedIncrement() {
        float expResult = 0.0F;
        float result = instance.getSpeedIncrement();
        Assert.assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of setF0 method, of class AbstractThrottle.
     */
    @Test
    public void testSetF0() {
        boolean f0 = false;
        instance.setF0(f0);
        jmri.util.JUnitAppender.assertErrorMessage("sendFunctionGroup1 needs to be implemented if invoked");
    }

    /**
     * Test of setF1 method, of class AbstractThrottle.
     */
    @Test
    public void testSetF1() {
        boolean f1 = false;
        instance.setF1(f1);
        jmri.util.JUnitAppender.assertErrorMessage("sendFunctionGroup1 needs to be implemented if invoked");
    }

    /**
     * Test of setF2 method, of class AbstractThrottle.
     */
    @Test
    public void testSetF2() {
        boolean f2 = false;
        instance.setF2(f2);
        jmri.util.JUnitAppender.assertErrorMessage("sendFunctionGroup1 needs to be implemented if invoked");
    }

    /**
     * Test of setF3 method, of class AbstractThrottle.
     */
    @Test
    public void testSetF3() {
        boolean f3 = false;
        instance.setF3(f3);
        jmri.util.JUnitAppender.assertErrorMessage("sendFunctionGroup1 needs to be implemented if invoked");
    }

    /**
     * Test of setF4 method, of class AbstractThrottle.
     */
    @Test
    public void testSetF4() {
        boolean f4 = false;
        instance.setF4(f4);
        jmri.util.JUnitAppender.assertErrorMessage("sendFunctionGroup1 needs to be implemented if invoked");
    }

    /**
     * Test of setF5 method, of class AbstractThrottle.
     */
    @Test
    public void testSetF5() {
        boolean f5 = false;
        instance.setF5(f5);
        jmri.util.JUnitAppender.assertErrorMessage("sendFunctionGroup2 needs to be implemented if invoked");
    }

    /**
     * Test of setF6 method, of class AbstractThrottle.
     */
    @Test
    public void testSetF6() {
        boolean f6 = false;
        instance.setF6(f6);
        jmri.util.JUnitAppender.assertErrorMessage("sendFunctionGroup2 needs to be implemented if invoked");
    }

    /**
     * Test of setF7 method, of class AbstractThrottle.
     */
    @Test
    public void testSetF7() {
        boolean f7 = false;
        instance.setF7(f7);
        jmri.util.JUnitAppender.assertErrorMessage("sendFunctionGroup2 needs to be implemented if invoked");
    }

    /**
     * Test of setF8 method, of class AbstractThrottle.
     */
    @Test
    public void testSetF8() {
        boolean f8 = false;
        instance.setF8(f8);
        jmri.util.JUnitAppender.assertErrorMessage("sendFunctionGroup2 needs to be implemented if invoked");
    }

    /**
     * Test of setF9 method, of class AbstractThrottle.
     */
    @Test
    public void testSetF9() {
        boolean f9 = false;
        instance.setF9(f9);
        jmri.util.JUnitAppender.assertErrorMessage("sendFunctionGroup3 needs to be implemented if invoked");
    }

    /**
     * Test of setF10 method, of class AbstractThrottle.
     */
    @Test
    public void testSetF10() {
        boolean f10 = false;
        instance.setF10(f10);
        jmri.util.JUnitAppender.assertErrorMessage("sendFunctionGroup3 needs to be implemented if invoked");
    }

    /**
     * Test of setF11 method, of class AbstractThrottle.
     */
    @Test
    public void testSetF11() {
        boolean f11 = false;
        instance.setF11(f11);
        jmri.util.JUnitAppender.assertErrorMessage("sendFunctionGroup3 needs to be implemented if invoked");
    }

    /**
     * Test of setF12 method, of class AbstractThrottle.
     */
    @Test
    public void testSetF12() {
        boolean f12 = false;
        instance.setF12(f12);
        jmri.util.JUnitAppender.assertErrorMessage("sendFunctionGroup3 needs to be implemented if invoked");
    }

    /**
     * Test of setF13 method, of class AbstractThrottle.
     */
    @Test
    public void testSetF13() {
        boolean f13 = false;
        instance.setF13(f13);
        jmri.util.JUnitAppender.assertErrorMessage("Can't send F13-F20 since no command station defined");
    }

    /**
     * Test of setF14 method, of class AbstractThrottle.
     */
    @Test
    public void testSetF14() {
        boolean f14 = false;
        instance.setF14(f14);
        jmri.util.JUnitAppender.assertErrorMessage("Can't send F13-F20 since no command station defined");
    }

    /**
     * Test of setF15 method, of class AbstractThrottle.
     */
    @Test
    public void testSetF15() {
        boolean f15 = false;
        instance.setF15(f15);
        jmri.util.JUnitAppender.assertErrorMessage("Can't send F13-F20 since no command station defined");
    }

    /**
     * Test of setF16 method, of class AbstractThrottle.
     */
    @Test
    public void testSetF16() {
        boolean f16 = false;
        instance.setF16(f16);
        jmri.util.JUnitAppender.assertErrorMessage("Can't send F13-F20 since no command station defined");
    }

    /**
     * Test of setF17 method, of class AbstractThrottle.
     */
    @Test
    public void testSetF17() {
        boolean f17 = false;
        instance.setF17(f17);
        jmri.util.JUnitAppender.assertErrorMessage("Can't send F13-F20 since no command station defined");
    }

    /**
     * Test of setF18 method, of class AbstractThrottle.
     */
    @Test
    public void testSetF18() {
        boolean f18 = false;
        instance.setF18(f18);
        jmri.util.JUnitAppender.assertErrorMessage("Can't send F13-F20 since no command station defined");
    }

    /**
     * Test of setF19 method, of class AbstractThrottle.
     */
    @Test
    public void testSetF19() {
        boolean f19 = false;
        instance.setF19(f19);
        jmri.util.JUnitAppender.assertErrorMessage("Can't send F13-F20 since no command station defined");
    }

    /**
     * Test of setF20 method, of class AbstractThrottle.
     */
    @Test
    public void testSetF20() {
        boolean f20 = false;
        instance.setF20(f20);
        jmri.util.JUnitAppender.assertErrorMessage("Can't send F13-F20 since no command station defined");
    }

    /**
     * Test of setF21 method, of class AbstractThrottle.
     */
    @Test
    public void testSetF21() {
        boolean f21 = false;
        instance.setF21(f21);
        jmri.util.JUnitAppender.assertErrorMessage("Can't send F21-F28 since no command station defined");
    }

    /**
     * Test of setF22 method, of class AbstractThrottle.
     */
    @Test
    public void testSetF22() {
        boolean f22 = false;
        instance.setF22(f22);
        jmri.util.JUnitAppender.assertErrorMessage("Can't send F21-F28 since no command station defined");
    }

    /**
     * Test of setF23 method, of class AbstractThrottle.
     */
    @Test
    public void testSetF23() {
        boolean f23 = false;
        instance.setF23(f23);
        jmri.util.JUnitAppender.assertErrorMessage("Can't send F21-F28 since no command station defined");
    }

    /**
     * Test of setF24 method, of class AbstractThrottle.
     */
    @Test
    public void testSetF24() {
        boolean f24 = false;
        instance.setF24(f24);
        jmri.util.JUnitAppender.assertErrorMessage("Can't send F21-F28 since no command station defined");
    }

    /**
     * Test of setF25 method, of class AbstractThrottle.
     */
    @Test
    public void testSetF25() {
        boolean f25 = false;
        instance.setF25(f25);
        jmri.util.JUnitAppender.assertErrorMessage("Can't send F21-F28 since no command station defined");
    }

    /**
     * Test of setF26 method, of class AbstractThrottle.
     */
    @Test
    public void testSetF26() {
        boolean f26 = false;
        instance.setF26(f26);
        jmri.util.JUnitAppender.assertErrorMessage("Can't send F21-F28 since no command station defined");
    }

    /**
     * Test of setF27 method, of class AbstractThrottle.
     */
    @Test
    public void testSetF27() {
        boolean f27 = false;
        instance.setF27(f27);
        jmri.util.JUnitAppender.assertErrorMessage("Can't send F21-F28 since no command station defined");
    }

    /**
     * Test of setF28 method, of class AbstractThrottle.
     */
    @Test
    public void testSetF28() {
        boolean f28 = false;
        instance.setF28(f28);
        jmri.util.JUnitAppender.assertErrorMessage("Can't send F21-F28 since no command station defined");
    }

    /**
     * Test of sendFunctionGroup1 method, of class AbstractThrottle.
     */
    @Test
    public void testSendFunctionGroup1() {
        instance.sendFunctionGroup1();
        jmri.util.JUnitAppender.assertErrorMessage("sendFunctionGroup1 needs to be implemented if invoked");
    }

    /**
     * Test of sendFunctionGroup2 method, of class AbstractThrottle.
     */
    @Test
    public void testSendFunctionGroup2() {
        instance.sendFunctionGroup2();
        jmri.util.JUnitAppender.assertErrorMessage("sendFunctionGroup2 needs to be implemented if invoked");
    }

    /**
     * Test of sendFunctionGroup3 method, of class AbstractThrottle.
     */
    @Test
    public void testSendFunctionGroup3() {
        instance.sendFunctionGroup3();
        jmri.util.JUnitAppender.assertErrorMessage("sendFunctionGroup3 needs to be implemented if invoked");
    }

    /**
     * Test of sendFunctionGroup4 method, of class AbstractThrottle.
     */
    @Test
    public void testSendFunctionGroup4() {
        instance.sendFunctionGroup4();
        jmri.util.JUnitAppender.assertErrorMessage("Can't send F13-F20 since no command station defined");
    }

    /**
     * Test of sendFunctionGroup5 method, of class AbstractThrottle.
     */
    @Test
    public void testSendFunctionGroup5() {
        instance.sendFunctionGroup5();
        jmri.util.JUnitAppender.assertErrorMessage("Can't send F21-F28 since no command station defined");
    }

    /**
     * Test of setF0Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testSetF0Momentary() {
        boolean f0Momentary = false;
        instance.setF0Momentary(f0Momentary);
    }

    /**
     * Test of setF1Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testSetF1Momentary() {
        boolean f1Momentary = false;
        instance.setF1Momentary(f1Momentary);
    }

    /**
     * Test of setF2Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testSetF2Momentary() {
        boolean f2Momentary = false;
        instance.setF2Momentary(f2Momentary);
    }

    /**
     * Test of setF3Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testSetF3Momentary() {
        boolean f3Momentary = false;
        instance.setF3Momentary(f3Momentary);
    }

    /**
     * Test of setF4Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testSetF4Momentary() {
        boolean f4Momentary = false;
        instance.setF4Momentary(f4Momentary);
    }

    /**
     * Test of setF5Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testSetF5Momentary() {
        boolean f5Momentary = false;
        instance.setF5Momentary(f5Momentary);
    }

    /**
     * Test of setF6Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testSetF6Momentary() {
        boolean f6Momentary = false;
        instance.setF6Momentary(f6Momentary);
    }

    /**
     * Test of setF7Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testSetF7Momentary() {
        boolean f7Momentary = false;
        instance.setF7Momentary(f7Momentary);
    }

    /**
     * Test of setF8Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testSetF8Momentary() {
        boolean f8Momentary = false;
        instance.setF8Momentary(f8Momentary);
    }

    /**
     * Test of setF9Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testSetF9Momentary() {
        boolean f9Momentary = false;
        instance.setF9Momentary(f9Momentary);
    }

    /**
     * Test of setF10Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testSetF10Momentary() {
        boolean f10Momentary = false;
        instance.setF10Momentary(f10Momentary);
    }

    /**
     * Test of setF11Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testSetF11Momentary() {
        boolean f11Momentary = false;
        instance.setF11Momentary(f11Momentary);
    }

    /**
     * Test of setF12Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testSetF12Momentary() {
        boolean f12Momentary = false;
        instance.setF12Momentary(f12Momentary);
    }

    /**
     * Test of setF13Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testSetF13Momentary() {
        boolean f13Momentary = false;
        instance.setF13Momentary(f13Momentary);
    }

    /**
     * Test of setF14Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testSetF14Momentary() {
        boolean f14Momentary = false;
        instance.setF14Momentary(f14Momentary);
    }

    /**
     * Test of setF15Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testSetF15Momentary() {
        boolean f15Momentary = false;
        instance.setF15Momentary(f15Momentary);
    }

    /**
     * Test of setF16Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testSetF16Momentary() {
        boolean f16Momentary = false;
        instance.setF16Momentary(f16Momentary);
    }

    /**
     * Test of setF17Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testSetF17Momentary() {
        boolean f17Momentary = false;
        instance.setF17Momentary(f17Momentary);
    }

    /**
     * Test of setF18Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testSetF18Momentary() {
        boolean f18Momentary = false;
        instance.setF18Momentary(f18Momentary);
    }

    /**
     * Test of setF19Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testSetF19Momentary() {
        boolean f19Momentary = false;
        instance.setF19Momentary(f19Momentary);
    }

    /**
     * Test of setF20Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testSetF20Momentary() {
        boolean f20Momentary = false;
        instance.setF20Momentary(f20Momentary);
    }

    /**
     * Test of setF21Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testSetF21Momentary() {
        boolean f21Momentary = false;
        instance.setF21Momentary(f21Momentary);
    }

    /**
     * Test of setF22Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testSetF22Momentary() {
        boolean f22Momentary = false;
        instance.setF22Momentary(f22Momentary);
    }

    /**
     * Test of setF23Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testSetF23Momentary() {
        boolean f23Momentary = false;
        instance.setF23Momentary(f23Momentary);
    }

    /**
     * Test of setF24Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testSetF24Momentary() {
        boolean f24Momentary = false;
        instance.setF24Momentary(f24Momentary);
    }

    /**
     * Test of setF25Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testSetF25Momentary() {
        boolean f25Momentary = false;
        instance.setF25Momentary(f25Momentary);
    }

    /**
     * Test of setF26Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testSetF26Momentary() {
        boolean f26Momentary = false;
        instance.setF26Momentary(f26Momentary);
    }

    /**
     * Test of setF27Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testSetF27Momentary() {
        boolean f27Momentary = false;
        instance.setF27Momentary(f27Momentary);
    }

    /**
     * Test of setF28Momentary method, of class AbstractThrottle.
     */
    @Test
    public void testSetF28Momentary() {
        boolean f28Momentary = false;
        instance.setF28Momentary(f28Momentary);
    }

    /**
     * Test of sendMomentaryFunctionGroup1 method, of class AbstractThrottle.
     */
    @Test
    public void testSendMomentaryFunctionGroup1() {
        instance.sendMomentaryFunctionGroup1();
    }

    /**
     * Test of sendMomentaryFunctionGroup2 method, of class AbstractThrottle.
     */
    @Test
    public void testSendMomentaryFunctionGroup2() {
        instance.sendMomentaryFunctionGroup2();
    }

    /**
     * Test of sendMomentaryFunctionGroup3 method, of class AbstractThrottle.
     */
    @Test
    public void testSendMomentaryFunctionGroup3() {
        instance.sendMomentaryFunctionGroup3();
    }

    /**
     * Test of sendMomentaryFunctionGroup4 method, of class AbstractThrottle.
     */
    @Test
    public void testSendMomentaryFunctionGroup4() {
        instance.sendMomentaryFunctionGroup4();
    }

    /**
     * Test of sendMomentaryFunctionGroup5 method, of class AbstractThrottle.
     */
    @Test
    public void testSendMomentaryFunctionGroup5() {
        instance.sendMomentaryFunctionGroup5();
    }

    /**
     * Test of setSpeedStepMode method, of class AbstractThrottle.
     */
    @Test
    public void testSetSpeedStepMode() {
        instance.setSpeedStepMode(SpeedStepMode.NMRA_DCC_128);
    }

    /**
     * Test of getSpeedStepMode method, of class AbstractThrottle.
     */
    @Test
    public void testGetSpeedStepMode() {
        SpeedStepMode expResult = SpeedStepMode.UNKNOWN;
        SpeedStepMode result = instance.getSpeedStepMode();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of record method, of class AbstractThrottle.
     */
    @Test
    public void testRecord() {
        float speed = 0.0F;
        instance.record(speed);
    }

    /**
     * Test of startClock method, of class AbstractThrottle.
     */
    @Test
    public void testStartClock() {
        instance.startClock();
    }

    /**
     * Test of stopClock method, of class AbstractThrottle.
     */
    @Test
    public void testStopClock() {
        instance.stopClock();
    }

    /**
     * Test of finishRecord method, of class AbstractThrottle.
     */
    @Test
    public void testFinishRecord() {
        instance.finishRecord();
    }

    /**
     * Test of setRosterEntry method, of class AbstractThrottle.
     */
    @Test
    public void testSetRosterEntry() {
        BasicRosterEntry re = null;
        instance.setRosterEntry(re);
    }

    /**
     * Test of getRosterEntry method, of class AbstractThrottle.
     */
    @Test
    public void testGetRosterEntry() {
        BasicRosterEntry expResult = null;
        BasicRosterEntry result = instance.getRosterEntry();
        Assert.assertEquals(expResult, result);
    }

    /**
     * Test of intSpeed method, of class AbstractThrottle.
     */
    @Test
    public void testGetSpeed_float() {
        Assert.assertEquals("Full Speed", 127, instance.intSpeed(1.0F));
        float incre = 0.007874016f;
        float speed = incre;
        // Cannot get speeedStep 1. range is 2 to 127
        int i = 2;
        while (speed < 0.999f) {
            int result = instance.intSpeed(speed);
            log.debug("speed= {} step= {}",speed,result);
            Assert.assertEquals("speed step ", i++, result);
            speed += incre;
        }
    }

    /**
     * Test of intSpeed method, of class AbstractThrottle.
     */
    @Test
    public void testGetSpeed_float_int() {
        float speed = 0.001F;
        int maxStepHi = 127;
        int maxStepLo = 28;
        Assert.assertEquals("Idle", 0, instance.intSpeed(0.0F, maxStepHi));
        Assert.assertEquals("Idle", 0, instance.intSpeed(0.0F, maxStepLo));
        Assert.assertEquals("Emergency", 1, instance.intSpeed(-1.0F, maxStepHi));
        Assert.assertEquals("Emergency", 1, instance.intSpeed(-1.0F, maxStepLo));
        Assert.assertEquals("Emergency", 1, instance.intSpeed(-0.001F, maxStepHi));
        Assert.assertEquals("Emergency", 1, instance.intSpeed(-0.001F, maxStepLo));
        Assert.assertEquals("Full Speed", maxStepHi, instance.intSpeed(1.0F, maxStepHi));
        Assert.assertEquals("Full Speed", maxStepLo, instance.intSpeed(1.0F, maxStepLo));
        while (speed < 1.1F) { // loop ~ 1100 times
            int result = instance.intSpeed(speed, maxStepHi);
            Assert.assertNotSame(speed + "(" + maxStepHi + " steps) should not idle", 0, result);
            Assert.assertNotSame(speed + "(" + maxStepHi + " steps) should not eStop", 1, result);
            Assert.assertTrue(speed + "(" + maxStepHi + " steps) should not exceed " + maxStepHi, result <= 127);
            result = instance.intSpeed(speed, maxStepLo);
            Assert.assertNotSame(speed + "(" + maxStepLo + " steps) should not idle", 0, result);
            Assert.assertNotSame(speed + "(" + maxStepLo + " steps) should not eStop", 1, result);
            Assert.assertTrue(speed + "(" + maxStepLo + " steps) should not exceed " + maxStepLo, result <= 127);
            speed = speed + 0.001F;
        }
    }

    public final class AbstractThrottleImpl extends AbstractThrottle {

        private LocoAddress locoAddress;

        public AbstractThrottleImpl() {
            super(null);
            this.setLocoAddress(new DccLocoAddress(3, LocoAddress.Protocol.DCC_SHORT));
        }

        @Override
        public void throttleDispose() {
        }

        public void setLocoAddress(LocoAddress locoAddress) {
            this.locoAddress = locoAddress;
        }

        @Override
        public LocoAddress getLocoAddress() {
            return this.locoAddress;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractThrottleTest.class);

}
