package jmri.jmrix.openlcb;

import jmri.*;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.Assert.*;

/**
 * Tests for the jmri.jmrix.openlcb.OlcbAddress class.
 *
 * @author Bob Jacobsen Copyright 2008, 2010
 */
public class OlcbAddressTest {

    @Test
    public void testAddressOK() {
        // hex form
        assertTrue(new OlcbAddress("x123456789ABCDEF0", null).check());
        assertTrue(new OlcbAddress("X123456789ABCDEF0", null).check());
        assertTrue(new OlcbAddress("12.34.56.78.00.BC.DE.F0", null).check());
        assertTrue(new OlcbAddress("1.34.5.0.9A.B.E.0", null).check());

    }

    @Test
    public void testAddressNotOK() {
        assertFalse(new OlcbAddress("+0A1", null).check());
        assertFalse(new OlcbAddress("- 001", null).check());
        assertFalse(new OlcbAddress("ABC", null).check());

        assertFalse(new OlcbAddress("xABC", null).check()); // odd number of digits
        assertFalse(new OlcbAddress("xprs0", null).check());

        assertFalse(new OlcbAddress("+n1e", null).check());
        assertFalse(new OlcbAddress("+ne1", null).check());
        assertFalse(new OlcbAddress("+e1", null).check());
        assertFalse(new OlcbAddress("+n1", null).check());

        // multiple address not OK
        assertFalse(new OlcbAddress("+1;+1;+1", null).check());
    }

    @Test
    public void testTurnoutAddressing() {
        assertEquals(new OlcbAddress("T1",    null).toString(),  "0101020000FF0008;0101020000FF0009");
        assertEquals(new OlcbAddress("T2044", null).toString(),  "0101020000FF0FFE;0101020000FF0FFF");
        assertEquals(new OlcbAddress("T2045", null).toString(),  "0101020000FF0000;0101020000FF0001");
        assertEquals(new OlcbAddress("T2048", null).toString(),  "0101020000FF0006;0101020000FF0007");
        assertEquals(new OlcbAddress("T509",  null).toString(),  "0101020000FF0400;0101020000FF0401");
    }
    
    @Test
    public void testCbusIdParseMatchReply() {
        CanReply c = new CanReply(
                new int[]{0x12, 0x34, 0x56, 0x78,
                        0x9A, 0xBC, 0xDE, 0xF0});
        assertFalse(new OlcbAddress("x123456789ABCDEF0", null).match(c));
        c.setExtended(true);
        c.setHeader(0x195B4000);
        assertTrue(new OlcbAddress("x123456789ABCDEF0", null).match(c));

        c = new CanReply(
                new int[]{0x01, 0x34, 0x05, 0x00,
                        0x9A, 0x0B, 0x0E, 0x00});
        assertFalse(new OlcbAddress("1.34.5.0.9A.B.E.0", null).match(c));
        c.setExtended(true);
        c.setHeader(0x195B4000);
        assertTrue(new OlcbAddress("1.34.5.0.9A.B.E.0", null).match(c));
    }

    @Test
    public void testCbusIdParseMatchMessage() {
        CanMessage c = new CanMessage(
                new int[]{0x12, 0x34, 0x56, 0x78,
                        0x9A, 0xBC, 0xDE, 0xF0},
                0x195B4123);
        assertFalse(new OlcbAddress("x123456789ABCDEF0", null).match(c));
        c.setExtended(true);

        assertTrue(new OlcbAddress("x123456789ABCDEF0", null).match(c));

        c = new CanMessage(
                new int[]{0x01, 0x34, 0x05, 0x00,
                        0x9A, 0x0B, 0x0E, 0x00},
                0x195B4123);
        assertFalse(new OlcbAddress("1.34.5.0.9A.B.E.0", null).match(c));
        c.setExtended(true);
        assertTrue(new OlcbAddress("1.34.5.0.9A.B.E.0", null).match(c));
    }

    @Test
    public void testEventAccess() {
        assertEquals(new OlcbAddress("x0102030405060708", null), new OlcbAddress(new org.openlcb.EventID("1.2.3.4.5.6.7.8")));
    }

    @Test
    public void testEqualsOK() {
        assertEquals((new OlcbAddress("1.34.5.0.9A.B.E.0", null)), new OlcbAddress("x013405009A0B0E00", null));
        assertEquals((new OlcbAddress("x013405009A0B0E00", null)), new OlcbAddress("1.34.5.0.9A.B.E.0", null));
        assertEquals((new OlcbAddress("x013405009A0B0E00", null)), new OlcbAddress("X013405009A0B0E00", null));
    }

    @Test
    public void testCompare() {
        assertEquals(0, (new OlcbAddress("1.34.5.0.9A.B.E.0", null)).compare(new OlcbAddress("x013405009A0B0E00", null)));
        assertEquals(0, (new OlcbAddress("x013405009A0B0E00", null)).compare(new OlcbAddress("1.34.5.0.9A.B.E.0", null)));
        assertEquals(0, (new OlcbAddress("x013405009A0B0E00", null)).compare(new OlcbAddress("X013405009A0B0E00", null)));

        assertEquals(-1, (new OlcbAddress("x013405009A0B0E00", null)).compare(new OlcbAddress("X013405009A0B0E01", null)));
        assertEquals(+1, (new OlcbAddress("x013405009A0B0E01", null)).compare(new OlcbAddress("X013405009A0B0E00", null)));

        assertEquals(-1, (new OlcbAddress("x013405009A0B0E", null)).compare(new OlcbAddress("X013405009A0B0E00", null)));
        assertEquals(+1, (new OlcbAddress("x013405009A0B0E00", null)).compare(new OlcbAddress("X013405009A0B0E", null)));

        // not testing the cases for non-match addresses
    }

    @Test
    public void testSplitCheckOK() {
        assertTrue(new OlcbAddress("x123456789ABCDEF0", null).checkSplit(null));
        assertTrue(new OlcbAddress("12.34.56.78.9A.BC.DE.F0", null).checkSplit(null));
    }

    @Test
    public void testMultiTermSplitCheckOK() {
        assertTrue(new OlcbAddress("x123456789ABCDEF0;x123456789ABCDEF0", null).checkSplit(null));
        assertTrue(new OlcbAddress("x123456789ABCDEF0;x123456789ABCDEF0;x123456789ABCDEF0", null).checkSplit(null));

        assertTrue(new OlcbAddress("1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.8", null).checkSplit(null));
        assertTrue(new OlcbAddress("1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.8", null).checkSplit(null));
    }

    @Test
    public void testSplitCheckNotOK() {
        assertFalse(new OlcbAddress("+0A1", null).check());
        assertFalse(new OlcbAddress("- 001", null).check());
        assertFalse(new OlcbAddress("ABC", null).check());
        assertFalse(new OlcbAddress("xprs0", null).check());

        assertFalse(new OlcbAddress("+1;;+1", null).checkSplit(null));
        assertFalse(new OlcbAddress("+001;", null).checkSplit(null));
        assertFalse(new OlcbAddress("-001;", null).checkSplit(null));
        assertFalse(new OlcbAddress("-001;;", null).checkSplit(null));
        assertFalse(new OlcbAddress("xABC;", null).checkSplit(null));
        assertFalse(new OlcbAddress("xabc;", null).checkSplit(null));
        assertFalse(new OlcbAddress("xa1b2c3;", null).checkSplit(null));
        assertFalse(new OlcbAddress("x123456789ABCDEF0;", null).checkSplit(null));

        assertFalse(new OlcbAddress("+001;xprs0", null).checkSplit(null));
        assertFalse(new OlcbAddress("-001;xprs0", null).checkSplit(null));
        assertFalse(new OlcbAddress("xABC;xprs0", null).checkSplit(null));
        assertFalse(new OlcbAddress("xabc;xprs0", null).checkSplit(null));
        assertFalse(new OlcbAddress("xa1b2c3;xprs0", null).checkSplit(null));
        assertFalse(new OlcbAddress("x123456789ABCDEF0;xprs0", null).checkSplit(null));
    }

    @Test
    public void testSplit() {
        OlcbAddress a;
        OlcbAddress[] v;

        a = new OlcbAddress("1.2.3.4.5.6.7.8", null);
        v = a.split(null);
        assertEquals(1, v.length);
        assertEquals(new OlcbAddress("1.2.3.4.5.6.7.8", null), v[0]);

        a = new OlcbAddress("1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9", null);
        v = a.split(null);
        assertEquals(2, v.length);
        assertEquals(new OlcbAddress("1.2.3.4.5.6.7.8", null), v[0]);
        assertEquals(new OlcbAddress("1.2.3.4.5.6.7.9", null), v[1]);

    }

    @Test
    public void testName() {
        JUnitUtil.initIdTagManager();
        IdTagManager tagmgr = InstanceManager.getDefault(IdTagManager.class);
        var tag1 = tagmgr.provideIdTag("IDOpenLCB$02.01.2C.01.00.00.00.8A");
        tag1.setUserName("evtBar");
        OlcbTestInterface iface = new OlcbTestInterface(new OlcbTestInterface.CreateConfigurationManager());
        var memo = iface.systemConnectionMemo;
        
        OlcbAddress a = new OlcbAddress("evtBar", memo);
        assertEquals(a, new OlcbAddress(new org.openlcb.EventID("02.01.2C.01.00.00.00.8A")));
        
        iface.dispose();
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
    }
        
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
