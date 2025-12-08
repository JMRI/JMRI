package jmri.jmrix.can.cbus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.can.cbus.CbusAddress class.
 *
 * @author Bob Jacobsen Copyright 2008
 */
public class CbusAddressTest {

    @Test
    public void testCanCreate() {
        CbusAddress t = new CbusAddress("X0A;+N15E6");
        assertNotNull( t, "exists");
    }

    @Test
    public void testCbusAddressOK() {
        // +/- form
        assertTrue(new CbusAddress("+001").check());
        assertTrue(new CbusAddress("-001").check());
        assertTrue(new CbusAddress("2700012").check());

        // hex form
        assertTrue(new CbusAddress("X0ABC").check());
        assertTrue(new CbusAddress("X0abc").check());
        assertTrue(new CbusAddress("Xa1b2c3").check());
        assertTrue(new CbusAddress("X123456789ABCDEF0").check());

        // n0e0 form
        assertTrue(new CbusAddress("+N1E2").check());
        assertTrue(new CbusAddress("+N01e002").check());
        assertTrue(new CbusAddress("+1E2").check());
        assertTrue(new CbusAddress("-N1E2").check());
        assertTrue(new CbusAddress("-N01e002").check());
        assertTrue(new CbusAddress("-1e2").check());
        assertTrue(new CbusAddress("N1e2").check());
        assertTrue(new CbusAddress("N01e002").check());
        assertTrue(new CbusAddress("1e2").check());
        assertTrue(new CbusAddress("+N12e34").check());
        assertTrue(new CbusAddress("+N12e35").check());
    }

    @Test
    public void testCbusAddressNotOK() {
        assertFalse( new CbusAddress("+0A1").check());
        assertFalse( new CbusAddress("- 001").check());
        assertFalse( new CbusAddress("ABC").check());

        assertFalse( new CbusAddress("XABC").check());    // odd number of digits
        assertFalse( new CbusAddress("Xprs0").check());

        assertFalse( new CbusAddress("+N1E").check());
        assertFalse( new CbusAddress("+NE1").check());
        assertFalse( new CbusAddress("+E1").check());
        assertFalse( new CbusAddress("+N1").check());

        // multiple address not OK
        assertFalse( new CbusAddress("+1;+1;+1").check());
    }

    @Test
    public void testCbusIdParseMatchReply() {
        // Cbus short events
        assertTrue(new CbusAddress("+12").match(
                new CanReply(
                        new int[]{CbusConstants.CBUS_ASON, 0x00, 0x00, 0x00, 12}
                )));
        assertTrue(new CbusAddress("-12").match(
                new CanReply(
                        new int[]{CbusConstants.CBUS_ASOF, 0x00, 0x00, 0x00, 12}
                )));
        // Cbus normal events
        assertTrue(new CbusAddress("+2700012").match(
                new CanReply(
                        new int[]{CbusConstants.CBUS_ACON, 0x00, 0x1b, 0x00, 12}
                )));
        assertTrue(new CbusAddress("-2700012").match(
                new CanReply(
                        new int[]{CbusConstants.CBUS_ACOF, 0x00, 0x1b, 0x00, 12}
                )));
        assertTrue(new CbusAddress("X123456789ABCDEF0").match(
                new CanReply(
                        new int[]{0x12, 0x34, 0x56, 0x78,
                            0x9A, 0xBC, 0xDE, 0xF0}
                )));
    }

    @Test
    public void testCbusIdParseMatchMessage() {
        assertTrue(new CbusAddress("+12").match(
                new CanMessage(
                        new int[]{CbusConstants.CBUS_ASON, 0x00, 0x00, 0x00, 12},
                        0x123
                )));
        assertTrue(new CbusAddress("-12").match(
                new CanMessage(
                        new int[]{CbusConstants.CBUS_ASOF, 0x00, 0x00, 0x00, 12},
                        0x123
                )));
        assertTrue(new CbusAddress("+2700012").match(
                new CanMessage(
                        new int[]{CbusConstants.CBUS_ACON, 0x00, 0x1b, 0x00, 12},
                        0x123
                )));
        assertTrue(new CbusAddress("-2700012").match(
                new CanMessage(
                        new int[]{CbusConstants.CBUS_ACOF, 0x00, 0x1b, 0x00, 12},
                        0x123
                )));
        assertTrue(new CbusAddress("X123456789ABCDEF0").match(
                new CanMessage(
                        new int[]{0x12, 0x34, 0x56, 0x78,
                            0x9A, 0xBC, 0xDE, 0xF0},
                        0x123
                )));
    }

    @Test
    public void testNEformMatch() {
        assertTrue(new CbusAddress("+N12E34").match(
                new CanMessage(
                        new int[]{CbusConstants.CBUS_ACON, 0x00, 12, 0x00, 34},
                        0x123
                )));

        assertTrue(new CbusAddress("+12E34").match(
                new CanMessage(
                        new int[]{CbusConstants.CBUS_ACON, 0x00, 12, 0x00, 34},
                        0x123
                )));

        assertTrue(new CbusAddress("12E34").match(
                new CanMessage(
                        new int[]{CbusConstants.CBUS_ACON, 0x00, 12, 0x00, 34},
                        0x123
                )));

        assertTrue(new CbusAddress("N12E34").match(
                new CanMessage(
                        new int[]{CbusConstants.CBUS_ACON, 0x00, 12, 0x00, 34},
                        0x123
                )));

        assertTrue(new CbusAddress("-N12E34").match(
                new CanMessage(
                        new int[]{CbusConstants.CBUS_ACOF, 0x00, 12, 0x00, 34},
                        0x123
                )));
        assertTrue(!new CbusAddress("-268").match(new CanMessage(
            new int[]{CbusConstants.CBUS_ASOF, 0x00, 0x00, 0x00},0x123 )));
        assertTrue(!new CbusAddress("+1").match(new CanMessage(
            new int[]{CbusConstants.CBUS_ASOF, 0x00, 0x00, 0x00, 0x01},0x123 )));
        assertTrue(!new CbusAddress("+2").match(new CanMessage(
            new int[]{CbusConstants.CBUS_ASON, 0x00, 0x00, 0x00, 0x01},0x123 )));
    }

    @Test
    public void testCbusIdNotParse() {
        assertTrue(!new CbusAddress("-12").match(
                new CanReply(
                        new int[]{CbusConstants.CBUS_ACON, 0x00, 0x00, 0x00, 12}
                )));
        assertTrue(!new CbusAddress("-268").match(
                new CanReply(
                        new int[]{CbusConstants.CBUS_ACOF, 0x00, 0x00, 0x00, 12}
                )));
        assertTrue(!new CbusAddress("-268").match(new CanReply(
            new int[]{CbusConstants.CBUS_ASOF, 0x00, 0x00, 0x00} )));
        assertTrue(!new CbusAddress("+1").match(new CanReply(
            new int[]{CbusConstants.CBUS_ASOF, 0x00, 0x00, 0x00, 0x01} )));
        assertTrue(!new CbusAddress("+2").match(new CanReply(
            new int[]{CbusConstants.CBUS_ASON, 0x00, 0x00, 0x00, 0x01} )));
    }

    @Test
    public void testPlusMinus() {
        assertTrue((new CbusAddress("+001")).equals(new CbusAddress("+001")));
        assertTrue((new CbusAddress("+001")).equals(new CbusAddress("x9800000001")));
        assertTrue((new CbusAddress("+200001")).equals(new CbusAddress("x9000020001")));
        assertTrue((new CbusAddress("-003")).equals(new CbusAddress("x9900000003")));
        assertTrue((new CbusAddress("-200003")).equals(new CbusAddress("x9100020003")));
    }

    @Test
    @SuppressWarnings({"unlikely-arg-type", "IncompatibleEquals"})
    public void testEqualsOK() {
        assertTrue((new CbusAddress("+001")).equals(new CbusAddress("+001")));
        assertTrue((new CbusAddress("+001")).equals(new CbusAddress("x9800000001")));
        assertTrue((new CbusAddress("+200001")).equals(new CbusAddress("x9000020001")));
        assertNotNull( new CbusAddress("+200001"));
        assertFalse((new CbusAddress("+200001")).equals("foo"));
        assertFalse((new CbusAddress("+001")).equals(new CbusAddress("+002")));
        assertFalse((new CbusAddress("+N123E123")).equals(new CbusAddress("+N456E123")));
        assertFalse((new CbusAddress("+N123E123")).equals(new CbusAddress("+N123E456")));
        assertFalse(new CbusAddress("-268").equals(
            new CanReply(new int[]{CbusConstants.CBUS_SNN, 0x00, 12})));
    }

    @Test
    public void testSplitCheckOK() {
        assertTrue(new CbusAddress("+001").checkSplit());
        assertTrue(new CbusAddress("-001").checkSplit());
        assertTrue(new CbusAddress("X0ABC").checkSplit());
        assertTrue(new CbusAddress("X0abc").checkSplit());
        assertTrue(new CbusAddress("Xa1b2c3").checkSplit());
        assertTrue(new CbusAddress("X123456789ABCDEF0").checkSplit());

        assertTrue(new CbusAddress("+001;+001").checkSplit());
        assertTrue(new CbusAddress("-001;+001").checkSplit());
        assertTrue(new CbusAddress("X0ABC;+001").checkSplit());
        assertTrue(new CbusAddress("X0abc;+001").checkSplit());
        assertTrue(new CbusAddress("Xa1b2c3;+001").checkSplit());
        assertTrue(new CbusAddress("X123456789ABCDEF0;+001").checkSplit());
    }

    @Test
    public void testMultiTermSplitCheckOK() {
        assertTrue(new CbusAddress("+1;+1").checkSplit());
        assertTrue(new CbusAddress("+N12e34;+1").checkSplit());
        assertTrue(new CbusAddress("+1;X1234").checkSplit());
        assertTrue(new CbusAddress("+1;N12e34").checkSplit());
        assertTrue(new CbusAddress("+1;+N12e34").checkSplit());
        assertTrue(new CbusAddress("+N12e34;+N12e35").checkSplit());
        assertTrue(new CbusAddress("X0A;+N15E6").checkSplit());
    }

    @Test
    public void testSplitCheckNotOK() {
        assertFalse( new CbusAddress("+0A1").check());
        assertFalse( new CbusAddress("- 001").check());
        assertFalse( new CbusAddress("ABC").check());
        assertFalse( new CbusAddress("Xprs0").check());

        assertFalse( new CbusAddress("+1;+1;+1").checkSplit());
        assertFalse( new CbusAddress("+1;;+1").checkSplit());
        assertFalse( new CbusAddress("+001;").checkSplit());
        assertFalse( new CbusAddress("-001;").checkSplit());
        assertFalse( new CbusAddress("-001;;").checkSplit());
        assertFalse( new CbusAddress("XABC;").checkSplit());
        assertFalse( new CbusAddress("Xabc;").checkSplit());
        assertFalse( new CbusAddress("Xa1b2c3;").checkSplit());
        assertFalse( new CbusAddress("X123456789ABCDEF0;").checkSplit());

        assertFalse( new CbusAddress("+001;Xprs0").checkSplit());
        assertFalse( new CbusAddress("-001;Xprs0").checkSplit());
        assertFalse( new CbusAddress("XABC;Xprs0").checkSplit());
        assertFalse( new CbusAddress("Xabc;Xprs0").checkSplit());
        assertFalse( new CbusAddress("Xa1b2c3;Xprs0").checkSplit());
        assertFalse( new CbusAddress("X123456789ABCDEF0;Xprs0").checkSplit());
    }
    
    @Test
    public void testElements() {
        CbusAddress a = new CbusAddress("X9801D203A4");
        int[] testa = a.elements();
        assertTrue(testa[0] == 0x98);
        assertTrue(testa[1] == 0x01);
        assertTrue(testa[2] == 0xD2);
        assertTrue(testa[3] == 0x03);
        assertTrue(testa[4] == 0xA4);
    }        
    
    @Test
    public void testtoString() {
        CbusAddress a = new CbusAddress("X9801D203A4");
        assertEquals("X9801D203A4", a.toString());
        
        CbusAddress b = new CbusAddress("+N123E456");
        assertEquals("+N123E456", b.toString());
        
        CbusAddress c = new CbusAddress("-456");
        assertEquals("-456", c.toString());
    }

    @Test
    public void testtoCanonString() {
        CbusAddress a = new CbusAddress("X9801D203A4");
        assertEquals( "x9801D203A4",a.toCanonicalString(), "hex form");

        CbusAddress b = new CbusAddress("+N123E456");
        assertEquals( "x90007B01C8",b.toCanonicalString(), "long form");

        CbusAddress c = new CbusAddress("-321");
        assertEquals( "x9900000141",c.toCanonicalString(), "short form");
    }

    @Test
    public void testSplit() {
        CbusAddress a;
        CbusAddress[] v;

        a = new CbusAddress("+001");
        v = a.split();
        assertEquals( 1, v.length);
        assertTrue(new CbusAddress("+001").equals(v[0]));

        a = new CbusAddress("+001;-2");
        v = a.split();
        assertEquals( 2, v.length);
        assertTrue(new CbusAddress("+001").equals(v[0]));
        assertTrue(new CbusAddress("-2").equals(v[1]));
    }
    
    @Test
    public void testgetIncrement() throws jmri.JmriException {
        
        assertEquals( "+N34E18;-N34E18",CbusAddress.getIncrement("+N34E17;-N34E17"), "+N34E17;-N34E17");
        assertEquals( "+N34E457;+N34E18",CbusAddress.getIncrement("+N34E456;+N34E17"), "+N34E456;+N34E17");
        assertEquals( "-N34E457;-N34E18",CbusAddress.getIncrement("-N34E456;-N34E17"), "-N34E456;-N34E17");
        assertEquals( "-N34E457;+N34E18",CbusAddress.getIncrement("-N34E456;+N34E17"), "-N34E456;+N34E17");
    }

    @Test
    public void testCbusAddressHashcode() {
        CbusAddress a = new CbusAddress("X9801D203A4");
        assertEquals( 530, a.hashCode(), "a hashcode is present");
    }

    @Test
    public void testMatchRequest() {
        
        assertTrue( new CbusAddress("+12").matchRequest(
            new CanReply(new int[]{CbusConstants.CBUS_ASRQ, 0x00, 0x00, 0x00, 12})),
                "short request 12 match");

        assertFalse( new CbusAddress("+13").matchRequest(
            new CanReply(new int[]{CbusConstants.CBUS_ASRQ, 0x00, 0x00, 0x00, 12})),
                "short request 13 no match");

        assertFalse( new CbusAddress("+12").matchRequest(
            new CanReply(new int[]{CbusConstants.CBUS_ACON3, 0x00, 0x00, 0x00, 12, 0x12, 0x13})),
                "Data element no match");

        assertFalse( new CbusAddress("+12").matchRequest(
            new CanReply(new int[]{CbusConstants.CBUS_ASON, 0x00, 0x00, 0x00, 12})),
                "ASON 12 no match");

        assertTrue( new CbusAddress("+N12E34").matchRequest(
            new CanReply(new int[]{CbusConstants.CBUS_AREQ, 0x00, 12, 0x00, 34})),
                "long request N12E34 match");

        assertFalse( new CbusAddress("+N11E34").matchRequest(
            new CanReply(new int[]{CbusConstants.CBUS_AREQ, 0x00, 12, 0x00, 34})),
                "long request N11E34 no match");

        assertFalse( new CbusAddress("+N12E35").matchRequest(
            new CanReply(new int[]{CbusConstants.CBUS_AREQ, 0x00, 12, 0x00, 34})),
                "long request N123E35 no match");

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CbusAddressTest.class);
}
