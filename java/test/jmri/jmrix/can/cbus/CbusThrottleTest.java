package jmri.jmrix.can.cbus;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;

import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.SpeedStepMode;
import jmri.jmrix.AbstractThrottleManager;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class CbusThrottleTest extends jmri.jmrix.AbstractThrottleTest {

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists",instance);
    }

    /**
     * Test of getSpeedIncrement method, of class AbstractThrottle.
     */
    @Override
    @Test
    public void testGetSpeedIncrement() {
        float expResult = 1.0F/126.0F;
        float result = instance.getSpeedIncrement();
        Assert.assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of getSpeedStepMode method, of class AbstractThrottle.
     */
    @Override
    @Test
    public void testGetSpeedStepMode() {
        SpeedStepMode expResult = SpeedStepMode.NMRA_DCC_128;
        SpeedStepMode result = instance.getSpeedStepMode();
        Assert.assertEquals(expResult, result);
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
    
    private void testSingleFunction(int function){
        Assert.assertTrue(instance.getFunction(function));
        Assert.assertTrue(tc.outbound.size()==1);
        instance.setFunction(function,false);
        Assert.assertFalse(instance.getFunction(function));
        Assert.assertTrue(tc.outbound.size()==2);
    }

    /**
     * Test of setF0 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF0() {
        instance.setF0(true);
        testSingleFunction(0);
    }

    /**
     * Test of setF1 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF1() {
        instance.setF1(true);
        testSingleFunction(1);
    }

    /**
     * Test of setF2 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF2() {
        instance.setF2(true);
        testSingleFunction(2);
    }

    /**
     * Test of setF3 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF3() {
        instance.setF3(true);
        testSingleFunction(3);
    }

    /**
     * Test of setF4 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF4() {
        instance.setF4(true);
        testSingleFunction(4);
    }

    /**
     * Test of setF5 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF5() {
        instance.setF5(true);
        testSingleFunction(5);
    }

    /**
     * Test of setF6 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF6() {
        instance.setF6(true);
        testSingleFunction(6);
    }

    /**
     * Test of setF7 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF7() {
        instance.setF7(true);
        testSingleFunction(7);
    }

    /**
     * Test of setF8 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF8() {
        instance.setF8(true);
        testSingleFunction(8);
    }

    /**
     * Test of setF9 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF9() {
        instance.setF9(true);
        testSingleFunction(9);
    }

    /**
     * Test of setF10 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF10() {
        instance.setF10(true);
        testSingleFunction(10);
    }

    /**
     * Test of setF11 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF11() {
        instance.setF11(true);
        testSingleFunction(11);
    }

    /**
     * Test of setF12 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF12() {
        instance.setF12(true);
        testSingleFunction(12);
    }

    /**
     * Test of setF13 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF13() {
        instance.setF13(true);
        testSingleFunction(13);
    }

    /**
     * Test of setF14 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF14() {
        instance.setF14(true);
        testSingleFunction(14);
    }

    /**
     * Test of setF15 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF15() {
        instance.setF15(true);
        testSingleFunction(15);
    }

    /**
     * Test of setF16 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF16() {
        instance.setF16(true);
        testSingleFunction(16);
    }

    /**
     * Test of setF17 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF17() {
        instance.setF17(true);
        testSingleFunction(17);
    }

    /**
     * Test of setF18 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF18() {
        instance.setF18(true);
        testSingleFunction(18);
    }

    /**
     * Test of setF19 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF19() {
        instance.setF19(true);
        testSingleFunction(19);
    }

    /**
     * Test of setF20 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF20() {
        instance.setF20(true);
        testSingleFunction(20);
    }

    /**
     * Test of setF21 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF21() {
        instance.setF21(true);
        testSingleFunction(21);
    }

    /**
     * Test of setF22 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF22() {
        instance.setF22(true);
        testSingleFunction(22);
    }

    /**
     * Test of setF23 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF23() {
        instance.setF23(true);
        testSingleFunction(23);
    }

    /**
     * Test of setF24 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF24() {
        instance.setF24(true);
        testSingleFunction(24);
    }

    /**
     * Test of setF25 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF25() {
        instance.setF25(true);
        testSingleFunction(25);
    }

    /**
     * Test of setF26 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF26() {
        instance.setF26(true);
        testSingleFunction(26);
    }

    /**
     * Test of setF27 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF27() {
        instance.setF27(true);
        testSingleFunction(27);
    }

    /**
     * Test of setF28 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF28() {
        instance.setF28(true);
        testSingleFunction(28);
    }

    /**
     * Test of sendFunctionGroup1 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSendFunctionGroup1() {
        
        // not testing via setFunction in case this ever changes to use DFON / DFOF.
        // instead, we update function then send the group manually.
        
        int startSize = tc.outbound.size();

        ((CbusThrottle) instance).sendFunctionGroup1();
        Assert.assertEquals(startSize+1, tc.outbound.size());
        Assert.assertEquals("[78] 60 64 01 00", tc.outbound.elementAt(0).toString());
        
        instance.updateFunction(0, true);
        ((CbusThrottle) instance).sendFunctionGroup1();
        Assert.assertEquals(startSize+2, tc.outbound.size());
        Assert.assertEquals("[78] 60 64 01 10", tc.outbound.elementAt(1).toString());
        instance.updateFunction(0, false);
        
        instance.updateFunction(1, true);
        ((CbusThrottle) instance).sendFunctionGroup1();
        Assert.assertEquals(startSize+3, tc.outbound.size());
        Assert.assertEquals("[78] 60 64 01 01", tc.outbound.elementAt(2).toString());
        instance.updateFunction(1, false);
        
        instance.updateFunction(2, true);
        ((CbusThrottle) instance).sendFunctionGroup1();
        Assert.assertEquals(startSize+4, tc.outbound.size());
        Assert.assertEquals("[78] 60 64 01 02", tc.outbound.elementAt(3).toString());
        instance.updateFunction(2, false);
        
        instance.updateFunction(3, true);
        ((CbusThrottle) instance).sendFunctionGroup1();
        Assert.assertEquals(startSize+5, tc.outbound.size());
        Assert.assertEquals("[78] 60 64 01 04", tc.outbound.elementAt(4).toString());
        instance.updateFunction(3, false);
        
        instance.updateFunction(4, true);
        ((CbusThrottle) instance).sendFunctionGroup1();
        Assert.assertEquals(startSize+6, tc.outbound.size());
        Assert.assertEquals("[78] 60 64 01 08", tc.outbound.elementAt(5).toString());
        instance.updateFunction(4, false);
        
        instance.updateFunction(5, true);
        ((CbusThrottle) instance).sendFunctionGroup1();
        Assert.assertEquals(startSize+7, tc.outbound.size());
        Assert.assertEquals("[78] 60 64 01 00", tc.outbound.elementAt(6).toString());
        
    }

    /**
     * Test of sendFunctionGroup2 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSendFunctionGroup2() {
        
        // not testing via setFunction in case this ever changes to use DFON / DFOF.
        // instead, we update function then send the group manually.
        
        int startSize = tc.outbound.size();

        ((CbusThrottle) instance).sendFunctionGroup2();
        Assert.assertEquals(startSize+1, tc.outbound.size());
        Assert.assertEquals("[78] 60 64 02 00", tc.outbound.elementAt(0).toString());
        
        instance.updateFunction(5, true);
        ((CbusThrottle) instance).sendFunctionGroup2();
        Assert.assertEquals(startSize+2, tc.outbound.size());
        Assert.assertEquals("[78] 60 64 02 01", tc.outbound.elementAt(1).toString());
        instance.updateFunction(5, false);
        
        instance.updateFunction(6, true);
        ((CbusThrottle) instance).sendFunctionGroup2();
        Assert.assertEquals(startSize+3, tc.outbound.size());
        Assert.assertEquals("[78] 60 64 02 02", tc.outbound.elementAt(2).toString());
        instance.updateFunction(6, false);
        
        instance.updateFunction(7, true);
        ((CbusThrottle) instance).sendFunctionGroup2();
        Assert.assertEquals(startSize+4, tc.outbound.size());
        Assert.assertEquals("[78] 60 64 02 04", tc.outbound.elementAt(3).toString());
        instance.updateFunction(7, false);
        
        instance.updateFunction(8, true);
        ((CbusThrottle) instance).sendFunctionGroup2();
        Assert.assertEquals(startSize+5, tc.outbound.size());
        Assert.assertEquals("[78] 60 64 02 08", tc.outbound.elementAt(4).toString());
        instance.updateFunction(8, false);
        
        instance.updateFunction(9, true);
        ((CbusThrottle) instance).sendFunctionGroup2();
        Assert.assertEquals(startSize+6, tc.outbound.size());
        Assert.assertEquals("[78] 60 64 02 00", tc.outbound.elementAt(5).toString());
        
    }

    /**
     * Test of sendFunctionGroup3 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSendFunctionGroup3() {
        
        // not testing via setFunction in case this ever changes to use DFON / DFOF.
        // instead, we update function then send the group manually.
        
        int startSize = tc.outbound.size();

        ((CbusThrottle) instance).sendFunctionGroup3();
        Assert.assertEquals(startSize+1, tc.outbound.size());
        Assert.assertEquals("[78] 60 64 03 00", tc.outbound.elementAt(0).toString());
        
        instance.updateFunction(9, true);
        ((CbusThrottle) instance).sendFunctionGroup3();
        Assert.assertEquals(startSize+2, tc.outbound.size());
        Assert.assertEquals("[78] 60 64 03 01", tc.outbound.elementAt(1).toString());
        instance.updateFunction(9, false);
        
        instance.updateFunction(10, true);
        ((CbusThrottle) instance).sendFunctionGroup3();
        Assert.assertEquals(startSize+3, tc.outbound.size());
        Assert.assertEquals("[78] 60 64 03 02", tc.outbound.elementAt(2).toString());
        instance.updateFunction(10, false);
        
        instance.updateFunction(11, true);
        ((CbusThrottle) instance).sendFunctionGroup3();
        Assert.assertEquals(startSize+4, tc.outbound.size());
        Assert.assertEquals("[78] 60 64 03 04", tc.outbound.elementAt(3).toString());
        instance.updateFunction(11, false);
        
        instance.updateFunction(12, true);
        ((CbusThrottle) instance).sendFunctionGroup3();
        Assert.assertEquals(startSize+5, tc.outbound.size());
        Assert.assertEquals("[78] 60 64 03 08", tc.outbound.elementAt(4).toString());
        instance.updateFunction(12, false);
        
        instance.updateFunction(13, true);
        ((CbusThrottle) instance).sendFunctionGroup3();
        Assert.assertEquals(startSize+6, tc.outbound.size());
        Assert.assertEquals("[78] 60 64 03 00", tc.outbound.elementAt(5).toString());
        
    }

    /**
     * Test of sendFunctionGroup4 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSendFunctionGroup4() {
        
        // not testing via setFunction in case this ever changes to use DFON / DFOF.
        // instead, we update function then send the group manually.
        
        int startSize = tc.outbound.size();

        ((CbusThrottle) instance).sendFunctionGroup4();
        Assert.assertEquals(startSize+1, tc.outbound.size());
        Assert.assertEquals("[78] 60 64 04 00", tc.outbound.elementAt(0).toString());
        
        instance.updateFunction(13, true);
        ((CbusThrottle) instance).sendFunctionGroup4();
        Assert.assertEquals(startSize+2, tc.outbound.size());
        Assert.assertEquals("[78] 60 64 04 01", tc.outbound.elementAt(1).toString());
        instance.updateFunction(13, false);
        
        instance.updateFunction(14, true);
        ((CbusThrottle) instance).sendFunctionGroup4();
        Assert.assertEquals(startSize+3, tc.outbound.size());
        Assert.assertEquals("[78] 60 64 04 02", tc.outbound.elementAt(2).toString());
        instance.updateFunction(14, false);
        
        instance.updateFunction(15, true);
        ((CbusThrottle) instance).sendFunctionGroup4();
        Assert.assertEquals(startSize+4, tc.outbound.size());
        Assert.assertEquals("[78] 60 64 04 04", tc.outbound.elementAt(3).toString());
        instance.updateFunction(15, false);
        
        instance.updateFunction(16, true);
        ((CbusThrottle) instance).sendFunctionGroup4();
        Assert.assertEquals(startSize+5, tc.outbound.size());
        Assert.assertEquals("[78] 60 64 04 08", tc.outbound.elementAt(4).toString());
        instance.updateFunction(16, false);
        
        instance.updateFunction(17, true);
        ((CbusThrottle) instance).sendFunctionGroup4();
        Assert.assertEquals(startSize+6, tc.outbound.size());
        Assert.assertEquals("[78] 60 64 04 10", tc.outbound.elementAt(5).toString());
        instance.updateFunction(17, false);
        
        instance.updateFunction(18, true);
        ((CbusThrottle) instance).sendFunctionGroup4();
        Assert.assertEquals(startSize+7, tc.outbound.size());
        Assert.assertEquals("[78] 60 64 04 20", tc.outbound.elementAt(6).toString());
        instance.updateFunction(18, false);
        
        instance.updateFunction(19, true);
        ((CbusThrottle) instance).sendFunctionGroup4();
        Assert.assertEquals(startSize+8, tc.outbound.size());
        Assert.assertEquals("[78] 60 64 04 40", tc.outbound.elementAt(7).toString());
        instance.updateFunction(19, false);
        
        instance.updateFunction(20, true);
        ((CbusThrottle) instance).sendFunctionGroup4();
        Assert.assertEquals(startSize+9, tc.outbound.size());
        Assert.assertEquals("[78] 60 64 04 80", tc.outbound.elementAt(8).toString());
        instance.updateFunction(20, false);
        
        instance.updateFunction(21, true);
        ((CbusThrottle) instance).sendFunctionGroup4();
        Assert.assertEquals(startSize+10, tc.outbound.size());
        Assert.assertEquals("[78] 60 64 04 00", tc.outbound.elementAt(9).toString());
        
    }

    /**
     * Test of sendFunctionGroup5 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSendFunctionGroup5() {
        
        
        // not testing via setFunction in case this ever changes to use DFON / DFOF.
        // instead, we update function then send the group manually.
        
        int startSize = tc.outbound.size();

        ((CbusThrottle) instance).sendFunctionGroup5();
        Assert.assertEquals(startSize+1, tc.outbound.size());
        Assert.assertEquals("[78] 60 64 05 00", tc.outbound.elementAt(0).toString());
        
        instance.updateFunction(21, true);
        ((CbusThrottle) instance).sendFunctionGroup5();
        Assert.assertEquals(startSize+2, tc.outbound.size());
        Assert.assertEquals("[78] 60 64 05 01", tc.outbound.elementAt(1).toString());
        instance.updateFunction(21, false);
        
        instance.updateFunction(22, true);
        ((CbusThrottle) instance).sendFunctionGroup5();
        Assert.assertEquals(startSize+3, tc.outbound.size());
        Assert.assertEquals("[78] 60 64 05 02", tc.outbound.elementAt(2).toString());
        instance.updateFunction(22, false);
        
        instance.updateFunction(23, true);
        ((CbusThrottle) instance).sendFunctionGroup5();
        Assert.assertEquals(startSize+4, tc.outbound.size());
        Assert.assertEquals("[78] 60 64 05 04", tc.outbound.elementAt(3).toString());
        instance.updateFunction(23, false);
        
        instance.updateFunction(24, true);
        ((CbusThrottle) instance).sendFunctionGroup5();
        Assert.assertEquals(startSize+5, tc.outbound.size());
        Assert.assertEquals("[78] 60 64 05 08", tc.outbound.elementAt(4).toString());
        instance.updateFunction(24, false);
        
        instance.updateFunction(25, true);
        ((CbusThrottle) instance).sendFunctionGroup5();
        Assert.assertEquals(startSize+6, tc.outbound.size());
        Assert.assertEquals("[78] 60 64 05 10", tc.outbound.elementAt(5).toString());
        instance.updateFunction(25, false);
        
        instance.updateFunction(26, true);
        ((CbusThrottle) instance).sendFunctionGroup5();
        Assert.assertEquals(startSize+7, tc.outbound.size());
        Assert.assertEquals("[78] 60 64 05 20", tc.outbound.elementAt(6).toString());
        instance.updateFunction(26, false);
        
        instance.updateFunction(27, true);
        ((CbusThrottle) instance).sendFunctionGroup5();
        Assert.assertEquals(startSize+8, tc.outbound.size());
        Assert.assertEquals("[78] 60 64 05 40", tc.outbound.elementAt(7).toString());
        instance.updateFunction(27, false);
        
        instance.updateFunction(28, true);
        ((CbusThrottle) instance).sendFunctionGroup5();
        Assert.assertEquals(startSize+9, tc.outbound.size());
        Assert.assertEquals("[78] 60 64 05 80", tc.outbound.elementAt(8).toString());
        instance.updateFunction(28, false);
        
        instance.updateFunction(7, true);
        ((CbusThrottle) instance).sendFunctionGroup5();
        Assert.assertEquals(startSize+10, tc.outbound.size());
        Assert.assertEquals("[78] 60 64 05 00", tc.outbound.elementAt(9).toString());
        
    }
    
    @Test
    public void testSendsDirectionChangeWhileMoving() {
        
        int startSize = tc.outbound.size();
        
        instance.setIsForward(false);
        Assert.assertEquals(startSize+1, tc.outbound.size());
        instance.setSpeedSetting(0.5f);
        Assert.assertEquals(startSize+2, tc.outbound.size());
        instance.setIsForward(true);
        Assert.assertEquals(startSize+3, tc.outbound.size());
        instance.setIsForward(false);
        Assert.assertEquals(startSize+4, tc.outbound.size());
        
        Assert.assertNotEquals("Different message sent",
            tc.outbound.elementAt(tc.outbound.size() - 2).toString(),
            tc.outbound.elementAt(tc.outbound.size() - 1).toString());
        
    }
    
    private void assertFunctionsOn( int[] fns) {
        for ( int i=0; i<=28; i++ ){
            assertSingleFunc(fns,i);
        }
    }
    
    private void assertSingleFunc( int[] fns, int i){
        if ( Arrays.stream(fns).anyMatch(j -> j == i)){
            Assert.assertTrue(instance.getFunction(i));
        }
        else {
            Assert.assertFalse(instance.getFunction(i));
        }
    }
    
    @Test
    public void testUpdateFunctionGroup1() {

        assertFunctionsOn(new int[]{}); // check all functions start off

        ((CbusThrottle)instance).updateFunctionGroup(1,CbusConstants.CBUS_F0);
        assertFunctionsOn(new int[]{0});
        
        ((CbusThrottle)instance).updateFunctionGroup(1,CbusConstants.CBUS_F1);
        assertFunctionsOn(new int[]{1});

        ((CbusThrottle)instance).updateFunctionGroup(1,CbusConstants.CBUS_F2);
        assertFunctionsOn(new int[]{2});

        ((CbusThrottle)instance).updateFunctionGroup(1,CbusConstants.CBUS_F3);
        assertFunctionsOn(new int[]{3});
        
        ((CbusThrottle)instance).updateFunctionGroup(1,CbusConstants.CBUS_F4);
        assertFunctionsOn(new int[]{4});
        
        ((CbusThrottle)instance).updateFunctionGroup(1,CbusConstants.CBUS_F1 + CbusConstants.CBUS_F4);
        assertFunctionsOn(new int[]{4,1});
        
        ((CbusThrottle)instance).updateFunctionGroup(1,CbusConstants.CBUS_F0 + CbusConstants.CBUS_F3);
        assertFunctionsOn(new int[]{0,3});
        
    }
    
    @Test
    public void testUpdateFunctionGroup2() {

        ((CbusThrottle)instance).updateFunctionGroup(2,CbusConstants.CBUS_F5);
        assertFunctionsOn(new int[]{5});
        
        ((CbusThrottle)instance).updateFunctionGroup(2,CbusConstants.CBUS_F6);
        assertFunctionsOn(new int[]{6});

        ((CbusThrottle)instance).updateFunctionGroup(2,CbusConstants.CBUS_F7);
        assertFunctionsOn(new int[]{7});

        ((CbusThrottle)instance).updateFunctionGroup(2,CbusConstants.CBUS_F8);
        assertFunctionsOn(new int[]{8});
        
        ((CbusThrottle)instance).updateFunctionGroup(2,CbusConstants.CBUS_F7 + CbusConstants.CBUS_F8);
        assertFunctionsOn(new int[]{7,8});
        
        ((CbusThrottle)instance).updateFunctionGroup(2,CbusConstants.CBUS_F5 + CbusConstants.CBUS_F8);
        assertFunctionsOn(new int[]{5,8});
        
    }
    
    @Test
    public void testUpdateFunctionGroup3() {

        ((CbusThrottle)instance).updateFunctionGroup(3,CbusConstants.CBUS_F9);
        assertFunctionsOn(new int[]{9});
        
        ((CbusThrottle)instance).updateFunctionGroup(3,CbusConstants.CBUS_F10);
        assertFunctionsOn(new int[]{10});

        ((CbusThrottle)instance).updateFunctionGroup(3,CbusConstants.CBUS_F11);
        assertFunctionsOn(new int[]{11});

        ((CbusThrottle)instance).updateFunctionGroup(3,CbusConstants.CBUS_F12);
        assertFunctionsOn(new int[]{12});
        
        ((CbusThrottle)instance).updateFunctionGroup(3,CbusConstants.CBUS_F11 + CbusConstants.CBUS_F9 );
        assertFunctionsOn(new int[]{9,11});

        ((CbusThrottle)instance).updateFunctionGroup(3,
                CbusConstants.CBUS_F9 + CbusConstants.CBUS_F10 +CbusConstants.CBUS_F11 + CbusConstants.CBUS_F12 );
        assertFunctionsOn(new int[]{9,10,11,12});
        
    }
    
    @Test
    public void testUpdateFunctionGroup4() {

        assertFunctionsOn(new int[]{}); // check all functions start off

        ((CbusThrottle)instance).updateFunctionGroup(4,CbusConstants.CBUS_F13);
        assertFunctionsOn(new int[]{13});
        
        ((CbusThrottle)instance).updateFunctionGroup(4,CbusConstants.CBUS_F14);
        assertFunctionsOn(new int[]{14});

        ((CbusThrottle)instance).updateFunctionGroup(4,CbusConstants.CBUS_F15);
        assertFunctionsOn(new int[]{15});

        ((CbusThrottle)instance).updateFunctionGroup(4,CbusConstants.CBUS_F16);
        assertFunctionsOn(new int[]{16});
        
        ((CbusThrottle)instance).updateFunctionGroup(4,CbusConstants.CBUS_F17);
        assertFunctionsOn(new int[]{17});
        
        ((CbusThrottle)instance).updateFunctionGroup(4,CbusConstants.CBUS_F18);
        assertFunctionsOn(new int[]{18});

        ((CbusThrottle)instance).updateFunctionGroup(4,CbusConstants.CBUS_F19);
        assertFunctionsOn(new int[]{19});

        ((CbusThrottle)instance).updateFunctionGroup(4,CbusConstants.CBUS_F20);
        assertFunctionsOn(new int[]{20});
        
        
        ((CbusThrottle)instance).updateFunctionGroup(4,CbusConstants.CBUS_F13 + CbusConstants.CBUS_F19 );
        assertFunctionsOn(new int[]{13,19});
        
        ((CbusThrottle)instance).updateFunctionGroup(4,CbusConstants.CBUS_F16 + CbusConstants.CBUS_F17 );
        assertFunctionsOn(new int[]{16,17});
        
    }

    @Test
    public void testUpdateFunctionGroup5() {

        assertFunctionsOn(new int[]{}); // check all functions start off

        ((CbusThrottle)instance).updateFunctionGroup(5,CbusConstants.CBUS_F21);
        assertFunctionsOn(new int[]{21});
        
        ((CbusThrottle)instance).updateFunctionGroup(5,CbusConstants.CBUS_F22);
        assertFunctionsOn(new int[]{22});

        ((CbusThrottle)instance).updateFunctionGroup(5,CbusConstants.CBUS_F23);
        assertFunctionsOn(new int[]{23});

        ((CbusThrottle)instance).updateFunctionGroup(5,CbusConstants.CBUS_F24);
        assertFunctionsOn(new int[]{24});
        
        ((CbusThrottle)instance).updateFunctionGroup(5,CbusConstants.CBUS_F25);
        assertFunctionsOn(new int[]{25});
        
        ((CbusThrottle)instance).updateFunctionGroup(5,CbusConstants.CBUS_F26);
        assertFunctionsOn(new int[]{26});

        ((CbusThrottle)instance).updateFunctionGroup(5,CbusConstants.CBUS_F27);
        assertFunctionsOn(new int[]{27});

        ((CbusThrottle)instance).updateFunctionGroup(5,CbusConstants.CBUS_F28);
        assertFunctionsOn(new int[]{28});
        
        
        ((CbusThrottle)instance).updateFunctionGroup(5,CbusConstants.CBUS_F28 + CbusConstants.CBUS_F21);
        assertFunctionsOn(new int[]{21,28});
        
        ((CbusThrottle)instance).updateFunctionGroup(5,
            CbusConstants.CBUS_F24 + CbusConstants.CBUS_F25 + CbusConstants.CBUS_F26);
        assertFunctionsOn(new int[]{24,25,26});
        
    }
    
    private boolean listenerkicked;
    private boolean listenerakicked;
    
    @Test
    public void testFunction0Listener(){
    
        listenerkicked = false;
        PropertyChangeListener l = (PropertyChangeEvent evt) -> {
            listenerkicked = true;
        };
        PropertyChangeListener la = (PropertyChangeEvent evt) -> {
            listenerakicked = true;
        };
        
        instance.addPropertyChangeListener("F0", l);
        instance.addPropertyChangeListener("F1", la);
        
        instance.setF0(true);
        
        Assert.assertEquals("F0 listener triggered", true, listenerkicked);
        Assert.assertEquals("F1 listener triggered", false, listenerakicked);
        
        instance.removePropertyChangeListener(l);
        instance.removePropertyChangeListener(la);
    
    }
    
    @Test
    public void testFunction7Listener(){
    
        listenerkicked = false;
        PropertyChangeListener l = (PropertyChangeEvent evt) -> {
            listenerkicked = true;
        };
        PropertyChangeListener la = (PropertyChangeEvent evt) -> {
            listenerakicked = true;
        };
        
        instance.addPropertyChangeListener("F0", l);
        instance.addPropertyChangeListener("F7", la);
        
        instance.setFunction(7,true);
        
        Assert.assertEquals("F0 listener triggered", false, listenerkicked);
        Assert.assertEquals("F7 listener triggered", true, listenerakicked);
        
        instance.removePropertyChangeListener(l);
        instance.removePropertyChangeListener(la);
    
    }
    
    @Test
    public void testDefaultSpeedSteps(){
        Assert.assertEquals("default 128 SS", jmri.SpeedStepMode.NMRA_DCC_128, instance.getSpeedStepMode());
    }
    
    private int propChangeCount = 0;
    private jmri.SpeedStepMode newMode;
    private jmri.SpeedStepMode oldMode;
    

    @Test
    public void testChangeSpeedSteps(){
        
        int outFrames = tc.outbound.size();
        
        propChangeCount = 0;
        PropertyChangeListener l = (PropertyChangeEvent evt) -> {
            propChangeCount++;
            oldMode = (SpeedStepMode) evt.getOldValue();
            newMode = (SpeedStepMode) evt.getNewValue();
        };
        instance.addPropertyChangeListener(jmri.Throttle.SPEEDSTEPS, l);
        
        instance.setSpeedStepMode(SpeedStepMode.NMRA_DCC_128);
        Assert.assertEquals(outFrames, tc.outbound.size());
        Assert.assertEquals(0, propChangeCount);
        
        instance.setSpeedStepMode(SpeedStepMode.NMRA_DCC_14);
        Assert.assertEquals("14 SS", SpeedStepMode.NMRA_DCC_14, instance.getSpeedStepMode());
        Assert.assertEquals(outFrames+1, tc.outbound.size());
        Assert.assertEquals("[78] 44 64 01", tc.outbound.elementAt(tc.outbound.size()-1).getToString());
        Assert.assertEquals(1, propChangeCount);
        Assert.assertEquals(SpeedStepMode.NMRA_DCC_128,oldMode);
        Assert.assertEquals(SpeedStepMode.NMRA_DCC_14,newMode);
        
        instance.setSpeedStepMode(SpeedStepMode.NMRA_DCC_128);
        Assert.assertEquals("128 SS", SpeedStepMode.NMRA_DCC_128, instance.getSpeedStepMode());
        Assert.assertEquals(outFrames+2, tc.outbound.size());
        Assert.assertEquals("[78] 44 64 00", tc.outbound.elementAt(tc.outbound.size()-1).getToString());
        Assert.assertEquals(2, propChangeCount);
        Assert.assertEquals(SpeedStepMode.NMRA_DCC_14,oldMode);
        Assert.assertEquals(SpeedStepMode.NMRA_DCC_128,newMode);
        
        instance.setSpeedStepMode(SpeedStepMode.NMRA_DCC_28);
        Assert.assertEquals("28 SS", SpeedStepMode.NMRA_DCC_28, instance.getSpeedStepMode());
        Assert.assertEquals(outFrames+3, tc.outbound.size());
        Assert.assertEquals("[78] 44 64 03", tc.outbound.elementAt(tc.outbound.size()-1).getToString());
        Assert.assertEquals(3, propChangeCount);
        Assert.assertEquals(SpeedStepMode.NMRA_DCC_128,oldMode);
        Assert.assertEquals(SpeedStepMode.NMRA_DCC_28,newMode);
        
        instance.removePropertyChangeListener(l);
    }
    
    private TrafficControllerScaffold tc;
    private CanSystemConnectionMemo memo;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        tc = new TrafficControllerScaffold();
        memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tc);
        memo.setProtocol(jmri.jmrix.can.ConfigurationManager.MERGCBUS);
        memo.configureManagers();
        
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
        instance = new CbusThrottle(memo, new DccLocoAddress(100,true),100);
    }

    @AfterEach
    @Override
    public void tearDown() {
        if (instance != null){
            instance.dispose(null);
        }
        memo.dispose();
        memo = null;
        tc.terminateThreads();
        tc = null;
        JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(CbusThrottleTest.class);

}
