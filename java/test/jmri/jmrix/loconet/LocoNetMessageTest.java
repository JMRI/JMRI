package jmri.jmrix.loconet;

import jmri.util.JUnitUtil;
import jmri.util.StringUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.loconet.LocoNetMessage class.
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 * <p>
 * Some of the message formats used in this class are Copyright Digitrax, Inc.
 * and used with permission as part of the JMRI project. That permission does
 * not extend to uses in other software products. If you wish to use this code,
 * algorithm or these message formats outside of JMRI, please contact Digitrax
 * Inc for separate permission.
 *
 * @author	Bob Jacobsen
 * @author B. Milhaupt Copyright (C) 2018
 *
 */
public class LocoNetMessageTest {

    @Test
    public void testCtor() {
        LocoNetMessage m = new LocoNetMessage(3);
        Assert.assertEquals("length", 3, m.getNumDataElements());
        m = new LocoNetMessage(2);
        Assert.assertEquals("length", 2, m.getNumDataElements());
        new LocoNetMessage(1);
        jmri.util.JUnitAppender.assertErrorMessage("LocoNetMessage does not allow object creation if length is less than 2.");

        new LocoNetMessage(0);
        jmri.util.JUnitAppender.assertErrorMessage("LocoNetMessage does not allow object creation if length is less than 2.");

        new LocoNetMessage(-1);
        jmri.util.JUnitAppender.assertErrorMessage("LocoNetMessage does not allow object creation if length is less than 2.");
    }

    @Test
    public void testArrayCtor() {
        LocoNetMessage m = new LocoNetMessage(new int[]{11, 12, 13, 14});
        Assert.assertEquals("length", 4, m.getNumDataElements());
        Assert.assertEquals("first value", 11, m.getElement(0));
        Assert.assertEquals("second value", 12, m.getElement(1));
        Assert.assertEquals("third value", 13, m.getElement(2));
        Assert.assertEquals("fourth value", 14, m.getElement(3));
        new LocoNetMessage(new int[] {0x85});
        jmri.util.JUnitAppender.assertErrorMessage("Cannot create a LocoNet message of length shorter than two.");

        byte[] t1 = new byte[]{(byte) 0x81};
        new LocoNetMessage(t1);
        jmri.util.JUnitAppender.assertErrorMessage("Cannot create a LocoNet message of length shorter than two.");
    }

    @Test
    public void testGetPeerXfr() {
        // basic message
        LocoNetMessage m1 = LocoNetMessage.makePeerXfr(0x1050, 0x1051,
                new int[]{1, 2, 3, 4, 5, 6, 7, 8}, 0);
        checkPeerXfr(m1, 0x1050, 0x1051,
                new int[]{1, 2, 3, 4, 5, 6, 7, 8}, 0);

        // some high data bits set
        LocoNetMessage m2 = LocoNetMessage.makePeerXfr(0x1050, 0x1051,
                new int[]{0x80, 0x81, 3, 4, 0xf5, 6, 7, 0xf8}, 0);
        checkPeerXfr(m2, 0x1050, 0x1051,
                new int[]{0x80, 0x81, 3, 4, 0xf5, 6, 7, 0xf8}, 0);

        // all high data bits set
        LocoNetMessage m3 = LocoNetMessage.makePeerXfr(0x1050, 0x1051,
                new int[]{0x80, 0x81, 0x83, 0x84, 0xf5, 0x86, 0x87, 0xf8}, 0);
        checkPeerXfr(m3, 0x1050, 0x1051,
                new int[]{0x80, 0x81, 0x83, 0x84, 0xf5, 0x86, 0x87, 0xf8}, 0);

        // check code three times
        LocoNetMessage m4 = LocoNetMessage.makePeerXfr(0x1050, 0x1051,
                new int[]{1, 2, 3, 4, 5, 6, 7, 8}, 0x11);
        checkPeerXfr(m4, 0x1050, 0x1051,
                new int[]{1, 2, 3, 4, 5, 6, 7, 8}, 0x11);

        m4 = LocoNetMessage.makePeerXfr(0x1050, 0x1051,
                new int[]{1, 2, 3, 4, 5, 6, 7, 8}, 0x38);
        checkPeerXfr(m4, 0x1050, 0x1051,
                new int[]{1, 2, 3, 4, 5, 6, 7, 8}, 0x38);

        m4 = LocoNetMessage.makePeerXfr(0x1050, 0x1051,
                new int[]{1, 2, 3, 4, 5, 6, 7, 8}, 63);
        checkPeerXfr(m4, 0x1050, 0x1051,
                new int[]{1, 2, 3, 4, 5, 6, 7, 8}, 63);
    }

    @Test
    public void testConstructorNoParams() {
        LocoNetMessage m = new LocoNetMessage();
        jmri.util.JUnitAppender.assertErrorMessage("LocoNetMessage does not allow a constructor with no argument");
        Assert.assertEquals("expect 0-length LocoNetMessage object", 0, m.getNumDataElements());
    }

    @Test
    public void testConstructorString() {
        LocoNetMessage m = new LocoNetMessage("");
        jmri.util.JUnitAppender.assertErrorMessage("LocoNetMessage does not allow a constructor with a 'String' argument");
        Assert.assertEquals("expect 0-length LocoNetMessage object", 0, m.getNumDataElements());

        m = new LocoNetMessage("A");
        jmri.util.JUnitAppender.assertErrorMessage("LocoNetMessage does not allow a constructor with a 'String' argument");
        Assert.assertEquals("expect 0-length LocoNetMessage object", 0, m.getNumDataElements());

        m = new LocoNetMessage("AB");
        jmri.util.JUnitAppender.assertErrorMessage("LocoNetMessage does not allow a constructor with a 'String' argument");
        Assert.assertEquals("expect 0-length LocoNetMessage object", 0, m.getNumDataElements());

        m = new LocoNetMessage("ABC");
        jmri.util.JUnitAppender.assertErrorMessage("LocoNetMessage does not allow a constructor with a 'String' argument");
        Assert.assertEquals("expect 0-length LocoNetMessage object", 0, m.getNumDataElements());

        m = new LocoNetMessage("ABCD");
        jmri.util.JUnitAppender.assertErrorMessage("LocoNetMessage does not allow a constructor with a 'String' argument");
        Assert.assertEquals("expect 0-length LocoNetMessage object", 0, m.getNumDataElements());

        m = new LocoNetMessage("81 7E");
        jmri.util.JUnitAppender.assertErrorMessage("LocoNetMessage does not allow a constructor with a 'String' argument");
        Assert.assertEquals("expect 0-length LocoNetMessage object", 0, m.getNumDataElements());

        m = new LocoNetMessage("81 7e");
        jmri.util.JUnitAppender.assertErrorMessage("LocoNetMessage does not allow a constructor with a 'String' argument");
        Assert.assertEquals("expect 0-length LocoNetMessage object", 0, m.getNumDataElements());
    }

    // use the makePeerXfr calls, already tested to check the decoding
    @Test
    public void testGetPeerXfrData() {
        int[] test;
        int[] data;
        LocoNetMessage m;

        test = new int[]{1, 2, 3, 4, 5, 6, 7, 8};
        m = LocoNetMessage.makePeerXfr(0x1050, 0x1051, test, 63);
        data = m.getPeerXfrData();
        for (int i = 0; i < 8; i++) {
            Assert.assertEquals("simple value " + i, "" + test[i], "" + data[i]);
        }

        test = new int[]{0x81, 0x21, 0x83, 0x84, 0x54, 0x86, 0x66, 0x88};
        m = LocoNetMessage.makePeerXfr(0x1050, 0x1051, test, 63);
        data = m.getPeerXfrData();
        for (int i = 0; i < 8; i++) {
            Assert.assertEquals("high-bit value " + i, "" + test[i], "" + data[i]);
        }

        test = new int[]{0xB5, 0xD3, 0x63, 0xF4, 0x5E, 0x77, 0xFF, 0x22};
        m = LocoNetMessage.makePeerXfr(0x1050, 0x1051, test, 63);
        data = m.getPeerXfrData();
        for (int i = 0; i < 8; i++) {
            Assert.assertEquals("complicated value " + i, "" + test[i], "" + data[i]);
        }

        m.setOpCode(0);
        data = m.getPeerXfrData();
        jmri.util.JUnitAppender.assertErrorMessage("getPeerXfrData called with wrong opcode 0");

        m.setOpCode(0xe4);
        data = m.getPeerXfrData();
        jmri.util.JUnitAppender.assertErrorMessage("getPeerXfrData called with wrong opcode 228");

        m.setOpCode(0xe5);
        m.setElement(1, 9);
        data = m.getPeerXfrData();
        jmri.util.JUnitAppender.assertErrorMessage("getPeerXfrData called with wrong secondary code 9");

        m = new LocoNetMessage(new int[] {0xe5, 0x10, 0x42, 0x40, 0x00, 0x00, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
        data = m.getPeerXfrData();
        jmri.util.JUnitAppender.assertErrorMessage("getPeerXfrData called with wrong length 17");

        m = new LocoNetMessage(new int[] {0xe5, 0x10, 0x42, 0x40, 0x00, 0x00});
        data = m.getPeerXfrData();
        jmri.util.JUnitAppender.assertErrorMessage("getPeerXfrData called with wrong length 6");

        m = new LocoNetMessage(new int[] {0xe5, 0x10, 0x42, 0x40, 0x00});
        data = m.getPeerXfrData();
        jmri.util.JUnitAppender.assertErrorMessage("getPeerXfrData called with wrong length 5");

        for (int j = 0; j < 8; ++j) {
            test = new int[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            test[j] = 0x80;
            m = LocoNetMessage.makePeerXfr(0x101, 0x8581, test, 0);
            data = m.getPeerXfrData();
            for (int i = 0; i < 8; i++) {
                Assert.assertEquals("complicated value " + i, "" + test[i], "" + data[i]);
            }
        }
    }

    @Test
    @SuppressWarnings("unlikely-arg-type") // int[] seems to be unrelated to LocoNetMessage
    public void testEqualsFromInt() {
        int[] t1 = new int[]{0x81, 0x01, 0x02, 0x02};
        int[] t2 = new int[]{0x81, 0x01, 0x02, 0x02, 0x03};
        int[] t3 = new int[]{0x81, 0x01, 0x02, 0x0F02};
        int[] t4 = new int[]{0x81, 0x01, 0x03, 0x02};
        int[] t5 = new int[]{0x81, 0x01, 0x02, 0x03};  // last byte not checked
        Assert.assertTrue((new LocoNetMessage(t1)).equals(new LocoNetMessage(t1)));
        Assert.assertTrue((new LocoNetMessage(t1)).equals(new LocoNetMessage(t3)));
        Assert.assertTrue(!(new LocoNetMessage(t1)).equals(new LocoNetMessage(t2)));
        Assert.assertTrue(!(new LocoNetMessage(t1)).equals(new LocoNetMessage(t4)));
        Assert.assertTrue((new LocoNetMessage(t1)).equals(new LocoNetMessage(t5)));
        Assert.assertFalse((new LocoNetMessage(t1)).equals(null));
        Assert.assertFalse((new LocoNetMessage(t1)).equals(new int[] {0x81, 0x01, 0x02, 0x02}));
    }

    @Test
    public void testEqualsFromBytes() {
        byte[] t1 = new byte[]{(byte) 0x81, (byte) 0x01, (byte) 0x02, (byte) 0x02};
        byte[] t2 = new byte[]{(byte) 0x81, (byte) 0x01, (byte) 0x02, (byte) 0x02, (byte) 0x03};
        byte[] t3 = new byte[]{(byte) 0x81, (byte) 0x01, (byte) 0x02, (byte) 0x02};
        byte[] t4 = new byte[]{(byte) 0x81, (byte) 0x01, (byte) 0x03, (byte) 0x02};
        byte[] t5 = new byte[]{(byte) 0x81, (byte) 0x01, (byte) 0x02, (byte) 0x03};  // last byte not checked
        Assert.assertTrue((new LocoNetMessage(t1)).equals(new LocoNetMessage(t1)));
        Assert.assertTrue((new LocoNetMessage(t1)).equals(new LocoNetMessage(t3)));
        Assert.assertTrue(!(new LocoNetMessage(t1)).equals(new LocoNetMessage(t2)));
        Assert.assertTrue(!(new LocoNetMessage(t1)).equals(new LocoNetMessage(t4)));
        Assert.assertTrue((new LocoNetMessage(t1)).equals(new LocoNetMessage(t5)));
    }

    @Test
    public void testEqualsFromString() {
        LocoNetMessage t1 = new LocoNetMessage(StringUtil.bytesFromHexString("81 01 02 02"));
        LocoNetMessage t2 = new LocoNetMessage(StringUtil.bytesFromHexString("81 01 02 02 03"));
        LocoNetMessage t3 = new LocoNetMessage(StringUtil.bytesFromHexString("81 01 02 02"));
        LocoNetMessage t4 = new LocoNetMessage(StringUtil.bytesFromHexString("81 01 03 02"));
        LocoNetMessage t5 = new LocoNetMessage(StringUtil.bytesFromHexString("81 01 02 03"));
        Assert.assertTrue((new LocoNetMessage(t1)).equals(t1));
        Assert.assertTrue((new LocoNetMessage(t1)).equals(t3));
        Assert.assertTrue(!(new LocoNetMessage(t1)).equals(t2));
        Assert.assertTrue(!(new LocoNetMessage(t1)).equals(t4));
        Assert.assertTrue((new LocoNetMessage(t1)).equals(t5));
    }

    @Test
    public void testEqualsSpecificCase() {
        LocoNetMessage t1 = new LocoNetMessage(StringUtil.bytesFromHexString("D7 12 00 09 20 13"));
        LocoNetMessage t2 = new LocoNetMessage(StringUtil.bytesFromHexString("D7 12 00 09 20 13"));
        LocoNetMessage t3 = new LocoNetMessage(StringUtil.bytesFromHexString("D7 1F 00 01 00 36"));
        LocoNetMessage t4 = new LocoNetMessage(StringUtil.bytesFromHexString("D7 1F 00 01 00 36"));
        Assert.assertTrue((new LocoNetMessage(t1)).equals(t1));
        Assert.assertTrue((new LocoNetMessage(t1)).equals(t2));
        Assert.assertTrue((new LocoNetMessage(t3)).equals(t3));
        Assert.assertTrue((new LocoNetMessage(t3)).equals(t4));
        Assert.assertTrue(!(new LocoNetMessage(t1)).equals(t3));
        Assert.assertTrue(!(new LocoNetMessage(t3)).equals(t1));
    }

    // service routine to check the contents of a single message (not a test)
    protected void checkPeerXfr(LocoNetMessage m, int src, int dst, int[] d, int code) {
        Assert.assertEquals("opcode ", 0xE5, m.getElement(0));
        Assert.assertEquals("secondary op code ", 0x10, m.getElement(1));

        // check the 8 data bytes
        int pxct1 = m.getElement(5);
        int pxct2 = m.getElement(10);

        Assert.assertEquals("data 0", d[0], (m.getElement(6) & 0x7F) + ((pxct1 & 0x01) != 0 ? 0x80 : 0));
        Assert.assertEquals("data 1", d[1], (m.getElement(7) & 0x7F) + ((pxct1 & 0x02) != 0 ? 0x80 : 0));
        Assert.assertEquals("data 2", d[2], (m.getElement(8) & 0x7F) + ((pxct1 & 0x04) != 0 ? 0x80 : 0));
        Assert.assertEquals("data 3", d[3], (m.getElement(9) & 0x7F) + ((pxct1 & 0x08) != 0 ? 0x80 : 0));

        Assert.assertEquals("data 4", d[4], (m.getElement(11) & 0x7F) + ((pxct2 & 0x01) != 0 ? 0x80 : 0));
        Assert.assertEquals("data 5", d[5], (m.getElement(12) & 0x7F) + ((pxct2 & 0x02) != 0 ? 0x80 : 0));
        Assert.assertEquals("data 6", d[6], (m.getElement(13) & 0x7F) + ((pxct2 & 0x04) != 0 ? 0x80 : 0));
        Assert.assertEquals("data 7", d[7], (m.getElement(14) & 0x7F) + ((pxct2 & 0x08) != 0 ? 0x80 : 0));

        // check code
        Assert.assertEquals("code low nibble", code & 0x7, (m.getElement(5) & 0x70) / 16);
        Assert.assertEquals("code high nibble", (code & 0x38) / 8, (m.getElement(10) & 0x70) / 16);

        // check the source address
        Assert.assertEquals("low 7 src address", src & 0x7F, m.getElement(2));

        // check the dest address
        Assert.assertEquals("low 7 dst address", dst & 0x7F, m.getElement(3));
        Assert.assertEquals("high 7 dst address", (dst & 0x7F00) / 256, m.getElement(4));
    }

    @Test
    public void testToString() {
        LocoNetMessage m1, m2;
        m1 = new LocoNetMessage(new int[] {0x81, 0x7e});
        m2 = new LocoNetMessage(new int[] {0xd0, 0x00, 0x40, 0x00, 0x48, 0x65});

        Assert.assertEquals("first toString test", "81 7E", m1.toString());
        Assert.assertEquals("second toString test", "D0 00 40 00 48 65", m2.toString());

        m1 = new LocoNetMessage(new int[] {0xff, 0x00});
        Assert.assertEquals("third toString test", "FF 00", m1.toString());
        m2 = new LocoNetMessage(new int[] {0xEF, 0x0E, 0x01, 0x33, 0x03, 0x00, 0x30, 0x47, 0x00, 0x00, 0x00, 0x17, 0x53, 0x1C});
        Assert.assertEquals("fourth toString test", "EF 0E 01 33 03 00 30 47 00 00 00 17 53 1C", m2.toString());

    }

    @Test
    public void testToMonitorString() {
        LocoNetMessage m = new LocoNetMessage(new int[] {0xB2, 0x15, 0x63, 0x72});
        Assert.assertEquals("no LocoNet Sensor Manager installed yet", "Sensor LS812 () is Low.  (BDL16 # 51, DS12; DS54/DS64 # 102, SwiB/S2).\n", m.toMonitorString());

        m = new LocoNetMessage(new int[] {0xb2, 0x1E, 0x47, 0x00});

        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold(new LocoNetSystemConnectionMemo());
        LocoNetInterfaceScaffold lnis2 = new LocoNetInterfaceScaffold(new LocoNetSystemConnectionMemo("L2", "LocoNet2"));
        LnTurnoutManager lntm = new LnTurnoutManager(lnis.getSystemConnectionMemo(), lnis, false);
        LnTurnoutManager lntm2 = new LnTurnoutManager(lnis2.getSystemConnectionMemo(), lnis2, false);
        LnSensorManager lnsm = new LnSensorManager(lnis.getSystemConnectionMemo());
        LnSensorManager lnsm2 = new LnSensorManager(lnis2.getSystemConnectionMemo());

        jmri.InstanceManager.setTurnoutManager(lntm);
        jmri.InstanceManager.setTurnoutManager(lntm2);
        jmri.InstanceManager.setSensorManager(lnsm);
        jmri.InstanceManager.setSensorManager(lnsm2);

        LnSensor s1 = (LnSensor) lnsm.provideSensor("LS1853");
        LnSensor s2 = (LnSensor) lnsm2.provideSensor("L2S1853");
        Assert.assertEquals("Sensor LS1853 () is Low.  (BDL16 # 116, DS13; DS54/DS64 # 232, AuxC/A3).\n", m.toMonitorString());
        Assert.assertEquals("Sensor LS1853 () is Low.  (BDL16 # 116, DS13; DS54/DS64 # 232, AuxC/A3).\n", m.toMonitorString("L"));
        Assert.assertEquals("Sensor L2S1853 () is Low.  (BDL16 # 116, DS13; DS54/DS64 # 232, AuxC/A3).\n", m.toMonitorString("L2"));

        s1.setUserName("grime");

        Assert.assertEquals("Sensor LS1853 (grime) is Low.  (BDL16 # 116, DS13; DS54/DS64 # 232, AuxC/A3).\n", m.toMonitorString());
        Assert.assertEquals("Sensor LS1853 (grime) is Low.  (BDL16 # 116, DS13; DS54/DS64 # 232, AuxC/A3).\n", m.toMonitorString("L"));
        Assert.assertEquals("Sensor L2S1853 () is Low.  (BDL16 # 116, DS13; DS54/DS64 # 232, AuxC/A3).\n", m.toMonitorString("L2"));

        s2.setUserName("brightly");

        Assert.assertEquals("Sensor LS1853 (grime) is Low.  (BDL16 # 116, DS13; DS54/DS64 # 232, AuxC/A3).\n", m.toMonitorString());
        Assert.assertEquals("Sensor LS1853 (grime) is Low.  (BDL16 # 116, DS13; DS54/DS64 # 232, AuxC/A3).\n", m.toMonitorString("L"));
        Assert.assertEquals("Sensor L2S1853 (brightly) is Low.  (BDL16 # 116, DS13; DS54/DS64 # 232, AuxC/A3).\n", m.toMonitorString("L2"));

        lntm.dispose();
        lntm2.dispose();
        lnsm.dispose();
        lnsm2.dispose();
    }

    @Test
    public void testLowByte() {
        for (int i = 0; i < 1025; ++i) {
            Assert.assertEquals("testing lowbyte for "+i, i & 0xFF, LocoNetMessage.lowByte(i));
        }
    }

    @Test
    public void testHighByte() {
        for (int i = 0; i < 18; ++i) {
            Assert.assertEquals("testing highbyte for " + (1 << i),
                    ((1 << i) & 0xFF00) >> 8, LocoNetMessage.highByte(1 << i));
            Assert.assertEquals("testing highbyte for "+ (0x0ffff >> i),
                    ((0xffff >> (i+8)) & 0xFF),
                    LocoNetMessage.highByte(0xffff >> i));
        }
        jmri.util.JUnitAppender.assertErrorMessage("highByte called with too large value: 10000");
        jmri.util.JUnitAppender.assertErrorMessage("highByte called with too large value: 20000");
        LocoNetMessage.highByte(0xfeffff);
        jmri.util.JUnitAppender.assertErrorMessage("highByte called with too large value: feffff");
    }

    @Test
    public void testHighBit() {
        for (int i = -1; i < 1025; ++i) {
            Assert.assertEquals(((i & 0x80)== 0x80),LocoNetMessage.highBit(i));
            if ((i < 0) || ((i > 255))) {
                jmri.util.JUnitAppender.assertErrorMessage(
                        "highBit called with too large value: 0x"+Integer.toHexString(i));
            }
        }

    }

    @Test
    public void testInputRepAddr() {
        LocoNetMessage m = new LocoNetMessage(2);
        m.setOpCode(0x81);
        Assert.assertEquals(-1, m.inputRepAddr());
        m = new LocoNetMessage(new int[] {0xb2, 0x00, 0x00, 0x00});
        Assert.assertEquals(0, m.inputRepAddr());
        m.setElement(1, 0x1);
        Assert.assertEquals(2, m.inputRepAddr());
        m.setElement(1, 0x0);
        m.setElement(2, 0x1);
        Assert.assertEquals(256, m.inputRepAddr());
        m.setElement(2, 0x21);
        Assert.assertEquals(257, m.inputRepAddr());
    }

    @Test
    public void testSensorAddr() {
        LocoNetMessage m = new LocoNetMessage(new int[] {0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(0, m.sensorAddr());
        for (int i = 0; i < 7; ++i) {
            m.setElement(1, 1<<i);
            Assert.assertEquals(1 << (i+1), m.sensorAddr());
        }
        m.setElement(1, 0);
        for (int i = 0; i < 7; ++i) {
            m.setElement(2, 1<<i);
            Assert.assertEquals("iteration "+i,
                    256 * ((1 << i) & 0xF) + (((1 << i) & 0x20)==0x20 ? 1:0),
                    m.sensorAddr());
        }
    }

    @Test
    public void testGetOpCodeHex() {
        LocoNetMessage m = new LocoNetMessage(new int[] {0x81, 0x00});
        Assert.assertEquals("0x81", m.getOpCodeHex());
        m.setOpCode(5);
        Assert.assertEquals("0x5", m.getOpCodeHex());
        m.setOpCode(0xef);
        Assert.assertEquals("0xef", m.getOpCodeHex());
        m.setOpCode(0xe3);
        m.setElement(1, 0x38);
        Assert.assertEquals("0xe3", m.getOpCodeHex());
    }

    @Test
    public void testTurnoutAddr() {
        LocoNetMessage m = new LocoNetMessage(new int[] {0x00, 0x00, 0x00, 0x00});
        Assert.assertEquals(1, m.turnoutAddr());
        for (int i = 0; i < 7; ++i) {
            m.setElement(1, 1<<i);
            Assert.assertEquals((1 << i) +1, m.turnoutAddr());
        }
        m.setElement(1, 0);
        for (int i = 0; i < 7; ++i) {
            m.setElement(2, 1<<i);
            Assert.assertEquals("iteration "+i,
                    128 * ((1 << i) & 0xF) + 1,
                    m.turnoutAddr());
        }
    }

    @Test
    public void testGetElement() {

        int[] a = { 33, 32, 31, 30, 29,
                    28, 27, 26, 25, 24,
                    23, 22, 21, 20, 19,
                    18, 17, 16, 15, 14 };

        LocoNetMessage m = new LocoNetMessage(a);
        m.getElement(-1);
        jmri.util.JUnitAppender.assertErrorMessage("reference element -1 in message of 20 elements: 21 20 1F 1E 1D 1C 1B 1A 19 18 17 16 15 14 13 12 11 10 0F 0E");

        m.getElement(21);
        jmri.util.JUnitAppender.assertErrorMessage("reference element 21 in message of 20 elements: 21 20 1F 1E 1D 1C 1B 1A 19 18 17 16 15 14 13 12 11 10 0F 0E");

        m.getElement(20);
        jmri.util.JUnitAppender.assertErrorMessage("reference element 20 in message of 20 elements: 21 20 1F 1E 1D 1C 1B 1A 19 18 17 16 15 14 13 12 11 10 0F 0E");

        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals(a[i], m.getElement(i));
        }
    }

    @Test
    public void testsetElement() {

        LocoNetMessage m = new LocoNetMessage(20);
        int val = 0;
        for (int i = 0; i < 20; ++i) {
            Assert.assertEquals(0, m.getElement(i));
        }

        for (int i = 0; i < 20; ++i) {
            int val2 = (val ^ 0x24) << 1;
            val = ((val2 & 0x100) == 1 ? 1:0) + (val2 & 0xFE);
            m.setElement(i, val);
            Assert.assertEquals(val, m.getElement(i));
        }
        int startingPoint = val;
        for (int i = 0; i < 20; ++i) {
            int val2 = (val ^ 0x24) << 1;
            val = ((val2 & 0x100) == 1 ? 1:0) + (val2 & 0xFE);
            m.setElement(i, val);
        }
        val = startingPoint;
        for (int i = 0; i < 20; ++i) {
            int val2 = (val ^ 0x24) << 1;
            val = ((val2 & 0x100) == 1 ? 1:0) + (val2 & 0xFE);
            Assert.assertEquals(val, m.getElement(i));
        }

        m.setElement(2, 0x150);
        m.setElement(3, -1);
        Assert.assertEquals(0xff, m.getElement(3));
        m.setElement(-1, 3);
        jmri.util.JUnitAppender.assertErrorMessage(
            "reference element -1 in message of 20 elements: 38 38 50 FF 38 38 38 38 38 38 38 38 38 38 38 38 38 38 38 38");

        m.setElement(21, 45);
        Assert.assertEquals(0x50, m.getElement(2));
        jmri.util.JUnitAppender.assertErrorMessage(
            "reference element 21 in message of 20 elements: 38 38 50 FF 38 38 38 38 38 38 38 38 38 38 38 38 38 38 38 38");
    }

    @Test
    public void testHashCode() {
        int[] a = new int[] {0xE7, 0x0E, 0x02, 0x23, 0x00, 0x00, 0x00, 0x47, 0x00, 0x00, 0x00, 0x17, 0x53, 0x34};
        LocoNetMessage m = new LocoNetMessage(a);

        int expectHash = 0;
        for (int i = 2; i >=0; --i) {
            expectHash = (expectHash << 7) +a[i];
        }
        expectHash += m.getNumDataElements();

        Assert.assertEquals(expectHash, m.hashCode());

        LocoNetMessage m2 = new LocoNetMessage(new int[] {0x81, 0x52});
        Assert.assertEquals(0x81+(0x52*128)+2, m2.hashCode());

        LocoNetMessage m3 = new LocoNetMessage(new int[] {0x97});
        Assert.assertEquals(0x98, m3.hashCode());
        jmri.util.JUnitAppender.assertErrorMessage("Cannot create a LocoNet message of length shorter than two.");

        LocoNetMessage m4 = new LocoNetMessage(new int[] {});
        Assert.assertEquals(0x0, m4.hashCode());
        jmri.util.JUnitAppender.assertErrorMessage("Cannot create a LocoNet message of length shorter than two.");
    }

    @Test
    public void testSetParity() {
        int[] a = new int[] {0xE7, 0x0E, 0x02, 0x23, 0x00, 0x00, 0x00, 0x47, 0x00, 0x00, 0x00, 0x17, 0x53, 99};
        LocoNetMessage m = new LocoNetMessage(a);
        m.setParity();
        Assert.assertEquals(0x34, m.getElement(13));

        int[] b = new int[] {0xD3, 0x12, 0x34, 0x56, 0x78, 0, 0x98, 0x87, 0x76, 0x65, 0};
        m = new LocoNetMessage(b);
        m.setParity();
        Assert.assertEquals("byte 5 expected", 0x24, m.getElement(5));
        Assert.assertEquals("byte 10 expected", 0xF3, m.getElement(10));

        int[] c = new int[] {0xD3, 0x34, 0x12, 0x78, 0x56, 0};
        m = new LocoNetMessage(c);
        m.setParity();
        Assert.assertEquals("byte 5 expected", 0x24, m.getElement(5));

        int[] d = new int[] {0xD3, 0x34, 0x10, 0x78, 0x56, 0};
        m = new LocoNetMessage(d);
        m.setParity();
        Assert.assertEquals("byte 5 expected", 0x26, m.getElement(5));
    }

    @Test
    public void testCheckParity() {
        int[] a = new int[] {0xD3, 0x12, 0x34, 0x56, 0x78, 0, 0x98, 0x87, 0x76, 0x65, 0};
        LocoNetMessage m = new LocoNetMessage(a);
        Assert.assertEquals(11, m.getNumDataElements());
        Assert.assertFalse(m.checkParity());
        for (int i = 0; i < 128; ++i) {
            m.setElement(10, i);
            Assert.assertFalse(m.checkParity());
        }
        m.setElement(5, 0x24);
        Assert.assertFalse(m.checkParity());
        m.setElement(10, 0xF3);
        Assert.assertTrue(m.checkParity());
        m.setElement(5, 0x23);
        Assert.assertFalse(m.checkParity());

        a = new int[] {0xD3, 0x2, 0x4, 0x8, 0x20, 0};
        m = new LocoNetMessage(a);
        Assert.assertFalse(m.checkParity());
        m.setElement(5, 2);
        Assert.assertTrue(m.checkParity());

        m = new LocoNetMessage(new int[] {0x89, 0x53, 0x3c, 0x12});
        Assert.assertFalse(m.checkParity());
        m.setElement(3, 0x19);
        Assert.assertTrue(m.checkParity());
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
