package jmri.jmrix.openlcb;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.openlcb.OlcbAddress class.
 *
 * @author	Bob Jacobsen Copyright 2008, 2010
 */
public class OlcbAddressTest extends TestCase {

    public void testAddressOK() {
        // hex form
        assertTrue(new OlcbAddress("x123456789ABCDEF0").check());
        assertTrue(new OlcbAddress("X123456789ABCDEF0").check());
        assertTrue(new OlcbAddress("12.34.56.78.00.BC.DE.F0").check());
        assertTrue(new OlcbAddress("1.34.5.0.9A.B.E.0").check());

    }

    public void testAddressNotOK() {
        assertTrue(!new OlcbAddress("+0A1").check());
        assertTrue(!new OlcbAddress("- 001").check());
        assertTrue(!new OlcbAddress("ABC").check());

        assertTrue(!new OlcbAddress("xABC").check());    // odd number of digits     
        assertTrue(!new OlcbAddress("xprs0").check());

        assertTrue(!new OlcbAddress("+n1e").check());
        assertTrue(!new OlcbAddress("+ne1").check());
        assertTrue(!new OlcbAddress("+e1").check());
        assertTrue(!new OlcbAddress("+n1").check());

        // multiple address not OK
        assertTrue(!new OlcbAddress("+1;+1;+1").check());
    }

    public void testCbusIdParseMatchReply() {
        CanReply c = new CanReply(
                new int[]{0x12, 0x34, 0x56, 0x78,
                    0x9A, 0xBC, 0xDE, 0xF0});
        assertTrue(!new OlcbAddress("x123456789ABCDEF0").match(c));
        c.setExtended(true);
        c.setHeader(0x195B4000);
        assertTrue(new OlcbAddress("x123456789ABCDEF0").match(c));

        c = new CanReply(
                new int[]{0x01, 0x34, 0x05, 0x00,
                    0x9A, 0x0B, 0x0E, 0x00});
        assertTrue(!new OlcbAddress("1.34.5.0.9A.B.E.0").match(c));
        c.setExtended(true);
        c.setHeader(0x195B4000);
        assertTrue(new OlcbAddress("1.34.5.0.9A.B.E.0").match(c));
    }

    public void testCbusIdParseMatchMessage() {
        CanMessage c = new CanMessage(
                new int[]{0x12, 0x34, 0x56, 0x78,
                    0x9A, 0xBC, 0xDE, 0xF0},
                0x195B4123);
        assertTrue(!new OlcbAddress("x123456789ABCDEF0").match(c));
        c.setExtended(true);

        assertTrue(new OlcbAddress("x123456789ABCDEF0").match(c));

        c = new CanMessage(
                new int[]{0x01, 0x34, 0x05, 0x00,
                    0x9A, 0x0B, 0x0E, 0x00},
                0x195B4123);
        assertTrue(!new OlcbAddress("1.34.5.0.9A.B.E.0").match(c));
        c.setExtended(true);
        assertTrue(new OlcbAddress("1.34.5.0.9A.B.E.0").match(c));
    }

    public void testEqualsOK() {
        assertTrue((new OlcbAddress("1.34.5.0.9A.B.E.0")).equals(new OlcbAddress("x013405009A0B0E00")));
        assertTrue((new OlcbAddress("x013405009A0B0E00")).equals(new OlcbAddress("1.34.5.0.9A.B.E.0")));
        assertTrue((new OlcbAddress("x013405009A0B0E00")).equals(new OlcbAddress("X013405009A0B0E00")));
    }

    public void testCompare() {
        assertEquals(0, (new OlcbAddress("1.34.5.0.9A.B.E.0")).compare(new OlcbAddress("x013405009A0B0E00")));
        assertEquals(0, (new OlcbAddress("x013405009A0B0E00")).compare(new OlcbAddress("1.34.5.0.9A.B.E.0")));
        assertEquals(0, (new OlcbAddress("x013405009A0B0E00")).compare(new OlcbAddress("X013405009A0B0E00")));
        
        assertEquals(-1, (new OlcbAddress("x013405009A0B0E00")).compare(new OlcbAddress("X013405009A0B0E01")));
        assertEquals(+1, (new OlcbAddress("x013405009A0B0E01")).compare(new OlcbAddress("X013405009A0B0E00")));

        assertEquals(-1, (new OlcbAddress("x013405009A0B0E")).compare(new OlcbAddress("X013405009A0B0E00")));
        assertEquals(+1, (new OlcbAddress("x013405009A0B0E00")).compare(new OlcbAddress("X013405009A0B0E")));

        // not testing the cases for non-match addresses
    }
    
    public void testSplitCheckOK() {
        assertTrue(new OlcbAddress("x123456789ABCDEF0").checkSplit());
        assertTrue(new OlcbAddress("12.34.56.78.9A.BC.DE.F0").checkSplit());
    }

    public void testMultiTermSplitCheckOK() {
        assertTrue(new OlcbAddress("x123456789ABCDEF0;x123456789ABCDEF0").checkSplit());
        assertTrue(new OlcbAddress("x123456789ABCDEF0;x123456789ABCDEF0;x123456789ABCDEF0").checkSplit());

        assertTrue(new OlcbAddress("1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.8").checkSplit());
        assertTrue(new OlcbAddress("1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.8").checkSplit());
    }

    public void testSplitCheckNotOK() {
        assertTrue(!new OlcbAddress("+0A1").check());
        assertTrue(!new OlcbAddress("- 001").check());
        assertTrue(!new OlcbAddress("ABC").check());
        assertTrue(!new OlcbAddress("xprs0").check());

        assertTrue(!new OlcbAddress("+1;;+1").checkSplit());
        assertTrue(!new OlcbAddress("+001;").checkSplit());
        assertTrue(!new OlcbAddress("-001;").checkSplit());
        assertTrue(!new OlcbAddress("-001;;").checkSplit());
        assertTrue(!new OlcbAddress("xABC;").checkSplit());
        assertTrue(!new OlcbAddress("xabc;").checkSplit());
        assertTrue(!new OlcbAddress("xa1b2c3;").checkSplit());
        assertTrue(!new OlcbAddress("x123456789ABCDEF0;").checkSplit());

        assertTrue(!new OlcbAddress("+001;xprs0").checkSplit());
        assertTrue(!new OlcbAddress("-001;xprs0").checkSplit());
        assertTrue(!new OlcbAddress("xABC;xprs0").checkSplit());
        assertTrue(!new OlcbAddress("xabc;xprs0").checkSplit());
        assertTrue(!new OlcbAddress("xa1b2c3;xprs0").checkSplit());
        assertTrue(!new OlcbAddress("x123456789ABCDEF0;xprs0").checkSplit());
    }

    public void testSplit() {
        OlcbAddress a;
        OlcbAddress[] v;

        a = new OlcbAddress("1.2.3.4.5.6.7.8");
        v = a.split();
        assertTrue(v.length == 1);
        assertTrue(new OlcbAddress("1.2.3.4.5.6.7.8").equals(v[0]));

        a = new OlcbAddress("1.2.3.4.5.6.7.8;1.2.3.4.5.6.7.9");
        v = a.split();
        assertTrue(v.length == 2);
        assertTrue(new OlcbAddress("1.2.3.4.5.6.7.8").equals(v[0]));
        assertTrue(new OlcbAddress("1.2.3.4.5.6.7.9").equals(v[1]));

    }

    // from here down is testing infrastructure
    public OlcbAddressTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {OlcbAddressTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(OlcbAddressTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        JUnitUtil.setUp();
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }

}
