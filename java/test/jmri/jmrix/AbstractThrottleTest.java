package jmri.jmrix;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import jmri.BasicRosterEntry;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.ThrottleListener;
import junit.framework.TestCase;

/**
 *
 * @author Randall Wood 2015
 */
public class AbstractThrottleTest extends TestCase {

    public AbstractThrottleTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp(); 
        super.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
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
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        apps.tests.Log4JFixture.tearDown(); 
    }

    /**
     * Test of getSpeedSetting method, of class AbstractThrottle.
     */
    public void testGetSpeedSetting() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        float expResult = 0.0F;
        float result = instance.getSpeedSetting();
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of setSpeedSetting method, of class AbstractThrottle.
     */
    public void testSetSpeedSetting() {
        float speed = 1.0F;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setSpeedSetting(speed);
        assertEquals(speed, instance.getSpeedSetting(), 0.0);
    }

    /**
     * Test of getIsForward method, of class AbstractThrottle.
     */
    public void testGetIsForward() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getIsForward();
        assertEquals(expResult, result);
    }

    /**
     * Test of setIsForward method, of class AbstractThrottle.
     */
    public void testSetIsForward() {
        boolean forward = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setIsForward(forward);
    }

    /**
     * Test of getF0 method, of class AbstractThrottle.
     */
    public void testGetF0() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF0();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF1 method, of class AbstractThrottle.
     */
    public void testGetF1() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF1();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF2 method, of class AbstractThrottle.
     */
    public void testGetF2() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF2();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF3 method, of class AbstractThrottle.
     */
    public void testGetF3() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF3();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF4 method, of class AbstractThrottle.
     */
    public void testGetF4() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF4();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF5 method, of class AbstractThrottle.
     */
    public void testGetF5() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF5();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF6 method, of class AbstractThrottle.
     */
    public void testGetF6() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF6();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF7 method, of class AbstractThrottle.
     */
    public void testGetF7() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF7();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF8 method, of class AbstractThrottle.
     */
    public void testGetF8() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF8();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF9 method, of class AbstractThrottle.
     */
    public void testGetF9() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF9();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF10 method, of class AbstractThrottle.
     */
    public void testGetF10() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF10();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF11 method, of class AbstractThrottle.
     */
    public void testGetF11() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF11();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF12 method, of class AbstractThrottle.
     */
    public void testGetF12() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF12();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF13 method, of class AbstractThrottle.
     */
    public void testGetF13() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF13();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF14 method, of class AbstractThrottle.
     */
    public void testGetF14() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF14();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF15 method, of class AbstractThrottle.
     */
    public void testGetF15() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF15();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF16 method, of class AbstractThrottle.
     */
    public void testGetF16() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF16();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF17 method, of class AbstractThrottle.
     */
    public void testGetF17() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF17();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF18 method, of class AbstractThrottle.
     */
    public void testGetF18() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF18();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF19 method, of class AbstractThrottle.
     */
    public void testGetF19() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF19();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF20 method, of class AbstractThrottle.
     */
    public void testGetF20() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF20();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF21 method, of class AbstractThrottle.
     */
    public void testGetF21() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF21();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF22 method, of class AbstractThrottle.
     */
    public void testGetF22() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF22();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF23 method, of class AbstractThrottle.
     */
    public void testGetF23() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF23();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF24 method, of class AbstractThrottle.
     */
    public void testGetF24() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF24();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF25 method, of class AbstractThrottle.
     */
    public void testGetF25() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF25();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF26 method, of class AbstractThrottle.
     */
    public void testGetF26() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF26();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF27 method, of class AbstractThrottle.
     */
    public void testGetF27() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF27();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF28 method, of class AbstractThrottle.
     */
    public void testGetF28() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF28();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF0Momentary method, of class AbstractThrottle.
     */
    public void testGetF0Momentary() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF0Momentary();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF1Momentary method, of class AbstractThrottle.
     */
    public void testGetF1Momentary() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF1Momentary();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF2Momentary method, of class AbstractThrottle.
     */
    public void testGetF2Momentary() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF2Momentary();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF3Momentary method, of class AbstractThrottle.
     */
    public void testGetF3Momentary() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF3Momentary();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF4Momentary method, of class AbstractThrottle.
     */
    public void testGetF4Momentary() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF4Momentary();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF5Momentary method, of class AbstractThrottle.
     */
    public void testGetF5Momentary() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF5Momentary();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF6Momentary method, of class AbstractThrottle.
     */
    public void testGetF6Momentary() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF6Momentary();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF7Momentary method, of class AbstractThrottle.
     */
    public void testGetF7Momentary() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF7Momentary();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF8Momentary method, of class AbstractThrottle.
     */
    public void testGetF8Momentary() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF8Momentary();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF9Momentary method, of class AbstractThrottle.
     */
    public void testGetF9Momentary() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF9Momentary();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF10Momentary method, of class AbstractThrottle.
     */
    public void testGetF10Momentary() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF10Momentary();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF11Momentary method, of class AbstractThrottle.
     */
    public void testGetF11Momentary() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF11Momentary();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF12Momentary method, of class AbstractThrottle.
     */
    public void testGetF12Momentary() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF12Momentary();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF13Momentary method, of class AbstractThrottle.
     */
    public void testGetF13Momentary() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF13Momentary();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF14Momentary method, of class AbstractThrottle.
     */
    public void testGetF14Momentary() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF14Momentary();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF15Momentary method, of class AbstractThrottle.
     */
    public void testGetF15Momentary() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF15Momentary();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF16Momentary method, of class AbstractThrottle.
     */
    public void testGetF16Momentary() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF16Momentary();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF17Momentary method, of class AbstractThrottle.
     */
    public void testGetF17Momentary() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF17Momentary();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF18Momentary method, of class AbstractThrottle.
     */
    public void testGetF18Momentary() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF18Momentary();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF19Momentary method, of class AbstractThrottle.
     */
    public void testGetF19Momentary() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF19Momentary();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF20Momentary method, of class AbstractThrottle.
     */
    public void testGetF20Momentary() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF20Momentary();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF21Momentary method, of class AbstractThrottle.
     */
    public void testGetF21Momentary() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF21Momentary();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF22Momentary method, of class AbstractThrottle.
     */
    public void testGetF22Momentary() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF22Momentary();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF23Momentary method, of class AbstractThrottle.
     */
    public void testGetF23Momentary() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF23Momentary();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF24Momentary method, of class AbstractThrottle.
     */
    public void testGetF24Momentary() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF24Momentary();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF25Momentary method, of class AbstractThrottle.
     */
    public void testGetF25Momentary() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF25Momentary();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF26Momentary method, of class AbstractThrottle.
     */
    public void testGetF26Momentary() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF26Momentary();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF27Momentary method, of class AbstractThrottle.
     */
    public void testGetF27Momentary() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF27Momentary();
        assertEquals(expResult, result);
    }

    /**
     * Test of getF28Momentary method, of class AbstractThrottle.
     */
    public void testGetF28Momentary() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        boolean expResult = false;
        boolean result = instance.getF28Momentary();
        assertEquals(expResult, result);
    }

    /**
     * Test of removePropertyChangeListener method, of class AbstractThrottle.
     */
    public void testRemovePropertyChangeListener() {
        PropertyChangeListener l = (PropertyChangeEvent evt) -> {
        };
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.removePropertyChangeListener(l);
    }

    /**
     * Test of addPropertyChangeListener method, of class AbstractThrottle.
     */
    public void testAddPropertyChangeListener() {
        PropertyChangeListener l = null;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.addPropertyChangeListener(l);
    }

    /**
     * Test of notifyPropertyChangeListener method, of class AbstractThrottle.
     */
    public void testNotifyPropertyChangeListener() {
        String property = "";
        Object oldValue = null;
        Object newValue = null;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.notifyPropertyChangeListener(property, oldValue, newValue);
        jmri.util.JUnitAppender.assertErrorMessage("notifyPropertyChangeListener without change");
    }

    /**
     * Test of getListeners method, of class AbstractThrottle.
     */
    public void testGetListeners() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        Vector<PropertyChangeListener> expResult = new Vector<>();
        Vector<PropertyChangeListener> result = instance.getListeners();
        assertEquals(expResult, result);
    }

    /**
     * Test of dispose method, of class AbstractThrottle.
     */
    public void testDispose_0args() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.dispose();
        jmri.util.JUnitAppender.assertWarnMessage("Dispose called without knowing the original throttle listener");
    }

    /**
     * Test of dispose method, of class AbstractThrottle.
     */
    public void testDispose_ThrottleListener() {
        ThrottleListener l = null;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.dispose(l);
    }

    /**
     * Test of dispatch method, of class AbstractThrottle.
     */
    public void testDispatch_0args() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.dispatch();
        jmri.util.JUnitAppender.assertWarnMessage("dispatch called without knowing the original throttle listener");
    }

    /**
     * Test of dispatch method, of class AbstractThrottle.
     */
    public void testDispatch_ThrottleListener() {
        ThrottleListener l = null;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.dispatch(l);
    }

    /**
     * Test of release method, of class AbstractThrottle.
     */
    public void testRelease_0args() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.release();
        jmri.util.JUnitAppender.assertWarnMessage("Release called without knowing the original throttle listener");
    }

    /**
     * Test of release method, of class AbstractThrottle.
     */
    public void testRelease_ThrottleListener() {
        ThrottleListener l = null;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.release(l);
    }

    /**
     * Test of throttleDispose method, of class AbstractThrottle.
     */
    public void testThrottleDispose() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.throttleDispose();
    }

    /**
     * Test of getSpeedIncrement method, of class AbstractThrottle.
     */
    public void testGetSpeedIncrement() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        float expResult = 0.0F;
        float result = instance.getSpeedIncrement();
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of setF0 method, of class AbstractThrottle.
     */
    public void testSetF0() {
        boolean f0 = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF0(f0);
        jmri.util.JUnitAppender.assertErrorMessage("sendFunctionGroup1 needs to be implemented if invoked");
    }

    /**
     * Test of setF1 method, of class AbstractThrottle.
     */
    public void testSetF1() {
        boolean f1 = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF1(f1);
        jmri.util.JUnitAppender.assertErrorMessage("sendFunctionGroup1 needs to be implemented if invoked");
    }

    /**
     * Test of setF2 method, of class AbstractThrottle.
     */
    public void testSetF2() {
        boolean f2 = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF2(f2);
        jmri.util.JUnitAppender.assertErrorMessage("sendFunctionGroup1 needs to be implemented if invoked");
    }

    /**
     * Test of setF3 method, of class AbstractThrottle.
     */
    public void testSetF3() {
        boolean f3 = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF3(f3);
        jmri.util.JUnitAppender.assertErrorMessage("sendFunctionGroup1 needs to be implemented if invoked");
    }

    /**
     * Test of setF4 method, of class AbstractThrottle.
     */
    public void testSetF4() {
        boolean f4 = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF4(f4);
        jmri.util.JUnitAppender.assertErrorMessage("sendFunctionGroup1 needs to be implemented if invoked");
    }

    /**
     * Test of setF5 method, of class AbstractThrottle.
     */
    public void testSetF5() {
        boolean f5 = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF5(f5);
        jmri.util.JUnitAppender.assertErrorMessage("sendFunctionGroup2 needs to be implemented if invoked");
    }

    /**
     * Test of setF6 method, of class AbstractThrottle.
     */
    public void testSetF6() {
        boolean f6 = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF6(f6);
        jmri.util.JUnitAppender.assertErrorMessage("sendFunctionGroup2 needs to be implemented if invoked");
    }

    /**
     * Test of setF7 method, of class AbstractThrottle.
     */
    public void testSetF7() {
        boolean f7 = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF7(f7);
        jmri.util.JUnitAppender.assertErrorMessage("sendFunctionGroup2 needs to be implemented if invoked");
    }

    /**
     * Test of setF8 method, of class AbstractThrottle.
     */
    public void testSetF8() {
        boolean f8 = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF8(f8);
        jmri.util.JUnitAppender.assertErrorMessage("sendFunctionGroup2 needs to be implemented if invoked");
    }

    /**
     * Test of setF9 method, of class AbstractThrottle.
     */
    public void testSetF9() {
        boolean f9 = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF9(f9);
        jmri.util.JUnitAppender.assertErrorMessage("sendFunctionGroup3 needs to be implemented if invoked");
    }

    /**
     * Test of setF10 method, of class AbstractThrottle.
     */
    public void testSetF10() {
        boolean f10 = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF10(f10);
        jmri.util.JUnitAppender.assertErrorMessage("sendFunctionGroup3 needs to be implemented if invoked");
    }

    /**
     * Test of setF11 method, of class AbstractThrottle.
     */
    public void testSetF11() {
        boolean f11 = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF11(f11);
        jmri.util.JUnitAppender.assertErrorMessage("sendFunctionGroup3 needs to be implemented if invoked");
    }

    /**
     * Test of setF12 method, of class AbstractThrottle.
     */
    public void testSetF12() {
        boolean f12 = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF12(f12);
        jmri.util.JUnitAppender.assertErrorMessage("sendFunctionGroup3 needs to be implemented if invoked");
    }

    /**
     * Test of setF13 method, of class AbstractThrottle.
     */
    public void testSetF13() {
        boolean f13 = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF13(f13);
        jmri.util.JUnitAppender.assertErrorMessage("Can't send F13-F20 since no command station defined");
    }

    /**
     * Test of setF14 method, of class AbstractThrottle.
     */
    public void testSetF14() {
        boolean f14 = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF14(f14);
        jmri.util.JUnitAppender.assertErrorMessage("Can't send F13-F20 since no command station defined");
    }

    /**
     * Test of setF15 method, of class AbstractThrottle.
     */
    public void testSetF15() {
        boolean f15 = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF15(f15);
        jmri.util.JUnitAppender.assertErrorMessage("Can't send F13-F20 since no command station defined");
    }

    /**
     * Test of setF16 method, of class AbstractThrottle.
     */
    public void testSetF16() {
        boolean f16 = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF16(f16);
        jmri.util.JUnitAppender.assertErrorMessage("Can't send F13-F20 since no command station defined");
    }

    /**
     * Test of setF17 method, of class AbstractThrottle.
     */
    public void testSetF17() {
        boolean f17 = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF17(f17);
        jmri.util.JUnitAppender.assertErrorMessage("Can't send F13-F20 since no command station defined");
    }

    /**
     * Test of setF18 method, of class AbstractThrottle.
     */
    public void testSetF18() {
        boolean f18 = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF18(f18);
        jmri.util.JUnitAppender.assertErrorMessage("Can't send F13-F20 since no command station defined");
    }

    /**
     * Test of setF19 method, of class AbstractThrottle.
     */
    public void testSetF19() {
        boolean f19 = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF19(f19);
        jmri.util.JUnitAppender.assertErrorMessage("Can't send F13-F20 since no command station defined");
    }

    /**
     * Test of setF20 method, of class AbstractThrottle.
     */
    public void testSetF20() {
        boolean f20 = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF20(f20);
        jmri.util.JUnitAppender.assertErrorMessage("Can't send F13-F20 since no command station defined");
    }

    /**
     * Test of setF21 method, of class AbstractThrottle.
     */
    public void testSetF21() {
        boolean f21 = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF21(f21);
        jmri.util.JUnitAppender.assertErrorMessage("Can't send F21-F28 since no command station defined");
    }

    /**
     * Test of setF22 method, of class AbstractThrottle.
     */
    public void testSetF22() {
        boolean f22 = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF22(f22);
        jmri.util.JUnitAppender.assertErrorMessage("Can't send F21-F28 since no command station defined");
    }

    /**
     * Test of setF23 method, of class AbstractThrottle.
     */
    public void testSetF23() {
        boolean f23 = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF23(f23);
        jmri.util.JUnitAppender.assertErrorMessage("Can't send F21-F28 since no command station defined");
    }

    /**
     * Test of setF24 method, of class AbstractThrottle.
     */
    public void testSetF24() {
        boolean f24 = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF24(f24);
        jmri.util.JUnitAppender.assertErrorMessage("Can't send F21-F28 since no command station defined");
    }

    /**
     * Test of setF25 method, of class AbstractThrottle.
     */
    public void testSetF25() {
        boolean f25 = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF25(f25);
        jmri.util.JUnitAppender.assertErrorMessage("Can't send F21-F28 since no command station defined");
    }

    /**
     * Test of setF26 method, of class AbstractThrottle.
     */
    public void testSetF26() {
        boolean f26 = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF26(f26);
        jmri.util.JUnitAppender.assertErrorMessage("Can't send F21-F28 since no command station defined");
    }

    /**
     * Test of setF27 method, of class AbstractThrottle.
     */
    public void testSetF27() {
        boolean f27 = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF27(f27);
        jmri.util.JUnitAppender.assertErrorMessage("Can't send F21-F28 since no command station defined");
    }

    /**
     * Test of setF28 method, of class AbstractThrottle.
     */
    public void testSetF28() {
        boolean f28 = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF28(f28);
        jmri.util.JUnitAppender.assertErrorMessage("Can't send F21-F28 since no command station defined");
    }

    /**
     * Test of sendFunctionGroup1 method, of class AbstractThrottle.
     */
    public void testSendFunctionGroup1() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.sendFunctionGroup1();
        jmri.util.JUnitAppender.assertErrorMessage("sendFunctionGroup1 needs to be implemented if invoked");
    }

    /**
     * Test of sendFunctionGroup2 method, of class AbstractThrottle.
     */
    public void testSendFunctionGroup2() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.sendFunctionGroup2();
        jmri.util.JUnitAppender.assertErrorMessage("sendFunctionGroup2 needs to be implemented if invoked");
    }

    /**
     * Test of sendFunctionGroup3 method, of class AbstractThrottle.
     */
    public void testSendFunctionGroup3() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.sendFunctionGroup3();
        jmri.util.JUnitAppender.assertErrorMessage("sendFunctionGroup3 needs to be implemented if invoked");
    }

    /**
     * Test of sendFunctionGroup4 method, of class AbstractThrottle.
     */
    public void testSendFunctionGroup4() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.sendFunctionGroup4();
        jmri.util.JUnitAppender.assertErrorMessage("Can't send F13-F20 since no command station defined");
    }

    /**
     * Test of sendFunctionGroup5 method, of class AbstractThrottle.
     */
    public void testSendFunctionGroup5() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.sendFunctionGroup5();
        jmri.util.JUnitAppender.assertErrorMessage("Can't send F21-F28 since no command station defined");
    }

    /**
     * Test of setF0Momentary method, of class AbstractThrottle.
     */
    public void testSetF0Momentary() {
        boolean f0Momentary = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF0Momentary(f0Momentary);
    }

    /**
     * Test of setF1Momentary method, of class AbstractThrottle.
     */
    public void testSetF1Momentary() {
        boolean f1Momentary = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF1Momentary(f1Momentary);
    }

    /**
     * Test of setF2Momentary method, of class AbstractThrottle.
     */
    public void testSetF2Momentary() {
        boolean f2Momentary = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF2Momentary(f2Momentary);
    }

    /**
     * Test of setF3Momentary method, of class AbstractThrottle.
     */
    public void testSetF3Momentary() {
        boolean f3Momentary = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF3Momentary(f3Momentary);
    }

    /**
     * Test of setF4Momentary method, of class AbstractThrottle.
     */
    public void testSetF4Momentary() {
        boolean f4Momentary = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF4Momentary(f4Momentary);
    }

    /**
     * Test of setF5Momentary method, of class AbstractThrottle.
     */
    public void testSetF5Momentary() {
        boolean f5Momentary = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF5Momentary(f5Momentary);
    }

    /**
     * Test of setF6Momentary method, of class AbstractThrottle.
     */
    public void testSetF6Momentary() {
        boolean f6Momentary = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF6Momentary(f6Momentary);
    }

    /**
     * Test of setF7Momentary method, of class AbstractThrottle.
     */
    public void testSetF7Momentary() {
        boolean f7Momentary = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF7Momentary(f7Momentary);
    }

    /**
     * Test of setF8Momentary method, of class AbstractThrottle.
     */
    public void testSetF8Momentary() {
        boolean f8Momentary = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF8Momentary(f8Momentary);
    }

    /**
     * Test of setF9Momentary method, of class AbstractThrottle.
     */
    public void testSetF9Momentary() {
        boolean f9Momentary = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF9Momentary(f9Momentary);
    }

    /**
     * Test of setF10Momentary method, of class AbstractThrottle.
     */
    public void testSetF10Momentary() {
        boolean f10Momentary = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF10Momentary(f10Momentary);
    }

    /**
     * Test of setF11Momentary method, of class AbstractThrottle.
     */
    public void testSetF11Momentary() {
        boolean f11Momentary = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF11Momentary(f11Momentary);
    }

    /**
     * Test of setF12Momentary method, of class AbstractThrottle.
     */
    public void testSetF12Momentary() {
        boolean f12Momentary = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF12Momentary(f12Momentary);
    }

    /**
     * Test of setF13Momentary method, of class AbstractThrottle.
     */
    public void testSetF13Momentary() {
        boolean f13Momentary = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF13Momentary(f13Momentary);
    }

    /**
     * Test of setF14Momentary method, of class AbstractThrottle.
     */
    public void testSetF14Momentary() {
        boolean f14Momentary = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF14Momentary(f14Momentary);
    }

    /**
     * Test of setF15Momentary method, of class AbstractThrottle.
     */
    public void testSetF15Momentary() {
        boolean f15Momentary = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF15Momentary(f15Momentary);
    }

    /**
     * Test of setF16Momentary method, of class AbstractThrottle.
     */
    public void testSetF16Momentary() {
        boolean f16Momentary = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF16Momentary(f16Momentary);
    }

    /**
     * Test of setF17Momentary method, of class AbstractThrottle.
     */
    public void testSetF17Momentary() {
        boolean f17Momentary = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF17Momentary(f17Momentary);
    }

    /**
     * Test of setF18Momentary method, of class AbstractThrottle.
     */
    public void testSetF18Momentary() {
        boolean f18Momentary = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF18Momentary(f18Momentary);
    }

    /**
     * Test of setF19Momentary method, of class AbstractThrottle.
     */
    public void testSetF19Momentary() {
        boolean f19Momentary = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF19Momentary(f19Momentary);
    }

    /**
     * Test of setF20Momentary method, of class AbstractThrottle.
     */
    public void testSetF20Momentary() {
        boolean f20Momentary = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF20Momentary(f20Momentary);
    }

    /**
     * Test of setF21Momentary method, of class AbstractThrottle.
     */
    public void testSetF21Momentary() {
        boolean f21Momentary = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF21Momentary(f21Momentary);
    }

    /**
     * Test of setF22Momentary method, of class AbstractThrottle.
     */
    public void testSetF22Momentary() {
        boolean f22Momentary = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF22Momentary(f22Momentary);
    }

    /**
     * Test of setF23Momentary method, of class AbstractThrottle.
     */
    public void testSetF23Momentary() {
        boolean f23Momentary = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF23Momentary(f23Momentary);
    }

    /**
     * Test of setF24Momentary method, of class AbstractThrottle.
     */
    public void testSetF24Momentary() {
        boolean f24Momentary = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF24Momentary(f24Momentary);
    }

    /**
     * Test of setF25Momentary method, of class AbstractThrottle.
     */
    public void testSetF25Momentary() {
        boolean f25Momentary = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF25Momentary(f25Momentary);
    }

    /**
     * Test of setF26Momentary method, of class AbstractThrottle.
     */
    public void testSetF26Momentary() {
        boolean f26Momentary = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF26Momentary(f26Momentary);
    }

    /**
     * Test of setF27Momentary method, of class AbstractThrottle.
     */
    public void testSetF27Momentary() {
        boolean f27Momentary = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF27Momentary(f27Momentary);
    }

    /**
     * Test of setF28Momentary method, of class AbstractThrottle.
     */
    public void testSetF28Momentary() {
        boolean f28Momentary = false;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setF28Momentary(f28Momentary);
    }

    /**
     * Test of sendMomentaryFunctionGroup1 method, of class AbstractThrottle.
     */
    public void testSendMomentaryFunctionGroup1() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.sendMomentaryFunctionGroup1();
    }

    /**
     * Test of sendMomentaryFunctionGroup2 method, of class AbstractThrottle.
     */
    public void testSendMomentaryFunctionGroup2() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.sendMomentaryFunctionGroup2();
    }

    /**
     * Test of sendMomentaryFunctionGroup3 method, of class AbstractThrottle.
     */
    public void testSendMomentaryFunctionGroup3() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.sendMomentaryFunctionGroup3();
    }

    /**
     * Test of sendMomentaryFunctionGroup4 method, of class AbstractThrottle.
     */
    public void testSendMomentaryFunctionGroup4() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.sendMomentaryFunctionGroup4();
    }

    /**
     * Test of sendMomentaryFunctionGroup5 method, of class AbstractThrottle.
     */
    public void testSendMomentaryFunctionGroup5() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.sendMomentaryFunctionGroup5();
    }

    /**
     * Test of setSpeedStepMode method, of class AbstractThrottle.
     */
    public void testSetSpeedStepMode() {
        int Mode = 0;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setSpeedStepMode(Mode);
    }

    /**
     * Test of getSpeedStepMode method, of class AbstractThrottle.
     */
    public void testGetSpeedStepMode() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        int expResult = 0;
        int result = instance.getSpeedStepMode();
        assertEquals(expResult, result);
    }

    /**
     * Test of record method, of class AbstractThrottle.
     */
    public void testRecord() {
        float speed = 0.0F;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.record(speed);
    }

    /**
     * Test of startClock method, of class AbstractThrottle.
     */
    public void testStartClock() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.startClock();
    }

    /**
     * Test of stopClock method, of class AbstractThrottle.
     */
    public void testStopClock() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.stopClock();
    }

    /**
     * Test of finishRecord method, of class AbstractThrottle.
     */
    public void testFinishRecord() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.finishRecord();
    }

    /**
     * Test of setRosterEntry method, of class AbstractThrottle.
     */
    public void testSetRosterEntry() {
        BasicRosterEntry re = null;
        AbstractThrottle instance = new AbstractThrottleImpl();
        instance.setRosterEntry(re);
    }

    /**
     * Test of getRosterEntry method, of class AbstractThrottle.
     */
    public void testGetRosterEntry() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        BasicRosterEntry expResult = null;
        BasicRosterEntry result = instance.getRosterEntry();
        assertEquals(expResult, result);
    }

    /**
     * Test of intSpeed method, of class AbstractThrottle.
     */
    public void testGetSpeed_float() {
        AbstractThrottle instance = new AbstractThrottleImpl();
        assertEquals("Full Speed", 127, instance.intSpeed(1.0F));
    }

    /**
     * Test of intSpeed method, of class AbstractThrottle.
     */
    public void testGetSpeed_float_int() {
        float speed = 0.001F;
        int maxStepHi = 127;
        int maxStepLo = 28;
        AbstractThrottle instance = new AbstractThrottleImpl();
        assertEquals("Idle", 0, instance.intSpeed(0.0F, maxStepHi));
        assertEquals("Idle", 0, instance.intSpeed(0.0F, maxStepLo));
        assertEquals("Emergency", 1, instance.intSpeed(-1.0F, maxStepHi));
        assertEquals("Emergency", 1, instance.intSpeed(-1.0F, maxStepLo));
        assertEquals("Emergency", 1, instance.intSpeed(-0.001F, maxStepHi));
        assertEquals("Emergency", 1, instance.intSpeed(-0.001F, maxStepLo));
        assertEquals("Full Speed", maxStepHi, instance.intSpeed(1.0F, maxStepHi));
        assertEquals("Full Speed", maxStepLo, instance.intSpeed(1.0F, maxStepLo));
        while (speed < 1.1F) { // loop ~ 1100 times 
            int result = instance.intSpeed(speed, maxStepHi);
            assertNotSame(speed + "(" + maxStepHi + " steps) should not idle", 0, result);
            assertNotSame(speed + "(" + maxStepHi + " steps) should not eStop", 1, result);
            assertTrue(speed + "(" + maxStepHi + " steps) should not exceed " + maxStepHi, result <= 127);
            result = instance.intSpeed(speed, maxStepLo);
            assertNotSame(speed + "(" + maxStepLo + " steps) should not idle", 0, result);
            assertNotSame(speed + "(" + maxStepLo + " steps) should not eStop", 1, result);
            assertTrue(speed + "(" + maxStepLo + " steps) should not exceed " + maxStepLo, result <= 127);
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

}
