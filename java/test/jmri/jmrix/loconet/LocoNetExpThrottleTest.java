    package jmri.jmrix.loconet;

    import jmri.CommandStation;
    import jmri.util.JUnitUtil;
    import jmri.SpeedStepMode;

    import org.junit.Assert;
    import org.junit.jupiter.api.*;

    public class LocoNetExpThrottleTest extends jmri.jmrix.AbstractThrottleTest {

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
            super.testGetSpeed_float();
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
            instance.setFunction(0,f0);
            Assert.assertEquals(f0, instance.getFunction(0));
            Assert.assertEquals("number of messages is 1", 1, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(0).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F0F6, lnis.outbound.get(0).getElement(1) & 0x78);
            Assert.assertEquals("sent f0 in correct state", 0, lnis.outbound.get(0).getElement(4) & LnConstants.DIRF_F0);
            f0 = true;
            instance.setFunction(0,f0);
            Assert.assertEquals(f0, instance.getFunction(0));
            Assert.assertEquals("number of messages is 2", 2, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(1).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F0F6, lnis.outbound.get(1).getElement(1) & 0x78);
            Assert.assertEquals("sent f0 in correct state", LnConstants.DIRF_F0, lnis.outbound.get(1).getElement(4) & LnConstants.DIRF_F0);
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
            instance.setFunction(1,f1);
            Assert.assertEquals(f1, instance.getFunction(1));
            Assert.assertEquals("number of messages is 1", 1, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(0).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F0F6, lnis.outbound.get(0).getElement(1) & 0x78);
            Assert.assertEquals("sent f0 in correct state", 0, lnis.outbound.get(0).getElement(4) & LnConstants.DIRF_F1);
            f1 = true;
            instance.setFunction(1,f1);
            Assert.assertEquals(f1, instance.getFunction(1));
            Assert.assertEquals("number of messages is 2", 2, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(1).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F0F6, lnis.outbound.get(1).getElement(1) & 0x78);
            Assert.assertEquals("sent f1 in correct state", LnConstants.DIRF_F1, lnis.outbound.get(1).getElement(4) & LnConstants.DIRF_F1);
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
            instance.setFunction(2,f2);
            Assert.assertEquals(f2, instance.getFunction(2));
            Assert.assertEquals("number of messages is 1", 1, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(0).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F0F6, lnis.outbound.get(0).getElement(1) & 0x78);
            Assert.assertEquals("sent f2 in correct state", 0, lnis.outbound.get(0).getElement(4) & LnConstants.DIRF_F2);
            f2 = true;
            instance.setFunction(2,f2);
            Assert.assertEquals(f2, instance.getFunction(2));
            Assert.assertEquals("number of messages is 2", 2, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(1).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F0F6, lnis.outbound.get(1).getElement(1) & 0x78);
            Assert.assertEquals("sent f2 in correct state", LnConstants.DIRF_F2, lnis.outbound.get(1).getElement(4) & LnConstants.DIRF_F2);
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
            instance.setFunction(3,f3);
            Assert.assertEquals(f3, instance.getFunction(3));
            Assert.assertEquals("number of messages is 1", 1, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(0).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F0F6, lnis.outbound.get(0).getElement(1) & 0x78);
            Assert.assertEquals("sent f3 in correct state", 0, lnis.outbound.get(0).getElement(4) & LnConstants.DIRF_F3);
            f3 = true;
            instance.setFunction(3,f3);
            Assert.assertEquals(f3, instance.getFunction(3));
            Assert.assertEquals("number of messages is 2", 2, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(1).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F0F6, lnis.outbound.get(1).getElement(1) & 0x78);
            Assert.assertEquals("sent f3 in correct state", LnConstants.DIRF_F3, lnis.outbound.get(1).getElement(4) & LnConstants.DIRF_F3);
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
            instance.setFunction(4,f4);
            Assert.assertEquals(f4, instance.getFunction(4));
            Assert.assertEquals("number of messages is 1", 1, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(0).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F0F6, lnis.outbound.get(0).getElement(1) & 0x78);
            Assert.assertEquals("sent f4 in correct state", 0, lnis.outbound.get(0).getElement(4) & LnConstants.DIRF_F4);
            f4 = true;
            instance.setFunction(4,f4);
            Assert.assertEquals(f4, instance.getFunction(4));
            Assert.assertEquals("number of messages is 2", 2, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(1).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F0F6, lnis.outbound.get(1).getElement(1) & 0x78);
            Assert.assertEquals("sent f4 in correct state", LnConstants.DIRF_F4, lnis.outbound.get(1).getElement(4) & LnConstants.DIRF_F4);
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
            instance.setFunction(5,f5);
            Assert.assertEquals(f5, instance.getFunction(5));
            Assert.assertEquals("number of messages is 1", 1, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(0).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F0F6, lnis.outbound.get(0).getElement(1) & 0x78);
            Assert.assertEquals("sent f5 in correct state", 0, lnis.outbound.get(0).getElement(4) & 0x20);
            f5 = true;
            instance.setFunction(5,f5);
            Assert.assertEquals(f5, instance.getFunction(5));
            Assert.assertEquals("number of messages is 2", 2, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(1).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F0F6, lnis.outbound.get(1).getElement(1) & 0x78);
            Assert.assertEquals("sent f5 in correct state", 0x20, lnis.outbound.get(1).getElement(4) & 0x20);
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
            instance.setFunction(6,f6);
            Assert.assertEquals(f6, instance.getFunction(6));
            Assert.assertEquals("number of messages is 1", 1, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(0).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F0F6, lnis.outbound.get(0).getElement(1) & 0x78);
            Assert.assertEquals("sent f6 in correct state", 0, lnis.outbound.get(0).getElement(4) & 0x40);
            f6 = true;
            instance.setFunction(6,f6);
            Assert.assertEquals(f6, instance.getFunction(6));
            Assert.assertEquals("number of messages is 2", 2, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(1).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F0F6, lnis.outbound.get(1).getElement(1) & 0x78);
            Assert.assertEquals("sent f6 in correct state", 0x40, lnis.outbound.get(1).getElement(4) & 0x40);
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
            int func = 7;
            instance.setFunction(func,f7);
            Assert.assertEquals(f7, instance.getFunction(func));
            Assert.assertEquals("number of messages is 1", 1, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(0).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F7F13, lnis.outbound.get(0).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", 0, lnis.outbound.get(0).getElement(4) & 0x01);
            f7 = true;
            instance.setFunction(func,f7);
            Assert.assertEquals(f7, instance.getFunction(func));
            Assert.assertEquals("number of messages is 2", 2, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(1).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F7F13, lnis.outbound.get(1).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", 0x01, lnis.outbound.get(1).getElement(4) & 0x01);
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
            int func = 8;
            int bit = 0x02;
            instance.setFunction(func,f8);
            Assert.assertEquals(f8, instance.getFunction(func));
            Assert.assertEquals("number of messages is 1", 1, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(0).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F7F13, lnis.outbound.get(0).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", 0, lnis.outbound.get(0).getElement(4) & bit);
            f8 = true;
            instance.setFunction(func,f8);
            Assert.assertEquals(f8, instance.getFunction(func));
            Assert.assertEquals("number of messages is 2", 2, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(1).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F7F13, lnis.outbound.get(1).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", bit, lnis.outbound.get(1).getElement(4) & bit);
        }

        /**
         * Test of setF9 method, of class AbstractThrottle.
         */
        @Test
        @Override
        public void testSetF9() {
            lnis.outbound.clear();
            lnis.resetStatistics();
            boolean f9 = false;
            int func = 9;
            int bit = 0x04;
            instance.setFunction(func,f9);
            Assert.assertEquals(f9, instance.getFunction(func));
            Assert.assertEquals("number of messages is 1", 1, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(0).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F7F13, lnis.outbound.get(0).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", 0, lnis.outbound.get(0).getElement(4) & bit);
            f9 = true;
            instance.setFunction(func,f9);
            Assert.assertEquals(f9, instance.getFunction(func));
            Assert.assertEquals("number of messages is 2", 2, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(1).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F7F13, lnis.outbound.get(1).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", bit, lnis.outbound.get(1).getElement(4) & bit);
        }

        /**
         * Test of setF10 method, of class AbstractThrottle.
         */
        @Test
        @Override
        public void testSetF10() {
            lnis.outbound.clear();
            lnis.resetStatistics();
            boolean f10 = false;
            int func = 10;
            int bit = 0x08;
            instance.setFunction(func,f10);
            Assert.assertEquals(f10, instance.getFunction(func));
            Assert.assertEquals("number of messages is 1", 1, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(0).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F7F13, lnis.outbound.get(0).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", 0, lnis.outbound.get(0).getElement(4) & bit);
            f10 = true;
            instance.setFunction(func,f10);
            Assert.assertEquals(f10, instance.getFunction(func));
            Assert.assertEquals("number of messages is 2", 2, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(1).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F7F13, lnis.outbound.get(1).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", bit, lnis.outbound.get(1).getElement(4) & bit);
        }

        /**
         * Test of setF11 method, of class AbstractThrottle.
         */
        @Test
        @Override
        public void testSetF11() {
            lnis.outbound.clear();
            lnis.resetStatistics();
            boolean f11 = false;
            int func = 11;
            int bit = 0x10;
            instance.setFunction(func,f11);
            Assert.assertEquals(f11, instance.getFunction(func));
            Assert.assertEquals("number of messages is 1", 1, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(0).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F7F13, lnis.outbound.get(0).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", 0, lnis.outbound.get(0).getElement(4) & bit);
            f11 = true;
            instance.setFunction(func,f11);
            Assert.assertEquals(f11, instance.getFunction(func));
            Assert.assertEquals("number of messages is 2", 2, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(1).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F7F13, lnis.outbound.get(1).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", bit, lnis.outbound.get(1).getElement(4) & bit);
        }

        /**
         * Test of setF12 method, of class AbstractThrottle.
         */
        @Test
        @Override
        public void testSetF12() {
            lnis.outbound.clear();
            lnis.resetStatistics();
            boolean f12 = false;
            int func = 12;
            int bit = 0x20;
            instance.setFunction(func,f12);
            Assert.assertEquals(f12, instance.getFunction(func));
            Assert.assertEquals("number of messages is 1", 1, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(0).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F7F13, lnis.outbound.get(0).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", 0, lnis.outbound.get(0).getElement(4) & bit);
            f12 = true;
            instance.setFunction(func,f12);
            Assert.assertEquals(f12, instance.getFunction(func));
            Assert.assertEquals("number of messages is 2", 2, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(1).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F7F13, lnis.outbound.get(1).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", bit, lnis.outbound.get(1).getElement(4) & bit);
        }

        /**
         * Test of setF13 method, of class AbstractThrottle.
         */
        @Test
        @Override
        public void testSetF13() {
            lnis.outbound.clear();
            lnis.resetStatistics();
            boolean f13 = false;
            int func = 13;
            int bit = 0x40;
            instance.setFunction(func,f13);
            Assert.assertEquals(f13, instance.getFunction(func));
            Assert.assertEquals("number of messages is 1", 1, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(0).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F7F13, lnis.outbound.get(0).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", 0, lnis.outbound.get(0).getElement(4) & bit);
            f13 = true;
            instance.setFunction(func,f13);
            Assert.assertEquals(f13, instance.getFunction(func));
            Assert.assertEquals("number of messages is 2", 2, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(1).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F7F13, lnis.outbound.get(1).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", bit, lnis.outbound.get(1).getElement(4) & bit);
        }

        /**
         * Test of setF14 method, of class AbstractThrottle.
         */
        @Test
        @Override
        public void testSetF14() {
            lnis.outbound.clear();
            lnis.resetStatistics();
            boolean f14 = false;
            int func = 14;
            int bit = 0x01;
            instance.setFunction(func,f14);
            Assert.assertEquals(f14, instance.getFunction(func));
            Assert.assertEquals("number of messages is 1", 1, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(0).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F14F20, lnis.outbound.get(0).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", 0, lnis.outbound.get(0).getElement(4) & bit);
            f14 = true;
            instance.setFunction(func,f14);
            Assert.assertEquals(f14, instance.getFunction(func));
            Assert.assertEquals("number of messages is 2", 2, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(1).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F14F20, lnis.outbound.get(1).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", bit, lnis.outbound.get(1).getElement(4) & bit);
        }

        /**
         * Test of setF15 method, of class AbstractThrottle.
         */
        @Test
        @Override
        public void testSetF15() {
            lnis.outbound.clear();
            lnis.resetStatistics();
            boolean f15 = false;
            int func = 15;
            int bit = 0x02;
            instance.setFunction(func,f15);
            Assert.assertEquals(f15, instance.getFunction(func));
            Assert.assertEquals("number of messages is 1", 1, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(0).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F14F20, lnis.outbound.get(0).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", 0, lnis.outbound.get(0).getElement(4) & bit);
            f15 = true;
            instance.setFunction(func,f15);
            Assert.assertEquals(f15, instance.getFunction(func));
            Assert.assertEquals("number of messages is 2", 2, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(1).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F14F20, lnis.outbound.get(1).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", bit, lnis.outbound.get(1).getElement(4) & bit);
        }

        /**
         * Test of setF16 method, of class AbstractThrottle.
         */
        @Test
        @Override
        public void testSetF16() {
            lnis.outbound.clear();
            lnis.resetStatistics();
            boolean f16 = false;
            int func = 16;
            int bit = 0x04;
            instance.setFunction(func,f16);
            Assert.assertEquals(f16, instance.getFunction(func));
            Assert.assertEquals("number of messages is 1", 1, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(0).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F14F20, lnis.outbound.get(0).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", 0, lnis.outbound.get(0).getElement(4) & bit);
            f16 = true;
            instance.setFunction(func,f16);
            Assert.assertEquals(f16, instance.getFunction(func));
            Assert.assertEquals("number of messages is 2", 2, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(1).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F14F20, lnis.outbound.get(1).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", bit, lnis.outbound.get(1).getElement(4) & bit);
        }

        /**
         * Test of setF17 method, of class AbstractThrottle.
         */
        @Test
        @Override
        public void testSetF17() {
            lnis.outbound.clear();
            lnis.resetStatistics();
            boolean f17 = false;
            int func = 17;
            int bit = 0x08;
            instance.setFunction(func,f17);
            Assert.assertEquals(f17, instance.getFunction(func));
            Assert.assertEquals("number of messages is 1", 1, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(0).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F14F20, lnis.outbound.get(0).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", 0, lnis.outbound.get(0).getElement(4) & bit);
            f17 = true;
            instance.setFunction(func,f17);
            Assert.assertEquals(f17, instance.getFunction(func));
            Assert.assertEquals("number of messages is 2", 2, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(1).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F14F20, lnis.outbound.get(1).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", bit, lnis.outbound.get(1).getElement(4) & bit);
        }

        /**
         * Test of setF18 method, of class AbstractThrottle.
         */
        @Test
        @Override
        public void testSetF18() {
            lnis.outbound.clear();
            lnis.resetStatistics();
            boolean f18 = false;
            int func = 18;
            int bit = 0x10;
            instance.setFunction(func,f18);
            Assert.assertEquals(f18, instance.getFunction(func));
            Assert.assertEquals("number of messages is 1", 1, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(0).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F14F20, lnis.outbound.get(0).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", 0, lnis.outbound.get(0).getElement(4) & bit);
            f18 = true;
            instance.setFunction(func,f18);
            Assert.assertEquals(f18, instance.getFunction(func));
            Assert.assertEquals("number of messages is 2", 2, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(1).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F14F20, lnis.outbound.get(1).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", bit, lnis.outbound.get(1).getElement(4) & bit);
        }

        /**
         * Test of setF19 method, of class AbstractThrottle.
         */
        @Test
        @Override
        public void testSetF19() {
            lnis.outbound.clear();
            lnis.resetStatistics();
            boolean f19 = false;
            int func = 19;
            int bit = 0x20;
            instance.setFunction(func,f19);
            Assert.assertEquals(f19, instance.getFunction(func));
            Assert.assertEquals("number of messages is 1", 1, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(0).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F14F20, lnis.outbound.get(0).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", 0, lnis.outbound.get(0).getElement(4) & bit);
            f19 = true;
            instance.setFunction(func,f19);
            Assert.assertEquals(f19, instance.getFunction(func));
            Assert.assertEquals("number of messages is 2", 2, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(1).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F14F20, lnis.outbound.get(1).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", bit, lnis.outbound.get(1).getElement(4) & bit);
        }

        /**
         * Test of setF20 method, of class AbstractThrottle.
         */
        @Test
        @Override
        public void testSetF20() {
            lnis.outbound.clear();
            lnis.resetStatistics();
            boolean f20 = false;
            int func = 20;
            int bit = 0x40;
            instance.setFunction(func,f20);
            Assert.assertEquals(f20, instance.getFunction(func));
            Assert.assertEquals("number of messages is 1", 1, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(0).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F14F20, lnis.outbound.get(0).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", 0, lnis.outbound.get(0).getElement(4) & bit);
            f20 = true;
            instance.setFunction(func,f20);
            Assert.assertEquals(f20, instance.getFunction(func));
            Assert.assertEquals("number of messages is 2", 2, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(1).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F14F20, lnis.outbound.get(1).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", bit, lnis.outbound.get(1).getElement(4) & bit);
        }

        /**
         * Test of setF21 method, of class AbstractThrottle.
         */
        @Test
        @Override
        public void testSetF21() {
            lnis.outbound.clear();
            lnis.resetStatistics();
            boolean f21 = false;
            int func = 21;
            int bit = 0x01;
            instance.setFunction(func,f21);
            Assert.assertEquals(f21, instance.getFunction(func));
            Assert.assertEquals("number of messages is 1", 1, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(0).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F21F28_F28OFF, lnis.outbound.get(0).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", 0, lnis.outbound.get(0).getElement(4) & bit);
            f21 = true;
            instance.setFunction(func,f21);
            Assert.assertEquals(f21, instance.getFunction(func));
            Assert.assertEquals("number of messages is 2", 2, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(1).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F21F28_F28OFF, lnis.outbound.get(1).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", bit, lnis.outbound.get(1).getElement(4) & bit);
        }

        /**
         * Test of setF22 method, of class AbstractThrottle.
         */
        @Test
        @Override
        public void testSetF22() {
            lnis.outbound.clear();
            lnis.resetStatistics();
            boolean f22 = false;
            int func = 22;
            int bit = 0x02;
            instance.setFunction(func,f22);
            Assert.assertEquals(f22, instance.getFunction(func));
            Assert.assertEquals("number of messages is 1", 1, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(0).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F21F28_F28OFF, lnis.outbound.get(0).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", 0, lnis.outbound.get(0).getElement(4) & bit);
            f22 = true;
            instance.setFunction(func,f22);
            Assert.assertEquals(f22, instance.getFunction(func));
            Assert.assertEquals("number of messages is 2", 2, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(1).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F21F28_F28OFF, lnis.outbound.get(1).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", bit, lnis.outbound.get(1).getElement(4) & bit);
        }

        /**
         * Test of setF23 method, of class AbstractThrottle.
         */
        @Test
        @Override
        public void testSetF23() {
            lnis.outbound.clear();
            lnis.resetStatistics();
            boolean f23 = false;
            int func = 23;
            int bit = 0x04;
            instance.setFunction(func,f23);
            Assert.assertEquals(f23, instance.getFunction(func));
            Assert.assertEquals("number of messages is 1", 1, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(0).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F21F28_F28OFF, lnis.outbound.get(0).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", 0, lnis.outbound.get(0).getElement(4) & bit);
            f23 = true;
            instance.setFunction(func,f23);
            Assert.assertEquals(f23, instance.getFunction(func));
            Assert.assertEquals("number of messages is 2", 2, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(1).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F21F28_F28OFF, lnis.outbound.get(1).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", bit, lnis.outbound.get(1).getElement(4) & bit);
        }

        /**
         * Test of setF24 method, of class AbstractThrottle.
         */
        @Test
        @Override
        public void testSetF24() {
            lnis.outbound.clear();
            lnis.resetStatistics();
            boolean f24 = false;
            int func = 24;
            int bit = 0x08;
            instance.setFunction(func,f24);
            Assert.assertEquals(f24, instance.getFunction(func));
            Assert.assertEquals("number of messages is 1", 1, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(0).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F21F28_F28OFF, lnis.outbound.get(0).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", 0, lnis.outbound.get(0).getElement(4) & bit);
            f24 = true;
            instance.setFunction(func,f24);
            Assert.assertEquals(f24, instance.getFunction(func));
            Assert.assertEquals("number of messages is 2", 2, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(1).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F21F28_F28OFF, lnis.outbound.get(1).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", bit, lnis.outbound.get(1).getElement(4) & bit);
        }

        /**
         * Test of setF25 method, of class AbstractThrottle.
         */
        @Test
        @Override
        public void testSetF25() {
            lnis.outbound.clear();
            lnis.resetStatistics();
            boolean f25 = false;
            int func = 25;
            int bit = 0x10;
            instance.setFunction(func,f25);
            Assert.assertEquals(f25, instance.getFunction(func));
            Assert.assertEquals("number of messages is 1", 1, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(0).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F21F28_F28OFF, lnis.outbound.get(0).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", 0, lnis.outbound.get(0).getElement(4) & bit);
            f25 = true;
            instance.setFunction(func,f25);
            Assert.assertEquals(f25, instance.getFunction(func));
            Assert.assertEquals("number of messages is 2", 2, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(1).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F21F28_F28OFF, lnis.outbound.get(1).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", bit, lnis.outbound.get(1).getElement(4) & bit);
        }

        /**
         * Test of setF26 method, of class AbstractThrottle.
         */
        @Test
        @Override
        public void testSetF26() {
            lnis.outbound.clear();
            lnis.resetStatistics();
            boolean f26 = false;
            int func = 26;
            int bit = 0x20;
            instance.setFunction(func,f26);
            Assert.assertEquals(f26, instance.getFunction(func));
            Assert.assertEquals("number of messages is 1", 1, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(0).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F21F28_F28OFF, lnis.outbound.get(0).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", 0, lnis.outbound.get(0).getElement(4) & bit);
            f26 = true;
            instance.setFunction(func,f26);
            Assert.assertEquals(f26, instance.getFunction(func));
            Assert.assertEquals("number of messages is 2", 2, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(1).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F21F28_F28OFF, lnis.outbound.get(1).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", bit, lnis.outbound.get(1).getElement(4) & bit);
        }

        /**
         * Test of setF27 method, of class AbstractThrottle.
         */
        @Test
        @Override
        public void testSetF27() {
            lnis.outbound.clear();
            lnis.resetStatistics();
            boolean f27 = false;
            int func = 27;
            int bit = 0x40;
            instance.setFunction(func,f27);
            Assert.assertEquals(f27, instance.getFunction(func));
            Assert.assertEquals("number of messages is 1", 1, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(0).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F21F28_F28OFF, lnis.outbound.get(0).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", 0, lnis.outbound.get(0).getElement(4) & bit);
            f27 = true;
            instance.setFunction(func,f27);
            Assert.assertEquals(f27, instance.getFunction(func));
            Assert.assertEquals("number of messages is 2", 2, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(1).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F21F28_F28OFF, lnis.outbound.get(1).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", bit, lnis.outbound.get(1).getElement(4) & bit);
        }

        /**
         * Test of setF28 method, of class AbstractThrottle.
         */
        @Test
        @Override
        public void testSetF28() {
            lnis.outbound.clear();
            lnis.resetStatistics();
            boolean f28 = false;
            int func = 28;
            int bit = 0xFF;   // all off both ways
            instance.setFunction(func,f28);
            Assert.assertEquals(f28, instance.getFunction(func));
            Assert.assertEquals("number of messages is 1", 1, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(0).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F21F28_F28OFF, lnis.outbound.get(0).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", 0, lnis.outbound.get(0).getElement(4) & bit);
            f28 = true;
            instance.setFunction(func,f28);
            Assert.assertEquals(f28, instance.getFunction(func));
            Assert.assertEquals("number of messages is 2", 2, lnis.outbound.size());
            Assert.assertEquals("opcode is OPC_EXP", LnConstants.OPC_EXP_SEND_FUNCTION_OR_SPEED_AND_DIR, lnis.outbound.get(1).getOpCode());
            Assert.assertEquals("opcode is OPC_EXP2", LnConstants.OPC_EXP_SEND_FUNCTION_GROUP_F21F28_F28ON, lnis.outbound.get(1).getElement(1) & 0x78);
            Assert.assertEquals("sent " + func + " in correct state", 0, lnis.outbound.get(1).getElement(4) & bit);
        }

        /**
         * Test of sendFunction 29
         */
        @Test
        public void testSendExpFunctionF29() {
            lnis.outbound.clear();
            lnis.resetStatistics();
            int func = 29;
            LocoNetMessage funcOnMess = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7f, 0x33, 0x02, 0x00, 0x58, 0x01, 0x00, 0x00, 0x00});
            LocoNetMessage funcOffMess = new LocoNetMessage(new int [] {0xED, 0x0B, 0x7F, 0x33, 0x02, 0x00, 0x58, 0x00, 0x00, 0x00, 0x00});
            Assert.assertEquals("check send of function exp f" + func, 0, lnis.outbound.size());
            instance.setFunction(func,true);
            Assert.assertEquals("check send of function" + func, 1, lnis.outbound.size());
            Assert.assertTrue("check opcode",funcOnMess.equals(lnis.outbound.get(0)));
            instance.setFunction(func,false);
            Assert.assertEquals("check send OFF function" + func, 2, lnis.outbound.size());
            Assert.assertTrue("check opcode",funcOffMess.equals(lnis.outbound.get(1)));
        }

        /**
         * Test of sendFunction 69
         */
        @Test
        public void testSendExpFunctionF68() {
            lnis.outbound.clear();
            lnis.resetStatistics();
            int func = 68;
            LocoNetMessage funcOnMess = new LocoNetMessage(new int[] {0xED, 0x0B, 0x7f, 0x33, 0x06, 0x00, 0x5C, 0x00, 0x00, 0x00, 0x00});
            LocoNetMessage funcOffMess = new LocoNetMessage(new int [] {0xED, 0x0B, 0x7F, 0x33, 0x02, 0x00, 0x5C, 0x00, 0x00, 0x00, 0x00});
            Assert.assertEquals("check send of function exp f" + func, 0, lnis.outbound.size());
            instance.setFunction(func,true);
            Assert.assertEquals("check send of function" + func, 1, lnis.outbound.size());
            Assert.assertTrue("check opcode",funcOnMess.equals(lnis.outbound.get(0)));
            instance.setFunction(func,false);
            Assert.assertEquals("check send OFF function" + func, 2, lnis.outbound.size());
            Assert.assertTrue("check opcode",funcOffMess.equals(lnis.outbound.get(1)));
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
            lnis = null;
            JUnitUtil.tearDown();
        }

    }

