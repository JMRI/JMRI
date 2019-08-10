package jmri.jmrix.can.cbus;

import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.can.cbus.CbusAddress class.
 *
 * @author Bob Jacobsen Copyright 2008
 */
public class CbusAddressTest {

    @Test
    public void testCanCreate() {
        CbusAddress t = new CbusAddress("X0A;+N15E6");
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testCbusAddressOK() {
        // +/- form
        Assert.assertTrue(new CbusAddress("+001").check());
        Assert.assertTrue(new CbusAddress("-001").check());
        Assert.assertTrue(new CbusAddress("2700012").check());

        // hex form
        Assert.assertTrue(new CbusAddress("X0ABC").check());
        Assert.assertTrue(new CbusAddress("X0abc").check());
        Assert.assertTrue(new CbusAddress("Xa1b2c3").check());
        Assert.assertTrue(new CbusAddress("X123456789ABCDEF0").check());

        // n0e0 form
        Assert.assertTrue(new CbusAddress("+N1E2").check());
        Assert.assertTrue(new CbusAddress("+N01e002").check());
        Assert.assertTrue(new CbusAddress("+1E2").check());
        Assert.assertTrue(new CbusAddress("-N1E2").check());
        Assert.assertTrue(new CbusAddress("-N01e002").check());
        Assert.assertTrue(new CbusAddress("-1e2").check());
        Assert.assertTrue(new CbusAddress("N1e2").check());
        Assert.assertTrue(new CbusAddress("N01e002").check());
        Assert.assertTrue(new CbusAddress("1e2").check());
        Assert.assertTrue(new CbusAddress("+N12e34").check());
        Assert.assertTrue(new CbusAddress("+N12e35").check());
    }

    @Test
    public void testCbusAddressNotOK() {
        Assert.assertTrue(!new CbusAddress("+0A1").check());
        Assert.assertTrue(!new CbusAddress("- 001").check());
        Assert.assertTrue(!new CbusAddress("ABC").check());

        Assert.assertTrue(!new CbusAddress("XABC").check());    // odd number of digits     
        Assert.assertTrue(!new CbusAddress("Xprs0").check());

        Assert.assertTrue(!new CbusAddress("+N1E").check());
        Assert.assertTrue(!new CbusAddress("+NE1").check());
        Assert.assertTrue(!new CbusAddress("+E1").check());
        Assert.assertTrue(!new CbusAddress("+N1").check());

        // multiple address not OK
        Assert.assertTrue(!new CbusAddress("+1;+1;+1").check());
    }

    @Test
    public void testCbusIdParseMatchReply() {
        // Cbus short events
        Assert.assertTrue(new CbusAddress("+12").match(
                new CanReply(
                        new int[]{CbusConstants.CBUS_ASON, 0x00, 0x00, 0x00, 12}
                )));
        Assert.assertTrue(new CbusAddress("-12").match(
                new CanReply(
                        new int[]{CbusConstants.CBUS_ASOF, 0x00, 0x00, 0x00, 12}
                )));
        // Cbus normal events
        Assert.assertTrue(new CbusAddress("+2700012").match(
                new CanReply(
                        new int[]{CbusConstants.CBUS_ACON, 0x00, 0x1b, 0x00, 12}
                )));
        Assert.assertTrue(new CbusAddress("-2700012").match(
                new CanReply(
                        new int[]{CbusConstants.CBUS_ACOF, 0x00, 0x1b, 0x00, 12}
                )));
        Assert.assertTrue(new CbusAddress("X123456789ABCDEF0").match(
                new CanReply(
                        new int[]{0x12, 0x34, 0x56, 0x78,
                            0x9A, 0xBC, 0xDE, 0xF0}
                )));
    }

    @Test
    public void testCbusIdParseMatchMessage() {
        Assert.assertTrue(new CbusAddress("+12").match(
                new CanMessage(
                        new int[]{CbusConstants.CBUS_ASON, 0x00, 0x00, 0x00, 12},
                        0x123
                )));
        Assert.assertTrue(new CbusAddress("-12").match(
                new CanMessage(
                        new int[]{CbusConstants.CBUS_ASOF, 0x00, 0x00, 0x00, 12},
                        0x123
                )));
        Assert.assertTrue(new CbusAddress("+2700012").match(
                new CanMessage(
                        new int[]{CbusConstants.CBUS_ACON, 0x00, 0x1b, 0x00, 12},
                        0x123
                )));
        Assert.assertTrue(new CbusAddress("-2700012").match(
                new CanMessage(
                        new int[]{CbusConstants.CBUS_ACOF, 0x00, 0x1b, 0x00, 12},
                        0x123
                )));
        Assert.assertTrue(new CbusAddress("X123456789ABCDEF0").match(
                new CanMessage(
                        new int[]{0x12, 0x34, 0x56, 0x78,
                            0x9A, 0xBC, 0xDE, 0xF0},
                        0x123
                )));
    }

    @Test
    public void testNEformMatch() {
        Assert.assertTrue(new CbusAddress("+N12E34").match(
                new CanMessage(
                        new int[]{CbusConstants.CBUS_ACON, 0x00, 12, 0x00, 34},
                        0x123
                )));

        Assert.assertTrue(new CbusAddress("+12E34").match(
                new CanMessage(
                        new int[]{CbusConstants.CBUS_ACON, 0x00, 12, 0x00, 34},
                        0x123
                )));

        Assert.assertTrue(new CbusAddress("12E34").match(
                new CanMessage(
                        new int[]{CbusConstants.CBUS_ACON, 0x00, 12, 0x00, 34},
                        0x123
                )));

        Assert.assertTrue(new CbusAddress("N12E34").match(
                new CanMessage(
                        new int[]{CbusConstants.CBUS_ACON, 0x00, 12, 0x00, 34},
                        0x123
                )));

        Assert.assertTrue(new CbusAddress("-N12E34").match(
                new CanMessage(
                        new int[]{CbusConstants.CBUS_ACOF, 0x00, 12, 0x00, 34},
                        0x123
                )));
        Assert.assertTrue(!new CbusAddress("-268").match(new CanMessage(
            new int[]{CbusConstants.CBUS_ASOF, 0x00, 0x00, 0x00},0x123 )));
        Assert.assertTrue(!new CbusAddress("+1").match(new CanMessage(
            new int[]{CbusConstants.CBUS_ASOF, 0x00, 0x00, 0x00, 0x01},0x123 )));
        Assert.assertTrue(!new CbusAddress("+2").match(new CanMessage(
            new int[]{CbusConstants.CBUS_ASON, 0x00, 0x00, 0x00, 0x01},0x123 )));
    }

    @Test
    public void testCbusIdNotParse() {
        Assert.assertTrue(!new CbusAddress("-12").match(
                new CanReply(
                        new int[]{CbusConstants.CBUS_ACON, 0x00, 0x00, 0x00, 12}
                )));
        Assert.assertTrue(!new CbusAddress("-268").match(
                new CanReply(
                        new int[]{CbusConstants.CBUS_ACOF, 0x00, 0x00, 0x00, 12}
                )));
        Assert.assertTrue(!new CbusAddress("-268").match(new CanReply(
            new int[]{CbusConstants.CBUS_ASOF, 0x00, 0x00, 0x00} )));
        Assert.assertTrue(!new CbusAddress("+1").match(new CanReply(
            new int[]{CbusConstants.CBUS_ASOF, 0x00, 0x00, 0x00, 0x01} )));
        Assert.assertTrue(!new CbusAddress("+2").match(new CanReply(
            new int[]{CbusConstants.CBUS_ASON, 0x00, 0x00, 0x00, 0x01} )));
    }

    @Test
    public void testPlusMinus() {
        Assert.assertTrue((new CbusAddress("+001")).equals(new CbusAddress("+001")));
        Assert.assertTrue((new CbusAddress("+001")).equals(new CbusAddress("x9800000001")));
        Assert.assertTrue((new CbusAddress("+200001")).equals(new CbusAddress("x9000020001")));
        Assert.assertTrue((new CbusAddress("-003")).equals(new CbusAddress("x9900000003")));
        Assert.assertTrue((new CbusAddress("-200003")).equals(new CbusAddress("x9100020003")));
    }

    @Test
    @SuppressWarnings("unlikely-arg-type")
    public void testEqualsOK() {
        Assert.assertTrue((new CbusAddress("+001")).equals(new CbusAddress("+001")));
        Assert.assertTrue((new CbusAddress("+001")).equals(new CbusAddress("x9800000001")));
        Assert.assertTrue((new CbusAddress("+200001")).equals(new CbusAddress("x9000020001")));
        Assert.assertFalse((new CbusAddress("+200001")).equals(null));
        Assert.assertFalse((new CbusAddress("+200001")).equals("foo"));
        Assert.assertFalse((new CbusAddress("+001")).equals(new CbusAddress("+002")));
        Assert.assertFalse((new CbusAddress("+N123E123")).equals(new CbusAddress("+N456E123")));
        Assert.assertFalse((new CbusAddress("+N123E123")).equals(new CbusAddress("+N123E456")));
        Assert.assertTrue(!new CbusAddress("-268").equals(
            new CanReply(new int[]{CbusConstants.CBUS_SNN, 0x00, 12})));
    }

    @Test
    public void testSplitCheckOK() {
        Assert.assertTrue(new CbusAddress("+001").checkSplit());
        Assert.assertTrue(new CbusAddress("-001").checkSplit());
        Assert.assertTrue(new CbusAddress("X0ABC").checkSplit());
        Assert.assertTrue(new CbusAddress("X0abc").checkSplit());
        Assert.assertTrue(new CbusAddress("Xa1b2c3").checkSplit());
        Assert.assertTrue(new CbusAddress("X123456789ABCDEF0").checkSplit());

        Assert.assertTrue(new CbusAddress("+001;+001").checkSplit());
        Assert.assertTrue(new CbusAddress("-001;+001").checkSplit());
        Assert.assertTrue(new CbusAddress("X0ABC;+001").checkSplit());
        Assert.assertTrue(new CbusAddress("X0abc;+001").checkSplit());
        Assert.assertTrue(new CbusAddress("Xa1b2c3;+001").checkSplit());
        Assert.assertTrue(new CbusAddress("X123456789ABCDEF0;+001").checkSplit());
    }

    @Test
    public void testMultiTermSplitCheckOK() {
        Assert.assertTrue(new CbusAddress("+1;+1").checkSplit());
        Assert.assertTrue(new CbusAddress("+N12e34;+1").checkSplit());
        Assert.assertTrue(new CbusAddress("+1;X1234").checkSplit());
        Assert.assertTrue(new CbusAddress("+1;N12e34").checkSplit());
        Assert.assertTrue(new CbusAddress("+1;+N12e34").checkSplit());
        Assert.assertTrue(new CbusAddress("+N12e34;+N12e35").checkSplit());
        Assert.assertTrue(new CbusAddress("X0A;+N15E6").checkSplit());
    }

    @Test
    public void testSplitCheckNotOK() {
        Assert.assertTrue(!new CbusAddress("+0A1").check());
        Assert.assertTrue(!new CbusAddress("- 001").check());
        Assert.assertTrue(!new CbusAddress("ABC").check());
        Assert.assertTrue(!new CbusAddress("Xprs0").check());

        Assert.assertTrue(!new CbusAddress("+1;+1;+1").checkSplit());
        Assert.assertTrue(!new CbusAddress("+1;;+1").checkSplit());
        Assert.assertTrue(!new CbusAddress("+001;").checkSplit());
        Assert.assertTrue(!new CbusAddress("-001;").checkSplit());
        Assert.assertTrue(!new CbusAddress("-001;;").checkSplit());
        Assert.assertTrue(!new CbusAddress("XABC;").checkSplit());
        Assert.assertTrue(!new CbusAddress("Xabc;").checkSplit());
        Assert.assertTrue(!new CbusAddress("Xa1b2c3;").checkSplit());
        Assert.assertTrue(!new CbusAddress("X123456789ABCDEF0;").checkSplit());

        Assert.assertTrue(!new CbusAddress("+001;Xprs0").checkSplit());
        Assert.assertTrue(!new CbusAddress("-001;Xprs0").checkSplit());
        Assert.assertTrue(!new CbusAddress("XABC;Xprs0").checkSplit());
        Assert.assertTrue(!new CbusAddress("Xabc;Xprs0").checkSplit());
        Assert.assertTrue(!new CbusAddress("Xa1b2c3;Xprs0").checkSplit());
        Assert.assertTrue(!new CbusAddress("X123456789ABCDEF0;Xprs0").checkSplit());
    }
    
    @Test
    public void testElements() {
        CbusAddress a = new CbusAddress("X9801D203A4");
        int[] testa = a.elements();
        Assert.assertTrue(testa[0] == 0x98);
        Assert.assertTrue(testa[1] == 0x01);
        Assert.assertTrue(testa[2] == 0xD2);
        Assert.assertTrue(testa[3] == 0x03);
        Assert.assertTrue(testa[4] == 0xA4);
    }        
    
    @Test
    public void testtoString() {
        CbusAddress a = new CbusAddress("X9801D203A4");
        Assert.assertTrue(a.toString() == "X9801D203A4");
        
        CbusAddress b = new CbusAddress("+N123E456");
        Assert.assertTrue(b.toString() == "+N123E456");
        
        CbusAddress c = new CbusAddress("-456");
        Assert.assertTrue(c.toString() == "-456");
    }

    @Test
    public void testtoCanonString() {
        CbusAddress a = new CbusAddress("X9801D203A4");
        Assert.assertEquals("hex form", "x9801D203A4",a.toCanonicalString());
        
        CbusAddress b = new CbusAddress("+N123E456");
        Assert.assertEquals("long form", "x90007B01C8",b.toCanonicalString());

        CbusAddress c = new CbusAddress("-321");
        Assert.assertEquals("short form", "x9900000141",c.toCanonicalString());
    }

    @Test
    public void testSplit() {
        CbusAddress a;
        CbusAddress[] v;

        a = new CbusAddress("+001");
        v = a.split();
        Assert.assertTrue(v.length == 1);
        Assert.assertTrue(new CbusAddress("+001").equals(v[0]));

        a = new CbusAddress("+001;-2");
        v = a.split();
        Assert.assertTrue(v.length == 2);
        Assert.assertTrue(new CbusAddress("+001").equals(v[0]));
        Assert.assertTrue(new CbusAddress("-2").equals(v[1]));
    }
    
    @Test
    public void testgetIncrement() {
        
        Assert.assertEquals("+N34E17;-N34E17","+N34E18;-N34E18",CbusAddress.getIncrement("+N34E17;-N34E17"));
        Assert.assertEquals("+N34E456;+N34E17","+N34E457;+N34E18",CbusAddress.getIncrement("+N34E456;+N34E17"));
        Assert.assertEquals("-N34E456;-N34E17","-N34E457;-N34E18",CbusAddress.getIncrement("-N34E456;-N34E17"));
        Assert.assertEquals("-N34E456;+N34E17","-N34E457;+N34E18",CbusAddress.getIncrement("-N34E456;+N34E17"));
    }
    
    @Test
    public void testvalidateSysName() {
        try {
            CbusAddress.validateSysName("+0");
            Assert.fail("Expected exception not thrown");
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("Event cannot be 0 in address: +0", ex.getMessage());
        }

        try {
            Assert.assertEquals("-0",null,CbusAddress.validateSysName("-0"));
            Assert.fail("Expected exception not thrown");
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("Event cannot be 0 in address: -0", ex.getMessage());
        }   

    }
    
    @Test
    public void testhashcode() {
        CbusAddress a = new CbusAddress("X9801D203A4");
        Assert.assertEquals("a hashcode is present",530,a.hashCode());
    }
    
    @Test
    public void testMatchRequest() {
        
        Assert.assertTrue("short request 12 match",new CbusAddress("+12").matchRequest(
            new CanReply(new int[]{CbusConstants.CBUS_ASRQ, 0x00, 0x00, 0x00, 12})));
        
        Assert.assertFalse("short request 13 no match",new CbusAddress("+13").matchRequest(
            new CanReply(new int[]{CbusConstants.CBUS_ASRQ, 0x00, 0x00, 0x00, 12})));
            
        Assert.assertFalse("Data element no match",new CbusAddress("+12").matchRequest(
            new CanReply(new int[]{CbusConstants.CBUS_ACON3, 0x00, 0x00, 0x00, 12, 0x12, 0x13})));
        
        Assert.assertFalse("ASON 12 no match",new CbusAddress("+12").matchRequest(
            new CanReply(new int[]{CbusConstants.CBUS_ASON, 0x00, 0x00, 0x00, 12})));
            
        Assert.assertTrue("long request N12E34 match",new CbusAddress("+N12E34").matchRequest(
            new CanReply(new int[]{CbusConstants.CBUS_AREQ, 0x00, 12, 0x00, 34})));
            
        Assert.assertFalse("long request N11E34 no match",new CbusAddress("+N11E34").matchRequest(
            new CanReply(new int[]{CbusConstants.CBUS_AREQ, 0x00, 12, 0x00, 34})));
            
        Assert.assertFalse("long request N123E35 no match",new CbusAddress("+N12E35").matchRequest(
            new CanReply(new int[]{CbusConstants.CBUS_AREQ, 0x00, 12, 0x00, 34})));
            
    }
    

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
    // private final static Logger log = LoggerFactory.getLogger(CbusAddressTest.class);
}
