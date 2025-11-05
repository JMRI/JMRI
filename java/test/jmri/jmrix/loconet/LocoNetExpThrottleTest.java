package jmri.jmrix.loconet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.CommandStation;
import jmri.util.JUnitUtil;
import jmri.SpeedStepMode;

import org.junit.jupiter.api.*;

public class LocoNetExpThrottleTest extends jmri.jmrix.AbstractThrottleTest {

    @Test
    public void testCTor() {
        assertNotNull(instance);
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
        assertEquals(0.0f, t1.getSpeedSetting(), 0.0);
        t1.setSpeedSetting(0.5f);
        // the speed change SHOULD be changed.
        assertEquals(0.5f, t1.getSpeedSetting(), 0.0);

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
        assertEquals(0.0f, t2.getSpeedSetting(), 0.0);
        t2.setSpeedSetting(0.5f);
        // the speed change SHOULD be changed.
        assertEquals(0.5f, t2.getSpeedSetting(), 0.0);

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
        assertEquals(0.0f, t3.getSpeedSetting(), 0.0);
        t3.setSpeedSetting(0.5f);
        // the speed change SHOULD NOT be changed.
        assertEquals(0.0f, t3.getSpeedSetting(), 0.0);

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
        assertEquals(0.0f, t4.getSpeedSetting(), 0.0);
        t4.setSpeedSetting(0.5f);
        // the speed change SHOULD be ignored.
        assertEquals(0.0f, t4.getSpeedSetting(), 0.0);
    }

    /**
     * Test of getIsForward method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testGetIsForward() {
        boolean expResult = true;
        boolean result = instance.getIsForward();
        assertEquals(expResult, result);
    }

    /**
     * Test of getSpeedStepMode method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testGetSpeedStepMode() {
        SpeedStepMode expResult = SpeedStepMode.NMRA_DCC_28;
        SpeedStepMode result = instance.getSpeedStepMode();
        assertEquals(expResult, result);
    }

    /**
     * Test of getSpeedIncrement method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testGetSpeedIncrement() {
        float expResult = 1.0F/28.0F;
        float result = instance.getSpeedIncrement();
        assertEquals(expResult, result, 0.0);
    }

    /**
     * Test of intSpeed method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testGetSpeed_float() {
        // set speed step mode to 128.
        instance.setSpeedStepMode(jmri.SpeedStepMode.NMRA_DCC_128);
        super.testGetSpeed_float();
    }

    /**
     * Test of setF0 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF0() {

        lnis.resetStatistics(); // and clears outbound message log
        boolean f0 = false;
        instance.setFunction(0,f0);
        assertEquals(f0, instance.getFunction(0));
        assertEquals( 1, lnis.outbound.size(), "number of messages is 1");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(0).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F0F6,
            lnis.outbound.get(0).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( 0, lnis.outbound.get(0).getElement(4) & LnConstants.DIRF_F0,
            "sent f0 in correct state");
        f0 = true;
        instance.setFunction(0,f0);
        assertEquals(f0, instance.getFunction(0));
        assertEquals( 2, lnis.outbound.size(), "number of messages is 2");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(1).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F0F6,
            lnis.outbound.get(1).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( LnConstants.DIRF_F0, lnis.outbound.get(1).getElement(4) & LnConstants.DIRF_F0,
            "sent f0 in correct state");
    }

    /**
     * Test of setF1 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF1() {

        lnis.resetStatistics(); // and clears outbound message log
        boolean f1 = false;
        instance.setFunction(1,f1);
        assertEquals(f1, instance.getFunction(1));
        assertEquals( 1, lnis.outbound.size(), "number of messages is 1");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(0).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F0F6,
            lnis.outbound.get(0).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( 0, lnis.outbound.get(0).getElement(4) & LnConstants.DIRF_F1,
            "sent f0 in correct state");
        f1 = true;
        instance.setFunction(1,f1);
        assertEquals(f1, instance.getFunction(1));
        assertEquals( 2, lnis.outbound.size(), "number of messages is 2");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(1).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F0F6,
            lnis.outbound.get(1).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( LnConstants.DIRF_F1,
            lnis.outbound.get(1).getElement(4) & LnConstants.DIRF_F1, "sent f1 in correct state");
    }

    /**
     * Test of setF2 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF2() {

        lnis.resetStatistics(); // and clears outbound message log
        boolean f2 = false;
        instance.setFunction(2,f2);
        assertEquals(f2, instance.getFunction(2));
        assertEquals( 1, lnis.outbound.size(), "number of messages is 1");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(0).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F0F6,
            lnis.outbound.get(0).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( 0, lnis.outbound.get(0).getElement(4) & LnConstants.DIRF_F2,
            "sent f2 in correct state");
        f2 = true;
        instance.setFunction(2,f2);
        assertEquals(f2, instance.getFunction(2));
        assertEquals( 2, lnis.outbound.size(), "number of messages is 2");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(1).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F0F6, 
            lnis.outbound.get(1).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( LnConstants.DIRF_F2,
            lnis.outbound.get(1).getElement(4) & LnConstants.DIRF_F2, "sent f2 in correct state");
    }

    /**
     * Test of setF3 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF3() {

        lnis.resetStatistics(); // and clears outbound message log
        boolean f3 = false;
        instance.setFunction(3,f3);
        assertEquals(f3, instance.getFunction(3));
        assertEquals( 1, lnis.outbound.size(), "number of messages is 1");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(0).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F0F6,
            lnis.outbound.get(0).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( 0, lnis.outbound.get(0).getElement(4) & LnConstants.DIRF_F3,
            "sent f3 in correct state");
        f3 = true;
        instance.setFunction(3,f3);
        assertEquals(f3, instance.getFunction(3));
        assertEquals( 2, lnis.outbound.size(), "number of messages is 2");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(1).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F0F6,
            lnis.outbound.get(1).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( LnConstants.DIRF_F3,
            lnis.outbound.get(1).getElement(4) & LnConstants.DIRF_F3, "sent f3 in correct state");
    }

    /**
     * Test of setF4 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF4() {

        lnis.resetStatistics(); // and clears outbound message log
        boolean f4 = false;
        instance.setFunction(4,f4);
        assertEquals(f4, instance.getFunction(4));
        assertEquals( 1, lnis.outbound.size(), "number of messages is 1");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(0).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F0F6,
            lnis.outbound.get(0).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( 0, lnis.outbound.get(0).getElement(4) & LnConstants.DIRF_F4,
            "sent f4 in correct state");
        f4 = true;
        instance.setFunction(4,f4);
        assertEquals(f4, instance.getFunction(4));
        assertEquals( 2, lnis.outbound.size(), "number of messages is 2");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(1).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F0F6,
            lnis.outbound.get(1).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( LnConstants.DIRF_F4,
            lnis.outbound.get(1).getElement(4) & LnConstants.DIRF_F4, "sent f4 in correct state");
    }

    /**
     * Test of setF5 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF5() {

        lnis.resetStatistics(); // and clears outbound message log
        boolean f5 = false;
        instance.setFunction(5,f5);
        assertEquals(f5, instance.getFunction(5));
        assertEquals( 1, lnis.outbound.size(), "number of messages is 1");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(0).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F0F6,
            lnis.outbound.get(0).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( 0, lnis.outbound.get(0).getElement(4) & 0x20,
            "sent f5 in correct state");
        f5 = true;
        instance.setFunction(5,f5);
        assertEquals(f5, instance.getFunction(5));
        assertEquals( 2, lnis.outbound.size(), "number of messages is 2");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(1).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F0F6,
            lnis.outbound.get(1).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( 0x20, lnis.outbound.get(1).getElement(4) & 0x20,
            "sent f5 in correct state");
    }

    /**
     * Test of setF6 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF6() {

        lnis.resetStatistics(); // and clears outbound message log
        boolean f6 = false;
        instance.setFunction(6,f6);
        assertEquals(f6, instance.getFunction(6));
        assertEquals( 1, lnis.outbound.size(), "number of messages is 1");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(0).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F0F6,
            lnis.outbound.get(0).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( 0, lnis.outbound.get(0).getElement(4) & 0x40,
            "sent f6 in correct state");
        f6 = true;
        instance.setFunction(6,f6);
        assertEquals(f6, instance.getFunction(6));
        assertEquals( 2, lnis.outbound.size(), "number of messages is 2");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(1).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F0F6,
            lnis.outbound.get(1).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( 0x40, lnis.outbound.get(1).getElement(4) & 0x40,
            "sent f6 in correct state");
    }

    /**
     * Test of setF7 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF7() {

        lnis.resetStatistics(); // and clears outbound message log
        boolean f7 = false;
        int func = 7;
        int bit = 0x01;
        instance.setFunction(func,f7);
        assertEquals(f7, instance.getFunction(func));
        assertEquals( 1, lnis.outbound.size(), "number of messages is 1");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(0).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F7F13,
            lnis.outbound.get(0).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( 0, lnis.outbound.get(0).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
        f7 = true;
        instance.setFunction(func,f7);
        assertEquals(f7, instance.getFunction(func));
        assertEquals( 2, lnis.outbound.size(), "number of messages is 2");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(1).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F7F13,
            lnis.outbound.get(1).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( bit, lnis.outbound.get(1).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
    }

    /**
     * Test of setF8 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF8() {

        lnis.resetStatistics(); // and clears outbound message log
        boolean f8 = false;
        int func = 8;
        int bit = 0x02;
        instance.setFunction(func,f8);
        assertEquals(f8, instance.getFunction(func));
        assertEquals( 1, lnis.outbound.size(), "number of messages is 1");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(0).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F7F13,
            lnis.outbound.get(0).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( 0, lnis.outbound.get(0).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
        f8 = true;
        instance.setFunction(func,f8);
        assertEquals(f8, instance.getFunction(func));
        assertEquals( 2, lnis.outbound.size(), "number of messages is 2");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(1).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F7F13,
            lnis.outbound.get(1).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( bit, lnis.outbound.get(1).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
    }

    /**
     * Test of setF9 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF9() {

        lnis.resetStatistics(); // and clears outbound message log
        boolean f9 = false;
        int func = 9;
        int bit = 0x04;
        instance.setFunction(func,f9);
        assertEquals(f9, instance.getFunction(func));
        assertEquals( 1, lnis.outbound.size(), "number of messages is 1");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(0).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F7F13,
            lnis.outbound.get(0).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( 0, lnis.outbound.get(0).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
        f9 = true;
        instance.setFunction(func,f9);
        assertEquals(f9, instance.getFunction(func));
        assertEquals( 2, lnis.outbound.size(), "number of messages is 2");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(1).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F7F13,
            lnis.outbound.get(1).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( bit, lnis.outbound.get(1).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
    }

    /**
     * Test of setF10 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF10() {

        lnis.resetStatistics(); // and clears outbound message log
        boolean f10 = false;
        int func = 10;
        int bit = 0x08;
        instance.setFunction(func,f10);
        assertEquals(f10, instance.getFunction(func));
        assertEquals( 1, lnis.outbound.size(), "number of messages is 1");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(0).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F7F13,
            lnis.outbound.get(0).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( 0, lnis.outbound.get(0).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
        f10 = true;
        instance.setFunction(func,f10);
        assertEquals(f10, instance.getFunction(func));
        assertEquals( 2, lnis.outbound.size(), "number of messages is 2");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(1).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F7F13,
            lnis.outbound.get(1).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( bit, lnis.outbound.get(1).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
    }

    /**
     * Test of setF11 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF11() {

        lnis.resetStatistics(); // and clears outbound message log
        boolean f11 = false;
        int func = 11;
        int bit = 0x10;
        instance.setFunction(func,f11);
        assertEquals(f11, instance.getFunction(func));
        assertEquals( 1, lnis.outbound.size(), "number of messages is 1");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(0).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F7F13,
            lnis.outbound.get(0).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( 0, lnis.outbound.get(0).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
        f11 = true;
        instance.setFunction(func,f11);
        assertEquals(f11, instance.getFunction(func));
        assertEquals( 2, lnis.outbound.size(), "number of messages is 2");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(1).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F7F13,
            lnis.outbound.get(1).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( bit, lnis.outbound.get(1).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
    }

    /**
     * Test of setF12 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF12() {

        lnis.resetStatistics(); // and clears outbound message log
        boolean f12 = false;
        int func = 12;
        int bit = 0x20;
        instance.setFunction(func,f12);
        assertEquals(f12, instance.getFunction(func));
        assertEquals( 1, lnis.outbound.size(), "number of messages is 1");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(0).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F7F13,
            lnis.outbound.get(0).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( 0, lnis.outbound.get(0).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
        f12 = true;
        instance.setFunction(func,f12);
        assertEquals(f12, instance.getFunction(func));
        assertEquals( 2, lnis.outbound.size(), "number of messages is 2");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(1).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F7F13,
            lnis.outbound.get(1).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( bit, lnis.outbound.get(1).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
    }

    /**
     * Test of setF13 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF13() {

        lnis.resetStatistics(); // and clears outbound message log
        boolean f13 = false;
        int func = 13;
        int bit = 0x40;
        instance.setFunction(func,f13);
        assertEquals(f13, instance.getFunction(func));
        assertEquals( 1, lnis.outbound.size(), "number of messages is 1");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(0).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F7F13,
            lnis.outbound.get(0).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( 0, lnis.outbound.get(0).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
        f13 = true;
        instance.setFunction(func,f13);
        assertEquals(f13, instance.getFunction(func));
        assertEquals( 2, lnis.outbound.size(), "number of messages is 2");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(1).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F7F13,
            lnis.outbound.get(1).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( bit, lnis.outbound.get(1).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
    }

    /**
     * Test of setF14 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF14() {

        lnis.resetStatistics(); // and clears outbound message log
        boolean f14 = false;
        int func = 14;
        int bit = 0x01;
        instance.setFunction(func,f14);
        assertEquals(f14, instance.getFunction(func));
        assertEquals( 1, lnis.outbound.size(), "number of messages is 1");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(0).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F14F20,
            lnis.outbound.get(0).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( 0, lnis.outbound.get(0).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
        f14 = true;
        instance.setFunction(func,f14);
        assertEquals(f14, instance.getFunction(func));
        assertEquals( 2, lnis.outbound.size(), "number of messages is 2");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(1).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F14F20,
            lnis.outbound.get(1).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( bit, lnis.outbound.get(1).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
    }

    /**
     * Test of setF15 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF15() {

        lnis.resetStatistics(); // and clears outbound message log
        boolean f15 = false;
        int func = 15;
        int bit = 0x02;
        instance.setFunction(func,f15);
        assertEquals(f15, instance.getFunction(func));
        assertEquals( 1, lnis.outbound.size(), "number of messages is 1");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(0).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F14F20,
            lnis.outbound.get(0).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( 0, lnis.outbound.get(0).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
        f15 = true;
        instance.setFunction(func,f15);
        assertEquals(f15, instance.getFunction(func));
        assertEquals( 2, lnis.outbound.size(), "number of messages is 2");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(1).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F14F20,
            lnis.outbound.get(1).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( bit, lnis.outbound.get(1).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
    }

    /**
     * Test of setF16 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF16() {

        lnis.resetStatistics(); // and clears outbound message log
        boolean f16 = false;
        int func = 16;
        int bit = 0x04;
        instance.setFunction(func,f16);
        assertEquals(f16, instance.getFunction(func));
        assertEquals( 1, lnis.outbound.size(), "number of messages is 1");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(0).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F14F20,
            lnis.outbound.get(0).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( 0, lnis.outbound.get(0).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
        f16 = true;
        instance.setFunction(func,f16);
        assertEquals(f16, instance.getFunction(func));
        assertEquals( 2, lnis.outbound.size(), "number of messages is 2");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(1).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F14F20,
            lnis.outbound.get(1).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( bit, lnis.outbound.get(1).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
    }

    /**
     * Test of setF17 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF17() {

        lnis.resetStatistics(); // and clears outbound message log
        boolean f17 = false;
        int func = 17;
        int bit = 0x08;
        instance.setFunction(func,f17);
        assertEquals(f17, instance.getFunction(func));
        assertEquals( 1, lnis.outbound.size(), "number of messages is 1");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(0).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F14F20,
            lnis.outbound.get(0).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( 0, lnis.outbound.get(0).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
        f17 = true;
        instance.setFunction(func,f17);
        assertEquals(f17, instance.getFunction(func));
        assertEquals( 2, lnis.outbound.size(), "number of messages is 2");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(1).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F14F20,
            lnis.outbound.get(1).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( bit, lnis.outbound.get(1).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
    }

    /**
     * Test of setF18 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF18() {

        lnis.resetStatistics(); // and clears outbound message log
        boolean f18 = false;
        int func = 18;
        int bit = 0x10;
        instance.setFunction(func,f18);
        assertEquals(f18, instance.getFunction(func));
        assertEquals( 1, lnis.outbound.size(), "number of messages is 1");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(0).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F14F20,
            lnis.outbound.get(0).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( 0, lnis.outbound.get(0).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
        f18 = true;
        instance.setFunction(func,f18);
        assertEquals(f18, instance.getFunction(func));
        assertEquals( 2, lnis.outbound.size(), "number of messages is 2");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(1).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F14F20,
            lnis.outbound.get(1).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( bit, lnis.outbound.get(1).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
    }

    /**
     * Test of setF19 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF19() {

        lnis.resetStatistics(); // and clears outbound message log
        boolean f19 = false;
        int func = 19;
        int bit = 0x20;
        instance.setFunction(func,f19);
        assertEquals(f19, instance.getFunction(func));
        assertEquals( 1, lnis.outbound.size(), "number of messages is 1");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(0).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F14F20,
            lnis.outbound.get(0).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( 0, lnis.outbound.get(0).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
        f19 = true;
        instance.setFunction(func,f19);
        assertEquals(f19, instance.getFunction(func));
        assertEquals( 2, lnis.outbound.size(), "number of messages is 2");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(1).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F14F20,
            lnis.outbound.get(1).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( bit, lnis.outbound.get(1).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
    }

    /**
     * Test of setF20 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF20() {

        lnis.resetStatistics(); // and clears outbound message log
        boolean f20 = false;
        int func = 20;
        int bit = 0x40;
        instance.setFunction(func,f20);
        assertEquals(f20, instance.getFunction(func));
        assertEquals( 1, lnis.outbound.size(), "number of messages is 1");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(0).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F14F20,
            lnis.outbound.get(0).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( 0, lnis.outbound.get(0).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
        f20 = true;
        instance.setFunction(func,f20);
        assertEquals(f20, instance.getFunction(func));
        assertEquals( 2, lnis.outbound.size(), "number of messages is 2");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(1).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F14F20,
            lnis.outbound.get(1).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( bit, lnis.outbound.get(1).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
    }

    /**
     * Test of setF21 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF21() {

        lnis.resetStatistics(); // and clears outbound message log
        boolean f21 = false;
        int func = 21;
        int bit = 0x01;
        instance.setFunction(func,f21);
        assertEquals(f21, instance.getFunction(func));
        assertEquals( 1, lnis.outbound.size(), "number of messages is 1");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(0).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F21F28_F28OFF,
            lnis.outbound.get(0).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( 0, lnis.outbound.get(0).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
        f21 = true;
        instance.setFunction(func,f21);
        assertEquals(f21, instance.getFunction(func));
        assertEquals( 2, lnis.outbound.size(), "number of messages is 2");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(1).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F21F28_F28OFF,
            lnis.outbound.get(1).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( bit, lnis.outbound.get(1).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
    }

    /**
     * Test of setF22 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF22() {

        lnis.resetStatistics(); // and clears outbound message log
        boolean f22 = false;
        int func = 22;
        int bit = 0x02;
        instance.setFunction(func,f22);
        assertEquals(f22, instance.getFunction(func));
        assertEquals( 1, lnis.outbound.size(), "number of messages is 1");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(0).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F21F28_F28OFF,
            lnis.outbound.get(0).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( 0, lnis.outbound.get(0).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
        f22 = true;
        instance.setFunction(func,f22);
        assertEquals(f22, instance.getFunction(func));
        assertEquals( 2, lnis.outbound.size(), "number of messages is 2");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(1).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F21F28_F28OFF,
            lnis.outbound.get(1).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( bit, lnis.outbound.get(1).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
    }

    /**
     * Test of setF23 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF23() {

        lnis.resetStatistics(); // and clears outbound message log
        boolean f23 = false;
        int func = 23;
        int bit = 0x04;
        instance.setFunction(func,f23);
        assertEquals(f23, instance.getFunction(func));
        assertEquals( 1, lnis.outbound.size(), "number of messages is 1");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(0).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F21F28_F28OFF,
            lnis.outbound.get(0).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( 0, lnis.outbound.get(0).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
        f23 = true;
        instance.setFunction(func,f23);
        assertEquals(f23, instance.getFunction(func));
        assertEquals( 2, lnis.outbound.size(), "number of messages is 2");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(1).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F21F28_F28OFF,
            lnis.outbound.get(1).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( bit, lnis.outbound.get(1).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
    }

    /**
     * Test of setF24 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF24() {

        lnis.resetStatistics(); // and clears outbound message log
        boolean f24 = false;
        int func = 24;
        int bit = 0x08;
        instance.setFunction(func,f24);
        assertEquals(f24, instance.getFunction(func));
        assertEquals( 1, lnis.outbound.size(), "number of messages is 1");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(0).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F21F28_F28OFF,
            lnis.outbound.get(0).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( 0, lnis.outbound.get(0).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
        f24 = true;
        instance.setFunction(func,f24);
        assertEquals(f24, instance.getFunction(func));
        assertEquals( 2, lnis.outbound.size(), "number of messages is 2");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(1).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F21F28_F28OFF,
            lnis.outbound.get(1).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( bit, lnis.outbound.get(1).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
    }

    /**
     * Test of setF25 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF25() {

        lnis.resetStatistics(); // and clears outbound message log
        boolean f25 = false;
        int func = 25;
        int bit = 0x10;
        instance.setFunction(func,f25);
        assertEquals(f25, instance.getFunction(func));
        assertEquals( 1, lnis.outbound.size(), "number of messages is 1");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(0).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F21F28_F28OFF,
            lnis.outbound.get(0).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( 0, lnis.outbound.get(0).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
        f25 = true;
        instance.setFunction(func,f25);
        assertEquals(f25, instance.getFunction(func));
        assertEquals( 2, lnis.outbound.size(), "number of messages is 2");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(1).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F21F28_F28OFF,
            lnis.outbound.get(1).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( bit, lnis.outbound.get(1).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
    }

    /**
     * Test of setF26 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF26() {

        lnis.resetStatistics(); // and clears outbound message log
        boolean f26 = false;
        int func = 26;
        int bit = 0x20;
        instance.setFunction(func,f26);
        assertEquals(f26, instance.getFunction(func));
        assertEquals( 1, lnis.outbound.size(), "number of messages is 1");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(0).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F21F28_F28OFF,
            lnis.outbound.get(0).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( 0, lnis.outbound.get(0).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
        f26 = true;
        instance.setFunction(func,f26);
        assertEquals(f26, instance.getFunction(func));
        assertEquals( 2, lnis.outbound.size(), "number of messages is 2");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(1).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F21F28_F28OFF,
            lnis.outbound.get(1).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( bit, lnis.outbound.get(1).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
    }

    /**
     * Test of setF27 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF27() {

        lnis.resetStatistics(); // and clears outbound message log
        boolean f27 = false;
        int func = 27;
        int bit = 0x40;
        instance.setFunction(func,f27);
        assertEquals(f27, instance.getFunction(func));
        assertEquals( 1, lnis.outbound.size(), "number of messages is 1");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(0).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F21F28_F28OFF,
            lnis.outbound.get(0).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( 0, lnis.outbound.get(0).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
        f27 = true;
        instance.setFunction(func,f27);
        assertEquals(f27, instance.getFunction(func));
        assertEquals( 2, lnis.outbound.size(), "number of messages is 2");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(1).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F21F28_F28OFF,
            lnis.outbound.get(1).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( bit, lnis.outbound.get(1).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
    }

    /**
     * Test of setF28 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSetF28() {

        lnis.resetStatistics(); // and clears outbound message log
        boolean f28 = false;
        int func = 28;
        int bit = 0xFF;   // all off both ways
        instance.setFunction(func,f28);
        assertEquals(f28, instance.getFunction(func));
        assertEquals( 1, lnis.outbound.size(), "number of messages is 1");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(0).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F21F28_F28OFF,
            lnis.outbound.get(0).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( 0, lnis.outbound.get(0).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
        f28 = true;
        instance.setFunction(func,f28);
        assertEquals(f28, instance.getFunction(func));
        assertEquals( 2, lnis.outbound.size(), "number of messages is 2");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR,
            lnis.outbound.get(1).getOpCode(), "opcode is OPC_EXP");
        assertEquals( LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F21F28_F28ON,
            lnis.outbound.get(1).getElement(1) & 0x78, "opcode is OPC_EXP2");
        assertEquals( 0, lnis.outbound.get(1).getElement(4) & bit,
            () -> "sent " + func + " in correct state");
    }

    /**
     * Test of sendFunction 29
     */
    @Test
    public void testSendExpFunctionF29() {

        lnis.resetStatistics(); // and clears outbound message log
        int func = 29;
        LocoNetMessage funcOnMess = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7f, 0x33, 0x02, 0x00, 0x58, 0x01, 0x00, 0x00, 0x00});
        LocoNetMessage funcOffMess = new LocoNetMessage(new int [] {0xED, 0x0B, 0x7F, 0x33, 0x02, 0x00, 0x58, 0x00, 0x00, 0x00, 0x00});
        assertEquals( 0, lnis.outbound.size(), () -> "check send of function exp f" + func);
        instance.setFunction(func,true);
        assertEquals( 1, lnis.outbound.size(), () -> "check send of function" + func);
        assertTrue( funcOnMess.equals(lnis.outbound.get(0)), "check opcode");
        instance.setFunction(func,false);
        assertEquals( 2, lnis.outbound.size(), "check send OFF function" + func);
        assertTrue( funcOffMess.equals(lnis.outbound.get(1)), "check opcode");
    }

    /**
     * Test of sendFunction 68
     */
    @Test
    public void testSendExpFunctionF68() {

        lnis.resetStatistics(); // and clears outbound message log
        int func = 68;
        LocoNetMessage funcOnMess = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7f, 0x33, 0x06, 0x00, 0x5C, 0x00, 0x00, 0x00, 0x00});
        LocoNetMessage funcOffMess = new LocoNetMessage(new int [] {0xED, 0x0B, 0x7F, 0x33, 0x02, 0x00, 0x5C, 0x00, 0x00, 0x00, 0x00});
        assertEquals( 0, lnis.outbound.size(), () -> "check send of function exp f" + func);
        instance.setFunction(func,true);
        assertEquals( 1, lnis.outbound.size(), () -> "check send of function" + func);
        assertTrue( funcOnMess.equals(lnis.outbound.get(0)), "check opcode");
        instance.setFunction(func,false);
        assertEquals( 2, lnis.outbound.size(), () -> "check send OFF function" + func);
        assertTrue( funcOffMess.equals(lnis.outbound.get(1)), "check opcode");
    }

    /**
     * Test of sendFunctionGroup1 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSendFunctionGroup1() {

        lnis.resetStatistics(); // and clears outbound message log
        instance.setFunction( 0, false);
        instance.setFunction( 1, true);
        instance.setFunction( 2, true);
        instance.setFunction( 3, false);
        instance.setIsForward(true);

        lnis.resetStatistics();  // and clears outbound message log
        assertEquals( 0, lnis.outbound.size(), "check send of function group 1 (0)");
        ((LocoNetThrottle)instance).sendFunctionGroup1();
        assertEquals( 1, lnis.outbound.size(), "check send of function group 1 (1)");
        assertEquals( LnConstants.OPC_LOCO_DIRF, lnis.outbound.get(0).getOpCode(), "check opcode");
        assertEquals( 0x03, lnis.outbound.get(0).getElement(2), "check dirf byte");


        lnis.resetStatistics(); // and clears outbound message log
        instance.setIsForward(false);
        assertEquals( 1, lnis.outbound.size(), "check send of function group 1 (2)");
        ((LocoNetThrottle)instance).sendFunctionGroup1();
        assertEquals( 2, lnis.outbound.size(), "check send of function group 1 (3)");
        assertEquals( LnConstants.OPC_LOCO_DIRF, lnis.outbound.get(1).getOpCode(), "check opcode");
        assertEquals( 0x023, lnis.outbound.get(1).getElement(2), "check dirf byte {4}");

    }

    /**
     * Test of sendFunctionGroup2 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSendFunctionGroup2() {

        for (int i = 5; i <9; ++i ) {
            instance.setFunction( 5, i==5);
            instance.setFunction( 6, i==6);
            instance.setFunction( 7, i==7);
            instance.setFunction( 8, i==8);

            lnis.resetStatistics(); // and clears outbound message log
            ((LocoNetThrottle)instance).sendFunctionGroup2();

            assertEquals( 1, lnis.outbound.size(),
                "check send of function group 2 for F"+i+" (0)");
            assertEquals( LnConstants.OPC_LOCO_SND, lnis.outbound.get(0).getOpCode(),
                "check opcode is OPC_LOCO_SND for F"+i+"");
            assertEquals( 1<<(i-5), lnis.outbound.get(0).getElement(2),
                "check byte 2 for F"+i+"{0}");

            instance.setFunction( 5, !(i==5));
            instance.setFunction( 6, !(i==6));
            instance.setFunction( 7, !(i==7));
            instance.setFunction( 8, !(i==8));

            lnis.resetStatistics(); // and clears outbound message log
            ((LocoNetThrottle)instance).sendFunctionGroup2();

            assertEquals( 1, lnis.outbound.size(),
                "check send of function group 2 for !F"+i+"(1)");
            assertEquals( LnConstants.OPC_LOCO_SND, lnis.outbound.get(0).getOpCode(),
                "check opcode is OPC_LOCO_SND for F"+i+"");
            assertEquals( 0x0f - (1<<(i-5)), lnis.outbound.get(0).getElement(2),
                "check byte 2 for !F"+i+"{1}");
        }
    }

    /**
     * Test of sendFunctionGroup3 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSendFunctionGroup3() {

        for (int i = 9; i <13; ++i ) {
            instance.setFunction(9, i==9);
            instance.setFunction(10, i==10);
            instance.setFunction(11, i==11);
            instance.setFunction(12, i==12);

            lnis.resetStatistics(); // and clears outbound message log
            ((LocoNetThrottle)instance).sendFunctionGroup3();

            assertEquals( 1, lnis.outbound.size(), "check send of function group 3 for F"+i+" (0)");
            assertEquals( LnConstants.OPC_IMM_PACKET, lnis.outbound.get(0).getOpCode(),
                "check opcode is OPC_IMM_PACKET for F"+i+"");
            assertEquals( 0x0b, lnis.outbound.get(0).getElement(1), "check byte 1 for F"+i+"{0}");
            assertEquals( 0x7f, lnis.outbound.get(0).getElement(2), "check byte 2 for F"+i+"{0}");
            assertEquals( 0x23, lnis.outbound.get(0).getElement(3), "check byte 3 for F"+i+"{0}");
            assertEquals( 0x02, lnis.outbound.get(0).getElement(4), "check byte 4 for F"+i+"{0}");
            assertEquals( 0x00, lnis.outbound.get(0).getElement(5), "check byte 5 for F"+i+"{0}");
            assertEquals( 0x20+(1<<(i-9)), lnis.outbound.get(0).getElement(6), "check byte 6 for F"+i+"{0}");
            assertEquals( 0x00, lnis.outbound.get(0).getElement(7), "check byte 7 for F"+i+"{0}");
            assertEquals( 0x00, lnis.outbound.get(0).getElement(8), "check byte 8 for F"+i+"{0}");
            assertEquals( 0x00, lnis.outbound.get(0).getElement(9), "check byte 9 for F"+i+"{0}");

            instance.setFunction(9, !(i==9));
            instance.setFunction(10, !(i==10));
            instance.setFunction(11, !(i==11));
            instance.setFunction(12, !(i==12));

            lnis.resetStatistics(); // and clears outbound message log
            ((LocoNetThrottle)instance).sendFunctionGroup3();

            assertEquals( 1, lnis.outbound.size(), "check send of function group 3 for !F"+i+"(1)");
            assertEquals( LnConstants.OPC_IMM_PACKET, lnis.outbound.get(0).getOpCode(),
                "check opcode is OPC_IMM_PACKET for !F"+i+"{1}");
            assertEquals( 0x0b, lnis.outbound.get(0).getElement(1), "check byte 1 for !F"+i+"{1}");
            assertEquals( 0x7f, lnis.outbound.get(0).getElement(2), "check byte 2 for !F"+i+"{1}");
            assertEquals( 0x23, lnis.outbound.get(0).getElement(3), "check byte 3 for !F"+i+"{1}");
            assertEquals( 0x02, lnis.outbound.get(0).getElement(4), "check byte 4 for !F"+i+"{1}");
            assertEquals( 0x00, lnis.outbound.get(0).getElement(5), "check byte 5 for !F"+i+"{1}");
            assertEquals( 0x2F-(1<<(i-9)), lnis.outbound.get(0).getElement(6), "check byte 6 for !F"+i+"{1}");
            assertEquals( 0x00, lnis.outbound.get(0).getElement(7), "check byte 7 for !F"+i+"{1}");
            assertEquals( 0x00, lnis.outbound.get(0).getElement(8), "check byte 8 for !F"+i+"{1}");
            assertEquals( 0x00, lnis.outbound.get(0).getElement(9), "check byte 9 for 1F"+i+"{1}");
        }
    }

    /**
     * Test of sendFunctionGroup4 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSendFunctionGroup4() {
        for (int i = 13; i <21; ++i ) {
            instance.setFunction( 13, i==13);
            instance.setFunction( 14, i==14);
            instance.setFunction( 15, i==15);
            instance.setFunction( 16, i==16);
            instance.setFunction( 17, i==17);
            instance.setFunction( 18, i==18);
            instance.setFunction( 19, i==19);
            instance.setFunction( 20, i==20);

            lnis.resetStatistics(); // and clears outbound message log
            ((LocoNetThrottle)instance).sendFunctionGroup4();

            assertEquals( 1, lnis.outbound.size(), "check send of function group 4 for F"+i+" (0)");
            assertEquals( LnConstants.OPC_IMM_PACKET, lnis.outbound.get(0).getOpCode(),
                "check opcode is OPC_IMM_PACKET for F"+i+"");
            assertEquals( 0x0b, lnis.outbound.get(0).getElement(1), "check byte 1 for F"+i+"{0}");
            assertEquals( 0x7f, lnis.outbound.get(0).getElement(2), "check byte 2 for F"+i+"{0}");
            assertEquals( 0x33, lnis.outbound.get(0).getElement(3), "check byte 3 for F"+i+"{0}");
            assertEquals( (i==20)?0x06:0x02, lnis.outbound.get(0).getElement(4), "check byte 4 for F"+i+"{0}");
            assertEquals( 0x00, lnis.outbound.get(0).getElement(5), "check byte 5 for F"+i+"{0}");
            assertEquals( 0x5e, lnis.outbound.get(0).getElement(6), "check byte 6 for F"+i+"{0}");
            assertEquals( (i < 20)?(1<<(i-13)):0, lnis.outbound.get(0).getElement(7), "check byte 7 for F"+i+"{0}");
            assertEquals( 0x00, lnis.outbound.get(0).getElement(8), "check byte 8 for F"+i+"{0}");
            assertEquals( 0x00, lnis.outbound.get(0).getElement(9), "check byte 9 for F"+i+"{0}");

            instance.setFunction( 13, !(i==13));
            instance.setFunction( 14, !(i==14));
            instance.setFunction( 15, !(i==15));
            instance.setFunction( 16, !(i==16));
            instance.setFunction( 17, !(i==17));
            instance.setFunction( 18, !(i==18));
            instance.setFunction( 19, !(i==19));
            instance.setFunction( 20, !(i==20));

            lnis.resetStatistics(); // and clears outbound message log
            ((LocoNetThrottle)instance).sendFunctionGroup4();

            assertEquals( 1, lnis.outbound.size(), "check send of function group 4 for !F"+i+"(1)");
            assertEquals( LnConstants.OPC_IMM_PACKET, lnis.outbound.get(0).getOpCode(),
                "check opcode is OPC_IMM_PACKET for !F"+i+"{1}");
            assertEquals( 0x0b, lnis.outbound.get(0).getElement(1), "check byte 1 for !F"+i+"{1}");
            assertEquals( 0x7f, lnis.outbound.get(0).getElement(2), "check byte 2 for !F"+i+"{1}");
            assertEquals( 0x33, lnis.outbound.get(0).getElement(3), "check byte 3 for !F"+i+"{1}");
            assertEquals( (i==20)?0x02:0x06, lnis.outbound.get(0).getElement(4), "check byte 4 for !F"+i+"{1}");
            assertEquals( 0x00, lnis.outbound.get(0).getElement(5), "check byte 5 for !F"+i+"{1}");
            assertEquals( 0x5e, lnis.outbound.get(0).getElement(6), "check byte 6 for !F"+i+"{1}");
            assertEquals( (i < 20)?(127-(1<<(i-13))):0x7f, lnis.outbound.get(0).getElement(7), "check byte 7 for !F"+i+"{1}");
            assertEquals( 0x00, lnis.outbound.get(0).getElement(8), "check byte 8 for !F"+i+"{1}");
            assertEquals( 0x00, lnis.outbound.get(0).getElement(9), "check byte 9 for 1F"+i+"{1}");
        }
    }

    /**
     * Test of sendFunctionGroup5 method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testSendFunctionGroup5() {
        for (int i = 21; i <29; ++i ) {
            instance.setFunction( 21, i==21);
            instance.setFunction( 22, i==22);
            instance.setFunction( 23, i==23);
            instance.setFunction( 24, i==24);
            instance.setFunction( 25, i==25);
            instance.setFunction( 26, i==26);
            instance.setFunction( 27, i==27);
            instance.setFunction( 28, i==28);

            lnis.resetStatistics(); // and clears outbound message log
            ((LocoNetThrottle)instance).sendFunctionGroup5();

            assertEquals( 1, lnis.outbound.size(), "check send of function group 5 for F"+i+" (0)");
            assertEquals( LnConstants.OPC_IMM_PACKET, lnis.outbound.get(0).getOpCode(),
                "check opcode is OPC_IMM_PACKET for F"+i+"");
            assertEquals( 0x0b, lnis.outbound.get(0).getElement(1), "check byte 1 for F"+i+"{0}");
            assertEquals( 0x7f, lnis.outbound.get(0).getElement(2), "check byte 2 for F"+i+"{0}");
            assertEquals( 0x33, lnis.outbound.get(0).getElement(3), "check byte 3 for F"+i+"{0}");
            assertEquals( (i==28)?0x06:0x02, lnis.outbound.get(0).getElement(4), "check byte 4 for F"+i+"{0}");
            assertEquals( 0x00, lnis.outbound.get(0).getElement(5), "check byte 5 for F"+i+"{0}");
            assertEquals( 0x5f, lnis.outbound.get(0).getElement(6), "check byte 6 for F"+i+"{0}");
            assertEquals( (i < 28)?(1<<(i-21)):0, lnis.outbound.get(0).getElement(7), "check byte 7 for F"+i+"{0}");
            assertEquals( 0x00, lnis.outbound.get(0).getElement(8), "check byte 8 for F"+i+"{0}");
            assertEquals( 0x00, lnis.outbound.get(0).getElement(9), "check byte 9 for F"+i+"{0}");

            instance.setFunction( 21, !(i==21));
            instance.setFunction( 22, !(i==22));
            instance.setFunction( 23, !(i==23));
            instance.setFunction( 24, !(i==24));
            instance.setFunction( 25, !(i==25));
            instance.setFunction( 26, !(i==26));
            instance.setFunction( 27, !(i==27));
            instance.setFunction( 28, !(i==28));

            lnis.resetStatistics(); // and clears outbound message log
            ((LocoNetThrottle)instance).sendFunctionGroup5();

            assertEquals( 1, lnis.outbound.size(), "check send of function group 5 for !F"+i+"(1)");
            assertEquals( LnConstants.OPC_IMM_PACKET, lnis.outbound.get(0).getOpCode(),
                "check opcode is OPC_IMM_PACKET for !F"+i+"{1}");
            assertEquals( 0x0b, lnis.outbound.get(0).getElement(1), "check byte 1 for !F"+i+"{1}");
            assertEquals( 0x7f, lnis.outbound.get(0).getElement(2), "check byte 2 for !F"+i+"{1}");
            assertEquals( 0x33, lnis.outbound.get(0).getElement(3), "check byte 3 for !F"+i+"{1}");
            assertEquals( (i==28)?0x02:0x06, lnis.outbound.get(0).getElement(4), "check byte 4 for !F"+i+"{1}");
            assertEquals( 0x00, lnis.outbound.get(0).getElement(5), "check byte 5 for !F"+i+"{1}");
            assertEquals( 0x5f, lnis.outbound.get(0).getElement(6), "check byte 6 for !F"+i+"{1}");
            assertEquals( (i < 28)?(127-(1<<(i-21))):0x7f, lnis.outbound.get(0).getElement(7), "check byte 7 for !F"+i+"{1}");
            assertEquals( 0x00, lnis.outbound.get(0).getElement(8), "check byte 8 for !F"+i+"{1}");
            assertEquals( 0x00, lnis.outbound.get(0).getElement(9), "check byte 9 for 1F"+i+"{1}");

        }
    }

    /**
     * Test of getF2Momentary method, of class AbstractThrottle.
     */
    @Test
    @Override
    public void testGetF2Momentary() {
        boolean expResult = true;
        boolean result = instance.getFunctionMomentary(2);
        assertEquals( expResult, result, "Check F2 Momentary true");

        expResult = false;
        instance.setFunctionMomentary( 2, false);
        result = instance.getFunctionMomentary(2);
        assertEquals( expResult, result, "Check F2 Momentary false");

    }

    private LocoNetInterfaceScaffold lnis;
    private SlotManager slotmanager;
    private LocoNetSystemConnectionMemo memo = null;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        // prepare an interface
        maxFns = 69;

        lnis = new LocoNetInterfaceScaffold();
        slotmanager = new SlotManager(lnis);
        slotmanager.setLoconetProtocolAutoDetect(true);

        // set slot 3 to address 3
        LocoNetMessage m = new LocoNetMessage(21);
        m.setOpCode(LnConstants.OPC_EXP_WR_SL_DATA);
        m.setElement(1, 0x15);
        m.setElement(3, 0x03);
        m.setElement(4, 0x00);
        m.setElement(7, 0x47);
        slotmanager.slot(3).setSlot(m);

        memo = new LocoNetSystemConnectionMemo(lnis, slotmanager);
        memo.setThrottleManager(new LnThrottleManager(memo));
        memo.store(slotmanager,CommandStation.class);

        jmri.InstanceManager.setDefault(jmri.ThrottleManager.class, memo.getThrottleManager());
        // use slot 3
        instance = new LocoNetThrottle(memo, slotmanager.slot(3)); // creates throttle in exp mode

    }

    @AfterEach
    @Override
    public void tearDown() {
        memo.getThrottleManager().dispose();
        memo.dispose();
        memo = null;
        lnis = null;
        JUnitUtil.tearDown();
    }

}
